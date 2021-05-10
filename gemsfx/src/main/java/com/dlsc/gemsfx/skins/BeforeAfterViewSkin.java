package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.BeforeAfterView;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class BeforeAfterViewSkin extends SkinBase<BeforeAfterView> {

    private Region divider = new Region();

    public BeforeAfterViewSkin(BeforeAfterView view) {
        super(view);

        divider.getStyleClass().add("divider");
        divider.setManaged(false);

        InvalidationListener updateListener = it -> updateView();
        view.beforeProperty().addListener(updateListener);
        view.afterProperty().addListener(updateListener);

        view.dividerPositionProperty().addListener(it -> view.requestLayout());

        updateView();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        divider.resizeRelocate(contentWidth * getSkinnable().getDividerPosition() - divider.prefWidth(-1) / 2, contentY, divider.prefWidth(-1), contentHeight - contentY);
    }

    private void updateView() {
        getChildren().clear();
        BeforeAfterView view = getSkinnable();

        Node beforeNode = view.getBefore();
        if (beforeNode != null) {
            StackPane wrapper = new StackPane(beforeNode);
            getChildren().add(wrapper);

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(wrapper.widthProperty().multiply(view.dividerPositionProperty()));
            clip.heightProperty().bind(wrapper.heightProperty());
            wrapper.setClip(clip);
        }

        Node afterNode = view.getAfter();
        if (afterNode != null) {
            StackPane wrapper = new StackPane(afterNode);
            getChildren().add(wrapper);

            Rectangle clip = new Rectangle();
            clip.xProperty().bind(wrapper.widthProperty().multiply(view.dividerPositionProperty()));
            clip.widthProperty().bind(wrapper.widthProperty().subtract(wrapper.widthProperty().multiply(view.dividerPositionProperty())));
            clip.heightProperty().bind(wrapper.heightProperty());
            wrapper.setClip(clip);
        }

        getChildren().add(divider);
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

        return w;
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
