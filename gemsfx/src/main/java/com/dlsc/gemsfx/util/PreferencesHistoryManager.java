package com.dlsc.gemsfx.util;

import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * Manages a history of items of type T, storing them in a Java Preferences backend.
 * This class supports generic types T, allowing for flexible usage with any object type that
 * can be converted to and from a string representation using a provided {@link StringConverter}.
 *
 * <p>The history is maintained in a list that is limited by a specified maximum size. If the limit is exceeded,
 * the oldest items are removed. The history management includes functionalities to add, remove, clear, and retrieve
 * history items. Changes to the history are automatically persisted to the Preferences store using the specified
 * delimiter and preferences key.</p>
 *
 * <p>This class is particularly useful for applications needing to maintain a persistent, manageable history
 * of user actions or data entries, where entries must be stored across sessions in a simple and effective manner.</p>
 *
 * <p>Instances of this class are not thread-safe. If concurrent access is required, it should be managed externally.</p>
 *
 * @param <T> the type of objects managed in the history
 * @see HistoryManager
 * @see StringConverter
 */
public class PreferencesHistoryManager<T> extends HistoryManager<T> {

    private static final Logger LOG = Logger.getLogger(PreferencesHistoryManager.class.getName());

    /**
     * Using Unicode Record Separator as delimiter.
     * This character is not likely to be used in the history items.
     */
    public static String DELIMITER = "␞";

    private final Preferences preferences;

    private final String key;

    private final StringConverter<T> converter;

    public PreferencesHistoryManager(Preferences preferences, String key, StringConverter<T> converter) {
        this.preferences = Objects.requireNonNull(preferences);
        this.key = Objects.requireNonNull(key);
        this.converter = Objects.requireNonNull(converter);
        loadHistory();
    }

    /**
     * Stores the history items in the preferences.
     */
    @Override
    protected void storeHistory() {
        String result = getAllUnmodifiable().stream()
                .map(converter::toString)
                .collect(Collectors.joining(DELIMITER));
        preferences.put(key, result);
        LOG.finest(String.format("Stored history items with key: '%s'.", key));
    }

    /**
     * Loads the history items from the preferences.
     */
    @Override
    protected void loadHistory() {
        String items = preferences.get(key, "");
        if (StringUtils.isNotEmpty(items)) {
            String[] ary = items.split(DELIMITER);
            set(Arrays.stream(ary)
                    .map(converter::fromString)
                    .toList());
        }
        LOG.finest(String.format("Loaded history items with key: '%s'.", key));
    }
}
