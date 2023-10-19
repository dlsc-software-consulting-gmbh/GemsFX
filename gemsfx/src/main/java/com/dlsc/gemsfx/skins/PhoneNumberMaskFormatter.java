package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhoneNumberField;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;
import javafx.scene.control.TextFormatter;

import java.util.function.UnaryOperator;

public class PhoneNumberMaskFormatter implements UnaryOperator<TextFormatter.Change> {

    private PhoneNumberMaskFormatter(PhoneNumberField field, Label mask) {

    }

    public static TextFormatter<TextFormatter.Change> configure(PhoneNumberField field, Label mask) {
        return new TextFormatter<>(new PhoneNumberMaskFormatter(field, mask));
    }

    @Override
    public TextFormatter.Change apply(TextFormatter.Change change) {
        if (change.isAdded()) {
            String text = change.getText();
            if (!text.matches("[0-9]")) {
                return null;
            }
        }
        return change;
    }

}
