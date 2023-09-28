package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhoneNumberField;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    public PhoneNumberFieldSkin(PhoneNumberField control) {
        super(control);

        PhoneNumberFieldEditor editor = new PhoneNumberFieldEditor();

        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setMouseTransparent(true);// Disable all mouse events on the combo box
        comboBox.setButtonCell(editor);
        comboBox.getItems().setAll(Arrays.stream(Locale.getISOCountries())
            .map(c -> new Locale("", c).getDisplayCountry())
            .collect(Collectors.toList()));

        // Manually handle mouse pressed over either the text field or the country selector
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            Bounds textFieldBounds = editor.textField.getBoundsInParent();
            if (textFieldBounds.contains(evt.getX(), evt.getY())) {
                editor.textField.requestFocus();
                comboBox.hide();
            }
            else {
                Bounds selectorBounds = editor.countrySelector.getBoundsInParent();
                if (selectorBounds.contains(evt.getX(), evt.getY())) {
                    editor.countrySelector.requestFocus();
                    comboBox.show();
                }
                else {
                    comboBox.hide();
                }
            }
            evt.consume();
        });

        getChildren().addAll(comboBox);
    }

    private class PhoneNumberFieldEditor extends ListCell<String> {

        private final TextField textField = new TextField();
        private final HBox countrySelector = new HBox();

        public PhoneNumberFieldEditor() {
            getStyleClass().add("editor");

            Label countryCode = new Label("(+##)");
            countryCode.getStyleClass().add("country-code");

            Region arrow = new Region();
            arrow.getStyleClass().add("arrow");

            StackPane arrowButton = new StackPane();
            arrowButton.getStyleClass().add("arrow-button");
            arrowButton.getChildren().add(arrow);

            countrySelector.getStyleClass().add("country-selector");
            countrySelector.getChildren().addAll(countryCode, arrowButton);

            textField.textProperty().bindBidirectional(PhoneNumberFieldSkin.this.getSkinnable().phoneNumberProperty());
            textField.setTextFormatter(new TextFormatter<>(change -> {
                if (change.isAdded()) {
                    String text = change.getText();
                    if (!text.matches("[0-9]")) {
                        return null;
                    }
                }
                return change;
            }));
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new PhoneNumberFieldEditorSkin(this);
        }

    }

    private class PhoneNumberFieldEditorSkin extends SkinBase<PhoneNumberFieldEditor> {

        private final Label maskLabel = new Label("(###) ###-####");

        protected PhoneNumberFieldEditorSkin(PhoneNumberFieldEditor control) {
            super(control);
            maskLabel.getStyleClass().add("text-field-mask");
            getChildren().addAll(control.countrySelector, control.textField, maskLabel);
        }

        @Override
        protected void layoutChildren(double x, double y, double w, double h) {
            final double arrowWidth = snapSizeX(getSkinnable().countrySelector.prefWidth(-1));
            getSkinnable().countrySelector.resizeRelocate(x, y, arrowWidth, h);

            final double textFieldX = snapPositionX(x + arrowWidth);
            getSkinnable().textField.resizeRelocate(textFieldX, y, w - arrowWidth, h);

            final Node textNode = getSkinnable().textField.lookup(".text");
            final double maskX = snapPositionX(textFieldX + textNode.getLayoutBounds().getWidth());
            final double maskWidth = snapSizeX(Math.max(0, Math.min(maskLabel.prefWidth(-1), w - maskX)));
            maskLabel.resizeRelocate(maskX, y, maskWidth, h);
        }

    }

}
