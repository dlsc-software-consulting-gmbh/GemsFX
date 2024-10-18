package com.dlsc.gemsfx.util;

import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CustomMultipleSelectionModel allows for managing the selection state of items in an ObservableList.
 * It extends from MultipleSelectionModel and adds functionality to maintain a weak reference to the
 * items list, facilitating the handling of list changes and ensuring selection consistency.
 *
 * @param <T> the type of the items contained in the selection model.
 */
public class CustomMultipleSelectionModel<T> extends MultipleSelectionModel<T> {

    private WeakReference<ObservableList<T>> weakItemsRef;
    private final ListChangeListener<T> itemsContentListener = this::updateSelectionOnItemsChange;
    private final InvalidationListener itemsPropertyListener = observable -> updateItemsObserver();

    public CustomMultipleSelectionModel() {
        // Add listener to itemsProperty
        itemsProperty().addListener(new WeakInvalidationListener(itemsPropertyListener));

        // Listen to changes in selectedIndices to update selectedIndex and selectedItem
        selectedIndices.addListener((ListChangeListener<Integer>) change -> updateSelectedItemAndIndex());
    }

    /**
     * Updates the observer for the items list. If the reference to the items list changes,
     * this method sets up a new listener and clears the current selection. If the reference
     * remains the same, it validates the selected indices and updates the selected item
     * and index.
     */
    private void updateItemsObserver() {
        ObservableList<T> oldItems = weakItemsRef != null ? weakItemsRef.get() : null;
        ObservableList<T> newItems = getItems();

        if (oldItems != null) {
            oldItems.removeListener(itemsContentListener);
        }
        if (newItems != null) {
            newItems.addListener(itemsContentListener);
        }

        weakItemsRef = new WeakReference<>(newItems);

        if (oldItems != newItems) {
            // The items list reference has changed. Clear selection
            clearSelection();
        } else {
            // Items list is the same, revalidate selection
            validateSelectedIndices();
            updateSelectedItemAndIndex();
        }
    }

    /**
     * Updates the selection model when the items list changes. Ensures the selection
     * remains consistent with the current state of the items list.
     */
    private void updateSelectionOnItemsChange(ListChangeListener.Change<? extends T> c) {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            // Items list is empty, clear selection
            clearSelection();
            return;
        }

        if (c == null) {
            // No specific change information, revalidate selection
            validateSelectedIndices();
            updateSelectedItemAndIndex();
            return;
        }

        while (c.next()) {
            if (c.wasPermutated()) {
                // Handle permutation
                int[] perm = new int[items.size()];
                for (int i = c.getFrom(); i < c.getTo(); i++) {
                    perm[i] = c.getPermutation(i);
                }
                permuteSelection(perm);
            } else if (c.wasUpdated()) {
                // Handle updates
            } else {
                // Handle additions and removals
                if (c.wasRemoved()) {
                    removeIndices(c);
                }
                if (c.wasAdded()) {
                    addIndices(c);
                }
            }
        }

        // Ensure selected indices are valid
        validateSelectedIndices();

