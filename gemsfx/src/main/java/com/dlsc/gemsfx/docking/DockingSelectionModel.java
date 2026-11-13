package com.dlsc.gemsfx.docking;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;
import java.util.Objects;

/**
 * The selected dockable in each nonempty tab group of a docking context.
 * The selected-items list is observable and cannot be modified directly.
 */
public final class DockingSelectionModel {

    private final DockingContext context;
    private final ObservableList<Dockable> selectedItems = FXCollections.observableArrayList();
    private final ObservableList<Dockable> readOnlySelectedItems = FXCollections.unmodifiableObservableList(selectedItems);

    DockingSelectionModel(DockingContext context) {
        this.context = context;
    }

    /**
     * Returns the selected dockables across all panes, including floating panes.
     *
     * @return the unmodifiable observable selected-items list
     */
    public ObservableList<Dockable> getSelectedItems() {
        return readOnlySelectedItems;
    }

    /**
     * Tests whether the dockable is selected in its tab group.
     *
     * @param dockable the dockable
     * @return whether the dockable is selected
     */
    public boolean isSelected(Dockable dockable) {
        return selectedItems.contains(dockable);
    }

    /**
     * Selects the dockable in its group and makes it active in its pane. Other
     * groups retain their selections.
     *
     * @param dockable an open dockable in this context
     * @throws IllegalArgumentException if the dockable is not open in this context
     * @throws IllegalStateException if called off the JavaFX application thread
     */
    public void select(Dockable dockable) {
        DockingContext.requireFxThread();
        DockingPane pane = context.findDockingPane(Objects.requireNonNull(dockable, "dockable must not be null"));
        if (pane == null) {
            throw new IllegalArgumentException("Dockable is not open in this context");
        }
        pane.selectDockable(dockable.getId());
    }

    void update(List<Dockable> selected) {
        if (!selectedItems.equals(selected)) {
            selectedItems.setAll(selected);
        }
    }
}
