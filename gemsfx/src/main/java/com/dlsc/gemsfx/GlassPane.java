package com.dlsc.gemsfx;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * A simple pane that can be used to overlay the UI with a semi-transparent color,
 * indicating that input is blocked.
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

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY, Insets.EMPTY)));
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

    private final DoubleProperty blockingOpacity = new SimpleDoubleProperty(this, "blockingOpacity", .5);

    public final double getBlockingOpacity() {
        return blockingOpacity.get();
    }

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

    public final BooleanProperty fadeInOutProperty() {
        return fadeInOut;
    }

    private final BooleanProperty hide = new SimpleBooleanProperty(this, "hide", true);

    public final BooleanProperty hideProperty() {
        return hide;
    }

    public final boolean isHide() {
        return hide.get();
    }
}