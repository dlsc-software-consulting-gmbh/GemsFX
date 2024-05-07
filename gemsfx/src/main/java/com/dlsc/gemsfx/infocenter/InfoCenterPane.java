package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.skins.InfoCenterPaneSkin;
import javafx.animation.AnimationTimer;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import one.jpro.jproutils.treeshowing.TreeShowing;

/**
 * A pane used for managing a single instance of {@link InfoCenterView}. The pane will show or
 * hide the view depending on various criteria, e.g. events that have been received or the auto-hide
 * feature is kicking in.
 * <p>
 * <b>Note:</b> There is a known issue with the InfoCenterPane component on <b>Windows</b> systems where
 * ghost images of previously hidden InfoCenterViews may appear occasionally. This problem does not occur
 * on <b>macOS</b>, suggesting it may be a bug specific to the JavaFX implementation on Windows.
 * </p>
 * <p>
 * <b>Workaround:</b> To prevent this issue, avoid using InfoCenterPane directly as the root of a Scene.
 * Instead, wrap InfoCenterPane in another layout container, such as a StackPane or another suitable
 * container. This arrangement helps to resolve the rendering anomaly.
 * <pre>
 *    InfoCenterPane infoCenterPane = new InfoCenterPane();
 *    StackPane root = new StackPane(infoCenterPane);
 * </pre>
 * </p>
 */
public class InfoCenterPane extends Control {

    private static final Duration DEFAULT_SLIDE_IN_DURATION = Duration.millis(200);
    private static final Duration DEFAULT_AUTO_HIDE_DURATION = Duration.seconds(5);
    private static final boolean DEFAULT_PINNED = false;
    private static final boolean DEFAULT_AUTO_HIDE = true;
    private static final boolean DEFAULT_SHOW_INFO_CENTER = false;

    /*
     * The managed info center instance.
     */
    private final InfoCenterView infoCenterView = new InfoCenterView();

    /*
     * A flag used to signal whether the mouse cursor is currently inside the info center view or not.
     */
    private final ReadOnlyBooleanWrapper insideInfoCenter = new ReadOnlyBooleanWrapper(this, "insideInfoCenter");

    private final EventHandler<MouseEvent> mouseClickedHandler = evt -> {
        // hide the info center whenever the user clicks on the content node
        if (evt.getTarget() == getContent()) {
            if (isAutoHide() && !isPinned()) {
                setShowInfoCenter(false);
            }
        }
    };

    private final WeakEventHandler<MouseEvent> weakMouseClickedHandler = new WeakEventHandler<>(mouseClickedHandler);

    /**
     * Constructs a new pane with no content.
     */
    public InfoCenterPane() {
        this(null);

        setFocusTraversable(false);
    }

    /**
     * Constructs a new pane with the given content node.
     */
    public InfoCenterPane(Node content) {
        getStyleClass().add("info-center-pane");

        infoCenterView.showAllGroupProperty().addListener(it -> requestLayout());

        // attach or remove the mouse handler when the content node gets replaced
        contentProperty().addListener((it, oldContent, newContent) -> {
            if (oldContent != null) {
                oldContent.removeEventFilter(MouseEvent.MOUSE_CLICKED, weakMouseClickedHandler);
            }
            if (newContent != null) {
                newContent.addEventFilter(MouseEvent.MOUSE_CLICKED, weakMouseClickedHandler);
            }
        });

        setContent(content);

        // show the info center whenever the pane receives an event telling it that a new notification has arrived
        infoCenterView.addEventHandler(InfoCenterEvent.NOTIFICATION_ADDED, evt -> setShowInfoCenter(true));

        // hide the info center (if not pinned) when an explicit request to do so was received
        infoCenterView.addEventHandler(InfoCenterEvent.HIDE, evt -> {
            if (!isPinned()) {
                setShowInfoCenter(false);
            }
        });

        // hide the info center (if not  pinned) when all notifications have been removed
        infoCenterView.getUnmodifiableNotifications().addListener((Observable it) -> {
            if (infoCenterView.getUnmodifiableNotifications().isEmpty()) {
                if (!isPinned()) {
                    setShowInfoCenter(false);
                }
            }
        });

        // hide the info center (if not pinned) when the user clicks on the background of the info center
        infoCenterView.setOnMouseClicked(evt -> {
            if (!evt.isConsumed() &&
                    evt.isStillSincePress() &&
                    evt.getButton().equals(MouseButton.PRIMARY) &&
                    evt.getSource() == infoCenterView) {
                if (!isPinned()) {
                    setShowInfoCenter(false);
                }
                evt.consume();
            }
        });

        // show / or hide the info center when the "pinned" property changes
        pinnedProperty().addListener(it -> setShowInfoCenter(isPinned()));

        /*
         * We use an animation timer to automatically hide the info center after
         * a specific time out period.
         */
        AnimationTimer timer = new AnimationTimer() {

            private long lastToggle;
            private boolean reset;

            {
                // the timer gets reset whenever the visibility of the info center changes
                showInfoCenterProperty().addListener(it -> {
                    if (isShowInfoCenter()) {
                        reset();
                    }
                });

                // the timer gets reset whenever the mouse cursor gets moved outside the info center
                insideInfoCenter.addListener(it -> {
                    if (!insideInfoCenter.get()) {
                        reset();
                    }
                });

                // the timer gets reset whenever a notification event gets fired
                infoCenterView.addEventHandler(InfoCenterEvent.ANY, evt -> reset());
            }

            private void reset() {
                reset = true;
            }

            @Override
            public void handle(long now) {
                if (lastToggle == 0L || reset) {
                    lastToggle = now;
                    reset = false;
                } else {
                    long diff = now - lastToggle;

                    // Update interval: 1_000,000,000ns == 1000ms == 1s
                    if (diff >= getAutoHideDuration().toMillis() * 1_000_000) {
                        if (!isPinned() && !insideInfoCenter.get() && isAutoHide()) {
                            setShowInfoCenter(false);
                        }
                        lastToggle = now;
                    }
                }
            }
        };

        TreeShowing.treeShowing(this).addListener((p, o, n) -> {
            if (n) {
                timer.start();
            } else {
                timer.stop();
            }
        });

        timer.start();

        // monitor the mouse cursor
        infoCenterView.addEventHandler(MouseEvent.MOUSE_ENTERED, evt -> insideInfoCenter.set(true));
        infoCenterView.addEventHandler(MouseEvent.MOUSE_EXITED, evt -> insideInfoCenter.set(false));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new InfoCenterPaneSkin(this);
    }

