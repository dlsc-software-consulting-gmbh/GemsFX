package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ExpandingTextArea;
import com.dlsc.gemsfx.util.StageManager;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ExpandingTextAreaApp extends GemApplication {

    @Override
    public void start(Stage stage) { super.start(stage);
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
        parent.setMinHeight(Region.USE_PREF_SIZE);

        parent.setFillWidth(false);
        parent.setAlignment(Pos.CENTER);
        parent.setPadding(new Insets(20, 0, 20, 0));

        ScrollPane scrollPane = new ScrollPane(parent);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane);

        stage.setTitle("Expanding Text Area");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        StageManager.install(stage, "expanding.text.area.app");

        stage.show();
    }

    @Override
    public String getDescription() {
        return """
                ### ExpandingTextArea
                
                A customized text area that will never show scrollbars but instead will
                grow as high as needed in order to completely fit its text inside of it.
                """;
    }

    public static void main(String[] args) {
        launch();
    }
}
