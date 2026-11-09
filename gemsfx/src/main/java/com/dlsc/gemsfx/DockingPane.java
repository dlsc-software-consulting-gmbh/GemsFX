package com.dlsc.gemsfx;

import com.dlsc.gemsfx.docking.internal.DockingTree;
import com.dlsc.gemsfx.skins.DockingPaneSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.AccessibleRole;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Control associated with exactly one {@link DockingContext}. It owns one pane tree and renders each dockable as a
 * title tab with a close affordance; only the selected tab's content is displayed.
 */
public class DockingPane extends Control {

    private final DockingContext dockingContext;

    private final DockingTree tree = new DockingTree();
    private final Map<String, Dockable> dockablesById = new HashMap<>();

    DockingPane(DockingContext dockingContext) {
        this.dockingContext = Objects.requireNonNull(dockingContext, "dockingContext must not be null");
        getStyleClass().add("docking-pane");
        AccessibilityUtil.setRole(this, AccessibleRole.NODE, ResourceBundleManager.getString(
                ResourceBundleManager.BundleType.DOCKING_PANE, "accessible.role-description", "Docking pane"));
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

    private final ReadOnlyObjectWrapper<Dockable> activeDockable = new ReadOnlyObjectWrapper<>(this, "activeDockable");

    /**
     * The currently active dockable of this pane, or {@code null}.
     *
     * @return the active dockable property
     */
    public final ReadOnlyObjectProperty<Dockable> activeDockableProperty() {
        return activeDockable.getReadOnlyProperty();
    }

    public final Dockable getActiveDockable() {
        return activeDockable.get();
    }

    private final ReadOnlyObjectWrapper<DockingLayoutNode> layoutRoot =
            new ReadOnlyObjectWrapper<>(this, "layoutRoot", new DockingLayoutNode.Empty());

    /**
     * An immutable snapshot of this pane's current layout. A new snapshot is published on every structural or
     * selection change, so observers must not rely on node identity.
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
        DockingContext.requireFxThread();
        Dockable dockable = dockablesById.get(Objects.requireNonNull(id, "id must not be null"));
        if (dockable == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        tree.select(id);
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockable);
    }

    /**
     * Records a user-driven divider change so later layout snapshots keep the visible proportion. Observers of
     * {@link #layoutRootProperty()} are notified with a new snapshot.
     *
     * @param path child indices (0 = first, 1 = second) leading from the layout root to the split
     * @param position the new divider position within [0, 1]
     * @throws IllegalArgumentException if the path does not address a split or the position is invalid
     */
    public final void updateDividerPosition(List<Integer> path, double position) {
        DockingContext.requireFxThread();
        tree.setDividerPosition(Objects.requireNonNull(path, "path must not be null"), position);
        layoutRoot.set(tree.snapshot());
    }

    final boolean containsDockable(String id) {
        return dockablesById.containsKey(id);
    }

    final void insertDockable(Dockable dockable, DockingPosition position) {
        Dockable active = getActiveDockable();
        if (active == null) {
            tree.addToRoot(dockable);
        } else {
            tree.splitAt(active.getId(), dockable, position, 0.5);
        }
        dockablesById.put(dockable.getId(), dockable);
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockable);
    }

    final void removeDockable(Dockable dockable) {
        String id = dockable.getId();
        String neighbour = neighbourOf(getLayoutRoot(), id);
        tree.remove(id);
        dockablesById.remove(id);
        layoutRoot.set(tree.snapshot());
        if (dockable == getActiveDockable()) {
            activeDockable.set(neighbour == null ? null : dockablesById.get(neighbour));
            if (neighbour != null) {
                tree.select(neighbour);
                layoutRoot.set(tree.snapshot());
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
        return draggedId != null
                && targetId != null
                && position != null
                && dockablesById.containsKey(draggedId)
                && tree.canMove(draggedId, new DockingTree.DropTarget(targetId, position));
    }

    final boolean canInsertRelativeTo(String targetId, DockingPosition position) {
        return tree.canInsertRelativeTo(targetId, position);
    }

    final boolean moveInside(String draggedId, String targetId, DockingPosition position) {
        if (!canMoveInside(draggedId, targetId, position)
                || !tree.tryMove(draggedId, new DockingTree.DropTarget(targetId, position), 0.5)) {
            return false;
        }
        Dockable dockable = dockablesById.get(draggedId);
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockable);
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
        tree.insertRelativeTo(targetId, dockable, position, 0.5);
        dockablesById.put(dockable.getId(), dockable);
        layoutRoot.set(tree.snapshot());
        activeDockable.set(dockable);
    }

    final List<Dockable> getDockables() {
        return List.copyOf(dockablesById.values());
    }

    // The tab that stays selected when the given one is removed, or any remaining tab if it was alone in its group.
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
            String found = neighbourOf(split.getFirst(), id);
            return found != null ? found : neighbourOf(split.getSecond(), id);
        }
        return null;
    }

    private String remainingOutside(String id) {
        for (String other : tree.dockableIds()) {
            if (!other.equals(id)) {
                return other;
            }
        }
        return null;
    }
}
