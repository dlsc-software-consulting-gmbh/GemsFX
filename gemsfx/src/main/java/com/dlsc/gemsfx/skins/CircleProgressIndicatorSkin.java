package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CircleProgressIndicator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.util.Duration;

/**
 * Skin for the {@link CircleProgressIndicator} control.
 * <p>
 * The skin renders a complete circular track and progress arc with a centered
 * label or graphic and supplies the circular indeterminate animation.
 */
public class CircleProgressIndicatorSkin extends ArcProgressIndicatorSkin<CircleProgressIndicator> {

    /**
     * Creates a skin for the given circle progress indicator.
     *
     * @param control the circle progress indicator rendered by this skin
     */
    public CircleProgressIndicatorSkin(CircleProgressIndicator control) {
        super(control);
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        trackArc.setLength(360);
        progressArc.startAngleProperty().bind(getSkinnable().startAngleProperty());
    }

    @Override
    protected double getProgressMaxLength() {
        return -360;
    }

    @Override
    protected double computeLabelHeight(double diameter) {
        return diameter;
    }

    @Override
    protected Timeline initIndeterminateAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(rotate.angleProperty(), 0),
                        new KeyValue(progressArc.lengthProperty(), 45)),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(rotate.angleProperty(), 180),
                        new KeyValue(progressArc.lengthProperty(), 180)),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(rotate.angleProperty(), 360),
                        new KeyValue(progressArc.lengthProperty(), 45))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    @Override
    protected DoubleBinding getRadiusBinding(CircleProgressIndicator control) {
        return Bindings.createDoubleBinding(() -> {
            Insets insets = control.getInsets() != null ? control.getInsets() : Insets.EMPTY;
            double totalHorInset = insets.getLeft() + insets.getRight();
            double totalVerInset = insets.getTop() + insets.getBottom();
            double maxInset = Math.max(totalHorInset, totalVerInset);
            double maxRadius = Math.max(trackArc.getStrokeWidth(), progressArc.getStrokeWidth());
            return (Math.min(control.getWidth(), control.getHeight()) - maxInset - maxRadius) / 2;
        }, control.widthProperty(), control.heightProperty(), control.insetsProperty(), trackArc.strokeWidthProperty(), progressArc.strokeWidthProperty());
    }

    @Override
    protected double computeArcCenterY(double contentY, double contentHeight) {
        return contentY + contentHeight / 2;
    }

    @Override
    protected double computeLabelY(double arcCenterY, double labelHeight) {
        return arcCenterY - (labelHeight / 2);
    }

}
