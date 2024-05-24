package com.dlsc.gemsfx.demo.util;

import com.dlsc.gemsfx.util.EnumStringConverter;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * This class demonstrates the usage of EnumStringConverter, which is a specialized StringConverter implementation
 * for Enum types. It provides a default mechanism to format Enum values in title case, replaces underscores with spaces,
 * and capitalizes the first letter of each word. If the Enum value is null, it returns an empty string.
 * <p>
 * {@link EnumStringConverter}
 */
public class EnumStringConverterDemo extends Application {

    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    @Override
    public void start(Stage primaryStage) {
        // Create a ComboBox and populate it with Status objects
        ComboBox<Status> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(Status.values());
        comboBox.getSelectionModel().selectFirst();

        // The traditional way to set a StringConverter on a ComboBox
        // comboBox.setConverter(new StringConverter<Status>() {
        //     @Override
        //     public String toString(Status object) {
        //         if (object != null) {
        //             return Arrays.stream(object.name().split("_"))
        //                     .filter(word -> !word.isEmpty())
        //                     .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
        //                     .collect(Collectors.joining(" "));
        //         }
        //         return "";
        //     }
        //
        //     @Override
        //     public Status fromString(String string) {
        //         return null;
        //     }
        // });

        // Set the converter to use EnumStringConverter with the default title case formatting
        comboBox.setConverter(new EnumStringConverter<>());

        // Create a VBox layout and add the ComboBox to it
        Label label = new Label("Select a status:");
        label.setFont(Font.font(15));
        VBox vbox = new VBox(15, label, comboBox);
        vbox.setAlignment(Pos.CENTER);
        vbox.setPadding(new Insets(20));
        Scene scene = new Scene(vbox,300, 200);

        // Configure and show the primary stage
        primaryStage.setTitle("EnumStringConverter Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
