package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.HistoryButton;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.PreferencesHistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
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
                new Tab("Basic", basicDemo()),
                new Tab("Advanced", advancedDemo()),
                new Tab("Other", otherDemo())
        );

        primaryStage.setScene(new Scene(tabPane, 800, 600));
        primaryStage.setTitle("History Manager Demo");
        primaryStage.show();
    }

    private Node basicDemo() {
        TextField textField = new TextField();
        StringHistoryManager historyManager = new StringHistoryManager();
        // historyManager.setPreferences(Preferences.userNodeForPackage(HistoryManagerApp.class).node("simpleTextField"));

        HistoryButton<String> historyButton = new HistoryButton<>(textField, historyManager);
        historyButton.setConfigureHistoryPopup(historyPopup -> {
            // When choosing a history item, replace the text in the text field.
            historyPopup.setOnHistoryItemConfirmed(item -> {
                if (item != null) {
                    textField.setText(item);
                }
                historyPopup.hide();
            });
        });

        // Add history item to the history when the enter key is pressed.
        textField.setOnKeyPressed(e -> {
            historyButton.hideHistoryPopup();
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
    private Node advancedDemo() {
        TextField textField = new TextField();

        StringHistoryManager historyManager = new StringHistoryManager();
        // Tips: You can set the delimiter and preferencesKey when creating, otherwise use the default value.
        // PreferencesHistoryManager historyManager = new PreferencesHistoryManager(";", "save-items");

        // Tips: If you want to persist the history after the application restarts, Please set the preferences.
        historyManager.setPreferences(Preferences.userNodeForPackage(HistoryManagerApp.class).node("textField"));
        // Optional: Set the maximum history size.default is 30.
        historyManager.setMaxHistorySize(10);
        // Optional: if the history is empty, set some default values
        if (historyManager.getAll().isEmpty()) {
            historyManager.set(List.of("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten"));
        }

        HistoryButton<String> historyButton = new HistoryButton<>(textField, historyManager);

        // add history item to the history when the enter key is pressed.
        textField.setOnKeyPressed(e -> {
            historyButton.hideHistoryPopup();
            if (e.getCode() == KeyCode.ENTER) {
                historyManager.add(textField.getText());
            }
        });

        // Optional: true means the popup owner will be focused when the history popup is opened.
        // historyButton.setFocusPopupOwnerOnOpen(true);

        // Optional: Configure the history popup
        historyButton.setConfigureHistoryPopup(historyPopup -> {

                    historyPopup.setHistoryPlaceholder(new Label("Tips: No history items available."));

                    historyPopup.setOnHistoryItemConfirmed(item -> {
                        if (item != null) {
                            int length = textField.textProperty().getValueSafe().length();
                            textField.replaceText(0, length, item);
                        }
                        historyPopup.hide();
                    });

                    // create the left node;
                    VBox leftBox = new VBox();
                    Label label = new Label("History");
                    label.setRotate(90);
                    Group group = new Group(label);

                    Button clearAll = new Button("", new FontIcon(MaterialDesign.MDI_DELETE));
                    clearAll.setPadding(new Insets(2, 4, 2, 4));
                    clearAll.setOnAction(e -> {
                        historyManager.clear();
                        historyPopup.hide();
                    });
                    clearAll.managedProperty().bind(clearAll.visibleProperty());
                    clearAll.visibleProperty().bind(Bindings.isNotEmpty(historyManager.getAll()));

                    leftBox.getChildren().addAll(group, new Spacer(), clearAll);
                    leftBox.setAlignment(Pos.CENTER);
                    leftBox.setPadding(new Insets(10, 5, 5, 5));
                    leftBox.setPrefWidth(35);
                    leftBox.setStyle("-fx-background-color: #f4f4f4;");
                    historyPopup.setLeft(leftBox);
                }
        );


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
    private Node otherDemo() {
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

        PreferencesHistoryManager<Student> historyManager = new PreferencesHistoryManager<>(
                new StringConverter<>() {
                    @Override
                    public String toString(Student object) {
                        return object.name() + " : " + object.score();
                    }

                    @Override
                    public Student fromString(String string) {
                        String[] parts = string.split(" : ");
                        return new Student(parts[0], Integer.parseInt(parts[1]));
                    }
                }
        );

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                historyManager.add(listView.getSelectionModel().getSelectedItem());
            }
        });

        historyManager.setPreferences(Preferences.userNodeForPackage(HistoryManagerApp.class).node("list"));

        HistoryButton<Student> historyButton = new HistoryButton<>(null, historyManager);
        historyButton.setText("History");

        historyButton.setConfigureHistoryPopup(historyPopup -> {
            historyPopup.setOnHistoryItemConfirmed(item -> {
                if (item != null) {
                    listView.getSelectionModel().select(item);
                }
                historyPopup.hide();
            });
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
