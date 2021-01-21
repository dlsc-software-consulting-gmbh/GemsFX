/*
 * Copyright (c) 2016 by Gerrit Grunwald
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dlsc.gemsfx.skins.dialog;

import javafx.animation.Animation;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.DoublePropertyBase;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;


/**
 * Created by hansolo on 08.04.16.
 */
public class CircularProgressIndicator extends Region {
    private static final double PREFERRED_WIDTH = 24;
    private static final double PREFERRED_HEIGHT = 24;
    private static final double MINIMUM_WIDTH = 12;
    private static final double MINIMUM_HEIGHT = 12;
    private static final double MAXIMUM_WIDTH = 1024;
    private static final double MAXIMUM_HEIGHT = 1024;
    private final DoubleProperty dashOffset = new SimpleDoubleProperty(0);
    private final DoubleProperty dashArray_0 = new SimpleDoubleProperty(1);
    private StackPane indeterminatePane;
    private Pane progressPane;
    private Circle circle;
    private Arc arc;
    private final Timeline timeline;
    private RotateTransition indeterminatePaneRotation;
    private final InvalidationListener listener;
    private final DoubleProperty progress;
    private final BooleanProperty indeterminate;
    private final BooleanProperty roundLineCap;
    private boolean isRunning;


    // ******************** Constructors **************************************
    public CircularProgressIndicator() {
        getStyleClass().add("circular-progress");
        progress = new DoublePropertyBase(0) {
            @Override
            public void invalidated() {
                if (get() < 0) {
                    startIndeterminate();
                } else {
                    stopIndeterminate();
                    set(Math.max(0, Math.min(1, get())));
                    redraw();
                }
            }

            @Override
            public Object getBean() {
                return CircularProgressIndicator.this;
            }

            @Override
            public String getName() {
                return "progress";
            }
        };
        indeterminate = new BooleanPropertyBase(false) {
            @Override
            public Object getBean() {
                return CircularProgressIndicator.this;
            }

            @Override
            public String getName() {
                return "indeterminate";
            }
        };
        roundLineCap = new BooleanPropertyBase(false) {
            @Override
            public void invalidated() {
                if (get()) {
                    circle.setStrokeLineCap(StrokeLineCap.ROUND);
                    arc.setStrokeLineCap(StrokeLineCap.ROUND);
                } else {
                    circle.setStrokeLineCap(StrokeLineCap.SQUARE);
                    arc.setStrokeLineCap(StrokeLineCap.SQUARE);
                }
            }

            @Override
            public Object getBean() {
                return CircularProgressIndicator.this;
            }

            @Override
            public String getName() {
                return "roundLineCap";
            }
        };
        isRunning = false;
        timeline = new Timeline();
        listener = observable -> {
            circle.setStrokeDashOffset(dashOffset.get());
            circle.getStrokeDashArray().setAll(dashArray_0.getValue(), 200d);
        };
        init();
        initGraphics();
        registerListeners();
    }


    // ******************** Initialization ************************************
    private void init() {
        if (Double.compare(getPrefWidth(), 0.0) <= 0 || Double.compare(getPrefHeight(), 0.0) <= 0 ||
                Double.compare(getWidth(), 0.0) <= 0 || Double.compare(getHeight(), 0.0) <= 0) {
            if (getPrefWidth() > 0 && getPrefHeight() > 0) {
                setPrefSize(getPrefWidth(), getPrefHeight());
            } else {
                setPrefSize(PREFERRED_WIDTH, PREFERRED_HEIGHT);
            }
        }

        if (Double.compare(getMinWidth(), 0.0) <= 0 || Double.compare(getMinHeight(), 0.0) <= 0) {
            setMinSize(MINIMUM_WIDTH, MINIMUM_HEIGHT);
        }

        if (Double.compare(getMaxWidth(), 0.0) <= 0 || Double.compare(getMaxHeight(), 0.0) <= 0) {
            setMaxSize(MAXIMUM_WIDTH, MAXIMUM_HEIGHT);
        }
    }

