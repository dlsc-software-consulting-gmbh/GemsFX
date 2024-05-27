package com.dlsc.gemsfx.util;

import javafx.util.Callback;

/**
 * A specialized StringConverter implementation for Enum types.
 * This converter provides a default mechanism to format Enum values
 * in a title case, replacing underscores with spaces and capitalizing
 * the first letter of each word. If the enum value is null, it returns an empty string.
 *
 * <p>
 * This class extends SimpleStringConverter and leverages EnumUtil to provide
 * a default formatting for enum values. It can also accept custom callbacks for
 * more specific conversion requirements.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <p>Default usage with title case formatting:</p>
 * <pre>{@code
 * ComboBox<MyEnum> comboBox = new ComboBox<>();
 * comboBox.setConverter(new EnumStringConverter<>());
 * }</pre>
 *
 * <p>Custom callback for specific formatting:</p>
 * <pre>{@code
 * ComboBox<MyEnum> comboBox = new ComboBox<>();
 * comboBox.setConverter(new EnumStringConverter<>(myEnum -> "Custom format: " + myEnum.name()));
 * }</pre>
 *
 * @param <T> the type of the Enum to be converted.
 */
public class EnumStringConverter<T extends Enum<T>> extends SimpleStringConverter<T> {

    /**
     * Converts an enum value to title case, replacing underscores with spaces and
     * capitalizing the first letter of each word. If the enum value is null, returns an empty string.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> My Enum Value
     */
    public EnumStringConverter() {
        this(EnumUtil::formatEnumNameAsTitleCase);
    }

    /**
     * Constructor that accepts a custom callback for converting enum values to strings.
     * The provided callback should handle conversion from enum value to String, including any necessary null handling.
     *
     * @param valueToStringCallback The callback to convert enum value to a String.
     */
    public EnumStringConverter(Callback<T, String> valueToStringCallback) {
        super(valueToStringCallback);
    }

    /**
     * Constructor that accepts a custom callback for converting non-null enum values to strings and a default string
     * to return if the enum value is null.
     *
     * @param nonNullValueCallback The callback to convert non-null enum value to a String.
     * @param nullDefaultValue The default String value to return if the enum value is null.
     */
    public EnumStringConverter(Callback<T, String> nonNullValueCallback, String nullDefaultValue) {
        super(nonNullValueCallback, nullDefaultValue);
    }

}