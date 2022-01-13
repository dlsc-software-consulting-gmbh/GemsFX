package com.dlsc.gemsfx;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class GlassPane extends StackPane {

    private final FadeTransition fadeTransition = new FadeTransition();

    public GlassPane() {
        getStyleClass().add("glass-pane");

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

                fadeTransition.setFromValue(isHide() ? .5 : 0);
                fadeTransition.setToValue(isHide() ? 0 : .5);
                fadeTransition.setOnFinished(evt -> {
                    if (newHide) {
                        setVisible(false);
                    }
                });
                fadeTransition.play();
            } else {
                setOpacity(newHide ? 0 : .5);
                setVisible(!newHide);
            }
        });
    }

    private final ObjectProperty<Duration> fadeInOutDuration = new SimpleObjectProperty<>(this, "fadeInOutDuration", Duration.millis(100));

    public Duration getFadeInOutDuration() {
        return fadeInOutDuration.get();
    }

    public ObjectProperty<Duration> fadeInOutDurationProperty() {
        return fadeInOutDuration;
    }

    public void setFadeInOutDuration(Duration fadeInOutDuration) {
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