package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.skins.DateRangeViewSkin;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Objects;

public class DateRangeView extends Control {

    private CalendarView startCalendarView;

    private CalendarView endCalendarView;

    private final SelectionModel selectionModel;

    public DateRangeView() {
        getStyleClass().add("date-range-view");

        selectionModel = new SelectionModel();
        selectionModel.setSelectionMode(SelectionModel.SelectionMode.DATE_RANGE);

        DateRangePreset todayRange = createTodayRange();
        getPresets().addAll(todayRange, createYesterdayPreset(), createThisWeekPreset(), createThisMonthPreset(), createLastMonthPreset());
        setValue(todayRange);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangeViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DateRangeView.class.getResource("date-range-view.css")).toExternalForm();
    }

    /**
     * Returns the selection model used and shared by the two calendar picker instances.
     *
     * @return the shared calendar selection model
     * @see #getStartCalendarView()
     * @see #getEndCalendarView()
     */
    public final SelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Returns the {@link CalendarView} that is being used for the start date
     * selection.
     *
     * @return the calendar view for the start date selection
     */
    public CalendarView getStartCalendarView() {
        if (startCalendarView == null) {
            startCalendarView = new CalendarView();
        }
        return startCalendarView;
    }

    /**
     * Returns the {@link CalendarView} that is being used for the end date
     * selection.
     *
     * @return the calendar view for the end date selection
     */
    public CalendarView getEndCalendarView() {
        if (endCalendarView == null) {
            endCalendarView = new CalendarView();
        }
        return endCalendarView;
    }

    private final StringProperty presetTitle = new SimpleStringProperty(this, "presetsTitle", "QUICK SELECT");

    public String getPresetTitle() {
        return presetTitle.get();
    }

    public StringProperty presetTitleProperty() {
        return presetTitle;
    }

    public void setPresetTitle(String presetTitle) {
        this.presetTitle.set(presetTitle);
    }

    private final ObjectProperty<Side> presetsLocation = new SimpleObjectProperty<>(this, "presetsLocation", Side.LEFT) {
        @Override
        public void set(Side side) {
            if (!Objects.equals(side, Side.LEFT) && !Objects.equals(side, Side.RIGHT)) {
                throw new IllegalArgumentException("only sides LEFT and RIGHT are supported");
            }
            super.set(side);
        }
    };

    public final Side getPresetsLocation() {
        return presetsLocation.get();
    }

    public final ObjectProperty<Side> presetsLocationProperty() {
        return presetsLocation;
    }

    public final void setPresetsLocation(Side presetsLocation) {
        this.presetsLocation.set(presetsLocation);
    }

    private final BooleanProperty showPresets = new SimpleBooleanProperty(this, "showQuickSelect", true);

    public final boolean isShowPresets() {
        return showPresets.get();
    }

    public final BooleanProperty showPresetsProperty() {
        return showPresets;
    }

    public final void setShowPresets(boolean showPresets) {
        this.showPresets.set(showPresets);
    }

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose", () -> System.out.println("closing"));

    public final Runnable getOnClose() {
        return onClose.get();
    }

    public final ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public final void setOnClose(Runnable onClose) {
        this.onClose.set(onClose);
    }

    // date range

    private final ObjectProperty<DateRange> value = new SimpleObjectProperty<>(this, "value");

    public final DateRange getValue() {
        return value.get();
    }

    public final ObjectProperty<DateRange> valueProperty() {
        return value;
    }

    public final void setValue(DateRange value) {
        this.value.set(value);
    }

    // presets

    private final ObservableList<DateRangePreset> presets = FXCollections.observableArrayList();

    public final ObservableList<DateRangePreset> getPresets() {
        return presets;
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

    private DateRangePreset createThisMonthPreset() {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        return new DateRangePreset("This Month", start, end);
    }

    private DateRangePreset createLastMonthPreset() {
        LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
        LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
        return new DateRangePreset("Last Month", start, end);
    }
}
