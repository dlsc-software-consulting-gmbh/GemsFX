package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.YearMonthPicker;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class YearMonthPickerApp extends Application {

    @Override
    public void start(Stage stage) {
        YearMonthPicker yearMonthPicker = new YearMonthPicker();

        DatePicker datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());

        CheckBox editable = new CheckBox("Editable Textfield");
        editable.selectedProperty().bindBidirectional(yearMonthPicker.editableProperty());
        editable.selectedProperty().bindBidirectional(datePicker.editableProperty());

        CheckBox disable = new CheckBox("Disable");
        disable.selectedProperty().bindBidirectional(yearMonthPicker.disableProperty());
        disable.selectedProperty().bindBidirectional(datePicker.disableProperty());

        VBox vBox = new VBox(40, yearMonthPicker, datePicker, editable, disable);

        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        CSSFX.start();

        stage.setTitle("YearMonthPicker");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
