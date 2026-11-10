package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.docking.model.DockingTree;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Owns the factories and the pane membership of a docking setup and enforces dockable ID uniqueness across all of its
 * panes. All model changes must happen on the JavaFX application thread.
 */
public class DockingContext {

    private final DockableFactory dockableFactory;
    private final DockingStageFactory stageFactory;
    private final Map<String, Dockable> dockablesById = new LinkedHashMap<>();
    private final Map<String, DockingPane> dockablePaneById = new LinkedHashMap<>();
    private final Set<DockingPane> panes = new LinkedHashSet<>();
    private final Map<DockingPane, String> paneIdByPane = new LinkedHashMap<>();
    private final Map<String, DockingPane> paneById = new LinkedHashMap<>();
    private int nextPaneNumber = 1;
    private final Map<DockingPane, Stage> floatingStageByPane = new LinkedHashMap<>();
    private final Map<Stage, DockingPane> floatingPaneByStage = new LinkedHashMap<>();
    private final Map<Stage, Set<Stage>> floatingStagesByHost = new LinkedHashMap<>();
    private final Map<Stage, Stage> hostStageByFloatingStage = new LinkedHashMap<>();
    private final Map<Stage, ChangeListener<Boolean>> hostListeners = new LinkedHashMap<>();
    private final ObservableList<Dockable> dockables = FXCollections.observableArrayList();
    private final ObservableList<Dockable> unmodifiableDockables = FXCollections.unmodifiableObservableList(dockables);
    private Predicate<Dockable> canCloseDockable = dockable -> true;
    private Predicate<DragEvent> dockableDropPredicate = event -> false;
    private Callback<DragEvent, Dockable> dockableDropFactory;
    private boolean suppressEmptyFloatingClose;

    /**
     * Creates a context. The stage factory is only stored for later floating-window support.
     *
     * @param dockableFactory resolves dockables on explicit open requests
     * @param stageFactory    creates stages hosting panes
     */
    public DockingContext(DockableFactory dockableFactory, DockingStageFactory stageFactory) {
        this.dockableFactory = Objects.requireNonNull(dockableFactory, "dockableFactory must not be null");
        this.stageFactory = Objects.requireNonNull(stageFactory, "stageFactory must not be null");
    }

    /**
     * Returns the factory used to resolve dockables by ID.
     *
     * @return the dockable factory
     */
    public final DockableFactory getDockableFactory() {
        return dockableFactory;
    }

    /**
     * Returns the factory used to create floating stages.
     *
     * @return the stage factory
     */
    public final DockingStageFactory getStageFactory() {
        return stageFactory;
    }

    private final ObjectProperty<Callback<Dockable, Node>> tabFactory = new SimpleObjectProperty<>(
            this, "tabFactory", dockable -> new DockingTab(this, dockable));

    /**
     * Factory creating the entire tab header for each dockable. The default creates a {@link DockingTab},
     * including its title, graphic, optional toolbar, actions menu and help button, and close button.
     * A {@code null} factory also uses this default.
     * <p>
     * The factory is invoked on the JavaFX application thread whenever tabs are rebuilt and must return
     * a fresh, unparented, non-null node on every invocation. Changing the factory refreshes existing
     * tabs in all panes of this context, including floating panes.
     * <p>
     * The workspace applies the {@code docking-tab} and {@code selected} style classes, accessibility,
     * keyboard actions, and docking drag handlers to the returned node. A custom header supplies its own
     * close button, if desired, invoking {@link #requestCloseDockable(Dockable)}.
     *
     * @return the tab factory property
     */
    public final ObjectProperty<Callback<Dockable, Node>> tabFactoryProperty() {
        return tabFactory;
    }

    public final Callback<Dockable, Node> getTabFactory() {
        return tabFactory.get();
    }

    public final void setTabFactory(Callback<Dockable, Node> factory) {
        tabFactory.set(factory);
    }

    /**
     * Returns the predicate recognizing external drags that should create dockables.
     *
     * @return the acceptance predicate, initially rejecting all events
     */
    public final Predicate<DragEvent> getDockableDropPredicate() {
        return dockableDropPredicate;
    }

    /**
     * Sets the lightweight predicate evaluated during drag-over and drop. It must not create dockables or
     * mutate the layout. Reject unrelated content drags so their events remain available to dockable content.
     *
     * @param predicate the non-null acceptance predicate
     */
    public final void setDockableDropPredicate(Predicate<DragEvent> predicate) {
        dockableDropPredicate = Objects.requireNonNull(predicate, "predicate must not be null");
    }

    /**
     * Returns the factory invoked only when an accepted external drag is dropped at a valid docking location.
     *
     * @return the drop factory, or {@code null} if external docking is disabled
     */
    public final Callback<DragEvent, Dockable> getDockableDropFactory() {
        return dockableDropFactory;
    }

    /**
     * Sets the factory creating a new dockable from the drop event. The dockable must have a unique ID in this
     * context. Returning {@code null} declines the drop without changing the layout.
     *
     * @param factory the drop factory, or {@code null} to disable external docking
     */
    public final void setDockableDropFactory(Callback<DragEvent, Dockable> factory) {
        dockableDropFactory = factory;
    }

