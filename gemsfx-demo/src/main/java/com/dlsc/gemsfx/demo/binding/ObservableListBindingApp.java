package com.dlsc.gemsfx.demo.binding;

import com.dlsc.gemsfx.binding.ObservableValuesListBinding;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
    private Label sumLabel1;

    @Override
    public void start(Stage primaryStage) {
        ObservableList<ObservableValue<Number>> observableValues = FXCollections.observableArrayList();
        observableValues.add(new SimpleIntegerProperty(10));
        observableValues.add(new SimpleIntegerProperty(20));

        ListView<ObservableValue<Number>> listView = new ListView<>(observableValues);

        sumLabel1 = new Label();
        // Create a binding that calculates the sum of all numbers
        ObservableValuesListBinding<Number, String> sumBinding = new ObservableValuesListBinding<>(
                observableValues,
                stream -> "Sum: " + stream.mapToInt(Number::intValue).sum()
        );
        sumLabel1.textProperty().bind(sumBinding);

        Label averageLabel = new Label();
        // Create a binding that calculates the average of all numbers
        ObservableValuesListBinding<Number, String> averageBinding = new ObservableValuesListBinding<>(
                observableValues,
                stream -> "Average: " + String.format("%.2f", stream.mapToInt(Number::intValue).average().orElse(0))
        );
        averageLabel.textProperty().bind(averageBinding);

        VBox statsBox = createStatsBox("ObservableValuesListBinding", sumLabel1, averageLabel);

        VBox root = new VBox(10, listView, createButtons(observableValues, listView), statsBox);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Observable Values List View Demo");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private VBox createStatsBox(String title, Label sumLabel, Label averageLabel) {
        Label titleLabel = new Label(title);
        HBox statsBox = new HBox(30, sumLabel, averageLabel);

        VBox vBox = new VBox(10, titleLabel, statsBox);
        vBox.setStyle("-fx-border-radius: 5px;-fx-border-color: lightgrey;-fx-border-width: 1px;-fx-alignment: center-left;-fx-padding: 5px;");

        return vBox;
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
                listView.refresh();
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
