package com.dlsc.gemsfx.util;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
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
public class PreferencesHistoryManager<T> implements HistoryManager<T> {

    private static final Logger LOG = Logger.getLogger(PreferencesHistoryManager.class.getName());

    private static final int DEFAULT_MAX_HISTORY_SIZE = 30;

    /**
     * Using Unicode Record Separator as delimiter.
     * This character is not likely to be used in the history items.
     */
    private static final String DEFAULT_DELIMITER = "␞";

    /**
     * Default preferences key used to store history items.
     */
    private static final String DEFAULT_PREFERENCES_KEY = "history-items";

    private final String delimiter;
    private final String preferencesKey;
    private final StringConverter<T> converter;

    public PreferencesHistoryManager(StringConverter<T> converter) {
        this(DEFAULT_DELIMITER, DEFAULT_PREFERENCES_KEY, converter);
    }

    public PreferencesHistoryManager(String delimiter, String preferencesKey, StringConverter<T> converter) {
        this.delimiter = Objects.requireNonNull(delimiter);
        this.preferencesKey = Objects.requireNonNull(preferencesKey);
        this.converter = Objects.requireNonNull(converter);

        loadHistory();

        maxHistorySizeProperty().addListener(it -> {
            // Check if the max history size is negative. If so, log a warning.
            if (getMaxHistorySize() < 0) {
                LOG.warning("Max history size must be greater than or equal to 0. ");
            }
            trimHistory();
        });

        unmodifiableHistory.addListener((Observable it) -> storeHistory());
        preferencesProperty().addListener(it -> loadHistory());
    }

    /**
     * Stores the history items in the preferences.
     */
    private void storeHistory() {
        Preferences preferences = getPreferences();
        if (preferences != null) {
            String result = unmodifiableHistory.stream()
                    .map(converter::toString)
                    .collect(Collectors.joining(delimiter));
            preferences.put(preferencesKey, result);
        }
    }

    /**
     * Loads the history items from the preferences.
     */
    private void loadHistory() {
        Preferences preferences = getPreferences();
        if (preferences != null) {
            String items = preferences.get(preferencesKey, "");
            if (StringUtils.isNotEmpty(items)) {
                String[] ary = items.split(delimiter);
                Arrays.stream(ary)
                        .map(converter::fromString)
                        .forEach(history::add);
            }
        }
    }

    private final ObservableList<T> history = FXCollections.observableArrayList();

    /**
     * Sets the history of the HistoryManager with the provided list of strings.
     * The method ensures that duplicates are removed from the list.
     *
     * @param history the list of strings representing the history
     */
    public final void set(List<T> history) {
        this.history.setAll(convertToUniqueList(history));
    }

    /**
     * Adds the given item to the history. The method ensures that duplicates will not be added.
     *
     * @param item the item to add
     */
    public final void add(T item) {
        if (item != null) {
            history.remove(item);
            history.add(0, item);
            trimHistory();
        }
    }

    /**
     * Adds the given items to the history.
     *
     * @param items the items to add
     */
    public final void add(List<T> items) {
        List<T> uniqueItems = convertToUniqueList(items);
        if (!uniqueItems.isEmpty()) {
            history.removeAll(uniqueItems);
            history.addAll(0, uniqueItems);
            trimHistory();
        }
    }

    /**
     * Removes the given item from the history.
     *
     * @param item the item to remove
     * @return true if the item was removed, false otherwise
     */
    public final boolean remove(T item) {
        return history.remove(item);
    }

    /**
     * Removes the given items from the history.
     *
     * @param items the items to remove
     */
    public final void remove(List<T> items) {
        history.removeAll(items);
    }

    /**
     * Clears the history.
     */
    public final void clear() {
        history.clear();
    }

    private final ObservableList<T> unmodifiableHistory = FXCollections.unmodifiableObservableList(history);

    /**
     * Returns an unmodifiable list of the history.
     */
    public final ObservableList<T> getAll() {
        return unmodifiableHistory;
    }

    private final IntegerProperty maxHistorySize = new SimpleIntegerProperty(this, "maxHistorySize", DEFAULT_MAX_HISTORY_SIZE);

    /**
     * The maximum number of items that the history will store. If the number of items exceeds this value, the oldest
     * items will be removed.
     *
     * @return the maximum number of items in the history
     */
    public final IntegerProperty maxHistorySizeProperty() {
        return maxHistorySize;
    }

    public final int getMaxHistorySize() {
        return maxHistorySize.get();
    }

    public final void setMaxHistorySize(int maxHistorySize) {
        maxHistorySizeProperty().set(maxHistorySize);
    }

    private final ObjectProperty<Preferences> preferences = new SimpleObjectProperty<>(this, "preferences");

    /**
     * Returns the property object representing the preferences used for persisting history records.
     * This property can be used to set or get the `Preferences` instance for storing history items.
     *
     * @return the property object representing the preferences
     */
    public final ObjectProperty<Preferences> preferencesProperty() {
        return preferences;
    }

    public final Preferences getPreferences() {
        return preferences.get();
    }

    public final void setPreferences(Preferences preferences) {
        this.preferences.set(preferences);
    }

    /**
     * Trims the history list to ensure it does not exceed the maximum allowed size.
     * If the current history size is greater than the maximum size, the method removes
     * the extra elements from the history list.
     */
    private void trimHistory() {
        int max = Math.max(0, getMaxHistorySize());
        if (history.size() > max) {
            history.remove(max, history.size());
        }
    }

    /**
     * Converts a given list of strings to a unique list of strings. Filters out empty strings.
     *
     * @param history the list of strings to convert
     * @return the converted unique list of strings
     */
    private List<T> convertToUniqueList(List<T> history) {
        return history.stream().distinct().filter(Objects::nonNull).limit(Math.max(0, getMaxHistorySize())).toList();
    }

    /**
     * @return the delimiter used to separate history items
     */
    public final String getDelimiter() {
        return delimiter;
    }

    /**
     * @return the preferences key used to store history items
     */
    public final String getPreferencesKey() {
        return preferencesKey;
    }

}
