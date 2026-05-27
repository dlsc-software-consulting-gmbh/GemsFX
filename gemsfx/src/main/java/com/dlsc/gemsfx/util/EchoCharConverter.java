package com.dlsc.gemsfx.util;

import com.dlsc.gemsfx.EnhancedPasswordField;
import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;

/**
 * A CSS {@link javafx.css.StyleConverter} that converts a single-character
 * {@link String} CSS value into a {@link Character}.
 *
 * <p>This converter is used by {@link com.dlsc.gemsfx.EnhancedPasswordField} to
 * support the {@code -fx-echo-char} CSS property. The first character of the
 * string is returned; if the value is {@code null} or empty,
 * {@link com.dlsc.gemsfx.EnhancedPasswordField#DEFAULT_ECHO_CHAR} is used instead.
 *
 * <p>Instances are obtained via the singleton accessor {@link #getInstance()}.
 */
public final class EchoCharConverter extends StyleConverter<String, Character> {

    private static class Holder {
        static final EchoCharConverter INSTANCE = new EchoCharConverter();
    }

    /**
     * Returns the shared converter instance.
     *
     * @return the shared converter instance
     */
    public static EchoCharConverter getInstance() {
        return Holder.INSTANCE;
    }

    private EchoCharConverter() {
        super();
    }

    /**
     * Converts the parsed CSS value into an echo character.
     *
     * @param value the parsed value
     * @param font the font in use
     * @return the converted echo character
     */
    @Override
    public Character convert(ParsedValue<String, Character> value, Font font) {
        String str = value.getValue();
        if (str == null || str.isEmpty()) {
            return EnhancedPasswordField.DEFAULT_ECHO_CHAR;
        }
        return str.charAt(0);
    }

    /**
     * Returns the name of this converter.
     *
     * @return the converter name
     */
    @Override
    public String toString() {
        return "EchoCharConverter";
    }

}

