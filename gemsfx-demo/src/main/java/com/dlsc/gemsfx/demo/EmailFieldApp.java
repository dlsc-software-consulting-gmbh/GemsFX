package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EmailField;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EmailFieldApp extends Application {

    @Override
    public void start(Stage stage) {
        EmailField view = new EmailField();
        CheckBox required = new CheckBox("Required");

        required.selectedProperty().bindBidirectional(view.requiredProperty());

        VBox box = new VBox(20, required, view);

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
