/*
 *  Copyright (C) 2017 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.DateCell;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.control.SelectionMode.SINGLE;
import static javafx.scene.layout.Priority.ALWAYS;

public class CalendarViewSkin extends SkinBase<CalendarView> {

    private static final String WEEKDAY_NAME = "weekday-name";
    private static final String TODAY = "today";
    private static final String PREVIOUS_MONTH = "previous-month";
    private static final String NEXT_MONTH = "next-month";
    private static final String WEEKEND_DAY = "weekend-day";
    private static final String SELECTED = "selected";

    private final GridPane bodyGridPane;

    private final GridPane weekdayGridPane;

    private final VBox vBox;

    private final Label monthLabel;

    private final Label yearLabel;

    private final Map<String, DateCell> cellsMap = new HashMap<>();

    private final Label[] dayOfWeekLabels = new Label[7];

    private final Label[] weekNumberLabels = new Label[6];

    private YearMonth displayedYearMonth;

    public CalendarViewSkin(CalendarView view) {
        super(view);

        bodyGridPane = new GridPane();
        bodyGridPane.setAlignment(CENTER);
        bodyGridPane.getStyleClass().addAll("grid-pane", "body-grid-pane");

        weekdayGridPane = new GridPane();
        weekdayGridPane.setAlignment(CENTER);
        weekdayGridPane.getStyleClass().addAll("grid-pane", "weekday-grid-pane");

        monthLabel = new Label();
        monthLabel.getStyleClass().add("month-label");
        monthLabel.setMinWidth(Region.USE_PREF_SIZE);
        monthLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getYearMonth() != null ? view.getYearMonth().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) : "", view.yearMonthProperty()));
        monthLabel.visibleProperty().bind(view.showMonthProperty());
        monthLabel.managedProperty().bind(view.showMonthProperty());

        yearLabel = new Label();
        yearLabel.getStyleClass().add("year-label");
        yearLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getYearMonth() != null ? Integer.toString(view.getYearMonth().getYear()) : "", view.yearMonthProperty()));
        yearLabel.visibleProperty().bind(view.showYearProperty());
        yearLabel.managedProperty().bind(view.showYearProperty());

        StackPane yearUpArrow = new StackPane();
        yearUpArrow.getStyleClass().add("increment-arrow");

        StackPane yearUpButton = new StackPane(yearUpArrow);
        yearUpButton.getStyleClass().addAll("year-button", "increment-arrow-button");
        yearUpButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().plusYears(1)));

        StackPane yearDownArrow = new StackPane();
        yearDownArrow.getStyleClass().add("decrement-arrow");

        StackPane yearDownButton = new StackPane(yearDownArrow);
        yearDownButton.getStyleClass().addAll("year-button", "decrement-arrow-button");
        yearDownButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().minusYears(1)));

        VBox yearArrowBox = new VBox(yearUpButton, yearDownButton);
        yearArrowBox.getStyleClass().add("year-arrow-box");
        yearArrowBox.setMaxWidth(Region.USE_PREF_SIZE);
        yearArrowBox.visibleProperty().bind(view.showYearProperty().and(view.showYearSpinnerProperty()));
        yearArrowBox.managedProperty().bind(view.showYearProperty().and(view.showYearSpinnerProperty()));

        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.visibleProperty().bind(view.showHeaderProperty());
        header.managedProperty().bind(view.showHeaderProperty());

        FontIcon previousArrowIcon = new FontIcon(MaterialDesign.MDI_CHEVRON_LEFT);
        StackPane previousArrowButton = new StackPane(previousArrowIcon);
        previousArrowButton.getStyleClass().addAll("arrow-button", "previous-button");
        previousArrowButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().minusMonths(1)));
        previousArrowButton.visibleProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));
        previousArrowButton.managedProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));

        FontIcon nextArrowIcon = new FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT);
        StackPane nextArrowButton = new StackPane(nextArrowIcon);
        nextArrowButton.getStyleClass().addAll("arrow-button", "next-button");
        nextArrowButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().plusMonths(1)));
        nextArrowButton.visibleProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));
        nextArrowButton.managedProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));

        Region leftSpacer = new Region();
        leftSpacer.getStyleClass().addAll("spacer", "left");
        HBox.setHgrow(leftSpacer, ALWAYS);

        Region rightSpacer = new Region();
        rightSpacer.getStyleClass().addAll("spacer", "right");
        HBox.setHgrow(rightSpacer, ALWAYS);

        header.getChildren().setAll(previousArrowButton, leftSpacer, monthLabel, yearLabel, yearArrowBox, rightSpacer, nextArrowButton);

        InvalidationListener updateViewListener = evt -> updateView();
        view.yearMonthProperty().addListener(evt -> {
            if (displayedYearMonth == null || !displayedYearMonth.equals(view.getYearMonth())) {
                updateView();
            }
        });

        InvalidationListener buildViewListener = evt -> buildView();

        view.getSelectedDates().addListener(updateViewListener);

        view.showHeaderProperty().addListener(buildViewListener);
        view.showWeekNumbersProperty().addListener(buildViewListener);
        view.showMonthArrowsProperty().addListener(buildViewListener);
        view.showYearSpinnerProperty().addListener(buildViewListener);
        view.cellFactoryProperty().addListener(buildViewListener);
        view.weekFieldsProperty().addListener(buildViewListener);

        view.showTodayProperty().addListener(it -> updateView());

        Button todayButton = new Button("Today");
        todayButton.setOnAction(evt -> view.setYearMonth(YearMonth.from(view.getToday())));

        StackPane footer = new StackPane(todayButton);
        footer.visibleProperty().bind(view.showTodayButtonProperty());
        footer.managedProperty().bind(view.showTodayButtonProperty());
        footer.getStyleClass().add("footer");

        vBox = new VBox(header, weekdayGridPane, bodyGridPane, footer);
        vBox.getStyleClass().add("container");

        getChildren().add(vBox);

        buildView();

        view.showWeekNumbersProperty().addListener(it -> updateBodyConstraints());
        updateBodyConstraints();

        header.setViewOrder(-2000);
        weekdayGridPane.setViewOrder(-1000);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(vBox.widthProperty());
        clip.heightProperty().bind(vBox.heightProperty());
        vBox.setClip(clip);
    }

    private void updateBodyConstraints() {
        bodyGridPane.getRowConstraints().clear();
        bodyGridPane.getColumnConstraints().clear();

        weekdayGridPane.getRowConstraints().clear();
        weekdayGridPane.getColumnConstraints().clear();

        weekdayGridPane.getRowConstraints().add(createRowConstraints(-1));

        for (int row = 0; row < 6; row++) {
            bodyGridPane.getRowConstraints().add(createRowConstraints(row));
        }

        int numberOfColumns = getSkinnable().isShowWeekNumbers() ? 8 : 7;

        if (getSkinnable().isShowWeekNumbers()) {
            bodyGridPane.getColumnConstraints().add(createColumnConstraints(numberOfColumns, -1));
            weekdayGridPane.getColumnConstraints().add(createColumnConstraints(numberOfColumns, -1));
        }

        for (int col = 0; col < 7; col++) {
            bodyGridPane.getColumnConstraints().add(createColumnConstraints(numberOfColumns, col));
            weekdayGridPane.getColumnConstraints().add(createColumnConstraints(numberOfColumns, col));
        }
    }

    /**
     * Creates the constraints for the given column. The total number of columns can vary,
     * depending on whether the week number column is shown or not.
     *
     * @param numberOfColumns the number of total columns (either 7 or 8)
     * @param column          the index of the column for which to create the constraints, -1 indicates the column used for showing the "week of year" numbers
     * @return the column constraints for the given column
     */
    protected ColumnConstraints createColumnConstraints(int numberOfColumns, int column) {
        ColumnConstraints weekColumn = new ColumnConstraints();
        weekColumn.setHalignment(HPos.CENTER);
        weekColumn.setPercentWidth(100d / numberOfColumns);
        weekColumn.setFillWidth(true);
        return weekColumn;
    }

    /**
     * Creates the constraints for the given row.
     *
     * @param row the index of the row for which to create constraints, -1 indicates the row used for showing the "weekday name"
     * @return the row constraints for the given row
     */
    protected RowConstraints createRowConstraints(int row) {
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setFillHeight(true);
        rowConstraints.setMinHeight(Region.USE_PREF_SIZE);
        rowConstraints.setMaxHeight(Region.USE_PREF_SIZE);
        rowConstraints.setPrefHeight(Region.USE_COMPUTED_SIZE);
        return rowConstraints;
    }

    private LocalDate lastSelectedDate;

    private void buildView() {
        bodyGridPane.getChildren().clear();
        weekdayGridPane.getChildren().clear();

        CalendarView view = getSkinnable();

        boolean showWeekNumbers = view.isShowWeekNumbers();

        if (showWeekNumbers) {
            Label label = new Label();
            label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            label.getStyleClass().addAll("corner", WEEKDAY_NAME);
            weekdayGridPane.add(label, 0, 0);
        }

        DayOfWeek dayOfWeek = view.getFirstDayOfWeek();
        for (int i = 0; i < 7; i++) {
            dayOfWeekLabels[i] = new Label(dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()));
            dayOfWeekLabels[i].setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            dayOfWeekLabels[i].setAlignment(CENTER);
            dayOfWeekLabels[i].getStyleClass().add(WEEKDAY_NAME);
            weekdayGridPane.add(dayOfWeekLabels[i], showWeekNumbers ? i + 1 : i, 0);
            dayOfWeek = dayOfWeek.plus(1);
        }

        LocalDate date = getStartDate();

        int numberOfRows = 6;

        if (showWeekNumbers) {
            for (int row = 0; row < numberOfRows; row++) {
                weekNumberLabels[row] = new Label();
                weekNumberLabels[row].setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                weekNumberLabels[row].setAlignment(CENTER);
                weekNumberLabels[row].getStyleClass().add("week-number-label");
                bodyGridPane.add(weekNumberLabels[row], 0, row);
                date = date.plusWeeks(1);
            }
        }

        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < 7; col++) {
                Callback<CalendarView, DateCell> cellFactory = view.getCellFactory();
                DateCell cell = cellFactory.call(view);
                GridPane.setHgrow(cell, ALWAYS);
                GridPane.setVgrow(cell, ALWAYS);
                cell.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> handleMouseClick(evt, cell.getDate()));
                cellsMap.put(getKey(row, col), cell);
                bodyGridPane.add(cell, showWeekNumbers ? col + 1 : col, row);
                date = date.plusDays(1);
            }
        }

        // after a build we always have to update the view
        updateView();
    }

    private String getKey(int row, int col) {
        return row + "/" + col;
    }

    private void updateView() {
        lastSelectedDate = null;

        CalendarView view = getSkinnable();
        YearMonth yearMonth = view.getYearMonth();

        displayedYearMonth = yearMonth;

        // update the days (1 to 31) plus padding days

        LocalDate date = getStartDate();

        if (view.isShowWeekNumbers()) {
            for (int i = 0; i < 6; i++) {
                int weekOfYear = date.get(view.getWeekFields().weekOfYear());
                weekNumberLabels[i].setText(Integer.toString(weekOfYear));
                date = date.plusWeeks(1);
            }
        }

        // reset date after the loop
        date = getStartDate();

        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                LocalDate localDate = LocalDate.from(date);

                DateCell cell = cellsMap.get(getKey(row, col));
                cell.setDate(localDate);
                cell.getStyleClass().removeAll(TODAY, PREVIOUS_MONTH, NEXT_MONTH, WEEKEND_DAY, SELECTED);

                if (view.getSelectedDates().contains(date)) {
                    cell.getStyleClass().add(SELECTED);
                }

                if (view.isShowToday() && date.equals(view.getToday())) {
                    cell.getStyleClass().add(TODAY);
                }

                if (YearMonth.from(date).isBefore(yearMonth)) {
                    cell.getStyleClass().add(PREVIOUS_MONTH);
                } else if (YearMonth.from(date).isAfter(yearMonth)) {
                    cell.getStyleClass().add(NEXT_MONTH);
                }

                if (view.getWeekendDays().contains(date.getDayOfWeek())) {
                    cell.getStyleClass().add(WEEKEND_DAY);
                }

                date = date.plusDays(1);
            }
        }
    }

    private LocalDate getStartDate() {
        YearMonth yearMonth = getSkinnable().getYearMonth();
        return adjustToFirstDayOfWeek(
                LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1),
                getSkinnable().getFirstDayOfWeek());
    }

    private LocalDate adjustToFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate newDate = date.with(DAY_OF_WEEK, firstDayOfWeek.getValue());
        if (newDate.isAfter(date)) {
            newDate = newDate.minusWeeks(1);
        }

        return newDate;
    }

    private void handleMouseClick(MouseEvent evt, LocalDate date) {
        if (evt.getButton() != MouseButton.PRIMARY || evt.getClickCount() != 1) {
            return;
        }

        CalendarView view = getSkinnable();

        boolean multiSelect = evt.isShiftDown() || evt.isShortcutDown();
        if (!multiSelect || (view.getSelectionMode().equals(SINGLE) && !evt.isControlDown())) {
            view.getSelectedDates().clear();
        }

        if (evt.isShiftDown()) {
            if (lastSelectedDate != null) {
                LocalDate st = lastSelectedDate;
                LocalDate et = date;
                if (date.isBefore(st)) {
                    st = date;
                    et = lastSelectedDate;
                }

                do {
                    view.getSelectedDates().add(st);
                    st = st.plusDays(1);
                } while (!et.isBefore(st));
            } else {
                view.getSelectedDates().clear();
                view.getSelectedDates().add(date);
            }
        } else {
            if (view.getSelectedDates().contains(date)) {
                view.getSelectedDates().remove(date);
            } else {
                view.getSelectedDates().add(date);
            }
        }

        lastSelectedDate = date;

        if (!date.getMonth().equals(view.getYearMonth().getMonth())) {
            view.setYearMonth(YearMonth.from(date));
        }

    }
}
