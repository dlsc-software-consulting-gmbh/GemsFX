package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ArcProgressIndicator;
import javafx.animation.Animation;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;
import javafx.scene.transform.Rotate;
import javafx.util.StringConverter;

/**
 * Base skin for arc-based progress indicators.
 * <p>
 * The skin renders the control with a track arc, a progress arc, and an optional
 * label or graphic. Subclasses provide geometry and animation details
 * for concrete {@link ArcProgressIndicator} controls.
 *
 * @param <T> the type of progress indicator rendered by this skin
 */
public abstract class ArcProgressIndicatorSkin<T extends ArcProgressIndicator> extends GemsSkinBase<T> {

    private static final PseudoClass PSEUDO_CLASS_COMPLETED = PseudoClass.getPseudoClass("completed");
    /**
     * The label used to display the formatted progress value or control graphic.
     */
    protected final Label progressLabel = new Label();

    /**
     * The arc that renders the full track behind the progress arc.
     */
    protected final Arc trackArc = new Arc();

    /**
     * The arc that renders the current progress value.
     */
    protected final Arc progressArc = new Arc();

    /**
     * The rotation transform used while the progress is indeterminate.
     */
    protected final Rotate rotate = new Rotate();

    /**
     * Binding that computes the arc radius from the skinnable control.
     */
    protected DoubleBinding radiusBinding;

    /**
     * Timeline used for indeterminate progress animation.
     */
    protected Timeline indeterminateAnimation;

    /**
     * Creates a skin for the given arc progress indicator.
     *
     * @param control the progress indicator rendered by this skin
     */
    public ArcProgressIndicatorSkin(T control) {
        super(control);

        initComponents();

        registerListener();

        updateProgress();
    }

    /**
     * Initializes the arcs, label, and child nodes used by this skin.
     */
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

    /**
     * Stops the indeterminate animation and removes its rotation transform.
     */
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

    /**
     * Computes the maximum width available for the progress label.
     *
     * @param diameter the available diameter inside the progress arc
     * @return the label width
     */
    protected double computeLabelWidth(double diameter) {
        return diameter;
    }

    /**
     * Computes the x-coordinate of the center of the progress arc and track arc.
     *
     * @param contentX the x-coordinate of the content area
     * @param contentWidth the width of the content area
     * @return the x-coordinate of the arc center
     */
    protected double computeAcrCenterX(double contentX, double contentWidth) {
        return contentX + contentWidth / 2;
    }

    /**
     * Computes the x-coordinate of the progress label.
     *
     * @param arcCenterX the x-coordinate of the arc center
     * @param labelWidth the computed label width
     * @return the x-coordinate of the label
     */
    protected double computeLabelX(double arcCenterX, double labelWidth) {
        return arcCenterX - (labelWidth / 2);
    }

    /**
     * Returns the height of the label.
     *
     * @param diameter the available diameter inside the progress arc
     * @return the label height
     */
    protected abstract double computeLabelHeight(double diameter);

    /**
     * Returns the y-coordinate of the center of the progress arc and track arc.
     *
     * @param contentY the y-coordinate of the content area
     * @param contentHeight the height of the content area
     * @return the y-coordinate of the arc center
     */
    protected abstract double computeArcCenterY(double contentY, double contentHeight);


    /**
     * Returns the y-coordinate of the label.
     *
     * @param arcCenterY the y-coordinate of the arc center
     * @param labelHeight the computed label height
     * @return the y-coordinate of the label
     */
    protected abstract double computeLabelY(double arcCenterY, double labelHeight);

    /**
     * Initializes the animation that is used when the progress is indeterminate.
     *
     * @return the indeterminate animation timeline
     */
    protected abstract Timeline initIndeterminateAnimation();

    /**
     * Returns a binding that calculates the radius of the circle based on the size of the control.
     *
     * @param control the progress indicator rendered by this skin
     * @return the radius binding
     */
    protected abstract DoubleBinding getRadiusBinding(T control);

    /**
     * Returns the maximum length of the progress arc.
     *
     * @return the maximum progress arc length
     */
    protected abstract double getProgressMaxLength();

}
