package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.SimpleStringConverter;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.VBox;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A custom control that allows users to select days of the week.
 * It provides support for two {@link SelectionMode}: single and multiple .
 * <ul>
 *   <li>{@link SelectionMode#SINGLE} mode allows selection of only one day at a time.</li>
 *   <li>{@link SelectionMode#MULTIPLE} mode allows selection of multiple days.</li>
 * </ul>
 *
 * @see SelectionBox
 */
public class DayOfWeekPicker extends SelectionBox<DayOfWeek> {

    private static final String DEFAULT_STYLE_CLASS = "day-of-week-picker";

    public DayOfWeekPicker() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        // Set Items
        getItems().setAll(getLocalizedDayOrder());

        // Add quick selection buttons to the top of the popup
        setTop(createExtraButtonsBox());

        // set item converter
        setItemConverter(new SimpleStringConverter<>(dayOfWeek -> dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())));

        // Set selected items converter
        setSelectedItemsConverter(new SimpleStringConverter<>(selectedDays -> {
            if (selectedDays == null || selectedDays.isEmpty()) {
                return "";
            } else if (selectedDays.size() == 1) {
                return selectedDays.get(0).getDisplayName(TextStyle.FULL, Locale.getDefault());
            } else if (isSelectedAll()) {
                return "All Days";
            } else if (isOnlyWeekdaysSelected()) {
                return "Weekdays";
            } else if (isOnlyWeekendsSelected()) {
                return "Weekends";
            } else {
                // Group selected days into consecutive ranges
                List<List<DayOfWeek>> ranges = mergeConsecutiveItem(selectedDays);

                // Build the display text
                List<String> rangeStrings = new ArrayList<>();
                for (List<DayOfWeek> range : ranges) {
                    if (range.size() > 1) {
                        String startDay = range.get(0).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                        String endDay = range.get(range.size() - 1).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                        rangeStrings.add(startDay + " ~ " + endDay);
                    } else {
                        String dayName = range.get(0).getDisplayName(TextStyle.SHORT, Locale.getDefault());
                        rangeStrings.add(dayName);
                    }
                }
                return String.join(", ", rangeStrings);
            }
        }));
    }

    private VBox createExtraButtonsBox() {
        Button clearButton = createExtraButton("Clear", () -> getSelectionModel().clearSelection());
        clearButton.getStyleClass().add("clear-button");

        Button todayButton = createExtraButton("Today", () -> getSelectionModel().clearAndSelect(getItems().indexOf(LocalDate.now().getDayOfWeek())));
        todayButton.getStyleClass().add("today-button");
        todayButton.managedProperty().bind(todayButton.visibleProperty());
        todayButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.SINGLE));

        Button allButton = createExtraButton("All Days", () -> getSelectionModel().selectAll());
        allButton.getStyleClass().add("select-all-button");
        allButton.managedProperty().bind(allButton.visibleProperty());
        allButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));

        Button weekdaysButton = createExtraButton("Weekdays", () -> {
            getSelectionModel().clearSelection();
            for (DayOfWeek day : getWeekdays()) {
                getSelectionModel().select(day);
            }
        });
        weekdaysButton.getStyleClass().add("weekdays-button");
        weekdaysButton.managedProperty().bind(weekdaysButton.visibleProperty());
        weekdaysButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));

        Button weekendsButton = createExtraButton("Weekends", () -> {
            getSelectionModel().clearSelection();
            for (DayOfWeek day : getWeekendDays()) {
                getSelectionModel().select(day);
            }
        });
        weekendsButton.getStyleClass().add("weekends-button");
        weekendsButton.managedProperty().bind(weekendsButton.visibleProperty());
        weekendsButton.visibleProperty().bind(currentSelectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));

        VBox extraButtonsBox = new VBox(clearButton, todayButton, allButton, weekdaysButton, weekendsButton);
        extraButtonsBox.getStyleClass().addAll("extra-buttons-box");
        extraButtonsBox.managedProperty().bind(extraButtonsBox.visibleProperty());
        extraButtonsBox.visibleProperty().bind(itemsProperty().emptyProperty().not());

        return extraButtonsBox;
    }

    private List<List<DayOfWeek>> mergeConsecutiveItem(List<DayOfWeek> selectedDays) {
        List<DayOfWeek> days = new ArrayList<>(selectedDays);
        if (days.isEmpty()) {
            return Collections.emptyList();
        }

        // Get localized day order
        List<DayOfWeek> dayOrder = getLocalizedDayOrder();

        // Sort days according to localized order
        days.sort(Comparator.comparingInt(dayOrder::indexOf));

        List<List<DayOfWeek>> ranges = new ArrayList<>();
        List<DayOfWeek> currentRange = new ArrayList<>();
        currentRange.add(days.get(0));

        for (int i = 1; i < days.size(); i++) {
            DayOfWeek previousDay = days.get(i - 1);
            DayOfWeek currentDay = days.get(i);

            int prevIndex = dayOrder.indexOf(previousDay);
            int currIndex = dayOrder.indexOf(currentDay);

            if ((prevIndex + 1) % 7 == currIndex % 7) {
                currentRange.add(currentDay);
            } else {
                ranges.add(new ArrayList<>(currentRange));
                currentRange.clear();
                currentRange.add(currentDay);
            }
        }
        ranges.add(currentRange);
        return ranges;
    }

    /**
     * Get the localized order of DayOfWeek.
     */
    public List<DayOfWeek> getLocalizedDayOrder() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        DayOfWeek firstDayOfWeek = weekFields.getFirstDayOfWeek();
        List<DayOfWeek> dayOrderList = new ArrayList<>();
        DayOfWeek day = firstDayOfWeek;
        for (int i = 0; i < 7; i++) {
            dayOrderList.add(day);
            day = day.plus(1);
        }
        return dayOrderList;
    }

    /**
     * Checks if only weekdays are selected in the current selection model.
     *
     * @return true if the selected days match exactly with the weekdays, otherwise false
     */
    public boolean isOnlyWeekdaysSelected() {
        Set<DayOfWeek> selectedDays = new HashSet<>(getSelectionModel().getSelectedItems());
        Set<DayOfWeek> weekdays = new HashSet<>(getWeekdays());
        return selectedDays.equals(weekdays);
    }

    /**
     * Checks if only weekend days are selected in the current selection model.
     *
     * @return true if the selected days match exactly with the weekend days, otherwise false
     */
    public boolean isOnlyWeekendsSelected() {
        Set<DayOfWeek> selectedDays = new HashSet<>(getSelectionModel().getSelectedItems());
        Set<DayOfWeek> weekends = new HashSet<>(getWeekendDays());
        return selectedDays.equals(weekends);
    }

    /**
     * Checks if all days are selected in the current selection model.
     *
     * @return true if all days are selected, otherwise false
     */
    public boolean isSelectedAll() {
        return getSelectionModel().getSelectedItems().size() == getItems().size();
    }

    /**
     * Retrieves a list of the weekend days based on the current locale.
     *
     * @return a list of DayOfWeek objects representing the weekend days.
     */
    public List<DayOfWeek> getWeekendDays() {
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        DayOfWeek weekendStart = weekFields.getFirstDayOfWeek().plus(5 % 7);
        DayOfWeek weekendEnd = weekendStart.plus(1);

        return List.of(weekendStart, weekendEnd);
    }

    /**
     * Retrieves a list of the weekdays by excluding weekend days.
     *
     * @return a list of DayOfWeek objects representing the weekdays.
     */
    public List<DayOfWeek> getWeekdays() {
        List<DayOfWeek> weekdays = new ArrayList<>(List.of(DayOfWeek.values()));
        weekdays.removeAll(getWeekendDays());
        return weekdays;
    }

}
