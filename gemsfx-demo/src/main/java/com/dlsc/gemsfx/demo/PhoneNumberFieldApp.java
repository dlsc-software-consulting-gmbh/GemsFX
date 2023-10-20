package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CountryCallingCode;
import com.dlsc.gemsfx.PhoneNumberField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.controlsfx.control.CheckComboBox;

import java.util.function.Function;

public class PhoneNumberFieldApp extends Application {

    private static final Function<Object, String> COUNTRY_CODE_CONVERTER = c -> {
        if (c == null) {
            return null;
        }
        CountryCallingCode code = (CountryCallingCode) c;
        return "(+" + code.countryCode() + ") " + code.displayName("en");
    };

    @Override
    public void start(Stage stage) throws Exception {
        PhoneNumberField view = new PhoneNumberField();

        VBox controls = new VBox(10);
        controls.getChildren().add(defaultCountrySelector(view));
        controls.getChildren().add(preferredCountriesSelector(view));

        VBox fields = new VBox(10);
        addField(fields, "Number", view.phoneNumberProperty());
        addField(fields, "Country", view.countryCodeProperty(), COUNTRY_CODE_CONVERTER);
        addField(fields, "Local Number", view.localPhoneNumberProperty());

        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(controls, new Separator(), view, new Separator(), fields);

        Scene scene = new Scene(vBox, 500, 400);

        stage.setTitle("PhoneNumberField");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private Node defaultCountrySelector(PhoneNumberField view) {
        ComboBox<CountryCallingCode> comboBox = new ComboBox<>();
        comboBox.getItems().addAll(CountryCallingCode.defaultValues());
        comboBox.valueProperty().bindBidirectional(view.defaultCountryCodeProperty());
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(new Label("Default Country: "), comboBox);
        return hBox;
    }

    private Node preferredCountriesSelector(PhoneNumberField view) {
        CheckComboBox<CountryCallingCode> comboBox = new CheckComboBox<>();
        comboBox.getItems().addAll(CountryCallingCode.defaultValues());
        Bindings.bindContent(view.getPreferredCountryCodes(), comboBox.getCheckModel().getCheckedItems());
        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(new Label("Preferred Countries: "), comboBox);
        return hBox;
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

        HBox hBox = new HBox(10);
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.getChildren().addAll(new Label(label + ": "), value);
        fields.getChildren().add(hBox);
    }

    public static void main(String[] args) {
        launch();
    }

}
