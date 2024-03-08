package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.daterange.DateRangePreset;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateRangePickerApp extends Application {

    @Override
    public void start(Stage stage) {
        DateRangePicker picker = new DateRangePicker();
        picker.setValue(new DateRange("Initial Range", LocalDate.now(), LocalDate.now().plusDays(8)));

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

        Button changePresetsButton = new Button("Change Presets");
        changePresetsButton.setOnAction(evt -> picker.getDateRangeView().getPresets().setAll(createTodayRange(), createYesterdayPreset(), createThisWeekPreset()));

        VBox.setMargin(comparisonLabel, new Insets(20, 0, 0, 0));
        VBox.setMargin(optionsLabel, new Insets(20, 0, 0, 0));
        VBox.setMargin(scenicViewButton, new Insets(20, 0, 0, 0));

        Button showPopupButton = new Button("Show Popup");
        showPopupButton.setOnAction(evt -> picker.show());

        Button hidePopupButton = new Button("Hide Popup");
        hidePopupButton.setOnAction(evt -> picker.hide());

        HBox popupButtons = new HBox(10, showPopupButton, hidePopupButton);

        VBox vBox = new VBox(10, picker, comparisonLabel, comboBox, datePicker, choiceBox, calendarPicker, optionsLabel, smallBox, showPresetTitleCheckBox, showIconCheckBox, formattersBox, changePresetsButton, popupButtons, scenicViewButton);

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

    private DateRangePreset createTodayRange() {
        return new DateRangePreset("Today New", () -> new DateRange("Today", LocalDate.now()));
    }

    private DateRangePreset createYesterdayPreset() {
        return new DateRangePreset("Yesterday New", () -> new DateRange("Yesterday", LocalDate.now().minusDays(1)));
    }

    private DateRangePreset createThisWeekPreset() {
        return new DateRangePreset("This Week New", () -> {
            TemporalField fieldISO = WeekFields.of(Locale.getDefault()).dayOfWeek();
            return new DateRange("This Week", LocalDate.now().with(fieldISO, 1), LocalDate.now().with(fieldISO, 1).plusDays(6));
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
