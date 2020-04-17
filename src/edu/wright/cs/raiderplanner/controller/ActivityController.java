/*
 * Copyright (C) 2017 - Benjamin Dickson, Andrew Odintsov, Zilvinas Ceikauskas,
 * Bijan Ghasemi Afshar
 *
 * Copyright (C) 2018 - Ian Mahaffy, Gage Berghoff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package edu.wright.cs.raiderplanner.controller;

import edu.wright.cs.raiderplanner.controller.MainController;
import edu.wright.cs.raiderplanner.model.Activity;
import edu.wright.cs.raiderplanner.model.QuantityType;
import edu.wright.cs.raiderplanner.model.Task;
import edu.wright.cs.raiderplanner.view.UiManager;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Scanner;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Handle actions associated with the GUI window for creating new activities.
 * This includes validating the data contained in the various text fields,
 * retrieving the validated data, and storing the submitted data to the proper
 * objects.
 *
 * @author Zilvinas Ceikauskas
 */

public class ActivityController extends AccountController implements Initializable {
	private Activity activity;
	private boolean success = false;

	/**
	 * Default constructor.
	 */
	public ActivityController() {
	}

	/**
	 * Constructor for an ActivityController with an existing Activity.
	 *
	 * @param activity the activity which will be managed by the new controller
	 */
	public ActivityController(Activity activity) {
		this.activity = activity;
	}

	/**
	 * Returns the Activity object being managed by this controller.
	 *
	 * @return the Activity object being managed by this controller.
	 */
	public Activity getActivity() {
		return this.activity;
	}

	/**
	 * Returns true if the last submit operation succeeded, false otherwise.
	 *
	 * @return true if the last submit operation succeeded, false otherwise.
	 */
	@Override
	public boolean isSuccess() {
		return success;
	}

	// Panes:
	@FXML private GridPane pane;

	// Buttons:
	@FXML private Button submit;
	@FXML private Button addTask;
	@FXML private Button removeTask;

	// Text:
	@FXML private TextField name;
	@FXML private TextArea details;
	@FXML private ComboBox<String> quantityType;
	@FXML private TextField quantity;
	@FXML private TextField duration;
	@FXML private DatePicker date;

	// Labels:
	@FXML private Label title;

	// Lists:
	@FXML private ListView<Task> tasks;

	// Tooltips:
	@FXML private Label headingTooltip;



	/**
	 * Handle changes to the input fields.
	 * Checks inputs, and unlocks the submit button if inputs are valid
	 */
	public void handleChange() {
		if (!this.name.getText().trim().isEmpty()
				&& !this.quantity.getText().trim().isEmpty()
				&& MainController.isNumeric(this.quantity.getText())
				&& MainController.isInteger(this.quantity.getText())
				&& Integer.parseInt(this.quantity.getText()) >= 0
				&& !this.date.getValue().isBefore(LocalDate.now())
				&& !this.duration.getText().trim().isEmpty()
				&& MainController.isNumeric(this.duration.getText())
				&& MainController.isInteger(this.duration.getText())
				&& Integer.parseInt(this.duration.getText()) >= 0
				&& this.quantityType.getSelectionModel().getSelectedIndex() != -1
				&& this.tasks.getItems().size() > 0) {
			this.submit.setDisable(false);
		}
	}

	/**
	 * Validate data in the Duration field. If the isNumeric() method is called from
	 * the MainController Class and is false (checks if the text parameter is a double)
	 * or the Integer value of the text is less than 0. The duration TextField's
	 * border is set to red and the submit button is disabled. Otherwise the duration's
	 * style is set so that it is cohesive and handleChange() is called.
	 */
	public void validateDuration() {
		if (!MainController.isNumeric(this.duration.getText())) {
			this.duration.setStyle("-fx-text-box-border:red;");
			this.duration.setTooltip(new Tooltip("Duration must be numeric"));
			this.submit.setDisable(true);
		} else if (!MainController.isInteger(this.duration.getText())) {
			this.duration.setStyle("-fx-text-box-border:red;");
			this.duration.setTooltip(new Tooltip("Duration must be a whole number"));
			this.submit.setDisable(true);
		} else if (Integer.parseInt(this.duration.getText()) < 0) {
			this.duration.setStyle("-fx-text-box-border:red;");
			this.duration.setTooltip(new Tooltip("Duration cannot be negative"));
			this.submit.setDisable(true);
		} else {
			this.duration.setStyle("");
			this.duration.setTooltip(null);
			this.handleChange();
		}
	}

