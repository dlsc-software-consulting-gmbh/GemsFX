package com.dlsc.gemsfx;

import javafx.animation.Animation.Status;
import javafx.animation.FadeTransition;
import javafx.beans.property.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

import com.dlsc.gemsfx.util.DurationConverter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableDoubleProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.SizeConverter;

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

    private final DoubleProperty blockingOpacity = new StyleableDoubleProperty(.5) {
        @Override public Object getBean() { return GlassPane.this; }
        @Override public String getName() { return "blockingOpacity"; }
        @Override public CssMetaData<? extends Styleable, Number> getCssMetaData() { return StyleableProperties.BLOCKING_OPACITY; }
    };

    public final double getBlockingOpacity() {
        return blockingOpacity.get();
    }

    /**
     * The opacity value between 0 and 1 that will be used to gray out the
     * content over which the glass pane is place. A separate opacity property is needed
     * to support the fade in / fade out animation AND the regular opacity.
     * <p>
     * Can be set via CSS using the {@code -fx-blocking-opacity} property.
     * Valid values are: a number between 0 and 1.
     * The default value is {@code 0.5}.
     * </p>
     *
     * @return the opacity of the glass pane
     */
    public final DoubleProperty blockingOpacityProperty() {
        return blockingOpacity;
    }

    public final void setBlockingOpacity(double blockingOpacity) {
        this.blockingOpacity.set(blockingOpacity);
    }

    private final ObjectProperty<Duration> fadeInOutDuration = new StyleableObjectProperty<>(Duration.millis(100)) {
        @Override public Object getBean() { return GlassPane.this; }
        @Override public String getName() { return "fadeInOutDuration"; }
        @Override public CssMetaData<? extends Styleable, Duration> getCssMetaData() { return StyleableProperties.FADE_IN_OUT_DURATION; }
    };

    public final Duration getFadeInOutDuration() {
        return fadeInOutDuration.get();
    }

    /**
     * Stores the duration of the fade in / fade out animation.
     * <p>
     * Can be set via CSS using the {@code -fx-fade-in-out-duration} property.
     * Valid values are: a number in milliseconds.
     * The default value is {@code 100}.
     * </p>
     *
     * @return the animation duration in milliseconds
     */
    public final ObjectProperty<Duration> fadeInOutDurationProperty() {
        return fadeInOutDuration;
    }

    public final void setFadeInOutDuration(Duration fadeInOutDuration) {
        this.fadeInOutDuration.set(fadeInOutDuration);
    }

    private final BooleanProperty fadeInOut = new StyleableBooleanProperty(false) {
        @Override public Object getBean() { return GlassPane.this; }
        @Override public String getName() { return "fadeInOut"; }
        @Override public CssMetaData<? extends Styleable, Boolean> getCssMetaData() { return StyleableProperties.FADE_IN_OUT; }
    };

    public final boolean isFadeInOut() {
        return fadeInOut.get();
    }

    /**
     * A property that determines whether we want to use a fade in / out animation of the glass pane
     * when it gets shown or hidden.
     * <p>
     * Can be set via CSS using the {@code -fx-fade-in-out} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code false}.
     * </p>
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

    private static class StyleableProperties {

        private static final CssMetaData<GlassPane, Number> BLOCKING_OPACITY =
                new CssMetaData<>("-fx-blocking-opacity", SizeConverter.getInstance(), 0.5) {
                    @Override
                    public boolean isSettable(GlassPane n) {
                        return !n.blockingOpacity.isBound();
                    }
                    @Override
                    public StyleableProperty<Number> getStyleableProperty(GlassPane n) {
                        return (StyleableProperty<Number>) n.blockingOpacityProperty();
                    }
                };

        private static final CssMetaData<GlassPane, Boolean> FADE_IN_OUT =
                new CssMetaData<>("-fx-fade-in-out", BooleanConverter.getInstance(), Boolean.FALSE) {
                    @Override
                    public boolean isSettable(GlassPane n) {
                        return !n.fadeInOut.isBound();
                    }
                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(GlassPane n) {
                        return (StyleableProperty<Boolean>) n.fadeInOutProperty();
                    }
                };

        private static final CssMetaData<GlassPane, Duration> FADE_IN_OUT_DURATION =
                new CssMetaData<>("-fx-fade-in-out-duration", DurationConverter.getInstance(), Duration.millis(100)) {
                    @Override
                    public boolean isSettable(GlassPane n) {
                        return !n.fadeInOutDuration.isBound();
                    }
                    @Override
                    public StyleableProperty<Duration> getStyleableProperty(GlassPane n) {
                        return (StyleableProperty<Duration>) n.fadeInOutDurationProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(StackPane.getClassCssMetaData());
            Collections.addAll(styleables, BLOCKING_OPACITY, FADE_IN_OUT, FADE_IN_OUT_DURATION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}