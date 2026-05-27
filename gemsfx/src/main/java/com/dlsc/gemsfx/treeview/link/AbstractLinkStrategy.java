package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNode;
import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.CubicCurve;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.QuadCurve;

import java.util.ArrayList;

/**
 * Abstract base class for {@link LinkStrategy} implementations.
 *
 * <p>This class calculates the connection start and end points from the parent and child
 * node positions and the active {@link com.dlsc.gemsfx.treeview.TreeNodeView.LayoutDirection},
 * storing them in the protected fields {@link #startX}, {@link #startY}, {@link #endX},
 * and {@link #endY}. Subclasses implement {@link #drawLink} to produce the actual
 * shape(s) using those pre-computed coordinates.
 *
 * <p>The class also provides helper methods for computing angles along Bézier curves
 * and straight lines, which are used by subclasses to correctly orient arrow decorations.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public abstract class AbstractLinkStrategy<T> implements LinkStrategy<T> {

    protected double startX;
    protected double startY;
    protected double endX;
    protected double endY;

    /**
     * Calculates the link endpoints for the given parent and child nodes.
     *
     * @param direction the layout direction
     * @param parentPoint the parent node location
     * @param parentW the parent node width
     * @param parentH the parent node height
     * @param childPoint the child node location
     * @param childW the child node width
     * @param childH the child node height
     * @param nodeLineGap the gap between a node and its link
     */
    protected void calculateEndPoints(TreeNodeView.LayoutDirection direction, Point2D parentPoint, double parentW, double parentH, Point2D childPoint, double childW, double childH, double nodeLineGap) {
        if (direction == TreeNodeView.LayoutDirection.BOTTOM_TO_TOP) {
            startX = parentPoint.getX() + parentW / 2;
            startY = parentPoint.getY() - nodeLineGap;
            endX = childPoint.getX() + childW / 2;
            endY = childPoint.getY() + childH + nodeLineGap;
        } else if (direction == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM) {
            startX = parentPoint.getX() + parentW / 2;
            startY = parentPoint.getY() + parentH + nodeLineGap;
            endX = childPoint.getX() + childW / 2;
            endY = childPoint.getY() - nodeLineGap;
        } else if (direction == TreeNodeView.LayoutDirection.LEFT_TO_RIGHT) {
            startX = parentPoint.getX() + parentW + nodeLineGap;
            startY = parentPoint.getY() + parentH / 2;
            endX = childPoint.getX() - nodeLineGap;
            endY = childPoint.getY() + childH / 2;
        } else if (direction == TreeNodeView.LayoutDirection.RIGHT_TO_LEFT) {
            startX = parentPoint.getX() - nodeLineGap;
            startY = parentPoint.getY() + parentH / 2;
            endX = childPoint.getX() + childW + nodeLineGap;
            endY = childPoint.getY() + childH / 2;
        }

    }

    /**
     * Draws the link between the given parent and child nodes.
     *
     * @param direction the layout direction
     * @param maxDimensionInLine the maximum node dimension in the active line
     * @param parent the parent node
     * @param parentPoint the parent node location
     * @param parentW the parent node width
     * @param parentH the parent node height
     * @param child the child node
     * @param childPoint the child node location
     * @param childW the child node width
     * @param childH the child node height
     * @param nodeLineGap the gap between a node and its link
     * @param vgap the vertical gap between levels
     * @param hgap the horizontal gap between levels
     * @return the nodes used to render the link
     */
    @Override
    public ArrayList<Node> drawNodeLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, TreeNode<T> parent, Point2D parentPoint, double parentW, double parentH, TreeNode<T> child, Point2D childPoint, double childW, double childH, double nodeLineGap, double vgap, double hgap) {
        calculateEndPoints(direction, parentPoint, parentW, parentH, childPoint, childW, childH, nodeLineGap);
        return drawLink(direction, maxDimensionInLine, startX, startY, endX, endY, vgap, hgap);
    }

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
    protected abstract ArrayList<Node> drawLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, double startX, double startY, double endX, double endY, double vgap, double hgap);

    /**
     * Creates a simple arrow node positioned at the link end point.
     *
     * @return the arrow node
     */
    protected Node createSimpleArrow() {
        Region arrow = new Region();
        arrow.getStyleClass().add("link-arrow");
        arrow.setPrefSize(8, 8);
        arrow.setMaxSize(8, 8);

        arrow.layoutXProperty().bind(arrow.widthProperty().divide(-2).add(endX));
        arrow.layoutYProperty().bind(arrow.heightProperty().divide(-2).add(endY));
        return arrow;
    }

    /**
     * Calculates the angle between the current start and end points.
     *
     * @return the angle in degrees
     */
    protected double calculateAngle() {
        return calculateAngle(startX, startY, endX, endY);
    }

    /**
     * Calculates the angle of the line between two points.
     *
     * @param p1X the x-coordinate of the first point
     * @param p1Y the y-coordinate of the first point
     * @param p2X the x-coordinate of the second point
     * @param p2Y the y-coordinate of the second point
     * @return the angle in degrees
     */
    protected double calculateAngle(double p1X, double p1Y, double p2X, double p2Y) {
        double dx = p2X - p1X;
        double dy = p2Y - p1Y;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * Calculates the angle of the line between two points.
     *
     * @param p1 the first point
     * @param p2 the second point
     * @return the angle in degrees
     */
    protected double calculateAngle(Point2D p1, Point2D p2) {
        return calculateAngle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    /**
     * Calculates the angle of the line between two path segments.
     *
     * @param lineTo1 the first path element
     * @param lineTo2 the second path element
     * @return the angle in degrees
     */
    protected double calculateAngle(LineTo lineTo1, LineTo lineTo2) {
        return calculateAngle(lineTo1.getX(),lineTo1.getY(), lineTo2.getX(),lineTo2.getY());
    }


    /**
     * Calculates the tangent angle of the given cubic curve.
     *
     * @param curve the curve
     * @param t the relative position on the curve
     * @return the tangent angle in degrees
     */
    protected double getTangentAngle(CubicCurve curve, double t) {
        double dx = bezierDerivative(curve.getStartX(), curve.getControlX1(), curve.getControlX2(), curve.getEndX(), t);
        double dy = bezierDerivative(curve.getStartY(), curve.getControlY1(), curve.getControlY2(), curve.getEndY(), t);

        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * Calculates the tangent angle of the given cubic curve segment.
     *
     * @param curveTo the curve segment
     * @param t the relative position on the curve
     * @return the tangent angle in degrees
     */
    protected double getTangentAngle(CubicCurveTo curveTo, double t) {
        double dx = bezierDerivative(startX, curveTo.getControlX1(), curveTo.getControlX2(), curveTo.getX(), t);
        double dy = bezierDerivative(startY, curveTo.getControlY1(), curveTo.getControlY2(), curveTo.getY(), t);
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    private double bezierDerivative(double p0, double p1, double p2, double p3, double t) {
        return 3 * (1 - t) * (1 - t) * (p1 - p0) + 6 * (1 - t) * t * (p2 - p1) + 3 * t * t * (p3 - p2);
    }

    /**
     * Calculates the tangent angle of the given quadratic curve.
     *
     * @param curve the curve
     * @param t the relative position on the curve
     * @return the tangent angle in degrees
     */
    protected double getTangentAngle(QuadCurve curve, double t) {
        double dx = bezierDerivative(curve.getStartX(), curve.getControlX(), curve.getEndX(), t);
        double dy = bezierDerivative(curve.getStartY(), curve.getControlY(), curve.getEndY(), t);

        return Math.toDegrees(Math.atan2(dy, dx));
    }

    private double bezierDerivative(double p0, double p1, double p2, double t) {
        return 2 * (1 - t) * (p1 - p0) + 2 * t * (p2 - p1);
    }

}
