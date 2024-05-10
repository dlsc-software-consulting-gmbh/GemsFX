package com.dlsc.gemsfx;

import javafx.css.PseudoClass;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.PopupControl;
import javafx.stage.Screen;
import javafx.stage.Window;

/**
 * A custom popup control that extends PopupControl.
 * <p>
 * The popup can be displayed above or below the anchor node depending on the available space.
 */
public class CustomPopupControl extends PopupControl {

    private static final PseudoClass ABOVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("above");
    private static final PseudoClass BELOW_PSEUDO_CLASS = PseudoClass.getPseudoClass("below");

    public void show(Node node) {
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            Window parent = node.getScene().getWindow();
            getScene().setNodeOrientation(node.getEffectiveNodeOrientation());
            if (node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                setAnchorLocation(AnchorLocation.CONTENT_TOP_RIGHT);
            } else {
                setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
            }

            double nodeTopY = parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY();

            double anchorX = parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX();
            double anchorY = nodeTopY + node.getBoundsInParent().getHeight();

            double bridgeHeight = bridge.getHeight();
            double popupHeight = bridgeHeight == 0 ? getSkin().getNode().prefHeight(-1) : bridgeHeight;
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            boolean isShowAbove = anchorY + popupHeight > screenHeight;
            if (isShowAbove) {
                anchorY = nodeTopY - popupHeight;
            }
            this.pseudoClassStateChanged(ABOVE_PSEUDO_CLASS, isShowAbove);
            this.pseudoClassStateChanged(BELOW_PSEUDO_CLASS, !isShowAbove);

            show(node, anchorX, anchorY);
        } else {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
        }
    }

}
