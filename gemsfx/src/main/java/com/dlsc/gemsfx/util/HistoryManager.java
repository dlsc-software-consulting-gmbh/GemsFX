package com.dlsc.gemsfx.util;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.function.Predicate;

/**
 * The HistoryManager interface defines the standard operations to manage history storage
 * for any type of items, allowing for implementation of various data storage mechanisms.
 *
 * @param <T> the type of items stored in the history
 */
public interface HistoryManager<T> {

    /**
     * Adds a single item to the history storage.
     * If the item already exists, its position is updated.
     *
     * @param item The history item to be added.
     */
    void add(T item);

    /**
     * Adds multiple items to the history storage.
     * Duplicates in the input list are not added twice.
     *
     * @param items The list of history items to be added.
     */
    void add(List<T> items);

    /**
     * Removes a single item from the history storage.
     *
     * @param item The history item to be removed.
     * @return true if the item was successfully removed, false if the item was not found.
     */
    boolean remove(T item);

    /**
     * Removes multiple items from the history storage.
     *
     * @param items The list of history items to be removed.
     */
    void remove(List<T> items);

    /**
     * Clears all items from the history storage.
     */
    void clear();

    /**
     * Retrieves all stored history items.
     *
     * @return A list of all history items.
     */
    ObservableList<T> getAll();

    /**
     * Returns the property object for the maximum history size. This property can be
     * used to bind the history size limit to UI components or to observe changes.
     *
     * @return The IntegerProperty representing the maximum number of history items allowed.
     */
    IntegerProperty maxHistorySizeProperty();

    /**
     * Gets the current maximum size of the history list.
     *
     * @return The current maximum number of items that can be stored in the history.
     */
    int getMaxHistorySize();

    /**
     * Sets the maximum size of the history list. If the current number of items
     * exceeds the specified size, items will be removed from the end of the list.
     *
     * @param maxHistorySize The maximum number of items to retain in the history.
     */
    void setMaxHistorySize(int maxHistorySize);

    /**
     * Returns the property that holds the filter used when adding items to the history.
     * Only items that pass the filter will be added to the history.
     *
     * @return the property containing the filter
     */
    ObjectProperty<Predicate<T>> filterProperty();

    /**
     * Sets a filter to be used when adding items to the history. Only items that pass the
     * filter will be added to the history.
     *
     * @param filter The filter to apply.
     */
    void setFilter(Predicate<T> filter);

    /**
     * Gets the current filter used for adding items to the history.
     *
     * @return The current filter.
     */
    Predicate<T> getFilter();
}
