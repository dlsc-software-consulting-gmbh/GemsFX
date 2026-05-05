package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.skins.InfoCenterViewSkin;
import com.dlsc.gemsfx.util.DurationConverter;
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
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.css.converter.SizeConverter;
import javafx.geometry.HorizontalDirection;
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
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-auto-open-group}</td><td>{@code boolean}</td><td>Whether to auto-expand group on new notification.</td></tr>
 *     <tr><td>{@code -fx-expand-duration}</td><td>{@code Duration}</td><td>Duration for expand/collapse animation.</td></tr>
 *     <tr><td>{@code -fx-notification-spacing}</td><td>{@code double}</td><td>Spacing between notifications in the same group.</td></tr>
 *     <tr><td>{@code -fx-show-all-fade-duration}</td><td>{@code Duration}</td><td>Duration for fade animation when toggling views.</td></tr>
 *     <tr><td>{@code -fx-slide-in-duration}</td><td>{@code Duration}</td><td>Duration for slide-in/slide-out animation.</td></tr>
 *     <tr><td>{@code -fx-slide-in-origin}</td><td>{@code left | right}</td><td>Edge from which a notification slides in.</td></tr>
 *     <tr><td>{@code -fx-transparent}</td><td>{@code boolean}</td><td>Whether the control background is transparent.</td></tr>
 *   </tbody>
 * </table>
 */
public class InfoCenterView extends Control {

    private static final String TRANSPARENT = "transparent";
    private static final double DEFAULT_NOTIFICATION_SPACING = 10.0;
    private static final Duration DEFAULT_SHOW_ALL_FADE_DURATION = Duration.millis(300);
    private static final Duration DEFAULT_EXPAND_DURATION = Duration.millis(300);
    private static final Duration DEFAULT_SLIDE_IN_DURATION = Duration.millis(500);
    private static final boolean DEFAULT_AUTO_OPEN_GROUP = false;
    private static final boolean DEFAULT_TRANSPARENT = false;
    private static final HorizontalDirection DEFAULT_SLIDE_IN_ORIGIN = HorizontalDirection.RIGHT;
    private static final Node DEFAULT_PLACEHOLDER = createDefaultPlaceholder();

    private static final PseudoClass LEFT_PSEUDO_CLASS = PseudoClass.getPseudoClass("left");
    private static final PseudoClass RIGHT_PSEUDO_CLASS = PseudoClass.getPseudoClass("right");

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

        updateSlideInOriginPseudoClass(DEFAULT_SLIDE_IN_ORIGIN);

