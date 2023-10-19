package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhoneNumberFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

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

    // AVAILABLE COUNTRY CODES

    private final ObservableList<CountryCallingCode> availableCountryCodes = FXCollections.observableArrayList(CountryCallingCode.defaults());

    public final ObservableList<CountryCallingCode> getAvailableCountryCodes() {
        return availableCountryCodes;
    }

    // SELECTED COUNTRY CODE
    private final ObjectProperty<CountryCallingCode> selectedCountryCode = new SimpleObjectProperty<>(this, "countryCallingCode");

    public final ObjectProperty<CountryCallingCode> selectedCountryCodeProperty() {
        return selectedCountryCode;
    }

    public final CountryCallingCode getSelectedCountryCode() {
        return selectedCountryCodeProperty().get();
    }

    public final void setSelectedCountryCode(CountryCallingCode selectedCountryCode) {
        selectedCountryCodeProperty().set(selectedCountryCode);
    }

    // MASK
    private final StringProperty mask = new SimpleStringProperty(this, "mask", "(###) ###-####");

    public final StringProperty maskProperty() {
        return mask;
    }

    public final String getMask() {
        return maskProperty().get();
    }

    public final void setMask(String mask) {
        maskProperty().set(mask);
    }

}
