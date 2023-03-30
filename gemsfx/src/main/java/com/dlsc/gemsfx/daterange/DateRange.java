package com.dlsc.gemsfx.daterange;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

public class DateRange {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public DateRange(LocalDate startDate, LocalDate endDate) {
        this.startDate = Objects.requireNonNull(startDate);
        this.endDate = Objects.requireNonNull(endDate);
    }

    public DateRange(LocalDate date) {
        this(date, date);
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