package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.HeaderLayout;
import com.dlsc.gemsfx.CalendarView.SelectionModel.SelectionMode;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
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
        options1.getChildren().add(createOption("Show year spinner", calendarView.showYearSpinnerProperty()));
        options1.getChildren().add(createOption("Show year dropdown", calendarView.showYearDropdownProperty()));
        options1.getChildren().add(createOption("Show month arrows", calendarView.showMonthArrowsProperty()));
        options1.getChildren().add(createOption("Show month dropdown", calendarView.showMonthDropdownProperty()));
        options1.getChildren().add(createOption("Show week numbers", calendarView.showWeekNumbersProperty()));
        options1.getChildren().add(createOption("Show days of other months", calendarView.showDaysOfPreviousOrNextMonthProperty()));
        options1.getChildren().add(createOption("Mark days of other months selected", calendarView.markSelectedDaysOfPreviousOrNextMonthProperty()));
        options1.getChildren().add(createOption("Show grid lines", showGridLines));

        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.setMaxWidth(Double.MAX_VALUE);
        selectionModeComboBox.getItems().setAll(SelectionMode.values());
        selectionModeComboBox.valueProperty().bindBidirectional(calendarView.getSelectionModel().selectionModeProperty());
        Label selectionModeLabel = new Label("Selection mode");
        VBox.setMargin(selectionModeLabel, new Insets(10, 0, 0, 0));
        options1.getChildren().addAll(selectionModeLabel, selectionModeComboBox);

        ComboBox<HeaderLayout> headerLayoutComboBox = new ComboBox<>();
        headerLayoutComboBox.setMaxWidth(Double.MAX_VALUE);
        headerLayoutComboBox.getItems().setAll(HeaderLayout.values());
        headerLayoutComboBox.valueProperty().bindBidirectional(calendarView.headerLayoutProperty());
        Label headerLayoutLabel = new Label("Header layout");
        VBox.setMargin(headerLayoutLabel, new Insets(10, 0, 0, 0));
        options1.getChildren().addAll(headerLayoutLabel, headerLayoutComboBox);

        CheckBox disabledWeekendBox = new CheckBox("Disable Weekend");
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

    public static void main(String[] args) {
        launch();
    }
}
