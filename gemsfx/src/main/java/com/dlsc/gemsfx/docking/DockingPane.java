package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.docking.model.DockingTree;
import com.dlsc.gemsfx.docking.model.DockingSplit;
import com.dlsc.gemsfx.docking.model.DockingTabGroup;
import com.dlsc.gemsfx.skins.DockingPaneSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.geometry.Rectangle2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Control associated with exactly one {@link DockingContext}. It owns one pane tree and renders each dockable as a
 * title tab with a close affordance; only the selected tab's content is displayed.
 * Hold Shift when starting a tab drag to move its entire tab group, preserving tab order and selection.
 * Shift-double-click a tab or its strip to maximize or restore its group within this pane.
 * The tab group containing the active dockable has the {@code active} pseudo-class.
 * Internal floating groups are displayed above this pane's regular layout, including a maximized
 * group. Drag their headers to move them and their borders or corners to resize them. Entering or
 * leaving internal floating mode is controlled through the context's API.
 */
public class DockingPane extends Control {

    private final DockingContext dockingContext;

    private final DockingTree tree = new DockingTree();
    private final Map<String, Dockable> dockablesById = new HashMap<>();
    private final List<FloatingState> floatingStates = new ArrayList<>();
    private final ReadOnlyObjectWrapper<List<DockingFloatingGroup>> floatingGroups =
            new ReadOnlyObjectWrapper<>(this, "floatingGroups", List.of());

    /**
     * Immutable snapshots of the internal floating groups, ordered back to front.
     * Includes tab order, selection and persistent edge-relative geometry.
     *
     * @return the floating groups property
     */
    public final ReadOnlyObjectProperty<List<DockingFloatingGroup>> floatingGroupsProperty() {
        return floatingGroups.getReadOnlyProperty();
    }

    public final List<DockingFloatingGroup> getFloatingGroups() {
        return floatingGroups.get();
    }

    /**
     * Tests whether a dockable belongs to an internal floating group in this pane.
     * This does not indicate whether the pane itself is hosted in a floating window.
     *
     * @param id the dockable ID
     * @return {@code true} if internally floating in this pane, or {@code false} if docked or not present
     */
    public final boolean isDockableFloating(String id) {
        return floatingState(id) != null;
    }

    /**
     * Updates an internal floating group's bounds and captures its nearest horizontal and
     * vertical edges. Coordinates are relative to this pane's content area, excluding padding.
     *
     * @param id any dockable in the floating group
     * @param bounds the requested bounds, with finite coordinates and positive dimensions
     * @throws IllegalArgumentException if the dockable is not internally floating in this pane
     */
    public final void updateFloatingGroupBounds(String id, Rectangle2D bounds) {
        DockingContext.requireFxThread();
        FloatingState state = floatingState(id);
        if (state == null) {
            throw new IllegalArgumentException("Dockable is not internally floating in this pane: " + id);
        }
        state.layout = floatingLayout((DockingLayoutNode.Tabs) state.tree.snapshot(), bounds);
        publishFloatingGroups();
    }

    private DockingFloatingGroup floatingLayout(DockingLayoutNode.Tabs tabs, Rectangle2D bounds) {
        return DockingFloatingGroup.atBounds(tabs, bounds,
                Math.max(0, getWidth() - getInsets().getLeft() - getInsets().getRight()),
                Math.max(0, getHeight() - getInsets().getTop() - getInsets().getBottom()));
    }

