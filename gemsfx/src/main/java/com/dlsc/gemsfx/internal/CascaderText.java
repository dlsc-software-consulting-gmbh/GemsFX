package com.dlsc.gemsfx.internal;

import javafx.util.StringConverter;

/**
 * Internal helper that resolves a cascader item value to its display text. This
 * is the single source of the value-to-text fallback contract shared by the
 * built-in cell and the input field's default path text.
 *
 * <p>The {@code internal} package is not exported by the module, so this class
 * is reachable across the control's own packages but not part of the public API.
 */
public final class CascaderText {

    private CascaderText() {
    }

    /**
     * Resolves the display text for a value using the given converter (only its
     * {@code toString} is consulted), falling back to {@code String.valueOf(value)}
     * when the converter is {@code null}. A {@code null} value, or a converter that
     * returns {@code null}, yields the empty string.
     *
     * @param converter item text converter, or {@code null}
     * @param value     value to render
     * @param <T>       application value type
     * @return display text, never {@code null}
     */
    public static <T> String resolve(StringConverter<T> converter, T value) {
        if (value == null) {
            return "";
        }
        if (converter == null) {
            return String.valueOf(value);
        }
        String text = converter.toString(value);
        return text == null ? "" : text;
    }
}
