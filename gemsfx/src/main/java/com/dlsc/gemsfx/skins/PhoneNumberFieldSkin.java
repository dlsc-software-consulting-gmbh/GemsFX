package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhoneNumberField;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    private final ComboBox<String> comboBox = new ComboBox<>();

    public PhoneNumberFieldSkin(PhoneNumberField control) {
        super(control);

        comboBox.setButtonCell(new Editor());
        comboBox.getItems()
            .setAll(Arrays.stream(Locale.getISOCountries())
            .map(c -> new Locale("", c).getDisplayCountry())
            .collect(Collectors.toList()));

        getChildren().addAll(comboBox);
    }

    private class Editor extends ListCell<String> {

        public Editor() {
            getStyleClass().add("editor");
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new EditorSkin(this);
        }

        @Override
        public void requestFocus() {
            super.requestFocus();
        }
    }

    private class EditorSkin extends SkinBase<Editor> {

        private final HBox countrySelector = new HBox();
        private final TextField phoneNumberTextField = new TextField();
        private final Label maskLabel = new Label("### ### ####");

        protected EditorSkin(Editor control) {
            super(control);

            Label countryCode = new Label("(+##)");
            countryCode.getStyleClass().add("country-code");

            Region arrow = new Region();
            arrow.getStyleClass().add("arrow");

            StackPane arrowButton = new StackPane();
            arrowButton.getStyleClass().add("arrow-button");
            arrowButton.getChildren().add(arrow);

            countrySelector.getChildren().addAll(countryCode, arrowButton);
            countrySelector.getStyleClass().add("country-selector");
            countrySelector.setOnMouseClicked(evt -> comboBox.show());

            maskLabel.getStyleClass().add("mask");

            getChildren().addAll(countrySelector, phoneNumberTextField, maskLabel);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            final double arrowWidth = snapSizeX(countrySelector.prefWidth(-1));
            countrySelector.resizeRelocate(x, y, arrowWidth, h);

            final double textFieldX = snapPositionX(x + arrowWidth);
            phoneNumberTextField.resizeRelocate(textFieldX, y, w - arrowWidth, h);

            final Node textNode = phoneNumberTextField.lookup(".text");
            final double maskX = snapPositionX(textFieldX + textNode.getLayoutBounds().getWidth());
            final double maskWidth = snapSizeX(Math.max(0, Math.min(maskLabel.prefWidth(-1), w - maskX)));
            maskLabel.resizeRelocate(maskX, y, maskWidth, h);
        }

    }

}