    /**
     * Tests whether an external drag may create a dockable without invoking the drop factory.
     *
     * @param event the drag event
     * @return {@code true} if a factory is configured and the predicate accepts the event
     */
    public final boolean canAcceptDockableDrop(DragEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        return dockableDropFactory != null && dockableDropPredicate.test(event);
    }

    /**
     * Creates and opens a dockable from an accepted drop, selecting it at the requested location. The factory
     * is called once, after validating the target. Existing dockables are never replaced.
     *
     * @param pane the destination pane belonging to this context
     * @param event the drop event passed to the factory
     * @param targetId the target dockable ID, or {@code null} for an outer edge or an empty pane
     * @param position the requested docking position; CENTER with a null target requires an empty pane
     * @return the new dockable, or empty if the target or event is rejected or the factory declines
     * @throws IllegalStateException if called off the FX thread or the factory returns an already-open ID
     */
    public final Optional<Dockable> dropDockable(DockingPane pane, DragEvent event,
                                                String targetId, DockingPosition position) {
        requireFxThread();
        Objects.requireNonNull(event, "event must not be null");
        if (pane == null || pane.getDockingContext() != this || !paneIdByPane.containsKey(pane)
                || position == null
                || (targetId != null && !pane.canInsertRelativeTo(targetId, position))
                || (targetId == null && position == DockingPosition.CENTER
                    && !(pane.getLayoutRoot() instanceof DockingLayoutNode.Empty))
                || !canAcceptDockableDrop(event)) {
            return Optional.empty();
        }
        Dockable dockable = dockableDropFactory.call(event);
        if (dockable == null) {
            return Optional.empty();
        }
        String id = dockable.getId();
        if (dockablesById.containsKey(id)) {
            throw new IllegalStateException("Drop factory returned an already-open dockable ID: " + id);
        }
        dockablesById.put(id, dockable);
        dockablePaneById.put(id, pane);
        dockables.add(dockable);
        try {
            if (targetId != null) {
                pane.insertDockableRelativeTo(targetId, dockable, position);
            } else if (position == DockingPosition.CENTER) {
                pane.insertDockable(dockable, position);
            } else {
                pane.insertDockableAtPaneEdge(dockable, position);
            }
        } catch (RuntimeException | Error ex) {
            dockablesById.remove(id);
            dockablePaneById.remove(id);
            dockables.remove(dockable);
            throw ex;
        }
        return Optional.of(dockable);
    }

    /**
     * Creates a pane attached to this context.
     *
     * @return the new pane
     */
    public DockingPane createPane() {
        requireFxThread();
        return createPaneWithId(allocatePaneId());
    }

    /**
     * Opens the dockable with the given ID in the pane. If the ID is already open, the existing dockable is
     * activated instead. The dockable factory is only consulted for IDs that are not yet open.
     *
     * @param pane     a pane created by this context
     * @param id       the dockable ID
     * @param position the requested position
     * @return the opened or activated dockable, or empty if the factory cannot resolve the ID
     * @throws IllegalArgumentException if the pane belongs to another context
     * @throws IllegalStateException    if called off the FX thread or the factory returns a dockable with another ID
     */
    public Optional<Dockable> openDockable(DockingPane pane, String id, DockingPosition position) {
        requireFxThread();
        Objects.requireNonNull(pane, "pane must not be null");
        Objects.requireNonNull(id, "id must not be null");
        Objects.requireNonNull(position, "position must not be null");
        if (pane.getDockingContext() != this) {
            throw new IllegalArgumentException("pane belongs to a different docking context");
        }
        if (!paneIdByPane.containsKey(pane)) {
            attachPane(pane, allocatePaneId());
        }

        Dockable existing = dockablesById.get(id);
        if (existing != null) {
            dockablePaneById.get(id).selectDockable(id);
            return Optional.of(existing);
        }

        Optional<Dockable> created = Objects.requireNonNull(dockableFactory.create(id),
                "dockableFactory must not return null");
        if (created.isEmpty()) {
            return Optional.empty();
        }

        Dockable dockable = created.get();
        if (!id.equals(dockable.getId())) {
            throw new IllegalStateException("Factory returned dockable '" + dockable.getId() + "' for requested ID '" + id + "'");
        }

        dockablesById.put(id, dockable);
        dockablePaneById.put(id, pane);
        dockables.add(dockable);
        try {
            pane.insertDockable(dockable, position);
        } catch (RuntimeException | Error ex) {
            dockablesById.remove(id);
            dockablePaneById.remove(id);
            dockables.remove(dockable);
            throw ex;
        }
        return Optional.of(dockable);
    }

