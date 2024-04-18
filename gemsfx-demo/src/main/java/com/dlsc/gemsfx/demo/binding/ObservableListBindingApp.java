package com.dlsc.gemsfx.demo.binding;

import com.dlsc.gemsfx.binding.ObservableValuesListBinding;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;

/**
 * This demo shows how to use the {@link ObservableValuesListBinding} class to create a binding that
 * calculates the sum and average of a list of observable values.
 */
public class ObservableListBindingApp extends Application {

    private final Random random = new Random();

    @Override
    public void start(Stage primaryStage) {
        ObservableList<ObservableValue<Number>> observableValues = FXCollections.observableArrayList();

        ListView<ObservableValue<Number>> listView = new ListView<>(observableValues);

        Label sumLabel = new Label();
        // Create a binding that calculates the sum of all numbers
        ObservableValuesListBinding<Number, String> sumBinding = new ObservableValuesListBinding<>(
                observableValues,
                list -> "Sum: " + list.stream().mapToInt(Number::intValue).sum()
        );
        sumLabel.textProperty().bind(sumBinding);

        Label averageLabel = new Label();
        // Create a binding that calculates the average of all numbers
        ObservableValuesListBinding<Number, String> averageBinding = new ObservableValuesListBinding<>(
                observableValues,
                list -> "Average: " + String.format("%.2f", list.stream().mapToInt(Number::intValue).average().orElse(0))
        );
        averageLabel.textProperty().bind(averageBinding);

        // Layout setup
        HBox statsBox = new HBox(20, sumLabel, averageLabel);
        VBox root = new VBox(10, listView, statsBox, createButtons(observableValues, listView));
        root.setPadding(new javafx.geometry.Insets(15));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Observable Values List View Demo");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private HBox createButtons(ObservableList<ObservableValue<Number>> observableValues, ListView<ObservableValue<Number>> listView) {
        Button addButton = new Button("Add Random Number");
        addButton.setOnAction(e -> observableValues.add(new SimpleIntegerProperty(random.nextInt(100) + 1)));

        Button updateButton = new Button("Update Selected Number");
        updateButton.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
        updateButton.setOnAction(e -> {
            ObservableValue<Number> selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ((SimpleIntegerProperty) selected).set(random.nextInt(100) + 1);
            }
        });

        Button removeButton = new Button("Remove Selected Number");
        removeButton.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
        removeButton.setOnAction(e -> {
            ObservableValue<Number> selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                observableValues.remove(selected);
            }
        });
        return new HBox(10, addButton, updateButton, removeButton);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
