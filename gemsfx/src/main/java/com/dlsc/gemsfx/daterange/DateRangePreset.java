package com.dlsc.gemsfx.daterange;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * A date range preset is used to populate the "presets" area with the {@link DateRangeView}
 * control. Users can click on a preset, e.g. "last week" to automatically select the date
 * range that represents last week.
 * <p>
 * Presets have to be implemented in such a way that they return the correct date range at any
 * time, meaning the dates can not be fixed at the time of the creation of the date range, but they
 * have to be computed on-the-fly. The UI might have been up and running for several days, hence
 * the meaning of "last week" might have changed.
 */
public class DateRangePreset {

    private final String title;
    private final Supplier<DateRange> dateRangeSupplier;

    /**
     * Constructs a new date range preset with the given title and date range
     * supplier. The supplier will be used to retrieve the date range on-the-fly
     * when the user clicks on the preset.
     *
     * @param title the title / name of the preset, e.g. "last week"
     * @param dateRangeSupplier the supplier returning the date range object for the preset
     */
    public DateRangePreset(String title, Supplier<DateRange> dateRangeSupplier) {
        this.title = Objects.requireNonNull(title);
        this.dateRangeSupplier = Objects.requireNonNull(dateRangeSupplier);
    }

    public final String getTitle() {
        return title;
    }

    public final Supplier<DateRange> getDateRangeSupplier() {
        return dateRangeSupplier;
    }
}