    final void floatInternally(String id, Rectangle2D bounds, boolean wholeGroup) {
        DockingLayoutNode.Tabs source = tabGroupSnapshot(id);
        DockingLayoutNode.Tabs tabs = wholeGroup ? source : new DockingLayoutNode.Tabs(List.of(id), id);
        DockingFloatingGroup layout = floatingLayout(tabs, bounds);
        FloatingState existing = floatingState(id);
        if (existing != null && existing.tree.dockableIds().size() == tabs.getDockableIds().size()) {
            existing.layout = layout;
            selectDockable(tabs.getSelectedId());
            publishFloatingGroups();
            return;
        }
        List<Dockable> moving = new ArrayList<>();
        for (String tabId : tabs.getDockableIds()) {
            moving.add(dockablesById.get(tabId));
        }
        for (Dockable dockable : moving) {
            removeDockable(dockable);
        }
        FloatingState state = new FloatingState(layout);
        floatingStates.add(state);
        for (Dockable dockable : moving) {
            dockablesById.put(dockable.getId(), dockable);
        }
        publishFloatingGroups();
        activeDockable.set(dockablesById.get(tabs.getSelectedId()));
    }

    private FloatingState floatingState(String id) {
        for (FloatingState state : floatingStates) {
            if (state.tree.dockableIds().contains(id)) {
                return state;
            }
        }
        return null;
    }

    private DockingTree treeFor(String id) {
        FloatingState state = floatingState(id);
        return state == null ? tree : state.tree;
    }

    final DockingLayoutNode.Tabs tabGroupSnapshot(String id) {
        return findGroup(treeFor(id).snapshot(), id);
    }

    private void publishFloatingGroups() {
        List<DockingFloatingGroup> snapshots = new ArrayList<>();
        for (FloatingState state : floatingStates) {
            state.layout = state.layout.withTabs((DockingLayoutNode.Tabs) state.tree.snapshot());
            snapshots.add(state.layout);
        }
        floatingGroups.set(List.copyOf(snapshots));
    }

    private static final class FloatingState {
        private final DockingTree tree = new DockingTree();
        private DockingFloatingGroup layout;

        private FloatingState(DockingFloatingGroup layout) {
            this.layout = layout;
            tree.restore(layout.getTabs());
        }
    }

