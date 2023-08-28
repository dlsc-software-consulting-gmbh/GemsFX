package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearPickerSkin;
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
import java.time.LocalDate;
import java.util.function.UnaryOperator;

public class YearPicker extends ComboBoxBase<Integer> {

    private final TextField editor = new TextField();
    private final NumberStringFilteredConverter converter = new NumberStringFilteredConverter();

    public YearPicker() {
        getStyleClass().setAll("year-picker", "text-input");

        setValue(LocalDate.now().getYear());
        setFocusTraversable(false);

        valueProperty().addListener(it -> updateText());

        editor.setTextFormatter(new TextFormatter<>(converter, null, converter.getFilter()));
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
                setValue(getValue() + 1);
            } else if (evt.getCode().equals(KeyCode.UP)) {
                setValue(getValue() - 1);
            }
        });

        setMaxWidth(Region.USE_PREF_SIZE);
        updateText();
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

    private void commit() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            Number value = converter.fromString(text);
            if (value != null) {
                setValue(value.intValue());
            } else {
                setValue(null);
            }
        }
    }

    private void updateText() {
        Integer value = getValue();
        if (value != null) {
            editor.setText("" + value);
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
                System.out.println(change);
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