    /**
     * Creates a new floating pane containing only the given dockable and positions its stage at the screen
     * coordinates from the drag/drop event.
     *
     * @param id      the dockable ID
     * @param screenX target screen x coordinate
     * @param screenY target screen y coordinate
     * @return the floating pane
     */
    public DockingPane floatDockable(String id, double screenX, double screenY) {
        requireFxThread();
        Objects.requireNonNull(id, "id must not be null");
        Dockable dockable = dockablesById.get(id);
        DockingPane sourcePane = dockablePaneById.get(id);
        if (dockable == null || sourcePane == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }

        DockingPane floatingPane = createPaneWithId(allocatePaneId());
        Stage stage = null;
        try {
            stage = Objects.requireNonNull(stageFactory.create(floatingPane),
                    "stageFactory must not return null");
            if (!hosts(stage, floatingPane)) {
                throw new IllegalStateException("stageFactory must return a stage hosting the supplied pane");
            }
        } catch (RuntimeException | Error ex) {
            if (stage != null) {
                stage.close();
            }
            detachPane(floatingPane);
            throw ex;
        }

        sourcePane.removeDockable(dockable);
        floatingPane.insertDockable(dockable, DockingPosition.CENTER);
        dockablePaneById.put(id, floatingPane);
        registerFloatingStage(floatingPane, stage, hostingStage(sourcePane));
        stage.setX(screenX);
        stage.setY(screenY);
        closeFloatingStageIfEmpty(sourcePane);
        return floatingPane;
    }

