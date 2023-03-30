package com.dlsc.gemsfx.daterange;

import java.time.LocalDate;
import java.util.Objects;

public class DateRangePreset extends DateRange {

    private final String title;

    public DateRangePreset(String title, LocalDate startDate, LocalDate endDate) {
        super(startDate, endDate);
        this.title = Objects.requireNonNull(title);
    }

    public DateRangePreset(String title, LocalDate date) {
        this(title, date, date);
    }

    public String getTitle() {
        return title;
    }
}