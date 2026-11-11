package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.docking.model.DockingTree;
import com.dlsc.gemsfx.skins.DockingPaneSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Control associated with exactly one {@link DockingContext}. It owns one pane tree and renders each dockable as a
 * title tab with a close affordance; only the selected tab's content is displayed.
 * Hold Shift when starting a tab drag to move its entire tab group, preserving tab order and selection.
 */
public class DockingPane extends Control {

    private static final PseudoClass SHOW_FOCUS_BORDER = PseudoClass.getPseudoClass("show-focus-border");

    private final DockingContext dockingContext;

    private final DockingTree tree = new DockingTree();
    private final Map<String, Dockable> dockablesById = new HashMap<>();

    DockingPane(DockingContext dockingContext) {
        this.dockingContext = Objects.requireNonNull(dockingContext, "dockingContext must not be null");
        getStyleClass().add("docking-pane");
        pseudoClassStateChanged(SHOW_FOCUS_BORDER, isShowFocusBorder());
        AccessibilityUtil.setRole(this, AccessibleRole.NODE, ResourceBundleManager.getString(
                ResourceBundleManager.BundleType.DOCKING_PANE, "accessible.role-description", "Docking pane"));
        setPlaceholder(new DockingPanePlaceholder());
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

    private final BooleanProperty showFocusBorder = new SimpleBooleanProperty(this, "showFocusBorder", true) {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(SHOW_FOCUS_BORDER, get());
        }
    };

    /**
     * Whether to highlight the border of the tab group containing keyboard focus while its window is focused.
     * Defaults to {@code false}. Enabling this reserves a border around all tab groups so that moving focus
     * between groups does not shift their contents. It does not change tab selection or the active dockable.
     *
     * @return the show focus border property
     */
    public final BooleanProperty showFocusBorderProperty() {
        return showFocusBorder;
    }

    public final boolean isShowFocusBorder() {
        return showFocusBorder.get();
    }

    public final void setShowFocusBorder(boolean showFocusBorder) {
        this.showFocusBorder.set(showFocusBorder);
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
        tree.restore(root);
        dockablesById.clear();
        for (String id : tree.dockableIds()) {
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

    final boolean canMoveInsideToPaneEdge(String id, DockingPosition edge) {
        return tree.canMoveToPaneEdge(id, edge);
    }

    final boolean moveInsideToPaneEdge(String id, DockingPosition edge) {
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
        tree.insertRelativeTo(targetId, dockable, position, 0.5);
        dockablesById.put(dockable.getId(), dockable);
        layoutRoot.set(tree.snapshot());
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
        for (String other : tree.dockableIds()) {
            if (!other.equals(id)) {
                return other;
            }
        }
        return null;
    }
}