    private void initGraphics() {
        double center = PREFERRED_WIDTH * 0.5;
        double radius = PREFERRED_WIDTH * 0.45;
        circle = new Circle();
        circle.setCenterX(center);
        circle.setCenterY(center);
        circle.setRadius(radius);
        circle.getStyleClass().add("indicator");
        circle.setStrokeLineCap(isRoundLineCap() ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
        circle.setStrokeWidth(PREFERRED_WIDTH * 0.10526316);
        circle.setStrokeDashOffset(dashOffset.get());
        circle.getStrokeDashArray().setAll(dashArray_0.getValue(), 200d);

        arc = new Arc(center, center, radius, radius, 90, -360.0 * getProgress());
        arc.setStrokeLineCap(isRoundLineCap() ? StrokeLineCap.ROUND : StrokeLineCap.SQUARE);
        arc.setStrokeWidth(PREFERRED_WIDTH * 0.1);
        arc.getStyleClass().add("indicator");

        indeterminatePane = new StackPane(circle);
        indeterminatePane.setVisible(false);

        progressPane = new Pane(arc);
        progressPane.setVisible(Double.compare(getProgress(), 0.0) != 0);

        getChildren().setAll(progressPane, indeterminatePane);

        // Setup timeline animation
        KeyValue kvDashOffset_0 = new KeyValue(dashOffset, 0, Interpolator.EASE_BOTH);
        KeyValue kvDashOffset_50 = new KeyValue(dashOffset, -32, Interpolator.EASE_BOTH);
        KeyValue kvDashOffset_100 = new KeyValue(dashOffset, -64, Interpolator.EASE_BOTH);

        KeyValue kvDashArray_0_0 = new KeyValue(dashArray_0, 5, Interpolator.EASE_BOTH);
        KeyValue kvDashArray_0_50 = new KeyValue(dashArray_0, 89, Interpolator.EASE_BOTH);
        KeyValue kvDashArray_0_100 = new KeyValue(dashArray_0, 89, Interpolator.EASE_BOTH);

        KeyValue kvRotate_0 = new KeyValue(circle.rotateProperty(), -10, Interpolator.LINEAR);
        KeyValue kvRotate_100 = new KeyValue(circle.rotateProperty(), 370, Interpolator.LINEAR);

        KeyFrame kf0 = new KeyFrame(Duration.ZERO, kvDashOffset_0, kvDashArray_0_0, kvRotate_0);
        KeyFrame kf1 = new KeyFrame(Duration.millis(1000), kvDashOffset_50, kvDashArray_0_50);
        KeyFrame kf2 = new KeyFrame(Duration.millis(1500), kvDashOffset_100, kvDashArray_0_100, kvRotate_100);

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().setAll(kf0, kf1, kf2);

        // Setup additional pane rotation
        indeterminatePaneRotation = new RotateTransition();
        indeterminatePaneRotation.setNode(indeterminatePane);
        indeterminatePaneRotation.setFromAngle(0);
        indeterminatePaneRotation.setToAngle(-360);
        indeterminatePaneRotation.setInterpolator(Interpolator.LINEAR);
        indeterminatePaneRotation.setCycleCount(Animation.INDEFINITE);
        indeterminatePaneRotation.setDuration(new Duration(4500));
    }

    private void registerListeners() {
        widthProperty().addListener(o -> resize());
        heightProperty().addListener(o -> resize());
        progress.addListener(o -> redraw());
        dashOffset.addListener(listener);
    }


    // ******************** Methods *******************************************
    public double getProgress() {
        return progress.get();
    }

    public void setProgress(double PROGRESS) {
        progress.set(PROGRESS);
    }

    public DoubleProperty progressProperty() {
        return progress;
    }

    private void startIndeterminate() {
        if (isRunning) return;
        manageNode(indeterminatePane, true);
        manageNode(progressPane, false);
        timeline.play();
        indeterminatePaneRotation.play();
        isRunning = true;
        indeterminate.set(true);
    }

    private void stopIndeterminate() {
        if (!isRunning) return;
        timeline.stop();
        indeterminatePaneRotation.stop();
        indeterminatePane.setRotate(0);
        manageNode(progressPane, true);
        manageNode(indeterminatePane, false);
        isRunning = false;
        indeterminate.set(false);
    }

    public boolean isIndeterminate() {
        return Double.compare(ProgressIndicator.INDETERMINATE_PROGRESS, getProgress()) == 0;
    }

    public ReadOnlyBooleanProperty indeterminateProperty() {
        return indeterminate;
    }

    public boolean isRoundLineCap() {
        return roundLineCap.get();
    }

    public void setRoundLineCap(boolean BOOLEAN) {
        roundLineCap.set(BOOLEAN);
    }

    public BooleanProperty roundLineCapProperty() {
        return roundLineCap;
    }

    private void manageNode(Node NODE, boolean MANAGED) {
        if (MANAGED) {
            NODE.setManaged(true);
            NODE.setVisible(true);
        } else {
            NODE.setVisible(false);
            NODE.setManaged(false);
        }
    }


    // ******************** Resizing ******************************************
    private void resize() {
        double width = getWidth() - getInsets().getLeft() - getInsets().getRight();
        double height = getHeight() - getInsets().getTop() - getInsets().getBottom();
        double size = Math.min(width, height);

        if (width > 0 && height > 0) {
            indeterminatePane.setMaxSize(size, size);
            indeterminatePane.setPrefSize(size, size);
            indeterminatePane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            progressPane.setMaxSize(size, size);
            progressPane.setPrefSize(size, size);
            progressPane.relocate((getWidth() - size) * 0.5, (getHeight() - size) * 0.5);

            double center = size * 0.5;
            double radius = size * 0.45;

            arc.setCenterX(center);
            arc.setCenterY(center);
            arc.setRadiusX(radius);
            arc.setRadiusY(radius);
            arc.setStrokeWidth(size * 0.10526316);

            double factor = size / 24;
            circle.setScaleX(factor);
            circle.setScaleY(factor);
        }
    }

    private void redraw() {
        double progress = getProgress();
        progressPane.setVisible(Double.compare(progress, 0) > 0);
        arc.setLength(-360.0 * progress);
    }
}