package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CustomLabel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

public class CustomLabelApp extends Application {

    @Override
    public void start(Stage stage) {
        CustomLabel customLabel = new CustomLabel("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere, cubilia sagittis egestas pharetra sociis montes nullam netus erat. Fusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a. Mauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate." +
                "Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.");
        customLabel.setPrefWidth(400);
        HBox.setHgrow(customLabel, Priority.ALWAYS);

        TextArea textField = new TextArea();
        textField.setWrapText(true);
        textField.textProperty().bindBidirectional(customLabel.textProperty());

        Label selectedText = new Label();
        selectedText.setWrapText(true);
        selectedText.textProperty().bind(customLabel.selectedTextProperty());

        Button clear = new Button("Clear Selection");
        clear.setOnAction(evt -> customLabel.clearSelection());

        Button copy = new Button("Copy Selection to Clipboard");
        copy.setOnAction(evt -> customLabel.copySelectionToClipboard());

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(75);

        GridPane controls = new GridPane();
        controls.setMinWidth(400);
        controls.setPrefWidth(400);
        controls.setMaxWidth(400);
        controls.getColumnConstraints().add(col1);
        controls.getColumnConstraints().add(col2);
        controls.setVgap(10);
        controls.setHgap(10);
        controls.setPadding(new Insets(10));

        controls.add(new Label("Text"), 0, 0);
        controls.add(textField, 1, 0);

        controls.add(new Label("Selected Text"), 0, 1);
        controls.add(selectedText, 1, 1);

        controls.add(clear, 1, 2);
        controls.add(copy, 1, 3);

        HBox hBox = new HBox(10, customLabel, controls);
        hBox.setPadding(new Insets(20));

        stage.setScene(new Scene(hBox));
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Custom Label");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}