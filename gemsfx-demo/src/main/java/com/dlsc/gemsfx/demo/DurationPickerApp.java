package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DurationPicker;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

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

public class DurationPickerApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        DurationPicker durationPicker = new DurationPicker();
        durationPicker.setShowUnits(false);
        durationPicker.setDisplayShortLabel(true);

        ZonedDateTime timeA = ZonedDateTime.now();
        ZonedDateTime timeB = ZonedDateTime.now().plusDays(2).plusHours(10).plusMinutes(23).plusSeconds(55);

        durationPicker.setDuration(Duration.between(timeA, timeB));

        CheckBox rollOverBox = new CheckBox("Rollover");
        rollOverBox.selectedProperty().bindBidirectional(durationPicker.rolloverProperty());

        CheckBox showUnits = new CheckBox("Show units");
        showUnits.selectedProperty().bindBidirectional(durationPicker.showUnitsProperty());

        CheckBox displayShortLabels = new CheckBox("Short labels");
        displayShortLabels.selectedProperty().bindBidirectional(durationPicker.displayShortLabelProperty());

        CheckBox linkFieldsBox = new CheckBox("Link fields");
        linkFieldsBox.selectedProperty().bindBidirectional(durationPicker.linkingFieldsProperty());

        CheckBox fullWidth = new CheckBox("Full width");
        fullWidth.selectedProperty().addListener(it -> {
            if (fullWidth.isSelected()) {
                durationPicker.setMaxWidth(Double.MAX_VALUE);
            } else {
                durationPicker.setMaxWidth(Region.USE_PREF_SIZE);
            }
        });

        CheckBox showPopupButtonBox = new CheckBox("Show button");
        showPopupButtonBox.selectedProperty().bindBidirectional(durationPicker.showPopupTriggerButtonProperty());

        Button showOrHidePopupButton = new Button("Show Popup");
        showOrHidePopupButton.setOnAction(evt -> durationPicker.show());

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(() -> "Duration: " + humanReadableFormat(durationPicker.getDuration()), durationPicker.durationProperty()));

        TextField textField = new TextField();
        textField.setPromptText("Text field");

        DatePicker datePicker = new DatePicker();
        datePicker.setMaxWidth(Double.MAX_VALUE);

        Button updateButton = new Button("Update with zero duration");
        updateButton.setOnAction(evt -> durationPicker.setDuration(Duration.ZERO));

        ComboBox<Integer> configurationBox = new ComboBox<>();
        configurationBox.getItems().addAll(0, 1, 2, 3);
        configurationBox.setValue(0);
        configurationBox.valueProperty().addListener(it -> {
            int value = configurationBox.getValue();
            switch (value) {
                case 0:
                    durationPicker.getFields().setAll(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);
                    break;
                case 1:
                    durationPicker.getFields().setAll(ChronoUnit.HOURS, ChronoUnit.MINUTES);
                    break;
                case 2:
                    durationPicker.getFields().setAll(ChronoUnit.DAYS, ChronoUnit.HOURS);
                    break;
                case 3:
                    durationPicker.getFields().setAll(ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);
                    break;
            }
        });
        configurationBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Integer integer) {
                switch (integer) {
                    case 0:
                        return "All Fields";
                    case 1:
                        return "Hours & Minutes";
                    case 2:
                        return "Days & Hours";
                    case 3:
                        return "Minutes, Seconds, Millis";
                    default:
                       return "";
                }
            }

            @Override
            public Integer fromString(String s) {
                return null;
            }
        });

        ComboBox<Duration> minimumDurationBox = new ComboBox<>();
        minimumDurationBox.getItems().addAll(Duration.ZERO, Duration.ofMinutes(30), Duration.ofDays(1), Duration.ofHours(20));
        minimumDurationBox.valueProperty().addListener(it -> durationPicker.setMinimumDuration(minimumDurationBox.getValue()));
        minimumDurationBox.getSelectionModel().select(0);

        ComboBox<Duration> maximumDurationBox = new ComboBox<>();
        maximumDurationBox.getItems().addAll(null, Duration.ofMinutes(30), Duration.ofDays(1), Duration.ofHours(20));
        maximumDurationBox.valueProperty().addListener(it -> durationPicker.setMaximumDuration(maximumDurationBox.getValue()));
        maximumDurationBox.getSelectionModel().select(0);

        final StringConverter<Duration> converter = new StringConverter<>() {
            @Override
            public String toString(Duration duration) {
                if (duration == null) {
                    return "None";
                }
                if (duration.equals(Duration.ZERO)) {
                    return "0 Minutes";
                }
                if (duration.equals(Duration.ofMinutes(30))) {
                    return "30 Minutes";
                }
                if (duration.equals(Duration.ofDays(1))) {
                    return "1 Day";
                }
                if (duration.equals(Duration.ofHours(20))) {
                    return "20 Hours";
                }

                return duration.toString();
            }

            @Override
            public Duration fromString(String string) {
                return null;
            }
        };

        minimumDurationBox.setConverter(converter);
        maximumDurationBox.setConverter(converter);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);

        gridPane.add(new Label("Configuration:"), 0, 0);
        gridPane.add(configurationBox, 1, 0);
        gridPane.add(new Label("Minimum duration:"), 0, 1);
        gridPane.add(minimumDurationBox, 1, 1);
        gridPane.add(new Label("Maximum duration:"), 0, 2);
        gridPane.add(maximumDurationBox, 1, 02);

        VBox box0 = new VBox(20, durationPicker, valueLabel);
        VBox box1 = new VBox(20, datePicker, textField);
        VBox box2 = new VBox(20, fullWidth, showPopupButtonBox, linkFieldsBox, rollOverBox, showUnits, displayShortLabels, gridPane);
        HBox box3 = new HBox(20, showOrHidePopupButton, updateButton);

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

        primaryStage.setTitle("DurationPicker");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private String humanReadableFormat(Duration duration) {
        return duration.toDays() + " days, " + duration.toHoursPart() + " hours, " + duration.toMinutesPart() + " minutes, " + duration.toSecondsPart() + " seconds, " + duration.toMillisPart() + " millis";
    }

    public static void main(String[] args) {
        launch(args);
    }
}
