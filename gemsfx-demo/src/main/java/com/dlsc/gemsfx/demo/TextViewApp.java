package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.TextView;
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
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class TextViewApp extends Application {

    @Override
    public void start(Stage stage) {
        TextView textView = new TextView("Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna pos-uere!\nCubilia sagittis egestas pharetra sociis montes nullam netus erat.\n\nFusce mauris condimentum neque morbi nunc ligula pretium vehicula nulla, platea dictum mus sapien pulvinar eget porta mi praesent, orci hac dignissim suscipit imperdiet sem per a.\nMauris pellentesque dui vitae velit netus venenatis diam felis urna ultrices, potenti pretium sociosqu eros dictumst dis aenean nibh cursus, leo sagittis integer nullam malesuada aliquet et metus vulputate. Interdum facilisis congue ac proin libero mus ullamcorper mauris leo imperdiet eleifend porta, posuere dignissim erat tincidunt vehicula habitant taciti porttitor scelerisque laoreet neque. Habitant etiam cubilia tempor inceptos ad aptent est et varius, vitae imperdiet phasellus feugiat class purus curabitur ullamcorper maecenas, venenatis mollis fusce cras leo eros metus proin. Fusce aenean sociosqu dis habitant mi sapien inceptos, orci lacinia nisi nascetur convallis at erat sociis, purus integer arcu feugiat sollicitudin libero.\n\nLorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. Lorem ipsum dolor sit amet consectetur adipiscing elit nunc hendrerit purus, nisi dapibus primis nibh volutpat fringilla ad nisl urna posuere. A");

        textView.setPrefWidth(400);
        HBox.setHgrow(textView, Priority.ALWAYS);

        TextArea textField = new TextArea();
        textField.setWrapText(true);
        textField.textProperty().bindBidirectional(textView.textProperty());

        Label selectedText = new Label();
        selectedText.setWrapText(true);
        selectedText.textProperty().bind(textView.selectedTextProperty());

        Button copyAll = new Button("Copy entire text to clipboard");
        copyAll.setOnAction(evt -> textView.copyAll());

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
        controls.add(copyAll, 1, 2);

        HBox hBox = new HBox(10, textView, controls);
        hBox.setPadding(new Insets(20));

        Scene scene = new Scene(hBox);
        scene.focusOwnerProperty().addListener(it -> System.out.println("focus owner: " + scene.getFocusOwner()));
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.sizeToScene();
        stage.setTitle("Text View");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}