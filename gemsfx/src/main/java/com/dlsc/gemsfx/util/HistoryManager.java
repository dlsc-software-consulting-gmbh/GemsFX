package com.dlsc.gemsfx.util;

import javafx.beans.Observable;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * The HistoryManager class defines the standard operations to manage history storage
 * for any type of items, allowing for implementation of various data storage mechanisms.
 *
 * @param <T> the type of items stored in the history
 */
public abstract class HistoryManager<T> {

    private static final Logger LOG = Logger.getLogger(HistoryManager.class.getName());

    private static final int DEFAULT_MAX_HISTORY_SIZE = 30;

    private final ObservableList<T> history = FXCollections.observableArrayList();

    public HistoryManager() {
        maxHistorySizeProperty().addListener(it -> {
            // Check if the max history size is negative. If so, log a warning.
            if (getMaxHistorySize() < 0) {
                LOG.warning("Max history size must be greater than or equal to 0. ");
            }
            trimHistory();
        });

        unmodifiableHistory.addListener((Observable it) -> storeHistory());
    }

    protected abstract void loadHistory();

    protected abstract void storeHistory();

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
        if (item != null && getFilter().test(item)) {
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
    public final ObservableList<T> getAllUnmodifiable() {
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

    private final ObjectProperty<Predicate<T>> filter = new SimpleObjectProperty<>(this, "filter", it -> true);

    /**
     * Returns the property object for the filter used when adding items to the history.
     * Only items that pass the filter will be added to the history.
     *
     * @return the property object for the filter
     */
    public final ObjectProperty<Predicate<T>> filterProperty() {
        return filter;
    }

    public final Predicate<T> getFilter() {
        return filter.get();
    }

    public final void setFilter(Predicate<T> filter) {
        this.filter.set(filter);
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
        return history.stream().distinct().filter(Objects::nonNull).filter(getFilter()).limit(Math.max(0, getMaxHistorySize())).toList();
    }
}
