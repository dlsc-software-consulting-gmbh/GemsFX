package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhoneNumberField2;
import com.google.i18n.phonenumbers.AsYouTypeFormatter;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import javafx.application.Application;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.util.Collections;
import java.util.function.Function;

public class PhoneNumberFieldApp2 extends Application {

    private final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    private final PhoneNumberField2 field = new PhoneNumberField2();

    private static final Function<Object, String> COUNTRY_CODE_CONVERTER = c -> {
        if (c == null) {
            return null;
        }
        PhoneNumberField2.CountryCallingCode code = (PhoneNumberField2.CountryCallingCode) c;
        return "(" + code.phonePrefix() + ") " + code;
    };

    @Override
    public void start(Stage stage) throws Exception {
        field.setPhoneNumberValidator(phoneNumber -> {
            if (field.getCountryCallingCode() == null || phoneNumber == null || phoneNumber.isEmpty() ||
                field.getLocalPhoneNumber() == null || field.getLocalPhoneNumber().isEmpty()) {
                return true;
            }

            try {
                Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, field.getCountryCallingCode().iso2Code());
                return phoneUtil.isValidNumber(number);
            } catch (NumberParseException ignored) {
            }

            return false;
        });

        field.setPhoneNumberFormatter(phoneNumber -> {
            if (phoneNumber == null || phoneNumber.isEmpty() || field.getCountryCallingCode() == null) {
                return "";
            }

            AsYouTypeFormatter formatter = phoneUtil.getAsYouTypeFormatter(field.getCountryCallingCode().iso2Code());

            String formatted = "";
            for (char c : phoneNumber.toCharArray()) {
                formatted = formatter.inputDigit(c);
            }

            return formatted;
        });

        VBox controls = new VBox(10);
        addControl("Available Countries", availableCountriesSelector(field), controls);
        addControl("Preferred Countries", preferredCountriesSelector(field), controls);
        addControl("Disable Country", disableCountryCheck(field), controls);
        addControl("", clearButton(field), controls);

        VBox fields = new VBox(10);
        addField(fields, "Country Code", field.countryCallingCodeProperty(), COUNTRY_CODE_CONVERTER);
        addField(fields, "Phone Number", field.phoneNumberProperty());
        addField(fields, "Local Number", field.localPhoneNumberProperty());
        addField(fields, "E160 Number", Bindings.createStringBinding(() -> formatToE160(field.getLocalPhoneNumber()), field.localPhoneNumberProperty()));
        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(controls, new Separator(), field, new Separator(), fields);

        stage.setTitle("PhoneNumberField2");
        stage.setScene(new Scene(vBox, 500, 500));
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private String formatToE160(String phoneNumber) {
        if (field.getCountryCallingCode() == null || phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return "";
        }

        try {
            System.out.println("parsing '" + phoneNumber + "'");
            Phonenumber.PhoneNumber number = phoneUtil.parse(phoneNumber, field.getCountryCallingCode().iso2Code());
            return phoneUtil.formatByPattern(number, PhoneNumberUtil.PhoneNumberFormat.E164, Collections.emptyList());
        } catch (NumberParseException ignored) {
        }

        return "";
    }

    private Node availableCountriesSelector(PhoneNumberField2 field) {
        CheckBox allCountries = new CheckBox("All");

        CheckComboBox<PhoneNumberField2.CountryCallingCode> comboBox = new CheckComboBox<>();
        comboBox.getItems().addAll(PhoneNumberField2.CountryCallingCode.Defaults.values());
        comboBox.setPrefWidth(250);
        comboBox.getCheckModel().getCheckedItems().addListener((InvalidationListener) observable -> field.getAvailableCountryCodes().setAll(comboBox.getCheckModel().getCheckedItems()));
        comboBox.getCheckModel().checkAll();

        allCountries.selectedProperty().addListener((obs, oldV, newV) -> {
            if (newV) {
                comboBox.getCheckModel().checkAll();
                comboBox.setDisable(true);
            } else {
                comboBox.getCheckModel().clearChecks();
                comboBox.setDisable(false);
            }
        });

        allCountries.setSelected(true);

        HBox box = new HBox(10);
        box.getChildren().addAll(allCountries, comboBox);

        return box;
    }

    private Node preferredCountriesSelector(PhoneNumberField2 view) {
        CheckComboBox<PhoneNumberField2.CountryCallingCode> comboBox = new CheckComboBox<>();
        comboBox.getItems().addAll(PhoneNumberField2.CountryCallingCode.Defaults.values());
        comboBox.setPrefWidth(300);
        Bindings.bindContent(view.getPreferredCountryCodes(), comboBox.getCheckModel().getCheckedItems());
        return comboBox;
    }

    private Node disableCountryCheck(PhoneNumberField2 field) {
        CheckBox check = new CheckBox();
        check.selectedProperty().bindBidirectional(field.disableCountryCodeProperty());
        return check;
    }

    private Node clearButton(PhoneNumberField2 field) {
        Button clear = new Button("Clear all");
        clear.setOnAction(evt -> field.setPhoneNumber(null));
        return clear;
    }

    private void addControl(String name, Node control, VBox controls) {
        Label label = new Label(name);
        label.setPrefWidth(150);
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(label, control);
        HBox.setHgrow(label, Priority.NEVER);
        HBox.setHgrow(control, Priority.ALWAYS);
        controls.getChildren().add(hBox);
    }

    private void addField(VBox fields, String label, ObservableValue property) {
        addField(fields, label, property, null);
    }

    private void addField(VBox fields, String label, ObservableValue property, Function<Object, String> converter) {
        Label value = new Label();
        if (converter == null) {
            value.textProperty().bind(Bindings.convert(property));
        } else {
            value.textProperty().bind(Bindings.createStringBinding(() -> converter.apply(property.getValue()), property));
        }

        Label myLabel = new Label(label + ": ");
        myLabel.setPrefWidth(150);

        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(myLabel, value);
        HBox.setHgrow(myLabel, Priority.NEVER);
        HBox.setHgrow(value, Priority.ALWAYS);

        fields.getChildren().add(hBox);
    }

    public static void main(String[] args) {
        launch();
    }

}
