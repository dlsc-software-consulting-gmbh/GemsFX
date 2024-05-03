package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.infocenter.InfoCenterEvent;
import com.dlsc.gemsfx.infocenter.InfoCenterView;
import com.dlsc.gemsfx.infocenter.Notification;
import com.dlsc.gemsfx.infocenter.Notification.OnClickBehaviour;
import com.dlsc.gemsfx.infocenter.NotificationGroup;
import com.dlsc.gemsfx.infocenter.NotificationView;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.animation.AnimationTimer;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import one.jpro.jproutils.treeshowing.TreeShowing;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class InfoCenterViewSkin extends SkinBase<InfoCenterView> {

    private final VBox allGroupsContainer;
    private final VBox singleGroupContainer;

    private final ListView<Notification<?>> singleGroupListView = new ListView<>();

    private final InvalidationListener listItemsListener = it -> {
        // We want the info center to switch back to the normal view when the user
        // removes the last notification inside the list view. If there are no more notifications
        // at all then also hide the info center.
        InfoCenterView infoCenterView = getSkinnable();
        if (infoCenterView.getShowAllGroup() != null && singleGroupListView.getItems().isEmpty()) {
            getSkinnable().setShowAllGroup(null);
            if (infoCenterView.getUnmodifiableNotifications().isEmpty()) {
                singleGroupListView.fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE));
            }
        }
    };

    private final WeakInvalidationListener weakListItemsListener = new WeakInvalidationListener(listItemsListener);
    private final VBox mainPane;

    private VBox unpinnedGroupsContainer = new VBox();

    private VBox pinnedGroupsContainer = new VBox();

    public InfoCenterViewSkin(InfoCenterView view) {
        super(view);

        // first check, might have been set before view was added to scene
        if (view.getOnShowAllGroupNotifications() == null) {
            view.setOnShowAllGroupNotifications(group -> {
                view.setShowAllGroup(group);
            });
        }

        BooleanBinding pinnedNotificationsExist = Bindings.isNotEmpty(view.getUnmodifiablePinnedNotifications());
        BooleanBinding unpinnedNotificationsExist = Bindings.isNotEmpty(view.getUnmodifiableUnpinnedNotifications());

        pinnedGroupsContainer.getStyleClass().addAll("pinned", "groups-container", "wrapper");
        pinnedGroupsContainer.visibleProperty().bind(pinnedNotificationsExist);
        pinnedGroupsContainer.managedProperty().bind(pinnedNotificationsExist);
        pinnedGroupsContainer.setMinHeight(Region.USE_PREF_SIZE);

        Region separator = new Region();
        separator.getStyleClass().add("pinned-separator");
        separator.visibleProperty().bind(pinnedNotificationsExist.and(unpinnedNotificationsExist));
        separator.managedProperty().bind(pinnedNotificationsExist.and(unpinnedNotificationsExist));

        singleGroupListView.getStyleClass().add("single-group-list-view");

        unpinnedGroupsContainer.getStyleClass().add("groups-container");
        unpinnedGroupsContainer.setMinHeight(Region.USE_PREF_SIZE);

        ScrollPane scrollPane = new ScrollPane(unpinnedGroupsContainer) {

            @Override
            protected double computePrefHeight(double width) {
                return unpinnedGroupsContainer.prefHeight(width - getInsets().getLeft() - getInsets().getRight()) + getInsets().getTop() + getInsets().getBottom();
            }

            @Override
            public Orientation getContentBias() {
                return Orientation.HORIZONTAL;
            }
        };

        scrollPane.setPannable(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setFitToWidth(true);
        scrollPane.visibleProperty().bind(unpinnedNotificationsExist);
        scrollPane.managedProperty().bind(unpinnedNotificationsExist);
        scrollPane.getStyleClass().add("wrapper");

        allGroupsContainer = new VBox(pinnedGroupsContainer, separator, scrollPane);
        allGroupsContainer.getStyleClass().addAll("top-level-container", "all-groups-container");

        singleGroupListView.setCellFactory(lv -> new NotificationListCell());
        singleGroupListView.setSelectionModel(new MultipleSelectionModel<>() {
            @Override
            public ObservableList<Integer> getSelectedIndices() {
                return FXCollections.observableArrayList();
            }

            @Override
            public ObservableList<Notification<?>> getSelectedItems() {
                return FXCollections.observableArrayList();
            }

            @Override
            public void selectIndices(int index, int... indices) {

            }

            @Override
            public void selectAll() {

            }

            @Override
            public void selectFirst() {

            }

            @Override
            public void selectLast() {

            }

            @Override
            public void clearAndSelect(int index) {

            }

            @Override
            public void select(int index) {

            }

            @Override
            public void select(Notification<?> obj) {

            }

            @Override
            public void clearSelection(int index) {

            }

            @Override
            public void clearSelection() {

            }

            @Override
            public boolean isSelected(int index) {
                return false;
            }

            @Override
            public boolean isEmpty() {
                return true;
            }

            @Override
            public void selectPrevious() {
            }

            @Override
            public void selectNext() {
            }
        });
        singleGroupListView.itemsProperty().addListener((obs, oldItems, newItems) -> {
            if (oldItems != null) {
                oldItems.removeListener(weakListItemsListener);
            }

            if (newItems != null) {
                newItems.addListener(weakListItemsListener);
            }
        });

        VBox.setVgrow(singleGroupListView, Priority.ALWAYS);

        if (view.getShowAllGroup() != null) {
            singleGroupListView.setItems(view.getShowAllGroup().getNotifications());
        }

        view.showAllGroupProperty().addListener(it -> {
            NotificationGroup showAllGroup = view.getShowAllGroup();
            if (showAllGroup != null) {
                singleGroupListView.setItems(view.getShowAllGroup().getNotifications());
            } else {
                singleGroupListView.setItems(FXCollections.observableArrayList());
            }
        });

        Label groupNameLabel = new Label();
        groupNameLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getShowAllGroup() != null ? view.getShowAllGroup().getName() : "", view.showAllGroupProperty()));
        groupNameLabel.getStyleClass().add("group-name-label");
        groupNameLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(groupNameLabel, Priority.ALWAYS);

        Button closeShowAllButton = new Button(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"single.group.header.close"));
        closeShowAllButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"single.group.header.close.tip")));
        closeShowAllButton.getStyleClass().add("close-show-all-button");
        closeShowAllButton.setOnAction(evt -> view.setShowAllGroup(null));

        Button clearAllButton = new Button();
        clearAllButton.setGraphic(new FontIcon());
        clearAllButton.getStyleClass().add("clear-all-button");
        clearAllButton.setOnAction(evt -> view.getShowAllGroup().getNotifications().clear());
        clearAllButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"single.group.header.remove.all")));

        HBox singleGroupHeader = new HBox(groupNameLabel, closeShowAllButton, clearAllButton);
        singleGroupHeader.getStyleClass().add("single-group-header");

        singleGroupContainer = new VBox(singleGroupHeader, singleGroupListView);
        singleGroupContainer.getStyleClass().addAll("top-level-container", "single-group-wrapper", "wrapper");
        VBox.setVgrow(singleGroupContainer, Priority.ALWAYS);

        mainPane = new VBox(allGroupsContainer, singleGroupContainer);
        mainPane.setMinHeight(0);
        mainPane.setMaxHeight(Double.MAX_VALUE);
        mainPane.getStyleClass().add("main-pane");

        getChildren().add(mainPane);

        BooleanBinding emptyBinding = Bindings.createBooleanBinding(() -> view.getUnmodifiableNotifications().isEmpty(), view.getUnmodifiableNotifications());

        addPlaceholderIfNotNull(view.getPlaceholder(), emptyBinding);

        view.placeholderProperty().addListener((obs, oldVal, newVal) -> {
            removePlaceholderIfNotNull(oldVal);
            addPlaceholderIfNotNull(newVal, emptyBinding);
        });

        InvalidationListener invalidationListener = (Observable it) -> updateView();
        view.getUnmodifiablePinnedGroups().addListener(invalidationListener);
        view.getUnmodifiableUnpinnedGroups().addListener(invalidationListener);

        updateView();

        // a timer used for frequently updating the time stamps on the notifications
        AnimationTimer timer = new AnimationTimer() {

            private long lastToggle;

            @Override
            public void handle(long now) {
                if (lastToggle == 0L) {
                    updateTimes();
                    lastToggle = now;
                } else {
                    long diff = now - lastToggle;

                    // Update interval: 60_000,000,000ns == 60000ms == 60s == 1minute
                    if (diff >= 60_000_000_000L) {
                        updateTimes();
                        lastToggle = now;
                    }
                }
            }
        };

        TreeShowing.treeShowing(view).addListener((p,o,n) -> {
            if (n) {
                timer.start();
            } else {
                timer.stop();
            }
        });

        timer.start();

        updateVisibilities();
        view.showAllGroupProperty().addListener(it -> updateVisibilities());
    }


    private void addPlaceholderIfNotNull(Node placeholder, BooleanBinding emptyBing) {
        if (placeholder != null) {
            placeholder.managedProperty().bind(emptyBing);
            placeholder.visibleProperty().bind(emptyBing);
            mainPane.getChildren().add(placeholder);
        }
    }

    private void removePlaceholderIfNotNull(Node placeholder) {
        if (placeholder != null) {
            placeholder.managedProperty().unbind();
            placeholder.visibleProperty().unbind();
            mainPane.getChildren().remove(placeholder);
        }
    }

    private void updateVisibilities() {
        if (getSkinnable().getShowAllGroup() != null) {
            singleGroupContainer.setVisible(true);
            singleGroupContainer.setManaged(true);
            allGroupsContainer.setVisible(false);
            allGroupsContainer.setManaged(false);
        } else {
            singleGroupContainer.setVisible(false);
            singleGroupContainer.setManaged(false);
            allGroupsContainer.setVisible(true);
            allGroupsContainer.setManaged(true);
        }
    }

    private void updateTimes() {
        // streams API .... love! :-)
        unpinnedGroupsContainer.getChildren().stream()
                .filter(node -> node instanceof GroupView)
                .flatMap(groupView -> ((GroupView<?, ?>) groupView).getChildren().stream())
                .filter(child -> child instanceof NotificationView)
                .map(child -> (NotificationView) child)
                .collect(Collectors.toList())
                .forEach(notificationView -> notificationView.updateDateAndTimeLabel());
    }

    // stores the notification that will be animated
    private final ObjectProperty<Notification<?>> animatedNotification = new SimpleObjectProperty<>(this, "animatedNotification");

    private void updateView() {
        pinnedGroupsContainer.getChildren().clear();
        unpinnedGroupsContainer.getChildren().clear();

        InfoCenterView view = getSkinnable();

        List<NotificationGroup<?, ?>> sortedPinnedGroups = view.getUnmodifiablePinnedGroups().stream().sorted().collect(Collectors.toList());
        updateView(sortedPinnedGroups, pinnedGroupsContainer);

        List<NotificationGroup<?, ?>> sortedUnpinnedGroups = view.getUnmodifiableUnpinnedGroups().stream().sorted().collect(Collectors.toList());
        updateView(sortedUnpinnedGroups, unpinnedGroupsContainer);
    }

    private void updateView(List<NotificationGroup<?, ?>> groups, VBox container) {
        List<GroupView<?, ?>> groupViews = new ArrayList<>();

        int size = groups.size();

        for (int i = 0; i < size; i++) {
            NotificationGroup<?, ?> group = groups.get(i);
            GroupView groupView = new GroupView(group);
            groupViews.add(groupView);
        }

        for (int i = 0; i < size; i++) {
            NotificationGroup<?, ?> group = groups.get(i);

            GroupView<?,?> groupView = groupViews.get(i);
            container.getChildren().add(groupView);

            if (i < size - 1) {
                Region spacer = new Region();
                spacer.getStyleClass().add("group-separator");
                container.getChildren().add(spacer);

                /*
                 * Only show the spacer when the group is expanded AND when
                 * the next group view is visible / being used.
                 */
                GroupView<?,?> nextGroupView = groupViews.get(i + 1);
                BooleanBinding showSpacerBinding = nextGroupView.visibleProperty()
                        .and(group.expandedProperty())
                        .and(Bindings.size(group.getNotifications()).greaterThan(1));

                spacer.visibleProperty().bind(showSpacerBinding);
                spacer.managedProperty().bind(showSpacerBinding);
            }
        }
    }

    class GroupView<T, S extends Notification<T>> extends Pane {

        private final NotificationGroup<T, S> group;

        private final ListChangeListener<Notification> notificationsChangedListener = change -> {
            Notification<?> theOne = null;

            while (change.next()) {
                if (change.wasAdded()) {
                    for (Notification notification : change.getAddedSubList()) {
                        if (theOne == null) {
                            theOne = notification;
                        }
                        fireEvent(new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_ADDED, notification));
                    }
                } else if (change.wasRemoved()) {
                    change.getRemoved().forEach(notification -> fireEvent(new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_REMOVED, notification)));
                }
            }

            updateStyleClass();

            createNotificationViews();

            // run this later, so that the view actually exists
            if (theOne != null) {
                Notification<?> finalOne = theOne;
                Platform.runLater(() -> {
                    if (getSkinnable().isAutoOpenGroup()) {
                        finalOne.getGroup().setExpanded(true);
                        Platform.runLater(() -> animatedNotification.set(finalOne));
                    } else {
                        animatedNotification.set(finalOne);
                    }
                });
            }
        };

        private final WeakListChangeListener<Notification> weakNotificationsChangedListener = new WeakListChangeListener(notificationsChangedListener);

        private final Timeline expandTimeline = new Timeline();

        private final HBox headerBox = new HBox();

        private final InvalidationListener requestedNotificationListener = it -> showNotificationView();
        private final WeakInvalidationListener weakRequestedNotificationListener = new WeakInvalidationListener(requestedNotificationListener);

        private final InvalidationListener spacingListener = it -> layoutChildren();
        private final WeakInvalidationListener weakSpacingListener = new WeakInvalidationListener(spacingListener);


        public GroupView(NotificationGroup<T, S> group) {
            this.group = group;
            getStyleClass().add("group-view");

            headerBox.getStyleClass().add("header");

            Label groupNameLabel = new Label(group.getName());
            groupNameLabel.getStyleClass().add("group-name-label");
            groupNameLabel.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(groupNameLabel, Priority.ALWAYS);

            Button showLessButton = new Button(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW, "group.header.show.less"));
            showLessButton.getStyleClass().add("show-less-button");
            showLessButton.setOnAction(evt -> {
                requestFocus();
                group.setExpanded(false);
            });
            showLessButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"group.header.show.less.tip")));

            InfoCenterView infoCenterView = getSkinnable();
            infoCenterView.notificationSpacingProperty().addListener(weakSpacingListener);

            Button showAllButton = new Button();
            showAllButton.textProperty().bind(Bindings.createStringBinding(() -> MessageFormat.format("{0} {1}", ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"group.header.show.all"), group.getNotifications().size()), group.getNotifications()));
            showAllButton.getStyleClass().add("show-all-button");
            showAllButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"group.header.show.all.tip")));
            showAllButton.setOnAction(evt -> infoCenterView.getOnShowAllGroupNotifications().accept(group));
            showAllButton.visibleProperty().bind(Bindings.createBooleanBinding(() -> infoCenterView.getOnShowAllGroupNotifications() != null && group.getNotifications().size() > group.getMaximumNumberOfNotifications(),
                    group.maximumNumberOfNotificationsProperty(), group.getNotifications(), infoCenterView.onShowAllGroupNotificationsProperty()));

            Button clearButton = new Button();
            clearButton.getStyleClass().add("clear-button");
            clearButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"group.header.remove.all.tip")));
            clearButton.setGraphic(new FontIcon());
            clearButton.setOnAction(evt -> {
                group.setExpanded(false);
                group.getNotifications().clear();
            });

            ToggleButton pinButton = new ToggleButton();
            pinButton.getStyleClass().add("pin-button");
            pinButton.setTooltip(new Tooltip(ResourceBundleManager.getString(ResourceBundleManager.Type.INFO_CENTER_VIEW,"group.header.pin.tip")));
            pinButton.setGraphic(new FontIcon());
            pinButton.visibleProperty().bind(group.pinnableProperty());
            pinButton.managedProperty().bind(group.pinnableProperty());
            pinButton.selectedProperty().bindBidirectional(group.pinnedProperty());

            headerBox.getChildren().addAll(groupNameLabel, showAllButton, showLessButton, clearButton, pinButton);
            headerBox.visibleProperty().bind(Bindings.createBooleanBinding(() -> group.isExpanded() && group.getNotifications().size() > 1 && group.isShowHeader(), group.expandedProperty(), group.getNotifications()));

            group.getNotifications().addListener(weakNotificationsChangedListener);

            /*
             * We might have too many notification views when this property changes,
             * so we have to rebuild them.
             */
            group.maximumNumberOfNotificationsProperty().addListener(it -> createNotificationViews());

            group.pinnedProperty().addListener(it -> updateStyleClass());

            group.expandedProperty().addListener(it -> {
                updateStyleClass();
                animate(group.isExpanded());
            });

            expansionProgressProperty().addListener(it -> layoutChildren());

            if (group.isExpanded()) {
                setExpansionProgress(1);
            } else {
                setExpansionProgress(0);
            }

            animatedNotification.addListener(weakRequestedNotificationListener);

            visibleProperty().bind(Bindings.isNotEmpty(group.getNotifications()));
            managedProperty().bind(Bindings.isNotEmpty(group.getNotifications()));

            createNotificationViews();

            updateStyleClass();
        }

        private void updateStyleClass() {
            getStyleClass().removeAll("big-stack", "small-stack", "pinned", "expanded", "collapsed");

            if (!group.isExpanded()) {
                getStyleClass().add("collapsed");

                if (group.getNotifications().size() > 2) {
                    getStyleClass().add("big-stack");
                } else if (group.getNotifications().size() > 1) {
                    getStyleClass().add("small-stack");
                }
            } else {
                getStyleClass().add("expanded");
            }

            if (group.isPinned()) {
                getStyleClass().add("pinned");
            }
        }

        private void showNotificationView() {
            Notification<?> notification = animatedNotification.get();
            if (notification != null) {

                if (notification.getGroup().equals(group)) {

                    // find the view that belongs to this notification
                    Optional<NotificationView> notificationViewOptional = getChildren().stream()
                            .filter(child -> child instanceof NotificationView<?, ?>)
                            .map(child -> (NotificationView) child)
                            .filter(view -> view.getNotification().equals(notification))
                            .findFirst();

                    if (notificationViewOptional.isPresent()) {
                        NotificationView notificationView = notificationViewOptional.get();
                        notificationView.setTranslateX(getWidth());

                        Timeline slideInTimeline = new Timeline();
                        slideInTimeline.getKeyFrames().setAll(new KeyFrame(getSkinnable().getSlideInDuration(), new KeyValue(notificationView.translateXProperty(), 0, Interpolator.EASE_BOTH)));
                        slideInTimeline.play();
                    }
                }
            }
        }

        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }

        @Override
        protected double computePrefHeight(double width) {
            double h = getInsets().getTop() + getInsets().getBottom();

            width = width - getInsets().getLeft() - getInsets().getRight();

            double spacing = getSkinnable().getNotificationSpacing();
            if (headerBox.isVisible()) {
                h += headerBox.prefHeight(width);
                h += spacing;
            }

            List<Node> notificationViews = getChildren().stream().filter(node -> node instanceof NotificationView).collect(Collectors.toList());

            if (!notificationViews.isEmpty()) {
                // last one is on top
                Node latestNotification = notificationViews.get(notificationViews.size() - 1);
                h += latestNotification.prefHeight(width);
            }

            double innerHeight = 0;

            if (notificationViews.size() > 1) {
                for (int i = notificationViews.size() - 2; i >= 0; i--) {
                    Node node = notificationViews.get(i);
                    innerHeight += spacing;
                    innerHeight += node.prefHeight(width);
                }
            }

            h = h + (innerHeight * getExpansionProgress());

            return h;
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            double x = getInsets().getLeft();
            double y = getInsets().getTop();
            double w = getWidth() - getInsets().getLeft() - getInsets().getRight();

            List<Node> notificationViews = getChildren().stream().filter(node -> node instanceof NotificationView).collect(Collectors.toList());
            int size = notificationViews.size();

            double spacing = getSkinnable().getNotificationSpacing();
            if (headerBox.isVisible()) {
                double boxHeight = headerBox.prefHeight(w);
                headerBox.resizeRelocate(x, y, w, boxHeight);
                y += boxHeight;
                y += spacing;
            }

            for (int i = size - 1; i >= 0; i--) {
                Node node = notificationViews.get(i);
                double h = Math.max(node.prefHeight(w), node.minHeight(w));
                node.resizeRelocate(x, y * getExpansionProgress(), w, h);

                y += h;

                if (i > 0) {
                    y += spacing;
                }
            }
        }

        private void animate(boolean expand) {
            KeyValue keyValue = new KeyValue(expansionProgress, expand ? 1 : 0);
            KeyFrame keyFrame = new KeyFrame(getSkinnable().getExpandDuration(), keyValue);
            expandTimeline.stop();
            expandTimeline.getKeyFrames().setAll(keyFrame);
            expandTimeline.play();
        }

        private void createNotificationViews() {
            getChildren().setAll(headerBox);
            ObservableList<S> notifications = group.getNotifications();
            SortedList<S> sorted = notifications.sorted();

            int count = sorted.size();
            int start = Math.max(0, count - group.getMaximumNumberOfNotifications());

            // loop has to run to last element in list as it is the newest
            for (int i = start; i < count; i++) {
                int index = i;

                S notification = sorted.get(index);

                Callback<S, NotificationView<T, S>> viewFactory = group.getViewFactory();

                NotificationView<T, S> notificationView = viewFactory.call(notification);
                notificationView.visibleProperty().bind(Bindings.createBooleanBinding(() -> index == count - 1 || getExpansionProgress() > 0, expansionProgress));
                configureNotificationView(notificationView);

                if (i < count - 1) {
                    notificationView.opacityProperty().bind(expansionProgressProperty());
                }

                getChildren().add(notificationView);
            }
        }

        private final DoubleProperty expansionProgress = new SimpleDoubleProperty(this, "expansionProgress");

        public double getExpansionProgress() {
            return expansionProgress.get();
        }

        public DoubleProperty expansionProgressProperty() {
            return expansionProgress;
        }

        public void setExpansionProgress(double expansionProgress) {
            this.expansionProgress.set(expansionProgress);
        }
    }

    private void configureNotificationView(NotificationView notificationView) {
        notificationView.setOnMouseClicked(evt -> {
            Notification notification = notificationView.getNotification();
            NotificationGroup group = notification.getGroup();

            if (!evt.isConsumed() && evt.isStillSincePress() && evt.getClickCount() == 1 && evt.getButton().equals(MouseButton.PRIMARY)) {
                if (group.isExpanded()) {
                    InfoCenterView infoCenterView = getSkinnable();
                    infoCenterView.fireEvent(new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_CHOSEN, notification));
                    Callback<Notification<?>, OnClickBehaviour> onClick = notification.getOnClick();
                    if (onClick != null) {
                        OnClickBehaviour behaviour = onClick.call(notification);
                        switch (behaviour) {
                            case NONE:
                                break;
                            case REMOVE:
                                notification.remove();
                                break;
                            case HIDE:
                                notificationView.fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE));
                                infoCenterView.setShowAllGroup(null);
                                break;
                            case HIDE_AND_REMOVE:
                                notificationView.fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE));
                                infoCenterView.setShowAllGroup(null);
                                notification.remove();
                                break;
                        }
                    }
                } else {
                    group.setExpanded(true);
                }

                evt.consume();
            }
        });
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return mainPane.prefHeight(width - leftInset - rightInset) + topInset + bottomInset;
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return mainPane.minHeight(width - leftInset - rightInset) + topInset + bottomInset;
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return mainPane.maxHeight(width - leftInset - rightInset) + topInset + bottomInset;
    }

    private class NotificationListCell extends ListCell<Notification<?>> {

        public NotificationListCell() {
            getStyleClass().add("notification-list-cell");
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(Notification<?> notification, boolean empty) {
            super.updateItem(notification, empty);

            if (!empty && notification != null) {
                NotificationGroup<?, ?> group = notification.getGroup();
                Callback<Notification<?>, ? extends NotificationView> viewFactory = (Callback<Notification<?>, ? extends NotificationView>) group.getViewFactory();
                NotificationView notificationView = viewFactory.call(notification);
                notificationView.setPrefWidth(0); // determined by the list view
                notificationView.getProperties().put("stacking-enabled", "false");
                configureNotificationView(notificationView);
                setGraphic(notificationView);
            } else {
                setGraphic(null);
            }
        }
    }
}
