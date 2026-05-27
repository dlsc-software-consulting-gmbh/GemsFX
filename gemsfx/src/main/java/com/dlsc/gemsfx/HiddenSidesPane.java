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

import java.util.Objects;

import com.dlsc.gemsfx.skins.HiddenSidesPaneSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
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
public class HiddenSidesPane extends Control {

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

    /**
     * Creates the default skin for this control.
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new HiddenSidesPaneSkin(this);
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

    /**
     * Shows a specific side
     *
     * @param side the side to show
     */
    public void show(Side side) {
        Objects.requireNonNull(side, "side cannot be null");
        getProperties().put("showPane", side);
    }

    /**
     * Hides the currently showing side
     */
    public void hide() {
        getProperties().put("showPane", null);
    }
}
