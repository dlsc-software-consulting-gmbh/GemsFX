package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a smooth S-curve link using a cubic Bézier
 * approximation of a Catmull-Rom spline.
 *
 * <p>The control points are placed at half the gap distance along the primary axis,
 * offset toward the child, producing a gentle S-shaped curve that flows naturally
 * between parent and child. A directional arrow is added at the child end, oriented
 * along the curve's tangent.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class SimpleCatmullRomLink<T> extends AbstractLinkStrategy<T> {

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
        Path path = new Path();
        path.getStyleClass().add("link-path");
        path.getElements().add(new MoveTo(startX, startY));

        CubicCurveTo curve;
        Node arrow = createSimpleArrow();

        switch (direction) {
            case TOP_TO_BOTTOM:
                curve = new CubicCurveTo(startX, startY + vgap / 2,
                        endX, endY - vgap / 2,
                        endX, endY);
                path.getElements().add(curve);
                arrow.setRotate(getTangentAngle(curve, 0.95));
                break;

            case BOTTOM_TO_TOP:
                curve = new CubicCurveTo(startX, startY - vgap / 2,
                        endX, endY + vgap / 2,
                        endX, endY);
                path.getElements().add(curve);
                arrow.setRotate(getTangentAngle(curve, 0.05));
                break;

            case LEFT_TO_RIGHT:
                curve = new CubicCurveTo(startX + hgap / 2, startY,
                        endX - hgap / 2, endY,
                        endX, endY);
                path.getElements().add(curve);
                arrow.setRotate(getTangentAngle(curve, 0.95));
                break;

            case RIGHT_TO_LEFT:
                curve = new CubicCurveTo(startX - hgap / 2, startY,
                        endX + hgap / 2, endY,
                        endX, endY);
                path.getElements().add(curve);
                arrow.setRotate(getTangentAngle(curve, 0.05));
                break;
        }

        return new ArrayList<>(List.of(path, arrow));
    }
}
