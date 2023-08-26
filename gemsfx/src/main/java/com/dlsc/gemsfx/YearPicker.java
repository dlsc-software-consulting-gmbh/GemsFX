package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearPickerSkin;
import javafx.css.PseudoClass;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.time.LocalDate;

public class YearPicker extends ComboBoxBase<Integer> {

    private final TextField editor = new TextField();

    public YearPicker() {
        getStyleClass().setAll("year-picker", "text-input");

        setValue(LocalDate.now().getYear());
        setFocusTraversable(false);

        valueProperty().addListener(it -> updateText());

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
            int value = NumberUtils.toInt(text, -1);
            if (value != -1) {
                setValue(value);
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

    /**
     * Returns the text field control used for manual input.
     *
     * @return the editor / text field
     */
    public final TextField getEditor() {
        return editor;
    }

}
