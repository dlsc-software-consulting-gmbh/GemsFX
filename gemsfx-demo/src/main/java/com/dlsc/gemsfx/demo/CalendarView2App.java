package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel.SelectionMode;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.time.YearMonth;

public class CalendarView2App extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);

        CalendarView calendarView1 = new CalendarView();
        CalendarView calendarView2 = new CalendarView();

        calendarView1.setShowDaysOfPreviousOrNextMonth(false);
        calendarView1.getSelectionModel().setSelectionMode(SelectionMode.DATE_RANGE);
        calendarView1.disableNextMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> calendarView1.getYearMonth().equals(calendarView2.getYearMonth().minusMonths(1)), calendarView1.yearMonthProperty(), calendarView2.yearMonthProperty()));

        calendarView2.setShowDaysOfPreviousOrNextMonth(false);
        calendarView2.setYearMonth(YearMonth.now().plusMonths(1));
        calendarView2.disablePreviousMonthButtonProperty().bind(Bindings.createBooleanBinding(() -> calendarView1.getYearMonth().equals(calendarView2.getYearMonth().minusMonths(1)), calendarView1.yearMonthProperty(), calendarView2.yearMonthProperty()));

        // let calendars share the same selection model
        calendarView2.setSelectionModel(calendarView1.getSelectionModel());

        HBox box = new HBox(50, calendarView1, calendarView2);
        box.setPadding(new Insets(50));
        box.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);
        stage.setTitle("CalendarView");
        stage.setScene(scene);
        stage.sizeToScene();
        StageManager.install(stage, "calendar.view2.app");

        stage.show();
    }

        @Override
    public String getDescription() {
        return """
                ### CalendarView
                
                Displays a given month of a given year. The view can be configured in many
                ways:
                
                - Show / hide the name of the month
                - Show / hide the year
                - Show / hide arrow buttons for changing the month
                - Show / hide arrow buttons for changing the year
                - Show / hide today
                - Show / hide a button for going to today
                - Show / hide usage colors
                
                Additionally the application can choose from two different behaviours when
                the user clicks on a date:
                
                - Perform a selection / select the date
                - Show details of the date (by default shows a popover with all entries on
                that date)
                
                The image below shows the visual appearance of this control:
                """;
    }

    public static void main(String[] args) {
        launch();
    }
}
