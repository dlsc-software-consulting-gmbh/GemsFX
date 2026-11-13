package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.docking.model.DockingTree;
import com.dlsc.gemsfx.docking.model.DockingTabGroup;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Owns the factories and the pane membership of a docking setup and enforces dockable ID uniqueness across all of its
 * panes. All model changes must happen on the JavaFX application thread.
 * Docking decisions are logged at {@link java.util.logging.Level#FINE FINE} level using this class's logger.
 */
public class DockingContext {

    private static final Logger LOG = Logger.getLogger(DockingContext.class.getName());

    private final DockableFactory dockableFactory;
    private final DockingStageFactory stageFactory;
    private final Map<String, Dockable> dockablesById = new LinkedHashMap<>();
    private final Set<Dockable> closeRequestsInProgress = Collections.newSetFromMap(new IdentityHashMap<>());
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
    private final Map<EventType<?>, List<EventHandler<?>>> eventHandlers = new LinkedHashMap<>();
    private final DockingSelectionModel selectionModel = new DockingSelectionModel(this);
    private int restoreDepth;

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

    final <T> T withSelectionUpdates(Supplier<T> operation) {
        requireFxThread();
        if (restoreDepth > 0) {
            return operation.get();
        }
        try {
            return operation.get();
        } finally {
            updateSelectionModel();
        }
    }

    private void updateSelectionModel() {
        List<Dockable> selected = new ArrayList<>();
        for (DockingPane pane : panes) {
            selected.addAll(pane.selectedGroups().values());
        }
        selectionModel.update(selected);
    }

    /**
     * Returns the selection model spanning all panes in this context.
     *
     * @return the docking selection model
     */
    public final DockingSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Registers a handler for a docking event type and its subtypes. Handlers are called
     * synchronously on the JavaFX application thread. They may perform nested docking operations;
     * handler exceptions are logged and do not interrupt other handlers or required cleanup.
     * Layout restoration does not emit docking events.
     *
     * @param eventType the event type
     * @param handler the handler
     * @param <E> the event class
     * @throws IllegalStateException if called off the JavaFX application thread
     */
    public final <E extends Event> void addEventHandler(EventType<E> eventType, EventHandler<? super E> handler) {
        requireFxThread();
        eventHandlers.computeIfAbsent(Objects.requireNonNull(eventType), ignored -> new ArrayList<>())
                .add(Objects.requireNonNull(handler));
    }

    /**
     * Removes a previously registered event handler.
     *
     * @param eventType the event type
     * @param handler the handler
     * @param <E> the event class
     * @throws IllegalStateException if called off the JavaFX application thread
     */
    public final <E extends Event> void removeEventHandler(EventType<E> eventType, EventHandler<? super E> handler) {
        requireFxThread();
        List<EventHandler<?>> handlers = eventHandlers.get(Objects.requireNonNull(eventType));
        if (handlers != null) {
            handlers.remove(Objects.requireNonNull(handler));
            if (handlers.isEmpty()) {
                eventHandlers.remove(eventType);
            }
        }
    }

    private void fireDockingEvent(Event event) {
        if (restoreDepth > 0) {
            return;
        }
        updateSelectionModel();
        dispatchDockingEvent(event);
    }

    @SuppressWarnings("unchecked")
    private void dispatchDockingEvent(Event event) {
        for (EventType<?> type = event.getEventType(); type != null; type = type.getSuperType()) {
            List<EventHandler<?>> handlers = eventHandlers.get(type);
            if (handlers == null) {
                continue;
            }
            for (EventHandler<?> handler : new ArrayList<>(handlers)) {
                try {
                    ((EventHandler<Event>) handler).handle(event);
                } catch (Exception ex) {
                    LOG.log(java.util.logging.Level.WARNING, "Docking event handler failed", ex);
                }
            }
        }
    }

    DockingPane findDockingPane(Dockable dockable) {
        String id = dockable.getId();
        DockingPane pane = dockablePaneById.get(id);
        return dockablesById.get(id) == dockable && pane != null && pane.containsDockable(id) ? pane : null;
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

    private final BooleanProperty floatingEnabled = new SimpleBooleanProperty(this, "floatingEnabled", true);

    /**
     * Controls whether dragging a dockable or tab group outside all panes creates a floating
     * window. When disabled, the drop leaves the layout unchanged and the default tab group menu
     * hides its float action. The default is {@code true}. This setting does not restrict explicit
     * programmatic floating operations or layout restoration.
     *
     * @return the floating-enabled property
     */
    public final BooleanProperty floatingEnabledProperty() {
        return floatingEnabled;
    }

    public final boolean isFloatingEnabled() {
        return floatingEnabled.get();
    }

    public final void setFloatingEnabled(boolean enabled) {
        floatingEnabled.set(enabled);
    }

    private final ObjectProperty<Callback<DockingContext, DockingPane>> paneFactory =
            new SimpleObjectProperty<>(this, "paneFactory", DockingPane::new);

    /**
     * Factory used to create panes for this context, including floating and restored panes.
     * The default creates a {@link DockingPane}. Each invocation must return a fresh, unparented,
     * non-null pane belonging to the supplied context. The factory itself must not be {@code null}.
     * It is invoked on the JavaFX application thread. Changes apply only to subsequently created panes.
     * A primary pane already in use is retained when restoring a layout.
     *
     * @return the pane factory property
     */
    public final ObjectProperty<Callback<DockingContext, DockingPane>> paneFactoryProperty() {
        return paneFactory;
    }

    public final Callback<DockingContext, DockingPane> getPaneFactory() {
        return paneFactory.get();
    }

    public final void setPaneFactory(Callback<DockingContext, DockingPane> factory) {
        paneFactory.set(factory);
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
     * The workspace applies the {@code docking-tab} style class, the {@code selected} pseudo-class, accessibility,
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

    private final ObjectProperty<BiFunction<DockingPane, List<Dockable>, Node>> tabPaneHeaderFactory =
            new SimpleObjectProperty<>(this, "tabPaneHeaderFactory", DockingTabPaneHeader::new);

    /**
     * Factory for an optional node at the right edge of each tab group's header strip.
     * It receives the owning pane and an immutable snapshot of the group's dockables in tab order.
     * The returned node is wrapped in a stack pane styled as an unselected tab. The factory or
     * its result may be {@code null}; otherwise each invocation must return a fresh, unparented node.
     * The default creates a {@link DockingTabPaneHeader} for closing, floating internally or in a window,
     * docking internally floating groups back into their pane, and maximizing docked groups.
     * <p>
     * The factory is invoked on the JavaFX application thread when groups are rebuilt, including
     * when their membership changes. Changing it refreshes existing groups in all panes.
     *
     * @return the tab pane header factory property
     */
    public final ObjectProperty<BiFunction<DockingPane, List<Dockable>, Node>> tabPaneHeaderFactoryProperty() {
        return tabPaneHeaderFactory;
    }

    public final BiFunction<DockingPane, List<Dockable>, Node> getTabPaneHeaderFactory() {
        return tabPaneHeaderFactory.get();
    }

    public final void setTabPaneHeaderFactory(BiFunction<DockingPane, List<Dockable>, Node> factory) {
        tabPaneHeaderFactory.set(factory);
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
        if (dockableDropFactory == null) {
            return logDecision("external drag", null, null, null, null, false, "no drop factory is configured");
        }
        boolean accepted = dockableDropPredicate.test(event);
        return logDecision("external drag", null, null, null, null, accepted,
                accepted ? "drop predicate accepted the event" : "drop predicate rejected the event");
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
        return withSelectionUpdates(() -> dropDockableInternal(pane, event, targetId, position));
    }

    private Optional<Dockable> dropDockableInternal(DockingPane pane, DragEvent event,
                                                   String targetId, DockingPosition position) {
        requireFxThread();
        Objects.requireNonNull(event, "event must not be null");
        if (pane == null || pane.getDockingContext() != this || !paneIdByPane.containsKey(pane)) {
            logDecision("external drop", pane, null, targetId, position, false, "destination pane is missing or not attached to this context");
            return Optional.empty();
        }
        if (pane.getMaximizedDockable() != null) {
            logDecision("external drop", pane, null, targetId, position, false, "destination pane has a maximized group");
            return Optional.empty();
        }
        if (position == null) {
            logDecision("external drop", pane, null, targetId, position, false, "docking position is missing");
            return Optional.empty();
        }
        if (targetId != null && !pane.canInsertRelativeTo(targetId, position)) {
            logDecision("external drop", pane, null, targetId, position, false, "target dockable is not present in the destination pane");
            return Optional.empty();
        }
        if (targetId == null && position == DockingPosition.CENTER
                && !(pane.getLayoutRoot() instanceof DockingLayoutNode.Empty)) {
            logDecision("external drop", pane, null, null, position, false, "center drop without a target requires an empty pane");
            return Optional.empty();
        }
        if (!canAcceptDockableDrop(event)) {
            logDecision("external drop", pane, null, targetId, position, false, "external drag was rejected; see preceding decision");
            return Optional.empty();
        }
        Dockable dockable = dockableDropFactory.call(event);
        if (dockable == null) {
            logDecision("external drop", pane, null, targetId, position, false, "drop factory declined to create a dockable");
            return Optional.empty();
        }
        ensureNotClosing(dockable);
        String id = dockable.getId();
        if (dockablesById.containsKey(id)) {
            logDecision("external drop", pane, id, targetId, position, false, "dockable ID is already open");
            throw new IllegalStateException("Drop factory returned an already-open dockable ID: " + id);
        }
        dockablesById.put(id, dockable);
        dockablePaneById.put(id, pane);
        dockable.setDockingContext(this);
        dockables.add(dockable);
        try {
            if (targetId != null) {
                pane.insertDockableRelativeTo(targetId, dockable, position);
            } else if (position == DockingPosition.CENTER) {
                pane.insertDockableAtPaneEdge(dockable, DockingPosition.LEFT);
            } else {
                pane.insertDockableAtPaneEdge(dockable, position);
            }
        } catch (RuntimeException | Error ex) {
            dockablesById.remove(id);
            dockablePaneById.remove(id);
            dockable.setDockingContext(null);
            dockables.remove(dockable);
            LOG.fine(() -> "External drop of dockable '" + id + "' failed while inserting: " + ex);
            throw ex;
        }
        fireDockingEvent(new DockingEvent(this, DockingEvent.OPENED, dockable, null, pane));
        logDecision("external drop", pane, id, targetId, position, true, "dockable was created and inserted");
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
     * @throws IllegalStateException if called off the FX thread, the factory returns a dockable with another ID,
     *                               or the returned instance is currently being closed
     */
    public Optional<Dockable> openDockable(DockingPane pane, String id, DockingPosition position) {
        return withSelectionUpdates(() -> openDockableInternal(pane, id, position));
    }

    private Optional<Dockable> openDockableInternal(DockingPane pane, String id, DockingPosition position) {
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
            logDecision("open", dockablePaneById.get(id), id, null, position, true, "dockable was already open and has been selected");
            return Optional.of(existing);
        }

        Optional<Dockable> created = Objects.requireNonNull(dockableFactory.create(id),
                "dockableFactory must not return null");
        if (created.isEmpty()) {
            logDecision("open", pane, id, null, position, false, "dockable factory could not resolve the ID");
            return Optional.empty();
        }

        Dockable dockable = created.get();
        ensureNotClosing(dockable);
        if (!id.equals(dockable.getId())) {
            logDecision("open", pane, id, null, position, false, "dockable factory returned a different ID: " + dockable.getId());
            throw new IllegalStateException("Factory returned dockable '" + dockable.getId() + "' for requested ID '" + id + "'");
        }

        dockablesById.put(id, dockable);
        dockablePaneById.put(id, pane);
        dockable.setDockingContext(this);
        dockables.add(dockable);
        try {
            pane.insertDockable(dockable, position);
        } catch (RuntimeException | Error ex) {
            dockablesById.remove(id);
            dockablePaneById.remove(id);
            dockable.setDockingContext(null);
            dockables.remove(dockable);
            LOG.fine(() -> "Opening dockable '" + id + "' failed while inserting: " + ex);
            throw ex;
        }
        fireDockingEvent(new DockingEvent(this, DockingEvent.OPENED, dockable, null, pane));
        logDecision("open", pane, id, null, position, true, "dockable was created and inserted");
        return Optional.of(dockable);
    }

    /**
     * Creates a new floating pane containing only the given dockable and positions its stage at the screen
     * coordinates from the drag/drop event.
     *
     * @param id      the dockable ID
     * @param screenX target screen x coordinate
     * @param screenY target screen y coordinate
     * @return the floating pane, or {@code null} if the source pane has a maximized group
     */
    public DockingPane floatDockable(String id, double screenX, double screenY) {
        return withSelectionUpdates(() -> floatDockableInternal(id, screenX, screenY));
    }

    private DockingPane floatDockableInternal(String id, double screenX, double screenY) {
        requireFxThread();
        Objects.requireNonNull(id, "id must not be null");
        Dockable dockable = dockablesById.get(id);
        DockingPane sourcePane = dockablePaneById.get(id);
        if (dockable == null || sourcePane == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        if (sourcePane.getMaximizedDockable() != null) {
            return null;
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
        dockablePaneById.put(id, floatingPane);
        floatingPane.insertDockable(dockable, DockingPosition.CENTER);
        registerFloatingStage(floatingPane, stage, hostingStage(sourcePane));
        stage.setX(screenX);
        stage.setY(screenY);
        closeFloatingStageIfEmpty(sourcePane);
        fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, dockable, sourcePane, floatingPane));
        return floatingPane;
    }

    /**
     * Floats the entire tab group containing the given dockable in one window.
     * Tab order and the group's selected tab are preserved.
     *
     * @param id a dockable in the group to float
     * @param screenX target screen x coordinate
     * @param screenY target screen y coordinate
     * @return the floating pane, or {@code null} if the source pane has a maximized group
     * @throws IllegalArgumentException if the dockable is not open in this context
     */
    public final DockingPane floatTabGroup(String id, double screenX, double screenY) {
        requireFxThread();
        Objects.requireNonNull(id, "id must not be null");
        DockingPane source = dockablePaneById.get(id);
        if (source == null) {
            throw new IllegalArgumentException("Unknown dockable id: " + id);
        }
        if (source.getMaximizedDockable() != null) {
            return null;
        }
        DockingLayoutNode.Tabs group = source.tabGroupSnapshot(id);
        DockingTabGroup sourceGroup = source.groupOf(id);
        DockingPane floating = floatDockable(group.getDockableIds().get(0), screenX, screenY);
        if (floating != null) {
            moveRemainingTabs(source, sourceGroup, floating, group);
        }
        return floating;
    }

    /**
     * Moves one open dockable into an internal floating frame in its current pane.
     * Does not create a window and is independent of {@link #floatingEnabledProperty()}.
     * Does nothing while the pane has a maximized group.
     *
     * @param id the open dockable ID
     * @param bounds requested bounds relative to the pane's content area
     * @throws IllegalArgumentException if the ID or geometry is invalid
     */
    public final void floatDockableInternally(String id, Rectangle2D bounds) {
        floatInternally(id, bounds, false);
    }

    /**
     * Moves an open dockable's entire tab group into an internal floating frame,
     * preserving tab order and selection. Does nothing while the pane has a maximized group.
     *
     * @param id any open dockable in the group
     * @param bounds requested bounds relative to the pane's content area
     * @throws IllegalArgumentException if the ID or geometry is invalid
     */
    public final void floatTabGroupInternally(String id, Rectangle2D bounds) {
        floatInternally(id, bounds, true);
    }

    private void floatInternally(String id, Rectangle2D bounds, boolean wholeGroup) {
        withSelectionUpdates(() -> {
            requireFxThread();
            DockingPane pane = dockablePaneById.get(Objects.requireNonNull(id, "id must not be null"));
            if (pane == null) {
                throw new IllegalArgumentException("Unknown dockable id: " + id);
            }
            if (pane.getMaximizedDockable() != null) {
                return null;
            }
            DockingLayoutNode.Tabs source = pane.tabGroupSnapshot(id);
            List<String> movedIds = wholeGroup ? source.getDockableIds() : List.of(id);
            pane.floatInternally(id, bounds, wholeGroup);
            DockingTabGroup floatedGroup = pane.groupOf(movedIds.get(0));
            for (String movedId : movedIds) {
                Dockable moved = dockablesById.get(movedId);
                if (moved != null && dockablePaneById.get(movedId) == pane
                        && pane.groupOf(movedId) == floatedGroup) {
                    fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, moved, pane, pane));
                }
            }
            return null;
        });
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
            paneLayouts.add(new DockingLayout.PaneLayout(paneId, pane.getLayoutRoot(), bounds, pane.getFloatingGroups()));
        }
        return new DockingLayout(Objects.requireNonNull(paneIdByPane.get(primaryPane)), paneLayouts);
    }

    /**
     * Restores a complete context-wide docking layout.
     * Restore replaces the current layout regardless of close permissions and disposes dockable
     * instances that are not reused in the restored layout.
     *
     * @param layout the layout to restore
     * @return restore result containing unresolved dockable IDs
     */
    public DockingRestoreResult restoreLayout(DockingLayout layout) {
        restoreDepth++;
        try {
            return restoreLayoutInternal(layout);
        } finally {
            restoreDepth--;
            if (restoreDepth == 0) {
                updateSelectionModel();
            }
        }
    }

    private DockingRestoreResult restoreLayoutInternal(DockingLayout layout) {
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
        Map<String, List<DockingFloatingGroup>> normalizedFloating = new LinkedHashMap<>();
        Set<DockingPane> replacementPanes = Collections.newSetFromMap(new IdentityHashMap<>());
        DockingPane restoredPrimaryPane;

        try {
            for (DockingLayout.PaneLayout paneLayout : layout.getPanes()) {
                DockingLayoutNode normalized = normalizeNode(paneLayout.getRoot(), resolvedDockables, unresolvedDockables);
                List<DockingFloatingGroup> floating = new ArrayList<>();
                for (DockingFloatingGroup group : paneLayout.getFloatingGroups()) {
                    DockingLayoutNode tabs = normalizeNode(group.getTabs(), resolvedDockables, unresolvedDockables);
                    if (tabs instanceof DockingLayoutNode.Tabs) {
                        floating.add(group.withTabs((DockingLayoutNode.Tabs) tabs));
                    }
                }
                normalizedFloating.put(paneLayout.getPaneId(), floating);
                boolean empty = normalized instanceof DockingLayoutNode.Empty && floating.isEmpty();
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

                DockingPane replacementPane = createFactoryPane();
                if (!replacementPanes.add(replacementPane)) {
                    throw new IllegalArgumentException("paneFactory must return a fresh pane");
                }
                replacementPane.restoreLayout(normalized, floating, resolvedDockables);
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
            restoredPrimaryPane = mountedPrimaryPane == null ? createFactoryPane() : mountedPrimaryPane;
            if (mountedPrimaryPane == null && !replacementPanes.add(restoredPrimaryPane)) {
                throw new IllegalArgumentException("paneFactory must return a fresh pane");
            }
        } catch (RuntimeException | Error ex) {
            for (Stage stage : replacementStages) {
                stage.close();
            }
            throw ex;
        }

        List<Dockable> oldDockables = new ArrayList<>(dockables);
        List<Stage> oldFloatingStages = new ArrayList<>(floatingPaneByStage.keySet());
        for (Map.Entry<Stage, ChangeListener<Boolean>> entry : new ArrayList<>(hostListeners.entrySet())) {
            entry.getKey().showingProperty().removeListener(entry.getValue());
        }

        dockablesById.clear();
        dockablePaneById.clear();
        for (Dockable dockable : oldDockables) {
            dockable.setDockingContext(null);
        }
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

        clearPane(restoredPrimaryPane);
        Set<String> primaryIds = new LinkedHashSet<>();
        collectDockableIds(normalizedPrimaryRoot, primaryIds);
        for (DockingFloatingGroup group : normalizedFloating.get(primaryLayout.getPaneId())) {
            collectDockableIds(group.getTabs(), primaryIds);
        }
        for (String id : primaryIds) {
            Dockable dockable = resolvedDockables.get(id);
            dockablesById.put(id, dockable);
            dockablePaneById.put(id, restoredPrimaryPane);
            dockable.setDockingContext(this);
        }
        restoredPrimaryPane.restoreLayout(normalizedPrimaryRoot,
                normalizedFloating.get(primaryLayout.getPaneId()), resolvedDockables);
        attachPane(restoredPrimaryPane, primaryLayout.getPaneId());
        for (Dockable dockable : restoredPrimaryPane.getDockables()) {
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
                dockable.setDockingContext(this);
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
        for (Dockable dockable : oldDockables) {
            if (dockablesById.get(dockable.getId()) != dockable) {
                dockable.dispose();
            }
        }

        return new DockingRestoreResult(new ArrayList<>(unresolvedDockables));
    }

    /**
     * Closes the dockable if its closeable property, close policy, and close request callback permit
     * it. A veto leaves the panes and this context unchanged. A successfully removed dockable is disposed.
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
     * Evaluates the dockable's close permission and the context's close policy without invoking
     * the dockable's close request callback.
     *
     * @param dockable the dockable
     * @return {@code true} if the dockable may close
     */
    public final boolean canCloseDockable(Dockable dockable) {
        Objects.requireNonNull(dockable, "dockable must not be null");
        return dockable.isCloseable() && canCloseDockable.test(dockable);
    }

    /**
     * Tests whether a dockable's entire tab group can move to the given target.
     * A {@code null} target ID addresses a pane-wide edge, or an empty pane for a center drop.
     * Drops back onto the source group are rejected, as are moves involving a pane with a maximized group.
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
        if (source == null) {
            return logDecision("tab group", targetPane, id, targetId, position, false, "source dockable is not open");
        }
        if (targetPane == null || targetPane.getDockingContext() != this || !paneIdByPane.containsKey(targetPane)) {
            return logDecision("tab group", targetPane, id, targetId, position, false, "destination pane is missing or not attached to this context");
        }
        if (source.getMaximizedDockable() != null || targetPane.getMaximizedDockable() != null) {
            return logDecision("tab group", targetPane, id, targetId, position, false, "source or destination pane has a maximized group");
        }
        if (position == null) {
            return logDecision("tab group", targetPane, id, targetId, null, false, "docking position is missing");
        }
        DockingLayoutNode.Tabs group = source.tabGroupSnapshot(id);
        if (targetId == null) {
            if (targetPane.getLayoutRoot() instanceof DockingLayoutNode.Empty) {
                return logDecision("tab group", targetPane, id, null, position, true, "destination pane is empty");
            }
            if (position == DockingPosition.CENTER) {
                return logDecision("tab group", targetPane, id, null, position, false, "center drop without a target requires an empty pane");
            }
            if (source == targetPane && group.getDockableIds().size() >= source.getDockables().size()) {
                return logDecision("tab group", targetPane, id, null, position, false, "the group already occupies the entire pane");
            }
            return logDecision("tab group", targetPane, id, null, position, true, "group can move to the pane edge");
        }
        if (group.getDockableIds().contains(targetId)) {
            return logDecision("tab group", targetPane, id, targetId, position, false, "target is in the same tab group");
        }
        boolean allowed = canMoveDockable(targetPane, group.getDockableIds().get(0), targetId, position);
        return logDecision("tab group", targetPane, id, targetId, position, allowed,
                allowed ? "group can move relative to the target" : "target cannot accept the group; see preceding decision");
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
        DockingLayoutNode.Tabs group = dockablePaneById.get(id).tabGroupSnapshot(id);
        String first = group.getDockableIds().get(0);
        DockingPane source = dockablePaneById.get(id);
        DockingTabGroup sourceGroup = source.groupOf(id);
        boolean moved;
        if (targetId == null) {
            moved = moveDockableToPaneEdge(targetPane, first,
                    position == DockingPosition.CENTER ? DockingPosition.LEFT : position);
        } else {
            moved = moveDockable(targetPane, first, targetId, position);
        }
        return moved && moveRemainingTabs(source, sourceGroup, targetPane, group);
    }

    private boolean moveRemainingTabs(DockingPane sourcePane, DockingTabGroup sourceGroup,
                                      DockingPane targetPane, DockingLayoutNode.Tabs group) {
        List<String> ids = group.getDockableIds();
        DockingTabGroup targetGroup = targetPane.groupOf(ids.get(0));
        if (targetGroup == null || dockablePaneById.get(ids.get(0)) != targetPane) {
            return false;
        }
        for (int i = 1; i < ids.size(); i++) {
            for (int j = 0; j < i; j++) {
                if (dockablePaneById.get(ids.get(j)) != targetPane || targetPane.groupOf(ids.get(j)) != targetGroup) {
                    return false;
                }
            }
            for (int j = i; j < ids.size(); j++) {
                if (dockablePaneById.get(ids.get(j)) != sourcePane || sourcePane.groupOf(ids.get(j)) != sourceGroup) {
                    return false;
                }
            }
            if (!moveDockable(targetPane, ids.get(i), ids.get(0), DockingPosition.CENTER)) {
                return false;
            }
        }
        for (String id : ids) {
            if (dockablePaneById.get(id) != targetPane || targetPane.groupOf(id) != targetGroup) {
                return false;
            }
        }
        targetPane.selectDockable(group.getSelectedId());
        return true;
    }

    final boolean canMoveDockable(DockingPane targetPane, String draggedId, String targetId, DockingPosition position) {
        requireFxThread();
        if (targetPane == null || targetPane.getDockingContext() != this) {
            return logDecision("dockable", targetPane, draggedId, targetId, position, false, "destination pane is missing or belongs to another context");
        }
        DockingPane sourcePane = dockablePaneById.get(draggedId);
        if (sourcePane == null) {
            return logDecision("dockable", targetPane, draggedId, targetId, position, false, "source dockable is not open");
        }
        if (sourcePane.getMaximizedDockable() != null || targetPane.getMaximizedDockable() != null) {
            return logDecision("dockable", targetPane, draggedId, targetId, position, false, "source or destination pane has a maximized group");
        }
        boolean allowed;
        if (sourcePane == targetPane) {
            allowed = targetPane.canMoveInside(draggedId, targetId, position);
        } else {
            allowed = draggedId != null
                    && targetId != null
                    && !draggedId.equals(targetId)
                    && targetPane.canInsertRelativeTo(targetId, position);
        }
        String reason;
        if (allowed) {
            reason = sourcePane == targetPane ? "valid move within the pane" : "target pane can accept the dockable";
        } else if (position == null) {
            reason = "docking position is missing";
        } else if (targetId == null) {
            reason = "target dockable ID is missing";
        } else if (draggedId.equals(targetId)) {
            reason = "a dockable cannot be docked relative to itself";
        } else {
            reason = "target dockable is not present in the destination pane";
        }
        return logDecision("dockable", targetPane, draggedId, targetId, position, allowed, reason);
    }

    final boolean moveDockable(DockingPane targetPane, String draggedId, String targetId, DockingPosition position) {
        return withSelectionUpdates(() -> moveDockableInternal(targetPane, draggedId, targetId, position));
    }

    private boolean moveDockableInternal(DockingPane targetPane, String draggedId, String targetId, DockingPosition position) {
        requireFxThread();
        if (!canMoveDockable(targetPane, draggedId, targetId, position)) {
            return false;
        }

        DockingPane sourcePane = dockablePaneById.get(draggedId);
        if (sourcePane == targetPane) {
            Dockable dockable = dockablesById.get(draggedId);
            DockingLayoutNode.Tabs before = targetPane.tabGroupSnapshot(draggedId);
            boolean sameGroup = position == DockingPosition.CENTER && before.getDockableIds().contains(targetId);
            boolean moved = targetPane.moveInside(draggedId, targetId, position);
            if (moved && (!sameGroup || !before.getDockableIds().equals(
                    targetPane.tabGroupSnapshot(draggedId).getDockableIds()))) {
                fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, dockable, sourcePane, targetPane));
            }
            return moved;
        }
        Dockable dockable = sourcePane.removeDockableById(draggedId);
        if (dockable == null) {
            return false;
        }
        dockablePaneById.put(draggedId, targetPane);
        targetPane.insertDockableRelativeTo(targetId, dockable, position);
        closeFloatingStageIfEmpty(sourcePane);
        fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, dockable, sourcePane, targetPane));
        return true;
    }

    final boolean canMoveDockableToPaneEdge(DockingPane targetPane, String id, DockingPosition edge) {
        requireFxThread();
        if (targetPane == null || targetPane.getDockingContext() != this || !paneIdByPane.containsKey(targetPane)) {
            return logDecision("pane edge", targetPane, id, null, edge, false, "destination pane is missing or not attached to this context");
        }
        if (edge == null || edge == DockingPosition.CENTER) {
            return logDecision("pane edge", targetPane, id, null, edge, false, "position must be a pane edge");
        }
        DockingPane sourcePane = dockablePaneById.get(id);
        if (sourcePane == null) {
            return logDecision("pane edge", targetPane, id, null, edge, false, "source dockable is not open");
        }
        if (sourcePane.getMaximizedDockable() != null || targetPane.getMaximizedDockable() != null) {
            return logDecision("pane edge", targetPane, id, null, edge, false, "source or destination pane has a maximized group");
        }
        boolean allowed = sourcePane != targetPane || targetPane.canMoveInsideToPaneEdge(id, edge);
        return logDecision("pane edge", targetPane, id, null, edge, allowed,
                allowed ? "dockable can move to the pane edge" : "a lone dockable cannot move to its own pane edge");
    }

    private boolean logDecision(String operation, DockingPane pane, String id, String targetId,
                                DockingPosition position, boolean allowed, String reason) {
        LOG.fine(() -> "Docking " + operation + " " + (allowed ? "allowed" : "rejected")
                + " [dockable=" + id + ", sourcePane=" + paneIdByPane.get(dockablePaneById.get(id))
                + ", pane=" + (pane == null ? null : paneIdByPane.getOrDefault(pane, "<unattached>"))
                + ", target=" + targetId + ", position=" + position + "]: " + reason);
        return allowed;
    }

    final boolean moveDockableToPaneEdge(DockingPane targetPane, String id, DockingPosition edge) {
        return withSelectionUpdates(() -> moveDockableToPaneEdgeInternal(targetPane, id, edge));
    }

    private boolean moveDockableToPaneEdgeInternal(DockingPane targetPane, String id, DockingPosition edge) {
        if (!canMoveDockableToPaneEdge(targetPane, id, edge)) {
            return false;
        }
        DockingPane sourcePane = dockablePaneById.get(id);
        if (sourcePane == targetPane) {
            Dockable dockable = dockablesById.get(id);
            boolean moved = targetPane.moveInsideToPaneEdge(id, edge);
            if (moved) {
                fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, dockable, sourcePane, targetPane));
            }
            return moved;
        }
        Dockable dockable = sourcePane.removeDockableById(id);
        dockablePaneById.put(id, targetPane);
        targetPane.insertDockableAtPaneEdge(dockable, edge);
        closeFloatingStageIfEmpty(sourcePane);
        fireDockingEvent(new DockingEvent(this, DockingEvent.MOVED, dockable, sourcePane, targetPane));
        return true;
    }

    /**
     * Prepares a saved subtree for restore by removing dockables the factory cannot resolve and
     * choosing a surviving selected tab when necessary. Empty split children are removed together
     * with their preceding divider (the following divider for the first child), transferring their
     * space to a neighbor. Splits with fewer than two surviving children are collapsed.
     */
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
        List<DockingLayoutNode> children = new ArrayList<>();
        for (DockingLayoutNode child : split.getChildren()) {
            children.add(normalizeNode(child, resolvedDockables, unresolvedDockables));
        }
        List<Double> positions = new ArrayList<>(split.getDividerPositions());
        for (int i = children.size() - 1; i >= 0; i--) {
            if (children.get(i) instanceof DockingLayoutNode.Empty) {
                children.remove(i);
                if (!positions.isEmpty()) {
                    positions.remove(i == 0 ? 0 : i - 1);
                }
            }
        }
        if (children.isEmpty()) {
            return new DockingLayoutNode.Empty();
        }
        if (children.size() == 1) {
            return children.get(0);
        }
        return new DockingLayoutNode.Split(split.getOrientation(), children, positions);
    }

    /**
     * Resolves an ID through the factory, caching both successful and unsuccessful lookups for the
     * current restore. Factory results must retain the requested ID so the saved layout and the
     * context's ownership mappings refer to the same dockable.
     */
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
        ensureNotClosing(dockable);
        if (!id.equals(dockable.getId())) {
            throw new IllegalStateException("Factory returned dockable '" + dockable.getId() + "' for requested ID '" + id + "'");
        }
        resolvedDockables.put(id, dockable);
        return dockable;
    }

    /**
     * Rejects invalid trees, context-wide duplicate dockable IDs, and inconsistent primary/floating
     * pane metadata before restore invokes factories or changes the live workspace. Exactly the
     * designated primary pane must omit stage bounds; every other pane represents a floating window.
     */
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
            for (DockingFloatingGroup group : paneLayout.getFloatingGroups()) {
                DockingTree.validateSnapshotNode(group.getTabs());
                collectDockableIds(group.getTabs(), dockableIds);
            }

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
        for (DockingLayoutNode child : split.getChildren()) {
            collectDockableIds(child, ids);
        }
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

    /**
     * Chooses the existing primary pane to reuse so restore preserves its placement in the
     * application's scene graph. A scene-attached non-floating pane takes precedence over detached
     * panes; ambiguous candidates are rejected rather than silently replacing the wrong workspace.
     * Returns {@code null} when no non-floating pane exists yet.
     */
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

    /**
     * Captures floating-window bounds even when the stage has not acquired usable dimensions yet.
     * Missing or invalid dimensions fall back to the scene size, then to one, so the snapshot always
     * satisfies the positive, finite size requirements of {@link DockingLayout.StageBounds}.
     */
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
        DockingPane pane = createFactoryPane();
        attachPane(pane, paneId);
        return pane;
    }

    private DockingPane createFactoryPane() {
        Callback<DockingContext, DockingPane> factory = Objects.requireNonNull(getPaneFactory(),
                "paneFactory must not be null");
        DockingPane pane = Objects.requireNonNull(factory.call(this), "paneFactory must not return null");
        if (pane.getDockingContext() != this || panes.contains(pane) || pane.getParent() != null || pane.getScene() != null) {
            throw new IllegalArgumentException("paneFactory must return a fresh, unparented pane belonging to this context");
        }
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

    /**
     * Removes an owned dockable from its pane and all context registries, closing an emptied floating
     * window when appropriate. Identity checking prevents a stale instance from closing another
     * dockable with the same ID. Window teardown passes {@code false} for {@code checkPolicy} after
     * prechecking all tabs or when the window has already been hidden.
     */
    private boolean closeDockableInternal(Dockable dockable, boolean checkPolicy) {
        return withSelectionUpdates(() -> closeDockableImpl(dockable, checkPolicy));
    }

    private boolean closeDockableImpl(Dockable dockable, boolean checkPolicy) {
        String id = dockable.getId();
        if (dockablesById.get(id) != dockable) {
            return false;
        }
        if (checkPolicy && !canCloseDockable(dockable)) {
            return false;
        }
        if (dockablesById.get(id) != dockable) {
            return false;
        }
        if (checkPolicy && !approveCloseRequest(dockable)) {
            return false;
        }
        if (dockablesById.get(id) != dockable || (checkPolicy && !dockable.isCloseable())) {
            return false;
        }
        DockingPane pane = dockablePaneById.remove(id);
        if (pane == null) {
            return false;
        }
        dockable.setClosing(true);
        try {
            dockable.setDockingContext(null);
            pane.removeDockable(dockable);
            dockablesById.remove(id);
            dockables.remove(dockable);
            try {
                fireDockingEvent(new DockingEvent(this, DockingEvent.CLOSED, dockable, pane, null));
            } finally {
                dockable.dispose();
            }
        } finally {
            try {
                closeFloatingStageIfEmpty(pane);
            } finally {
                dockable.setClosing(false);
            }
        }
        return true;
    }

    private static void ensureNotClosing(Dockable dockable) {
        if (dockable.isClosing()) {
            throw new IllegalStateException("Cannot reopen a dockable while it is closing");
        }
    }

    /**
     * Invokes a dockable's close request callback at most once at a time, including when a modal
     * confirmation dialog runs a nested JavaFX event loop.
     */
    private boolean approveCloseRequest(Dockable dockable) {
        if (closeRequestsInProgress.contains(dockable)) {
            return false;
        }
        BooleanSupplier callback = dockable.getOnCloseRequest();
        if (callback == null) {
            return true;
        }
        closeRequestsInProgress.add(dockable);
        try {
            return callback.getAsBoolean();
        } finally {
            closeRequestsInProgress.remove(dockable);
        }
    }

    /**
     * Connects a floating pane to its stage and installs the close/hidden handlers that keep window
     * lifetime and context ownership in sync. A floating source passes on its logical host so closing
     * an emptied source window cannot close its replacement. Without a source, the stage's owner is used.
     */
    private void registerFloatingStage(DockingPane pane, Stage floatingStage, Stage hostStage) {
        floatingStageByPane.put(pane, floatingStage);
        floatingPaneByStage.put(floatingStage, pane);
        floatingStage.addEventHandler(WindowEvent.WINDOW_CLOSE_REQUEST,
                event -> handleFloatingStageCloseRequest(floatingStage, event));
        floatingStage.addEventHandler(WindowEvent.WINDOW_HIDDEN, event -> unregisterFloatingStage(floatingStage));
        Stage registeredHost = hostStage == null ? ownerOf(floatingStage) : hostStage;
        if (floatingPaneByStage.containsKey(registeredHost)) {
            registeredHost = hostStageByFloatingStage.get(registeredHost);
        }
        if (registeredHost != null) {
            hostStageByFloatingStage.put(floatingStage, registeredHost);
            registerHostStage(registeredHost, floatingStage);
        }
    }

    /**
     * Prechecks every tab's close permission and request callback before removing any dockable, so a
     * veto leaves the whole window intact. After approval, callbacks are not evaluated again and
     * automatic empty-window closure is suppressed while its dockables are being removed.
     */
    private void handleFloatingStageCloseRequest(Stage stage, WindowEvent event) {
        DockingPane pane = floatingPaneByStage.get(stage);
        if (pane == null) {
            return;
        }
        List<Dockable> paneDockables = new ArrayList<>(pane.getDockables());
        for (Dockable dockable : paneDockables) {
            if (!canCloseDockable(dockable) || !matchesFloatingCloseTarget(stage, pane, paneDockables)) {
                event.consume();
                return;
            }
        }
        for (Dockable dockable : paneDockables) {
            if (!approveCloseRequest(dockable) || !matchesFloatingCloseTarget(stage, pane, paneDockables)) {
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

    /**
     * Rejects stale approvals if a callback changed the floating window's membership or ownership.
     */
    private boolean matchesFloatingCloseTarget(Stage stage, DockingPane pane, List<Dockable> dockables) {
        if (floatingPaneByStage.get(stage) != pane || pane.getDockables().size() != dockables.size()) {
            return false;
        }
        for (Dockable dockable : dockables) {
            if (!dockable.isCloseable() || dockablesById.get(dockable.getId()) != dockable
                    || dockablePaneById.get(dockable.getId()) != pane) {
                return false;
            }
        }
        return true;
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

    /**
     * Tracks floating windows by their logical host and installs one showing listener per host.
     * Hiding the host closes its tracked windows, including windows not natively owned by that stage.
     * The listener is retained for removal when the last tracked floating window is unregistered.
     */
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

    /**
     * Tears down context state when a floating window is hidden or its close request is approved.
     * Remaining dockables are removed without policy checks because the window is already hidden or
     * approval has been obtained. Removing the stage mapping first makes repeated calls harmless;
     * suppressing empty-window closure prevents recursive teardown. The host listener is released
     * when its last tracked window is removed.
     */
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
