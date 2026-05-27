package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurve;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a smooth cubic Bézier curve between a parent
 * and a child node, with a directional arrow at the child end.
 *
 * <p>The control points of the curve are placed at half the gap distance along the
 * primary axis, producing a characteristic S-shaped connection that clearly indicates
 * the flow direction.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class CurvedLineLink<T> extends AbstractLinkStrategy<T> {

    /**
     * Draws the link for the given start and end coordinates.
     *
     * @param direction the layout direction
     * @param maxDimensionInLine the maximum node dimension in the active line
     * @param startX the x-coordinate of the link start
     * @param startY the y-coordinate of the link start
     * @param endX the x-coordinate of the link end
     * @param endY the y-coordinate of the link end
     * @param vgap the vertical gap between levels
     * @param hgap the horizontal gap between levels
     * @return the nodes used to render the link
     */
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
