/**
 * Copyright (c) 2014, 2015, 2018 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions, and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions, and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.dlsc.gemsfx;

import java.util.List;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

/**
 * A pane used to display a full-size content node and four initially hidden
 * nodes on the four sides. The hidden nodes can be made visible by moving the
 * mouse cursor to the edges (see {@link #setTriggerDistance(double)}) of the
 * pane. The hidden node will appear (at its preferred width or height) with a
 * short slide-in animation. The node will disappear again as soon as the mouse
 * cursor exits it. A hidden node / side can also be pinned by calling
 * {@link #setPinnedSide(Side)}. It will remain visible as long as it stays
 * pinned.
 *
 * <h2>Screenshot</h2> The following screenshots shows the right side node
 * hovering over a table after it was made visible:
 *
 * <img src="hiddenSidesPane.png" alt="Screenshot of HiddenSidesPane">
 *
 * <h2>Code Sample</h2>
 *
 * <pre>
 * HiddenSidesPane pane = new HiddenSidesPane();
 * pane.setContent(new TableView());
 * pane.setRight(new ListView());
 * </pre>
 */
public class HiddenSidesPane extends Pane {

    private boolean biasDirty = true;
    private Orientation bias;

    private final EventHandler<MouseEvent> exitedHandler;
    private boolean mousePressed;

