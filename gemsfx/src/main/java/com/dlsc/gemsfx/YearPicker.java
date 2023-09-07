package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearPickerSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.converter.NumberStringConverter;
import org.apache.commons.lang3.StringUtils;

import java.text.DecimalFormat;
import java.text.ParsePosition;
import java.time.Year;
import java.util.function.UnaryOperator;

public class YearPicker extends ComboBoxBase<Year> {

    private final TextField editor = new TextField();
    private final NumberStringFilteredConverter converter = new NumberStringFilteredConverter();

    public YearPicker() {
        getStyleClass().setAll("year-picker", "text-input");

        setFocusTraversable(false);

        valueProperty().addListener((obs, oldV, newV) -> {
            updateText(newV);
            year.set(newV == null ? null : newV.getValue());
        });

        editor.setTextFormatter(new TextFormatter<>(converter, null, converter.getFilter()));
        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commit());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commit();
            }
            pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), editor.isFocused());
        });

        editor.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            Year value = getValue();
            if (value != null) {
                if (evt.getCode().equals(KeyCode.DOWN)) {
                    setValue(value.plusYears(1));
                } else if (evt.getCode().equals(KeyCode.UP)) {
                    setValue(value.minusYears(1));
                }
            }
        });

        setMaxWidth(Region.USE_PREF_SIZE);
        updateText(null);
    }

    /**
     * Returns the text field control used for manual input.
     *
     * @return the editor / text field
     */
    public final TextField getEditor() {
        return editor;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearPickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return YearMonthView.class.getResource("year-picker.css").toExternalForm();
    }

    private final ReadOnlyObjectWrapper<Integer> year = new ReadOnlyObjectWrapper<>(this, "wrapper");

    public final ReadOnlyObjectProperty<Integer> yearProperty() {
        return year.getReadOnlyProperty();
    }

    public final Integer getYear() {
        return year.get();
    }

    private void commit() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            Number value = converter.fromString(text);
            if (value != null) {
                setValue(Year.of(value.intValue()));
            } else {
                setValue(null);
            }
        }
    }

    private void updateText(Year value) {
        if (value != null) {
            editor.setText(String.valueOf(value.getValue()));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());
    }

    static class NumberStringFilteredConverter extends NumberStringConverter {

        public NumberStringFilteredConverter() {
            super(new DecimalFormat("####"));
        }

        UnaryOperator<TextFormatter.Change> getFilter() {
            return change -> {
                String newText = change.getControlNewText();

                if (!newText.isEmpty()) {
                    // Convert to number
                    ParsePosition parsePosition = new ParsePosition(0);
                    Number value = getNumberFormat().parse(newText, parsePosition);
                    if (value == null || parsePosition.getIndex() < newText.length()) {
                        return null;
                    }

                    // Validate max length
                    if (newText.length() > 4) {
                        String head = change.getControlNewText().substring(0, 4);
                        change.setText(head);
                        int oldLength = change.getControlText().length();
                        change.setRange(0, oldLength);
                    }
                }

                return change;
            };
        }

    }

}
