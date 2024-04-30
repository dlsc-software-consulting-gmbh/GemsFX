package com.dlsc.gemsfx.infocenter;

import javafx.event.Event;
import javafx.event.EventType;

import java.util.Objects;

/**
 * An event class used by the {@link InfoCenterView}, especially in combination with
 * the {@link InfoCenterPane}. The pane can react to events fired by the view so that
 * the info center can be shown or hidden when needed / requested.
 */
public class InfoCenterEvent extends Event {

    public static final EventType<InfoCenterEvent> ANY = new EventType<>(Event.ANY, "ANY");

    /**
     * The user pressed on / chose the given notification.
     */
    public static final EventType<InfoCenterEvent> NOTIFICATION_CHOSEN = new EventType<>(ANY, "NOTIFICATION_CHOSEN");

    /**
     * A notification was added.
     */
    public static final EventType<InfoCenterEvent> NOTIFICATION_ADDED = new EventType<>(ANY, "NOTIFICATION_ADDED");

    /**
     * A notification was removed.
     */
    public static final EventType<InfoCenterEvent> NOTIFICATION_REMOVED = new EventType<>(ANY, "NOTIFICATION_REMOVED");

    /**
     * An explicit request to hide the info center.
     */
    public static final EventType<InfoCenterEvent> HIDE = new EventType<>(ANY, "HIDE");

    private Notification<?> notification;

    /**
     * Constructs a new event without referencing a specific notification.
     *
     * @param eventType the type of the event
     */
    public InfoCenterEvent(EventType<InfoCenterEvent> eventType) {
        super(eventType);
    }

    /**
     * Constructs a new event related to the given notification.
     *
     * @param eventType the event type
     * @param notification the affected notification
     */
    public InfoCenterEvent(EventType<InfoCenterEvent> eventType, Notification<?> notification) {
        super(eventType);
        this.notification = Objects.requireNonNull(notification);
    }

    /**
     * Returns the notification that was affected (null if none was affected).
     *
     * @return the affected notification
     */
    public final Notification<?> getNotification() {
        return notification;
    }
}
