package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.skins.InfoCenterViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A view capable of displaying groups of different types of notifications. The user
 * can choose to expand the groups or to stack all notifications of a group on top
 * of each other. The view is intended to be used together with the {@link InfoCenterPane},
 * so that the visibility of the view is actively managed.
 *
 * @see InfoCenterPane
 */
public class InfoCenterView extends Control {

    private static final String TRANSPARENT = "transparent";
    private static final double DEFAULT_NOTIFICATION_SPACING = 10.0;
    private static final Duration DEFAULT_EXPAND_DURATION = Duration.millis(300);
    private static final Duration DEFAULT_SLIDE_IN_DURATION = Duration.millis(500);
    private static final boolean DEFAULT_AUTO_OPEN_GROUP = false;
    private static final boolean DEFAULT_TRANSPARENT = false;
    private static final Node DEFAULT_PLACEHOLDER = createDefaultPlaceholder();

    private final InvalidationListener updateNotificationsListener = it -> updateNotificationsList();

    private final WeakInvalidationListener weakUpdateNotificationsListener = new WeakInvalidationListener(updateNotificationsListener);

    private final InvalidationListener updatePinnedGroupsListener = it -> updatePinnedAndUnpinnedGroupsList();

    private final WeakInvalidationListener weakUpdatePinnedGroupsListener = new WeakInvalidationListener(updatePinnedGroupsListener);

