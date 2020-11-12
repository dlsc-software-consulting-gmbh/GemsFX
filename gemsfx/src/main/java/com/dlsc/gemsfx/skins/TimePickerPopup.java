package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;
import com.dlsc.gemsfx.TimePicker.ClockType;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

import java.time.LocalTime;

/**
 * This popup is used by the {@link TimePicker} control to let the user
 * select a time via mouse or touch.
 */
public class TimePickerPopup extends PopupControl {

    public TimePickerPopup() {
        getStyleClass().add("time-picker-popup");

        setAutoFix(true);
        setAutoHide(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimePickerPopupSkin(this);
    }

    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>(this, "time");

    public final LocalTime getTime() {
        return time.get();
    }

    /**
     * The time currently shown by the popup.
     *
     * @see TimePicker#timeProperty()
     * @return the time
     */
    public final ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public final void setTime(LocalTime time) {
        this.time.set(time);
    }

    private final IntegerProperty stepRateInMinutes = new SimpleIntegerProperty(this, "stepRateInMinutes", 15);

    public final int getStepRateInMinutes() {
        return stepRateInMinutes.get();
    }

    public final IntegerProperty stepRateInMinutesProperty() {
        return stepRateInMinutes;
    }

    public final void setStepRateInMinutes(int stepRateInMinutes) {
        this.stepRateInMinutes.set(stepRateInMinutes);
    }

    private final ObjectProperty<ClockType> clockType = new SimpleObjectProperty<>(this, "clockType", ClockType.TWENTY_FOUR_HOUR_CLOCK);

    public final ClockType getClockType() {
        return clockType.get();
    }

    public final ObjectProperty<ClockType> clockTypeProperty() {
        return clockType;
    }

    public final void setClockType(ClockType clockType) {
        this.clockType.set(clockType);
    }

    private final ObjectProperty<LocalTime> earliestTime = new SimpleObjectProperty<>(this, "earliestTime", LocalTime.MIN);

    public final LocalTime getEarliestTime() {
        return earliestTime.get();
    }

    public final ObjectProperty<LocalTime> earliestTimeProperty() {
        return earliestTime;
    }

    public final void setEarliestTime(LocalTime earliestTime) {
        this.earliestTime.set(earliestTime);
    }

    private final ObjectProperty<LocalTime> latestTime = new SimpleObjectProperty<>(this, "latestTime", LocalTime.MAX);

    public final LocalTime getLatestTime() {
        return latestTime.get();
    }

    public final ObjectProperty<LocalTime> latestTimeProperty() {
        return latestTime;
    }

    public final void setLatestTime(LocalTime latestTime) {
        this.latestTime.set(latestTime);
    }
}
