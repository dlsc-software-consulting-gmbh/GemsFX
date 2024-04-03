package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EmailField;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmailFieldApp extends Application {

    @Override
    public void start(Stage stage) {
        EmailField view = new EmailField();

        CheckBox required = new CheckBox("Required");
        required.selectedProperty().bindBidirectional(view.requiredProperty());

        CheckBox showMailIcon = new CheckBox("Show Mail Icon");
        showMailIcon.selectedProperty().bindBidirectional(view.showMailIconProperty());

        CheckBox showValidationIcon = new CheckBox("Show Validation Icon");
        showValidationIcon.selectedProperty().bindBidirectional(view.showValidationIconProperty());

        TextField invalidTextField = new TextField(view.getInvalidText());
        invalidTextField.setPromptText("Invalid text for the tooltip");
        view.invalidTextProperty().bind(invalidTextField.textProperty());

        VBox topBox = new VBox(10, required, showMailIcon, showValidationIcon, new Label("Text to show when invalid:"), invalidTextField);

        VBox box = new VBox(20, topBox, view);
        box.setPadding(new Insets(10));
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle("Email Field");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
