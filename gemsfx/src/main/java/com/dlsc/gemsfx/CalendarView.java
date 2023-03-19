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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.WeekFields;
import java.util.Locale;

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
 * The image below shows the visual apperance of this control:
 *
 * <img src="doc-files/date-picker.png" alt="Date Picker">
 */
public class CalendarView extends Control {

    /**
     * Constructs a new view.
     */
    public CalendarView() {
        getStyleClass().add("calendar-view");
        setCellFactory(view -> new DateCell());
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return CalendarView.class.getResource("calendar-view.css").toExternalForm();
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

    private final BooleanProperty showWeeks = new SimpleBooleanProperty(this, "showWeeks", true);

    /**
     * Controls whether the view will show week numbers.
     *
     * @return true if week numbers are shown
     */
    public final BooleanProperty showWeekNumbersProperty() {
        return showWeeks;
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

    private final ObjectProperty<WeekFields> weekFields = new SimpleObjectProperty<>(this, "weekFields", WeekFields.of(Locale.getDefault()));

    /**
     * Week fields are used to determine the first day of a week (e.g. "Monday"
     * in Germany or "Sunday" in the US). It is also used to calculate the week
     * number as the week fields determine how many days are needed in the first
     * week of a year. This property is initialized with {@link WeekFields#ISO}.
     *
     * @return the week fields
     */
    public final ObjectProperty<WeekFields> weekFieldsProperty() {
        return weekFields;
    }

    /**
     * Sets the value of {@link #weekFieldsProperty()}.
     *
     * @param weekFields the new week fields
     */
    public final void setWeekFields(WeekFields weekFields) {
        requireNonNull(weekFields);
        weekFieldsProperty().set(weekFields);
    }

    /**
     * Returns the value of {@link #weekFieldsProperty()}.
     *
     * @return the week fields
     */
    public final WeekFields getWeekFields() {
        return weekFieldsProperty().get();
    }

    /**
     * A convenience method to look up the first day of the week ("Monday" in
     * Germany, "Sunday" in the US). This method delegates to
     * {@link WeekFields#getFirstDayOfWeek()}.
     *
     * @return the first day of the week
     * @see #weekFieldsProperty()
     */
    public final DayOfWeek getFirstDayOfWeek() {
        return getWeekFields().getFirstDayOfWeek();
    }

    private final ObservableSet<LocalDate> selectedDates = FXCollections.observableSet();

    /**
     * The selected dates.
     *
     * @return the selected dates
     */
    public final ObservableSet<LocalDate> getSelectedDates() {
        return selectedDates;
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

    private final ObjectProperty<SelectionMode> selectionMode = new SimpleObjectProperty<>(this, "selectionMode", SelectionMode.MULTIPLE);

    /**
     * Stores the selection mode. All date controls support single and multiple
     * selections.
     *
     * @return the selection mode
     * @see SelectionMode
     */
    public final ObjectProperty<SelectionMode> selectionModeProperty() {
        return selectionMode;
    }

    /**
     * Sets the value of {@link #selectionModeProperty()}.
     *
     * @param mode the selection mode (single, multiple)
     */
    public final void setSelectionMode(SelectionMode mode) {
        requireNonNull(mode);
        selectionModeProperty().set(mode);
    }

    /**
     * Returns the value of {@link #selectionModeProperty()}.
     *
     * @return the selection mode (single, multiple)
     */
    public final SelectionMode getSelectionMode() {
        return selectionModeProperty().get();
    }

    /**
     * The base date cell implementation for month views.
     *
     * @see #setCellFactory(Callback)
     */
    public static class DateCell extends Label {

        private LocalDate date;

        public DateCell() {
            getStyleClass().add("date-cell");
            setMaxSize(MAX_VALUE, MAX_VALUE);
            setAlignment(CENTER);
        }

        public final void setDate(LocalDate date) {
            this.date = date;
            update(date);
        }

        public final LocalDate getDate() {
            return date;
        }

        protected void update(LocalDate date) {
            setText(Integer.toString(date.getDayOfMonth()));
        }
    }

    private final ObjectProperty<Callback<CalendarView, DateCell>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    /**
     * A factory for creating alternative content for the month view. The image
     * below shows the {@link CalendarView} once with the default factory and
     * once with an alternative factory that creates checkboxes.
     *
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

    private final BooleanProperty showYearSpinner = new SimpleBooleanProperty(this, "showYearSpinner", true);

    /**
     * Show or hide the year / month spinner.
     *
     * @return true if the year will be shown
     */
    public final BooleanProperty showYearSpinnerProperty() {
        return showYearSpinner;
    }

    /**
     * Sets the value of {@link #showYearSpinnerProperty()}.
     *
     * @param show if true the year / month spinner at the top will be shown
     */
    public final void setShowYearSpinner(boolean show) {
        showYearSpinnerProperty().set(show);
    }

    /**
     * Returns the value of {@link #showYearSpinnerProperty()}.
     *
     * @return true if the year / month spinner will be shown
     */
    public final boolean isShowYearSpinner() {
        return showYearSpinnerProperty().get();
    }

    private final BooleanProperty showTodayButton = new SimpleBooleanProperty(this, "showTodayButton", true);

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

    private final BooleanProperty showHeader = new SimpleBooleanProperty(this, "showHeader", true);

    /**
     * Shows or hides the header where the user can change the current month.
     *
     * @return true if the header will be shown
     */
    public final BooleanProperty showHeaderProperty() {
        return showHeader;
    }

    public final void setShowHeader(boolean show) {
        showHeaderProperty().set(show);
    }

    public final boolean isShowHeader() {
        return showHeaderProperty().get();
    }
}
