package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.SimpleStringConverter;
import javafx.scene.control.Button;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A custom control that allows users to select time ranges.
 * It provides support for two selection modes: single range and multiple ranges.
 * <ul>
 *   <li>SINGLE mode allows selection of only one time range at a time.</li>
 *   <li>MULTIPLE mode allows selection of multiple time ranges.</li>
 * </ul>
 *
 * @see SelectionBox
 */
public class TimeRangePicker extends SelectionBox<TimeRangePicker.TimeRange> {

    private static final String DEFAULT_STYLE_CLASS = "time-range-picker";

    public TimeRangePicker() {
        // add some default time ranges
        this(new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)),
                new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0)),
                new TimeRange(LocalTime.of(14, 0), LocalTime.of(16, 0)),
                new TimeRange(LocalTime.of(16, 0), LocalTime.of(18, 0)));
    }

    public TimeRangePicker(TimeRange... ranges) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        // Set time ranges
        getItems().setAll(ranges);

        // set extra buttons
        setExtraButtonsProvider(model -> switch (model.getSelectionMode()) {
            case SINGLE -> {
                Button clearButton = createExtraButton("Clear", getSelectionModel()::clearSelection);
                yield List.of(clearButton);
            }
            case MULTIPLE -> {
                Button clearButton = createExtraButton("Clear", model::clearSelection);
                Button selectAllButton = createExtraButton("Select All", model::selectAll);
                yield List.of(clearButton, selectAllButton);
            }
        });

        // set result converter
        setSelectedItemsConverter(new SimpleStringConverter<>(selectedRanges -> {
            int selectedCount = selectedRanges.size();

            if (selectedCount == 0) {
                return "";
            } else if (selectedCount == 1) {
                return convertRangeToText(selectedRanges.get(0));
            } else {
                // Merge consecutive ranges
                List<TimeRange> mergedRanges = mergeConsecutiveItem(selectedRanges);

                // Build the display text
                List<String> rangeStrings = new ArrayList<>();
                for (TimeRange range : mergedRanges) {
                    rangeStrings.add(convertRangeToText(range));
                }
                return String.join(", ", rangeStrings);
            }
        }));
    }

    private List<TimeRange> mergeConsecutiveItem(List<TimeRange> ranges) {
        List<TimeRange> mergedRanges = new ArrayList<>();

        if (ranges.isEmpty()) {
            return mergedRanges;
        }

        // Sort ranges by start time
        ranges.sort(Comparator.comparing(TimeRange::startTime));

        TimeRange currentRange = ranges.get(0);

        for (int i = 1; i < ranges.size(); i++) {
            TimeRange nextRange = ranges.get(i);

            if (currentRange.endTime().equals(nextRange.startTime())) {
                // Ranges are consecutive, merge them
                currentRange = new TimeRange(currentRange.startTime(), nextRange.endTime());
            } else {
                // Ranges are not consecutive, add the current range and move to next
                mergedRanges.add(currentRange);
                currentRange = nextRange;
            }
        }

        // Add the last range
        mergedRanges.add(currentRange);

        return mergedRanges;
    }

    private String convertRangeToText(TimeRange range) {
        if (range == null) {
            return "";
        }
        StringConverter<TimeRange> itemConverter = getItemConverter();
        if (itemConverter != null) {
            return itemConverter.toString(range);
        }
        return range.toString();
    }

    /**
     * Represents a time range with a start time and an end time.
     * Ensures that the start time is not after the end time.
     */
    public record TimeRange(LocalTime startTime, LocalTime endTime) {

        public TimeRange {
            // Ensure startTime and endTime are not null
            Objects.requireNonNull(startTime, "startTime cannot be null");
            Objects.requireNonNull(endTime, "endTime cannot be null");

            // If startTime is after endTime, swap them
            if (startTime.isAfter(endTime)) {
                LocalTime temp = startTime;
                startTime = endTime;
                endTime = temp;
            }
        }

        @Override
        public String toString() {
            return startTime + " ~ " + endTime;
        }
    }
}
