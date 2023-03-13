package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthPickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * A control for quickly selecting the month of a year. The format used for the
 * month depends on the {@link #converterProperty()}. The default converter produces
 * and expects the full month name, e.g. "January", "February", etc. An invalid text
 * resets the value of the picker to null.
 */
public class YearMonthPicker extends Control {

    private final TextField editor = new TextField();

    /**
     * Constructs a new picker.
     */
    public YearMonthPicker() {
        super();

        getStyleClass().addAll("year-month-picker", "text-input");

        setFocusTraversable(false);

        valueProperty().addListener(it -> updateText());

        editor.setPromptText("Example: March 2023");
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commitValue());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commitValue();
            }
            pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), editor.isFocused());
        });

        editor.addEventHandler(KeyEvent.ANY, evt -> {
            if (evt.getCode().equals(KeyCode.DOWN)) {
                setValue(getValue().plusMonths(1));
            } else if (evt.getCode().equals(KeyCode.UP)) {
                setValue(getValue().minusMonths(1));
            }
        });

        converterProperty().addListener((obs, oldConverter, newConverter) -> {
            if (newConverter == null) {
                setConverter(oldConverter);
            }
        });

        setMaxWidth(Region.USE_PREF_SIZE);
        updateText();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthPickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return YearMonthView.class.getResource("year-month-picker.css").toExternalForm();
    }

    /*
     * Performs the work of actually creating and setting a new month value.
     */
    private void commitValue() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            StringConverter<YearMonth> converter = getConverter();
            if (converter != null) {
                YearMonth value = converter.fromString(text);
                if (value != null) {
                    setValue(value);
                } else {
                    setValue(null);
                }
            }
        }
    }

    /*
     * Updates the text of the text field based on the current value / month.
     */
    private void updateText() {
        YearMonth value = getValue();
        if (value != null && getConverter() != null) {
            editor.setText(getConverter().toString(value));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());
    }

    /**
     * Returns the text field control used for manual input.
     *
     * @return the editor / text field
     */
    public final TextField getEditor() {
        return editor;
    }

    private final ObjectProperty<StringConverter<YearMonth>> converter = new SimpleObjectProperty<>(this, "value", new StringConverter<>() {
        @Override
        public String toString(YearMonth object) {
            if (object != null) {
                return DateTimeFormatter.ofPattern("MMMM yyyy").format(object);
            }
            return null;
        }

        @Override
        public YearMonth fromString(String string) {
            try {
                return DateTimeFormatter.ofPattern("MMMM yyyy").parse(string, YearMonth::from);
            } catch (DateTimeParseException ex) {
                return null;
            }
        }
    });

    public final StringConverter<YearMonth> getConverter() {
        return converter.get();
    }

    /**
     * A converter used to translate a text into a YearMonth object and vice
     * versa.
     *
     * @return the converter object
     */
    public final ObjectProperty<StringConverter<YearMonth>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<YearMonth> converter) {
        this.converter.set(converter);
    }

    private final ObjectProperty<YearMonth> value = new SimpleObjectProperty<>(this, "value", YearMonth.now());

    public final YearMonth getValue() {
        return value.get();
    }

    /**
     * Stores the current value / the currently selected month.
     *
     * @return the current value / currently selected month
     */
    public final ObjectProperty<YearMonth> valueProperty() {
        return value;
    }

    public final void setValue(YearMonth value) {
        this.value.set(value);
    }

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

    public final boolean isEditable() {
        return editable.get();
    }

    /**
     * Controls whether the text field inside this control can be edited or not.
     *
     * @return true if the field can be edited
     */
    public final BooleanProperty editableProperty() {
        return editable;
    }

    public final void setEditable(boolean editable) {
        this.editable.set(editable);
    }
}
