package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhoneNumberFieldSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

public class PhoneNumberField extends Control {

    public static final String DEFAULT_STYLE_CLASS = "phone-number-field";

    public PhoneNumberField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        getAvailableCountryCodes().setAll(CountryCallingCode.defaultValues());

        phoneNumberProperty().addListener(new ChangeListener<>() {
            class CountryCallingCodeScore implements Comparable<CountryCallingCodeScore> {
                int score;
                String localPhoneNumber;
                @Override
                public int compareTo(CountryCallingCodeScore o) {
                    return Integer.compare(score, o.score);
                }
            }

            @Override
            public void changed(ObservableValue<? extends String> observableValue, String oldPhone, String newPhone) {
                TreeMap<CountryCallingCodeScore, List<CountryCallingCode>> scores = new TreeMap<>();

                for (CountryCallingCode code : getAvailableCountryCodes()) {
                    CountryCallingCodeScore score = calculateScore(code, newPhone);
                    if (score.score > 0) {
                        scores.computeIfAbsent(score, s -> new ArrayList<>()).add(code);
                    }
                }

                Map.Entry<CountryCallingCodeScore, List<CountryCallingCode>> higher = scores.lastEntry();

                if (higher != null) {
                    CountryCallingCodeScore score = higher.getKey();
                    // For now just picking the last one, but we could also check if there are multiple
                    CountryCallingCode code = higher.getValue().get(higher.getValue().size() - 1);
                    countryCode.set(code);
                    localPhoneNumber.set(score.localPhoneNumber);
                } else {
                    countryCode.set(null);
                    localPhoneNumber.set(null);
                }
            }

            private CountryCallingCodeScore calculateScore(CountryCallingCode code, String phoneNumber) {
                CountryCallingCodeScore score = new CountryCallingCodeScore();
                score.score = 0;

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    String countryStr = String.valueOf(code.countryCode());

                    if (code.areaCodes().length == 0) {
                        if (phoneNumber.startsWith(countryStr)) {
                            score.score = 1;
                            if (phoneNumber.length() > countryStr.length()) {
                                score.localPhoneNumber = phoneNumber.substring(countryStr.length());
                            }
                        }
                    } else {
                        for (int areaCode : code.areaCodes()) {
                            String areaCodeStr = countryStr + areaCode;
                            if (phoneNumber.startsWith(areaCodeStr)) {
                                score.score = 2;
                                if (phoneNumber.length() > areaCodeStr.length()) {
                                    score.localPhoneNumber = phoneNumber.substring(areaCodeStr.length());
                                } else {
                                    score.localPhoneNumber = phoneNumber.substring(countryStr.length());
                                }
                                break;
                            }
                        }
                    }
                }

                return score;
            }
        });

        defaultCountryCodeProperty().addListener((obs, oldCode, newCode) -> {
            if (countryCode.get() == null && newCode != null) {
                countryCode.set(newCode);
                setPhoneNumber(newCode.phonePrefix());
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PhoneNumberField.class.getResource("phone-number-field.css")).toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhoneNumberFieldSkin(this);
    }

    // VALUES
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

    final ReadOnlyStringWrapper localPhoneNumber = new ReadOnlyStringWrapper(this, "localPhoneNumber");

    public final ReadOnlyStringProperty localPhoneNumberProperty() {
        return localPhoneNumber.getReadOnlyProperty();
    }

    public final String getLocalPhoneNumber() {
        return localPhoneNumber.get();
    }

    final ReadOnlyObjectWrapper<CountryCallingCode> countryCode = new ReadOnlyObjectWrapper<>(this, "countryCode");

    public final ReadOnlyObjectProperty<CountryCallingCode> countryCodeProperty() {
        return countryCode.getReadOnlyProperty();
    }

    public final CountryCallingCode getCountryCode() {
        return countryCode.get();
    }

    // SETTINGS

    private final ObjectProperty<CountryCallingCode> defaultCountryCode = new SimpleObjectProperty<>(this, "defaultCountryCode");

    public final ObjectProperty<CountryCallingCode> defaultCountryCodeProperty() {
        return defaultCountryCode;
    }

    public final CountryCallingCode getDefaultCountryCode() {
        return defaultCountryCode.get();
    }

    public final void setDefaultCountryCode(CountryCallingCode defaultCountryCode) {
        this.defaultCountryCode.set(defaultCountryCode);
    }

    private final ObservableList<CountryCallingCode> availableCountryCodes = FXCollections.observableArrayList();

    public final ObservableList<CountryCallingCode> getAvailableCountryCodes() {
        return availableCountryCodes;
    }

    private final ObservableList<CountryCallingCode> preferredCountryCodes = FXCollections.observableArrayList();

    public final ObservableList<CountryCallingCode> getPreferredCountryCodes() {
        return preferredCountryCodes;
    }

}