        setFocusTraversable(false);
    }

    private void updateSlideInOriginPseudoClass(HorizontalDirection origin) {
        boolean isLeft = origin == HorizontalDirection.LEFT;
        pseudoClassStateChanged(LEFT_PSEUDO_CLASS, isLeft);
        pseudoClassStateChanged(RIGHT_PSEUDO_CLASS, !isLeft);
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
     * Groups can be opened automatically when a notification gets added to them.
     * <p>
     * Can be set via CSS using the {@code -fx-auto-open-group} property.
     * Valid values are: {@code true}, {@code false}.
     * The default value is {@code false}.
     * </p>
     *
     * @return true if the group of a newly added notification should be automatically expanded
     */
    public final BooleanProperty autoOpenGroupProperty() {
        if (autoOpenGroup == null) {
            autoOpenGroup = new StyleableBooleanProperty(DEFAULT_AUTO_OPEN_GROUP) {
                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "autoOpenGroup";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.AUTO_OPEN_GROUP;
                }
            };
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

    private final ObjectProperty<Duration> showAllFadeDuration = new StyleableObjectProperty<>(DEFAULT_SHOW_ALL_FADE_DURATION) {
        @Override
        public Object getBean() {
            return InfoCenterView.this;
        }

        @Override
        public String getName() {
            return "showAllFadeDuration";
        }

        @Override
        public CssMetaData<? extends Styleable, Duration> getCssMetaData() {
            return StyleableProperties.SHOW_ALL_FADE_DURATION;
        }
    };

    public final Duration getShowAllFadeDuration() {
        return showAllFadeDuration.get();
    }

    /**
     * The duration used for the animation when switching between the standard view
     * and the "show all" view.
     * <p>
     * Can be set via CSS using the {@code -fx-show-all-fade-duration} property.
     * Valid values are numeric millisecond values, e.g. {@code 300}.
     * The default value is {@code 300}.
     * </p>
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
     * <p>
     * Can be set via CSS using the {@code -fx-expand-duration} property.
     * Valid values are numeric millisecond values, e.g. {@code 300}.
     * The default value is {@code 300}.
     * </p>
     *
     * @return the expand / collapse animation duration
     */
    public final ObjectProperty<Duration> expandDurationProperty() {
        if (expandDuration == null) {
            expandDuration = new StyleableObjectProperty<>(DEFAULT_EXPAND_DURATION) {
                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "expandDuration";
                }

                @Override
                public CssMetaData<? extends Styleable, Duration> getCssMetaData() {
                    return StyleableProperties.EXPAND_DURATION;
                }
            };
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
     * <p>
     * Can be set via CSS using the {@code -fx-slide-in-duration} property.
     * Valid values are numeric millisecond values, e.g. {@code 500}.
     * The default value is {@code 500}.
     * </p>
     *
     * @return the slide-in duration of a new notification
     */
    public final ObjectProperty<Duration> slideInDurationProperty() {
        if (slideInDuration == null) {
            slideInDuration = new StyleableObjectProperty<>(DEFAULT_SLIDE_IN_DURATION) {
                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "slideInDuration";
                }

                @Override
                public CssMetaData<? extends Styleable, Duration> getCssMetaData() {
                    return StyleableProperties.SLIDE_IN_DURATION;
                }
            };
        }
        return slideInDuration;
    }

    public final void setSlideInDuration(Duration slideInDuration) {
        slideInDurationProperty().set(slideInDuration);
    }

    private ObjectProperty<HorizontalDirection> slideInOrigin;

    public final HorizontalDirection getSlideInOrigin() {
        return slideInOrigin == null ? DEFAULT_SLIDE_IN_ORIGIN : slideInOrigin.get();
    }

    /**
     * The edge from which a newly added notification slides into view. The default
     * value is {@link HorizontalDirection#RIGHT}. When the view is placed on the
     * left side of its container, applications should set this to
     * {@link HorizontalDirection#LEFT} so that the slide-in animation matches
     * the view's anchor side.
     * <p>
     * Can be set via CSS using the {@code -fx-slide-in-origin} property.
     * Valid values are: {@code left}, {@code right}.
     * The default value is {@code right}.
     * </p>
     * <p>
     * When this view is managed by an {@link InfoCenterPane}, the pane <b>binds</b>
     * this property to its {@link InfoCenterPane#infoCenterViewPosProperty()};
     * in that case callers must control the direction via
     * {@link InfoCenterPane#infoCenterViewPosProperty()}, since calling the
     * setter on a bound property throws a {@link RuntimeException}.
     * <p>
     * A {@code null} value is treated as {@link HorizontalDirection#RIGHT} by
     * the slide-in animation and the {@code :left} / {@code :right}
     * pseudo-classes. The getter itself returns the raw property value and may
     * therefore return {@code null} if the property has been explicitly set to
     * {@code null} or bound to a {@code null}-producing binding.
     *
     * @return the slide origin of newly added notifications
     */
    public final ObjectProperty<HorizontalDirection> slideInOriginProperty() {
        if (slideInOrigin == null) {
            slideInOrigin = new StyleableObjectProperty<>(DEFAULT_SLIDE_IN_ORIGIN) {
                @Override
                protected void invalidated() {
                    updateSlideInOriginPseudoClass(get());
                }

                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "slideInOrigin";
                }

                @Override
                public CssMetaData<? extends Styleable, HorizontalDirection> getCssMetaData() {
                    return StyleableProperties.SLIDE_IN_ORIGIN;
                }
            };
        }
        return slideInOrigin;
    }

    public final void setSlideInOrigin(HorizontalDirection slideInOrigin) {
        slideInOriginProperty().set(slideInOrigin);
    }

    private BooleanProperty transparent;

    public final boolean isTransparent() {
        return transparent == null ? DEFAULT_TRANSPARENT : transparent.get();
    }

    /**
     * Determines whether the control will be transparent or not.
     * By default, a semi-transparent black background is showing.
     * <p>
     * Can be set via CSS using the {@code -fx-transparent} property.
     * Valid values are: {@code true}, {@code false}.
     * The default value is {@code false}.
     * </p>
     *
     * @return true if the control is not transparent
     */
    public final BooleanProperty transparentProperty() {
        if (transparent == null) {
            transparent = new StyleableBooleanProperty(DEFAULT_TRANSPARENT) {
                @Override
                protected void invalidated() {
                    updateStyle();
                }

                @Override
                public Object getBean() {
                    return InfoCenterView.this;
                }

                @Override
                public String getName() {
                    return "transparent";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.TRANSPARENT_PROPERTY;
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
        Label noNotifications = new Label("No notifications");
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

        private static final CssMetaData<InfoCenterView, Boolean> AUTO_OPEN_GROUP =
                new CssMetaData<>("-fx-auto-open-group", BooleanConverter.getInstance(), DEFAULT_AUTO_OPEN_GROUP) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.autoOpenGroup == null || !view.autoOpenGroup.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Boolean>) view.autoOpenGroupProperty();
                    }
                };

        private static final CssMetaData<InfoCenterView, Boolean> TRANSPARENT_PROPERTY =
                new CssMetaData<>("-fx-transparent", BooleanConverter.getInstance(), DEFAULT_TRANSPARENT) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.transparent == null || !view.transparent.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Boolean>) view.transparentProperty();
                    }
                };

        private static final CssMetaData<InfoCenterView, Duration> SHOW_ALL_FADE_DURATION =
                new CssMetaData<>("-fx-show-all-fade-duration", DurationConverter.getInstance(), DEFAULT_SHOW_ALL_FADE_DURATION) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return !view.showAllFadeDuration.isBound();
                    }

                    @Override
                    public StyleableProperty<Duration> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Duration>) view.showAllFadeDurationProperty();
                    }
                };

        private static final CssMetaData<InfoCenterView, Duration> EXPAND_DURATION =
                new CssMetaData<>("-fx-expand-duration", DurationConverter.getInstance(), DEFAULT_EXPAND_DURATION) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.expandDuration == null || !view.expandDuration.isBound();
                    }

                    @Override
                    public StyleableProperty<Duration> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Duration>) view.expandDurationProperty();
                    }
                };

        private static final CssMetaData<InfoCenterView, Duration> SLIDE_IN_DURATION =
                new CssMetaData<>("-fx-slide-in-duration", DurationConverter.getInstance(), DEFAULT_SLIDE_IN_DURATION) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.slideInDuration == null || !view.slideInDuration.isBound();
                    }

                    @Override
                    public StyleableProperty<Duration> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<Duration>) view.slideInDurationProperty();
                    }
                };

        private static final CssMetaData<InfoCenterView, HorizontalDirection> SLIDE_IN_ORIGIN =
                new CssMetaData<>("-fx-slide-in-origin", new EnumConverter<>(HorizontalDirection.class), DEFAULT_SLIDE_IN_ORIGIN) {
                    @Override
                    public boolean isSettable(InfoCenterView view) {
                        return view.slideInOrigin == null || !view.slideInOrigin.isBound();
                    }

                    @Override
                    public StyleableProperty<HorizontalDirection> getStyleableProperty(InfoCenterView view) {
                        return (StyleableProperty<HorizontalDirection>) view.slideInOriginProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(NOTIFICATION_SPACING);
            styleables.add(AUTO_OPEN_GROUP);
            styleables.add(TRANSPARENT_PROPERTY);
            styleables.add(SHOW_ALL_FADE_DURATION);
            styleables.add(EXPAND_DURATION);
            styleables.add(SLIDE_IN_DURATION);
            styleables.add(SLIDE_IN_ORIGIN);
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
