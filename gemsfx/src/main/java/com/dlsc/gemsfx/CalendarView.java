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

package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.CalendarViewSkin;
import com.dlsc.gemsfx.skins.DateCellSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.EnumConverter;
import javafx.scene.control.Cell;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.lang.Double.MAX_VALUE;
import static java.util.Objects.requireNonNull;
import static javafx.geometry.Pos.CENTER;

/**
 * Displays a given month of a given year. The view can be configured in many
 * ways:
 * <ul>
 * <li>Show / hide the name of the month</li>
 * <li>Show / hide the year</li>
 * <li>Show / hide arrow buttons for changing the month</li>
 * <li>Show / hide arrow buttons for changing the year</li>
 * <li>Show / hide today</li>
 * <li>Show / hide a button for going to today</li>
 * <li>Show / hide usage colors</li>
 * </ul>
 * Additionally the application can choose from two different behaviours when
 * the user clicks on a date:
 * <ol>
 * <li>Perform a selection / select the date</li>
 * <li>Show details of the date (by default shows a popover with all entries on
 * that date)</li>
 * </ol>
 * The image below shows the visual appearance of this control:
 * <img src="doc-files/date-picker.png" alt="Date Picker">
 */
public class CalendarView extends Control {

    private static final YearDisplayMode DEFAULT_YEAR_DISPLAY_MODE = YearDisplayMode.TEXT_ONLY;
    private static final MonthDisplayMode DEFAULT_MONTH_DISPLAY_MODE = MonthDisplayMode.TEXT_ONLY;
    private YearMonthView yearMonthView;

    private YearView yearView;

    /**
     * Constructs a new view.
     */
    public CalendarView() {
        getStyleClass().add("calendar-view");

        addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> requestFocus());
        addEventFilter(TouchEvent.TOUCH_PRESSED, evt -> requestFocus());

