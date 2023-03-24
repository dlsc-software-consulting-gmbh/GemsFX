package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarPicker;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
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

        VBox vBox = new VBox(10, calendarPicker, valueLabel, editable, disable);
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
