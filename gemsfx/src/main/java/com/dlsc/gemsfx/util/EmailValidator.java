package com.dlsc.gemsfx.util;

import java.util.regex.Pattern;

/**
 * Validates whether a string is a well-formed email address.
 *
 * <p>The check covers the common subset of RFC 5321 / 5322:
 * {@code local-part@domain}, where:
 * <ul>
 *   <li>The <em>local part</em> may contain letters, digits, and the characters
 *       {@code !#$%&'*+/=?^_`{|}~-}, with internal dots allowed between
 *       non-dot segments (dots are not permitted at the start or end, nor
 *       consecutively).</li>
 *   <li>The <em>domain</em> consists of dot-separated labels. Each label may
 *       contain letters, digits, and hyphens, but must start and end with a
 *       letter or digit.</li>
 *   <li>The <em>top-level domain</em> must consist of at least two alphabetic
 *       characters.</li>
 *   <li>The total address length must not exceed 254 characters (RFC 5321 §4.5.3).</li>
 * </ul>
 *
 * <p>This class is intentionally minimal. More complex formats (IP-address
 * literals, quoted local parts, …) are not supported.
 *
 * <p>Use {@link #getInstance()} to obtain the shared, stateless singleton.
 */
public final class EmailValidator {

    // local-part: one or more allowed chars, optionally (dot + allowed chars)*
    private static final String LOCAL =
            "[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+" +
            "(?:\\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*";

    // domain label: starts and ends with alnum, may contain hyphens inside
    private static final String LABEL =
            "[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?";

    // domain: one or more dot-separated labels, followed by a TLD of >= 2 alpha chars
    private static final String DOMAIN =
            LABEL + "(?:\\." + LABEL + ")*\\.[a-zA-Z]{2,}";

    private static final Pattern PATTERN =
            Pattern.compile("^" + LOCAL + "@" + DOMAIN + "$");

    private static final EmailValidator INSTANCE = new EmailValidator();

    private EmailValidator() {
    }

    /** Returns the shared singleton instance. */
    public static EmailValidator getInstance() {
        return INSTANCE;
    }

    /**
     * Returns {@code true} if {@code email} is a well-formed address according
     * to the rules described in the class Javadoc.
     *
     * @param email the address to check; {@code null} always yields {@code false}
     * @return {@code true} if the address is valid
     */
    public boolean isValid(String email) {
        if (email == null || email.isEmpty() || email.length() > 254) {
            return false;
        }
        return PATTERN.matcher(email).matches();
    }
}
