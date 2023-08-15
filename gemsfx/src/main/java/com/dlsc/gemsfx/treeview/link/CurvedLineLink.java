package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurve;

import java.util.ArrayList;
import java.util.List;

public class CurvedLineLink<T> extends AbstractLinkStrategy<T> {

    @Override
    protected ArrayList<Node> drawLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, double startX, double startY, double endX, double endY, double vgap, double hgap) {
        CubicCurve curve;
        Node arrow;
        double arrowRotation;

        switch (direction) {
            case TOP_TO_BOTTOM:
                curve = createCurve(startX, startY, startX, startY + vgap / 2, endX, startY + vgap / 2, endX, endY);
                arrowRotation = getTangentAngle(curve, 0.95);
                break;
            case BOTTOM_TO_TOP:
                curve = createCurve(startX, startY, startX, startY - vgap / 2, endX, endY + vgap / 2, endX, endY);
                arrowRotation = getTangentAngle(curve, 0.05);
                break;
            case LEFT_TO_RIGHT:
                curve = createCurve(startX, startY, startX + hgap / 2, startY, endX - hgap / 2, endY, endX, endY);
                arrowRotation = getTangentAngle(curve, 0.95);
                break;
            case RIGHT_TO_LEFT:
                curve = createCurve(startX, startY, startX - hgap / 2, startY, endX + hgap / 2, endY, endX, endY);
                arrowRotation = getTangentAngle(curve, 0.05);
                break;
            default:
                throw new IllegalArgumentException("Unsupported direction: " + direction);
        }

        arrow = createSimpleArrow();
        arrow.setRotate(arrowRotation);

        return new ArrayList<>(List.of(curve, arrow));
    }

    private CubicCurve createCurve(double startX, double startY, double controlX1, double controlY1, double controlX2, double controlY2, double endX, double endY) {
        CubicCurve curve = new CubicCurve(startX, startY, controlX1, controlY1, controlX2, controlY2, endX, endY);
        curve.getStyleClass().add("link-curve");
        return curve;
    }
}
