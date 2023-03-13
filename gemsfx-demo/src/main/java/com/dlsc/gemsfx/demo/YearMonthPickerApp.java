package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.YearMonthPicker;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
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

        VBox vBox = new VBox(40, yearMonthPicker, datePicker);

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
