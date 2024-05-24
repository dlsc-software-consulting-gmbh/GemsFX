package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ArcProgressIndicator;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Arc;
import javafx.scene.transform.Rotate;
import javafx.util.StringConverter;

public abstract class ArcProgressIndicatorSkin<T extends ArcProgressIndicator> extends SkinBase<T> {

    private static final PseudoClass PSEUDO_CLASS_COMPLETED = PseudoClass.getPseudoClass("completed");
    protected final Label progressLabel = new Label();
    protected final Arc trackArc = new Arc();
    protected final Arc progressArc = new Arc();
    protected final Rotate rotate = new Rotate();
    protected DoubleBinding radiusBinding;
    protected Timeline indeterminateAnimation;

    public ArcProgressIndicatorSkin(T control) {
        super(control);

        initComponents();

        registerListener();

        updateProgress();
    }

    protected void initComponents() {
        T control = getSkinnable();

        // init the progress label
        progressLabel.getStyleClass().add("progress-label");
        progressLabel.setWrapText(true);
        progressLabel.graphicProperty().bind(control.graphicProperty());
        progressLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            double progress = control.getProgress();
            StringConverter<Double> converter = control.getConverter();
            return converter == null ? null : converter.toString(progress);
        }, control.progressProperty(), control.converterProperty()));
        progressLabel.managedProperty().bind(progressLabel.visibleProperty());
        progressLabel.visibleProperty().bind(control.graphicProperty().isNotNull().or(progressLabel.textProperty().isNotEmpty()));

        // calculate the radius of the circle based on the size of the control
        radiusBinding = getRadiusBinding(control);

        // init the track arc
        trackArc.getStyleClass().add("track-circle");
        trackArc.setManaged(false);
        trackArc.radiusXProperty().bind(radiusBinding);
        trackArc.radiusYProperty().bind(radiusBinding);
        trackArc.typeProperty().bind(control.trackArcTypeProperty());

        // init the progress arc
        progressArc.getStyleClass().add("progress-arc");
        progressArc.setManaged(false);
        progressArc.setLength(360);
        progressArc.radiusXProperty().bind(radiusBinding);
        progressArc.radiusYProperty().bind(radiusBinding);
        progressArc.typeProperty().bind(control.progressArcTypeProperty());

        getChildren().addAll(trackArc, progressArc, progressLabel);
    }

    private void registerListener() {
        T control = getSkinnable();

        registerChangeListener(control.progressProperty(), it -> updateProgress());

        registerChangeListener(control.visibleProperty(), it -> {
            if (control.isVisible() && control.getProgress() < 0.0) {
                playAnimation();
            } else {
                pauseAnimation();
            }
        });
    }

    private void updateProgress() {
        T control = getSkinnable();
        double progress = control.getProgress();
        control.pseudoClassStateChanged(PSEUDO_CLASS_COMPLETED, progress == 1.0);

        if (progress < 0.0) {
            if (control.isVisible()) {
                playAnimation();
            } else {
                pauseAnimation();
            }
        } else {
            stopAnimation();
            progressArc.setLength(getProgressMaxLength() * progress);
        }
    }

    protected void stopAnimation() {
        progressArc.getTransforms().remove(rotate);
        if (animationIsRunning()) {
            indeterminateAnimation.stop();
        }
    }

    private void pauseAnimation() {
        if (animationIsRunning()) {
            indeterminateAnimation.pause();
        }
    }

    private void playAnimation() {
        if (indeterminateAnimation == null) {
            indeterminateAnimation = initIndeterminateAnimation();
        }

        if (indeterminateAnimation.getStatus() != Animation.Status.RUNNING) {
            if (!progressArc.getTransforms().contains(rotate)) {
                progressArc.getTransforms().add(rotate);
            }
            indeterminateAnimation.play();
        }
    }

    private boolean animationIsRunning() {
        return indeterminateAnimation != null && indeterminateAnimation.getStatus() == Animation.Status.RUNNING;
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double arcCenterX = computeAcrCenterX(contentX, contentWidth);
        double arcCenterY = computeArcCenterY(contentY, contentHeight);

        // set the pivot point for the rotation
        rotate.setPivotX(arcCenterX - progressArc.getLayoutX());
        rotate.setPivotY(arcCenterY - progressArc.getLayoutY());

        // layout the arcs
        trackArc.setCenterX(arcCenterX);
        trackArc.setCenterY(arcCenterY);
        progressArc.setCenterX(arcCenterX);
        progressArc.setCenterY(arcCenterY);
        trackArc.resize(contentWidth, contentHeight);
        progressArc.resize(contentWidth, contentHeight);

        // layout the progress label
        double maxStrokeWidth = Math.max(trackArc.getStrokeWidth(), progressArc.getStrokeWidth());
        double diameter = (radiusBinding.get() - maxStrokeWidth) * 2;

        double labelMaxWidth = computeLabelWidth(diameter);
        double labelMaxHeight = computeLabelHeight(diameter);

        progressLabel.setMaxWidth(labelMaxWidth);
        progressLabel.setMaxHeight(labelMaxHeight);
        progressLabel.setPrefWidth(labelMaxWidth);
        progressLabel.setPrefHeight(labelMaxHeight);

        double labelWidth = Math.min(progressLabel.prefWidth(diameter), diameter);
        double labelHeight = Math.min(progressLabel.prefHeight(labelWidth), diameter);

        double labelX = computeLabelX(arcCenterX, labelWidth);
        double labelY = computeLabelY(arcCenterY, labelHeight);

        progressLabel.resizeRelocate(labelX, labelY, labelWidth, labelHeight);
    }

    protected double computeLabelWidth(double diameter) {
        return diameter;
    }

    protected double computeAcrCenterX(double contentX, double contentWidth) {
        return contentX + contentWidth / 2;
    }

    protected double computeLabelX(double arcCenterX, double labelWidth) {
        return arcCenterX - (labelWidth / 2);
    }

    /**
     * Returns the height of the label.
     */
    protected abstract double computeLabelHeight(double diameter);

    /**
     * Returns the y-coordinate of the center of the progress arc / track arc.
     */
    protected abstract double computeArcCenterY(double contentY, double contentHeight);


    /**
     * Returns the y-coordinate of the label.
     */
    protected abstract double computeLabelY(double arcCenterY, double labelHeight);

    /**
     * Initializes the animation that is used when the progress is indeterminate.
     */
    protected abstract Timeline initIndeterminateAnimation();

    /**
     * Returns a binding that calculates the radius of the circle based on the size of the control.
     */
    protected abstract DoubleBinding getRadiusBinding(T control);

    /**
     * Returns the maximum length of the progress arc.
     */
    protected abstract double getProgressMaxLength();

}
