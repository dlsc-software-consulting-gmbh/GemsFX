package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.HistoryButton;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.InMemoryHistoryManager;
import com.dlsc.gemsfx.util.PreferencesHistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.List;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * A demo application that shows how to use {@link HistoryButton} and {@link HistoryManager}.
 */
public class HistoryManagerApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.getTabs().addAll(
                new Tab("In-Memory History Manager", inMemoryDemmo()),
                new Tab("String History Manager", stringHistoryDemo()),
                new Tab("Preferences History Manager)", prefsDemo())
        );

        primaryStage.setScene(new Scene(tabPane, 800, 600));
        primaryStage.setTitle("History Manager Demo");
        primaryStage.show();
    }

    private Node inMemoryDemmo() {
        TextField textField = new TextField();

        HistoryButton<String> historyButton = new HistoryButton<>(textField);

        InMemoryHistoryManager<String> historyManager = new InMemoryHistoryManager<>();
        historyButton.setHistoryManager(historyManager);

        historyButton.setOnItemSelected(item -> {
            historyButton.hidePopup();
            textField.setText(item);
        });

        // Add history item to the history when the enter key is pressed.
        textField.setOnKeyPressed(e -> {
            historyButton.hidePopup();
            if (e.getCode() == KeyCode.ENTER) {
                historyManager.add(textField.getText());
            }
        });

        HBox box = new HBox(5, textField, historyButton);
        box.setAlignment(Pos.CENTER);
        Label label = new Label("""
                1. Tips: Press Enter to add the text to the history.
                2. Click the history button to show the history popup.
                3. This is a simple case, since the preferencesKey is not set, it will not be persisted, just saved in memory.
                """);
        label.setStyle("-fx-text-fill: #666;");

        VBox vbox = new VBox(50, label, box);
        vbox.setAlignment(Pos.CENTER);
        return vbox;
    }

    /**
     * Creates a text field with a history button.
     */
    private Node stringHistoryDemo() {
        TextField textField = new TextField();

        StringHistoryManager historyManager = new StringHistoryManager(Preferences.userNodeForPackage(HistoryManagerApp.class), "advanced-demo");
        historyManager.setMaxHistorySize(10);

        // Optional: if the history is empty, set some default values
        if (historyManager.getAllUnmodifiable().isEmpty()) {
            historyManager.set(List.of("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"));
        }

        HistoryButton<String> historyButton = new HistoryButton<>();
        historyButton.setHistoryManager(historyManager);
        historyButton.setOnItemSelected(item -> {
            historyButton.hidePopup();
            textField.setText(item);
        });

        // create the left node;
        VBox leftBox = new VBox();
        Label historyLabel = new Label("History");
        historyLabel.setRotate(90);
        Group group = new Group(historyLabel);

        Button clearAll = new Button("", new FontIcon(MaterialDesign.MDI_DELETE));
        clearAll.setPadding(new Insets(2, 4, 2, 4));
        clearAll.setOnAction(e -> {
            historyManager.clear();
            historyButton.hidePopup();
        });
        clearAll.managedProperty().bind(clearAll.visibleProperty());
        clearAll.visibleProperty().bind(Bindings.isNotEmpty(historyManager.getAllUnmodifiable()));

        leftBox.getChildren().addAll(group, new Spacer(), clearAll);
        leftBox.setAlignment(Pos.CENTER);
        leftBox.setPadding(new Insets(10, 5, 5, 5));
        leftBox.setPrefWidth(35);
        leftBox.setStyle("-fx-background-color: #f4f4f4;");
        historyButton.setListDecorationLeft(leftBox);

        // add history item to the history when the enter key is pressed.
        textField.setOnKeyPressed(e -> {
            historyButton.hidePopup();
            if (e.getCode() == KeyCode.ENTER) {
                historyManager.add(textField.getText());
            }
        });

        HBox box = new HBox(5, textField, historyButton);
        box.setAlignment(Pos.CENTER);

        Label label = new Label("""
                1. Tips: Press Enter to add the text to the history.
                2. Click the history button to show the history popup.
                """);
        label.setStyle("-fx-text-fill: #666;");

        VBox vbox = new VBox(50, label, box);
        vbox.setAlignment(Pos.CENTER);
        return vbox;
    }

    /**
     * Creates a list view with a history button.
     */
    private Node prefsDemo() {
        ListView<Student> listView = new ListView<>();
        listView.getItems().addAll(
                new Student("John", 90),
                new Student("Doe", 95),
                new Student("Jane", 85),
                new Student("Smith", 92),
                new Student("Alice", 72),
                new Student("Bob", 68),
                new Student("Eve", 91),
                new Student("Mallory", 66),
                new Student("Charlie", 79),
                new Student("David", 83)
        );

        StringConverter<Student> converter = new StringConverter<>() {
            @Override
            public String toString(Student object) {
                return object.name() + " : " + object.score();
            }

            @Override
            public Student fromString(String string) {
                String[] parts = string.split(" : ");
                return new Student(parts[0], Integer.parseInt(parts[1]));
            }
        };

        Preferences preferences = Preferences.userNodeForPackage(HistoryManagerApp.class);

        PreferencesHistoryManager<Student> historyManager = new PreferencesHistoryManager<>(preferences, "list-values", converter);

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                historyManager.add(listView.getSelectionModel().getSelectedItem());
            }
        });

        HistoryButton<Student> historyButton = new HistoryButton<>();
        historyButton.setHistoryManager(historyManager);
        historyButton.setText("History");
        historyButton.setOnItemSelected(item -> {
            if (item != null) {
                listView.getSelectionModel().select(item);
            }

            historyButton.hidePopup();
        });

        Label label = new Label("""
                1. Tips: Double-click the item to add it to the history.
                2. Click the history button to show the history popup.
                3. Click the item in the history popup to select it in the list view.
                """);
        label.setStyle("-fx-text-fill: #666;");

        VBox vBox = new VBox(15, label, listView, historyButton);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(30));
        return vBox;
    }

    public record Student(String name, int score) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Student student = (Student) o;
            return score == student.score && Objects.equals(name, student.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, score);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
