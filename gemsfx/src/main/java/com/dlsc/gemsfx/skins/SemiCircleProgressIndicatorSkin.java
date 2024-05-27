package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SemiCircleProgressIndicator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.geometry.Insets;
import javafx.util.Duration;

public class SemiCircleProgressIndicatorSkin extends ArcProgressIndicatorSkin<SemiCircleProgressIndicator> {

    public SemiCircleProgressIndicatorSkin(SemiCircleProgressIndicator control) {
        super(control);
    }

    @Override
    protected void initComponents() {
        super.initComponents();

        trackArc.setStartAngle(0);
        trackArc.setLength(180);
    }

    @Override
    protected double getProgressMaxLength() {
        return -180;
    }

    protected void stopAnimation() {
        super.stopAnimation();
        progressArc.setStartAngle(180);
    }

    @Override
    protected double computeLabelHeight(double diameter) {
        return diameter / 2;
    }

    protected Timeline initIndeterminateAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(progressArc.startAngleProperty(), 180),
                        new KeyValue(progressArc.lengthProperty(), 0)),
                new KeyFrame(Duration.seconds(0.75),
                        new KeyValue(progressArc.startAngleProperty(), 90),
                        new KeyValue(progressArc.lengthProperty(), -60)),
                new KeyFrame(Duration.seconds(1.5),
                        new KeyValue(progressArc.startAngleProperty(), 0),
                        new KeyValue(progressArc.lengthProperty(), 0))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        return timeline;
    }

    @Override
    protected DoubleBinding getRadiusBinding(SemiCircleProgressIndicator control) {
        return Bindings.createDoubleBinding(() -> {
            Insets insets = control.getInsets() != null ? control.getInsets() : Insets.EMPTY;
            double totalHorInset = insets.getLeft() + insets.getRight();
            double totalVerInset = insets.getTop() + insets.getBottom();
            double maxRadius = Math.max(trackArc.getStrokeWidth(), progressArc.getStrokeWidth());
            return (Math.min(control.getWidth() - totalHorInset - maxRadius, (control.getHeight() - totalVerInset - maxRadius) * 2)) / 2;
        }, control.widthProperty(), control.heightProperty(), control.insetsProperty(), trackArc.strokeWidthProperty(), progressArc.strokeWidthProperty());
    }

    @Override
    protected double computeArcCenterY(double contentY, double contentHeight) {
        return contentY + contentHeight / 2 + radiusBinding.get() / 2;
    }

    protected double computeLabelY(double centerY, double labelHeight) {
        return centerY - labelHeight;
    }

}
