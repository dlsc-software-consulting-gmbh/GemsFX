package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.BeforeAfterView;
import javafx.beans.InvalidationListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;

public class BeforeAfterViewSkin extends SkinBase<BeforeAfterView> {

    private StackPane content = new StackPane();
    private StackPane divider = new StackPane();
    private StackPane handle = new StackPane();
    private double startX;
    private double startY;

    public BeforeAfterViewSkin(BeforeAfterView view) {
        super(view);

        content.getStyleClass().add("content");

        divider.getStyleClass().add("divider");
        divider.setManaged(false);
        divider.setMouseTransparent(true);

        handle.getStyleClass().add("handle");
        handle.setManaged(false);
        handle.getChildren().add(new FontIcon(MaterialDesign.MDI_DRAG));
        handle.setMouseTransparent(true);

        view.setOnMousePressed(evt -> {
            startX = evt.getX();
            startY = evt.getY();
        });

        view.setOnMouseDragged(evt -> {
            if (view.getOrientation().equals(Orientation.HORIZONTAL)) {
                double x = evt.getX();
                double delta = x - startX;
                view.setDividerPosition(Math.min(1, Math.max(0, view.getDividerPosition() + (delta / view.getWidth()))));
                startX = x;
            } else {
                double y = evt.getY();
                double delta = y - startY;
                view.setDividerPosition(Math.min(1, Math.max(0, view.getDividerPosition() + (delta / view.getHeight()))));
                startY = y;
            }
        });

        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(view.widthProperty());
        clip.heightProperty().bind(view.heightProperty());
        content.setClip(clip);

        InvalidationListener updateListener = it -> updateView();
        view.beforeProperty().addListener(updateListener);
        view.afterProperty().addListener(updateListener);
        view.orientationProperty().addListener(updateListener);

        view.dividerPositionProperty().addListener(it -> view.requestLayout());

        getChildren().addAll(content, divider, handle);

        updateView();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        double dividerPosition = getSkinnable().getDividerPosition();
        double dividerPrefWidth = divider.prefWidth(-1);
        double dividerPrefHeight = divider.prefHeight(-1);
        double handlePrefWidth = handle.prefWidth(-1);
        double handlePrefHeight = handle.prefHeight(-1);
        double my = contentY + contentHeight / 2;
        double x = contentX + (contentWidth * dividerPosition);
        double y = contentY + (contentHeight * dividerPosition);

        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            divider.resizeRelocate(x - dividerPrefWidth / 2, contentY, dividerPrefWidth, contentHeight);
            handle.resizeRelocate(x - handlePrefWidth / 2, my - handlePrefHeight / 2, handlePrefWidth, handlePrefHeight);
        } else {
            divider.resizeRelocate(contentX, y - dividerPrefHeight / 2, contentWidth, dividerPrefHeight);
            handle.resizeRelocate(contentX + contentWidth / 2 - handlePrefWidth / 2, y - handlePrefHeight / 2, handlePrefWidth, handlePrefHeight);
        }
    }

    private void updateView() {
        content.getChildren().clear();
        BeforeAfterView view = getSkinnable();

        Node beforeNode = view.getBefore();
        if (beforeNode != null) {
            StackPane wrapper = new StackPane(beforeNode);
            content.getChildren().add(wrapper);

            Rectangle clip = new Rectangle();
            if (view.getOrientation().equals(Orientation.HORIZONTAL)) {
                clip.widthProperty().bind(wrapper.widthProperty().multiply(view.dividerPositionProperty()));
                clip.heightProperty().bind(wrapper.heightProperty());
            } else {
                clip.heightProperty().bind(wrapper.heightProperty().multiply(view.dividerPositionProperty()));
                clip.widthProperty().bind(wrapper.widthProperty());
            }
            wrapper.setClip(clip);
        }

        Node afterNode = view.getAfter();
        if (afterNode != null) {
            StackPane wrapper = new StackPane(afterNode);
            content.getChildren().add(wrapper);

            Rectangle clip = new Rectangle();
            if (view.getOrientation().equals(Orientation.HORIZONTAL)) {
                clip.xProperty().bind(wrapper.widthProperty().multiply(view.dividerPositionProperty()));
                clip.widthProperty().bind(wrapper.widthProperty().subtract(wrapper.widthProperty().multiply(view.dividerPositionProperty())));
                clip.heightProperty().bind(wrapper.heightProperty());
            } else {
                clip.yProperty().bind(wrapper.heightProperty().multiply(view.dividerPositionProperty()));
                clip.heightProperty().bind(wrapper.heightProperty().subtract(wrapper.heightProperty().multiply(view.dividerPositionProperty())));
                clip.widthProperty().bind(wrapper.widthProperty());
            }

            wrapper.setClip(clip);
        }
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        double w = 0;
        Node beforeNode = getSkinnable().getBefore();
        if (beforeNode != null) {
            w = Math.max(w, beforeNode.prefWidth(-1));
        }

        Node afterNode = getSkinnable().getAfter();
        if (afterNode != null) {
            w = Math.max(w, afterNode.prefWidth(-1));
        }

        return w + rightInset + leftInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }
}
