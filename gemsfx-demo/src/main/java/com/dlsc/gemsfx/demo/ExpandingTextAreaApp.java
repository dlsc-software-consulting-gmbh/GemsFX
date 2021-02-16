package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ExpandingTextArea;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExpandingTextAreaApp extends Application {

    @Override
    public void start(Stage stage) {
        TextField textField = new TextField();
        textField.setPromptText("Regular text field");
        textField.setMaxWidth(400);
        textField.setPrefWidth(400);

        ExpandingTextArea expandingTextArea = new ExpandingTextArea();
        expandingTextArea.setPromptText("Expanding text area");
        expandingTextArea.setMaxWidth(400);

        TextArea textArea = new TextArea();
        textArea.setPromptText("Regular text area");
        textArea.setMaxWidth(400);

        VBox parent = new VBox(20, textField, expandingTextArea, textArea);

        parent.setFillWidth(false);
        parent.setAlignment(Pos.CENTER);
        parent.setPadding(new Insets(100, 0, 100, 0));

        Scene scene = new Scene(parent);
        stage.setTitle("Expanding Text Area");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