	/**
	 * Validate data in the Quantity field. If the isNumeric() method is called from
	 * the MainController Class and is false (checks if the text parameter is a double)
	 * or the Integer value of the text is less than 0. The quantity TextField's
	 * border is set to red and the submit button is disabled. Otherwise the quantity's
	 * style is set so that it is cohesive and handleChange() is called.
	 */
	public void validateQuantity() {
		if (!MainController.isNumeric(this.quantity.getText())) {
			this.quantity.setStyle("-fx-text-box-border:red;");
			this.quantity.setTooltip(new Tooltip("Quantity must be numeric"));
			this.submit.setDisable(true);
		} else if (!MainController.isInteger(this.quantity.getText())) {
			this.quantity.setStyle("-fx-text-box-border:red;");
			this.quantity.setTooltip(new Tooltip("Quantity must be a whole number"));
			this.submit.setDisable(true);
		}  else if (Integer.parseInt(this.quantity.getText()) < 0) {
			this.quantity.setStyle("-fx-text-box-border:red;");
			this.quantity.setTooltip(new Tooltip("Quantity cannot be negative"));
			this.submit.setDisable(true);
		} else {
			this.quantity.setStyle("");
			this.quantity.setTooltip(null);
			this.handleChange();
		}
	}

	/**
	 * Validate data in the Date field. If the date is before the current date, the
	 * DatePicker's border is set to red and the submit button is disabled. Otherwise
	 * the date's style is set so that it is cohesive and the handleChange() is called.
	 */
	public void validateDate() {
		if (this.date.getValue().isBefore(LocalDate.now())) {
			this.date.setStyle("-fx-border-color:red;");
			this.date.setTooltip(new Tooltip("Date cannot be in the past"));
			this.submit.setDisable(true);
		} else {
			this.date.setStyle("");
			this.date.setTooltip(null);
			this.handleChange();
		}
	}

	/**
	 * Handle the 'Add Task' button action.
	 */
	public void addTask() {
		// Table items:
		ObservableList<Task> list = FXCollections
				.observableArrayList(MainController.getSpc().getCurrentTasks());
		// created a task to allow for the Add Activity UI to work
		// properly until tasks are able to be added
		String name = "Study";
		String details = "Study Notes";
		LocalDate deadline = LocalDate.of(2019, 12, 12);
		int weighting = 5;
		String type = "Study";
		Task task = new Task(name, details, deadline, weighting, type);
		list.add(task);
		// end of task creation for testing purposes
		list.removeAll(this.tasks.getItems());
		if (this.activity != null) {
			list.removeAll(this.activity.getTasks());
		}
		list.removeIf(e -> !e.dependenciesComplete());
		// =================

		// Parse selected Tasks:
		this.tasks.getItems().addAll(TaskController.taskSelectionWindow(list));
		// =================
	}

	/**
	 * Submit the form and create a new Activity.
	 */



