package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TimePickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Region;

import java.time.LocalTime;
import java.util.function.Consumer;

/**
 * A control for letting the user enter a time of day (see {@link LocalTime}). The control
 * can be configured to only enter a time within a given time range.
 */
public class TimePicker extends Control {

    /**
     * The time picker control supports 12 and 24 hour times. 12 hour times
     * require an additional field for the user to enter am / pm.
     */
    public enum ClockType {
        // TODO:implement support
        TWENTY_FOUR_HOUR_CLOCK,
        TWELVE_HOUR_CLOCK
    }

    /**
     * Constructs a new time picker.
     */
    public TimePicker() {
        getStyleClass().addAll("time-picker", "text-input");

        setFocusTraversable(false);

        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        separatorProperty().addListener(it -> {
            if (getSeparator() == null) {
                throw new IllegalArgumentException("separator can not be null");
            }
        });

        Label label = new Label(":");
        label.getStyleClass().add("separator");
        setSeparator(label);

        setTime(LocalTime.now());

        earliestTimeProperty().addListener(it -> {
            LocalTime earliestTime = getEarliestTime();
            if (earliestTime.isAfter(getLatestTime())) {
                throw new IllegalArgumentException("earliest time can not be after the latest time, earliest = " + earliestTime + ", latest time = " + getLatestTime());
            } else {
                adjust();
            }
        });

        latestTimeProperty().addListener(it -> {
            LocalTime latestTime = getLatestTime();

            if (latestTime.isBefore(getEarliestTime())) {
                throw new IllegalArgumentException("latest time can not be before the earliest time, latest = " + latestTime + ", earliest = " + getEarliestTime());
            } else {
                adjust();
            }
        });

        stepRateInMinutesProperty().addListener(it -> {
            int stepRateInMinutes = getStepRateInMinutes();
            if (stepRateInMinutes < 1) {
                throw new IllegalArgumentException("step rate can not be smaller than 0 minutes but was " + stepRateInMinutes);
            } else if (stepRateInMinutes > 60) {
                throw new IllegalArgumentException("step rate can not be larger than 60 minutes but was " + stepRateInMinutes);
            } else {
                adjust();
            }
        });

        /*
         * Added here, too, as a work-around for styling issues related to Ikonli font icon.
         */
        getStylesheets().add(TimePicker.class.getResource("time-picker.css").toExternalForm());

        setOnKeyPressed(evt -> {
            if (evt.getCode().equals(KeyCode.F4) || evt.getCode().equals(KeyCode.ENTER)) {
                getOnShowPopup().accept(this);
            }
        });

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("TIME_PICKER_POPUP")) {
                    setShowing(!isShowing());
                    getProperties().remove("TIME_PICKER_POPUP");
                } else if (change.getKey().equals("ADJUST_TIME")) {
                    adjust();
                    getProperties().remove("ADJUST_TIME");
                } else if (change.getKey().equals("CLEAR_ADJUSTED_TIME")) {
                    adjusted.set(false);
                    getProperties().remove("CLEAR_ADJUSTED_TIME");
                } else if (change.getKey().equals("NEW_TIME")) {
                    adjustmentInProgress = true;
                    try {
                        setTime((LocalTime) change.getValueAdded());
                    } finally {
                        adjustmentInProgress = false;
                    }
                }
            }
        };

        getProperties().addListener(propertiesListener);

        timeProperty().addListener(it -> {
            if (!adjustmentInProgress) {
                adjusted.set(false);
            }
        });

        setOnShowPopup(picker -> show());
    }

    private final ReadOnlyBooleanWrapper showing = new ReadOnlyBooleanWrapper(this, "showing", false);

    public final boolean isShowing() {
        return showing.get();
    }

    /**
     * A flag used to signal whether the popup should be showing itself or not.
     *
     * @return true if the popup should be showing
     */
    public final ReadOnlyBooleanProperty showingProperty() {
        return showing.getReadOnlyProperty();
    }

    private void setShowing(boolean showing) {
        this.showing.set(showing);
    }

    /**
     * Forces the popup for hour and minute selection to show itself.
     */
    public final void show() {
        setShowing(true);
    }

    /**
     * Forces the popup for hour and minute selection to hide itself.
     */
    public final void hide() {
        setShowing(false);
    }

    private final ReadOnlyBooleanWrapper adjusted = new ReadOnlyBooleanWrapper(this, "adjusted");

    public final boolean isAdjusted() {
        return adjusted.get();
    }

    public final ReadOnlyBooleanProperty adjustedProperty() {
        return adjusted.getReadOnlyProperty();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TimePickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return TimePicker.class.getResource("time-picker.css").toExternalForm();
    }

    private final ObjectProperty<LocalTime> earliestTime = new SimpleObjectProperty<>(this, "earliestTime", LocalTime.MIN);

    public final LocalTime getEarliestTime() {
        return earliestTime.get();
    }

    /**
     * The earliest time that the user can enter via the time picker.
     *
     * @return the earliest time allowed
     */
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

    /**
     * The latest time that the user can enter via the time picker.
     *
     * @return the latest time allowed
     */
    public final ObjectProperty<LocalTime> latestTimeProperty() {
        return latestTime;
    }

    public final void setLatestTime(LocalTime latestTime) {
        this.latestTime.set(latestTime);
    }

    private final ObjectProperty<Node> separator = new SimpleObjectProperty<>(this, "separator");

    public final Node getSeparator() {
        return separator.get();
    }

    /**
     * The node that will be placed between the hours and the minutes field. The
     * default separator is a label with text ":".
     *
     * @return a node used as a separator
     */
    public final ObjectProperty<Node> separatorProperty() {
        return separator;
    }

    public final void setSeparator(Node separator) {
        this.separator.set(separator);
    }

    private final BooleanProperty showPopupTriggerButton = new SimpleBooleanProperty(this, "showPopupTriggerButton", true);

    public final boolean isShowPopupTriggerButton() {
        return showPopupTriggerButton.get();
    }

    /**
     * Determines if the control will show a button for showing or hiding the
     * popup.
     *
     * @return true if the control will show a button for showing the popup
     */
    public final BooleanProperty showPopupTriggerButtonProperty() {
        return showPopupTriggerButton;
    }

    public final void setShowPopupTriggerButton(boolean showPopupTriggerButton) {
        this.showPopupTriggerButton.set(showPopupTriggerButton);
    }

    private boolean adjustmentInProgress;

    /**
     * Adjusts the time picker's time based on the earliest and latest
     * time allowed and also based on the step rate currently in effect.
     */
    public final void adjust() {
        adjustmentInProgress = true;
        try {
            boolean adjusted = adjustViaTimeBounds() || adjustViaStepRate();
            if (adjusted) {
                /*
                 * Only update property if it changes to "true". The control will
                 * only get marked as not adjusted if a new editing cycle begins.
                 */
                this.adjusted.set(true);
            }
        } finally {
            adjustmentInProgress = false;
        }
    }

    private boolean adjustViaTimeBounds() {
        LocalTime time = getTime();
        if (time != null) {
            LocalTime earliestTime = getEarliestTime();
            LocalTime latestTime = getLatestTime();

            int hour = time.getHour();
            int minute = time.getMinute();

            LocalTime newTime = LocalTime.of(hour, minute);

            if (time.isBefore(earliestTime)) {

                // adjustment of hours needed?
                if (hour < earliestTime.getHour()) {
                    newTime = newTime.withHour(earliestTime.getHour());
                }

                // still too early? adjust minutes
                if (newTime.isBefore(earliestTime)) {
                    newTime = newTime.withMinute(earliestTime.getMinute());
                }

            } else if (time.isAfter(latestTime)) {

                // adjustment of hours needed?
                if (hour > latestTime.getHour()) {
                    newTime = newTime.withHour(latestTime.getHour());
                }

                // still too early? adjust minutes
                if (newTime.isAfter(latestTime)) {
                    newTime = newTime.withMinute(latestTime.getMinute());
                }

            }

            boolean adjusted = newTime.getHour() != time.getHour() || newTime.getMinute() != time.getMinute();

            if (adjusted) {
                setTime(newTime);
            }

            return adjusted;
        }

        return false;
    }

    private boolean adjustViaStepRate() {
        LocalTime time = getTime();
        if (time != null) {
            int hour = time.getHour();

            int unadjustedMinutes = time.getMinute();
            int lowerAdjustment = unadjustedMinutes - time.getMinute() % getStepRateInMinutes();
            int higherAdjustment = lowerAdjustment + getStepRateInMinutes();

            LocalTime adjustedTime = LocalTime.of(hour, lowerAdjustment);
            if (Math.abs(lowerAdjustment - unadjustedMinutes) > Math.abs(higherAdjustment - unadjustedMinutes)) {
                if (higherAdjustment > 59) {
                    higherAdjustment = lowerAdjustment;
                }

                adjustedTime = LocalTime.of(hour, higherAdjustment);
            }

            /*
             * We have to check "manually" for equality of the original time and the adjusted time as equality for us
             * means "equal hour and equal minutes", which is different than what the equals() method of LocalTime is
             * checking (also checks seconds and nanos). Without this check we enter into an infinite recursion.
             */
            if (adjustedTime.getHour() != time.getHour() || adjustedTime.getMinute() != time.getMinute()) {
                setTime(adjustedTime);
            }

            return !(time.getHour() == adjustedTime.getHour() && time.getMinute() == adjustedTime.getMinute());
        }

        return false;
    }

    private final ObjectProperty<LocalTime> time = new SimpleObjectProperty<>(this, "time");

    public final LocalTime getTime() {
        return time.get();
    }

    /**
     * Stores the current time displayed by the control.
     *
     * @return the chosen time
     */
    public final ObjectProperty<LocalTime> timeProperty() {
        return time;
    }

    public final void setTime(LocalTime time) {
        this.time.set(time);
    }

    private final IntegerProperty stepRateInMinutes = new SimpleIntegerProperty(this, "stepRateInMinutes", 1);

    public final int getStepRateInMinutes() {
        return stepRateInMinutes.get();
    }

    /**
     * Stores the "step rate" used by the control when the user increases or
     * decreases the minutes field. The step rate can be used to (for example) make
     * the minutes increase or decrease by 15 minutes every time the user presses
     * the arrow up or down keys.
     *
     * @return the step rate in minutes
     */
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

    /**
     * The clock type determines whether the control will display 24 or 12 hours. If
     * the control shows 12 hours then an additional field for choosing between the "am"
     * or "pm" meridian will be added.
     *
     * @return the type of the clock
     */
    public final ObjectProperty<ClockType> clockTypeProperty() {
        return clockType;
    }

    public final void setClockType(ClockType clockType) {
        this.clockType.set(clockType);
    }

    private final BooleanProperty linkingFields = new SimpleBooleanProperty(this, "linkingFields", true);

    public final boolean isLinkingFields() {
        return linkingFields.get();
    }

    /**
     * A property used to control whether the fields should automatically increase or decrease
     * the previous field when they reach their upper or lower limit.
     *
     * @return true if rollover is desired
     */
    public final BooleanProperty linkingFieldsProperty() {
        return linkingFields;
    }

    public final void setLinkingFields(boolean linkingFields) {
        this.linkingFields.set(linkingFields);
    }

    private final BooleanProperty rollover = new SimpleBooleanProperty(this, "rollOver", true);

    public final boolean isRollover() {
        return rollover.get();
    }

    /**
     * A flag used to signal whether the time fields should start at the beginning of its value range
     * when it reaches the end of it. E.g. incrementing hour 23 would result in hour 0 when the user tries
     * to increase it by one.
     *
     * @return true if the fields should rollover
     */
    public final BooleanProperty rolloverProperty() {
        return rollover;
    }

    public final void setRollover(boolean rollover) {
        this.rollover.set(rollover);
    }

    private final ObjectProperty<Consumer<TimePicker>> onShowPopup = new SimpleObjectProperty<>(this, "onShowPopup");

    public final Consumer<TimePicker> getOnShowPopup() {
        return onShowPopup.get();
    }

    /**
     * This consumer will be invoked to bring up a control for entering the
     * time without using the keyboard. The default implementation shows a popup.
     *
     * @return the "on show popup" consumer
     */
    public final ObjectProperty<Consumer<TimePicker>> onShowPopupProperty() {
        return onShowPopup;
    }

    public final void setOnShowPopup(Consumer<TimePicker> onShowPopup) {
        this.onShowPopup.set(onShowPopup);
    }
}
