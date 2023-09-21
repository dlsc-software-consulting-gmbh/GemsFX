package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel.SelectionMode;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.time.LocalDate;

public class CalendarViewApp extends Application {

    @Override
    public void start(Stage stage) {
        CalendarView calendarView = new CalendarView();
        calendarView.setShowTodayButton(true);
        calendarView.getSelectionModel().setSelectedDate(LocalDate.now().minusWeeks(1));
        HBox.setHgrow(calendarView, Priority.ALWAYS);

        VBox options1 = new VBox(10);

        options1.getChildren().add(createOption("Show today", calendarView.showTodayProperty()));
        options1.getChildren().add(createOption("Show today button", calendarView.showTodayButtonProperty()));
        options1.getChildren().add(createOption("Show month", calendarView.showMonthProperty()));
        options1.getChildren().add(createOption("Show year", calendarView.showYearProperty()));
        options1.getChildren().add(createOption("Show year spinner", calendarView.showYearSpinnerProperty()));
        options1.getChildren().add(createOption("Show month arrows", calendarView.showMonthArrowsProperty()));
        options1.getChildren().add(createOption("Show week numbers", calendarView.showWeekNumbersProperty()));
        options1.getChildren().add(createOption("Show days of other months", calendarView.showDaysOfPreviousOrNextMonthProperty()));

        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().setAll(SelectionMode.values());
        selectionModeComboBox.valueProperty().bindBidirectional(calendarView.getSelectionModel().selectionModeProperty());
        options1.getChildren().add(selectionModeComboBox);

        VBox options2 = new VBox(10);
        options2.getChildren().add(createOption("Disable previous month", calendarView.disablePreviousMonthButtonProperty()));
        options2.getChildren().add(createOption("Disable next month", calendarView.disableNextMonthButtonProperty()));
        options2.getChildren().add(createOption("Disable previous year", calendarView.disablePreviousYearButtonProperty()));
        options2.getChildren().add(createOption("Disable next year", calendarView.disableNextYearButtonProperty()));

        Button scenicViewButton = new Button("Scenic View");
        scenicViewButton.setMaxWidth(Double.MAX_VALUE);

        VBox calendarWrapper = new VBox(20, calendarView, scenicViewButton);

        HBox box = new HBox(50, calendarWrapper, options1, options2);
        box.setPadding(new Insets(50));
        box.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);

        scenicViewButton.setOnAction(evt -> ScenicView.show(scene));

        stage.setTitle("CalendarView");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();

        CSSFX.start(calendarView);
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
