package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import com.dlsc.gemsfx.TimePicker;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;

/**
 * This popup is used by the {@link TimePicker} control to let the user
 * select a time via mouse or touch.
 */
public class DurationPickerPopup extends PopupControl {

    public DurationPickerPopup() {
        getStyleClass().add("duration-picker-popup");

        setAutoFix(true);
        setAutoHide(true);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DurationPickerPopupSkin(this);
    }

    private final ListProperty<ChronoUnit> fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList(ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS));

    public final ObservableList<ChronoUnit> getFields() {
        return fields.get();
    }

    public final ListProperty<ChronoUnit> fieldsProperty() {
        return fields;
    }

    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration");

    public final Duration getDuration() {
        return duration.get();
    }

    /**
     * The time currently shown by the popup.
     *
     * @see DurationPicker#durationProperty()
     * @return the duration
     */
    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public final void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    private final ObjectProperty<Duration> minimumDuration = new SimpleObjectProperty<>(this, "earliestTime", Duration.ZERO);

    public final Duration getMinimumDuration() {
        return minimumDuration.get();
    }

    public final ObjectProperty<Duration> minimumDurationProperty() {
        return minimumDuration;
    }

    public final void setMinimumDuration(Duration minimumDuration) {
        this.minimumDuration.set(minimumDuration);
    }

    private final ObjectProperty<Duration> maximumDuration = new SimpleObjectProperty<>(this, "maximumDuration");

    public final Duration getMaximumDuration() {
        return maximumDuration.get();
    }

    public final ObjectProperty<Duration> maximumDurationProperty() {
        return maximumDuration;
    }

    public final void setMaximumDuration(Duration maximumDuration) {
        this.maximumDuration.set(maximumDuration);
    }
}
