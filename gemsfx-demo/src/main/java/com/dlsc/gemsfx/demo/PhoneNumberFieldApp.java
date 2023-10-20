package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.CountryCallingCode;
import com.dlsc.gemsfx.PhoneNumberField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Function;

public class PhoneNumberFieldApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        PhoneNumberField view = new PhoneNumberField();
        view.setDefaultCountryCode(CountryCallingCode.Defaults.GERMANY);
        view.getPreferredCountryCodes().add(CountryCallingCode.Defaults.COLOMBIA);
        view.getPreferredCountryCodes().add(CountryCallingCode.Defaults.SWITZERLAND);

        VBox controls = new VBox(10);
        controls.setPadding(new Insets(20));

        VBox fields = new VBox(10);
        addField(fields, "Value", view.phoneNumberProperty());
        addField(fields, "Country Code", view.countryCodeProperty(), code -> {
            if (code == null) {
                return null;
            }
            return "(+" + ((CountryCallingCode) code).countryCode() + ") " + ((CountryCallingCode) code).displayName("en");
        });
        addField(fields, "Local Number", view.localPhoneNumberProperty());

        VBox vBox = new VBox(20);
        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);
        vBox.getChildren().addAll(controls, new Separator(), view, new Separator(), fields);

        Scene scene = new Scene(vBox, 500, 500);

        stage.setTitle("PhoneNumberField");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
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
