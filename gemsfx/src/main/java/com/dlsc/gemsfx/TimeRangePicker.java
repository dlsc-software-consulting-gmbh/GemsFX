package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.SimpleStringConverter;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import com.dlsc.gemsfx.util.ResourceBundleManager;

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

    /**
     * Constructs a new time range picker with a predefined set of ranges.
     */
    public TimeRangePicker() {
        // add some default time ranges
        this(new TimeRange(LocalTime.of(10, 0), LocalTime.of(12, 0)),
                new TimeRange(LocalTime.of(12, 0), LocalTime.of(14, 0)),
                new TimeRange(LocalTime.of(14, 0), LocalTime.of(16, 0)),
                new TimeRange(LocalTime.of(16, 0), LocalTime.of(18, 0)));
    }

    /**
     * Constructs a new time range picker with the given ranges.
     *
     * @param ranges the time ranges to display
     */
    public TimeRangePicker(TimeRange... ranges) {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        // Set time ranges
        getItems().setAll(ranges);

        // Add quick selection buttons to the top of the popup
        setTop(createExtraButtonsBox());

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

    private Node createExtraButtonsBox() {
        Button clearButton = createExtraButton(ResourceBundleManager.getString(ResourceBundleManager.BundleType.TIME_RANGE_PICKER, "action.clear", "Clear"), getSelectionModel()::clearSelection);
        clearButton.getStyleClass().add("clear-button");

        Button selectAllButton = createExtraButton(ResourceBundleManager.getString(ResourceBundleManager.BundleType.TIME_RANGE_PICKER, "action.select-all", "Select All"), getSelectionModel()::selectAll);
        selectAllButton.getStyleClass().add("select-all-button");
        selectAllButton.managedProperty().bind(selectAllButton.visibleProperty());
        selectAllButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));

        VBox extraButtonsBox = new VBox(clearButton, selectAllButton);
        extraButtonsBox.getStyleClass().addAll("extra-buttons-box");
        extraButtonsBox.managedProperty().bind(extraButtonsBox.visibleProperty());
        extraButtonsBox.visibleProperty().bind(itemsProperty().emptyProperty().not());

        return extraButtonsBox;
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
    public static final class TimeRange {

        private final LocalTime startTime;
        private final LocalTime endTime;

        /**
         * Constructs a new time range.
         *
         * @param startTime the start time
         * @param endTime the end time
         */
        public TimeRange(LocalTime startTime, LocalTime endTime) {
            // Ensure startTime and endTime are not null
            Objects.requireNonNull(startTime, "startTime cannot be null");
            Objects.requireNonNull(endTime, "endTime cannot be null");

            // If startTime is after endTime, swap them
            if (startTime.isAfter(endTime)) {
                LocalTime temp = startTime;
                startTime = endTime;
                endTime = temp;
            }

            this.startTime = startTime;
            this.endTime = endTime;
        }

        /**
         * Returns the start time of the range.
         *
         * @return the start time
         */
        public LocalTime startTime() {
            return startTime;
        }

        /**
         * Returns the end time of the range.
         *
         * @return the end time
         */
        public LocalTime endTime() {
            return endTime;
        }

        /**
         * {@inheritDoc}
         *
         * @return the result
         *
         * @param o the o value
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof TimeRange)) {
                return false;
            }

            TimeRange timeRange = (TimeRange) o;
            return Objects.equals(startTime, timeRange.startTime) && Objects.equals(endTime, timeRange.endTime);
        }

        /**
         * {@inheritDoc}
         *
         * @return the result
         */
        @Override
        public int hashCode() {
            return Objects.hash(startTime, endTime);
        }

        /**
         * {@inheritDoc}
         *
         * @return the string representation of the value
         */
        @Override
        public String toString() {
            return startTime + " ~ " + endTime;
        }
    }
}
