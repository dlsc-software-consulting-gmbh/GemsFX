/**
 * Copyright (c) 2013, 2018 ControlsFX
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of ControlsFX, any associated website, nor the
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
package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.infocenter.InfoCenterPane;
import com.dlsc.gemsfx.infocenter.InfoCenterView;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.shape.Rectangle;

public class InfoCenterPaneSkin extends SkinBase<InfoCenterPane> {


    public InfoCenterPaneSkin(InfoCenterPane pane) {
        super(pane);

        updateChildren();

        InvalidationListener rebuildListener = observable -> updateChildren();
        pane.contentProperty().addListener(rebuildListener);

        visibility.addListener(it -> pane.requestLayout());

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(getSkinnable().widthProperty());
        clip.heightProperty().bind(getSkinnable().heightProperty());

        getSkinnable().setClip(clip);

        pane.showInfoCenterProperty().addListener(it -> {
            if (pane.isShowInfoCenter()) {
                show();
            } else {
                hide();
            }
        });
    }

    private DoubleProperty visibility = new SimpleDoubleProperty(this, "visibility");

    private Timeline timeline = new Timeline();

    private void show() {
        timeline.stop();
        KeyValue keyValue = new KeyValue(visibility, 1);
        KeyFrame keyFrame = new KeyFrame(getSkinnable().getSlideInDuration(), keyValue);
        timeline.getKeyFrames().setAll(keyFrame);
        timeline.play();
    }

    private void hide() {
        timeline.stop();
        KeyValue keyValues = new KeyValue(visibility, 0);
        KeyFrame keyFrame = new KeyFrame(getSkinnable().getSlideInDuration(), keyValues);
        timeline.getKeyFrames().setAll(keyFrame);
        timeline.play();
    }

    private void updateChildren() {
        getChildren().clear();

        InfoCenterPane infoCenterPane = getSkinnable();

        if (infoCenterPane.getContent() != null) {
            getChildren().add(infoCenterPane.getContent());
        }

        if (infoCenterPane.getInfoCenterView() != null) {
            getChildren().add(infoCenterPane.getInfoCenterView());
        }
    }

    @Override protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.minWidth(height) + leftInset + rightInset;
        } else {
            return 0;
        }
    }

    @Override protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.prefWidth(height) + leftInset + rightInset;
        } else {
            return 0;
        }
    }

    @Override protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.maxWidth(height) + leftInset + rightInset;
        } else {
            return 0;
        }
    }

    @Override protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.minHeight(width) + leftInset + rightInset;
        } else {
            return 0;
        }
    }

    @Override protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.prefHeight(width) + leftInset + rightInset;
        } else {
            return 0;
        }
    }

    @Override protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {

        Node content = getSkinnable().getContent();

        if (content != null) {
            return content.maxHeight(width) + leftInset + rightInset;
        } else {
            return 0;
        }
    }


    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        Node content = getSkinnable().getContent();
        if (content != null) {
            content.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
        }

        // special layout for the info center view based on the animation progress / visibility
        InfoCenterView view = getSkinnable().getInfoCenterView();
        if (view != null) {
            double prefWidth = view.prefWidth(-1);
            double prefHeight = view.prefHeight(prefWidth);
            double v = visibility.get();
            double offset = prefWidth * v;
            if (view.getShowAllGroup() != null) {
                view.resizeRelocate(contentX + contentWidth - offset, contentY, prefWidth, contentHeight);
            } else {
                view.resizeRelocate(contentX + contentWidth - offset, contentY, prefWidth, Math.min(contentHeight, prefHeight));
            }
            view.setVisible(v > 0);
        }
    }
}
