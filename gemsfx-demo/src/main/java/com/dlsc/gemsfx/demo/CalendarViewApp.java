package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel.SelectionMode;
import com.dlsc.gemsfx.CalendarView.YearDisplayMode;
import com.dlsc.gemsfx.CalendarView.MonthDisplayMode;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
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

        showGridLines.addListener(it -> {
            if (showGridLines.get()) {
                calendarView.getStyleClass().add("show-grid-lines");
            } else {
                calendarView.getStyleClass().remove("show-grid-lines");
            }
        });

        VBox options1 = new VBox(10);

        options1.getChildren().add(createOption("Show today", calendarView.showTodayProperty()));
        options1.getChildren().add(createOption("Show today button", calendarView.showTodayButtonProperty()));
        options1.getChildren().add(createOption("Show month", calendarView.showMonthProperty()));
        options1.getChildren().add(createOption("Show year", calendarView.showYearProperty()));
        options1.getChildren().add(createOption("Show month arrows", calendarView.showMonthArrowsProperty()));
        options1.getChildren().add(createOption("Show week numbers", calendarView.showWeekNumbersProperty()));
        options1.getChildren().add(createOption("Show days of other months", calendarView.showDaysOfPreviousOrNextMonthProperty()));
        options1.getChildren().add(createOption("Mark days of other months selected", calendarView.markSelectedDaysOfPreviousOrNextMonthProperty()));
        options1.getChildren().add(createOption("Show grid lines", showGridLines));
        options1.getChildren().add(new Separator());

        VBox yearDisplayMode = createComboBoxOption("Year Display Mode", YearDisplayMode.TEXT_ONLY, calendarView.yearDisplayModeProperty());
        options1.getChildren().add(yearDisplayMode);

        VBox monthDisplayMode = createComboBoxOption("Month Display Mode", MonthDisplayMode.TEXT_ONLY, calendarView.monthDisplayModeProperty());
        options1.getChildren().add(monthDisplayMode);

        VBox selectionModeBox = createComboBoxOption("Selection mode", SelectionMode.DATE_RANGE, calendarView.getSelectionModel().selectionModeProperty());
        options1.getChildren().add(selectionModeBox);

        VBox headerLayoutBox = createComboBoxOption("Header layout", CalendarView.HeaderLayout.CENTER, calendarView.headerLayoutProperty());
        options1.getChildren().add(headerLayoutBox);

        CheckBox disabledWeekendBox = new CheckBox("Filter: Disable Weekend");
        calendarView.dateFilterProperty().bind(Bindings.createObjectBinding(() -> {
            if (disabledWeekendBox.isSelected()) {
                return date -> date.getDayOfWeek().getValue() < 6;
            } else {
                return null; // return date -> true;
            }
        }, disabledWeekendBox.selectedProperty()));

        VBox options2 = new VBox(10);
        options2.getChildren().add(createOption("Disable previous month", calendarView.disablePreviousMonthButtonProperty()));
        options2.getChildren().add(createOption("Disable next month", calendarView.disableNextMonthButtonProperty()));
        options2.getChildren().add(createOption("Disable previous year", calendarView.disablePreviousYearButtonProperty()));
        options2.getChildren().add(createOption("Disable next year", calendarView.disableNextYearButtonProperty()));
        options2.getChildren().add(createOption("Disable month dropdown", calendarView.disableMonthDropdownButtonProperty()));
        options2.getChildren().add(createOption("Disable year dropdown", calendarView.disableYearDropdownButtonProperty()));
        options2.getChildren().add(new Separator(Orientation.HORIZONTAL));
        options2.getChildren().add(createOption("Enable year selection view", calendarView.yearSelectionViewEnabledProperty()));
        options2.getChildren().add(createOption("Enable month selection view", calendarView.monthSelectionViewEnabledProperty()));
        options2.getChildren().add(new Separator(Orientation.HORIZONTAL));
        options2.getChildren().add(disabledWeekendBox);
        Button scenicViewButton = new Button("Scenic View");

        VBox calendarWrapper = new VBox(50, calendarView, scenicViewButton);

        HBox box = new HBox(50, calendarWrapper, options1, options2);
        box.setPadding(new Insets(50));
        box.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(box);

        Scene scene = new Scene(stackPane);
        CSSFX.start(scene);

        scenicViewButton.setOnAction(evt -> ScenicView.show(scene));

        stage.setTitle("CalendarView");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private final BooleanProperty showGridLines = new SimpleBooleanProperty();

    private Node createOption(String name, BooleanProperty property) {
        CheckBox box = new CheckBox(name);
        box.selectedProperty().bindBidirectional(property);
        return box;
    }

    private <E extends Enum<E>> VBox createComboBoxOption(String title, E defaultEnum, ObjectProperty<E> property) {
        ComboBox<E> comboBox = new ComboBox<>();
        comboBox.setMaxWidth(Double.MAX_VALUE);
        comboBox.getItems().addAll(defaultEnum.getDeclaringClass().getEnumConstants());
        comboBox.setValue(defaultEnum);
        comboBox.valueProperty().bindBidirectional(property);
        VBox.setMargin(comboBox, new Insets(0, 0, 5, 0));

        Label selectionModeLabel = new Label(title);
        return new VBox(2, selectionModeLabel, comboBox);
    }

    public static void main(String[] args) {
        launch();
    }
}