        // Update selectedIndex and selectedItem
        updateSelectedItemAndIndex();
    }

    /**
     * Removes the indices and items from the selection model based on the change
     * described by the provided ListChangeListener.Change instance.
     *
     * @param c the ListChangeListener.Change instance that describes the changes
     *          to the list and which indices should be removed from the selection.
     */
    private void removeIndices(ListChangeListener.Change<? extends T> c) {
        // Collect indices of removed items
        List<Integer> removedIndices = new ArrayList<>();
        for (int i = 0; i < c.getRemovedSize(); i++) {
            removedIndices.add(c.getFrom() + i);
        }

        // Remove selected indices corresponding to removed items
        selectedIndices.removeIf(removedIndices::contains);
        selectedItems.removeIf(c.getRemoved()::contains);

        // Adjust selected indices after the removal point
        for (int i = 0; i < selectedIndices.size(); i++) {
            int idx = selectedIndices.get(i);
            if (idx >= c.getFrom() + c.getRemovedSize()) {
                selectedIndices.set(i, idx - c.getRemovedSize());
            } else if (idx >= c.getFrom()) {
                // This should have been removed above; extra safety check
                selectedIndices.remove(i);
                selectedItems.remove(i);
                i--;
            }
        }
    }

    private void addIndices(ListChangeListener.Change<? extends T> c) {
        // Adjust selected indices after the addition point
        for (int i = 0; i < selectedIndices.size(); i++) {
            int idx = selectedIndices.get(i);
            if (idx >= c.getFrom()) {
                selectedIndices.set(i, idx + c.getAddedSize());
            }
        }
    }

    private void permuteSelection(int[] perm) {
        // Map old indices to new indices
        Map<Integer, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < perm.length; i++) {
            indexMap.put(i, perm[i]);
        }

        // Update selected indices based on permutation
        for (int i = 0; i < selectedIndices.size(); i++) {
            int oldIndex = selectedIndices.get(i);
            Integer newIndex = indexMap.get(oldIndex);
            if (newIndex != null) {
                selectedIndices.set(i, newIndex);
            } else {
                // If the old index is not in the map, remove it
                selectedIndices.remove(i);
                selectedItems.remove(i);
                i--;
            }
        }
    }

    private void validateSelectedIndices() {
        ObservableList<T> items = getItems();
        if (items == null) {
            clearSelection();
            return;
        }

        // Remove any indices that are out of bounds
        selectedIndices.removeIf(idx -> idx < 0 || idx >= items.size());

        // Sync selectedItems with selectedIndices
        selectedItems.clear();
        for (int idx : selectedIndices) {
            selectedItems.add(items.get(idx));
        }
    }

    private void updateSelectedItemAndIndex() {
        ObservableList<T> items = getItems();
        if (items == null || selectedIndices.isEmpty() || items.isEmpty()) {
            setSelectedIndex(-1);
            setSelectedItem(null);
        } else {
            int lastSelectedIndex = selectedIndices.get(selectedIndices.size() - 1);
            if (lastSelectedIndex >= 0 && lastSelectedIndex < items.size()) {
                setSelectedIndex(lastSelectedIndex);
                setSelectedItem(items.get(lastSelectedIndex));
            } else {
                // Index out of bounds, clear selection
                clearSelection();
            }
        }
    }

    // items

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public final ObservableList<T> getItems() {
        return items.get();
    }

    public final void setItems(ObservableList<T> value) {
        items.set(value);
    }

    public final ListProperty<T> itemsProperty() {
        return items;
    }

    // selectedIndices

    private final ReadOnlyListWrapper<Integer> selectedIndices = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return selectedIndices.getReadOnlyProperty();
    }

    // selectedItems

    private final ReadOnlyListWrapper<T> selectedItems = new ReadOnlyListWrapper<>(FXCollections.observableArrayList());

    @Override
    public ObservableList<T> getSelectedItems() {
        return selectedItems.getReadOnlyProperty();
    }

    @Override
    public void selectIndices(int index, int... indices) {
        if (getSelectionMode() == SelectionMode.SINGLE) {
            clearAndSelect(index);
        } else {
            select(index);
            for (int idx : indices) {
                select(idx);
            }
        }
    }

    @Override
    public void selectAll() {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        if (getSelectionMode() == SelectionMode.SINGLE) {
            select(0);
        } else {
            clearSelection();
            for (int i = 0; i < items.size(); i++) {
                selectedIndices.add(i);
                selectedItems.add(items.get(i));
            }
            setSelectedIndex(items.size() - 1);
            setSelectedItem(items.get(items.size() - 1));
        }
    }

    @Override
    public void clearAndSelect(int index) {
        ObservableList<T> items = getItems();
        if (items == null || index < 0 || index >= items.size()) {
            clearSelection();
            return;
        }
        if (!isSelected(index)) {
            setSelectedIndex(index);
            setSelectedItem(items.get(index));
        }
        selectedIndices.setAll(index);
        selectedItems.setAll(items.get(index));
    }

    @Override
    public void select(int index) {
        ObservableList<T> items = getItems();
        if (items == null || index < 0 || index >= items.size()) {
            return;
        }
        if (getSelectionMode() == SelectionMode.SINGLE) {
            if (isSelected(index)) {
                // Already selected, no need to repeat
                return;
            }
            clearSelection();
        }
        if (!selectedIndices.contains(index)) {
            selectedIndices.add(index);
            selectedItems.add(items.get(index));
        }
        // Update selectedIndex and selectedItem
        setSelectedIndex(index);
        setSelectedItem(items.get(index));
    }

    @Override
    public void select(T obj) {
        ObservableList<T> items = getItems();
        if (items == null) {
            clearSelection();
            return;
        }
        int index = items.indexOf(obj);
        if (index >= 0) {
            select(index);
        }
    }

    @Override
    public void clearSelection(int index) {
        int idx = selectedIndices.indexOf(index);
        if (idx >= 0) {
            selectedIndices.remove(idx);
            selectedItems.remove(idx);
        }
        // Update selectedIndex and selectedItem
        updateSelectedItemAndIndex();
    }

    @Override
    public void clearSelection() {
        selectedIndices.clear();
        selectedItems.clear();
        setSelectedIndex(-1);
        setSelectedItem(null);
    }

    @Override
    public boolean isSelected(int index) {
        return selectedIndices.contains(index);
    }

    @Override
    public boolean isEmpty() {
        return selectedIndices.isEmpty();
    }

    @Override
    public void selectPrevious() {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        if (selectedIndices.isEmpty()) {
            select(items.size() - 1);
        } else {
            int lastIndex = getSelectedIndex();
            if (lastIndex > 0) {
                select(lastIndex - 1);
            }
        }
    }

    @Override
    public void selectNext() {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        if (selectedIndices.isEmpty()) {
            select(0);
        } else {
            int lastIndex = getSelectedIndex();
            if (lastIndex < items.size() - 1) {
                select(lastIndex + 1);
            }
        }
    }

    @Override
    public void selectFirst() {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        select(0);
    }

    @Override
    public void selectLast() {
        ObservableList<T> items = getItems();
        if (items == null || items.isEmpty()) {
            return;
        }
        select(items.size() - 1);
    }
}
