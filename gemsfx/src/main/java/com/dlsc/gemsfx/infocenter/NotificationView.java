package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.infocenter.Notification.OnClickBehaviour;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.animation.FadeTransition;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * A view used for visualizing a notification
 *
 * @param <T> the type of the business object
 * @param <S> the type of the notification
 */
public class NotificationView<T, S extends Notification<T>> extends StackPane {

    private static final PseudoClass PSEUDO_CLASS_EXPANDED = PseudoClass.getPseudoClass("expanded");
    private static final DateTimeFormatter SHORT_TIME_FORMATTER = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT);

    private final S notification;
    private final ContentPane contentPane;
    private final StackPane stackNotification1;
    private final StackPane stackNotification2;

    /**
     * Constructs a new view for the given notification.
     *
     * @param notification the notification
     */
    public NotificationView(S notification) {
        this.notification = Objects.requireNonNull(notification);
        getStyleClass().add("notification-view");

        setPickOnBounds(false);
        setMinHeight(Region.USE_PREF_SIZE);

        stackNotification1 = new StackPane();
        stackNotification1.getStyleClass().addAll("content", "stack-notification1");
        stackNotification1.visibleProperty().bind(stackingEnabled.and(getNotification().expandedProperty().not().and(Bindings.size(notification.getGroup().getNotifications()).greaterThan(1))));
        stackNotification1.managedProperty().bind(stackNotification1.visibleProperty());

        stackNotification2 = new StackPane();
        stackNotification2.getStyleClass().addAll("content", "stack-notification2");
        stackNotification2.visibleProperty().bind(stackingEnabled.and(getNotification().expandedProperty().not().and(Bindings.size(notification.getGroup().getNotifications()).greaterThan(2))));
        stackNotification2.managedProperty().bind(stackNotification2.visibleProperty());

        contentPane = new ContentPane();

        FontIcon defaultIcon = new FontIcon();
        defaultIcon.getStyleClass().add("default-icon");
        setGraphic(defaultIcon);

        getChildren().addAll(stackNotification2, stackNotification1, contentPane);

        // This is needed. Maybe a bug in the centerProperty() binding inside ContentPane?
        showContentProperty().addListener(it -> Optional.ofNullable(getParent()).ifPresent(Parent::requestLayout));

        MapChangeListener<? super Object, ? super Object> ml = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("stacking-enabled")) {
                    stackingEnabled.set(change.getValueAdded().equals("true"));
                    getProperties().remove("stacking-enabled");
                }
            }
        };

        getProperties().addListener(ml);

        InvalidationListener updateStyleClassListener = it -> updateStyleClass();
        notification.getGroup().expandedProperty().addListener(updateStyleClassListener);
        notification.getGroup().getNotifications().addListener(updateStyleClassListener);
        updateStyleClass();
    }

    @Override
    protected double computePrefHeight(double width) {
        double h = contentPane.prefHeight(width - getInsets().getLeft() - getInsets().getRight());

        if (stackNotification1.isVisible()) {
            h += 5;
        }

        if (stackNotification2.isVisible()) {
            h += 5;
        }

        return h;
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double ph = contentPane.prefHeight(width);
        contentPane.resizeRelocate(getInsets().getLeft(), getInsets().getTop(), width, ph);

        if (stackNotification1.isVisible()) {
            stackNotification1.resizeRelocate(getInsets().getLeft() + 10, getInsets().getTop() + 5, width - 20, ph);
        }

        if (stackNotification2.isVisible()) {
            stackNotification2.resizeRelocate(getInsets().getLeft() + 20, getInsets().getTop() + 10, width - 40, ph);
        }
    }

    private void updateStyleClass() {
        getStyleClass().removeAll("small-stack", "big-stack");
        if (!notification.getGroup().isExpanded()) {
            int size = notification.getGroup().getNotifications().size();
            if (size > 1) {
                getStyleClass().add("big-stack");
            } else if (size > 0) {
                getStyleClass().add("small-stack");
            }
        }
    }

    /**
     * The notification for which the view was created.
     *
     * @return the notification
     */
    public final S getNotification() {
        return notification;
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public final Node getGraphic() {
        return graphic.get();
    }

    /**
     * An (optional) node that will be used as the notification's graphic
     * object / icon on the left-hand side.
     *
     * @return the graphic / the icon
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    // Used for properly styling the view when used inside a list view ("show all")
    private final BooleanProperty stackingEnabled = new SimpleBooleanProperty(this, "stackingEnabled", true);

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content");

    public final Node getContent() {
        return content.get();
    }

    /**
     * An (optional) detailed UI that can be revealed interactively by the user.
     * Example: a map view that shows the location of a meeting scheduled in a calendar.
     *
     * @return the optional graphic
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final void setContent(Node content) {
        this.content.set(content);
    }

    private final BooleanProperty showContent = new SimpleBooleanProperty(this, "showContent");

    public final boolean isShowContent() {
        return showContent.get();
    }

    public final BooleanProperty showContentProperty() {
        return showContent;
    }

    public final void setShowContent(boolean showContent) {
        this.showContent.set(showContent);
    }

    private static final StringConverter<ZonedDateTime> DEFAULT_TIME_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(ZonedDateTime dateTime) {
            if (dateTime != null) {
                Duration between = Duration.between(dateTime, ZonedDateTime.now());
                if (between.toDays() == 0) {
                    if (between.toHours() > 2) {
                        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).format(dateTime.toLocalTime());
                    } else if (between.toHours() > 0) {
                        return MessageFormat.format("{0}{1}", between.toHours(), ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "time.hours.ago"));
                    } else if (between.toMinutes() > 0) {
                        return MessageFormat.format("{0}{1}", between.toMinutes(), ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "time.minutes.ago"));
                    } else {
                        return ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "time.now");
                    }
                } else if (between.toDays() == 1) {
                    return MessageFormat.format("{0}, {1}", ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "time.yesterday"), SHORT_TIME_FORMATTER.format(dateTime.toLocalTime()));
                } else if (between.toDays() < 7) {
                    return MessageFormat.format("{0} {1}", between.toDays(), ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "time.days.ago"));
                } else {
                    return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(dateTime);
                }
            }
            return "";
        }

        @Override
        public ZonedDateTime fromString(String string) {
            return null;
        }
    };

    private ObjectProperty<StringConverter<ZonedDateTime>> timeConverter;

    public final StringConverter<ZonedDateTime> getTimeConverter() {
        return timeConverter == null ? DEFAULT_TIME_CONVERTER : timeConverter.get();
    }

    /**
     * A converter that is used to convert the date and time of the notification
     * into a human-readable text. The default converter creates a text like
     * "now", "yesterday", "2 days ago", etc...
     *
     * @return the time converter
     */
    public final ObjectProperty<StringConverter<ZonedDateTime>> timeConverterProperty() {
        if (timeConverter == null) {
            timeConverter = new SimpleObjectProperty<>(this, "timeConverter", DEFAULT_TIME_CONVERTER) {
                @Override
                protected void invalidated() {
                    updateDateAndTimeLabel();
                }
            };
        }
        return timeConverter;
    }

    public final void setTimeConverter(StringConverter<ZonedDateTime> timeConverter) {
        timeConverterProperty().set(timeConverter);
    }

    public class ContentPane extends BorderPane {

        private final StackPane closeIconWrapper;
        private final Label timeLabel;

        private final InvalidationListener updateStackClassListener = it -> updateStackStyle();
        private final WeakInvalidationListener weakUpdateStackClassListener = new WeakInvalidationListener(updateStackClassListener);

        private FadeTransition fadeTransition;

        public ContentPane() {
            getStyleClass().add("content");

            setMinHeight(Region.USE_PREF_SIZE);
            setPickOnBounds(false);

            leftProperty().bind(graphicProperty());

            Label titleLabel = new Label();
            titleLabel.textProperty().bind(notification.titleProperty());
            titleLabel.setWrapText(true);
            titleLabel.setMaxWidth(Double.MAX_VALUE);
            titleLabel.getStyleClass().add("title-label");
            HBox.setHgrow(titleLabel, Priority.ALWAYS);

            BooleanBinding showArrowBinding = Bindings.createBooleanBinding(() -> {
                if ((getContent() == null) || !isHover()) {
                    return false;
                }

                if (notification.getGroup().getNotifications().size() == 1) {
                    return true;
                }

                return notification.getGroup().isExpanded();
            }, hoverProperty(), notification.getGroup().expandedProperty(), notification.getGroup().getNotifications(), contentProperty());

            timeLabel = new Label();
            timeLabel.setMinWidth(Region.USE_PREF_SIZE);
            timeLabel.getStyleClass().add("time-label");
            timeLabel.tooltipProperty().bind(Bindings.createObjectBinding(() -> new Tooltip(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.SHORT).format(notification.getDateTime())), notification.dateTimeProperty()));
            timeLabel.visibleProperty().bind(showArrowBinding.not());

            FontIcon arrowIcon = new FontIcon();
            arrowIcon.getStyleClass().add("arrow");

            StackPane arrowPane = new StackPane(arrowIcon);
            arrowPane.getStyleClass().add("arrow-pane");
            arrowPane.setOnMouseClicked(evt -> {
                evt.consume();
                if (getContent() != null) {
                    setShowContent(!isShowContent());
                }
            });
            arrowPane.visibleProperty().bind(showArrowBinding);

            StackPane upperRightPane = new StackPane(timeLabel, arrowPane);
            upperRightPane.getStyleClass().add("upper-right-pane");
            upperRightPane.setAlignment(Pos.TOP_RIGHT);

            HBox titleTimeBox = new HBox(titleLabel, upperRightPane);
            titleTimeBox.getStyleClass().add("title-time-box");

            Label descriptionLabel = new Label();
            descriptionLabel.textProperty().bind(notification.summaryProperty());
            descriptionLabel.getStyleClass().add("description-label");
            descriptionLabel.setWrapText(true);

            HBox actionsBox = new HBox();
            actionsBox.getStyleClass().add("actions-box");
            actionsBox.visibleProperty().bind(Bindings.isNotEmpty(actionsBox.getChildren()).and(notification.expandedProperty()));
            actionsBox.managedProperty().bind(Bindings.isNotEmpty(actionsBox.getChildren()).and(notification.expandedProperty()));

            notification.getActions().addListener((Observable it) -> updateActions(actionsBox));
            updateActions(actionsBox);

            VBox center = new VBox(titleTimeBox, descriptionLabel, actionsBox);
            center.setFillWidth(true);
            center.setAlignment(Pos.CENTER_LEFT);
            center.getStyleClass().add("text-container");
            center.setMinHeight(Region.USE_PREF_SIZE);
            setCenter(center);

            contentProperty().addListener(it -> updateCenterNode(center));
            showContentProperty().addListener(it -> updateCenterNode(center));

            Label clearAllLabel = new Label(ResourceBundleManager.getString(ResourceBundleManager.Type.NOTIFICATION_VIEW, "group.clear.all"));
            clearAllLabel.getStyleClass().add("clear-all");
            clearAllLabel.setMouseTransparent(true);

            FontIcon closeIcon = new FontIcon();
            closeIcon.setMouseTransparent(true);

            /*
             * Note: the visibility of the close icon and the "clear all" label are
             * specified via CSS. Only one of the two is visible at any time.
             */
            closeIconWrapper = new StackPane(closeIcon, clearAllLabel);
            closeIconWrapper.setPickOnBounds(false);
            closeIconWrapper.setOpacity(0);
            closeIconWrapper.getStyleClass().add("close-icon-wrapper");
            closeIconWrapper.setOnMouseEntered(evt -> requestLayout());
            closeIconWrapper.setOnMouseExited(evt -> requestLayout());
            closeIconWrapper.setOnMouseClicked(evt -> {
                evt.consume();
                NotificationGroup group = notification.getGroup();
                if (group.isExpanded()) {
                    notification.remove();
                } else {
                    group.getNotifications().clear();
                }
            });

            StackPane.setAlignment(closeIconWrapper, Pos.TOP_LEFT);

            /*
             * Move the wrapper to the front, otherwise the graphic / the icon might overlap with
             * the wrapper / the close icon when it gets set after the rest of this view has already
             * been created.
             */
            graphicProperty().addListener(it -> closeIconWrapper.toFront());

            notification.getGroup().expandedProperty().addListener(weakUpdateStackClassListener);
            stackingEnabled.addListener(weakUpdateStackClassListener);
            updateStackStyle();

            hoverProperty().addListener((obs, oldHover, newHover) -> {
                if (fadeTransition != null) {
                    fadeTransition.stop();
                }
                fadeTransition = new FadeTransition(javafx.util.Duration.millis(100), closeIconWrapper);
                fadeTransition.setToValue(newHover ? 1 : 0);
                fadeTransition.play();
            });

            getChildren().add(closeIconWrapper);

            // we are updating the date and time here and also via an animation timer
            updateDateAndTimeLabel();
        }

        private void updateCenterNode(VBox center) {
            Node content = getContent();
            boolean contentIsExpanded = isShowContent() && content != null;
            NotificationView.this.pseudoClassStateChanged(PSEUDO_CLASS_EXPANDED, contentIsExpanded);

            if (contentIsExpanded) {
                if (!center.getChildren().contains(content)) {
                    VBox.setMargin(content, new Insets(5, 0, 0, 0));
                    center.getChildren().add(2, content);
                }
            } else {
                if (content != null) {
                    center.getChildren().remove(content);
                }
            }
        }

        private void updateStackStyle() {
            closeIconWrapper.getStyleClass().remove("stack");
            if (stackingEnabled.get() && !notification.getGroup().isExpanded() && notification.getGroup().getNotifications().size() > 1) {
                closeIconWrapper.getStyleClass().add("stack");
            }
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            // laying this out "manual" as this container is a BorderPane and we want this
            // in the upper left corner.
            closeIconWrapper.resizeRelocate(0, 0, closeIconWrapper.prefWidth(-1), closeIconWrapper.prefHeight(-1));
        }

        private void updateActions(HBox actionsBox) {
            actionsBox.getChildren().clear();

            List<NotificationAction> actions = notification.getActions();
            if (!actions.isEmpty()) {
                actions.forEach(action -> {
                    Button button = new Button();
                    button.textProperty().bind(action.textProperty());
                    HBox.setHgrow(button, Priority.ALWAYS);
                    button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                    button.setPrefWidth(1);
                    button.setOnAction(evt -> {
                        fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE, getNotification()));

                        OnClickBehaviour call = (OnClickBehaviour) action.getOnAction().call(notification);
                        switch (call) {
                            case NONE:
                                break;
                            case REMOVE:
                                notification.remove();
                                break;
                            case HIDE:
                                fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE));
                                break;
                            case HIDE_AND_REMOVE:
                                fireEvent(new InfoCenterEvent(InfoCenterEvent.HIDE));
                                notification.remove();
                                break;
                        }
                    });
                    actionsBox.getChildren().add(button);
                });
            }
        }

        private void updateDateAndTimeLabel() {
            timeLabel.setText(NotificationView.this.getTimeConverter().toString(notification.getDateTime()));
        }
    }

    /**
     * Updates the date and time label with a human-readable text
     * that is relative to the current time, e.g. "now", "yesterday".
     */
    public void updateDateAndTimeLabel() {
        contentPane.updateDateAndTimeLabel();
    }
}