package com.dlsc.gemsfx.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StringUtilsTest {

    // -------------------------------------------------------------------------
    // isEmpty / isNotEmpty
    // -------------------------------------------------------------------------

    @Test
    public void isEmpty_null_returnsTrue() {
        assertTrue(StringUtils.isEmpty(null));
    }

    @Test
    public void isEmpty_emptyString_returnsTrue() {
        assertTrue(StringUtils.isEmpty(""));
    }

    @Test
    public void isEmpty_blank_returnsFalse() {
        assertFalse(StringUtils.isEmpty(" "));
    }

    @Test
    public void isEmpty_nonEmpty_returnsFalse() {
        assertFalse(StringUtils.isEmpty("a"));
    }

    @Test
    public void isNotEmpty_null_returnsFalse() {
        assertFalse(StringUtils.isNotEmpty(null));
    }

    @Test
    public void isNotEmpty_emptyString_returnsFalse() {
        assertFalse(StringUtils.isNotEmpty(""));
    }

    @Test
    public void isNotEmpty_nonEmpty_returnsTrue() {
        assertTrue(StringUtils.isNotEmpty("a"));
    }

    // -------------------------------------------------------------------------
    // isBlank / isNotBlank
    // -------------------------------------------------------------------------

    @Test
    public void isBlank_null_returnsTrue() {
        assertTrue(StringUtils.isBlank(null));
    }

    @Test
    public void isBlank_emptyString_returnsTrue() {
        assertTrue(StringUtils.isBlank(""));
    }

    @Test
    public void isBlank_spaces_returnsTrue() {
        assertTrue(StringUtils.isBlank("   "));
    }

    @Test
    public void isBlank_tab_returnsTrue() {
        assertTrue(StringUtils.isBlank("\t"));
    }

    @Test
    public void isBlank_newline_returnsTrue() {
        assertTrue(StringUtils.isBlank("\n"));
    }

    @Test
    public void isBlank_nonBlank_returnsFalse() {
        assertFalse(StringUtils.isBlank("a"));
    }

    @Test
    public void isBlank_surroundedBySpaces_returnsFalse() {
        assertFalse(StringUtils.isBlank("  a  "));
    }

    @Test
    public void isNotBlank_null_returnsFalse() {
        assertFalse(StringUtils.isNotBlank(null));
    }

    @Test
    public void isNotBlank_blank_returnsFalse() {
        assertFalse(StringUtils.isNotBlank("  "));
    }

    @Test
    public void isNotBlank_nonBlank_returnsTrue() {
        assertTrue(StringUtils.isNotBlank("hello"));
    }

    // -------------------------------------------------------------------------
    // capitalize
    // -------------------------------------------------------------------------

    @Test
    public void capitalize_null_returnsNull() {
        assertNull(StringUtils.capitalize(null));
    }

    @Test
    public void capitalize_empty_returnsEmpty() {
        assertEquals("", StringUtils.capitalize(""));
    }

    @Test
    public void capitalize_lowercase_capitalisesFirst() {
        assertEquals("Hello", StringUtils.capitalize("hello"));
    }

    @Test
    public void capitalize_alreadyCapitalized_unchanged() {
        assertEquals("Hello", StringUtils.capitalize("Hello"));
    }

    @Test
    public void capitalize_singleChar_capitalisesIt() {
        assertEquals("A", StringUtils.capitalize("a"));
    }

    @Test
    public void capitalize_doesNotChangeRemainder() {
        // only the first character is changed; the rest stays as-is
        assertEquals("HELLO", StringUtils.capitalize("hELLO"));
    }

    // -------------------------------------------------------------------------
    // split
    // -------------------------------------------------------------------------

    @Test
    public void split_null_returnsNull() {
        assertNull(StringUtils.split(null));
    }

    @Test
    public void split_empty_returnsEmptyArray() {
        assertArrayEquals(new String[0], StringUtils.split(""));
    }

    @Test
    public void split_blank_returnsEmptyArray() {
        assertArrayEquals(new String[0], StringUtils.split("   "));
    }

    @Test
    public void split_singleWord_returnsSingleElement() {
        assertArrayEquals(new String[]{"hello"}, StringUtils.split("hello"));
    }

    @Test
    public void split_multipleWords_returnsTokens() {
        assertArrayEquals(new String[]{"hello", "world"}, StringUtils.split("hello world"));
    }

    @Test
    public void split_extraSpaces_ignored() {
        assertArrayEquals(new String[]{"a", "b", "c"}, StringUtils.split("  a   b   c  "));
    }

    @Test
    public void split_tabSeparated_returnsTokens() {
        assertArrayEquals(new String[]{"a", "b"}, StringUtils.split("a\tb"));
    }

    // -------------------------------------------------------------------------
    // join
    // -------------------------------------------------------------------------

    @Test
    public void join_null_returnsNull() {
        assertNull(StringUtils.join(null, ","));
    }

    @Test
    public void join_emptyArray_returnsEmptyString() {
        assertEquals("", StringUtils.join(new String[0], ","));
    }

    @Test
    public void join_singleElement_returnsElement() {
        assertEquals("a", StringUtils.join(new String[]{"a"}, ","));
    }

    @Test
    public void join_multipleElements_joinedWithSeparator() {
        assertEquals("a, b, c", StringUtils.join(new String[]{"a", "b", "c"}, ", "));
    }

    @Test
    public void join_nullSeparator_treatedAsEmpty() {
        assertEquals("abc", StringUtils.join(new String[]{"a", "b", "c"}, null));
    }

    // -------------------------------------------------------------------------
    // startsWithIgnoreCase
    // -------------------------------------------------------------------------

    @Test
    public void startsWithIgnoreCase_bothNull_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase(null, null));
    }

    @Test
    public void startsWithIgnoreCase_strNull_returnsFalse() {
        assertFalse(StringUtils.startsWithIgnoreCase(null, "a"));
    }

    @Test
    public void startsWithIgnoreCase_prefixNull_returnsFalse() {
        assertFalse(StringUtils.startsWithIgnoreCase("a", null));
    }

    @Test
    public void startsWithIgnoreCase_emptyPrefix_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase("hello", ""));
    }

    @Test
    public void startsWithIgnoreCase_exactMatch_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase("hello", "hello"));
    }

    @Test
    public void startsWithIgnoreCase_prefixLongerThanStr_returnsFalse() {
        assertFalse(StringUtils.startsWithIgnoreCase("hi", "hello"));
    }

    @Test
    public void startsWithIgnoreCase_sameCase_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase("Hello", "Hel"));
    }

    @Test
    public void startsWithIgnoreCase_differentCase_returnsTrue() {
        assertTrue(StringUtils.startsWithIgnoreCase("Hello", "hel"));
    }

    @Test
    public void startsWithIgnoreCase_noMatch_returnsFalse() {
        assertFalse(StringUtils.startsWithIgnoreCase("Hello", "world"));
    }

    // -------------------------------------------------------------------------
    // equals
    // -------------------------------------------------------------------------

    @Test
    public void equals_bothNull_returnsTrue() {
        assertTrue(StringUtils.equals(null, null));
    }

    @Test
    public void equals_firstNull_returnsFalse() {
        assertFalse(StringUtils.equals(null, "a"));
    }

    @Test
    public void equals_secondNull_returnsFalse() {
        assertFalse(StringUtils.equals("a", null));
    }

    @Test
    public void equals_sameContent_returnsTrue() {
        assertTrue(StringUtils.equals("hello", "hello"));
    }

    @Test
    public void equals_sameReference_returnsTrue() {
        String s = "hello";
        assertTrue(StringUtils.equals(s, s));
    }

    @Test
    public void equals_differentContent_returnsFalse() {
        assertFalse(StringUtils.equals("hello", "world"));
    }

    @Test
    public void equals_differentCase_returnsFalse() {
        assertFalse(StringUtils.equals("hello", "Hello"));
    }

    @Test
    public void equals_emptyStrings_returnsTrue() {
        assertTrue(StringUtils.equals("", ""));
    }

    @Test
    public void equals_emptyAndNonEmpty_returnsFalse() {
        assertFalse(StringUtils.equals("", "a"));
    }

    // -------------------------------------------------------------------------
    // equalsIgnoreCase
    // -------------------------------------------------------------------------

    @Test
    public void equalsIgnoreCase_bothNull_returnsTrue() {
        assertTrue(StringUtils.equalsIgnoreCase(null, null));
    }

    @Test
    public void equalsIgnoreCase_firstNull_returnsFalse() {
        assertFalse(StringUtils.equalsIgnoreCase(null, "a"));
    }

    @Test
    public void equalsIgnoreCase_secondNull_returnsFalse() {
        assertFalse(StringUtils.equalsIgnoreCase("a", null));
    }

    @Test
    public void equalsIgnoreCase_sameCase_returnsTrue() {
        assertTrue(StringUtils.equalsIgnoreCase("hello", "hello"));
    }

    @Test
    public void equalsIgnoreCase_differentCase_returnsTrue() {
        assertTrue(StringUtils.equalsIgnoreCase("Hello", "hELLO"));
    }

    @Test
    public void equalsIgnoreCase_differentStrings_returnsFalse() {
        assertFalse(StringUtils.equalsIgnoreCase("hello", "world"));
    }

    // -------------------------------------------------------------------------
    // replaceEach
    // -------------------------------------------------------------------------

    @Test
    public void replaceEach_nullText_returnsNull() {
        assertNull(StringUtils.replaceEach(null, new String[]{"a"}, new String[]{"b"}));
    }

    @Test
    public void replaceEach_nullSearchList_returnsOriginal() {
        assertEquals("hello", StringUtils.replaceEach("hello", null, new String[]{"b"}));
    }

    @Test
    public void replaceEach_nullReplacementList_returnsOriginal() {
        assertEquals("hello", StringUtils.replaceEach("hello", new String[]{"h"}, null));
    }

    @Test
    public void replaceEach_mismatchedListLengths_returnsOriginal() {
        assertEquals("hello", StringUtils.replaceEach("hello", new String[]{"h", "e"}, new String[]{"x"}));
    }

    @Test
    public void replaceEach_singleReplacement() {
        assertEquals("xello", StringUtils.replaceEach("hello", new String[]{"h"}, new String[]{"x"}));
    }

    @Test
    public void replaceEach_multipleReplacements() {
        // each search string appears exactly once; replacements are applied in order
        assertEquals("xyz", StringUtils.replaceEach("abc", new String[]{"a", "b", "c"}, new String[]{"x", "y", "z"}));
    }

    @Test
    public void replaceEach_filterViewUsage() {
        // Mirrors the actual usage in FilterView
        String name = "Foo (Bar) & Baz";
        String result = StringUtils.replaceEach(name,
                new String[]{"(", ")", "&", "_", " "},
                new String[]{"", "", "and", "-", "-"});
        assertEquals("Foo-Bar-and-Baz", result);
    }

    // -------------------------------------------------------------------------
    // splitByCharacterTypeCamelCase
    // -------------------------------------------------------------------------

    @Test
    public void splitByCharacterTypeCamelCase_null_returnsNull() {
        assertNull(StringUtils.splitByCharacterTypeCamelCase(null));
    }

    @Test
    public void splitByCharacterTypeCamelCase_empty_returnsEmptyArray() {
        assertArrayEquals(new String[0], StringUtils.splitByCharacterTypeCamelCase(""));
    }

    @Test
    public void splitByCharacterTypeCamelCase_singleWord_returnsSingleToken() {
        assertArrayEquals(new String[]{"hello"}, StringUtils.splitByCharacterTypeCamelCase("hello"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_camelCase_splitsAtBoundary() {
        assertArrayEquals(new String[]{"foo", "Bar"}, StringUtils.splitByCharacterTypeCamelCase("fooBar"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_multipleWords() {
        assertArrayEquals(new String[]{"customer", "Service", "Order"},
                StringUtils.splitByCharacterTypeCamelCase("customerServiceOrder"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_acronymPrefix() {
        // "ASFRules" → ["ASF", "Rules"]
        assertArrayEquals(new String[]{"ASF", "Rules"},
                StringUtils.splitByCharacterTypeCamelCase("ASFRules"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_allUppercase() {
        assertArrayEquals(new String[]{"ONS"}, StringUtils.splitByCharacterTypeCamelCase("ONS"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_withDigits() {
        assertArrayEquals(new String[]{"foo", "200", "Bar"},
                StringUtils.splitByCharacterTypeCamelCase("foo200Bar"));
    }

    @Test
    public void splitByCharacterTypeCamelCase_uiUtilUsage() {
        // Mirrors the actual usage in UIUtil.camelCaseToNaturalCase
        String[] tokens = StringUtils.splitByCharacterTypeCamelCase("customerServiceOrder");
        assertEquals("Customer Service Order",
                StringUtils.capitalize(StringUtils.join(tokens, " ")));
    }

    @Test
    public void splitByCharacterTypeCamelCase_onsCode() {
        String[] tokens = StringUtils.splitByCharacterTypeCamelCase("ONSCode");
        assertEquals("ONS Code", StringUtils.join(tokens, " "));
    }
}
