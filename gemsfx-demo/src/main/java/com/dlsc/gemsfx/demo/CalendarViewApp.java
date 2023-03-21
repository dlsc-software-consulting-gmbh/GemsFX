package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel.SelectionMode;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CalendarViewApp extends Application {

    @Override
    public void start(Stage stage) {
        CalendarView calendarView = new CalendarView();
        HBox.setHgrow(calendarView, Priority.ALWAYS);

        VBox options = new VBox(10);

        options.getChildren().add(createOption("Show header", calendarView.showHeaderProperty()));
        options.getChildren().add(createOption("Show today", calendarView.showTodayProperty()));
        options.getChildren().add(createOption("Show today button", calendarView.showTodayButtonProperty()));
        options.getChildren().add(createOption("Show month", calendarView.showMonthProperty()));
        options.getChildren().add(createOption("Show year", calendarView.showYearProperty()));
        options.getChildren().add(createOption("Show year spinner", calendarView.showYearSpinnerProperty()));
        options.getChildren().add(createOption("Show month arrows", calendarView.showMonthArrowsProperty()));
        options.getChildren().add(createOption("Show week numbers", calendarView.showWeekNumbersProperty()));
        options.getChildren().add(createOption("Show days of other months", calendarView.showDaysOfPreviousOrNextMonthProperty()));

        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().setAll(SelectionMode.values());
        selectionModeComboBox.valueProperty().bindBidirectional(calendarView.getSelectionModel().selectionModeProperty());
        options.getChildren().add(selectionModeComboBox);

        HBox box = new HBox(50, calendarView, options);
        box.setPadding(new Insets(50));
        box.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);
        stage.setTitle("CalendarView");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private Node createOption(String name, BooleanProperty property) {
        CheckBox box = new CheckBox(name);
        box.selectedProperty().bindBidirectional(property);
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}