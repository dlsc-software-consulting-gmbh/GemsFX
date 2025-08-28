package com.dlsc.gemsfx;

import com.dlsc.gemsfx.daterange.DateRange;
import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.util.EnumStringConverter;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A control for creating filters based on various other controls such as the {@link SelectionBox},
 * the {@link DateRangePicker}, the {@link SearchTextField}, etc...
 *
 * The control automatically manages a list of {@link ChipView} instances based on the current selection.
 * These chip views can be displayed by the {@link ChipsViewContainer}. To do so bind the observable list
 * of chips views of the filter view with the one provided by the {@link ChipsViewContainer}.
 */
public class SimpleFilterView extends HBox {

    private static final PseudoClass COMPACT_PSEUDO_CLASS = PseudoClass.getPseudoClass("compact");

    /*
     * Internal data structure used to map controls to the chip views used to represent
     * their selected items.
     */
    private final Map<Node, Map<Object, ChipView<?>>> map = new HashMap<>();

    private boolean clearing;

    private final InvalidationListener changeListener = it -> {
        if (!clearing && getOnChange() != null) {
            getOnChange().run();
        }
    };

    /**
     * Constructs a new instance of {@code SimpleFilterView}.
     */
    public SimpleFilterView() {
        getStyleClass().add("simple-filter-view");

        layoutModeProperty().addListener(it -> updatePseudoClass());
        updatePseudoClass();

        getChildren().addListener((ListChangeListener<Node>) change -> {
            int size = getChildren().size();
            if (size == 1) {
                updateNodeStyles(getChildren().getFirst());
                getChildren().getFirst().getStyleClass().add("only");
            } else {
                for (int i = 0; i < getChildren().size(); i++) {

                    Node node = getChildren().get(i);
                    updateNodeStyles(node);

                    if (i == 0) {
                        node.getStyleClass().add("first");
                    } else if (i == size - 1) {
                        node.getStyleClass().add("last");
                    } else {
                        node.getStyleClass().add("middle");
                    }
                }
            }
        });
    }

    private void updateNodeStyles(Node node) {
        node.getStyleClass().removeAll("first", "middle", "last", "only", "selection-item");
        node.getStyleClass().add("selection-item");
        if (node instanceof Region) {
            ((Region) node).setMaxHeight(Double.MAX_VALUE);
        }
    }

