package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CountryCallingCode;
import com.dlsc.gemsfx.PhoneNumberField;
import javafx.geometry.Bounds;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;

public class PhoneNumberFieldSkin extends SkinBase<PhoneNumberField> {

    public PhoneNumberFieldSkin(PhoneNumberField control) {
        super(control);

        PhoneNumberFieldEditor editor = new PhoneNumberFieldEditor(control);

        ComboBox<CountryCallingCode> comboBox = new ComboBox<>();
        comboBox.setButtonCell(editor);
        comboBox.setCellFactory(lv -> new CountryCallingCodeCell());
        comboBox.setItems(control.getAvailableCountryCodes());
        comboBox.valueProperty().bindBidirectional(control.selectedCountryCodeProperty());

        // Disable all mouse events on the combo box itself
        comboBox.setMouseTransparent(true);

        // Manually handle mouse pressed over either the text field or the trigger button box
        control.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
            Bounds buttonBounds = editor.getButtonBox().getBoundsInParent();
            if (buttonBounds.contains(evt.getX(), evt.getY())) {
                editor.getButtonBox().requestFocus();
                if (!comboBox.isShowing()) {
                    comboBox.show();
                }
            }
            else {
                comboBox.hide();
                Bounds textFieldBounds = editor.getTextField().getBoundsInParent();
                if (textFieldBounds.contains(evt.getX(), evt.getY())) {
                    editor.getTextField().requestFocus();
                }
            }
            evt.consume();
        });

        getChildren().addAll(comboBox);
    }

}
