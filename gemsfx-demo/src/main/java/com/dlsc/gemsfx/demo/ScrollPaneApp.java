package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.incubator.GemScrollPane;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ScrollPaneApp extends Application {

    @Override
    public void start(Stage stage) {

        ComboBox<String> comboBox = new ComboBox<>();

        VBox box = new VBox(5);
        VBox.setVgrow(box, Priority.ALWAYS);
        GemScrollPane scrollPane = new GemScrollPane(box);
        scrollPane.vvalueProperty().addListener(it -> System.out.println("vValue now: " + scrollPane.getVvalue()));
        scrollPane.setFitToWidth(true);

        for (int i = 0; i < 100; i++) {
            String id = "node" + i;
            Label label = new Label(id);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setStyle("-fx-padding: 10px 5px; -fx-background-color: orange; -fx-text-fill: black;");
            label.setId(id);
            box.getChildren().add(label);
            comboBox.getItems().add(label.getId());
        }

        comboBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        CheckBox animated = new CheckBox("Animated");
        animated.setSelected(true);

        HBox footer = new HBox(10, animated, comboBox);

        comboBox.valueProperty().addListener(it -> {
            Node lookup = box.lookup("#" + comboBox.getValue());
//            scrollPane.showNode(lookup, animated.isSelected());
            scrollPane.bottomNode(lookup);
        });


        VBox wrapper = new VBox(20, scrollPane, footer);
        wrapper.setPadding(new Insets(20));

        Scene scene = new Scene(wrapper);
        stage.setTitle("Drawer Demo");
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
