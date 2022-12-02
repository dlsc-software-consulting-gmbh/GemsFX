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
 */
public class InfoCenterPane extends Control {

    @Override
    public void requestLayout() {
        super.requestLayout();

//        Thread.dumpStack();
    }

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
    }

    /**
     * Constructs a new pane with the given content node.
     */
    public InfoCenterPane(Node content) {
        getStyleClass().add("notification-pane");

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
        pinned.addListener(it -> setShowInfoCenter(isPinned()));

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

        TreeShowing.treeShowing(this).addListener((p,o,n) -> {
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

    private final ObjectProperty<Duration> autoHideDuration = new SimpleObjectProperty<>(this, "autoHideDuration", Duration.seconds(5));

    public final Duration getAutoHideDuration() {
        return autoHideDuration.get();
    }

    /**
     * A duration after which the pane will automatically hide the info center (if it isn't currently pinned
     * and the mouse cursor is not on top of the info center).
     *
     * @return the auto-hide duration
     */
    public final ObjectProperty<Duration> autoHideDurationProperty() {
        return autoHideDuration;
    }

    public final void setAutoHideDuration(Duration autoHideDuration) {
        this.autoHideDuration.set(autoHideDuration);
    }

    private final BooleanProperty pinned = new SimpleBooleanProperty(this, "pinned", false);

    public final boolean isPinned() {
        return pinned.get();
    }

    /**
     * A flag that can be used to pin the info center view so that it will not hide
     * under any circumstances.
     *
     * @return true if the info center view is pinned
     */
    public final BooleanProperty pinnedProperty() {
        return pinned;
    }

    public final void setPinned(boolean pinned) {
        this.pinned.set(pinned);
    }

    private final BooleanProperty autoHide = new SimpleBooleanProperty(this, "autoHide", true);

    public final boolean isAutoHide() {
        return autoHide.get();
    }

    /**
     * A flag that determines if the info center view should automatically disappear again
     * after a certain timeout duration.
     *
     * @see #autoHideDuration
     *
     * @return true if the info center hides automatically after a certain period of time
     */
    public final BooleanProperty autoHideProperty() {
        return autoHide;
    }

    public final void setAutoHide(boolean autoHide) {
        this.autoHide.set(autoHide);
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

    private final ObjectProperty<Duration> slideInDuration = new SimpleObjectProperty<>(this, "slideDuration", Duration.millis(200)); //$NON-NLS-1$

    /**
     * The duration used for the "slide in" / "slide out" animation when the info center view
     * gets shown or hidden.
     *
     * @return animation duration for the sliding in and out of the info center view
     */
    public final ObjectProperty<Duration> slideInDuration() {
        return slideInDuration;
    }

    public final Duration getSlideInDuration() {
        return slideInDuration.get();
    }

    public final void setSlideInDuration(Duration duration) {
        slideInDuration.set(duration);
    }

    private final BooleanProperty showInfoCenter = new SimpleBooleanProperty(this, "showInfoCenter", false);

    public final boolean isShowInfoCenter() {
        return showInfoCenter.get();
    }

    /**
     * The flag that controls whether the info center shall be shown or not.
     *
     * @return true if the info center shall be shown
     */
    public final BooleanProperty showInfoCenterProperty() {
        return showInfoCenter;
    }

    public final void setShowInfoCenter(boolean showInfoCenter) {
        this.showInfoCenter.set(showInfoCenter);
    }
}
