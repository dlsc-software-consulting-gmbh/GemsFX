package com.dlsc.gemsfx.docking.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A tab group of the docking tree, referring to its dockables by ID. A group without
 * dockables is only valid as the root of an empty tree.
 */
public final class DockingTabGroup implements DockingTree.Node {

    private final List<String> dockableIds = new ArrayList<>();
    private String selectedId;

    DockingTabGroup() {
    }

    public List<String> getDockableIds() {
        return Collections.unmodifiableList(dockableIds);
    }

    public String getSelectedId() {
        return selectedId;
    }

    public boolean isEmpty() {
        return dockableIds.isEmpty();
    }

    List<String> ids() {
        return dockableIds;
    }

    void add(String id) {
        dockableIds.add(id);
        selectedId = id;
    }

    void select(String id) {
        if (!dockableIds.contains(id)) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        selectedId = id;
    }

    void moveTab(String id, int index) {
        int current = dockableIds.indexOf(id);
        if (current < 0) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        if (index < 0 || index >= dockableIds.size()) {
            throw new IllegalArgumentException("Tab index out of range: " + index);
        }
        dockableIds.remove(current);
        dockableIds.add(index, id);
    }

    void remove(String id) {
        int index = dockableIds.indexOf(id);
        dockableIds.remove(index);
        if (id.equals(selectedId)) {
            selectedId = dockableIds.isEmpty() ? null : dockableIds.get(Math.min(index, dockableIds.size() - 1));
        }
    }
}
