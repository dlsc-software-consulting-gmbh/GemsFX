package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthPickerSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
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
import java.util.Objects;

/**
 * A control for quickly selecting the month of a year. The format used for the
 * month depends on the {@link #converterProperty()}. The default converter produces
 * and expects the full month name, e.g. "January", "February", etc. An invalid text
 * resets the value of the picker to null.
 */
public class YearMonthPicker extends CustomComboBox<YearMonth> {

    private final TextField editor = new TextField();

    private YearMonthView yearMonthView;

    /**
     * Constructs a new picker.
     */
    public YearMonthPicker() {
        super();

        getStyleClass().setAll("year-month-picker", "text-input");

        setFocusTraversable(false);
        setOnTouchPressed(evt -> commitValueAndShow());

        valueProperty().addListener(it -> updateTextAndHidedPopup());

        editor.setPromptText("Example: March 2023");
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commit());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commit();
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

        setValue(YearMonth.now());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthPickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(YearMonthView.class.getResource("year-month-picker.css")).toExternalForm();
    }

    public YearMonthView getYearMonthView() {
        if (yearMonthView == null) {
            yearMonthView = new YearMonthView();
        }
        return yearMonthView;
    }

    private void commitValueAndShow() {
        commit();
        show();
    }

    /*
     * Performs the work of actually creating and setting a new month value.
     */
    public void commit() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            StringConverter<YearMonth> converter = getConverter();
            if (converter != null) {
                setValue(converter.fromString(text));
            }
        }
    }

    /*
     * Updates the text of the text field based on the current value / month.
     * Hide the popup.
     */
    private void updateTextAndHidedPopup() {
        YearMonth value = getValue();
        if (value != null && getConverter() != null) {
            editor.setText(getConverter().toString(value));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());

        YearMonthPickerSkin skin = (YearMonthPickerSkin) getSkin();
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
}
