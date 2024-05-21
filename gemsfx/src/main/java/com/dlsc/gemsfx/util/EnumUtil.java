package com.dlsc.gemsfx.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for working with enums.
 * <p> formatEnumNameAsCapitalized: MY_ENUM_VALUE -> My enum value
 * <p> formatEnumNameAsTitleCase: MY_ENUM_VALUE -> My Enum Value
 * <p> formatEnumNameAsSpacedWords: MY_ENUM_VALUE -> MY ENUM VALUE
 * <p> convertToStyleClassName: MY_ENUM_VALUE -> my-enum-value
 */
public class EnumUtil {

    private EnumUtil() {
    }

    /**
     * Converts a string representation of an enum name to a capitalized format,
     * replacing underscores with spaces and making the rest lowercase.
     * If the input string is null, returns an empty string.
     *
     * @param enumValue The enum value to convert.
     * @return A capitalized string representation of the enum name.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> My enum value
     */
    public static <T extends Enum<T>> String formatEnumNameAsCapitalized(T enumValue) {
        return formatEnumNameAsCapitalized(enumValue, "");
    }

    /**
     * Converts a string representation of an enum name to a capitalized format,
     * replacing underscores with spaces and making the rest lowercase.
     * If the input string is null, returns the specified default value.
     *
     * @param enumValue The enum value to convert.
     * @param nullDefaultValue The default value to return if the input is null.
     * @return A capitalized string representation of the enum name.
     * <p> Example: 1. null -> nullDefaultValue
     * <p> Example: 2. Example: MY_ENUM_VALUE -> My enum value
     */
    public static <T extends Enum<T>> String formatEnumNameAsCapitalized(T enumValue, String nullDefaultValue) {
        return enumValue == null ? nullDefaultValue : formatEnumNameAsCapitalized(enumValue.name());
    }

    /**
     * Converts a string representation of an enum name to a capitalized format,
     * replacing underscores with spaces and making the rest lowercase.
     * If the input string is null, returns an empty string.
     *
     * @param enumName The enum name to convert.
     * @return A capitalized string representation of the enum name.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. Example: MY_ENUM_VALUE -> My enum value
     */
    public static <T extends Enum<T>> String formatEnumNameAsCapitalized(String enumName) {
        return enumName == null ? "" : StringUtils.capitalize(enumName.replace("_", " ").toLowerCase());
    }

    /**
     * Converts an enum value to title case, replacing underscores with spaces and
     * capitalizing the first letter of each word. If the enum value is null, returns an empty string.
     *
     * @param enumValue The enum value to be formatted.
     * @return A title-cased string representation of the enum value.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> My Enum Value
     */
    public static <T extends Enum<T>> String formatEnumNameAsTitleCase(T enumValue) {
        return formatEnumNameAsTitleCase(enumValue, "");
    }

    /**
     * Converts an enum value to title case, replacing underscores with spaces and
     * capitalizing the first letter of each word. If the enum value is null, returns the specified default value.
     *
     * @param enumValue The enum value to be formatted.
     * @param nullDefaultValue The default value to return if the input is null.
     * @return A title-cased string representation of the enum value.
     * <p> Example: 1. null -> nullDefaultValue
     * <p> Example: 2. MY_ENUM_VALUE -> My Enum Value
     */
    public static <T extends Enum<T>> String formatEnumNameAsTitleCase(T enumValue, String nullDefaultValue) {
        return formatEnumNameAsTitleCase(enumValue == null ? null : enumValue.name(), nullDefaultValue);
    }

    /**
     * Converts an enum value to title case, replacing underscores with spaces and
     * capitalizing the first letter of each word. If the enum value is null, returns an empty string.
     *
     * @param enumName The string representation of the enum value to be formatted.
     * @return A title-cased string representation of the enum value.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> My Enum Value
     */
    public static <T extends Enum<T>> String formatEnumNameAsTitleCase(String enumName) {
        return formatEnumNameAsTitleCase(enumName, "");
    }

    /**
     * Converts an enum value to title case, replacing underscores with spaces and
     * capitalizing the first letter of each word. If the enum value is null, returns the specified default value.
     *
     * @param enumName The string representation of the enum value to be formatted.
     * @param nullDefaultValue The default value to return if the input is null.
     * @return A title-cased string representation of the enum value.
     * <p> Example: 1. null -> nullDefaultValue
     * <p> Example: 2. MY_ENUM_VALUE -> My Enum Value
     */
    public static <T extends Enum<T>> String formatEnumNameAsTitleCase(String enumName, String nullDefaultValue) {
        if (enumName == null) {
            return nullDefaultValue;
        }

        // Replace underscores with spaces and convert to lower case
        String lowerCased = enumName.replace("_", " ").toLowerCase();

        // Split the string into words
        String[] words = StringUtils.split(lowerCased);

        // Use StringBuilder to build the final string
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!result.isEmpty()) {
                result.append(" ");
            }
            // Capitalize the first letter of each word and append to result
            result.append(StringUtils.capitalize(word));
        }

        return result.toString();
    }

    /**
     * Replaces underscores in the string representation of an enum name with spaces.
     * Does not change letter case. If the input string is null, returns an empty string.
     *
     * @param enumValue The enum value to be formatted.
     * @return A string representation of the enum name with underscores replaced by spaces.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> MY ENUM VALUE
     */
    public static <T extends Enum<T>> String formatEnumNameAsSpacedWords(T enumValue) {
        return enumValue == null ? "" : enumValue.name().replace("_", " ");
    }

    /**
     * Replaces underscores in the string representation of an enum name with spaces.
     * Does not change letter case. If the input string is null, returns an empty string.
     *
     * @param enumName The string representation of the enum name to be formatted.
     * @return A string representation of the enum name with underscores replaced by spaces.
     * <p> Example: 1. null -> ""
     * <p> Example: 2. MY_ENUM_VALUE -> MY ENUM VALUE
     */
    public static <T extends Enum<T>> String formatEnumNameAsSpacedWords(String enumName) {
        return enumName == null ? "" : enumName.replace("_", " ");
    }

    /**
     * Converts the enum name to a lower case string, replacing underscores with hyphens,
     * suitable for use as a CSS class name. This method does not accept null values for enumValue.
     *
     * @param enumValue The enum value to be converted. Must not be null.
     * @return A string suitable for use as a CSS class name.
     * Example: MY_ENUM_VALUE -> my-enum-value
     */
    public static <T extends Enum<T>> String convertToStyleClassName(T enumValue) {
        return enumValue.name().toLowerCase().replace("_", "-");
    }

    public static <T extends Enum<T>> String[] convertAllToStylesClassName(Class<T> enumClass) {
        T[] enumConstants = enumClass.getEnumConstants();
        return convertAllToStylesClassName(enumConstants);
    }

    public static <T extends Enum<T>> String[] convertAllToStylesClassName(T[] enumValues) {
        String[] styles = new String[enumValues.length];
        for (int i = 0; i < enumValues.length; i++) {
            styles[i] = convertToStyleClassName(enumValues[i]);
        }
        return styles;
    }

}
