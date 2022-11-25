package com.dlsc.gemsfx.infocenter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.time.ZonedDateTime;
import java.util.Objects;
import java.util.StringJoiner;

/**
 * A pure model object containing the data for a {@link NotificationView}.
 * Notifications can be added to groups ({@link NotificationGroup}).
 *
 * @see NotificationGroup#getNotifications()
 *
 * @param <T> the type of the user object
 */
public class Notification<T> implements Comparable<Notification<T>> {

    private NotificationGroup group;

    /**
     * Constructs a new notification.
     *
     * @param title the title that will usually be shown in bold
     * @param summary a summary text of the noteworthy thing that happened
     * @param dateTime the date and time when the noteworthy thing happened
     */
    public Notification(String title, String summary, ZonedDateTime dateTime) {
        setTitle(Objects.requireNonNull(title));
        setSummary(Objects.requireNonNull(summary));
        setDateTime(Objects.requireNonNull(dateTime));
    }

    /**
     * Constructs a new notification.
     *
     * @param title the title that will usually be shown in bold
     * @param summary a summary text of the noteworthy thing that happened
     */
    public Notification(String title, String summary) {
        this(title, summary, ZonedDateTime.now());
    }

    /**
     * Package protected so that it can only be called from {@link NotificationGroup}.
     */
    void setGroup(NotificationGroup group) {
        this.group = group;
    }

    /**
     * The group to which the notification belongs.
     *
     * @return the notification's parent
     */
    public final NotificationGroup getGroup() {
        return group;
    }

    /**
     * Convenience method to remove the notification from its group / its parent.
     */
    public final void remove() {
        getGroup().getNotifications().remove(this);
    }

    private final StringProperty title = new SimpleStringProperty(this, "title");

    public final String getTitle() {
        return title.get();
    }

    /**
     * The title of the notification, e.g. "Purchase Order #123456" when this is
     * the subject of an email for which the notification was created.
     *
     * @return the title of the notification
     */
    public final StringProperty titleProperty() {
        return title;
    }

    public final void setTitle(String title) {
        this.title.set(title);
    }

    private final StringProperty summary = new SimpleStringProperty(this, "description");

    public final String getSummary() {
        return summary.get();
    }

    /**
     * A short summary of the noteworthy thing that happened.
     *
     * @return the summary text
     */
    public final StringProperty summaryProperty() {
        return summary;
    }

    public final void setSummary(String summary) {
        this.summary.set(summary);
    }

    private final ObjectProperty<ZonedDateTime> dateTime = new SimpleObjectProperty<>(this, "dateTime", ZonedDateTime.now());

    public final ZonedDateTime getDateTime() {
        return dateTime.get();
    }

    /**
     * The time stamp for this notification.
     *
     * @return the date and time
     */
    public final ObjectProperty<ZonedDateTime> dateTimeProperty() {
        return dateTime;
    }

    public final void setDateTime(ZonedDateTime dateTime) {
        this.dateTime.set(dateTime);
    }

    private final ObservableList<NotificationAction> actions = FXCollections.observableArrayList();

    /**
     * A list of (optional) actions that the user can perform directly from within
     * the notification.
     *
     * @return a list of possible actions
     */
    public final ObservableList<NotificationAction> getActions() {
        return actions;
    }

    private final ObjectProperty<T> userObject = new SimpleObjectProperty<>(this, "userObject");

    public final T getUserObject() {
        return userObject.get();
    }

    /**
     * An (optional) user object for easy linking between the notification
     * and the business object for which the notification was created, e.g. an
     * email or a calendar entry.
     *
     * @return the user object
     */
    public final ObjectProperty<T> userObjectProperty() {
        return userObject;
    }

    public final void setUserObject(T userObject) {
        this.userObject.set(userObject);
    }

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    public final boolean isExpanded() {
        return expanded.get();
    }

    /**
     * A flag that gets updated when the notification's group was expanded or collapsed.
     *
     * @return true if the notification's parent / group is currently expanded
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    /**
     * A list of possible behaviours that are supported when the user clicks on a notification.
     */
    public enum OnClickBehaviour {

        /**
         * Do nothing when the user clicks on the notification.
         */
        NONE,

        /**
         * Remove the notification from its group when the user clicks on it.
         */
        REMOVE,

        /**
         * Hide the parent view (the info center) when the user clicks on the notification.
         */
        HIDE,

        /**
         * Hide the parent view and remove the notification from its group when the user clicks on it.
         */
        HIDE_AND_REMOVE
    }

    public final ObjectProperty<Callback<Notification<T>, OnClickBehaviour>> onClick = new SimpleObjectProperty<>(this, "onClick", notification -> OnClickBehaviour.HIDE_AND_REMOVE);

    public final Callback<Notification<T>, OnClickBehaviour> getOnClick() {
        return onClick.get();
    }

    /**
     * A callback that determines how the notification and the view will behave when the user clicks
     * on it. The default behaviour is to hide the view and remove the notification from its group.
     *
     * @return the behaviour upon click (do nothing, remove the notification, hide the view, ....)
     */
    public final ObjectProperty<Callback<Notification<T>, OnClickBehaviour>> onClickProperty() {
        return onClick;
    }

    public final void setOnClick(Callback<Notification<T>, OnClickBehaviour> onClick) {
        this.onClick.set(onClick);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Notification.class.getSimpleName() + "[", "]")
                .add("title=" + getTitle())
                .add("description=" + getSummary())
                .add("expanded=" + isExpanded())
                .toString();
    }

    @Override
    public int compareTo(Notification<T> o) {
        return getDateTime().compareTo(o.getDateTime());
    }
}
