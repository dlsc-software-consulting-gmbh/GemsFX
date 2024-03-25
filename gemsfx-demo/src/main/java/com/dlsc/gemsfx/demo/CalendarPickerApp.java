package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.CustomComboBox;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class CalendarPickerApp extends Application {

    @Override
    public void start(Stage stage) {
        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setValue(LocalDate.now());

        Label valueLabel = new Label();
        valueLabel.textProperty().bind(Bindings.createStringBinding(() -> calendarPicker.getConverter().toString(calendarPicker.getValue()), calendarPicker.valueProperty()));

        CheckBox editable = new CheckBox("Editable");
        editable.selectedProperty().bindBidirectional(calendarPicker.editableProperty());

        CheckBox disable = new CheckBox("Disable");
        disable.selectedProperty().bindBidirectional(calendarPicker.disableProperty());

        CheckBox disabledWeekendBox = new CheckBox("Filter: Disable Weekend");
        calendarPicker.dateFilterProperty().bind(Bindings.createObjectBinding(() -> {
            if (disabledWeekendBox.isSelected()) {
                return date -> date.getDayOfWeek().getValue() < 6;
            } else {
                return null; // return date -> true;
            }
        }, disabledWeekendBox.selectedProperty()));

        CheckBox showTodayButton = new CheckBox("Show Today Button");
        showTodayButton.selectedProperty().bindBidirectional(calendarPicker.getCalendarView().showTodayButtonProperty());

        Button showPopupButton = new Button("Show Popup");
        showPopupButton.setOnAction(evt -> calendarPicker.show());

        Button hidePopupButton = new Button("Hide Popup");
        hidePopupButton.setOnAction(evt -> calendarPicker.hide());

        HBox popupButtons = new HBox(10, showPopupButton, hidePopupButton);

        ComboBox<CustomComboBox.ButtonDisplay> buttonDisplayComboBox = new ComboBox<>();
        buttonDisplayComboBox.getItems().addAll(CustomComboBox.ButtonDisplay.values());
        buttonDisplayComboBox.valueProperty().bindBidirectional(calendarPicker.buttonDisplayProperty());
        Label buttonDisplayLabel = new Label("Button Display:");
        HBox buttonDisplayBox = new HBox(10, buttonDisplayLabel, buttonDisplayComboBox);
        buttonDisplayBox.setAlignment(Pos.CENTER_LEFT);

        VBox vBox = new VBox(10, popupButtons, calendarPicker, valueLabel, editable, disable, disabledWeekendBox, showTodayButton, buttonDisplayBox);
        vBox.setAlignment(Pos.TOP_LEFT);
        vBox.setPadding(new Insets(20));

        Scene scene = new Scene(vBox);
        CSSFX.start();

        stage.setTitle("CalendarPicker");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