    /**
     * Constructs a new pane with the given content node and the four side
     * nodes. Each one of the side nodes may be null.
     *
     * @param content the primary node that will fill the entire width and height of
     *                the pane
     * @param top     the hidden node on the top side
     * @param right   the hidden node on the right side
     * @param bottom  the hidden node on the bottom side
     * @param left    the hidden node on the left side
     */
    public HiddenSidesPane(Node content, Node top, Node right, Node bottom, Node left) {
        exitedHandler = event -> {
            if (isMouseEnabled() && getPinnedSide() == null && !mousePressed) {
                hide();
            }
        };

        updateStackPane();

        InvalidationListener rebuildListener = observable -> updateStackPane();
        contentProperty().addListener(rebuildListener);
        topProperty().addListener(rebuildListener);
        rightProperty().addListener(rebuildListener);
        bottomProperty().addListener(rebuildListener);
        leftProperty().addListener(rebuildListener);

        addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            if (isMouseEnabled() && getPinnedSide() == null) {
                Side side = getSide(event);
                if (side != null) {
                    show(side);
                } else if (isMouseMovedOutsideSides(event)) {
                    hide();
                }
            }
        });

        addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);

        addEventFilter(MouseEvent.MOUSE_PRESSED, event -> mousePressed = true);

        addEventFilter(MouseEvent.MOUSE_RELEASED, event -> {
            mousePressed = false;

            if (isMouseEnabled() && getPinnedSide() == null) {
                Side side = getSide(event);
                if (side != null) {
                    show(side);
                } else {
                    hide();
                }
            }
        });

        for (Side side : Side.values()) {
            visibility[side.ordinal()] = new SimpleDoubleProperty(0);
            visibility[side.ordinal()].addListener(observable -> requestLayout());
        }

        Side pinnedSide = getPinnedSide();
        if (pinnedSide != null) {
            show(pinnedSide);
        }

        pinnedSideProperty().addListener(observable -> show(getPinnedSide()));
        
        Rectangle clip = new Rectangle();
        clip.setX(0);
        clip.setY(0);
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());

        setClip(clip);
        
        setContent(content);
        setTop(top);
        setRight(right);
        setBottom(bottom);
        setLeft(left);
    }

    /**
     * Constructs a new pane with no content and no side nodes.
     */
    public HiddenSidesPane() {
        this(null, null, null, null, null);
    }

    @Override
    public Orientation getContentBias() {
        if (biasDirty) {
            bias = null;
            final List<Node> children = getManagedChildren();
            for (Node child : children) {
                Orientation contentBias = child.getContentBias();
                if (contentBias != null) {
                    bias = contentBias;
                    if (contentBias == Orientation.HORIZONTAL) {
                        break;
                    }
                }
            }
            biasDirty = false;
        }
        return bias;
    }

    private final DoubleProperty triggerDistance = new SimpleDoubleProperty(this, "triggerDistance", 16); 

    /**
     * The property that stores the distance to the pane's edges that will
     * trigger the appearance of the hidden side nodes.<br>
     * Setting the property to zero or a negative value will disable this
     * functionality, so a hidden side can only be made visible with
     * {@link #setPinnedSide(Side)}.
     *
     * @return the trigger distance property
     */
    public final DoubleProperty triggerDistanceProperty() {
        return triggerDistance;
    }

    public final double getTriggerDistance() {
        return triggerDistance.get();
    }

    public final void setTriggerDistance(double distance) {
        triggerDistance.set(distance);
    }

    // Content node support.

    private final ObjectProperty<Node> content = new SimpleObjectProperty<>(this, "content"); 

    /**
     * The property that is used to store a reference to the content node. The
     * content node will fill the entire width and height of the pane.
     *
     * @return the content node property
     */
    public final ObjectProperty<Node> contentProperty() {
        return content;
    }

    public final Node getContent() {
        return contentProperty().get();
    }

    public final void setContent(Node content) {
        contentProperty().set(content);
    }

    // Top node support.

    private final ObjectProperty<Node> top = new SimpleObjectProperty<>(this, "top"); 

    /**
     * The property used to store a reference to the node shown at the top side
     * of the pane.
     *
     * @return the hidden node at the top side of the pane
     */
    public final ObjectProperty<Node> topProperty() {
        return top;
    }

    public final Node getTop() {
        return topProperty().get();
    }

    public final void setTop(Node top) {
        topProperty().set(top);
    }

    // Right node support.

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

    /**
     * The node displayed on the right side of the pane. It slides in when the
     * cursor moves to the right edge.
     *
     * @return the right node property
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    public final Node getRight() {
        return rightProperty().get();
    }

    public final void setRight(Node right) {
        rightProperty().set(right);
    }

    // Bottom node support.

    private final ObjectProperty<Node> bottom = new SimpleObjectProperty<>(this, "bottom");

    /**
     * The node displayed at the bottom of the pane. It slides in when the
     * cursor moves to the bottom edge.
     *
     * @return the bottom node property
     */
    public final ObjectProperty<Node> bottomProperty() {
        return bottom;
    }

    public final Node getBottom() {
        return bottomProperty().get();
    }

    public final void setBottom(Node bottom) {
        bottomProperty().set(bottom);
    }

    // Left node support.

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    /**
     * The node displayed on the left side of the pane. It slides in when the
     * cursor moves to the left edge.
     *
     * @return the left node property
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final Node getLeft() {
        return leftProperty().get();
    }

    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    // Pinned side support.

    private final ObjectProperty<Side> pinnedSide = new SimpleObjectProperty<>(this, "pinnedSide"); 

    /**
     * Returns the pinned side property. The value of this property determines
     * if one of the four hidden sides stays visible all the time.
     *
     * @return the pinned side property
     */
    public final ObjectProperty<Side> pinnedSideProperty() {
        return pinnedSide;
    }

    public final Side getPinnedSide() {
        return pinnedSideProperty().get();
    }

    public final void setPinnedSide(Side side) {
        pinnedSideProperty().set(side);
    }

    // slide in animation delay

    private final ObjectProperty<Duration> animationDelay = new SimpleObjectProperty<>(this, "animationDelay", Duration.millis(300)); 

    /**
     * Returns the animation delay property. The value of this property
     * determines the delay before the hidden side slide in / slide out
     * animation starts to play.
     *
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDelayProperty() {
        return animationDelay;
    }

    public final Duration getAnimationDelay() {
        return animationDelay.get();
    }

    public final void setAnimationDelay(Duration duration) {
        animationDelay.set(duration);
    }

    // slide in / slide out duration

    private final ObjectProperty<Duration> animationDuration = new SimpleObjectProperty<>(this, "animationDuration", Duration.millis(200)); 

    /**
     * Returns the animation duration property. The value of this property
     * determines the fade in time for a hidden side to become visible.
     *
     * @return animation delay property
     */
    public final ObjectProperty<Duration> animationDurationProperty() {
        return animationDuration;
    }

    public final Duration getAnimationDuration() {
        return animationDuration.get();
    }

    public final void setAnimationDuration(Duration duration) {
        animationDuration.set(duration);
    }

    @Override
    protected void layoutChildren() {
        double contentX = 0;
        double contentY = 0;
        double contentWidth = getWidth();
        double contentHeight = getHeight();

        getContent().resizeRelocate(contentX, contentY, contentWidth, contentHeight);

        // layout the unmanaged side nodes

        Node bottom = getBottom();
        if (bottom != null) {
            double prefHeight = bottom.prefHeight(contentWidth);
            double offset = prefHeight * visibility[Side.BOTTOM.ordinal()].get();
            bottom.resizeRelocate(contentX, contentY + contentHeight - offset, contentWidth, prefHeight);
            bottom.setVisible(visibility[Side.BOTTOM.ordinal()].get() > 0);
        }

        Node left = getLeft();
        if (left != null) {
            double prefWidth = left.prefWidth(contentHeight);
            double offset = prefWidth * visibility[Side.LEFT.ordinal()].get();
            left.resizeRelocate(contentX - (prefWidth - offset), contentY, prefWidth, contentHeight);
            left.setVisible(visibility[Side.LEFT.ordinal()].get() > 0);
        }

        Node right = getRight();
        if (right != null) {
            double prefWidth = right.prefWidth(contentHeight);
            double offset = prefWidth * visibility[Side.RIGHT.ordinal()].get();

            System.out.println("prefWidth: " + prefWidth + ", offset: " + offset + ", contentX: " + contentX + ", contentWidth: " + contentWidth + ", contentheight: " + contentHeight);
            right.resizeRelocate(contentX + contentWidth - offset, contentY, prefWidth, contentHeight);
            right.setVisible(visibility[Side.RIGHT.ordinal()].get() > 0);
        }

        Node top = getTop();
        if (top != null) {
            double prefHeight = top.prefHeight(contentWidth);
            double offset = prefHeight * visibility[Side.TOP.ordinal()].get();
            top.resizeRelocate(contentX, contentY - (prefHeight - offset), contentWidth, prefHeight);
            top.setVisible(visibility[Side.TOP.ordinal()].get() > 0);
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return getContent() != null ? getContent().prefWidth(height) : 0;
    }

    @Override
    protected double computePrefHeight(double width) {
        return getContent() != null ? getContent().prefHeight(width) : 0;
    }

    @Override
    protected double computeMinWidth(double height) {
        return getContent() != null ? getContent().minWidth(height) : 0;
    }

    @Override
    protected double computeMinHeight(double width) {
        return getContent() != null ? getContent().minHeight(width) : 0;
    }

    @Override
    protected double computeMaxWidth(double height) {
        return getContent() != null ? getContent().maxWidth(height) : 0;
    }

    @Override
    protected double computeMaxHeight(double width) {
        return getContent() != null ? getContent().maxHeight(width) : 0;
    }

    private boolean isMouseMovedOutsideSides(MouseEvent event) {
        if (getLeft() != null && getLeft().getBoundsInParent().contains(event.getX(), event.getY())) {
            return false;
        }

        if (getTop() != null && getTop().getBoundsInParent().contains(event.getX(), event.getY())) {
            return false;
        }

        if (getRight() != null && getRight().getBoundsInParent().contains(event.getX(), event.getY())) {
            return false;
        }

        if (getBottom() != null && getBottom().getBoundsInParent().contains(event.getX(), event.getY())) {
            return false;
        }

        return true;
    }

    private boolean isMouseEnabled() {
        return getTriggerDistance() > 0;
    }

    private Side getSide(MouseEvent evt) {
        if (getBoundsInLocal().contains(evt.getX(), evt.getY())) {
            double trigger = getTriggerDistance();
            if (evt.getX() <= trigger) {
                return Side.LEFT;
            } else if (evt.getX() > getWidth() - trigger) {
                return Side.RIGHT;
            } else if (evt.getY() <= trigger) {
                return Side.TOP;
            } else if (evt.getY() > getHeight() - trigger) {
                return Side.BOTTOM;
            }
        }

        return null;
    }

    private final DoubleProperty[] visibility = new SimpleDoubleProperty[Side.values().length];

    private Timeline showTimeline;

    private void show(Side side) {
        if (hideTimeline != null) {
            hideTimeline.stop();
        }

        if (showTimeline != null && showTimeline.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        KeyValue[] keyValues = new KeyValue[Side.values().length];
        for (Side s : Side.values()) {
            keyValues[s.ordinal()] = new KeyValue(visibility[s.ordinal()], s.equals(side) ? 1 : 0);
        }

        Duration delay = getAnimationDelay() != null ? getAnimationDelay() : Duration.millis(300);
        Duration duration = getAnimationDuration() != null ? getAnimationDuration() : Duration.millis(200);

        KeyFrame keyFrame = new KeyFrame(duration, keyValues);
        showTimeline = new Timeline(keyFrame);
        showTimeline.setDelay(delay);
        showTimeline.play();
    }

    private Timeline hideTimeline;

    private void hide() {
        if (showTimeline != null) {
            showTimeline.stop();
        }

        if (hideTimeline != null && hideTimeline.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        boolean sideVisible = false;
        for (Side side : Side.values()) {
            if (visibility[side.ordinal()].get() > 0) {
                sideVisible = true;
                break;
            }
        }

        // nothing to do here
        if (!sideVisible) {
            return;
        }

        KeyValue[] keyValues = new KeyValue[Side.values().length];
        for (Side side : Side.values()) {
            keyValues[side.ordinal()] = new KeyValue(visibility[side.ordinal()], 0);
        }

        Duration delay = getAnimationDelay() != null ? getAnimationDelay() : Duration.millis(300);
        Duration duration = getAnimationDuration() != null ? getAnimationDuration() : Duration.millis(200);

        KeyFrame keyFrame = new KeyFrame(duration, keyValues);
        hideTimeline = new Timeline(keyFrame);
        hideTimeline.setDelay(delay);
        hideTimeline.play();
    }

    private void updateStackPane() {
        getChildren().clear();

        if (getContent() != null) {
            getChildren().add(getContent());
        }

        if (getTop() != null) {
            getChildren().add(getTop());
            ((Region) getTop()).setMaxHeight(Region.USE_PREF_SIZE);
            ((Region) getTop()).setMaxWidth(Double.MAX_VALUE);
            getTop().removeEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
            getTop().addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
        }
        if (getRight() != null) {
            getChildren().add(getRight());
            ((Region) getRight()).setMaxWidth(Region.USE_PREF_SIZE);
            ((Region) getRight()).setMaxHeight(Double.MAX_VALUE);
            getRight().removeEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
            getRight().addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
        }
        if (getBottom() != null) {
            getChildren().add(getBottom());
            ((Region) getBottom()).setMaxHeight(Region.USE_PREF_SIZE);
            ((Region) getBottom()).setMaxWidth(Double.MAX_VALUE);
            getBottom().removeEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
            getBottom().addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
        }
        if (getLeft() != null) {
            getChildren().add(getLeft());
            ((Region) getLeft()).setMaxWidth(Region.USE_PREF_SIZE);
            ((Region) getLeft()).setMaxHeight(Double.MAX_VALUE);
            getLeft().removeEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
            getLeft().addEventFilter(MouseEvent.MOUSE_EXITED, exitedHandler);
        }
    }
}
