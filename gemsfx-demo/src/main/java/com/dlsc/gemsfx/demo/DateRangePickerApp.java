package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import com.dlsc.gemsfx.daterange.DateRangeView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;

public class DateRangePickerApp extends Application {

    @Override
    public void start(Stage stage) {
        DateRangePicker picker = new DateRangePicker();
        picker.setValue(new DateRangePreset("My Preset", LocalDate.now(), LocalDate.now().plusDays(8)));

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setEditable(false);
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.getItems().addAll("Dirk", "Katja", "Philip", "Jule", "Armin");
        comboBox.getSelectionModel().select(0);

        DatePicker datePicker = new DatePicker();
        datePicker.setEditable(false);
        datePicker.setMaxWidth(Double.MAX_VALUE);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll("Dirk", "Katja", "Philip", "Jule", "Armin");
        choiceBox.setMaxWidth(Double.MAX_VALUE);

        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setMaxWidth(Double.MAX_VALUE);

        Button scenicViewButton = new Button("Scenic View");

        CheckBox showPresetTitleCheckBox = new CheckBox("Show preset title");
        showPresetTitleCheckBox.selectedProperty().bindBidirectional(picker.showPresetTitleProperty());

        CheckBox showIconCheckBox = new CheckBox("Show icon");
        showIconCheckBox.selectedProperty().bindBidirectional(picker.showIconProperty());

        ComboBox<DateTimeFormatter> formattersBox = new ComboBox<>();
        formattersBox.getItems().add(DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT));
        formattersBox.getItems().add(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));
        formattersBox.getItems().add(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG));
        formattersBox.getItems().add(DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL));
        formattersBox.getSelectionModel().select(0);
        picker.formatterProperty().bindBidirectional(formattersBox.valueProperty());

        CheckBox smallBox = new CheckBox("Small");
        smallBox.selectedProperty().bindBidirectional(picker.smallProperty());

        Label comparisonLabel = new Label("Similar controls for comparison");
        Label optionsLabel = new Label("Options");

        VBox.setMargin(comparisonLabel, new Insets(20, 0, 0, 0));
        VBox.setMargin(optionsLabel, new Insets(20, 0, 0, 0));
        VBox.setMargin(scenicViewButton, new Insets(20, 0, 0, 0));

        VBox vBox = new VBox(10, picker, comparisonLabel, comboBox, datePicker, choiceBox, calendarPicker, optionsLabel, smallBox, showPresetTitleCheckBox, showIconCheckBox, formattersBox, scenicViewButton);

        vBox.setPadding(new Insets(20));

        Scene scene = new Scene(vBox);
        scenicViewButton.setOnAction(evt -> ScenicView.show(scene));

        CSSFX.start(scene);

        stage.setTitle("Date Range Picker");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
