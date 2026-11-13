package com.dlsc.gemsfx.docking;

import javafx.event.Event;
import javafx.event.EventType;

/**
 * Notification that a dockable has been opened, closed, or moved. Events are
 * delivered synchronously to handlers registered on the owning context.
 */
public class DockingEvent extends Event {

    /** The common supertype of all docking lifecycle events. */
    public static final EventType<DockingEvent> ANY = new EventType<>(Event.ANY, "DOCKING_EVENT");
    /** A dockable was added to the context. */
    public static final EventType<DockingEvent> OPENED = new EventType<>(ANY, "DOCKABLE_OPENED");
    /** A dockable was removed; it has not yet been disposed. */
    public static final EventType<DockingEvent> CLOSED = new EventType<>(ANY, "DOCKABLE_CLOSED");
    /** A dockable was moved or reordered. */
    public static final EventType<DockingEvent> MOVED = new EventType<>(ANY, "DOCKABLE_MOVED");

    private final Dockable dockable;
    private final DockingPane oldPane;
    private final DockingPane newPane;

    DockingEvent(DockingContext source, EventType<DockingEvent> type, Dockable dockable,
                 DockingPane oldPane, DockingPane newPane) {
        super(source, null, type);
        this.dockable = dockable;
        this.oldPane = oldPane;
        this.newPane = newPane;
    }

    /**
     * Returns the affected dockable.
     *
     * @return the dockable
     */
    public final Dockable getDockable() {
        return dockable;
    }

    /**
     * Returns the source pane, or {@code null} for an open event.
     *
     * @return the source pane
     */
    public final DockingPane getOldPane() {
        return oldPane;
    }

    /**
     * Returns the destination pane, or {@code null} for a close event.
     *
     * @return the destination pane
     */
    public final DockingPane getNewPane() {
        return newPane;
    }
}
