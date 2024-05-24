package com.dlsc.gemsfx.demo.util;

import com.dlsc.gemsfx.util.SimpleStringConverter;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.time.LocalDate;

/**
 * This class demonstrates the usage of SimpleStringConverter, which is a specialized StringConverter implementation
 * that allows you to define a custom formatting function for the conversion of objects to strings. It also provides
 * a default mechanism to handle null values and empty strings.
 * {@link SimpleStringConverter}
 */
public class SimpleStringConverterDemo extends Application {

    public record Task(String description, LocalDate dueDate) {
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a ComboBox and populate it with Task objects
        ComboBox<Task> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(
                new Task("Task 1", LocalDate.now().plusWeeks(3)),
                new Task("Task 2", LocalDate.now().plusWeeks(5)),
                new Task("Task 3", LocalDate.now().plusWeeks(6))
        );
        comboBox.getSelectionModel().selectFirst();

        // The traditional way to set a StringConverter on a ComboBox
        // comboBox.setConverter(new StringConverter<Task>() {
        //     @Override
        //     public String toString(Task object) {
        //         if (object != null) {
        //             return object.description() + "  [Due: " + object.dueDate() +"]";
        //         }
        //         return "";
        //     }
        //
        //     @Override
        //     public Task fromString(String string) {
        //         return null;
        //     }
        // });

        // Set the converter to use SimpleStringConverter with the default title case formatting
        comboBox.setConverter(new SimpleStringConverter<>(task -> task.description() + "  [Due: " + task.dueDate() + "]", ""));

        // Create a VBox layout and add the ComboBox to it
        Label label = new Label("Select a status:");
        label.setFont(new Font(16));
        VBox vbox = new VBox(15, label, comboBox);
        vbox.setPadding(new Insets(15));
        vbox.setAlignment(Pos.CENTER);
        Scene scene = new Scene(vbox, 300, 200);

        // Configure and show the primary stage
        primaryStage.setTitle("SimpleStringConverter Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
