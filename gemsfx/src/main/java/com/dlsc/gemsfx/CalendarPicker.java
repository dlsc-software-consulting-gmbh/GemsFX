package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.CalendarPickerSkin;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

/**
 * A control for selecting a date that replaces the default {@link javafx.scene.control.DatePicker} that
 * ships with JavaFX. The month view that the user can bring up by clicking on the button on the right-hand
 * side of this control is an instance of {@link CalendarView}, which is more powerful than the normal view
 * used by {@link javafx.scene.control.DatePicker}. The {@link CalendarView} allows the user to directly jump
 * to a specific month or year.
 */
public class CalendarPicker extends CustomComboBox<LocalDate> {

    private final TextField editor = new TextField();

    private CalendarView calendarView;

    /**
     * Constructs a new calendar picker.
     */
    public CalendarPicker() {
        super();

        getStyleClass().setAll("calendar-picker", "text-input");

        setEditable(true);

        setOnTouchPressed(evt -> commitValueAndShow());

        calendarView = getCalendarView();
        calendarView.setShowToday(true);
        calendarView.setShowTodayButton(true);
        calendarView.dateFilterProperty().bind(dateFilterProperty());

        setFocusTraversable(false);

        valueProperty().addListener(it -> updateTextAndHidePopup());

        editor.promptTextProperty().bindBidirectional(promptTextProperty());
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commitValue());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commitValue();
            }
            pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), editor.isFocused());
        });

        editor.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.UP)) {
                setValue(getValue() != null ? getValue().minusDays(1) : LocalDate.now());
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.DOWN)) {
                setValue(getValue() != null ? getValue().plusDays(1) : LocalDate.now());
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.LEFT) && !isEditable()) {
                setValue(getValue() != null ? getValue().minusDays(1) : LocalDate.now());
                placeCaretAtEnd();
            } else if (evt.getCode().equals(KeyCode.RIGHT) && !isEditable()) {
                setValue(getValue() != null ? getValue().plusDays(1) : LocalDate.now());
                placeCaretAtEnd();
            }
        });

        converterProperty().addListener((obs, oldConverter, newConverter) -> {
            if (newConverter == null) {
                setConverter(oldConverter);
            }
        });

        setMaxWidth(Region.USE_PREF_SIZE);
        updateTextAndHidePopup();
    }

    private void placeCaretAtEnd() {
        Platform.runLater(() -> getEditor().positionCaret(getEditor().textProperty().getValueSafe().length()));
    }

    private void commitValueAndShow() {
        commitValue();
        show();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CalendarPickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(CalendarPicker.class.getResource("calendar-picker.css")).toExternalForm();
    }

    /**
     * Returns the view used to display dates when the picker is opening up. This method can be overridden
     * to return a customized version of a {@link CalendarView}.
     *
     * @return the calendar view for picking a date
     */
    public CalendarView getCalendarView() {
        if (calendarView == null) {
            calendarView = new CalendarView();
        }
        return calendarView;
    }

    /*
     * Performs the work of actually creating and setting a new month value.
     */
    private void commitValue() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            StringConverter<LocalDate> converter = getConverter();
            if (converter != null) {
                LocalDate value = converter.fromString(text);
                setValue(value);
            }
        }
    }

    /*
     * Updates the text of the text field based on the current value / month.
     * Hide the popup.
     */
    private void updateTextAndHidePopup() {
        LocalDate value = getValue();
        if (value != null && getConverter() != null) {
            editor.setText(getConverter().toString(value));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());

        CalendarPickerSkin skin = (CalendarPickerSkin) getSkin();
        if (skin != null) {
            skin.hide();
        }
    }

    /**
     * Returns the text field control used for manual input.
     *
     * @return the editor / text field
     */
    public final TextField getEditor() {
        return editor;
    }

    private final ObjectProperty<StringConverter<LocalDate>> converter = new SimpleObjectProperty<>(this, "value", new LocalDateStringConverter());

    public final StringConverter<LocalDate> getConverter() {
        return converter.get();
    }

    /**
     * A converter used to translate a text into a YearMonth object and vice
     * versa.
     *
     * @return the converter object
     */
    public final ObjectProperty<StringConverter<LocalDate>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<LocalDate> converter) {
        this.converter.set(converter);
    }

    private final ObjectProperty<Callback<LocalDate,Boolean>> dateFilter = new SimpleObjectProperty<>(this, "dateFilter");

    public final Callback<LocalDate, Boolean> getDateFilter() {
        return dateFilter.get();
    }

    /**
     * A property to define a filter for determining which dates in the calendar can be selected.
     * This filter is applied to each date displayed in the calendar. If the filter returns true for
     * a given date, that date will be selectable (i.e., it passes the filter). If the filter returns
     * false, the date will be disabled and cannot be selected. This property is particularly useful
     * for scenarios where only specific dates should be available for selection based on custom
     * logic, such as business rules, holidays, or availability.
     * <p>
     * When SelectionMode is {@link CalendarView.SelectionModel.SelectionMode#DATE_RANGE}, disabled dates can be included within the selected range.
     * However, disabled dates cannot be used as either the starting or ending point of the range.
     *
     * @return a callback that determines the selection ability of each date based on custom criteria.
     */
    public final ObjectProperty<Callback<LocalDate, Boolean>> dateFilterProperty() {
        return dateFilter;
    }

    public final void setDateFilter(Callback<LocalDate, Boolean> dateFilter) {
        this.dateFilter.set(dateFilter);
    }
}
