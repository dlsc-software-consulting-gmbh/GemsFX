package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.QuadCurve;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a quadratic Bézier curve between a parent and a
 * child node, with a directional arrow at the child end.
 *
 * <p>A single control point is placed at the midpoint between parent and child along
 * the perpendicular axis, producing a smooth, single-bowed curve. A directional
 * arrow is oriented along the tangent at the child end of the curve.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class QuadCurveLink<T> extends AbstractLinkStrategy<T> {

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
        QuadCurve quad = new QuadCurve();
        quad.getStyleClass().add("link-curve");
        Node arrow = createSimpleArrow();

        switch (direction) {
            case TOP_TO_BOTTOM:
                quad.setStartX(startX);
                quad.setStartY(startY);
                quad.setControlX((startX + endX) / 2);
                quad.setControlY(startY + vgap / 2);
                quad.setEndX(endX);
                quad.setEndY(endY);
                arrow.setRotate(getTangentAngle(quad, 0.95));
                break;

            case BOTTOM_TO_TOP:
                quad.setStartX(startX);
                quad.setStartY(startY);
                quad.setControlX((startX + endX) / 2);
                quad.setControlY(startY - vgap / 2);
                quad.setEndX(endX);
                quad.setEndY(endY);
                arrow.setRotate(getTangentAngle(quad, 0.05));
                break;

            case LEFT_TO_RIGHT:
                quad.setStartX(startX);
                quad.setStartY(startY);
                quad.setControlX(startX + hgap);
                quad.setControlY((startY + endY) / 2);
                quad.setEndX(endX);
                quad.setEndY(endY);
                arrow.setRotate(getTangentAngle(quad, 0.95));
                break;

            case RIGHT_TO_LEFT:
                quad.setStartX(startX);
                quad.setStartY(startY);
                quad.setControlX(startX - hgap);
                quad.setControlY((startY + endY) / 2);
                quad.setEndX(endX);
                quad.setEndY(endY);

                if (startY == endY) {
                    arrow.setRotate(180);
                } else {
                    //Point2D p1 = getPointOnQuadCurve(quad, 0.05);
                    //Point2D p2 = getPointOnQuadCurve(quad, 0.04999);
                    //double angle = Math.toDegrees(Math.atan2(p2.getY() - p1.getY(), p2.getX() - p1.getX()));
                    //arrow.setRotate(angle);
                    double rtlAngle = getTangentAngle(quad, 0.05);
                    arrow.setRotate(rtlAngle > 0 ? rtlAngle - 180 : rtlAngle + 180);

                }
                break;
        }

        return new ArrayList<>(List.of(quad, arrow));
    }

    private Point2D getPointOnQuadCurve(QuadCurve curve, double t) {
        double x = (1 - t) * (1 - t) * curve.getStartX() + 2 * (1 - t) * t * curve.getControlX() + t * t * curve.getEndX();
        double y = (1 - t) * (1 - t) * curve.getStartY() + 2 * (1 - t) * t * curve.getControlY() + t * t * curve.getEndY();
        return new Point2D(x, y);
    }

}