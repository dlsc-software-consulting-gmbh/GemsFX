package com.dlsc.gemsfx.demo.binding;

import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.binding.NestedListChangeTracker;
import com.dlsc.gemsfx.binding.TransformedNestedListBinding;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
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
public class NestedListChangeTrackerApp extends Application {

    private final Random random = new Random();
    private ListView<ObservableList<Number>> listView;
    private ObservableList<ObservableList<Number>> scores;
    private NestedListChangeTracker<Number> changeTracker;
    private Label sumLabel2;
    private TransformedNestedListBinding<Number, Integer> sumBinding;
    private Label sumLabel;

    @Override
    public void start(Stage primaryStage) {
        scores = FXCollections.observableArrayList();
        scores.addAll(FXCollections.observableArrayList(80, 90, 85), FXCollections.observableArrayList(70, 75, 60));

        listView = createListView();
        HBox buttonBox = createButtonBox();

        sumLabel = new Label();
        sumBinding = new TransformedNestedListBinding<>(
                scores,
                list -> list.stream().flatMapToInt(innerList -> innerList.stream().mapToInt(Number::intValue)).sum()
        );
        sumLabel.textProperty().bind(sumBinding.asString("SumBinding:\t%d"));
        HBox box1 = createBindingBox();

        sumLabel2 = new Label("ChangeTracker");
        changeTracker = new NestedListChangeTracker<>(scores);
        changeTracker.setOnChanged(list -> {
            int sum = list.stream().flatMapToInt(innerList -> innerList.stream().mapToInt(Number::intValue)).sum();
            sumLabel2.setText("ChangeTracker:\t" + sum);
            System.out.println("NestedListChangeTracker: changed");
        });
        HBox box2 = createBox2();

        VBox root = new VBox(20, listView, buttonBox, box1, box2);
        root.setPadding(new Insets(15));
        Scene scene = new Scene(root);

        primaryStage.setTitle("Student Score History");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.show();
    }

    private HBox createBox2() {
        Button disposeButton = new Button("Dispose");
        disposeButton.setOnAction(e -> {
            disposeButton.setDisable(true);
            changeTracker.dispose();
            sumLabel2.setText("ChangeTracker:\tdisposed");
        });
        HBox hBox = new HBox(sumLabel2, new Spacer(), disposeButton);
        hBox.setStyle("-fx-border-radius: 5px;-fx-border-color: lightgrey;-fx-border-width: 1px;-fx-alignment: center-left;-fx-padding: 5px;");
        return hBox;
    }

    private HBox createBindingBox() {
        Button disposeButton = new Button("Dispose");
        disposeButton.setOnAction(e -> {
            disposeButton.setDisable(true);
            sumBinding.dispose();
            sumLabel.textProperty().unbind();
            sumLabel.setText("SumBinding:\tdisposed");
        });

        HBox hBox = new HBox(sumLabel, new Spacer(), disposeButton);
        hBox.setStyle("-fx-border-radius: 5px;-fx-border-color: lightgrey;-fx-border-width: 1px;-fx-alignment: center-left;-fx-padding: 5px;");
        return hBox;
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
