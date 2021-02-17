package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.TimePicker;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.FormatStyle;

import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class TimePickerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        TimePicker timePicker = new TimePicker();

        CheckBox rollOverBox = new CheckBox("Rollover");
        rollOverBox.selectedProperty().bindBidirectional(timePicker.rolloverProperty());

        CheckBox linkFieldsBox = new CheckBox("Link Fields");
        linkFieldsBox.selectedProperty().bindBidirectional(timePicker.linkingFieldsProperty());

        CheckBox fullWidth = new CheckBox("Full Width");
        fullWidth.selectedProperty().addListener(it -> {
            if (fullWidth.isSelected()) {
                timePicker.setMaxWidth(Double.MAX_VALUE);
            } else {
                timePicker.setMaxWidth(Region.USE_PREF_SIZE);
            }
        });

        CheckBox showPopupButtonBox = new CheckBox("Show Button");
        showPopupButtonBox.selectedProperty().bindBidirectional(timePicker.showPopupTriggerButtonProperty());

        Button showOrHidePopupButton = new Button("Show Popup");
        showOrHidePopupButton.setOnAction(evt -> timePicker.show());

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            LocalTime time = timePicker.getTime();
            if (time != null) {
                return "Time: " + DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(time) + " (adjusted: " + (timePicker.isAdjusted() ? "yes" : "no") + ")";
            }
            return "empty";
        }, timePicker.timeProperty(), timePicker.adjustedProperty()));

        TextField textField = new TextField();
        textField.setPromptText("Text field");

        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);
        datePicker.valueProperty().addListener(it -> System.out.println("date: " + datePicker.getValue()));
        datePicker.getEditor().textProperty().addListener(it -> {
            try {
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).parse(datePicker.getEditor().getText());
            } catch (DateTimeParseException ex) {
            }
        });

        Button updateButton = new Button("Update with current time");
        updateButton.setOnAction(evt -> {
            timePicker.setTime(LocalTime.now());
            timePicker.adjust();
        });

        Button nullButton = new Button("Set 'null'");
        nullButton.setOnAction(evt -> timePicker.setTime(null));

        ComboBox<Integer> stepRateBox = new ComboBox<>();
        stepRateBox.getItems().addAll(1, 5, 10, 15, 30);
        stepRateBox.valueProperty().addListener(it -> timePicker.setStepRateInMinutes(stepRateBox.getValue()));
        stepRateBox.getSelectionModel().select(Integer.valueOf(timePicker.getStepRateInMinutes())); // must be "Integer" object, not int

        ComboBox<LocalTime> earliestTimeBox = new ComboBox<>();
        earliestTimeBox.getItems().addAll(LocalTime.MIN, LocalTime.of(6, 30), LocalTime.of(23, 00));
        earliestTimeBox.valueProperty().addListener(it -> timePicker.setEarliestTime(earliestTimeBox.getValue()));
        earliestTimeBox.getSelectionModel().select(LocalTime.MIN);

        ComboBox<LocalTime> latestTimeBox = new ComboBox<>();
        latestTimeBox.getItems().addAll(LocalTime.MAX, LocalTime.of(18, 00), LocalTime.of(2, 00));
        latestTimeBox.valueProperty().addListener(it -> timePicker.setLatestTime(latestTimeBox.getValue()));
        latestTimeBox.getSelectionModel().select(LocalTime.MAX);
        latestTimeBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalTime time) {
                if (time.equals(LocalTime.MAX)) {
                    return "23:59";
                }
                return time.toString();
            }

            @Override
            public LocalTime fromString(String string) {
                return null;
            }
        });

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);

        gridPane.add(new Label("Step rate:"), 0, 0);
        gridPane.add(stepRateBox, 1, 0);
        gridPane.add(new Label("Earliest time:"), 0, 1);
        gridPane.add(earliestTimeBox, 1, 1);
        gridPane.add(new Label("Latest time:"), 0, 2);
        gridPane.add(latestTimeBox, 1, 02);

        VBox box0 = new VBox(20, timePicker, valueLabel);
        VBox box1 = new VBox(20, datePicker, textField);
        VBox box2 = new VBox(20, fullWidth, showPopupButtonBox, linkFieldsBox, rollOverBox, gridPane);
        HBox box3 = new HBox(20, showOrHidePopupButton, updateButton, nullButton);

        box1.setStyle("-fx-padding: 20px; -fx-background-color: white; -fx-background-radius: 2px; -fx-border-color: gray; -fx-border-radius: 2px;");
        box2.setStyle(box1.getStyle()); // same style
        box3.setStyle(box2.getStyle()); // same style

        Label label1 = new Label("Compare");
        Label label2 = new Label("Settings");
        Label label3 = new Label("Actions");
        Separator separator = new Separator(Orientation.HORIZONTAL);

        VBox.setMargin(label1, new Insets(0, 0, 5, 0));
        VBox.setMargin(label2, new Insets(15, 0, 5, 0));
        VBox.setMargin(label3, new Insets(15, 0, 5, 0));
        VBox.setMargin(separator, new Insets(15, 0, 15, 0));

        VBox box = new VBox(box0, separator, label1, box1, label2, box2, label3, box3);
        box.setFillWidth(true);

        StackPane stackPane = new StackPane(box);
        stackPane.getStyleClass().add("dvc");
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        primaryStage.setTitle("TimePicker");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
