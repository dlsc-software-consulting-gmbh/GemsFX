package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EmailField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Random;

public class EmailFieldApp extends GemApplication {

    private final Random random = new Random();

    @Override
    public void start(Stage stage) { super.start(stage);
        EmailField emailField = new EmailField();
        emailField.setPromptText("Enter an email address");

        CheckBox required = new CheckBox("Required");
        required.selectedProperty().bindBidirectional(emailField.requiredProperty());

        // When user types '@' in the email field, show a list of suggestions
        CheckBox autoCompletion = new CheckBox("Auto-Complete Domain");
        autoCompletion.selectedProperty().bindBidirectional(emailField.autoDomainCompletionEnabledProperty());

        CheckBox multipleAddresses = new CheckBox("Multiple addresses");
        multipleAddresses.selectedProperty().bindBidirectional(emailField.supportingMultipleAddressesProperty());

        CheckBox enableCustomCell = new CheckBox("Enable Custom Cell");
        enableCustomCell.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                emailField.setDomainListCellFactory(param -> new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            setText(item + "     (Custom)");
                            setTextFill(Color.rgb(random.nextInt(210), random.nextInt(210), random.nextInt(210)));
                            setGraphic(new FontIcon(MaterialDesign.MDI_EMAIL));
                        }
                    }
                });
            } else {
                emailField.setDomainListCellFactory(null);
            }
        });

        CheckBox showMailIcon = new CheckBox("Show Mail Icon");
        showMailIcon.selectedProperty().bindBidirectional(emailField.showMailIconProperty());

        CheckBox showValidationIcon = new CheckBox("Show Validation Icon");
        showValidationIcon.selectedProperty().bindBidirectional(emailField.showValidationIconProperty());

        TextField invalidTextField = new TextField(emailField.getInvalidText());
        invalidTextField.setPromptText("Invalid text for the tooltip");
        emailField.invalidTextProperty().bind(invalidTextField.textProperty());

        Label resultLabel = new Label("Entered address(es)");
        resultLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            if (emailField.isValid()) {
                if (emailField.isSupportingMultipleAddresses()) {
                    return String.join(", ", emailField.getMultipleEmailAddresses());
                }
                return emailField.getEmailAddress();
            } else {
                if (emailField.isSupportingMultipleAddresses()) {
                    return "Invalid: " + String.join(", ", emailField.getMultipleEmailAddresses());
                }
                return "Invalid " + emailField.getEmailAddress();
            }
        }, emailField.supportingMultipleAddressesProperty(), emailField.emailAddressProperty(), emailField.getMultipleEmailAddresses()));

        VBox optionsBox = new VBox(10, required, autoCompletion, multipleAddresses, enableCustomCell, showMailIcon, showValidationIcon, new Label("Text to show when invalid:"), invalidTextField);

        VBox box = new VBox(10, new Label("Enter an email address:"), emailField, resultLabel, optionsBox);
        box.setPrefWidth(300);
        box.setPadding(new Insets(10));

        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("Email Field");

        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
