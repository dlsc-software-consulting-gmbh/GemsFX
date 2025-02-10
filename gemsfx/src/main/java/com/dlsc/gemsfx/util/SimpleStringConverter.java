package com.dlsc.gemsfx.util;

import javafx.util.Callback;
import javafx.util.StringConverter;

import java.util.Optional;

/**
 * A generic StringConverter implementation primarily used for displaying objects in the UI.
 * This class provides flexible mechanisms for converting objects to Strings using custom callbacks.
 * It also supports handling null values by returning a default string for null values.
 *
 * <p>
 * This converter is typically used to format objects as strings for display purposes, with optional
 * handling for null values. The conversion from string back to object is not usually required,
 * hence the {@link #fromString(String)} method returns null.
 * </p>
 *
 * <p><strong>Usage Example:</strong></p>
 * <p>Before using SimpleStringConverter:</p>
 * <pre>{@code
 * comboBox.setConverter(new StringConverter<>() {
 *     @Override
 *     public String toString(Status status) {
 *         if (status != null) {
 *             return status.getDescription();
 *         }
 *         return "";
 *     }
 *
 *     @Override
 *     public Status fromString(String string) {
 *         return null;
 *     }
 * });
 * }</pre>
 *
 * <p>After using SimpleStringConverter:</p>
 * <pre>{@code
 * comboBox.setConverter(new SimpleStringConverter<>(Status::getDescription, ""));
 * }</pre>
 *
 * @param <T> the type of the object to be converted.
 */
public class SimpleStringConverter<T> extends StringConverter<T> {

    private final Callback<T, String> valueToStringCallback;

    /**
     * Creates a new {@code SimpleStringConverter} with a default callback:
     * <ul>
     *   <li>If the object is non-null, its {@code toString()} method is used.</li>
     *   <li>If the object is null, an empty string ("") is returned.</li>
     * </ul>
     */
    public SimpleStringConverter() {
        this(Object::toString, "");
    }

    /**
     * Creates a new {@code SimpleStringConverter} that uses the given callback to convert non-null
     * values to strings. This internally leverages the two-argument constructor with an empty string
     * ("") as the default for null values.
     *
     * <ul>
     *   <li>If the object is non-null, {@code valueToStringCallback} is invoked to produce the string.</li>
     *   <li>If the object is null, an empty string ("") is returned directly (the callback is not called).</li>
     * </ul>
     *
     * @param valueToStringCallback the callback to convert a non-null value to a String
     */
    public SimpleStringConverter(Callback<T, String> valueToStringCallback) {
        this(valueToStringCallback, "");
    }

    /**
     * Constructor that automatically handles null values by returning a default null value string.
     * <ul>
     *   <li>If the object is non-null, the provided callback is used to convert it.</li>
     *   <li>If the object is null, the specified {@code nullDefaultValue} is returned.</li>
     * </ul>
     *
     * @param nonNullValueCallback The callback to convert a non-null value to a String.
     * @param nullDefaultValue     The default String value to return if the value is null.
     */
    public SimpleStringConverter(Callback<T, String> nonNullValueCallback, String nullDefaultValue) {
        this.valueToStringCallback = value -> Optional.ofNullable(value)
                .map(nonNullValueCallback::call)
                .orElse(nullDefaultValue);
    }

    @Override
    public String toString(T object) {
        if (this.valueToStringCallback != null) {
            return this.valueToStringCallback.call(object);
        }
        return "";
    }

    /**
     * This method is not implemented and always returns null.
     *
     * <p>
     * Since the primary use of this converter is to display objects as strings in the UI,
     * converting strings back to objects is not required. Therefore, this method simply returns null.
     * </p>
     *
     * @param s the string to be converted to an object.
     * @return always returns null.
     */
    @Override
    public T fromString(String s) {
        return null;
    }
}
