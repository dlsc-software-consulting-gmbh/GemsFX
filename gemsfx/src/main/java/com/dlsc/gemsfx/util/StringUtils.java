package com.dlsc.gemsfx.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal string utility methods that cover the subset of
 * {@code org.apache.commons.lang3.StringUtils} used by GemsFX.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Returns {@code true} if {@code str} is {@code null} or its length is 0.
     *
     * @param str the string to test
     * @return {@code true} if the string is empty
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Returns {@code true} if {@code str} is neither {@code null} nor empty.
     *
     * @param str the string to test
     * @return {@code true} if the string is not empty
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Returns {@code true} if {@code str} is {@code null}, empty, or contains
     * only whitespace characters (as defined by {@link Character#isWhitespace}).
     *
     * @param str the string to test
     * @return {@code true} if the string is blank
     */
    public static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} if {@code str} is not blank (see {@link #isBlank}).
     *
     * @param str the string to test
     * @return {@code true} if the string is not blank
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Capitalises the first character of {@code str} using
     * {@link Character#toTitleCase}. Returns {@code str} unchanged when it is
     * {@code null} or empty.
     *
     * @param str the string to capitalize
     * @return the capitalized string
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        char first = str.charAt(0);
        char titleCase = Character.toTitleCase(first);
        if (first == titleCase) {
            return str;
        }
        return titleCase + str.substring(1);
    }

    /**
     * Splits {@code str} on whitespace, discarding empty tokens.
     * Returns {@code null} when {@code str} is {@code null} and an empty
     * array when {@code str} is empty or all-whitespace.
     *
     * @param str the string to split
     * @return the split parts
     */
    public static String[] split(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        return trimmed.split("\\s+");
    }

    /**
     * Joins the elements of {@code array} with {@code separator} between each pair.
     * A {@code null} separator is treated as an empty string.
     * Returns {@code null} when {@code array} is {@code null}.
     *
     * @param array the strings to join
     * @param separator the separator to use
     * @return the joined string
     */
    public static String join(String[] array, String separator) {
        if (array == null) {
            return null;
        }
        return String.join(separator == null ? "" : separator, array);
    }

    /**
     * Returns {@code true} if {@code str} starts with {@code prefix},
     * ignoring case. Null-safe: two {@code null} values are considered equal,
     * otherwise a {@code null} argument yields {@code false}.
     *
     * @param str the string to test
     * @param prefix the prefix to look for
     * @return {@code true} if the string starts with the prefix
     */
    public static boolean startsWithIgnoreCase(String str, String prefix) {
        if (str == null || prefix == null) {
            return str == null && prefix == null;
        }
        if (prefix.length() > str.length()) {
            return false;
        }
        return str.regionMatches(true, 0, prefix, 0, prefix.length());
    }

    /**
     * Returns {@code true} if the two strings are equal.
     * Two {@code null} values are considered equal.
     *
     * @param str1 the first string
     * @param str2 the second string
     * @return {@code true} if both strings are equal
     */
    public static boolean equals(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    /**
     * Returns {@code true} if the two strings are equal ignoring case.
     * Two {@code null} values are considered equal.
     *
     * @param str1 the first string
     * @param str2 the second string
     * @return {@code true} if both strings are equal ignoring case
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * Replaces each string in {@code searchList} with the corresponding entry
     * in {@code replacementList} within {@code text}. Replacements are applied
     * in order; each operates on the result of the previous one.
     * Returns {@code text} unchanged when any argument is {@code null} or
     * the two lists have different lengths.
     *
     * @param text the text to process
     * @param searchList the strings to search for
     * @param replacementList the replacement strings
     * @return the processed string
     */
    public static String replaceEach(String text, String[] searchList, String[] replacementList) {
        if (text == null || searchList == null || replacementList == null
                || searchList.length != replacementList.length) {
            return text;
        }
        String result = text;
        for (int i = 0; i < searchList.length; i++) {
            result = result.replace(searchList[i], replacementList[i]);
        }
        return result;
    }

    /**
     * Splits {@code str} at boundaries where the character type (as returned by
     * {@link Character#getType}) changes, with the additional camel-case rule:
     * when transitioning from {@link Character#UPPERCASE_LETTER} to
     * {@link Character#LOWERCASE_LETTER}, the split is placed <em>before</em>
     * the last uppercase character so that acronym prefixes are kept together
     * with the following word (e.g. {@code "ASFRules"} → {@code ["ASF", "Rules"]}).
     *
     * <p>Returns {@code null} when {@code str} is {@code null} and an empty array
     * when {@code str} is empty.
     *
     * @param str the string to split
     * @return the split parts
     */
    public static String[] splitByCharacterTypeCamelCase(String str) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return new String[0];
        }
        char[] chars = str.toCharArray();
        List<String> tokens = new ArrayList<>();
        int tokenStart = 0;
        int currentType = Character.getType(chars[tokenStart]);
        for (int pos = tokenStart + 1; pos < chars.length; pos++) {
            int type = Character.getType(chars[pos]);
            if (type == currentType) {
                continue;
            }
            if (type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
                int newTokenStart = pos - 1;
                if (newTokenStart != tokenStart) {
                    tokens.add(new String(chars, tokenStart, newTokenStart - tokenStart));
                    tokenStart = newTokenStart;
                }
            } else {
                tokens.add(new String(chars, tokenStart, pos - tokenStart));
                tokenStart = pos;
            }
            currentType = type;
        }
        tokens.add(new String(chars, tokenStart, chars.length - tokenStart));
        return tokens.toArray(new String[0]);
    }
}
