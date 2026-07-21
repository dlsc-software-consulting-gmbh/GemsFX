package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthPickerSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import com.dlsc.gemsfx.util.StringUtils;

import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import com.dlsc.gemsfx.util.ResourceBundleManager;

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
        AccessibilityUtil.setRole(this, AccessibleRole.COMBO_BOX);
        AccessibilityUtil.bindAccessibleText(this, Bindings.createStringBinding(() -> {
            YearMonth value = getValue();
            StringConverter<YearMonth> converter = getConverter();
            return value == null ? null : converter != null ? converter.toString(value) : value.toString();
        }, valueProperty(), converterProperty()));

        setFocusTraversable(false);
        setOnTouchPressed(evt -> commitValueAndShow());

        valueProperty().addListener(it -> updateTextAndHidedPopup());

        editor.setPromptText(ResourceBundleManager.getString(ResourceBundleManager.BundleType.YEAR_MONTH_PICKER, "prompt.example-month-year", "Example: March 2023"));
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commit());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commit();
            }
            pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), editor.isFocused());
        });

        editor.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
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

        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        setValue(YearMonth.now());
    }

    /**
     * {@inheritDoc}
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthPickerSkin(this);
    }

    /**
     * {@inheritDoc}
     *
     * @return the user agent stylesheet
     */
    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(YearMonthView.class.getResource("year-month-picker.css")).toExternalForm();
    }

    /**
     * Returns the year-month view used by this picker.
     *
     * @return the year-month view
     */
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
    /**
     * Commits the text currently shown in the editor to the picker value.
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
        /**
         * {@inheritDoc}
         *
         * @return the string representation of the value
         *
         * @param object the value to convert
         */
        @Override
        public String toString(YearMonth object) {
            if (object != null) {
                return DateTimeFormatter.ofPattern("MMMM yyyy").format(object);
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @return the parsed value
         *
         * @param string the string to parse
         */
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
