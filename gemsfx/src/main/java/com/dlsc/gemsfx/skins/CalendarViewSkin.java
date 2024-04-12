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
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.YearMonthView;
import com.dlsc.gemsfx.YearView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.geometry.HPos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.stage.Window;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.time.temporal.ChronoField.DAY_OF_WEEK;
import static javafx.geometry.Pos.CENTER;
import static javafx.scene.layout.Priority.ALWAYS;

public class CalendarViewSkin extends SkinBase<CalendarView> {

    private static final String WEEKDAY_NAME = "weekday-name";
    private static final String TODAY = "today";
    private static final String PREVIOUS_MONTH = "previous-month";
    private static final String NEXT_MONTH = "next-month";
    private static final String WEEKEND_DAY = "weekend-day";
    private static final String SELECTED = "selected";
    private static final String RANGE_START_DATE = "range-start";
    private static final String RANGE_END_DATE = "range-end";
    private static final String RANGE_DATE = "range-date";
    private static final String DROPDOWN = "dropdown";

    private final Label monthLabel;

    private final Label yearLabel;

    private enum ViewMode {
        DATE, MONTH, YEAR
    }

    private final GridPane bodyGridPane;

    private final GridPane weekdayGridPane;

    private final VBox container;

    private final YearMonthView yearMonthView;

    private final YearView yearView;

    private final Map<String, DateCell> cellsMap = new HashMap<>();

    private final Label[] dayOfWeekLabels = new Label[7];

    private final Label[] weekNumberLabels = new Label[6];

    private final InvalidationListener updateViewListener = (Observable it) -> updateView();

    private final WeakInvalidationListener weakUpdateViewListener = new WeakInvalidationListener(updateViewListener);

    private final ObjectProperty<ViewMode> viewMode = new SimpleObjectProperty<>(this, "viewMode", ViewMode.DATE);

    private YearMonth displayedYearMonth;

    private final ChangeListener<Boolean> windowShowingListener = (obs, oldShowing, newShowing) -> {
        if (!newShowing) {
            viewMode.set(ViewMode.DATE);
        }
    };

    private final WeakChangeListener weakWindowShowingListener = new WeakChangeListener(windowShowingListener);

