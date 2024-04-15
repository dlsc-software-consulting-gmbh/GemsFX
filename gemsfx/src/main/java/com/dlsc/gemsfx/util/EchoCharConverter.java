package com.dlsc.gemsfx.util;

import com.dlsc.gemsfx.EnhancedPasswordField;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

public final class EchoCharConverter extends StyleConverter<String, Character> {

    private static class Holder {
        static final EchoCharConverter INSTANCE = new EchoCharConverter();
    }

    public static EchoCharConverter getInstance() {
        return Holder.INSTANCE;
    }

    private EchoCharConverter() {
        super();
    }

    @Override
    public Character convert(ParsedValue<String, Character> value, Font font) {
        String str = value.getValue();
        if (str == null || str.isEmpty()) {
            return EnhancedPasswordField.DEFAULT_ECHO_CHAR;
        }
        return str.charAt(0);
    }

    @Override
    public String toString() {
        return "EchoCharConverter";
    }

}

