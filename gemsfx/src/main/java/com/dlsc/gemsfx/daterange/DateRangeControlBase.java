package com.dlsc.gemsfx.daterange;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;

public class DateRangeControlBase extends Control {

    protected DateRangeControlBase() {
        DateRangePreset todayRange = createTodayRange();
        getPresets().setAll(todayRange, createYesterdayPreset(), createThisWeekPreset(), createLastMonthPreset(), createLastYearPreset());
        setSelectedDateRange(todayRange);
    }

    // date range

    private final ObjectProperty<DateRange> selectedDateRange = new SimpleObjectProperty<>(this, "selectedDateRange");

    public DateRange getSelectedDateRange() {
        return selectedDateRange.get();
    }

    public ObjectProperty<DateRange> selectedDateRangeProperty() {
        return selectedDateRange;
    }

    public void setSelectedDateRange(DateRange selectedDateRange) {
        this.selectedDateRange.set(selectedDateRange);
    }

    // default date range

    private final ObjectProperty<DateRangePreset> defaultPreset = new SimpleObjectProperty<>(this, "defaultPreset");

    public DateRangePreset getDefaultPreset() {
        return defaultPreset.get();
    }

    public ObjectProperty<DateRangePreset> defaultPresetProperty() {
        return defaultPreset;
    }

    public void setDefaultPreset(DateRangePreset defaultPreset) {
        this.defaultPreset.set(defaultPreset);
    }

    // presets

    private final ObservableList<DateRangePreset> presets = FXCollections.observableArrayList();

    public final ObservableList<DateRangePreset> getPresets() {
        return presets;
    }

    public enum Mode {
        SIMPLE,
        ADVANCED
    }

    private final ObjectProperty<Mode> mode = new SimpleObjectProperty<>(this, "mode", Mode.ADVANCED);

    public final Mode getMode() {
        return mode.get();
    }

    public final ObjectProperty<Mode> modeProperty() {
        return mode;
    }

    public final void setMode(Mode mode) {
        this.mode.set(mode);
    }

    private DateRangePreset createTodayRange() {
        return new DateRangePreset("Today", LocalDate.now());
    }

    private DateRangePreset createYesterdayPreset() {
        return new DateRangePreset("Yesterday", LocalDate.now().minusDays(1));
    }

    private DateRangePreset createThisWeekPreset() {
        TemporalField fieldISO = WeekFields.of(Locale.getDefault()).dayOfWeek();
        LocalDate start = LocalDate.now().with(fieldISO, 1);
        LocalDate end = start.plusDays(6);
        return new DateRangePreset("This Week", start, end);
    }

    private DateRangePreset createLastMonthPreset() {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        return new DateRangePreset("Last Month", start, end);
    }

    private DateRangePreset createLastYearPreset() {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfYear()).minusYears(1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfYear());
        return new DateRangePreset("Last Year", start, end);
    }
}