    public CalendarViewSkin(CalendarView view) {
        super(view);

        Scene scene = view.getScene();
        Window window = scene.getWindow();
        window.showingProperty().addListener(weakWindowShowingListener);

        bodyGridPane = new GridPane();
        bodyGridPane.setAlignment(CENTER);
        bodyGridPane.getStyleClass().addAll("grid-pane", "body-grid-pane");

        weekdayGridPane = new GridPane();
        weekdayGridPane.setAlignment(CENTER);
        weekdayGridPane.getStyleClass().addAll("grid-pane", "weekday-grid-pane");

        monthLabel = new Label();
        monthLabel.getStyleClass().addAll("date-label", "month-label");
        monthLabel.setMinWidth(Region.USE_PREF_SIZE);
        monthLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getYearMonth() != null ? view.getYearMonth().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()) : "", view.yearMonthProperty()));
        monthLabel.visibleProperty().bind(view.showMonthProperty());
        monthLabel.managedProperty().bind(view.showMonthProperty());
        monthLabel.setOnMouseClicked(evt -> {
            if (view.isMonthSelectionViewEnabled()) {
                viewMode.set(ViewMode.MONTH);
            }
        });

        yearLabel = new Label();
        yearLabel.getStyleClass().addAll("date-label", "year-label");
        yearLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getYearMonth() != null ? Integer.toString(view.getYearMonth().getYear()) : "", view.yearMonthProperty()));
        yearLabel.visibleProperty().bind(view.showYearProperty());
        yearLabel.managedProperty().bind(view.showYearProperty());
        yearLabel.setOnMouseClicked(evt -> {
            if (view.isYearSelectionViewEnabled()) {
                viewMode.set(ViewMode.YEAR);
            }
        });

        StackPane incrementYearArrow = new StackPane();
        incrementYearArrow.getStyleClass().add("arrow");

        StackPane incrementYearButton = new StackPane(incrementYearArrow);
        incrementYearButton.getStyleClass().add("increment-year-button");
        incrementYearButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().plusYears(1)));
        incrementYearButton.disableProperty().bind(view.disableNextYearButtonProperty());

        StackPane decrementYearArrow = new StackPane();
        decrementYearArrow.getStyleClass().add("arrow");

        StackPane decrementYearButton = new StackPane(decrementYearArrow);
        decrementYearButton.getStyleClass().add("decrement-year-button");
        decrementYearButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().minusYears(1)));
        decrementYearButton.disableProperty().bind(view.disablePreviousYearButtonProperty());

        VBox yearSpinnerBox = new VBox(incrementYearButton, decrementYearButton);
        yearSpinnerBox.getStyleClass().add("year-spinner");
        yearSpinnerBox.setMaxWidth(Region.USE_PREF_SIZE);
        yearSpinnerBox.visibleProperty().bind(view.showYearProperty().and(view.yearDisplayModeProperty().isEqualTo(CalendarView.YearDisplayMode.TEXT_AND_SPINNER)));
        yearSpinnerBox.managedProperty().bind(yearSpinnerBox.visibleProperty());

        HBox header = new HBox();
        header.getStyleClass().add("header");

        StackPane previousMonthArrow = new StackPane();
        previousMonthArrow.getStyleClass().add("arrow");

        StackPane previousArrowButton = new StackPane(previousMonthArrow);
        previousArrowButton.getStyleClass().addAll("arrow-button", "previous-month-button");
        previousArrowButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().minusMonths(1)));
        previousArrowButton.visibleProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));
        previousArrowButton.managedProperty().bind(previousArrowButton.visibleProperty());
        previousArrowButton.disableProperty().bind(view.disablePreviousMonthButtonProperty());

        StackPane nextMonthArrow = new StackPane();
        nextMonthArrow.getStyleClass().add("arrow");

        StackPane nextMonthArrowButton = new StackPane(nextMonthArrow);
        nextMonthArrowButton.getStyleClass().addAll("arrow-button", "next-month-button");
        nextMonthArrowButton.setOnMouseClicked(evt -> view.setYearMonth(view.getYearMonth().plusMonths(1)));
        nextMonthArrowButton.visibleProperty().bind(view.showMonthArrowsProperty().and(view.showMonthProperty()));
        nextMonthArrowButton.managedProperty().bind(nextMonthArrowButton.visibleProperty());
        nextMonthArrowButton.disableProperty().bind(view.disableNextMonthButtonProperty());

        StackPane monthDropdownArrow = new StackPane();
        monthDropdownArrow.getStyleClass().add("arrow");

        StackPane monthDropdownArrowButton = new StackPane(monthDropdownArrow);
        monthDropdownArrowButton.getStyleClass().addAll("dropdown-button", "month-dropdown-button");
        monthDropdownArrowButton.visibleProperty().bind(
                view.monthSelectionViewEnabledProperty()
                        .and(view.monthDisplayModeProperty().isEqualTo(CalendarView.MonthDisplayMode.TEXT_AND_DROPDOWN))
                        .and(view.showMonthProperty())
        );
        monthDropdownArrowButton.managedProperty().bind(monthDropdownArrowButton.visibleProperty());
        monthDropdownArrowButton.disableProperty().bind(view.disableMonthDropdownButtonProperty());
        monthLabel.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (view.isMonthSelectionViewEnabled() && view.getMonthDisplayMode() == CalendarView.MonthDisplayMode.TEXT_AND_DROPDOWN) {
                        return monthDropdownArrowButton;
                    }
                    return null;
                },
                view.monthDisplayModeProperty(), view.monthSelectionViewEnabledProperty()));

        StackPane yearDropdownArrow = new StackPane();
        yearDropdownArrow.getStyleClass().add("arrow");

        StackPane yearDropdownArrowButton = new StackPane(yearDropdownArrow);
        yearDropdownArrowButton.getStyleClass().addAll("dropdown-button", "year-dropdown-button");
        yearDropdownArrowButton.visibleProperty().bind(
                view.yearSelectionViewEnabledProperty()
                        .and(view.yearDisplayModeProperty().isEqualTo(CalendarView.YearDisplayMode.TEXT_AND_DROPDOWN))
                        .and(view.showYearProperty())
        );
        yearDropdownArrowButton.managedProperty().bind(yearDropdownArrowButton.visibleProperty());
        yearDropdownArrowButton.disableProperty().bind(view.disableYearDropdownButtonProperty());

        yearLabel.graphicProperty().bind(Bindings.createObjectBinding(() -> {
                    if (view.isYearSelectionViewEnabled() && view.getYearDisplayMode() == CalendarView.YearDisplayMode.TEXT_AND_DROPDOWN) {
                        return yearDropdownArrowButton;
                    }
                    return null;
                },
                view.yearDisplayModeProperty(), view.yearSelectionViewEnabledProperty()));

        Spacer leftSpacer = new Spacer();
        leftSpacer.getStyleClass().add("left");

        Spacer rightSpacer = new Spacer();
        rightSpacer.getStyleClass().add("right");

        updateHeader(header, previousArrowButton, leftSpacer, yearSpinnerBox, rightSpacer, nextMonthArrowButton);
        view.headerLayoutProperty().addListener(it -> updateHeader(header, previousArrowButton, leftSpacer, yearSpinnerBox, rightSpacer, nextMonthArrowButton));

        InvalidationListener updateViewListener = evt -> updateView();
        view.yearMonthProperty().addListener(evt -> {
            if (displayedYearMonth == null || !displayedYearMonth.equals(view.getYearMonth())) {
                updateView();
            }
        });

        InvalidationListener buildViewListener = evt -> buildView();
        view.showWeekNumbersProperty().addListener(buildViewListener);
        view.showMonthArrowsProperty().addListener(buildViewListener);
        view.cellFactoryProperty().addListener(buildViewListener);
        view.markSelectedDaysOfPreviousOrNextMonthProperty().addListener(buildViewListener);

        view.showTodayProperty().addListener(updateViewListener);

        Button todayButton = new Button();
        todayButton.textProperty().bind(view.todayTextProperty());
        todayButton.getStyleClass().add("today-button");
        todayButton.setOnAction(evt -> view.setYearMonth(YearMonth.from(view.getToday())));
        todayButton.setMaxWidth(Double.MAX_VALUE);

        StackPane footer = new StackPane(todayButton);
        footer.visibleProperty().bind(view.showTodayButtonProperty());
        footer.managedProperty().bind(view.showTodayButtonProperty());
        footer.getStyleClass().add("footer");

        container = new VBox(header, weekdayGridPane, bodyGridPane, footer);
        container.getStyleClass().add("container");

        yearMonthView = view.getYearMonthView();
        yearMonthView.getStyleClass().add("inner-year-month-view");
        yearMonthView.setShowYear(false);
        yearMonthView.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (!evt.isConsumed()) {
                viewMode.set(ViewMode.DATE);
            }
        });
        yearMonthView.addEventHandler(TouchEvent.TOUCH_PRESSED, evt -> {
            if (!evt.isConsumed()) {
                viewMode.set(ViewMode.DATE);
            }
        });

        yearMonthView.earliestMonthProperty().bind(Bindings.createObjectBinding(() -> view.getEarliestDate() != null ? YearMonth.from(view.getEarliestDate()) : null, view.earliestDateProperty()));
        yearMonthView.latestMonthProperty().bind(Bindings.createObjectBinding(() -> view.getLatestDate() != null ? YearMonth.from(view.getLatestDate()) : null, view.latestDateProperty()));

        view.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.ESCAPE)) {
                if (!evt.isConsumed() && !viewMode.get().equals(ViewMode.DATE)) {
                    viewMode.set(ViewMode.DATE);
                    evt.consume();
                }
            }
        });

        yearView = view.getYearView();
        yearView.earliestYearProperty().bind(Bindings.createObjectBinding(() -> view.getEarliestDate() != null ? Year.from(view.getEarliestDate()) : null, view.earliestDateProperty()));
        yearView.latestYearProperty().bind(Bindings.createObjectBinding(() -> view.getLatestDate() != null ? Year.from(view.getLatestDate()) : null, view.latestDateProperty()));
        yearView.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (!evt.isConsumed()) {
                viewMode.set(ViewMode.DATE);
            }
        });
        yearView.addEventHandler(TouchEvent.TOUCH_PRESSED, evt -> {
            if (!evt.isConsumed()) {
                viewMode.set(ViewMode.DATE);
            }
        });

        buildView();

        view.showWeekNumbersProperty().addListener(it -> updateBodyConstraints());
        updateBodyConstraints();

        header.setViewOrder(-2000);
        weekdayGridPane.setViewOrder(-1000);

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(container.widthProperty());
        clip.heightProperty().bind(container.heightProperty());
        container.setClip(clip);

        view.selectionModelProperty().addListener(it -> bindSelectionModel(view.getSelectionModel()));
        bindSelectionModel(view.getSelectionModel());

        StackPane stackPane = new StackPane(yearView, yearMonthView, container);
        stackPane.getStyleClass().add("stack-pane");
        getChildren().setAll(stackPane);
        updateView();

        viewMode.addListener(obs -> updateViewMode());
        updateViewMode();

        InvalidationListener updateStyleClassesListener = it -> updateStyleClasses();
        view.monthSelectionViewEnabledProperty().addListener(updateStyleClassesListener);
        view.yearSelectionViewEnabledProperty().addListener(updateStyleClassesListener);

        updateStyleClasses();
    }

    private void updateHeader(HBox header, StackPane previousArrowButton, Spacer leftSpacer, VBox yearSpinnerBox, Spacer rightSpacer, StackPane nextMonthArrowButton) {
        switch (getSkinnable().getHeaderLayout()) {
            case CENTER:
                header.getChildren().setAll(previousArrowButton, leftSpacer, monthLabel, yearLabel, yearSpinnerBox, rightSpacer, nextMonthArrowButton);
                break;
            case LEFT:
                header.getChildren().setAll(monthLabel, yearLabel, yearSpinnerBox, rightSpacer, previousArrowButton, nextMonthArrowButton);
                break;
            case RIGHT:
                header.getChildren().setAll(previousArrowButton, nextMonthArrowButton, leftSpacer, monthLabel, yearLabel);
                break;
        }
    }

    private void updateStyleClasses() {
        CalendarView view = getSkinnable();
        if (view.isMonthSelectionViewEnabled()) {
            if (!monthLabel.getStyleClass().contains(DROPDOWN)) {
                monthLabel.getStyleClass().add(DROPDOWN);
            }
        } else {
            monthLabel.getStyleClass().remove(DROPDOWN);
        }
        if (view.isYearSelectionViewEnabled()) {
            if (!yearLabel.getStyleClass().contains(DROPDOWN)) {
                yearLabel.getStyleClass().add(DROPDOWN);
            }
        } else {
            yearLabel.getStyleClass().remove(DROPDOWN);
        }
    }

    private void bindSelectionModel(SelectionModel model) {
        model.selectionModeProperty().addListener(weakUpdateViewListener);
        model.selectedDateProperty().addListener(weakUpdateViewListener);
        model.selectedEndDateProperty().addListener(weakUpdateViewListener);
        model.selectedDatesProperty().addListener(weakUpdateViewListener);
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
     * @param columnIndex     the index of the column for which to create the constraints, -1 indicates the column used for showing the "week of year" numbers
     * @return the column constraints for the given column
     */
    protected ColumnConstraints createColumnConstraints(int numberOfColumns, int columnIndex) {
        ColumnConstraints column = new ColumnConstraints();
        column.setHalignment(HPos.CENTER);
        if (columnIndex == -1) {
            column.setPrefWidth(getSkinnable().getWeekNumberColumnWidth());
        } else {
            column.setPercentWidth(100d / numberOfColumns);
        }
        column.setFillWidth(true);
        return column;
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

        DayOfWeek dayOfWeek = getFirstDayOfWeek();
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

        Callback<CalendarView, DateCell> cellFactory = view.getCellFactory();

        final LocalDate finalDate = date;
        for (int row = 0; row < numberOfRows; row++) {
            for (int col = 0; col < 7; col++) {

                DateCell cell = cellFactory.call(view);
                GridPane.setHgrow(cell, ALWAYS);
                GridPane.setVgrow(cell, ALWAYS);
                cellsMap.put(getKey(row, col), cell);

                cell.visibleProperty().bind(Bindings.createBooleanBinding(() -> view.isShowDaysOfPreviousOrNextMonth() || cell.getDate() != null && YearMonth.from(cell.getDate()).equals(view.getYearMonth()), cell.itemProperty(), view.showDaysOfPreviousOrNextMonthProperty()));
                cell.disableProperty().bind(Bindings.createBooleanBinding(() -> {
                    LocalDate cellDate = cell.getItem();
                    if (cellDate != null) {
                        LocalDate earliestDate = view.getEarliestDate();
                        if (earliestDate != null && cellDate.isBefore(earliestDate)) {
                            return true;
                        }
                        LocalDate latestDate = view.getLatestDate();
                        if (latestDate != null && cellDate.isAfter(latestDate)) {
                            return true;
                        }
                        Callback<LocalDate, Boolean> dateFilter = view.getDateFilter();
                        return dateFilter != null && !dateFilter.call(cellDate);
                    }
                    return false;
                }, view.earliestDateProperty(), view.latestDateProperty(), view.dateFilterProperty(), cell.itemProperty()));

                bodyGridPane.add(cell, showWeekNumbers ? col + 1 : col, row);

                installSelectionSupport(cell);

                date = date.plusDays(1);
            }
        }

        // after a build, we always have to update the view
        updateView();
    }

    private void installSelectionSupport(DateCell cell) {
        cell.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            SelectionModel selectionModel = getSkinnable().getSelectionModel();

            switch (selectionModel.getSelectionMode()) {
                case SINGLE_DATE:
                    selectionModel.select(cell.getDate());
                    break;
                case MULTIPLE_DATES:
                    if (selectionModel.isSelected(cell.getDate())) {
                        selectionModel.clearSelection(cell.getDate());
                    } else {
                        selectionModel.select(cell.getDate());
                    }
                    break;
                case DATE_RANGE:
                    if (selectionModel.getSelectedDate() != null && selectionModel.getSelectedEndDate() != null) {
                        selectionModel.clearSelection();
                    }

                    if (selectionModel.getSelectedDate() == null) {
                        selectionModel.select(cell.getDate());
                    } else {
                        if (cell.getDate().isBefore(selectionModel.getSelectedDate())) {
                            // swap the dates, as the new end date is BEFORE the current start date
                            LocalDate endDate = selectionModel.getSelectedDate();
                            selectionModel.clearAndSelect(cell.getDate());
                            selectionModel.setSelectedEndDate(endDate);
                        } else {
                            selectionModel.setSelectedEndDate(cell.getDate());
                        }
                    }
                    break;
            }
        });
    }

    private String getKey(int row, int col) {
        return row + "/" + col;
    }

    private final ChangeListener<YearMonth> yearMonthChangeListener = (obs, oldV, newV) -> {
        getSkinnable().setYearMonth(newV);
        viewMode.set(ViewMode.DATE);
    };

    private final ChangeListener<Year> yearChangeListener = (obs, oldV, newV) -> {
        YearMonth yearMonth = Optional.ofNullable(getSkinnable().getYearMonth()).orElse(YearMonth.now());
        getSkinnable().setYearMonth(newV.atMonth(yearMonth.getMonth()));
        viewMode.set(ViewMode.DATE);
    };

    private void updateViewMode() {
        yearMonthView.valueProperty().removeListener(yearMonthChangeListener);
        yearView.valueProperty().removeListener(yearChangeListener);

        container.setVisible(false);
        yearMonthView.setVisible(false);
        yearView.setVisible(false);

        if (viewMode.get() == ViewMode.DATE) {
            container.toFront();
            container.setVisible(true);
        } else if (viewMode.get() == ViewMode.MONTH) {
            yearMonthView.toFront();
            yearMonthView.setVisible(true);
            yearMonthView.setValue(getSkinnable().getYearMonth());
            yearMonthView.valueProperty().addListener(yearMonthChangeListener);
        } else if (viewMode.get() == ViewMode.YEAR) {
            yearView.toFront();
            yearView.setVisible(true);
            YearMonth yearMonth = Optional.ofNullable(getSkinnable().getYearMonth()).orElse(YearMonth.now());
            yearView.setValue(Year.of(yearMonth.getYear()));
            yearView.valueProperty().addListener(yearChangeListener);
        }
    }

    private void updateView() {
        CalendarView view = getSkinnable();
        YearMonth yearMonth = view.getYearMonth();

        displayedYearMonth = yearMonth;

        // update the days (1 to 31) plus padding days

        LocalDate date = getStartDate();

        if (view.isShowWeekNumbers()) {
            for (int i = 0; i < 6; i++) {
                int weekOfYear = date.get(getWeekFields().weekOfYear());
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
                cell.updateItem(localDate, false);
                cell.getStyleClass().removeAll(TODAY, PREVIOUS_MONTH, NEXT_MONTH, WEEKEND_DAY, SELECTED, RANGE_START_DATE, RANGE_END_DATE, RANGE_DATE);

                if (view.isShowToday() && date.equals(view.getToday())) {
                    cell.getStyleClass().add(TODAY);
                }

                YearMonth cellYearMonth = YearMonth.from(date);
                if (cellYearMonth.isBefore(yearMonth)) {
                    cell.getStyleClass().add(PREVIOUS_MONTH);
                } else if (cellYearMonth.isAfter(yearMonth)) {
                    cell.getStyleClass().add(NEXT_MONTH);
                }

                if (view.getWeekendDays().contains(date.getDayOfWeek())) {
                    cell.getStyleClass().add(WEEKEND_DAY);
                }

                if (view.getSelectionModel().isSelected(localDate)) {
                    if (cellYearMonth.equals(yearMonth) || view.isMarkSelectedDaysOfPreviousOrNextMonth()) {
                        cell.getStyleClass().add(SELECTED);
                    }

                    if (Objects.equals(view.getSelectionModel().getSelectionMode(), SelectionModel.SelectionMode.DATE_RANGE)) {

                        if (Objects.equals(view.getSelectionModel().getSelectedDate(), localDate)) {
                            cell.getStyleClass().add(RANGE_START_DATE);
                        } else if (Objects.equals(view.getSelectionModel().getSelectedEndDate(), localDate)) {
                            cell.getStyleClass().add(RANGE_END_DATE);
                        } else {
                            cell.getStyleClass().add(RANGE_DATE);
                        }
                    }
                }

                date = date.plusDays(1);
            }
        }
    }

    private DayOfWeek getFirstDayOfWeek() {
        return getWeekFields().getFirstDayOfWeek();
    }

    private WeekFields getWeekFields() {
        return WeekFields.of(Locale.getDefault());
    }

    private LocalDate getStartDate() {
        YearMonth yearMonth = getSkinnable().getYearMonth();
        return adjustToFirstDayOfWeek(LocalDate.of(yearMonth.getYear(), yearMonth.getMonth(), 1), getFirstDayOfWeek());
    }

    private LocalDate adjustToFirstDayOfWeek(LocalDate date, DayOfWeek firstDayOfWeek) {
        LocalDate newDate = date.with(DAY_OF_WEEK, firstDayOfWeek.getValue());
        if (newDate.isAfter(date)) {
            newDate = newDate.minusWeeks(1);
        }

        return newDate;
    }
}
