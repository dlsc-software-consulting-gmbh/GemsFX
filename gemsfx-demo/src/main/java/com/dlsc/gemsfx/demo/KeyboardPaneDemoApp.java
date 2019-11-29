package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.keyboard.KeyboardPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class KeyboardPaneDemoApp extends Application {

    @Override
    public void start(Stage stage) {
        final KeyboardPane keyboardPane = createKeyboardPane();
        keyboardPane.setPrefSize(1000, 900);
        Scene scene = new Scene(keyboardPane);
        stage.setScene(scene);
        stage.setTitle("JavaFX Keyboard Pane");
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    protected KeyboardPane createKeyboardPane() {
        KeyboardPane pane = new KeyboardPane();

        TextField textField = new TextField();
        textField.setPromptText("Focus text field to activate keyboard");

        TextArea textArea = new TextArea();

        TextField textField1 = new TextField();
        textField1.setPromptText("Focus text field to activate keyboard");

        TextField textField2 = new TextField();
        textField2.setPromptText("Focus text field to activate keyboard (supports auto-close)");

        TextField textField3 = new TextField();
        textField3.setPromptText("Focus text field to activate keyboard");

        TextField textField4 = new TextField();
        textField4.setPromptText("Focus text field to activate keyboard");

        TextField textField5 = new TextField();
        textField5.setPromptText("Focus text field to activate keyboard");

        TextField textField6 = new TextField();
        textField6.setPromptText("Focus text field to activate keyboard");

        VBox content = new VBox(20, textField, textArea, textField1, textField2, textField3, textField4, textField5, textField6);
        content.setPadding(new Insets(20));
        pane.setContent(content);

        pane.setAutoCloseStrategy(node -> node == textField2);

        return pane;
    }
}