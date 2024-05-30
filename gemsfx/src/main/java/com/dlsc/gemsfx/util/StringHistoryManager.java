package com.dlsc.gemsfx.util;

import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.prefs.Preferences;

/**
 * Manages a history of string records using the Java Preferences API. This class specializes
 * the generic {@link PreferencesHistoryManager} for strings, providing simple and efficient
 * history management functionalities such as adding, removing, and clearing history items.
 * <p>
 * It is designed for use cases where history items are plain strings, making it ideal for
 * applications requiring simple, persistent storage of string data like recent user inputs
 * or configurations. This class uses the {@code Preferences} API to persist history records,
 * ensuring that they are maintained across application restarts.
 * </p>
 * <p>
 * Due to the limitations of the {@code Preferences} API, this manager is not suitable for
 * scenarios involving large-scale data or complex data structures. It is optimized for
 * lightweight history management tasks where history items are stored and retrieved
 * straightforwardly without transformation.
 * </p>
 * <p>
 * Key features include:
 * - Persisting history in a simple, local manner using {@code Preferences}
 * - Adding items while ensuring uniqueness
 * - Removing specific history items or clearing all history
 * - Providing a read-only view of history items to external components
 * - Observability of updates, suitable for UI integration
 * </p>
 */
public class StringHistoryManager extends PreferencesHistoryManager<String> {

    private static final StringConverter<String> DEFAULT_STRING_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(String object) {
            return object;
        }

        @Override
        public String fromString(String string) {
            return string;
        }
    };

    public StringHistoryManager() {
        super(DEFAULT_STRING_CONVERTER);
        setFilter(StringUtils::isNotEmpty);
    }

    public StringHistoryManager(Preferences preferences) {
        this();
        setPreferences(preferences);
    }

    public StringHistoryManager(String delimiter, String preferencesKey) {
        super(delimiter, preferencesKey, DEFAULT_STRING_CONVERTER);
        setFilter(StringUtils::isNotEmpty);
    }

    public StringHistoryManager(String delimiter, String preferencesKey, Preferences preferences) {
        this(delimiter, preferencesKey);
        setPreferences(preferences);
    }

}
