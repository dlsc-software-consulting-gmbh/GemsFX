package com.dlsc.gemsfx.demo.binding;

import com.dlsc.gemsfx.binding.TransformedFlattenedNestedListStreamBinding;
import com.dlsc.gemsfx.binding.TransformedNestedListBinding;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Random;
import java.util.stream.Collectors;

/**
 * This demo shows how to use the {@link TransformedNestedListBinding} class to create a binding that aggregates
 * values from a nested list structure. In this example, we have a list of student scores where each
 * student has a list of scores. We create a binding that calculates the total and average score of
 * all students.
 */
public class NestedListBindingApp extends Application {

    private final Random random = new Random();
    private ListView<ObservableList<Number>> listView;
    private ObservableList<ObservableList<Number>> scores;

    @Override
    public void start(Stage primaryStage) {
        scores = FXCollections.observableArrayList();
        scores.addAll(FXCollections.observableArrayList(80, 90, 85), FXCollections.observableArrayList(70, 75, 60));

        listView = createListView();
        HBox buttonBox = createButtonBox();

        Label totalLabel = new Label();
        // Create a binding that calculates the total sum of all scores
        TransformedNestedListBinding<Number, Integer> totalSumBinding = new TransformedNestedListBinding<>(
                scores,
                list -> list.stream().flatMapToInt(innerList -> innerList.stream().mapToInt(Number::intValue)).sum()
        );
        totalLabel.textProperty().bind(totalSumBinding.asString("Total: %d"));

        Label averageLabel = new Label();
        // Create a binding that calculates the average of all scores
        TransformedNestedListBinding<Number, Double> averageBinding = new TransformedNestedListBinding<>(
                scores,
                list -> list.stream().flatMapToDouble(innerList -> innerList.stream().mapToDouble(Number::doubleValue)).average().orElse(0)
        );
        averageLabel.textProperty().bind(averageBinding.asString("Average: %.2f"));

        Label totalLabel2 = new Label();
        TransformedFlattenedNestedListStreamBinding<Number, Integer> totalSumBinding2 = new TransformedFlattenedNestedListStreamBinding<>(
                scores,
                stream -> stream.mapToInt(Number::intValue).sum()
        );
        totalLabel2.textProperty().bind(totalSumBinding2.asString("Total: %d"));

        Label averageLabel2 = new Label();
        TransformedFlattenedNestedListStreamBinding<Number, Double> averageBinding2 = new TransformedFlattenedNestedListStreamBinding<>(
                scores,
                stream -> stream.mapToDouble(Number::doubleValue).average().orElse(0)
        );
        averageLabel2.textProperty().bind(averageBinding2.asString("Average: %.2f"));

        VBox statsBox = createStatsBox("TransformedNestedListBinding", totalLabel, averageLabel);
        VBox statsBox2 = createStatsBox("TransformedFlattenedNestedListStreamBinding", totalLabel2, averageLabel2);
        VBox root = new VBox(10, listView, buttonBox, statsBox, statsBox2);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Student Score History");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private VBox createStatsBox(String title, Label totalLabel, Label averageLabel) {
        Label titleLabel = new Label(title);
        HBox innerBox = new HBox(30, totalLabel, averageLabel);

        VBox vBox = new VBox(10, titleLabel, innerBox);
        vBox.setAlignment(Pos.CENTER_LEFT);
        vBox.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1px; -fx-border-radius: 5px;-fx-padding: 5px;-fx-background-color: #f9f9f9;");
        return vBox;
    }

    private HBox createButtonBox() {
        // Button to add scores
        Button addButton = new Button("Add Scores");
        addButton.setOnAction(e -> scores.add(FXCollections.observableArrayList(randomScore(), randomScore(), randomScore())));

        // Button to remove selected scores
        Button removeButton = new Button("Remove Selected Scores");
        removeButton.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
        removeButton.setOnAction(e -> {
            ObservableList<Number> selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) scores.remove(selected);
        });

        // Button to update selected scores
        Button updateButton = new Button("Update Selected Scores");
        updateButton.disableProperty().bind(listView.getSelectionModel().selectedItemProperty().isNull());
        updateButton.setOnAction(e -> {
            ObservableList<Number> selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                int itemCount = random.nextInt(1, 6);
                selected.setAll(FXCollections.observableArrayList(
                        random.ints(itemCount, 50, 100).boxed().collect(Collectors.toList())
                ));
                listView.refresh();
            }
        });
        return new HBox(10, addButton, removeButton, updateButton);
    }

    private ListView<ObservableList<Number>> createListView() {
        ListView<ObservableList<Number>> listView = new ListView<>(scores);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ObservableList<Number> item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.stream().map(Object::toString).collect(Collectors.joining(", ")));
                }
            }
        });
        return listView;
    }

    private int randomScore() {
        return random.nextInt(51) + 50;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
