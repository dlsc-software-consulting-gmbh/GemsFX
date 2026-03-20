package com.dlsc.gemsfx.util;

import javafx.css.ParsedValue;
import javafx.css.StyleConverter;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 * A CSS {@link StyleConverter} that converts a numeric millisecond value to a
 * {@link Duration}.
 *
 * <p>Usage in CSS:</p>
 * <pre>
 *   -fx-animation-duration: 300;   /* 300 milliseconds *&#47;
 * </pre>
 *
 * <p>In Java:</p>
 * <pre>
 *   new CssMetaData&lt;&gt;("-fx-animation-duration", DurationConverter.getInstance(), Duration.millis(200)) { … }
 * </pre>
 */
public class DurationConverter extends StyleConverter<Number, Duration> {

    private static class Holder {
        static final DurationConverter INSTANCE = new DurationConverter();
    }

    /**
     * Returns the singleton instance of this converter.
     *
     * @return the singleton {@code DurationConverter}
     */
    public static DurationConverter getInstance() {
        return Holder.INSTANCE;
    }

    private DurationConverter() {
    }

    @Override
    public Duration convert(ParsedValue<Number, Duration> value, Font font) {
        Number number = value.getValue();
        if (number == null) {
            return Duration.ZERO;
        }
        return Duration.millis(number.doubleValue());
    }

    @Override
    public String toString() {
        return "DurationConverter";
    }
}
