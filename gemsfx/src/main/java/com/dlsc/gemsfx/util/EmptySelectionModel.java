package com.dlsc.gemsfx.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.MultipleSelectionModel;

/**
 * A "do nothing" selection model.
 *
 * @param <T> the model object type
 */
public class EmptySelectionModel<T> extends MultipleSelectionModel<T> {

    private final ObservableList<Integer> indices = FXCollections.observableArrayList();

    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

    @Override
    public ObservableList<Integer> getSelectedIndices() {
        return indices;
    }

    @Override
    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    @Override
    public void selectIndices(int index, int... indices) {
    }

    @Override
    public void selectAll() {
    }

    @Override
    public void selectFirst() {
    }

    @Override
    public void selectLast() {
    }

    @Override
    public void clearAndSelect(int index) {
    }

    @Override
    public void select(int index) {
    }

    @Override
    public void select(T obj) {
    }

    @Override
    public void clearSelection(int index) {
    }

    @Override
    public void clearSelection() {
    }

    @Override
    public boolean isSelected(int index) {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void selectPrevious() {
    }

    @Override
    public void selectNext() {
    }
}