    private ObjectProperty<Duration> autoHideDuration;

    public final Duration getAutoHideDuration() {
        return autoHideDuration == null ? DEFAULT_AUTO_HIDE_DURATION : autoHideDuration.get();
    }

    /**
     * A duration after which the pane will automatically hide the info center (if it isn't currently pinned
     * and the mouse cursor is not on top of the info center).
     *
     * @return the auto-hide duration
     */
    public final ObjectProperty<Duration> autoHideDurationProperty() {
        if (autoHideDuration == null) {
            autoHideDuration = new SimpleObjectProperty<>(this, "autoHideDuration", DEFAULT_AUTO_HIDE_DURATION);
        }
        return autoHideDuration;
    }

    public final void setAutoHideDuration(Duration autoHideDuration) {
        autoHideDurationProperty().set(autoHideDuration);
    }

    private BooleanProperty pinned;

    public final boolean isPinned() {
        return pinned == null ? DEFAULT_PINNED : pinned.get();
    }

    /**
     * A flag that can be used to pin the info center view so that it will not hide
     * under any circumstances.
     *
     * @return true if the info center view is pinned
     */
    public final BooleanProperty pinnedProperty() {
        if (pinned == null) {
            pinned = new SimpleBooleanProperty(this, "pinned", DEFAULT_PINNED);
        }
        return pinned;
    }

    public final void setPinned(boolean pinned) {
        pinnedProperty().set(pinned);
    }

    private BooleanProperty autoHide;

    public final boolean isAutoHide() {
        return autoHide == null ? DEFAULT_AUTO_HIDE : autoHide.get();
    }

    /**
     * A flag that determines if the info center view should automatically disappear again
     * after a certain timeout duration.
     *
     * @return true if the info center hides automatically after a certain period of time
     * @see #autoHideDuration
     */
    public final BooleanProperty autoHideProperty() {
        if (autoHide == null) {
            autoHide = new SimpleBooleanProperty(this, "autoHide", DEFAULT_AUTO_HIDE);
        }
        return autoHide;
    }

    public final void setAutoHide(boolean autoHide) {
        autoHideProperty().set(autoHide);
    }

    public final InfoCenterView getInfoCenterView() {
        return infoCenterView;
    }

    private ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); //$NON-NLS-1$

    /**
     * The property that is used to store a reference to the content node. The
     * content node will fill the entire width and height of the pane.
     *
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return contentProperty().get();
    }

    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    // slide in / slide out duration

    private ObjectProperty<Duration> slideInDuration; //$NON-NLS-1$

    /**
     * The duration used for the "slide in" / "slide out" animation when the info center view
     * gets shown or hidden.
     *
     * @return animation duration for the sliding in and out of the info center view
     */
    public final ObjectProperty<Duration> slideInDurationProperty() {
        if (slideInDuration == null) {
            slideInDuration = new SimpleObjectProperty<>(this, "slideInDuration", DEFAULT_SLIDE_IN_DURATION);
        }
        return slideInDuration;
    }

    public final Duration getSlideInDuration() {
        return slideInDuration == null ? DEFAULT_SLIDE_IN_DURATION : slideInDuration.get();
    }

    public final void setSlideInDuration(Duration duration) {
        slideInDurationProperty().set(duration);
    }

    private BooleanProperty showInfoCenter;

    public final boolean isShowInfoCenter() {
        return showInfoCenter == null ? DEFAULT_SHOW_INFO_CENTER : showInfoCenter.get();
    }

    /**
     * The flag that controls whether the info center shall be shown or not.
     *
     * @return true if the info center shall be shown
     */
    public final BooleanProperty showInfoCenterProperty() {
        if (showInfoCenter == null) {
            showInfoCenter = new SimpleBooleanProperty(this, "showInfoCenter", DEFAULT_SHOW_INFO_CENTER);
        }
        return showInfoCenter;
    }

    public final void setShowInfoCenter(boolean showInfoCenter) {
        showInfoCenterProperty().set(showInfoCenter);
    }
}
