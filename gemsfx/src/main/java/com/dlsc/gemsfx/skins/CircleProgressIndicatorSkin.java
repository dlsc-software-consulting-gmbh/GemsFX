package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CircleProgressIndicator;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class CircleProgressIndicatorSkin extends SkinBase<CircleProgressIndicator> {

    private static final PseudoClass PSEUDO_CLASS_COMPLETED = PseudoClass.getPseudoClass("completed");
    private final Label progressLabel = new Label();
    private final Circle trackCircle = new Circle();
    private final Arc progressArc = new Arc();
    private final DoubleBinding radiusBinding;
    private final Rotate rotate = new Rotate();
    private Timeline indeterminateAnimation;

    public CircleProgressIndicatorSkin(CircleProgressIndicator control) {
        super(control);

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
        radiusBinding = Bindings.createDoubleBinding(() -> {
            Insets insets = control.getInsets() != null ? control.getInsets() : Insets.EMPTY;
            double totalHorInset = insets.getLeft() + insets.getRight();
            double totalVerInset = insets.getTop() + insets.getBottom();
            double maxInset = Math.max(totalHorInset, totalVerInset);
            double maxRadius = Math.max(trackCircle.getStrokeWidth(), progressArc.getStrokeWidth());
            return (Math.min(control.getWidth(), control.getHeight()) - maxInset - maxRadius) / 2;
        }, control.widthProperty(), control.heightProperty(), control.insetsProperty(), trackCircle.strokeWidthProperty(), progressArc.strokeWidthProperty());

        // init the track circle
        trackCircle.getStyleClass().add("track-circle");
        trackCircle.setManaged(false);
        trackCircle.radiusProperty().bind(radiusBinding);

        // init the progress arc
        progressArc.getStyleClass().add("progress-arc");
        progressArc.setManaged(false);
        progressArc.setStartAngle(90);
        progressArc.setLength(360);
        progressArc.radiusXProperty().bind(radiusBinding);
        progressArc.radiusYProperty().bind(radiusBinding);

        getChildren().addAll(trackCircle, progressArc, progressLabel);
        updateProgress();

        registerListener(control);
    }

    private void registerListener(CircleProgressIndicator control) {
        registerChangeListener(control.progressProperty(), it -> updateProgress());
        registerChangeListener(control.visibleProperty(), it -> {
            if (control.isVisible() && control.getProgress() < 0.0) {
                playAnimation();
            } else {
                pauseAnimation();
            }
        });
        registerChangeListener(control.arcTypeProperty(), it -> {
            ArcType arcType = control.getArcType();
            //trackArc.setType(arcType);
            progressArc.setType(arcType);
        });
    }

    private void updateProgress() {
        CircleProgressIndicator control = getSkinnable();
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
            progressArc.setLength(-360 * progress);
        }
    }

    private void stopAnimation() {
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
            initIndeterminateAnimation();
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

    private void initIndeterminateAnimation() {
        indeterminateAnimation = new Timeline(
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
        indeterminateAnimation.setCycleCount(Animation.INDEFINITE);
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double centerX = contentX + contentWidth / 2;
        double centerY = contentY + contentHeight / 2;

        // set the pivot point for the rotation
        rotate.setPivotX(centerX - progressArc.getLayoutX());
        rotate.setPivotY(centerY - progressArc.getLayoutY());

        // layout the arcs
        trackCircle.setCenterX(centerX);
        trackCircle.setCenterY(centerY);
        progressArc.setCenterX(centerX);
        progressArc.setCenterY(centerY);
        trackCircle.resize(contentWidth, contentHeight);
        progressArc.resize(contentWidth, contentHeight);

        // layout the progress label
        double maxStrokeWidth = Math.max(trackCircle.getStrokeWidth(), progressArc.getStrokeWidth());
        double diameter = (radiusBinding.get() - maxStrokeWidth) * 2;

        progressLabel.setMaxWidth(diameter);
        progressLabel.setMaxHeight(diameter);
        progressLabel.setPrefWidth(diameter);
        progressLabel.setPrefHeight(diameter);

        double labelWidth = Math.min(progressLabel.prefWidth(diameter), diameter);
        double labelHeight = Math.min(progressLabel.prefHeight(labelWidth), diameter);

        double labelX = centerX - (labelWidth / 2);
        double labelY = centerY - (labelHeight / 2);

        progressLabel.resizeRelocate(labelX, labelY, labelWidth, labelHeight);
    }

}