	@Override
	public void handleSubmit() {
		if (this.activity == null) {
			this.activity = new Activity(this.name.getText(),
					this.details.getText(), this.date.getValue(),
					Integer.parseInt(this.duration.getText()),
					Integer.parseInt(this.quantity.getText()),
					this.quantityType.getValue());

			this.activity.addTasks(this.tasks.getItems());
		}

		this.success = true;
		Stage stage = (Stage) this.submit.getScene().getWindow();
		stage.close();
		System.out.println("Please enter email address so we can send you a notification:");
		Scanner sc = new Scanner(System.in);
		String uemail = sc.nextLine();
		final String username = "raiderplanner3120@gmail.com";
		final String password = "Ngbjss3120";
		final String Email_From = "raiderplanner3120@gmail.com";
		final String Email_To = uemail;
		final String Email_Subject = this.name.getText();
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", "smtp.gmail.com");
		properties.put("mail.smtp.port", "587");
		properties.put("mail.smtp.ssl.trust", "smtp.gmail.com");
		Session session = Session.getDefaultInstance(properties,new javax.mail.Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username,password);
	 	}
		});
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(Email_To));
			message.setSubject(Email_Subject);
			MimeMultipart part = new MimeMultipart();
			String sb = "<head>" + "<style type=\"text/css\">" + " .red { color: #f00; }" + "</style>"
					+ "</head>" + "<img src=\"cid:image\">" + "<h1 class=\red\">"
					+ message.getSubject() + "</h1>"  +  "<p>" + "Hello, you have "
					+ "created/added a new task: " + this.details.getText() + "</p>" + "<footer>"
					+ "RaiderPlanner@CopyRight 2020" + "</footer>";
			BodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(sb, "text/html; charset=utf-8");
			part.addBodyPart(messageBodyPart);
			messageBodyPart = new MimeBodyPart();
			DataSource fds = new FileDataSource("/Users/Twili/git/RaiderPlanner/src/edu/"
					+ "wright/cs/raiderplanner/content/raiderlogo.png");
			messageBodyPart.setDataHandler(new DataHandler(fds));
			messageBodyPart.setHeader("Content-ID", "<image>");
			part.addBodyPart(messageBodyPart);
			message.setContent(part);
			Transport.send(message);
			System.out.println("message sent");

		} catch (MessagingException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Binds properties on the quit button as well as sets the button actions for exiting.
	 */


	@Override
	public void handleQuit() {
		Stage stage = (Stage) this.submit.getScene().getWindow();
		stage.close();
	}

	/**
	 * Limits number of characters typed in all textArea/textfields.
	 */
	public void limitTextInput() {
		this.details.setTextFormatter(new TextFormatter<String>(change
				-> change.getControlNewText().length() <= 400 ? change : null));
		this.name.setTextFormatter(new TextFormatter<String>(change
				-> change.getControlNewText().length() <= 100 ? change : null));
		this.quantity.setTextFormatter(new TextFormatter<String>(change
				-> change.getControlNewText().length() <= 50 ? change : null));
		this.duration.setTextFormatter(new TextFormatter<String>(change
				-> change.getControlNewText().length() <= 50 ? change : null));
	}


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.quantityType.getItems().addAll(QuantityType.listOfNames());
		this.date.setValue(LocalDate.now());

		// TODO draggable tasks (same as requirement)
		// ListChangeListener:
		this.tasks.getItems().addListener((ListChangeListener<Task>) c -> handleChange());
		// =================

		// Bind properties on buttons:
		this.removeTask.disableProperty().bind(new BooleanBinding() {
			{
				bind(tasks.getSelectionModel().getSelectedItems());
			}

			@Override
			protected boolean computeValue() {
				return !(tasks.getItems().size() > 0
						&& tasks.getSelectionModel().getSelectedItem() != null);
			}
		});
		// =================

		// Button actions:
		this.removeTask.setOnAction(e -> {
			if (UiManager.confirm("Are you sure you want to remove this Task from the list?")) {
				Task task = this.tasks.getSelectionModel().getSelectedItem();
				this.tasks.getItems().remove(task);
				if (this.activity != null) {
					this.activity.removeTask(task);
				}
			}
		});

		this.tasks.setCellFactory(e -> {
			ListCell<Task> cell = new ListCell<Task>() {
				@Override
				protected void updateItem(final Task item, final boolean empty) {
					super.updateItem(item, empty);
					// If completed, mark:
					if (!empty && item != null) {
						setText(item.toString());
						if (item.isCheckedComplete()) {
							this.getStyleClass().add("current-item");
						}
					} else {
						setText(null);
						this.getStyleClass().remove("current-item");
					}
				}
			};
			return cell;
		});
		// =================

		// Handle Activity details:
		if (this.activity != null) {
			// Disable/modify elements:
			this.title.setText("Activity");
			this.addTask.setVisible(false);
			this.removeTask.setVisible(false);
			this.name.setEditable(false);
			this.details.setEditable(false);
			this.duration.setEditable(false);
			this.quantity.setEditable(false);
			this.date.setDisable(true);
			this.quantityType.setDisable(true);
			// =================

			// Fill in data:
			this.name.setText(this.activity.getName());
			this.details.setText(this.activity.getDetails().getAsString());
			this.duration.setText(Integer.toString(this.activity.getDuration()));
			this.quantity.setText(Integer.toString(this.activity.getActivityQuantity()));
			this.date.setValue(this.activity.getDate().toInstant()
					.atZone(ZoneId.systemDefault()).toLocalDate());
			this.quantityType.getSelectionModel().select(this.activity.getType().getName());
			this.tasks.getItems().addAll(this.activity.getTasks());
			// =================
		} else {
			this.handleChange();
		}
		// =================

		// Initialize Tooltips:
		headingTooltip.setTooltip(new Tooltip("An Activity is something that you need to do "
				+ "and\nfeatures a duration, quantity, quantity type, date, and tasks."));
		Tooltip nameTooltip = new Tooltip("Enter the name for your new activity.");
		name.setTooltip(nameTooltip);
		Tooltip dateTooltip = new Tooltip("Enter the date to complete this activity.");
		date.setTooltip(dateTooltip);
		Tooltip durationTooltip = new Tooltip("Enter how long this Activity will take you "
				+ "to complete.");
		duration.setTooltip(durationTooltip);
		Tooltip quantityTooltip = new Tooltip("Enter how many times this activity needs to "
				+ "be completed.");
		quantity.setTooltip(quantityTooltip);
		Tooltip taskTooltip = new Tooltip("Add tasks to your activity to help stay organized "
				+ "and efficient.");
		tasks.setTooltip(taskTooltip);
		Tooltip detailsTooltip = new Tooltip("Enter any additional information for this "
				+ "activity.");
		details.setTooltip(detailsTooltip);

		Platform.runLater(() -> this.pane.requestFocus());
	}
}
