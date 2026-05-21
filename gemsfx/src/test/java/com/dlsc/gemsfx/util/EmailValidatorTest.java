package com.dlsc.gemsfx.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class EmailValidatorTest {

    private final EmailValidator v = EmailValidator.getInstance();

    // -------------------------------------------------------------------------
    // Singleton
    // -------------------------------------------------------------------------

    @Test
    public void getInstance_returnsSameInstance() {
        assertSame(EmailValidator.getInstance(), EmailValidator.getInstance());
    }

    // -------------------------------------------------------------------------
    // Null / empty
    // -------------------------------------------------------------------------

    @Test
    public void isValid_null_returnsFalse() {
        assertFalse(v.isValid(null));
    }

    @Test
    public void isValid_emptyString_returnsFalse() {
        assertFalse(v.isValid(""));
    }

    // -------------------------------------------------------------------------
    // Valid addresses
    // -------------------------------------------------------------------------

    @Test
    public void isValid_simple_returnsTrue() {
        assertTrue(v.isValid("user@example.com"));
    }

    @Test
    public void isValid_subdomain_returnsTrue() {
        assertTrue(v.isValid("user@mail.example.com"));
    }

    @Test
    public void isValid_deepSubdomain_returnsTrue() {
        assertTrue(v.isValid("user@a.b.c.example.com"));
    }

    @Test
    public void isValid_plusAddressing_returnsTrue() {
        assertTrue(v.isValid("user+tag@example.com"));
    }

    @Test
    public void isValid_dotsInLocalPart_returnsTrue() {
        assertTrue(v.isValid("first.last@example.com"));
    }

    @Test
    public void isValid_digitsEverywhere_returnsTrue() {
        assertTrue(v.isValid("123@456.org"));
    }

    @Test
    public void isValid_hyphenInDomainLabel_returnsTrue() {
        assertTrue(v.isValid("user@my-company.com"));
    }

    @Test
    public void isValid_longTld_returnsTrue() {
        assertTrue(v.isValid("user@example.museum"));
    }

    @Test
    public void isValid_twoCharTld_returnsTrue() {
        assertTrue(v.isValid("user@example.de"));
    }

    @Test
    public void isValid_specialCharsInLocalPart_returnsTrue() {
        // allowed special chars from RFC 5321
        assertTrue(v.isValid("user!#$%&'*+/=?^_`{|}~-name@example.com"));
    }

    @Test
    public void isValid_upperCaseLocalPart_returnsTrue() {
        assertTrue(v.isValid("User.Name@Example.COM"));
    }

    @Test
    public void isValid_exactly254Chars_returnsTrue() {
        // local(64) + '@' + label(185) + '.' + tld(2)  = 254
        String local = "a".repeat(64);
        String domain = "b".repeat(185) + ".co";
        assertTrue(v.isValid(local + "@" + domain));
    }

    // -------------------------------------------------------------------------
    // Missing structural elements
    // -------------------------------------------------------------------------

    @Test
    public void isValid_missingAtSign_returnsFalse() {
        assertFalse(v.isValid("userexample.com"));
    }

    @Test
    public void isValid_missingLocalPart_returnsFalse() {
        assertFalse(v.isValid("@example.com"));
    }

    @Test
    public void isValid_missingDomain_returnsFalse() {
        assertFalse(v.isValid("user@"));
    }

    @Test
    public void isValid_missingTld_returnsFalse() {
        assertFalse(v.isValid("user@example"));
    }

    @Test
    public void isValid_doubleAtSign_returnsFalse() {
        assertFalse(v.isValid("user@@example.com"));
    }

    // -------------------------------------------------------------------------
    // TLD constraints
    // -------------------------------------------------------------------------

    @Test
    public void isValid_singleCharTld_returnsFalse() {
        assertFalse(v.isValid("user@example.c"));
    }

    @Test
    public void isValid_numericTld_returnsFalse() {
        assertFalse(v.isValid("user@example.123"));
    }

    @Test
    public void isValid_hyphenInTld_returnsFalse() {
        assertFalse(v.isValid("user@example.co-m"));
    }

    // -------------------------------------------------------------------------
    // Local-part dot rules
    // -------------------------------------------------------------------------

    @Test
    public void isValid_leadingDotInLocalPart_returnsFalse() {
        assertFalse(v.isValid(".user@example.com"));
    }

    @Test
    public void isValid_trailingDotInLocalPart_returnsFalse() {
        assertFalse(v.isValid("user.@example.com"));
    }

    @Test
    public void isValid_consecutiveDotsInLocalPart_returnsFalse() {
        assertFalse(v.isValid("user..name@example.com"));
    }

    // -------------------------------------------------------------------------
    // Domain label hyphen rules
    // -------------------------------------------------------------------------

    @Test
    public void isValid_leadingHyphenInDomainLabel_returnsFalse() {
        assertFalse(v.isValid("user@-example.com"));
    }

    @Test
    public void isValid_trailingHyphenInDomainLabel_returnsFalse() {
        assertFalse(v.isValid("user@example-.com"));
    }

    // -------------------------------------------------------------------------
    // Whitespace
    // -------------------------------------------------------------------------

    @Test
    public void isValid_spaceInLocalPart_returnsFalse() {
        assertFalse(v.isValid("user name@example.com"));
    }

    @Test
    public void isValid_spaceInDomain_returnsFalse() {
        assertFalse(v.isValid("user@exam ple.com"));
    }

    @Test
    public void isValid_surroundingSpaces_returnsFalse() {
        assertFalse(v.isValid(" user@example.com "));
    }

    // -------------------------------------------------------------------------
    // Length limit (RFC 5321 §4.5.3 — max 254 chars)
    // -------------------------------------------------------------------------

    @Test
    public void isValid_255Chars_returnsFalse() {
        // local(64) + '@' + label(187) + '.' + tld(2) = 255
        String local = "a".repeat(64);
        String domain = "b".repeat(187) + ".co";
        assertFalse(v.isValid(local + "@" + domain));
    }
}
