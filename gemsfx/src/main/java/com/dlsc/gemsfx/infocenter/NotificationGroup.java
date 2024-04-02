package com.dlsc.gemsfx.infocenter;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.Objects;

/**
 * The model object used to group a list of notifications. Each group can hold notifications of
 * a specific type. Each group also has its own view factory for notifications so that different
 * groups can display notifications in different ways.
 *
 * @param <T> the type of the business / user objects
 * @param <S> the type of the notifications
 */
public class NotificationGroup<T, S extends Notification<T>> implements Comparable<NotificationGroup> {

    public NotificationGroup(String name) {
        setName(Objects.requireNonNull(name));

        ListChangeListener<? super S> listListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(notification -> {
                        notification.setGroup(this);
                        notification.expandedProperty().bind(expanded);
                    });
                }
            }

            // auto close the group when it is down to a single child
            if (isAutoCollapse() && notifications.size() < 2) {
                setExpanded(false);
            }
        };

        notifications.addListener(listListener);

        autoCollapseProperty().addListener(it -> {
            if (isAutoCollapse() && notifications.size() < 2) {
                setExpanded(false);
            }
        });

        // default view factory
        setViewFactory(notification -> new NotificationView<>(notification));
    }

    private final StringProperty name = new SimpleStringProperty(this, "name");

    public final String getName() {
        return name.get();
    }

    /**
     * The group's name.
     *
     * @return the name of the group
     */
    public final StringProperty nameProperty() {
        return name;
    }

    public final void setName(String name) {
        this.name.set(name);
    }

    private final IntegerProperty sortOrder = new SimpleIntegerProperty(this, "sortOrder");

    public final Integer getSortOrder() {
        return sortOrder.get();
    }

    /**
     * An (optional) value for the sort order. Can be used to influence the order in which
     * the groups are showing up in the info center. The value can be null.
     *
     * @return the sort order
     */
    public final IntegerProperty sortOrderProperty() {
        return sortOrder;
    }

    public final void setSortOrder(Integer sortOrder) {
        this.sortOrder.set(sortOrder);
    }

    private final ObservableList<S> notifications = FXCollections.observableArrayList();

    /**
     * The list of notifications in the group.
     *
     * @return the list of notifications
     */
    public final ObservableList<S> getNotifications() {
        return notifications;
    }

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", false);

    public final boolean isExpanded() {
        return expanded.get();
    }

    /**
     * A flag used to signal whether the group should be shown expanded or stacked.
     *
     * @return true if the group is expanded
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    public final BooleanProperty showHeader = new SimpleBooleanProperty(this, "showHeader", true);

    public final boolean isShowHeader() {
        return showHeader.get();
    }

    /**
     * Determines if this group will show its name and controls when
     * in expanded state. Default is true.
     *
     * @return true if the group will show its header
     */
    public final BooleanProperty showHeaderProperty() {
        return showHeader;
    }

    public final void setShowHeader(boolean showHeader) {
        this.showHeader.set(showHeader);
    }

    public final BooleanProperty pinnable = new SimpleBooleanProperty(this, "pinnable", true);

    public final boolean isPinnable() {
        return pinnable.get();
    }

    /**
     * Determines whether the user is allowed to pin the group so that is always
     * stays visible at the top of the info center.
     *
     * @return true if the group can be pinned
     */
    public final BooleanProperty pinnableProperty() {
        return pinnable;
    }

    public final void setPinnable(boolean pinnable) {
        this.pinnable.set(pinnable);
    }

    public final BooleanProperty pinned = new SimpleBooleanProperty(this, "pinned");

    public final boolean isPinned() {
        return pinned.get();
    }

    /**
     * Pinned groups will always be shown at the top of the info center. They can not
     * be scrolled out of the visible area.
     *
     * @return true if the group is pinned
     */
    public final BooleanProperty pinnedProperty() {
        return pinned;
    }

    public final void setPinned(boolean pinned) {
        this.pinned.set(pinned);
    }

    private final ObjectProperty<Callback<S, NotificationView<T, S>>> viewFactory = new SimpleObjectProperty<>(this, "viewFactory");

    public final Callback<S, NotificationView<T, S>> getViewFactory() {
        return viewFactory.get();
    }

    /**
     * A callback used to create the {@link NotificationView} instances for the info
     * center. Each group can have its own factory and customize the notification views.
     *
     * @return the view factory callback
     */
    public final ObjectProperty<Callback<S, NotificationView<T, S>>> viewFactoryProperty() {
        return viewFactory;
    }

    public final void setViewFactory(Callback<S, NotificationView<T, S>> viewFactory) {
        this.viewFactory.set(viewFactory);
    }

    private final IntegerProperty maximumNumberOfNotifications = new SimpleIntegerProperty(this, "maximumNumberOfNotifications", 10);

    public final int getMaximumNumberOfNotifications() {
        return maximumNumberOfNotifications.get();
    }

    /**
     * A number that determines how many notifications can be shown in the group before the
     * "show all" button will be accessible. When the user presses this button the info center
     * will switch to a different layout and display only this group's notifications inside
     * a {@link javafx.scene.control.ListView}.
     *
     * @return the maximum number of notifications to show in the standard layout
     */
    public final IntegerProperty maximumNumberOfNotificationsProperty() {
        return maximumNumberOfNotifications;
    }

    public final void setMaximumNumberOfNotifications(int maximumNumberOfNotifications) {
        this.maximumNumberOfNotifications.set(maximumNumberOfNotifications);
    }

    private final BooleanProperty autoCollapse = new SimpleBooleanProperty(this, "autoCollapse", true);

    public final boolean isAutoCollapse() {
        return autoCollapse.get();
    }

    /**
     * A flag used to control whether the group will automatically change to "collapsed" state
     * once the group is down to less than two notifications. This behaviour is application-specific
     * and some might choose to always keep the last expansion state, some might prefer to always
     * return to a collapsed state to avoid clutter in the UI.
     *
     * @return true if the group automatically caollapses when less than two notifications are visible
     * inside it.
     */
    public final BooleanProperty autoCollapseProperty() {
        return autoCollapse;
    }

    public final void setAutoCollapse(boolean autoCollapse) {
        this.autoCollapse.set(autoCollapse);
    }

    @Override
    public int compareTo(NotificationGroup o) {
        // if the app uses sort order then use that
        if (sortOrder.getValue() != null) {
            return sortOrder.getValue().compareTo(o.getSortOrder());
        }

        // fallback: sorting is based on the group name
        return getName().compareTo(o.getName());
    }
}
