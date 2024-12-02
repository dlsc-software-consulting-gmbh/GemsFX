package com.dlsc.gemsfx;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A simple pane that can be used to overlay the UI with a semi-transparent color,
 * indicating that input is blocked. The glass pane can be animated, which means it
 * will fade in / fade out when it becomes visible or invisible.
 */
public class GlassPane extends StackPane {

    private final FadeTransition fadeTransition = new FadeTransition();

    public GlassPane() {
        getStyleClass().add("glass-pane");

        blockingOpacity.addListener((obs, oldValue, newValue) -> {
            if (newValue.doubleValue() < 0d || newValue.doubleValue() > 1) {
                setBlockingOpacity(oldValue.doubleValue());
            }
        });

        setMouseTransparent(false);
        setVisible(false);

        fadeTransition.durationProperty().bind(fadeInOutDuration);
        fadeTransition.setNode(this);

        hideProperty().addListener((it, oldHide, newHide) -> {
            if (fadeTransition.getStatus().equals(Status.RUNNING)) {
                fadeTransition.stop();
            }

            if (isFadeInOut()) {
                setVisible(true);

                fadeTransition.setFromValue(isHide() ? getBlockingOpacity() : 0);
                fadeTransition.setToValue(isHide() ? 0 : getBlockingOpacity());
                fadeTransition.setOnFinished(evt -> {
                    if (newHide) {
                        setVisible(false);
                    }
                });
                fadeTransition.play();
            } else {
                setOpacity(newHide ? 0 : getBlockingOpacity());
                setVisible(!newHide);
            }
        });
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(GlassPane.class.getResource("glass-pane.css")).toExternalForm();
    }

    private final DoubleProperty blockingOpacity = new SimpleDoubleProperty(this, "blockingOpacity", .5);

    public final double getBlockingOpacity() {
        return blockingOpacity.get();
    }

    /**
     * The opacity value between 0 and 1 that will be used to gray out the
     * content over which the glass pane is place. A separate opacity property is needed
     * to support the fade in / fade out animation AND the regular opacity.
     *
     * @return the opacity of the glass pane
     */
    public final DoubleProperty blockingOpacityProperty() {
        return blockingOpacity;
    }

    public final void setBlockingOpacity(double blockingOpacity) {
        this.blockingOpacity.set(blockingOpacity);
    }

    private final ObjectProperty<Duration> fadeInOutDuration = new SimpleObjectProperty<>(this, "fadeInOutDuration", Duration.millis(100));

    public final Duration getFadeInOutDuration() {
        return fadeInOutDuration.get();
    }

    /**
     * Stores the duration of the fade in / fade out animation.
     *
     * @return the animation duration in milliseconds
     */
    public final ObjectProperty<Duration> fadeInOutDurationProperty() {
        return fadeInOutDuration;
    }

    public final void setFadeInOutDuration(Duration fadeInOutDuration) {
        this.fadeInOutDuration.set(fadeInOutDuration);
    }

    private final BooleanProperty fadeInOut = new SimpleBooleanProperty(this, "fadeInOut");

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    /**
     * A property that determines whether we want to use a fade in / out animation of the glass pane
     * when it gets shown or hidden.
     *
     * @see #fadeInOutDurationProperty()
     * @return true if the fade in / out process will be animated
     */
    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    public final void setFadeInOut(boolean fadeInOut) {
        this.fadeInOut.set(fadeInOut);
    }

    private final BooleanProperty hide = new SimpleBooleanProperty(this, "hide", true);

    /**
     * Controls whether the glass pane is hidden or shown.
     *
     * @return true if the pane is currently hidden
     */
    public final BooleanProperty hideProperty() {
        return hide;
    }

    public final void setHide(boolean hide) {
        this.hide.set(hide);
    }

    public final boolean isHide() {
        return hide.get();
    }
}