    /**
     * Constructs a new view.
     */
    public InfoCenterView() {
        getStyleClass().add("info-center-view");

        ListChangeListener<? super NotificationGroup<?, ?>> groupListListener = change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    change.getAddedSubList().forEach(group -> {
                        group.getNotifications().addListener(weakUpdateNotificationsListener);
                        group.pinnedProperty().addListener(weakUpdateNotificationsListener);
                        group.pinnedProperty().addListener(weakUpdatePinnedGroupsListener);
                    });
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(group -> {
                        group.getNotifications().removeListener(weakUpdateNotificationsListener);
                        group.pinnedProperty().removeListener(weakUpdateNotificationsListener);
                        group.pinnedProperty().removeListener(weakUpdatePinnedGroupsListener);
                    });
                }
            }

            updateNotificationsList();
            updatePinnedAndUnpinnedGroupsList();
        };

        getGroups().addListener(groupListListener);

        setFocusTraversable(false);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new InfoCenterViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(InfoCenterView.class.getResource("info-center-view.css")).toExternalForm();
    }

    private void updateStyle() {
        getStyleClass().remove(TRANSPARENT);
        if (isTransparent()) {
            getStyleClass().add(TRANSPARENT);
        }
    }

    private void updateNotificationsList() {
        // Important approach: by using the intermediate list we make sure that the
        // notifications list will not be in an "empty" state by accident. E.g. first
        // calling "clear()" would result in such a state and this would then trigger
        // the automatic closing of the info center inside the InfoCenterPane view.

        List<Notification<?>> allNotifications = new ArrayList<>();
        List<Notification<?>> allPinnedNotifications = new ArrayList<>();
        List<Notification<?>> allUnpinnedNotifications = new ArrayList<>();

        getGroups().forEach(group -> {
            allNotifications.addAll(group.getNotifications());
            if (group.isPinned()) {
                allPinnedNotifications.addAll(group.getNotifications());
            } else {
                allUnpinnedNotifications.addAll(group.getNotifications());
            }
        });

        notifications.setAll(allNotifications);
        pinnedNotifications.setAll(allPinnedNotifications);
        unpinnedNotifications.setAll(allUnpinnedNotifications);
    }

    private void updatePinnedAndUnpinnedGroupsList() {
        pinnedGroups.setAll(getGroups().stream().filter(group -> group.isPinned()).collect(Collectors.toList()));
        unpinnedGroups.setAll(getGroups().stream().filter(group -> !group.isPinned()).collect(Collectors.toList()));
    }

    private final ObservableList<NotificationGroup<?, ?>> groups = FXCollections.observableArrayList();

    /**
     * A list of groups of notifications that will be visualized by the view. Every
     * notification has to be a member of a group.
     *
     * @return the list of notification groups
     */
    public final ObservableList<NotificationGroup<?, ?>> getGroups() {
        return groups;
    }

    private final ObservableList<NotificationGroup<?, ?>> unpinnedGroups = FXCollections.observableArrayList();

    private final ObservableList<NotificationGroup<?, ?>> unmodifiableUnpinnedGroups = FXCollections.unmodifiableObservableList(unpinnedGroups);

    /**
     * A read-only list of the currently unpinned groups.
     *
     * @return the list of pinned groups
     */
    public final ObservableList<NotificationGroup<?, ?>> getUnmodifiableUnpinnedGroups() {
        return unmodifiableUnpinnedGroups;
    }

    private final ObservableList<NotificationGroup<?, ?>> pinnedGroups = FXCollections.observableArrayList();

    private final ObservableList<NotificationGroup<?, ?>> unmodifiablePinnedGroups = FXCollections.unmodifiableObservableList(pinnedGroups);

    /**
     * A read-only list of the currently pinned groups.
     *
     * @return the list of pinned groups
     */
    public final ObservableList<NotificationGroup<?, ?>> getUnmodifiablePinnedGroups() {
        return unmodifiablePinnedGroups;
    }

    private final ObjectProperty<Consumer<NotificationGroup<?, ?>>> onShowAllGroupNotifications = new SimpleObjectProperty(this, "onShowAllGroupNotifications");

    public final Consumer<NotificationGroup<?, ?>> getOnShowAllGroupNotifications() {
        return onShowAllGroupNotifications.get();
    }

    /**
     * A callback that gets invoked when the user presses the "show all" button of a group
     * that has more than the maximum number of notifications linked to it. The default
     * callback will switch to a list view control which will then be able to show all
     * notifications of that group. Applications can choose to display the notifications in
     * a different place, e.g. in a dialog.
     *
     * @return the callback that gets invoked when requesting to see all notifications of a group
     */
    public final ObjectProperty<Consumer<NotificationGroup<?, ?>>> onShowAllGroupNotificationsProperty() {
        return onShowAllGroupNotifications;
    }

    public final void setOnShowAllGroupNotifications(Consumer<NotificationGroup<?, ?>> onShowAllGroupNotifications) {
        this.onShowAllGroupNotifications.set(onShowAllGroupNotifications);
    }

    private BooleanProperty autoOpenGroup;

    public final boolean isAutoOpenGroup() {
        return autoOpenGroup == null ? DEFAULT_AUTO_OPEN_GROUP : autoOpenGroup.get();
    }

    /**
     * Groups can be opened automatically when a notification gets added to them. The
     * default is false.
     *
     * @return true if the group of a newly added notification should be automatically expanded
     */
    public final BooleanProperty autoOpenGroupProperty() {
        if (autoOpenGroup == null) {
            autoOpenGroup = new SimpleBooleanProperty(this, "autoOpenGroup", DEFAULT_AUTO_OPEN_GROUP);
        }
        return autoOpenGroup;
    }

    public final void setAutoOpenGroup(boolean autoOpenGroup) {
        autoOpenGroupProperty().set(autoOpenGroup);
    }

    private final ObservableList<Notification<?>> notifications = FXCollections.observableArrayList();

    private final ObservableList<Notification<?>> unmodifiableNotifications = FXCollections.unmodifiableObservableList(notifications);

    /**
     * A read-only list of all notifications of all groups currently added to the view.
     *
     * @return a flattened list of all notifications
     */
    public final ObservableList<Notification<?>> getUnmodifiableNotifications() {
        return unmodifiableNotifications;
    }

    private final ObservableList<Notification<?>> pinnedNotifications = FXCollections.observableArrayList();

    private final ObservableList<Notification<?>> unmodifiablePinnedNotifications = FXCollections.unmodifiableObservableList(pinnedNotifications);

    /**
     * A read-only list of all notifications of all pinned groups currently added to the view.
     *
     * @return a flattened list of all notifications inside pinned groups
     */
    public final ObservableList<Notification<?>> getUnmodifiablePinnedNotifications() {
        return unmodifiablePinnedNotifications;
    }

    private final ObservableList<Notification<?>> unpinnedNotifications = FXCollections.observableArrayList();

    private final ObservableList<Notification<?>> unmodifiableUnpinnedNotifications = FXCollections.unmodifiableObservableList(unpinnedNotifications);

    /**
     * A read-only list of all notifications of all unpinned groups currently added to the view.
     *
     * @return a flattened list of all notifications inside groups that are currently not pinned
     */
    public final ObservableList<Notification<?>> getUnmodifiableUnpinnedNotifications() {
        return unmodifiableUnpinnedNotifications;
    }

    private final ObjectProperty<NotificationGroup> showAllGroup = new SimpleObjectProperty<>(this, "showAllGroup");

    public final NotificationGroup getShowAllGroup() {
        return showAllGroup.get();
    }

    /**
     * The group that is currently being shown in the "show all" view of the control.
     *
     * @return the group for which all notifications are shown in a list view
     */
    public final ObjectProperty<NotificationGroup> showAllGroupProperty() {
        return showAllGroup;
    }

    public final void setShowAllGroup(NotificationGroup showAllGroup) {
        this.showAllGroup.set(showAllGroup);
    }

    private final ObjectProperty<Duration> showAllFadeDuration = new SimpleObjectProperty<>(this, "showAllFadeDuration", Duration.millis(300));

    public final Duration getShowAllFadeDuration() {
        return showAllFadeDuration.get();
    }

    /**
     * The duration used for the animation when switching between the standard view
     * and the "show all" view.
     *
     * @return the duration for the fade-in / fade-out animation used for toggling views
     */
    public final ObjectProperty<Duration> showAllFadeDurationProperty() {
        return showAllFadeDuration;
    }

    public final void setShowAllFadeDuration(Duration showAllFadeDuration) {
        this.showAllFadeDuration.set(showAllFadeDuration);
    }

    private ObjectProperty<Duration> expandDuration;

    public final Duration getExpandDuration() {
        return expandDuration == null ? DEFAULT_EXPAND_DURATION : expandDuration.get();
    }

    /**
     * The duration used for the expand / collapse animation when the view opens or closes
     * a group.
     *
     * @return the expand / collapse animation duration
     */
    public final ObjectProperty<Duration> expandDurationProperty() {
        if (expandDuration == null) {
            expandDuration = new SimpleObjectProperty<>(this, "expandDuration", DEFAULT_EXPAND_DURATION);
        }
        return expandDuration;
    }

    public final void setExpandDuration(Duration expandDuration) {
        expandDurationProperty().set(expandDuration);
    }

    private ObjectProperty<Duration> slideInDuration;

    public final Duration getSlideInDuration() {
        return slideInDuration == null ? DEFAULT_SLIDE_IN_DURATION : slideInDuration.get();
    }

    /**
     * The duration used for animating the slide-in / slide-out of a notification.
     *
     * @return the slide-in duration of a new notification
     */
    public final ObjectProperty<Duration> slideInDurationProperty() {
        if (slideInDuration == null) {
            slideInDuration = new SimpleObjectProperty<>(this, "slideInDuration", DEFAULT_SLIDE_IN_DURATION);
        }
        return slideInDuration;
    }

    public final void setSlideInDuration(Duration slideInDuration) {
        slideInDurationProperty().set(slideInDuration);
    }

    private BooleanProperty transparent;

    public final boolean isTransparent() {
        return transparent == null ? DEFAULT_TRANSPARENT : transparent.get();
    }

    /**
     * Determines whether the control will be transparent or not.
     * By default, a semi-transparent black background is showing.
     *
     * @return true if the control is not transparent
     */
    public final BooleanProperty transparentProperty() {
        if (transparent == null) {
            transparent = new SimpleBooleanProperty(this, "transparent", DEFAULT_TRANSPARENT) {
                @Override
                protected void invalidated() {
                    updateStyle();
                }
            };
        }
        return transparent;
    }

    public final void setTransparent(boolean transparent) {
        transparentProperty().set(transparent);
    }

    private ObjectProperty<Node> placeholder;

    /**
     * A placeholder node that is shown when the info is empty.
     *
     * @return the placeholder node
     */
    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<>(this, "placeholder", DEFAULT_PLACEHOLDER);
        }
        return placeholder;
    }

    public final void setPlaceholder(Node placeholder) {
        placeholderProperty().set(placeholder);
    }

    public final Node getPlaceholder() {
        return placeholder == null ? DEFAULT_PLACEHOLDER : placeholder.get();
    }

    private static StackPane createDefaultPlaceholder() {
        FontIcon graphic = new FontIcon(MaterialDesign.MDI_CREATION);
        graphic.setIconSize(20);
        graphic.setIconColor(Color.WHITE);

        Label noNotifications = new Label("No notifications", graphic);

        StackPane placeholder = new StackPane(noNotifications);
        placeholder.getStyleClass().add("default-placeholder");
        return placeholder;
    }

    private DoubleProperty notificationSpacing;

    /**
     * Represents the spacing between individual notification views within the same notification group.
     * This property allows adjustment of the vertical gap depending on the layout of the notification container.
     * The default value is 10.0.
     *
     * @return the DoubleProperty that controls the spacing between notifications in the same group.
     */
    public final DoubleProperty notificationSpacingProperty() {
        if (notificationSpacing == null) {
            notificationSpacing = new StyleableDoubleProperty(DEFAULT_NOTIFICATION_SPACING) {
                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "notificationSpacing";
                }

                @Override
                public CssMetaData<? extends Styleable, Number> getCssMetaData() {
                    return StyleableProperties.NOTIFICATION_SPACING;
                }
            };
        }
        return notificationSpacing;
    }

    public final double getNotificationSpacing() {
        return notificationSpacing == null ? DEFAULT_NOTIFICATION_SPACING : notificationSpacing.get();
    }

    public final void setNotificationSpacing(double notificationSpacing) {
        notificationSpacingProperty().set(notificationSpacing);
    }

    private static class StyleableProperties {

        private static final CssMetaData<InfoCenterView, Number> NOTIFICATION_SPACING =
                new CssMetaData<>("-fx-notification-spacing", SizeConverter.getInstance(), DEFAULT_NOTIFICATION_SPACING) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.notificationSpacing == null || !view.notificationSpacing.isBound();
                    }

                    @Override
                    public StyleableProperty<Number> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Number>) view.notificationSpacingProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(NOTIFICATION_SPACING);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return InfoCenterView.StyleableProperties.STYLEABLES;
    }

    public void clearAll() {
        getGroups().forEach(group -> group.getNotifications().clear());
    }
}