    /**
     * Creates a pane belonging to the supplied context. Applications should create panes via the context's
     * pane factory and {@link DockingContext#createPane()}.
     *
     * @param dockingContext the owning context
     */
    protected DockingPane(DockingContext dockingContext) {
        this.dockingContext = Objects.requireNonNull(dockingContext, "dockingContext must not be null");
        getStyleClass().add("docking-pane");
        AccessibilityUtil.setRole(this, AccessibleRole.NODE, ResourceBundleManager.getString(
                ResourceBundleManager.BundleType.DOCKING_PANE, "accessible.role-description", "Docking pane"));
        setPlaceholder(new DockingPanePlaceholder());
        activeDockable.addListener((observable, previous, current) -> {
            Dockable maximized = getMaximizedDockable();
            if (maximized != null && (current == null || floatingState(current.getId()) == null)) {
                DockingLayoutNode.Tabs group = findGroup(getLayoutRoot(), maximized.getId());
                maximizedDockable.set(current != null && group != null && group.getDockableIds().contains(current.getId())
                        ? current : null);
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DockingPane.class.getResource("docking-pane.css")).toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DockingPaneSkin(this);
    }

    /**
     * Returns the context this pane belongs to.
     *
     * @return the docking context
     */
    public final DockingContext getDockingContext() {
        return dockingContext;
    }

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder");

    /**
     * The node displayed in the center of this pane when it contains no dockables.
     * The default is a {@link DockingPanePlaceholder} with a theme-aware vector illustration
     * of a docking workspace and optional text.
     * Set this property to {@code null} to leave an empty pane blank.
     *
     * @return the placeholder property
     */
    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final Node getPlaceholder() {
        return placeholder.get();
    }

    public final void setPlaceholder(Node placeholder) {
        this.placeholder.set(placeholder);
    }

    private final ReadOnlyObjectWrapper<Dockable> activeDockable = new ReadOnlyObjectWrapper<>(this, "activeDockable");

    /**
     * The currently active dockable of this pane, or {@code null}.
     * Clicking or focusing a dockable's content or tab makes it active. If another dockable is activated
     * while focus is in a different dockable in this pane, focus moves to the newly active
     * dockable's content, or to its tab if the content has no focusable node. Focus outside
     * this pane is left unchanged.
     *
     * @return the active dockable property
     */
    public final ReadOnlyObjectProperty<Dockable> activeDockableProperty() {
        return activeDockable.getReadOnlyProperty();
    }

    public final Dockable getActiveDockable() {
        return activeDockable.get();
    }

    private final ReadOnlyObjectWrapper<Dockable> maximizedDockable =
            new ReadOnlyObjectWrapper<>(this, "maximizedDockable");

    /**
     * The selected dockable in the maximized tab group, or {@code null} when no group is maximized.
     * Maximization is temporary and is not included in layout snapshots. Use
     * {@link #setMaximizedDockable(Dockable)} to maximize a dockable belonging to this pane,
     * or pass {@code null} to restore the normal layout.
     *
     * @return the maximized dockable property
     */
    public final ReadOnlyObjectProperty<Dockable> maximizedDockableProperty() {
        return maximizedDockable.getReadOnlyProperty();
    }

    public final Dockable getMaximizedDockable() {
        return maximizedDockable.get();
    }

    public final void setMaximizedDockable(Dockable dockable) {
        DockingContext.requireFxThread();
        if (dockable != null && dockablesById.get(dockable.getId()) != dockable) {
            throw new IllegalArgumentException("Dockable does not belong to this pane");
        }
        if (dockable != null && floatingState(dockable.getId()) != null) {
            throw new IllegalArgumentException("Internal floating groups cannot be maximized");
        }
        if (dockable != null && getActiveDockable() != dockable) {
            selectDockable(dockable.getId());
        }
        maximizedDockable.set(dockable);
    }

    private final ReadOnlyObjectWrapper<DockingLayoutNode> layoutRoot =
            new ReadOnlyObjectWrapper<>(this, "layoutRoot", new DockingLayoutNode.Empty());

    /**
     * An immutable snapshot of this pane's regular split/tab layout, excluding
     * {@link #floatingGroupsProperty() internal floating groups}. A new snapshot is published on
     * every structural or selection change to that layout, so observers must not rely on node identity.
     *
     * @return the layout root property
     */
    public final ReadOnlyObjectProperty<DockingLayoutNode> layoutRootProperty() {
        return layoutRoot.getReadOnlyProperty();
    }

    public final DockingLayoutNode getLayoutRoot() {
        return layoutRoot.get();
    }

    /**
     * Selects the dockable with the given ID and makes it the active dockable.
     *
     * @param id the dockable ID
     * @throws IllegalArgumentException if the dockable is not part of this pane
     */
    public final void selectDockable(String id) {
        dockingContext.withSelectionUpdates(() -> {
            selectDockableInternal(id);
            return null;
        });
    }

    private void selectDockableInternal(String id) {
        DockingContext.requireFxThread();
        Dockable dockable = dockablesById.get(Objects.requireNonNull(id, "id must not be null"));
        if (dockable == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        DockingTree selectedTree = treeFor(id);
        selectedTree.select(id);
        if (selectedTree == tree) {
            layoutRoot.set(tree.snapshot());
        } else {
            FloatingState state = floatingState(id);
            floatingStates.remove(state);
            floatingStates.add(state);
            publishFloatingGroups();
        }
        activeDockable.set(dockable);
    }

    final Map<DockingTabGroup, Dockable> selectedGroups() {
        Map<DockingTabGroup, Dockable> result = new LinkedHashMap<>();
        collectSelectedGroups(tree.root(), result);
        for (FloatingState state : floatingStates) {
            collectSelectedGroups(state.tree.root(), result);
        }
        return result;
    }

    final DockingTabGroup groupOf(String id) {
        for (DockingTabGroup group : selectedGroups().keySet()) {
            if (group.getDockableIds().contains(id)) {
                return group;
            }
        }
        return null;
    }

    private void collectSelectedGroups(DockingTree.Node node, Map<DockingTabGroup, Dockable> result) {
        if (node instanceof DockingTabGroup) {
            DockingTabGroup group = (DockingTabGroup) node;
            if (group.getSelectedId() != null) {
                result.put(group, dockablesById.get(group.getSelectedId()));
            }
        } else {
            for (DockingTree.Node child : ((DockingSplit) node).getChildren()) {
                collectSelectedGroups(child, result);
            }
        }
    }

    private DockingLayoutNode.Tabs findGroup(DockingLayoutNode node, String id) {
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) node;
            return tabs.getDockableIds().contains(id) ? tabs : null;
        }
        if (node instanceof DockingLayoutNode.Split) {
            for (DockingLayoutNode child : ((DockingLayoutNode.Split) node).getChildren()) {
                DockingLayoutNode.Tabs found = findGroup(child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Atomically records all divider positions of a split. Observers of {@link #layoutRootProperty()}
     * are notified with one new snapshot after the entire vector has been validated.
     *
     * @param path ordered child indices leading from the layout root to the split
     * @param positions one fewer positions than children, finite and nondecreasing within [0, 1]
     * @throws IllegalArgumentException if the path or positions are invalid
     */
    public final void updateDividerPositions(List<Integer> path, List<Double> positions) {
        DockingContext.requireFxThread();
        tree.setDividerPositions(Objects.requireNonNull(path, "path must not be null"), positions);
        layoutRoot.set(tree.snapshot());
    }

    final void restoreLayout(DockingLayoutNode root, Map<String, Dockable> resolvedDockables) {
        restoreLayout(root, List.of(), resolvedDockables);
    }

    final void restoreLayout(DockingLayoutNode root, List<DockingFloatingGroup> floating,
                             Map<String, Dockable> resolvedDockables) {
        tree.restore(root);
        floatingStates.clear();
        for (DockingFloatingGroup group : floating) {
            floatingStates.add(new FloatingState(group));
        }
        dockablesById.clear();
        List<String> allIds = new ArrayList<>(tree.dockableIds());
        for (FloatingState state : floatingStates) {
            allIds.addAll(state.tree.dockableIds());
        }
        for (String id : allIds) {
            dockablesById.put(id, Objects.requireNonNull(resolvedDockables.get(id), "Unresolved dockable: " + id));
        }
        DockingLayoutNode snapshot = tree.snapshot();
        DockingLayoutNode last = snapshot;
        while (last instanceof DockingLayoutNode.Split) {
            List<DockingLayoutNode> children = ((DockingLayoutNode.Split) last).getChildren();
            last = children.get(children.size() - 1);
        }
        activeDockable.set(last instanceof DockingLayoutNode.Tabs
                ? dockablesById.get(((DockingLayoutNode.Tabs) last).getSelectedId()) : null);
        layoutRoot.set(snapshot);
        publishFloatingGroups();
        if (!floating.isEmpty()) {
            activeDockable.set(dockablesById.get(floating.get(floating.size() - 1).getTabs().getSelectedId()));
        }
    }

    final boolean containsDockable(String id) {
        return dockablesById.containsKey(id);
    }

    final void insertDockable(Dockable dockable, DockingPosition position) {
        Dockable active = getActiveDockable();
        DockingTree targetTree = tree;
        if (active == null) {
            tree.addToRoot(dockable);
        } else if (floatingState(active.getId()) != null && position != DockingPosition.CENTER) {
            tree.insertAtPaneEdge(dockable, position, 0.5);
        } else {
            targetTree = treeFor(active.getId());
            targetTree.splitAt(active.getId(), dockable, position, 0.5);
        }
        dockablesById.put(dockable.getId(), dockable);
        if (targetTree == tree) {
            layoutRoot.set(tree.snapshot());
        }
        publishFloatingGroups();
        activeDockable.set(dockable);
    }

    final void removeDockable(Dockable dockable) {
        if (getMaximizedDockable() == dockable) {
            DockingLayoutNode.Tabs group = findGroup(getLayoutRoot(), dockable.getId());
            List<String> ids = group.getDockableIds();
            int index = ids.indexOf(dockable.getId());
            maximizedDockable.set(ids.size() == 1 ? null
                    : dockablesById.get(ids.get(index == 0 ? 1 : index - 1)));
        }
        String id = dockable.getId();
        DockingTree sourceTree = treeFor(id);
        String neighbour = neighbourOf(sourceTree.snapshot(), id);
        sourceTree.remove(id);
        floatingStates.removeIf(state -> state.tree.dockableIds().isEmpty());
        dockablesById.remove(id);
        if (sourceTree == tree) {
            layoutRoot.set(tree.snapshot());
        }
        publishFloatingGroups();
        if (dockable == getActiveDockable()) {
            activeDockable.set(neighbour == null ? null : dockablesById.get(neighbour));
            if (neighbour != null) {
                DockingTree selectedTree = treeFor(neighbour);
                selectedTree.select(neighbour);
                if (selectedTree == tree) {
                    layoutRoot.set(tree.snapshot());
                }
                publishFloatingGroups();
            }
        }
    }

    /**
     * Tests whether the specified dockable can be moved relative to a target dockable.
     *
     * @param draggedId the ID of the dockable to move
     * @param targetId  the ID of the target dockable
     * @param position  the target position
     * @return {@code true} if the move is valid
     */
    public final boolean canMoveDockable(String draggedId, String targetId, DockingPosition position) {
        return dockingContext.canMoveDockable(this, draggedId, targetId, position);
    }

    /**
     * Moves the specified dockable relative to a target dockable.
     *
     * @param draggedId the ID of the dockable to move
     * @param targetId  the ID of the target dockable
     * @param position  the target position
     * @return {@code true} if the move was completed
     */
    public final boolean moveDockable(String draggedId, String targetId, DockingPosition position) {
        return dockingContext.moveDockable(this, draggedId, targetId, position);
    }

    /**
     * Tests whether a dockable can be placed beside this pane's entire layout.
     *
     * @param id the ID of an open dockable in this context
     * @param edge the outer edge: LEFT, RIGHT, TOP, or BOTTOM
     * @return {@code true} if the move is valid; {@code false} for an invalid edge or a lone tab in this pane
     */
    public final boolean canMoveDockableToPaneEdge(String id, DockingPosition edge) {
        return dockingContext.canMoveDockableToPaneEdge(this, id, edge);
    }

    /**
     * Places a dockable beside this pane's entire layout, rather than beside one tab group.
     * The dockable is removed from its source before the remaining layout is wrapped in a new split.
     *
     * @param id the ID of an open dockable in this context
     * @param edge the outer edge: LEFT, RIGHT, TOP, or BOTTOM
     * @return {@code true} if the move completed; {@code false} if it was invalid
     */
    public final boolean moveDockableToPaneEdge(String id, DockingPosition edge) {
        return dockingContext.moveDockableToPaneEdge(this, id, edge);
    }

    /**
     * Resolves a drop target from pane-relative coordinates and moves the dockable there.
     *
     * @param id       the ID of the dockable to move
     * @param x        horizontal drop coordinate within this pane
     * @param y        vertical drop coordinate within this pane
     * @param width    width used to resolve the target zone
     * @param height   height used to resolve the target zone
     * @param hoveredId ID of the title under the pointer, or {@code null}
     * @return {@code true} if a valid target was resolved and the move completed
     */
    public final boolean moveDockable(String id, double x, double y, double width, double height, String hoveredId) {
        DockingTree.ResolvedDropTarget target = DockingTree.resolveDropTarget(getLayoutRoot(), x, y, width, height, hoveredId, id);
        return target != null && moveDockable(id, target.getTargetId(), target.getPosition());
    }

    final boolean canMoveInside(String draggedId, String targetId, DockingPosition position) {
        if (!(draggedId != null
                && targetId != null
                && position != null
                && dockablesById.containsKey(draggedId)
                && !draggedId.equals(targetId)
                && canInsertRelativeTo(targetId, position))) {
            return false;
        }
        DockingTree sourceTree = treeFor(draggedId);
        DockingTree targetTree = treeFor(targetId);
        return sourceTree != targetTree || sourceTree.canMove(draggedId, new DockingTree.DropTarget(targetId, position));
    }

    final boolean canInsertRelativeTo(String targetId, DockingPosition position) {
        return containsDockable(targetId) && (floatingState(targetId) == null || position == DockingPosition.CENTER)
                && treeFor(targetId).canInsertRelativeTo(targetId, position);
    }

    final boolean moveInside(String draggedId, String targetId, DockingPosition position) {
        if (!canMoveInside(draggedId, targetId, position)) {
            return false;
        }
        DockingTree sourceTree = treeFor(draggedId);
        if (sourceTree != treeFor(targetId)) {
            Dockable dockable = removeDockableById(draggedId);
            insertDockableRelativeTo(targetId, dockable, position);
            return true;
        }
        if (!sourceTree.tryMove(draggedId, new DockingTree.DropTarget(targetId, position), 0.5)) {
            return false;
        }
        Dockable dockable = dockablesById.get(draggedId);
        if (sourceTree == tree) {
            layoutRoot.set(tree.snapshot());
        }
        publishFloatingGroups();
        activeDockable.set(dockable);
        return true;
    }

    final boolean canMoveInsideToPaneEdge(String id, DockingPosition edge) {
        return floatingState(id) != null || tree.canMoveToPaneEdge(id, edge);
    }

    final boolean moveInsideToPaneEdge(String id, DockingPosition edge) {
        if (floatingState(id) != null) {
            Dockable dockable = removeDockableById(id);
            insertDockableAtPaneEdge(dockable, edge);
            return true;
        }
        if (!tree.moveToPaneEdge(id, edge, 0.5)) {
            return false;
        }
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockablesById.get(id));
        return true;
    }

    final Dockable removeDockableById(String id) {
        Dockable dockable = dockablesById.get(id);
        if (dockable == null) {
            return null;
        }
        removeDockable(dockable);
        return dockable;
    }

    final void insertDockableRelativeTo(String targetId, Dockable dockable, DockingPosition position) {
        DockingTree targetTree = treeFor(targetId);
        targetTree.insertRelativeTo(targetId, dockable, position, 0.5);
        dockablesById.put(dockable.getId(), dockable);
        if (targetTree == tree) {
            layoutRoot.set(tree.snapshot());
        }
        publishFloatingGroups();
        activeDockable.set(dockable);
    }

    final void insertDockableAtPaneEdge(Dockable dockable, DockingPosition edge) {
        tree.insertAtPaneEdge(dockable, edge, 0.5);
        dockablesById.put(dockable.getId(), dockable);
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockable);
    }

    /**
     * Returns an immutable snapshot of the dockables currently contained in this pane.
     *
     * @return the dockables in this pane
     */
    public final List<Dockable> getDockables() {
        return List.copyOf(dockablesById.values());
    }

    /**
     * Finds a replacement active dockable before removal changes the tree. The preceding tab in the
     * same group is preferred, or the following tab when removing the first tab. If the group would
     * become empty, any remaining dockable elsewhere in the pane may take over.
     */
    private String neighbourOf(DockingLayoutNode node, String id) {
        if (node instanceof DockingLayoutNode.Tabs) {
            List<String> ids = ((DockingLayoutNode.Tabs) node).getDockableIds();
            int index = ids.indexOf(id);
            if (index < 0) {
                return null;
            }
            if (ids.size() == 1) {
                return remainingOutside(id);
            }
            return ids.get(index == 0 ? 1 : index - 1);
        }
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            for (DockingLayoutNode child : split.getChildren()) {
                String found = neighbourOf(child, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private String remainingOutside(String id) {
        for (String other : dockablesById.keySet()) {
            if (!other.equals(id)) {
                return other;
            }
        }
        return null;
    }
}
