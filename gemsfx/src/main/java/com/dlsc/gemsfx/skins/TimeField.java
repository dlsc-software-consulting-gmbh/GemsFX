package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;
import javafx.scene.control.Label;

/**
 * The common superclass for all controls that represent a field
 * inside the {@link TimePicker} control. Fields can be used to show
 * hours, minutes, or meridians.
 */
public abstract class TimeField extends Label {

    private final TimePicker timePicker;

    protected TimeField(TimePicker timePicker) {
        this.timePicker = timePicker;

        getStyleClass().add("time-field");

        focusedProperty().addListener(it -> {
            if (!isFocused()) {

                /*
                 * Whenever any of the fields inside the time picker looses its
                 * focus we want to run the code that adjusts the time based on
                 * the earliest and latest times and also on the step rate.
                 */
                timePicker.getProperties().put("ADJUST_TIME", "ADJUST_TIME");
            }
        });
    }

    protected TimePicker getTimePicker() {
        return timePicker;
    }

    /**
     * Fields that can be incremented have to implement this method.
     */
    abstract void increment();

    /**
     * Fields that can be decremented have to implement this method.
     */
    abstract void decrement();
}
