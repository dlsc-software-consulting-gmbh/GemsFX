package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthPickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
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

public class YearMonthPicker extends Control {

    private TextField editor = new TextField();

    public YearMonthPicker() {
        super();

        getStyleClass().addAll("year-month-picker", "text-input");

        valueProperty().addListener(it -> updateText());

        editor.editableProperty().bind(editableProperty());
        editor.setOnAction(evt -> commitValue());
        editor.focusedProperty().addListener(it -> {
            if (!editor.isFocused()) {
                commitValue();
            }
        });
        editor.addEventHandler(KeyEvent.ANY, evt -> {
            if (evt.getCode().equals(KeyCode.DOWN)) {
                setValue(getValue().plusMonths(1));
            } else if (evt.getCode().equals(KeyCode.UP)) {
                setValue(getValue().minusMonths(1));
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

    private void commitValue() {
        String text = editor.getText();
        if (StringUtils.isNotBlank(text)) {
            StringConverter<YearMonth> converter = getConverter();
            if (converter != null) {
                setValue(converter.fromString(text));
            }
        }
    }

    private void updateText() {
        YearMonth value = getValue();
        if (value != null && getConverter() != null) {
            editor.setText(getConverter().toString(value));
        } else {
            editor.setText("");
        }
        editor.positionCaret(editor.getText().length());
    }

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
            return DateTimeFormatter.ofPattern("MMMM yyyy").parse(string, YearMonth::from);
        }
    });

    public final StringConverter<YearMonth> getConverter() {
        return converter.get();
    }

    public final ObjectProperty<StringConverter<YearMonth>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<YearMonth> converter) {
        this.converter.set(converter);
    }

    private final ObjectProperty<YearMonth> value = new SimpleObjectProperty<>(this, "value", YearMonth.now());

    public YearMonth getValue() {
        return value.get();
    }

    public ObjectProperty<YearMonth> valueProperty() {
        return value;
    }

    public void setValue(YearMonth value) {
        this.value.set(value);
    }

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

    public final boolean isEditable() {
        return editable.get();
    }

    public final BooleanProperty editableProperty() {
        return editable;
    }

    public final void setEditable(boolean editable) {
        this.editable.set(editable);
    }
}