        setFocusTraversable(true);
        setCellFactory(view -> new DateCell());
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return requireNonNull(CalendarView.class.getResource("calendar-view.css")).toExternalForm();
    }

    /**
     * Returns the view used to display a month selection to the user. Applications
     * can choose to override this method and return their own custom view.
     *
     * @return the view used for showing and selecting a month
     */
    public YearMonthView getYearMonthView() {
        if (yearMonthView == null) {
            yearMonthView = new YearMonthView();
        }
        return yearMonthView;
    }

    /**
     * Returns the view used to display a year selection to the user. Applications
     * can choose to override this method and return their own custom view.
     *
     * @return the view used for showing and selecting a year
     */
    public YearView getYearView() {
        if (yearView == null) {
            yearView = new YearView();
        }
        return yearView;
    }

    private final ObjectProperty<YearMonth> yearMonth = new SimpleObjectProperty<>(this, "yearMonth", YearMonth.now());

    /**
     * Stores the year and month shown by the control.
     *
     * @return the year and month
     */
    public final ObjectProperty<YearMonth> yearMonthProperty() {
        return yearMonth;
    }

    /**
     * Returns the value of {@link #yearMonthProperty()}.
     *
     * @return the year and month
     */
    public final YearMonth getYearMonth() {
        return yearMonth.get();
    }

    public void setYearMonth(YearMonth yearMonth) {
        this.yearMonth.set(yearMonth);
    }

    private final ObservableSet<DayOfWeek> weekendDays = FXCollections.observableSet();

    /**
     * Returns the days of the week that are considered to be weekend days, for
     * example Saturday and Sunday, or Friday and Saturday.
     *
     * @return the weekend days
     */
    public ObservableSet<DayOfWeek> getWeekendDays() {
        return weekendDays;
    }

    private final BooleanProperty showWeekNumbers = new SimpleBooleanProperty(this, "showWeekNumbers");

    /**
     * Controls whether the view will show week numbers.
     *
     * @return true if week numbers are shown
     */
    public final BooleanProperty showWeekNumbersProperty() {
        return showWeekNumbers;
    }

    /**
     * Sets the value of {@link #showWeekNumbersProperty()}.
     *
     * @param show if true will show week numbers
     */
    public final void setShowWeekNumbers(boolean show) {
        showWeekNumbersProperty().set(show);
    }

    /**
     * Returns the value of {@link #showWeekNumbersProperty()}.
     *
     * @return true if week numbers will be shown
     */
    public final boolean isShowWeekNumbers() {
        return showWeekNumbersProperty().get();
    }

    private final BooleanProperty markSelectedDaysOfPreviousOrNextMonth = new SimpleBooleanProperty(this, "markSelectedDaysOfPreviousOrNextMonth", true);

    public final boolean isMarkSelectedDaysOfPreviousOrNextMonth() {
        return markSelectedDaysOfPreviousOrNextMonth.get();
    }

    /**
     * Determines whether selected dates will be marked as such when they are being displayed at the
     * beginning of the next month or at the end of the previous month.
     *
     * @return true if days not belonging to the current month will be marked selected
     */
    public final BooleanProperty markSelectedDaysOfPreviousOrNextMonthProperty() {
        return markSelectedDaysOfPreviousOrNextMonth;
    }

    public final void setMarkSelectedDaysOfPreviousOrNextMonth(boolean markSelectedDaysOfPreviousOrNextMonth) {
        this.markSelectedDaysOfPreviousOrNextMonth.set(markSelectedDaysOfPreviousOrNextMonth);
    }

    private final BooleanProperty showDaysOfPreviousOrNextMonth = new SimpleBooleanProperty(this, "showDaysOfPreviousOrNextMonth", true);

    public final boolean isShowDaysOfPreviousOrNextMonth() {
        return showDaysOfPreviousOrNextMonth.get();
    }

    /**
     * By default, the calendar for a given month might also show some days of the previous
     * and the next month. This property allows applications to hide them if needed.
     *
     * @return true if the calendar will be filled up with days of the previous and the next month
     */
    public final BooleanProperty showDaysOfPreviousOrNextMonthProperty() {
        return showDaysOfPreviousOrNextMonth;
    }

    public final void setShowDaysOfPreviousOrNextMonth(boolean showDaysOfPreviousOrNextMonth) {
        this.showDaysOfPreviousOrNextMonth.set(showDaysOfPreviousOrNextMonth);
    }

    private final BooleanProperty showToday = new SimpleBooleanProperty(this, "showToday", true);

    private final ObjectProperty<LocalDate> today = new SimpleObjectProperty<>(this, "today", LocalDate.now());

    /**
     * Stores the date that is considered to represent "today". This property is
     * initialized with {@link LocalDate#now()} but can be any date.
     *
     * @return the date representing "today"
     */
    public final ObjectProperty<LocalDate> todayProperty() {
        return today;
    }

    /**
     * Sets the value of {@link #todayProperty()}.
     *
     * @param date the date representing "today"
     */
    public final void setToday(LocalDate date) {
        requireNonNull(date);
        todayProperty().set(date);
    }

    /**
     * Returns the value of {@link #todayProperty()}.
     *
     * @return the date representing "today"
     */
    public final LocalDate getToday() {
        return todayProperty().get();
    }

    /**
     * A flag used to indicate that the view will mark the area that represents
     * the value of {@link #todayProperty()}. By default, this area will be
     * filled with a different color (red) than the rest (white).
     * <img src="doc-files/all-day-view-today.png" alt="All Day View Today">
     *
     * @return true if today will be shown differently
     */
    public final BooleanProperty showTodayProperty() {
        return showToday;
    }

    /**
     * Returns the value of {@link #showTodayProperty()}.
     *
     * @return true if today will be highlighted visually
     */
    public final boolean isShowToday() {
        return showTodayProperty().get();
    }

    /**
     * Sets the value of {@link #showTodayProperty()}.
     *
     * @param show if true today will be highlighted visually
     */
    public final void setShowToday(boolean show) {
        showTodayProperty().set(show);
    }

    private final BooleanProperty disablePreviousMonthButton = new SimpleBooleanProperty(this, "disablePreviousMonth");

    public final boolean isDisablePreviousMonthButton() {
        return disablePreviousMonthButton.get();
    }

    /**
     * A property to control whether the "show previous month" button will be disabled or not.
     * This property can be very useful when working with (for example) two calendars used
     * for selecting a date range. Then the second calendar should never show a month that
     * is earlier than the first calendar.
     *
     * @return true if the button used for going to the next month is currently disabled
     */
    public final BooleanProperty disablePreviousMonthButtonProperty() {
        return disablePreviousMonthButton;
    }

    public final void setDisablePreviousMonthButton(boolean disablePreviousMonthButton) {
        this.disablePreviousMonthButton.set(disablePreviousMonthButton);
    }

    private final BooleanProperty disableNextMonthButton = new SimpleBooleanProperty(this, "disablePreviousMonth");

    public final boolean isDisableNextMonthButton() {
        return disableNextMonthButton.get();
    }

    /**
     * A property to control whether the "show next month" button will be disabled or not.
     * This property can be very useful when working with (for example) two calendars used
     * for selecting a date range. Then the first calendar should never show a month that
     * is later than the second calendar.
     *
     * @return true if the button used for going to the next month is currently disabled
     */
    public final BooleanProperty disableNextMonthButtonProperty() {
        return disableNextMonthButton;
    }

    public final void setDisableNextMonthButton(boolean disableNextMonthButton) {
        this.disableNextMonthButton.set(disableNextMonthButton);
    }

    private final BooleanProperty disableNextYearButton = new SimpleBooleanProperty(this, "disableNextYearButton");

    public final boolean isDisableNextYearButton() {
        return disableNextYearButton.get();
    }

    /**
     * A property to control whether the "show next year" button will be disabled or not.
     * This property can be very useful when working with (for example) two calendars used
     * for selecting a date range. Then the first calendar should never show a year that
     * is later than the second calendar.
     *
     * @return true if the button used for going to the next year is currently disabled
     */
    public final BooleanProperty disableNextYearButtonProperty() {
        return disableNextYearButton;
    }

    public final void setDisableNextYearButton(boolean disableNextYearButton) {
        this.disableNextYearButton.set(disableNextYearButton);
    }

    private final BooleanProperty disablePreviousYearButton = new SimpleBooleanProperty(this, "disablePreviousYearButton");

    public final boolean isDisablePreviousYearButton() {
        return disablePreviousYearButton.get();
    }

    /**
     * A property to control whether the "show previous year" button will be disabled or not.
     * This property can be very useful when working with (for example) two calendars used
     * for selecting a date range. Then the second calendar should never show a year that
     * is earlier than the first calendar.
     *
     * @return true if the button used for going to the next year is currently disabled
     */
    public final BooleanProperty disablePreviousYearButtonProperty() {
        return disablePreviousYearButton;
    }

    public final void setDisablePreviousYearButton(boolean disablePreviousYearButton) {
        this.disablePreviousYearButton.set(disablePreviousYearButton);
    }

    public final BooleanProperty disableMonthDropdownButton = new SimpleBooleanProperty(this, "disableMonthDropdownButton", false);

    public final boolean isDisableMonthDropdownButton() {
        return disableMonthDropdownButton.get();
    }

    /**
     * A property to control whether the "show month view" button will be disabled or not.
     *
     * @return true if the button used for showing the month selection view should be disabled
     */
    public final BooleanProperty disableMonthDropdownButtonProperty() {
        return disableMonthDropdownButton;
    }

    public final void setDisableMonthDropdownButton(boolean disableMonthDropdownButton) {
        this.disableMonthDropdownButton.set(disableMonthDropdownButton);
    }

    public final BooleanProperty disableYearDropdownButton = new SimpleBooleanProperty(this, "disableYearDropdownButton", false);

    public final boolean isDisableYearDropdownButton() {
        return disableYearDropdownButton.get();
    }

    /**
     * A property to control whether the "show year view" button will be disabled or not.
     *
     * @return true if the button used for showing the year selection view should be disabled
     */
    public final BooleanProperty disableYearDropdownButtonProperty() {
        return disableYearDropdownButton;
    }

    public final void setDisableYearDropdownButton(boolean disableYearDropdownButton) {
        this.disableYearDropdownButton.set(disableYearDropdownButton);
    }

    private final ObjectProperty<Callback<LocalDate, Boolean>> dateFilter = new SimpleObjectProperty<>(this, "dateFilter");

    public final Callback<LocalDate, Boolean> getDateFilter() {
        return dateFilter.get();
    }

    /**
     * A property to define a filter for determining which dates in the calendar can be selected.
     * This filter is applied to each date displayed in the calendar. If the filter returns true for
     * a given date, that date will be selectable (i.e., it passes the filter). If the filter returns
     * false, the date will be disabled and cannot be selected. This property is particularly useful
     * for scenarios where only specific dates should be available for selection based on custom
     * logic, such as business rules, holidays, or availability.
     * <p>
     * When SelectionMode is {@link CalendarView.SelectionModel.SelectionMode#DATE_RANGE}, disabled dates can be included within the selected range.
     * However, disabled dates cannot be used as either the starting or ending point of the range.
     *
     * @return a callback that determines the selectability of each date based on custom criteria.
     */
    public final ObjectProperty<Callback<LocalDate, Boolean>> dateFilterProperty() {
        return dateFilter;
    }

    public final void setDateFilter(Callback<LocalDate, Boolean> dateFilter) {
        this.dateFilter.set(dateFilter);
    }

    /**
     * The base date cell implementation for month views.
     *
     * @see #setCellFactory(Callback)
     */
    public static class DateCell extends Cell<LocalDate> {

        public DateCell() {
            getStyleClass().add("date-cell");
            setMaxSize(MAX_VALUE, MAX_VALUE);
            setAlignment(CENTER);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new DateCellSkin(this);
        }

        public final LocalDate getDate() {
            return getItem();
        }

        @Override
        public void updateItem(LocalDate date, boolean empty) {
            super.updateItem(date, empty);

            if (date != null) {
                setText(Integer.toString(date.getDayOfMonth()));
            }
        }
    }

    private final ObjectProperty<Callback<CalendarView, DateCell>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    /**
     * A factory for creating alternative content for the month view. The image
     * below shows the {@link CalendarView} once with the default factory and
     * once with an alternative factory that creates checkboxes.
     * </p>
     * <img src="doc-files/month-cell-factory.png" alt="Month Cell Factory">
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<CalendarView, DateCell>> cellFactoryProperty() {
        return cellFactory;
    }

    /**
     * Sets the value of {@link #cellFactoryProperty()}.
     *
     * @param factory the cell factory
     */
    public final void setCellFactory(Callback<CalendarView, DateCell> factory) {
        requireNonNull(factory);
        cellFactoryProperty().set(factory);
    }

    /**
     * Returns the value of {@link #cellFactoryProperty()}.
     *
     * @return the cell factory
     */
    public final Callback<CalendarView, DateCell> getCellFactory() {
        return cellFactoryProperty().get();
    }

    private final BooleanProperty monthSelectionViewEnabled = new SimpleBooleanProperty(this, "monthSelectionViewEnabled", true);

    public final boolean isMonthSelectionViewEnabled() {
        return monthSelectionViewEnabled.get();
    }

    /**
     * Determines whether the control offers the option to the user to pick a different
     * month directly via a designated month selection view.
     *
     * @return true if the user can pick a month directly
     * @see YearMonthView
     */
    public final BooleanProperty monthSelectionViewEnabledProperty() {
        return monthSelectionViewEnabled;
    }

    private final BooleanProperty yearSelectionViewEnabled = new SimpleBooleanProperty(this, "yearSelectionViewEnabled", true);

    public final boolean isYearSelectionViewEnabled() {
        return yearSelectionViewEnabled.get();
    }

    /**
     * Determines whether the control offers the option to the user to pick a different
     * year directly via a designated year selection view.
     *
     * @return true if the user can pick a month directly
     * @see YearView
     */
    public final BooleanProperty yearSelectionViewEnabledProperty() {
        return yearSelectionViewEnabled;
    }

    public final void setYearSelectionViewEnabled(boolean yearSelectionViewEnabled) {
        this.yearSelectionViewEnabled.set(yearSelectionViewEnabled);
    }

    public final void setMonthSelectionViewEnabled(boolean monthSelectionViewEnabled) {
        this.monthSelectionViewEnabled.set(monthSelectionViewEnabled);
    }

    private final BooleanProperty showMonth = new SimpleBooleanProperty(this, "showMonth", true);

    public boolean isShowMonth() {
        return showMonth.get();
    }

    public BooleanProperty showMonthProperty() {
        return showMonth;
    }

    public void setShowMonth(boolean showMonth) {
        this.showMonth.set(showMonth);
    }

    private final BooleanProperty showYear = new SimpleBooleanProperty(this, "showYear", true);

    public final boolean isShowYear() {
        return showYear.get();
    }

    /**
     * Show or hide the year in the header.
     *
     * @return true if the year is shown in the header
     */
    public final BooleanProperty showYearProperty() {
        return showYear;
    }

    public final void setShowYear(boolean showYear) {
        this.showYear.set(showYear);
    }

    private final BooleanProperty showTodayButton = new SimpleBooleanProperty(this, "showTodayButton");

    /**
     * Show or hide a button to quickly go to today's date.
     *
     * @return true if the button will be shown
     */
    public final BooleanProperty showTodayButtonProperty() {
        return showTodayButton;
    }

    /**
     * Sets the value of the {@link #showTodayButtonProperty()}.
     *
     * @param show if true will show the button
     */
    public final void setShowTodayButton(boolean show) {
        showTodayButtonProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showTodayButtonProperty()}.
     *
     * @return true if the button is shown
     */
    public final boolean isShowTodayButton() {
        return showTodayButtonProperty().get();
    }

    private final BooleanProperty showMonthArrows = new SimpleBooleanProperty(this, "showMonthArrows", true);

    /**
     * Shows or hides the arrows to change the month.
     *
     * @return true if the arrows will be shown
     */
    public final BooleanProperty showMonthArrowsProperty() {
        return showMonthArrows;
    }

    /**
     * Sets the value of the {@link #showMonthArrowsProperty()}.
     *
     * @param show if true will show the arrows
     */
    public final void setShowMonthArrows(boolean show) {
        showMonthArrowsProperty().set(show);
    }

    /**
     * Returns the value of the {@link #showMonthArrowsProperty()}.
     *
     * @return true if the arrows will be shown
     */
    public final boolean isShowMonthArrows() {
        return showMonthArrowsProperty().get();
    }

    private final ObjectProperty<SelectionModel> selectionModel = new SimpleObjectProperty<>(this, "selectionModel", new SelectionModel());

    public final SelectionModel getSelectionModel() {
        return selectionModel.get();
    }

    public final ObjectProperty<SelectionModel> selectionModelProperty() {
        return selectionModel;
    }

    public final void setSelectionModel(SelectionModel selectionModel) {
        this.selectionModel.set(selectionModel);
    }

    private final DoubleProperty weekNumberColumnWidth = new SimpleDoubleProperty(this, "weekNumberColumnWidth", 16);

    public final double getWeekNumberColumnWidth() {
        return weekNumberColumnWidth.get();
    }

    public final DoubleProperty weekNumberColumnWidthProperty() {
        return weekNumberColumnWidth;
    }

    public final void setWeekNumberColumnWidth(double weekNumberColumnWidth) {
        this.weekNumberColumnWidth.set(weekNumberColumnWidth);
    }

    /**
     * Different layouts that can be used to position the month, the year, and
     * the various navigation elements in the header of the view. We are intentionally
     * not using the {@link javafx.geometry.HPos} enumeration object as we have good
     * reason to believe that additional layouts will be added in the future.
     */
    public enum HeaderLayout {

        /**
         * Centers the month and year in the middle. The previous / next month
         * buttons will be on the left and right.
         */
        CENTER,

        /**
         * Positions the month and the year on the left side. The previous / next month
         * buttons will both be on the right side.
         */
        LEFT,

        /**
         * Positions the month and the year on the right side. The previous / next month
         * buttons will both be on the left side.
         */
        RIGHT
    }

    private final ObjectProperty<HeaderLayout> headerLayout = new SimpleObjectProperty<>(this, "headerLayout", HeaderLayout.CENTER);

    public final HeaderLayout getHeaderLayout() {
        return headerLayout.get();
    }

    /**
     * The header layout value determines how the information and the navigation elements
     * in the header will be laid out.
     *
     * @return the currently used layout used for the header
     */
    public final ObjectProperty<HeaderLayout> headerLayoutProperty() {
        return headerLayout;
    }

    public final void setHeaderLayout(HeaderLayout headerLayout) {
        this.headerLayout.set(headerLayout);
    }

    private final StringProperty todayText = new SimpleStringProperty(this, "todayText", "Today");

    public final String getTodayText() {
        return todayText.get();
    }

    /**
     * The text that will be shown on the button used for going to today's
     * date.
     *
     * @return the today button text
     */
    public final StringProperty todayTextProperty() {
        return todayText;
    }

    public final void setTodayText(String todayText) {
        this.todayText.set(todayText);
    }

    private final ObjectProperty<LocalDate> earliestDate = new SimpleObjectProperty<>(this, "earliestDay");

    public final LocalDate getEarliestDate() {
        return earliestDate.get();
    }

    /**
     * The earliest day that can be selected in the view.
     *
     * @return the earliest possible date available for selection
     */
    public final ObjectProperty<LocalDate> earliestDateProperty() {
        return earliestDate;
    }

    public final void setEarliestDate(LocalDate earliestDate) {
        this.earliestDate.set(earliestDate);
    }

    private final ObjectProperty<LocalDate> latestDate = new SimpleObjectProperty<>(this, "latestDate");

    public final LocalDate getLatestDate() {
        return latestDate.get();
    }

    /**
     * The latest day that can be selected in the view.
     *
     * @return the earliest possible date available for selection
     */
    public final ObjectProperty<LocalDate> latestDateProperty() {
        return latestDate;
    }

    public final void setLatestDate(LocalDate latestDate) {
        this.latestDate.set(latestDate);
    }

    /**
     * Enumerates the display modes for the year label at the top of the calendar view.
     */
    public enum YearDisplayMode {
        /**
         * Displays only the year text. This is the default mode.
         */
        TEXT_ONLY,

        /**
         * Displays the year text with a spinner for adjustment.
         */
        TEXT_AND_SPINNER,

        /**
         * Displays the year text with a dropdown button.
         */
        TEXT_AND_DROPDOWN
    }

    private ObjectProperty<YearDisplayMode> yearDisplayMode;

    /**
     * The display mode for the year label at the top of the calendar view.
     * {@link YearDisplayMode#TEXT_ONLY} is the default mode.
     *
     * @return the year display mode property.
     */
    public final ObjectProperty<YearDisplayMode> yearDisplayModeProperty() {
        if (yearDisplayMode == null) {
            yearDisplayMode = new StyleableObjectProperty<>(DEFAULT_YEAR_DISPLAY_MODE) {
                @Override
                public Object getBean() {
                    return CalendarView.this;
                }

                @Override
                public String getName() {
                    return "yearDisplayMode";
                }

                @Override
                public CssMetaData<? extends Styleable, YearDisplayMode> getCssMetaData() {
                    return StyleableProperties.YEAR_DISPLAY_MODE;
                }
            };
        }
        return yearDisplayMode;
    }

    public final YearDisplayMode getYearDisplayMode() {
        return yearDisplayMode == null ? DEFAULT_YEAR_DISPLAY_MODE : yearDisplayMode.get();
    }

    public final void setYearDisplayMode(YearDisplayMode yearDisplayMode) {
        yearDisplayModeProperty().set(yearDisplayMode);
    }

    /**
     * Enumerates the display modes for the month label at the top of the calendar view.
     */
    public enum MonthDisplayMode {
        /**
         * Displays only the month text.  This is the default mode.
         */
        TEXT_ONLY,

        /**
         * Displays the month text with a dropdown button.
         */
        TEXT_AND_DROPDOWN
    }

    private ObjectProperty<MonthDisplayMode> monthDisplayMode;

    /**
     * The display mode for the month label at the top of the calendar view.
     * {@link MonthDisplayMode#TEXT_ONLY} is the default mode.
     *
     * @return the month display mode property.
     */
    public final ObjectProperty<MonthDisplayMode> monthDisplayModeProperty() {
        if (monthDisplayMode == null) {
            monthDisplayMode = new StyleableObjectProperty<>(DEFAULT_MONTH_DISPLAY_MODE) {
                @Override
                public Object getBean() {
                    return CalendarView.this;
                }

                @Override
                public String getName() {
                    return "monthDisplayMode";
                }

                @Override
                public CssMetaData<? extends Styleable, MonthDisplayMode> getCssMetaData() {
                    return StyleableProperties.MONTH_DISPLAY_MODE;
                }
            };
        }
        return monthDisplayMode;
    }

    public final MonthDisplayMode getMonthDisplayMode() {
        return monthDisplayMode == null ? DEFAULT_MONTH_DISPLAY_MODE : monthDisplayMode.get();
    }

    public final void setMonthDisplayMode(MonthDisplayMode monthDisplayMode) {
        monthDisplayModeProperty().set(monthDisplayMode);
    }

    public static class SelectionModel {

        public enum SelectionMode {
            SINGLE_DATE,
            MULTIPLE_DATES,
            DATE_RANGE
        }

        public SelectionModel() {
            selectionMode.addListener(it -> clearSelection());
        }

        public final void clearSelection() {
            setSelectedDate(null);
            setSelectedEndDate(null);
            getSelectedDates().clear();
        }

        private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.SINGLE_DATE);

        public final SelectionMode getSelectionMode() {
            return selectionMode.get();
        }

        public final ObjectProperty<SelectionMode> selectionModeProperty() {
            return selectionMode;
        }

        public final void setSelectionMode(SelectionMode selectionMode) {
            this.selectionMode.set(selectionMode);
        }

        public void clearAndSelect(LocalDate date) {
            clearSelection();
            select(date);
        }


        public void select(LocalDate date) {
            if (date == null) {
                return;
            }

            switch (getSelectionMode()) {
                case SINGLE_DATE:
                    setSelectedDate(date);
                    break;
                case MULTIPLE_DATES:
                    getSelectedDates().add(date);
                    break;
                case DATE_RANGE:
                    if (getSelectedDate() == null) {
                        setSelectedDate(date);
                    } else {
                        setSelectedEndDate(date);
                    }
                    break;
            }
        }

        public void clearSelection(LocalDate date) {
            switch (getSelectionMode()) {
                case SINGLE_DATE:
                    clearSelection();
                    break;
                case MULTIPLE_DATES:
                    getSelectedDates().remove(date);
                    break;
                case DATE_RANGE:
                    if (Objects.equals(getSelectedDate(), date)) {
                        setSelectedDate(null);
                    } else if (Objects.equals(getSelectedEndDate(), date)) {
                        setSelectedEndDate(null);
                    }
                    break;
            }
        }

        public boolean isSelected(LocalDate date) {
            if (date == null) {
                return false;
            }

            LocalDate selectedDate = getSelectedDate();
            switch (getSelectionMode()) {
                case SINGLE_DATE:
                    return Objects.equals(selectedDate, date);
                case MULTIPLE_DATES:
                    return getSelectedDates().contains(date);
                case DATE_RANGE:
                    LocalDate selectedEndDate = getSelectedEndDate();
                    if (selectedDate == null && selectedEndDate == null) {
                        return false;
                    } else if (selectedDate != null && Objects.equals(selectedDate, date)) {
                        return true;
                    } else if (selectedDate != null && selectedEndDate != null) {
                        return !(date.isBefore(selectedDate) || date.isAfter(selectedEndDate));
                    }

                    return false;
            }

            return false;
        }

        private final ObjectProperty<LocalDate> selectedDate = new SimpleObjectProperty<>(this, "selectedDate");

        public final LocalDate getSelectedDate() {
            return selectedDate.get();
        }

        public final ObjectProperty<LocalDate> selectedDateProperty() {
            return selectedDate;
        }

        public final void setSelectedDate(LocalDate selectedDate) {
            this.selectedDate.set(selectedDate);
        }

        private final ObjectProperty<LocalDate> selectedEndDate = new SimpleObjectProperty<>(this, "endDate");

        public final LocalDate getSelectedEndDate() {
            return selectedEndDate.get();
        }

        public final ObjectProperty<LocalDate> selectedEndDateProperty() {
            return selectedEndDate;
        }

        public final void setSelectedEndDate(LocalDate selectedEndDate) {
            this.selectedEndDate.set(selectedEndDate);
        }

        private final ListProperty<LocalDate> selectedDates = new SimpleListProperty<>(this, "selectedDates", FXCollections.observableArrayList());

        public final ObservableList<LocalDate> getSelectedDates() {
            return selectedDates.get();
        }

        public final ListProperty<LocalDate> selectedDatesProperty() {
            return selectedDates;
        }

        public final void setSelectedDates(ObservableList<LocalDate> selectedDates) {
            this.selectedDates.set(selectedDates);
        }
    }

    private static class StyleableProperties {

        private static final CssMetaData<CalendarView, YearDisplayMode> YEAR_DISPLAY_MODE = new CssMetaData<>(
                "-fx-year-display-mode", new EnumConverter<>(YearDisplayMode.class), DEFAULT_YEAR_DISPLAY_MODE) {

            @Override
            public boolean isSettable(CalendarView control) {
                return control.yearDisplayMode == null || !control.yearDisplayMode.isBound();
            }

            @Override
            public StyleableProperty<YearDisplayMode> getStyleableProperty(CalendarView control) {
                return (StyleableProperty<YearDisplayMode>) control.yearDisplayModeProperty();
            }
        };

        private static final CssMetaData<CalendarView, MonthDisplayMode> MONTH_DISPLAY_MODE = new CssMetaData<>(
                "-fx-month-display-mode", new EnumConverter<>(MonthDisplayMode.class), DEFAULT_MONTH_DISPLAY_MODE) {

            @Override
            public boolean isSettable(CalendarView control) {
                return control.monthDisplayMode == null || !control.monthDisplayMode.isBound();
            }

            @Override
            public StyleableProperty<MonthDisplayMode> getStyleableProperty(CalendarView control) {
                return (StyleableProperty<MonthDisplayMode>) control.monthDisplayModeProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, YEAR_DISPLAY_MODE, MONTH_DISPLAY_MODE);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return CalendarView.StyleableProperties.STYLEABLES;
    }

}
