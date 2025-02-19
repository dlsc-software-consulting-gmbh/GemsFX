package com.dlsc.gemsfx.demo.util;

import com.dlsc.gemsfx.demo.GemApplication;
import com.dlsc.gemsfx.util.EnumUtil;
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
public class SimpleStringConverterDemo extends GemApplication {

    public record Task(String description, LocalDate dueDate) {
    }

    private enum Status {
        OPEN,
        IN_PROGRESS,
        DONE;

        @Override
        public String toString() {
            return EnumUtil.formatEnumNameAsTitleCase(this);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        // Create a ComboBox and populate it with Task objects
        ComboBox<Task> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(
                new Task("Task 1", LocalDate.now().plusWeeks(3)),
                new Task("Task 2", LocalDate.now().plusWeeks(5)),
                null,
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
        //         return "No Task";
        //     }
        //
        //     @Override
        //     public Task fromString(String string) {
        //         return null;
        //     }
        // });

        // Set the converter to use SimpleStringConverter with the default title case formatting
        comboBox.setConverter(new SimpleStringConverter<>(task -> task.description() + "  [Due: " + task.dueDate() + "]", "No Task"));

        ComboBox<Status> statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll(Status.OPEN, null, Status.IN_PROGRESS, Status.DONE);
        statusComboBox.getSelectionModel().selectFirst();

        // For enum types, you can use EnumStringConverter directly instead of SimpleStringConverter.
        // This approach eliminates the need to override the toString() method in the Status enum,
        // as long as the default conversion logic meets your requirements.
        //
        // By default, EnumStringConverter (with the no-argument constructor):
        //  - Converts null values to an empty string ("").
        //  - Formats enum names in title case (e.g., "IN_PROGRESS" → "In Progress").
        //
        // Example usage:
        //
        // statusComboBox.setConverter(new EnumStringConverter<>());
        //  With this, you no longer need to override toString() in the Status enum.
        //
        statusComboBox.setConverter(new SimpleStringConverter<>());

        // Create a VBox layout and add the ComboBox to it
        Label label = new Label("Select a task:");
        label.setFont(new Font(16));

        Label statusLabel = new Label("Select a status:");
        statusLabel.setFont(new Font(16));

        VBox vbox = new VBox(15, label, comboBox, statusLabel, statusComboBox);
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