    /**
     * Floats the entire tab group containing the given dockable in one window.
     * Tab order and the group's selected tab are preserved.
     *
     * @param id a dockable in the group to float
     * @param screenX target screen x coordinate
     * @param screenY target screen y coordinate
     * @return the floating pane
     * @throws IllegalArgumentException if the dockable is not open in this context
     */
    public final DockingPane floatTabGroup(String id, double screenX, double screenY) {
        requireFxThread();
        Objects.requireNonNull(id, "id must not be null");
        DockingPane source = dockablePaneById.get(id);
        if (source == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        DockingLayoutNode.Tabs group = findTabGroup(source.getLayoutRoot(), id);
        DockingPane floating = floatDockable(group.getDockableIds().get(0), screenX, screenY);
        moveRemainingTabs(floating, group);
        return floating;
    }

    /**
     * Captures the complete context-wide docking layout.
     *
     * @return immutable layout snapshot
     */
    public DockingLayout getLayout() {
        requireFxThread();
        if (panes.isEmpty()) {
            return new DockingLayout("primary", List.of(
                    new DockingLayout.PaneLayout("primary", new DockingLayoutNode.Empty(), null)));
        }

        List<DockingPane> nonFloatingPanes = nonFloatingPanes();
        if (nonFloatingPanes.size() != 1) {
            throw new IllegalStateException("Docking context must have exactly one non-floating pane to snapshot");
        }
        DockingPane primaryPane = nonFloatingPanes.get(0);

        List<DockingLayout.PaneLayout> paneLayouts = new ArrayList<>();
        for (DockingPane pane : panes) {
            String paneId = paneIdByPane.get(pane);
            if (paneId == null) {
                throw new IllegalStateException("Internal pane id mapping missing");
            }
            Stage floatingStage = floatingStageByPane.get(pane);
            DockingLayout.StageBounds bounds = floatingStage == null ? null : toStageBounds(floatingStage);
            paneLayouts.add(new DockingLayout.PaneLayout(paneId, pane.getLayoutRoot(), bounds));
        }
        return new DockingLayout(Objects.requireNonNull(paneIdByPane.get(primaryPane)), paneLayouts);
    }

    /**
     * Restores a complete context-wide docking layout.
     *
     * @param layout the layout to restore
     * @return restore result containing unresolved dockable IDs
     */
    public DockingRestoreResult restoreLayout(DockingLayout layout) {
        requireFxThread();
        Objects.requireNonNull(layout, "layout must not be null");

        validateLayout(layout);

        Map<String, Dockable> resolvedDockables = new LinkedHashMap<>();
        Set<String> unresolvedDockables = new LinkedHashSet<>();
        List<PanePlan> plans = new ArrayList<>();
        List<Stage> replacementStages = new ArrayList<>();
        DockingPane mountedPrimaryPane = resolveMountedPrimaryPaneForRestore();
        DockingLayout.PaneLayout primaryLayout = findPrimaryLayout(layout);
        DockingLayoutNode normalizedPrimaryRoot = null;

        try {
            for (DockingLayout.PaneLayout paneLayout : layout.getPanes()) {
                DockingLayoutNode normalized = normalizeNode(paneLayout.getRoot(), resolvedDockables, unresolvedDockables);
                boolean empty = normalized instanceof DockingLayoutNode.Empty;
                if (empty && paneLayout.getStageBounds() != null) {
                    continue;
                }
                if (empty && !paneLayout.getPaneId().equals(layout.getPrimaryPaneId())) {
                    continue;
                }

                if (paneLayout == primaryLayout) {
                    normalizedPrimaryRoot = normalized;
                    plans.add(PanePlan.primary(paneLayout.getPaneId()));
                    continue;
                }

                DockingPane replacementPane = new DockingPane(this);
                if (!(normalized instanceof DockingLayoutNode.Empty)) {
                    buildPaneTree(replacementPane, normalized, resolvedDockables);
                }
                Stage replacementStage = null;
                DockingLayout.StageBounds stageBounds = paneLayout.getStageBounds();
                if (stageBounds != null) {
                    replacementStage = Objects.requireNonNull(stageFactory.create(replacementPane),
                            "stageFactory must not return null");
                    replacementStages.add(replacementStage);
                    if (!hosts(replacementStage, replacementPane)) {
                        throw new IllegalStateException("stageFactory must return a stage hosting the supplied pane");
                    }
                    replacementStage.setX(stageBounds.getX());
                    replacementStage.setY(stageBounds.getY());
                    replacementStage.setWidth(stageBounds.getWidth());
                    replacementStage.setHeight(stageBounds.getHeight());
                }
                plans.add(PanePlan.floating(paneLayout.getPaneId(), replacementPane, replacementStage));
            }
        } catch (RuntimeException | Error ex) {
            for (Stage stage : replacementStages) {
                stage.close();
            }
            throw ex;
        }

        List<Stage> oldFloatingStages = new ArrayList<>(floatingPaneByStage.keySet());
        for (Map.Entry<Stage, ChangeListener<Boolean>> entry : new ArrayList<>(hostListeners.entrySet())) {
            entry.getKey().showingProperty().removeListener(entry.getValue());
        }

        dockablesById.clear();
        dockablePaneById.clear();
        dockables.clear();
        panes.clear();
        paneIdByPane.clear();
        paneById.clear();
        floatingStageByPane.clear();
        floatingPaneByStage.clear();
        floatingStagesByHost.clear();
        hostStageByFloatingStage.clear();
        hostListeners.clear();
        nextPaneNumber = 1;

        DockingPane restoredPrimaryPane = mountedPrimaryPane == null ? new DockingPane(this) : mountedPrimaryPane;
        clearPane(restoredPrimaryPane);
        if (!(normalizedPrimaryRoot instanceof DockingLayoutNode.Empty)) {
            buildPaneTree(restoredPrimaryPane, normalizedPrimaryRoot, resolvedDockables);
        }
        attachPane(restoredPrimaryPane, primaryLayout.getPaneId());
        for (Dockable dockable : restoredPrimaryPane.getDockables()) {
            dockablesById.put(dockable.getId(), dockable);
            dockablePaneById.put(dockable.getId(), restoredPrimaryPane);
            dockables.add(dockable);
        }

        for (PanePlan plan : plans) {
            if (plan.primary) {
                continue;
            }
            attachPane(plan.pane, plan.paneId);
            for (Dockable dockable : plan.pane.getDockables()) {
                dockablesById.put(dockable.getId(), dockable);
                dockablePaneById.put(dockable.getId(), plan.pane);
                dockables.add(dockable);
            }
            if (plan.stage != null) {
                Stage hostStage = hostingStage(restoredPrimaryPane);
                registerFloatingStage(plan.pane, plan.stage,
                        hostStage == null ? ownerOf(plan.stage) : hostStage);
                plan.stage.show();
            }
        }

        for (Stage stage : oldFloatingStages) {
            stage.close();
        }

        return new DockingRestoreResult(new ArrayList<>(unresolvedDockables));
    }

    /**
     * Closes the dockable if the close policy permits it. A veto leaves the panes and this context unchanged.
     *
     * @param dockable the dockable to close
     * @return {@code true} only if the dockable was open and has been removed
     * @throws IllegalStateException if called off the FX thread
     */
    public boolean requestCloseDockable(Dockable dockable) {
        requireFxThread();
        Objects.requireNonNull(dockable, "dockable must not be null");
        return closeDockableInternal(dockable, true);
    }

    /**
     * Returns an unmodifiable view of all open dockables of this context.
     *
     * @return the open dockables
     */
    public final ObservableList<Dockable> getDockables() {
        return unmodifiableDockables;
    }

    /**
     * Sets the policy deciding whether a dockable may close; {@code true} means it may close.
     *
     * @param policy the close policy
     */
    public final void setCanCloseDockable(Predicate<Dockable> policy) {
        this.canCloseDockable = Objects.requireNonNull(policy, "policy must not be null");
    }

    /**
     * Evaluates the close policy.
     *
     * @param dockable the dockable
     * @return {@code true} if the dockable may close
     */
    public final boolean canCloseDockable(Dockable dockable) {
        return canCloseDockable.test(Objects.requireNonNull(dockable, "dockable must not be null"));
    }

    /**
     * Tests whether a dockable's entire tab group can move to the given target.
     * A {@code null} target ID addresses a pane-wide edge, or an empty pane for a center drop.
     * Drops back onto the source group are rejected.
     *
     * @param targetPane the destination pane in this context
     * @param id a dockable in the source group
     * @param targetId a dockable in the target group, or {@code null} for the whole pane
     * @param position the docking position
     * @return whether the move is valid
     */
    public final boolean canMoveTabGroup(DockingPane targetPane, String id, String targetId, DockingPosition position) {
        requireFxThread();
        DockingPane source = dockablePaneById.get(id);
        if (source == null || targetPane == null || targetPane.getDockingContext() != this
                || !paneIdByPane.containsKey(targetPane) || position == null) {
            return false;
        }
        DockingLayoutNode.Tabs group = findTabGroup(source.getLayoutRoot(), id);
        if (targetId == null) {
            if (targetPane.getLayoutRoot() instanceof DockingLayoutNode.Empty) {
                return true;
            }
            return position != DockingPosition.CENTER
                    && (source != targetPane || group.getDockableIds().size() < source.getDockables().size());
        }
        return !group.getDockableIds().contains(targetId)
                && canMoveDockable(targetPane, group.getDockableIds().get(0), targetId, position);
    }

    /**
     * Moves a dockable's entire tab group, preserving its order and selected tab.
     * Center drops append the tabs to the target group; edge drops create one new group.
     *
     * @param targetPane the destination pane in this context
     * @param id a dockable in the source group
     * @param targetId a dockable in the target group, or {@code null} for a pane-wide edge or empty pane
     * @param position the docking position
     * @return {@code true} if the move completed, or {@code false} without changes for an invalid target
     */
    public final boolean moveTabGroup(DockingPane targetPane, String id, String targetId, DockingPosition position) {
        if (!canMoveTabGroup(targetPane, id, targetId, position)) {
            return false;
        }
        DockingLayoutNode.Tabs group = findTabGroup(dockablePaneById.get(id).getLayoutRoot(), id);
        String first = group.getDockableIds().get(0);
        if (targetId == null) {
            moveDockableToPaneEdge(targetPane, first,
                    position == DockingPosition.CENTER ? DockingPosition.LEFT : position);
        } else {
            moveDockable(targetPane, first, targetId, position);
        }
        moveRemainingTabs(targetPane, group);
        return true;
    }

    private void moveRemainingTabs(DockingPane targetPane, DockingLayoutNode.Tabs group) {
        List<String> ids = group.getDockableIds();
        for (int i = 1; i < ids.size(); i++) {
            moveDockable(targetPane, ids.get(i), ids.get(0), DockingPosition.CENTER);
        }
        targetPane.selectDockable(group.getSelectedId());
    }

    private static DockingLayoutNode.Tabs findTabGroup(DockingLayoutNode node, String id) {
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) node;
            return tabs.getDockableIds().contains(id) ? tabs : null;
        }
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            DockingLayoutNode.Tabs first = findTabGroup(split.getFirst(), id);
            return first != null ? first : findTabGroup(split.getSecond(), id);
        }
        return null;
    }

    final boolean canMoveDockable(DockingPane targetPane, String draggedId, String targetId, DockingPosition position) {
        requireFxThread();
        if (targetPane == null || targetPane.getDockingContext() != this) {
            return false;
        }
        DockingPane sourcePane = dockablePaneById.get(draggedId);
        if (sourcePane == null) {
            return false;
        }
        if (sourcePane == targetPane) {
            return targetPane.canMoveInside(draggedId, targetId, position);
        }
        return draggedId != null
                && targetId != null
                && !draggedId.equals(targetId)
                && targetPane.canInsertRelativeTo(targetId, position);
    }

    final boolean moveDockable(DockingPane targetPane, String draggedId, String targetId, DockingPosition position) {
        requireFxThread();
        if (!canMoveDockable(targetPane, draggedId, targetId, position)) {
            return false;
        }

        DockingPane sourcePane = dockablePaneById.get(draggedId);
        if (sourcePane == targetPane) {
            return targetPane.moveInside(draggedId, targetId, position);
        }
        Dockable dockable = sourcePane.removeDockableById(draggedId);
        if (dockable == null) {
            return false;
        }
        targetPane.insertDockableRelativeTo(targetId, dockable, position);
        dockablePaneById.put(draggedId, targetPane);
        closeFloatingStageIfEmpty(sourcePane);
        return true;
    }

    final boolean canMoveDockableToPaneEdge(DockingPane targetPane, String id, DockingPosition edge) {
        requireFxThread();
        if (targetPane == null || targetPane.getDockingContext() != this || !paneIdByPane.containsKey(targetPane)
                || edge == null || edge == DockingPosition.CENTER) {
            return false;
        }
        DockingPane sourcePane = dockablePaneById.get(id);
        return sourcePane != null
                && (sourcePane != targetPane || targetPane.canMoveInsideToPaneEdge(id, edge));
    }

    final boolean moveDockableToPaneEdge(DockingPane targetPane, String id, DockingPosition edge) {
        if (!canMoveDockableToPaneEdge(targetPane, id, edge)) {
            return false;
        }
        DockingPane sourcePane = dockablePaneById.get(id);
        if (sourcePane == targetPane) {
            return targetPane.moveInsideToPaneEdge(id, edge);
        }
        Dockable dockable = sourcePane.removeDockableById(id);
        targetPane.insertDockableAtPaneEdge(dockable, edge);
        dockablePaneById.put(id, targetPane);
        closeFloatingStageIfEmpty(sourcePane);
        return true;
    }

    private void buildPaneTree(DockingPane pane, DockingLayoutNode root, Map<String, Dockable> dockablesById) {
        String seed = firstDockableId(root);
        pane.insertDockable(requireDockable(seed, dockablesById), DockingPosition.CENTER);
        expandNode(pane, root, seed, dockablesById);
        applyDividerPositions(pane, root, List.of());
    }

    private void expandNode(DockingPane pane, DockingLayoutNode node, String seedId, Map<String, Dockable> dockablesById) {
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) node;
            List<String> ids = tabs.getDockableIds();
            for (int i = 1; i < ids.size(); i++) {
                pane.insertDockableRelativeTo(seedId, requireDockable(ids.get(i), dockablesById), DockingPosition.CENTER);
            }
            pane.selectDockable(tabs.getSelectedId());
            return;
        }
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
        String secondSeed = firstDockableId(split.getSecond());
        DockingPosition edge = split.getOrientation() == Orientation.HORIZONTAL ? DockingPosition.RIGHT : DockingPosition.BOTTOM;
        pane.insertDockableRelativeTo(seedId, requireDockable(secondSeed, dockablesById), edge);
        expandNode(pane, split.getFirst(), seedId, dockablesById);
        expandNode(pane, split.getSecond(), secondSeed, dockablesById);
    }

    private void applyDividerPositions(DockingPane pane, DockingLayoutNode node, List<Integer> path) {
        if (!(node instanceof DockingLayoutNode.Split)) {
            return;
        }
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
        pane.updateDividerPosition(path, split.getDividerPosition());

        List<Integer> firstPath = new ArrayList<>(path);
        firstPath.add(0);
        applyDividerPositions(pane, split.getFirst(), firstPath);

        List<Integer> secondPath = new ArrayList<>(path);
        secondPath.add(1);
        applyDividerPositions(pane, split.getSecond(), secondPath);
    }

    private String firstDockableId(DockingLayoutNode node) {
        if (node instanceof DockingLayoutNode.Tabs) {
            return ((DockingLayoutNode.Tabs) node).getDockableIds().get(0);
        }
        if (node instanceof DockingLayoutNode.Split) {
            return firstDockableId(((DockingLayoutNode.Split) node).getFirst());
        }
        throw new IllegalArgumentException("Cannot build pane tree from empty layout node");
    }

    private Dockable requireDockable(String id, Map<String, Dockable> resolvedDockables) {
        Dockable dockable = resolvedDockables.get(id);
        if (dockable == null) {
            throw new IllegalStateException("Resolved dockable missing for id: " + id);
        }
        return dockable;
    }

    private DockingLayoutNode normalizeNode(DockingLayoutNode node,
                                            Map<String, Dockable> resolvedDockables,
                                            Set<String> unresolvedDockables) {
        if (node instanceof DockingLayoutNode.Empty) {
            return node;
        }
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) node;
            List<String> resolvedIds = new ArrayList<>();
            for (String id : tabs.getDockableIds()) {
                Dockable dockable = resolveDockable(id, resolvedDockables, unresolvedDockables);
                if (dockable != null) {
                    resolvedIds.add(id);
                }
            }
            if (resolvedIds.isEmpty()) {
                return new DockingLayoutNode.Empty();
            }
            String selected = resolvedIds.contains(tabs.getSelectedId()) ? tabs.getSelectedId() : resolvedIds.get(0);
            return new DockingLayoutNode.Tabs(resolvedIds, selected);
        }

        DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
        DockingTree.validateSnapshotNode(split);
        DockingLayoutNode first = normalizeNode(split.getFirst(), resolvedDockables, unresolvedDockables);
        DockingLayoutNode second = normalizeNode(split.getSecond(), resolvedDockables, unresolvedDockables);

        if (first instanceof DockingLayoutNode.Empty && second instanceof DockingLayoutNode.Empty) {
            return new DockingLayoutNode.Empty();
        }
        if (first instanceof DockingLayoutNode.Empty) {
            return second;
        }
        if (second instanceof DockingLayoutNode.Empty) {
            return first;
        }
        return new DockingLayoutNode.Split(split.getOrientation(), split.getDividerPosition(), first, second);
    }

    private Dockable resolveDockable(String id, Map<String, Dockable> resolvedDockables, Set<String> unresolvedDockables) {
        Dockable existing = resolvedDockables.get(id);
        if (existing != null) {
            return existing;
        }
        if (unresolvedDockables.contains(id)) {
            return null;
        }

        Optional<Dockable> created = Objects.requireNonNull(dockableFactory.create(id),
                "dockableFactory must not return null");
        if (created.isEmpty()) {
            unresolvedDockables.add(id);
            return null;
        }

        Dockable dockable = created.get();
        if (!id.equals(dockable.getId())) {
            throw new IllegalStateException("Factory returned dockable '" + dockable.getId() + "' for requested ID '" + id + "'");
        }
        resolvedDockables.put(id, dockable);
        return dockable;
    }

    private void validateLayout(DockingLayout layout) {
        List<DockingLayout.PaneLayout> paneLayouts = layout.getPanes();
        if (paneLayouts.isEmpty()) {
            throw new IllegalArgumentException("layout must contain at least one pane");
        }

        int nullBounds = 0;
        boolean foundPrimary = false;
        Set<String> dockableIds = new LinkedHashSet<>();
        for (DockingLayout.PaneLayout paneLayout : paneLayouts) {
            DockingLayoutNode root = paneLayout.getRoot();
            DockingTree.validateSnapshotNode(root);
            collectDockableIds(root, dockableIds);

            boolean primary = paneLayout.getPaneId().equals(layout.getPrimaryPaneId());
            boolean hasBounds = paneLayout.getStageBounds() != null;
            if (!hasBounds) {
                nullBounds++;
                if (primary) {
                    foundPrimary = true;
                }
            }
            if (primary && hasBounds) {
                throw new IllegalArgumentException("Primary pane must not define stage bounds");
            }
            if (!primary && !hasBounds) {
                throw new IllegalArgumentException("Only the primary pane may omit stage bounds");
            }
        }

        if (nullBounds != 1 || !foundPrimary) {
            throw new IllegalArgumentException("Layout must contain exactly one primary pane with null stage bounds");
        }
    }

    private void collectDockableIds(DockingLayoutNode node, Set<String> ids) {
        if (node instanceof DockingLayoutNode.Empty) {
            return;
        }
        if (node instanceof DockingLayoutNode.Tabs) {
            for (String id : ((DockingLayoutNode.Tabs) node).getDockableIds()) {
                if (!ids.add(id)) {
                    throw new IllegalArgumentException("Duplicate dockable id in layout: " + id);
                }
            }
            return;
        }
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
        collectDockableIds(split.getFirst(), ids);
        collectDockableIds(split.getSecond(), ids);
    }

    private List<DockingPane> nonFloatingPanes() {
        List<DockingPane> nonFloatingPanes = new ArrayList<>();
        for (DockingPane pane : panes) {
            if (!floatingStageByPane.containsKey(pane)) {
                nonFloatingPanes.add(pane);
            }
        }
        return nonFloatingPanes;
    }

    private DockingPane resolveMountedPrimaryPaneForRestore() {
        List<DockingPane> nonFloating = nonFloatingPanes();
        if (nonFloating.isEmpty()) {
            return null;
        }
        List<DockingPane> mounted = new ArrayList<>();
        for (DockingPane pane : nonFloating) {
            if (pane.getScene() != null) {
                mounted.add(pane);
            }
        }
        if (mounted.size() > 1) {
            throw new IllegalStateException("Cannot restore with more than one mounted non-floating pane");
        }
        if (mounted.size() == 1) {
            return mounted.get(0);
        }
        if (nonFloating.size() > 1) {
            throw new IllegalStateException("Cannot restore with more than one non-floating pane");
        }
        return nonFloating.get(0);
    }

    private DockingLayout.PaneLayout findPrimaryLayout(DockingLayout layout) {
        for (DockingLayout.PaneLayout paneLayout : layout.getPanes()) {
            if (paneLayout.getPaneId().equals(layout.getPrimaryPaneId())) {
                return paneLayout;
            }
        }
        throw new IllegalArgumentException("Primary pane id '" + layout.getPrimaryPaneId() + "' not present in layout");
    }

    private void clearPane(DockingPane pane) {
        for (Dockable dockable : new ArrayList<>(pane.getDockables())) {
            pane.removeDockable(dockable);
        }
    }

    private DockingLayout.StageBounds toStageBounds(Stage stage) {
        double width = stage.getWidth();
        double height = stage.getHeight();
        if (!(Double.isFinite(width) && width > 0) && stage.getScene() != null) {
            width = stage.getScene().getWidth();
        }
        if (!(Double.isFinite(height) && height > 0) && stage.getScene() != null) {
            height = stage.getScene().getHeight();
        }
        if (!(Double.isFinite(width) && width > 0)) {
            width = 1;
        }
        if (!(Double.isFinite(height) && height > 0)) {
            height = 1;
        }
        return new DockingLayout.StageBounds(stage.getX(), stage.getY(), width, height);
    }

    private DockingPane createPaneWithId(String paneId) {
        DockingPane pane = new DockingPane(this);
        attachPane(pane, paneId);
        return pane;
    }

    private String allocatePaneId() {
        String id;
        do {
            id = "pane-" + nextPaneNumber++;
        } while (paneById.containsKey(id));
        return id;
    }

    private void attachPane(DockingPane pane, String paneId) {
        if (paneById.containsKey(paneId) && paneById.get(paneId) != pane) {
            throw new IllegalStateException("Duplicate pane id: " + paneId);
        }
        panes.add(pane);
        paneIdByPane.put(pane, paneId);
        paneById.put(paneId, pane);
        bumpPaneCounter(paneId);
    }

    private void detachPane(DockingPane pane) {
        String paneId = paneIdByPane.remove(pane);
        if (paneId != null) {
            paneById.remove(paneId);
        }
        panes.remove(pane);
    }

    private void bumpPaneCounter(String paneId) {
        if (paneId.startsWith("pane-")) {
            try {
                int parsed = Integer.parseInt(paneId.substring("pane-".length()));
                if (parsed >= nextPaneNumber) {
                    nextPaneNumber = parsed + 1;
                }
            } catch (NumberFormatException ignored) {
                // non-numeric suffix is allowed and does not influence generated IDs
            }
        }
    }

    private boolean closeDockableInternal(Dockable dockable, boolean checkPolicy) {
        String id = dockable.getId();
        if (dockablesById.get(id) != dockable) {
            return false;
        }
        if (checkPolicy && !canCloseDockable(dockable)) {
            return false;
        }
        DockingPane pane = dockablePaneById.remove(id);
        if (pane == null) {
            return false;
        }
        pane.removeDockable(dockable);
        dockablesById.remove(id);
        dockables.remove(dockable);
        closeFloatingStageIfEmpty(pane);
        return true;
    }

    private void registerFloatingStage(DockingPane pane, Stage floatingStage, Stage hostStage) {
        floatingStageByPane.put(pane, floatingStage);
        floatingPaneByStage.put(floatingStage, pane);
        floatingStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,
                event -> handleFloatingStageCloseRequest(floatingStage, event));
        floatingStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> unregisterFloatingStage(floatingStage));
        Stage registeredHost = hostStage == null ? ownerOf(floatingStage) : hostStage;
        if (registeredHost != null) {
            hostStageByFloatingStage.put(floatingStage, registeredHost);
            registerHostStage(registeredHost, floatingStage);
        }
    }

    private void handleFloatingStageCloseRequest(Stage stage, WindowEvent event) {
        DockingPane pane = floatingPaneByStage.get(stage);
        if (pane == null) {
            return;
        }
        List<Dockable> paneDockables = new ArrayList<>(pane.getDockables());
        for (Dockable dockable : paneDockables) {
            if (!canCloseDockable(dockable)) {
                event.consume();
                return;
            }
        }

        suppressEmptyFloatingClose = true;
        try {
            for (Dockable dockable : paneDockables) {
                closeDockableInternal(dockable, false);
            }
        } finally {
            suppressEmptyFloatingClose = false;
        }
        unregisterFloatingStage(stage);
    }

    private void closeFloatingStageIfEmpty(DockingPane pane) {
        if (suppressEmptyFloatingClose) {
            return;
        }
        Stage stage = floatingStageByPane.get(pane);
        if (stage != null && pane.getDockables().isEmpty()) {
            stage.close();
        }
    }

    private void registerHostStage(Stage hostStage, Stage floatingStage) {
        if (hostStage == null) {
            return;
        }
        Set<Stage> hosted = floatingStagesByHost.computeIfAbsent(hostStage, stage -> {
            ChangeListener<Boolean> listener = (obs, oldShowing, newShowing) -> {
                if (!newShowing) {
                    closeHostedFloatingStages(hostStage);
                }
            };
            hostStage.showingProperty().addListener(listener);
            hostListeners.put(hostStage, listener);
            return new LinkedHashSet<>();
        });
        hosted.add(floatingStage);
    }

    private void closeHostedFloatingStages(Stage hostStage) {
        Set<Stage> hosted = floatingStagesByHost.get(hostStage);
        if (hosted == null) {
            return;
        }
        for (Stage stage : new ArrayList<>(hosted)) {
            stage.close();
        }
    }

    private void unregisterFloatingStage(Stage stage) {
        DockingPane pane = floatingPaneByStage.remove(stage);
        if (pane == null) {
            return;
        }
        suppressEmptyFloatingClose = true;
        try {
            for (Dockable dockable : new ArrayList<>(pane.getDockables())) {
                closeDockableInternal(dockable, false);
            }
        } finally {
            suppressEmptyFloatingClose = false;
        }
        floatingStageByPane.remove(pane);
        detachPane(pane);

        Stage host = hostStageByFloatingStage.remove(stage);
        if (host != null) {
            Set<Stage> hosted = floatingStagesByHost.get(host);
            if (hosted != null) {
                hosted.remove(stage);
                if (hosted.isEmpty()) {
                    floatingStagesByHost.remove(host);
                    ChangeListener<Boolean> listener = hostListeners.remove(host);
                    if (listener != null) {
                        host.showingProperty().removeListener(listener);
                    }
                }
            }
        }
    }

    private static Stage hostingStage(DockingPane pane) {
        if (pane.getScene() == null) {
            return null;
        }
        Window window = pane.getScene().getWindow();
        return window instanceof Stage ? (Stage) window : null;
    }

    private static Stage ownerOf(Stage stage) {
        Window owner = stage.getOwner();
        return owner instanceof Stage ? (Stage) owner : null;
    }

    private static boolean hosts(Stage stage, DockingPane pane) {
        return stage.getScene() != null && contains(stage.getScene().getRoot(), pane);
    }

    private static boolean contains(javafx.scene.Node root, DockingPane pane) {
        if (root == pane) {
            return true;
        }
        if (!(root instanceof javafx.scene.Parent)) {
            return false;
        }
        for (javafx.scene.Node child : ((javafx.scene.Parent) root).getChildrenUnmodifiable()) {
            if (contains(child, pane)) {
                return true;
            }
        }
        return false;
    }

    static void requireFxThread() {
        if (!Platform.isFxApplicationThread()) {
            throw new IllegalStateException("Docking model changes must happen on the JavaFX application thread");
        }
    }

    private static final class PanePlan {
        private final String paneId;
        private final DockingPane pane;
        private final Stage stage;
        private final boolean primary;

        private PanePlan(String paneId, DockingPane pane, Stage stage, boolean primary) {
            this.paneId = paneId;
            this.pane = pane;
            this.stage = stage;
            this.primary = primary;
        }

        private static PanePlan primary(String paneId) {
            return new PanePlan(paneId, null, null, true);
        }

        private static PanePlan floating(String paneId, DockingPane pane, Stage stage) {
            return new PanePlan(paneId, pane, stage, false);
        }
    }
}
