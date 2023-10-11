package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhoneNumberFieldSkin;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Objects;

public class PhoneNumberField extends Control {

    public static final String DEFAULT_STYLE_CLASS = "phone-number-field";

    public PhoneNumberField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhoneNumberFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PhoneNumberField.class.getResource("phone-number-field.css")).toExternalForm();
    }

    // VALUE
    private final StringProperty value = new SimpleStringProperty(this, "value");

    public final StringProperty valueProperty() {
        return value;
    }

    public final String getValue() {
        return valueProperty().get();
    }

    public final void setValue(String value) {
        valueProperty().set(value);
    }

}
