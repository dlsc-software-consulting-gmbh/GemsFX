package com.dlsc.gemsfx;

import com.dlsc.gemsfx.infocenter.InfoCenterPane;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.HiddenSidesPane;

public class PowerPane extends StackPane {

    private final InfoCenterPane infoCenterPane = new InfoCenterPane();
    private final DialogPane dialogPane = new DialogPane();
    private final DrawerStackPane drawerStackPane = new DrawerStackPane();
    private final HiddenSidesPane hiddenSidesPane = new HiddenSidesPane();

    public PowerPane() {
        getStyleClass().add("power-pane");

        BorderPane borderPane = new BorderPane();
        borderPane.topProperty().bind(topProperty());
        borderPane.bottomProperty().bind(bottomProperty());
        borderPane.leftProperty().bind(leftProperty());
        borderPane.rightProperty().bind(rightProperty());
        borderPane.centerProperty().bind(centerProperty());

        InfoCenterPane infoCenterPane = getInfoCenterPane();
        infoCenterPane.setContent(new StackPane(borderPane, getHiddenSidesPane(), getDrawerStackPane(), getDialogPane()));

        getChildren().add(infoCenterPane);
    }

    public InfoCenterPane getInfoCenterPane() {
        return infoCenterPane;
    }

    public DialogPane getDialogPane() {
        return dialogPane;
    }

    public DrawerStackPane getDrawerStackPane() {
        return drawerStackPane;
    }

    public HiddenSidesPane getHiddenSidesPane() {
        return hiddenSidesPane;
    }

    /**
     * The node placed in the center of this pane.
     * If resizable, it will be resized fill the center of the border pane
     * between the top, bottom, left, and right nodes.   If the node cannot be
     * resized to fill the center space (it's not resizable or its max size prevents
     * it) then it will be center aligned unless the child's alignment constraint
     * has been set.
     *
     * @return the node placed in the center of this pane
     */
    public final ObjectProperty<Node> centerProperty() {
        if (center == null) {
            center = new SimpleObjectProperty<>(this, "center");
        }
        return center;
    }

    private ObjectProperty<Node> center;

    public final void setCenter(Node value) {
        centerProperty().set(value);
    }

    public final Node getCenter() {
        return center == null ? null : center.get();
    }

    /**
     * The node placed on the top edge of this pane.
     * If resizable, it will be resized to its preferred height and it's width
     * will span the width of the border pane.  If the node cannot be
     * resized to fill the top space (it's not resizable or its max size prevents
     * it) then it will be aligned top-left within the space unless the child's
     * alignment constraint has been set.
     *
     * @return the node placed on the top edge of this pane
     */
    public final ObjectProperty<Node> topProperty() {
        if (top == null) {
            top = new SimpleObjectProperty<>(this, "top");
        }
        return top;
    }

    private ObjectProperty<Node> top;

    public final void setTop(Node value) {
        topProperty().set(value);
    }

    public final Node getTop() {
        return top == null ? null : top.get();
    }

    /**
     * The node placed on the bottom edge of this pane.
     * If resizable, it will be resized to its preferred height and it's width
     * will span the width of the border pane.  If the node cannot be
     * resized to fill the bottom space (it's not resizable or its max size prevents
     * it) then it will be aligned bottom-left within the space unless the child's
     * alignment constraint has been set.
     *
     * @return the node placed on the bottom edge of this pane
     */
    public final ObjectProperty<Node> bottomProperty() {
        if (bottom == null) {
            bottom = new SimpleObjectProperty<>(this, "bottom");
        }
        return bottom;
    }

    private ObjectProperty<Node> bottom;

    public final void setBottom(Node value) {
        bottomProperty().set(value);
    }

    public final Node getBottom() {
        return bottom == null ? null : bottom.get();
    }

    /**
     * The node placed on the left edge of this pane.
     * If resizable, it will be resized to its preferred width and it's height
     * will span the height of the border pane between the top and bottom nodes.
     * If the node cannot be resized to fill the left space (it's not resizable
     * or its max size prevents it) then it will be aligned top-left within the space
     * unless the child's alignment constraint has been set.
     *
     * @return the node placed on the left edge of this pane
     */
    public final ObjectProperty<Node> leftProperty() {
        if (left == null) {
            left = new SimpleObjectProperty<>(this, "left");
        }
        return left;
    }

    private ObjectProperty<Node> left;

    public final void setLeft(Node value) {
        leftProperty().set(value);
    }

    public final Node getLeft() {
        return left == null ? null : left.get();
    }

    /**
     * The node placed on the right edge of this pane.
     * If resizable, it will be resized to its preferred width and it's height
     * will span the height of the border pane between the top and bottom nodes.
     * If the node cannot be resized to fill the right space (it's not resizable
     * or its max size prevents it) then it will be aligned top-right within the space
     * unless the child's alignment constraint has been set.
     *
     * @return the node placed on the right edge of this pane
     */
    public final ObjectProperty<Node> rightProperty() {
        if (right == null) {
            right = new SimpleObjectProperty<>(this, "right");
        }
        return right;
    }

    private ObjectProperty<Node> right;

    public final void setRight(Node value) {
        rightProperty().set(value);
    }

    public final Node getRight() {
        return right == null ? null : right.get();
    }
}
