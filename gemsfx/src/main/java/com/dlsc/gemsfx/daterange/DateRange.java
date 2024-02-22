package com.dlsc.gemsfx.daterange;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

/**
 * A model class used for the {@link DateRangeView} and {@link DateRangePicker} controls.
 * A date range defines a start and an end date plus an optional title.
 */
public class DateRange {

    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    /**
     * Constructs a new date range object with the specified parameters.
     *
     * @param title     the optional title of the date range
     * @param startDate the start date of the date range
     * @param endDate   the end date of the date range
     */
    public DateRange(String title, LocalDate startDate, LocalDate endDate) {
        this.title = title;
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
    }

    /**
     * Constructs a new date range object with the specified parameters.
     *
     * @param startDate the start date of the date range
     * @param endDate   the end date of the date range
     */
    public DateRange(LocalDate startDate, LocalDate endDate) {
        this(null, startDate, endDate);
    }

    /**
     * Constructs a new date range object with the specified title and date where the
     * date will be used as the start and end date (single day range).
     *
     * @param title the optional title of the date range
     * @param date  the start and end date of the date range
     */
    public DateRange(String title, LocalDate date) {
        this(title, date, date);
    }

    /**
     * Constructs a new date range object with the specified start date and end date.
     *
     * @param date the start and end date of the date range
     */
    public DateRange(LocalDate date) {
        this(date, date);
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public String toString() {
        if (getStartDate().equals(getEndDate())) {
            return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(getStartDate());
        }

        return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(getStartDate()) +
                " - " +
                DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).format(getEndDate());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DateRange that = (DateRange) o;

        if (!startDate.equals(that.startDate)) return false;
        return endDate.equals(that.endDate);
    }

    @Override
    public int hashCode() {
        int result = startDate.hashCode();
        result = 31 * result + endDate.hashCode();
        return result;
    }
}