    private void updatePseudoClass() {
        pseudoClassStateChanged(COMPACT_PSEUDO_CLASS, getLayoutMode() == LayoutMode.COMPACT);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SimpleFilterView.class.getResource("simple-filter-view.css")).toExternalForm();
    }

    /**
     * An enumeration of possible layouts supported by the filter view.
     */
    public enum LayoutMode {

        /**
         * The filter elements (selection boxes, date pickers, etc.) will be laid
         * out one after the other with standard spacing and each element styled in its
         * own standard way.
         */
        STANDARD,

        /**
         * The filter elements will be laid out right next to each other to create a
         * compact visualization.
         * The styling will ensure a unified look of the overall filter view.
         */
        COMPACT
    }

    private final ObjectProperty<LayoutMode> layoutMode = new SimpleObjectProperty<>(this, "layoutMode", LayoutMode.STANDARD);

    public final LayoutMode getLayoutMode() {
        return layoutMode.get();
    }

    /**
     * The layout used by the control, either "standard" or "compact" / "unified".
     *
     * @return the layout mode
     */
    public final ObjectProperty<LayoutMode> layoutModeProperty() {
        return layoutMode;
    }

    public final void setLayoutMode(LayoutMode layoutMode) {
        this.layoutMode.set(layoutMode);
    }

    /**
     * Clears all selections and values in the filter view.
     *
     * This method iterates through the filter components, such as selection boxes and pickers, and resets
     * their values or selections to the default state. It also clears any associated mappings and updates
     * the UI accordingly.
     *
     * If an unknown node type is encountered, this method will throw an {@code IllegalStateException}.
     *
     * After clearing, if a change listener is defined, it will be executed.
     *
     * This operation ensures that the filter view is in a clean state and ready for new inputs or selections.
     *
     * Note: During the execution of this method, a flag is set to avoid triggering unwanted actions
     * due to internal state changes.
     *
     * @see #setOnChange(Runnable)
     *
     * @throws IllegalStateException - if an unsupported node type is found during iteration.
     */
    public final void clear() {
        clearing = true;
        try {
            map.keySet().forEach(node -> {
                if (node instanceof SelectionBox) {
                    ((SelectionBox<?>) node).getSelectionModel().clearSelection();
                } else if (node instanceof DateRangePicker) {
                    ((DateRangePicker) node).setValue(null);
                } else if (node instanceof CalendarPicker) {
                    ((CalendarPicker) node).setValue(null);
                } else if (node instanceof DatePicker) {
                    ((DatePicker) node).setValue(null);
                } else if (node instanceof TextField) {
                    ((TextField) node).setText(null);
                } else {
                    throw new IllegalStateException("Unknown node type: " + node.getClass().getName());
                }

                Map<Object, ChipView<?>> innerMap = map.get(node);
                if (innerMap != null) {
                    innerMap.clear();
                }
            });

            if (getOnChange() != null) {
                getOnChange().run();
            }
        } finally {
            clearing = false;
        }
    }

    private final ObjectProperty<Runnable> onChange = new SimpleObjectProperty<>(this, "onChange");

    public final Runnable getOnChange() {
        return onChange.get();
    }

    /**
     * Will be invoked whenever anything changes within the filter settings.
     *
     * @return the runnable to executed upon change
     */
    public final ObjectProperty<Runnable> onChangeProperty() {
        return onChange;
    }

    public final void setOnChange(Runnable onChange) {
        this.onChange.set(onChange);
    }

    /**
     * Adds a selection box to the UI with the given prompt name and the values of the given
     * enum type.
     *
     * @param text the prompt text
     * @param clz the type of the enum
     * @return the newly added selection box
     *
     * @param <T> the enum type
     */
    public final <T extends Enum<T>> SelectionBox<T> addSelectionBox(String text, Class<T> clz) {
        return addSelectionBox(text, clz.getEnumConstants());
    }

    /**
     * Adds a selection box to the UI with the given prompt name and the given values.
     *
     * @param text the prompt text
     * @param values the possible values
     * @return the newly added selection box
     *
     * @param <T> the enum type
     */
    @SafeVarargs
    public final <T extends Enum<T>> SelectionBox<T> addSelectionBox(String text, T... values) {
        SelectionBox<T> selectionBox = addSelectionBox(text);
        selectionBox.getItems().setAll(values);
        selectionBox.setItemConverter(new EnumStringConverter<>());
        return selectionBox;
    }

    /**
     * Adds a selection box to the UI with the given prompt name and the list of the
     * given values.
     *
     * @param text the prompt text
     * @param values the possible values collection
     * @return the newly added selection box
     *
     * @param <T> the type of the items in the selection box
     */
    public final <T> SelectionBox<T> addSelectionBox(String text, Collection<T> values) {
        SelectionBox<T> selectionBox = addSelectionBox(text);
        selectionBox.getItems().setAll(values);
        return selectionBox;
    }

    /**
     * Adds a selection box to the UI with the given prompt name and the list of the
     * given values.
     *
     * @param text the prompt text
     * @param values the possible values
     * @return the newly added selection box
     *
     * @param <T> the type of the items in the selection box
     */
    @SafeVarargs
    public final <T> SelectionBox<T> addSelectionBox(String text, T... values) {
        SelectionBox<T> selectionBox = addSelectionBox(text);
        selectionBox.getItems().setAll(values);
        return selectionBox;
    }

    /**
     * Adds a selection box to the filter view with the specified prompt text. The selection box
     * allows users to select multiple items, and the selected items are represented as chips
     * in the filter view.
     *
     * The method initializes the selection box with default properties, sets up listeners to
     * handle changes in the selected items, and manages the associated chip view for each selected item.
     * It ensures proper updates to the UI elements when items are added or removed from the selection box.
     *
     * @param text the prompt text to display in the selection box
     * @param <T> the type of the items in the selection box
     *
     * @return the newly created and configured {@code SelectionBox} instance
     */
    public final <T> SelectionBox<T> addSelectionBox(String text) {
        SelectionBox<T> box = createSelectionBox();
        box.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        box.setPromptText(text);
        box.setMaxWidth(Double.MAX_VALUE);
        box.getSelectionModel().getSelectedItems().addListener(changeListener);
        box.setSelectedItemsConverter(new StringConverter<>() {
            @Override
            public String toString(List<T> object) {
                return text;
            }

            @Override
            public List<T> fromString(String string) {
                return List.of();
            }
        });

        ListChangeListener<? super T> l = c -> {
            while (c.next()) {
                if (c.wasReplaced()) {
                    removeChips(c.getRemoved(), box);
                    addChips(c.getAddedSubList(), box);
                } else if (c.wasRemoved()) {
                    removeChips(c.getRemoved(), box);
                } else if (c.wasAdded()) {
                    addChips(c.getAddedSubList(), box);
                }
            }
        };

        box.getSelectionModel().getSelectedItems().addListener(l);
        getChildren().add(box);

        return box;
    }

    private <T> void removeChips(List<? extends T> removedList, SelectionBox<T> box) {
        removedList.forEach(item -> {
            Map<Object, ChipView<?>> innerMap = map.get(box);
            if (innerMap != null) {
                ChipView<?> chip = innerMap.remove(item);
                if (chip != null) {
                    getChips().remove(chip);
                }
            }
        });
    }

    private <T> void addChips(List<? extends T> addedList, SelectionBox<T> box) {
        addedList.forEach(item -> {
            ChipView<T> chip = new ChipView<>();
            chip.setValue(item);
            chip.textProperty().bind(Bindings.createStringBinding(() -> {
                StringConverter<T> itemConverter = box.getItemConverter();
                if (itemConverter != null) {
                    return itemConverter.toString(item);
                }
                return "";
            }, box.itemConverterProperty()));
            chip.setOnClose(status -> box.getSelectionModel().clearSelection(box.getItems().indexOf(status)));
            getChips().add(chip);
            map.computeIfAbsent(box, it -> new HashMap<>()).put(item, chip);
        });
    }

    /**
     * Creates a new instance of a {@code SelectionBox}. This method serves as a factory for creating
     * selection boxes, in case customization or modification of the default selection box behavior
     * is required. Subclasses can override this method to provide a different implementation.
     *
     * @param <T> the type of the items that the selection box will manage
     * @return a new instance of {@code SelectionBox} configured with default settings
     */
    protected <T> SelectionBox<T> createSelectionBox() {
        return new SelectionBox<>();
    }

    /**
     * Creates a new instance of a textfield.
     *
     * @param promptText the initial prompt text
     * @return the new text field
     */
    public final CustomTextField addTextField(String promptText) {
        CustomTextField textField = new CustomTextField();
        textField.setPromptText(promptText);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.textProperty().addListener(changeListener);
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (oldText != null) {
                Map<Object, ChipView<?>> innerMap = map.get(textField);
                if (innerMap != null) {
                    ChipView<?> chip = innerMap.remove(oldText);
                    if (chip != null) {
                        getChips().remove(chip);
                    }
                }
            }
            if (StringUtils.isNotBlank(newText)) {
                ChipView<String> chip = new ChipView<>();
                chip.setValue(newText);
                chip.setText(newText);
                chip.setOnClose(it -> textField.setText(null));
                getChips().add(chip);

                map.computeIfAbsent(textField, it -> new HashMap<>()).put(newText, chip);
            }
        });

        getChildren().add(textField);

        return textField;
    }

    /**
     * Add a new instance of a search text field.
     *
     * @see SearchTextField
     * @param promptText the initial prompt text
     * @return the new text field
     */
    public final SearchTextField addSearchTextField(String promptText) {
        SearchTextField textField = new SearchTextField();
        textField.setPromptText(promptText);
        textField.setMaxWidth(Double.MAX_VALUE);
        textField.textProperty().addListener(changeListener);
        textField.textProperty().addListener((obs, oldText, newText) -> {
            if (oldText != null) {
                Map<Object, ChipView<?>> innerMap = map.get(textField);
                if (innerMap != null) {
                    ChipView<?> chip = innerMap.remove(oldText);
                    if (chip != null) {
                        getChips().remove(chip);
                    }
                }
            }
            if (StringUtils.isNotBlank(newText)) {
                ChipView<String> chip = new ChipView<>();
                chip.setValue(newText);
                chip.setText(newText);
                chip.setOnClose(it -> textField.setText(null));
                getChips().add(chip);

                map.computeIfAbsent(textField, it -> new HashMap<>()).put(newText, chip);
            }
        });

        getChildren().add(textField);

        return textField;
    }

    /**
     * Adds a date range picker to the filter view and configures it with the specified prompt text.
     * This date range picker allows users to select a range of dates, and selected ranges
     * are represented as chips in the filter view.
     *
     * The method initialises the date range picker, adds listeners to handle changes in selection,
     * and manages the associated chip view for each selected range.
     * It ensures proper update of the UI elements when ranges are added or removed.
     *
     * @param text the prompt text to display in the date range picker
     *
     * @return the newly created and configured {@code DateRangePicker} instance
     */
    public final DateRangePicker addDateRangePicker(String text) {
        DateRangePicker dateRangePicker = createDateRangePicker();
        dateRangePicker.setValue(null);
        dateRangePicker.setShowPresetTitle(false);
        dateRangePicker.setPromptText(text);
        dateRangePicker.setShowIcon(false);
        dateRangePicker.valueProperty().addListener(changeListener);
        dateRangePicker.valueProperty().addListener((obs, oldRange, newRange) -> {
            if (oldRange != null) {
                Map<Object, ChipView<?>> innerMap = map.get(dateRangePicker);
                if (innerMap != null) {
                    ChipView<?> chip = innerMap.remove(oldRange);
                    if (chip != null) {
                        getChips().remove(chip);
                    }
                }
            }
            if (newRange != null) {
                ChipView<DateRange> chip = new ChipView<>();
                chip.setValue(newRange);
                chip.setText(newRange.toString());
                chip.setOnClose(it -> dateRangePicker.setValue(null));
                getChips().add(chip);

                map.computeIfAbsent(dateRangePicker, it -> new HashMap<>()).put(newRange, chip);
            }
        });

        getChildren().add(dateRangePicker);

        return dateRangePicker;
    }

    /**
     * Factory method for creating the date range picker that will be added to the view. This method
     * can be overridden to provide a custom date range picker implementations.
     *
     * @return a date range picker
     */
    protected DateRangePicker createDateRangePicker() {
        return new DateRangePicker();
    }

    /**
     * Adds a calendar picker to the filter view with the specified prompt text. The calendar picker allows
     * users to select a date, and the selected date is represented as a chip in the filter view.
     *
     * The method initializes the calendar picker, sets its default properties, and adds listeners to
     * handle changes in the selected date. When a date is selected, it is displayed as a chip, and
     * the chip can be removed by the user, clearing the date from the picker.
     *
     * @param text the prompt text to display in the date picker
     *
     * @return the newly created and configured {@code DatePicker} instance
     */
    public final CalendarPicker addCalendarPicker(String text) {
        CalendarPicker datePicker = createCalendarPicker();
        datePicker.setEditable(false);
        datePicker.setValue(null);
        datePicker.setPromptText(text);
        datePicker.valueProperty().addListener(changeListener);
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (oldDate != null) {
                Map<Object, ChipView<?>> innerMap = map.get(datePicker);
                if (innerMap != null) {
                    ChipView<?> chip = innerMap.remove(oldDate);
                    if (chip != null) {
                        getChips().remove(chip);
                    }
                }
            }
            if (newDate != null) {
                ChipView<LocalDate> chip = new ChipView<>();
                chip.setValue(newDate);
                chip.setText(newDate.toString());
                chip.setOnClose(it -> datePicker.setValue(null));
                getChips().add(chip);

                map.computeIfAbsent(datePicker, it -> new HashMap<>()).put(newDate, chip);
            }
        });

        getChildren().add(datePicker);

        return datePicker;
    }

    /**
     * Factory method for creating a new {@code CalendarPicker} instance.
     * This method is used to instantiate a {@code CalendarPicker} with default properties.
     * Subclasses can override this method to customize the behavior or appearance of the
     * {@code CalendarPicker}.
     *
     * @return a new instance of {@code CalendarPicker}.
     */
    protected CalendarPicker createCalendarPicker() {
        return new CalendarPicker();
    }

    /**
     * Adds a date picker to the filter view with the specified prompt text. The date picker allows
     * users to select a date, and the selected date is represented as a chip in the filter view.
     *
     * The method initializes the date picker, sets its default properties, and adds listeners to
     * handle changes in the selected date. When a date is selected, it is displayed as a chip, and
     * the chip can be removed by the user, clearing the date from the picker.
     *
     * @param text the prompt text to display in the date picker
     *
     * @return the newly created and configured {@code DatePicker} instance
     */
    public final DatePicker addDatePicker(String text) {
        DatePicker datePicker = createDatePicker();
        datePicker.setEditable(false);
        datePicker.setValue(null);
        datePicker.setPromptText(text);
        datePicker.valueProperty().addListener(changeListener);
        datePicker.valueProperty().addListener((obs, oldDate, newDate) -> {
            if (oldDate != null) {
                Map<Object, ChipView<?>> innerMap = map.get(datePicker);
                if (innerMap != null) {
                    ChipView<?> chip = innerMap.remove(oldDate);
                    if (chip != null) {
                        getChips().remove(chip);
                    }
                }
            }
            if (newDate != null) {
                ChipView<LocalDate> chip = new ChipView<>();
                chip.setValue(newDate);
                chip.setText(newDate.toString());
                chip.setOnClose(it -> datePicker.setValue(null));
                getChips().add(chip);

                map.computeIfAbsent(datePicker, it -> new HashMap<>()).put(newDate, chip);
            }
        });

        getChildren().add(datePicker);

        return datePicker;
    }

    /**
     * Factory method for creating a new {@code DatePicker} instance.
     * This method can be overridden to provide a customized implementation
     * of the {@code DatePicker}.
     *
     * @return a new instance of {@code DatePicker}.
     */
    protected DatePicker createDatePicker() {
        return new DatePicker();
    }

    private final ListProperty<ChipView<?>> chips = new SimpleListProperty<>(FXCollections.observableArrayList());

    public final ObservableList<ChipView<?>> getChips() {
        return chips.get();
    }

    /**
     * An observable list containing the currently managed chip views. Each chip view represents one selected
     * filter criteria.
     *
     * @return the list of chip views
     */
    public final ListProperty<ChipView<?>> chipsProperty() {
        return chips;
    }

    public final void setChips(ObservableList<ChipView<?>> chips) {
        this.chips.set(chips);
    }
}
