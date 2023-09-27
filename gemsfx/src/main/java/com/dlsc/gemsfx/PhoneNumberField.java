package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhoneNumberFieldSkin;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
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
    private final StringProperty phoneNumber = new SimpleStringProperty(this, "phoneNumber");

    public final StringProperty phoneNumberProperty() {
        return phoneNumber;
    }

    public final String getPhoneNumber() {
        return phoneNumberProperty().get();
    }

    public final void setPhoneNumber(String phoneNumber) {
        phoneNumberProperty().set(phoneNumber);
    }

    // COUNTRY
    private final IntegerProperty countryCode = new SimpleIntegerProperty(this, "countryCode");

    public final IntegerProperty countryCodeProperty() {
        return countryCode;
    }

    public final int getCountryCode() {
        return countryCodeProperty().get();
    }

    public final void setCountryCode(int countryCode) {
        countryCodeProperty().set(countryCode);
    }

}
