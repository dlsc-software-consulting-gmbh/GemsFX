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

public abstract class AbstractLinkStrategy<T> implements LinkStrategy<T> {

    protected double startX;
    protected double startY;
    protected double endX;
    protected double endY;

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

    @Override
    public ArrayList<Node> drawNodeLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, TreeNode<T> parent, Point2D parentPoint, double parentW, double parentH, TreeNode<T> child, Point2D childPoint, double childW, double childH, double nodeLineGap, double vgap, double hgap) {
        calculateEndPoints(direction, parentPoint, parentW, parentH, childPoint, childW, childH, nodeLineGap);
        return drawLink(direction, maxDimensionInLine, startX, startY, endX, endY, vgap, hgap);
    }

    protected abstract ArrayList<Node> drawLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, double startX, double startY, double endX, double endY, double vgap, double hgap);

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
     * @return Calculate the angle of the line between the start and end points.
     */
    protected double calculateAngle() {
        return calculateAngle(startX, startY, endX, endY);
    }

    /**
     * @return Calculate the angle of the line between the p1 and p2.
     */
    protected double calculateAngle(double p1X, double p1Y, double p2X, double p2Y) {
        double dx = p2X - p1X;
        double dy = p2Y - p1Y;
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * @return Calculate the angle of the line between the points.
     */
    protected double calculateAngle(Point2D p1, Point2D p2) {
        return calculateAngle(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }

    protected double calculateAngle(LineTo lineTo1, LineTo lineTo2) {
        return calculateAngle(lineTo1.getX(),lineTo1.getY(), lineTo2.getX(),lineTo2.getY());
    }


    /**
     * @return Calculate the angle of the tangent of the curve at the given t.
     */
    protected double getTangentAngle(CubicCurve curve, double t) {
        double dx = bezierDerivative(curve.getStartX(), curve.getControlX1(), curve.getControlX2(), curve.getEndX(), t);
        double dy = bezierDerivative(curve.getStartY(), curve.getControlY1(), curve.getControlY2(), curve.getEndY(), t);

        return Math.toDegrees(Math.atan2(dy, dx));
    }

    /**
     * @return Calculate the angle of the tangent of the curve at the given t.
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
     * @return Calculate the angle of the tangent of the curve at the given t.
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
