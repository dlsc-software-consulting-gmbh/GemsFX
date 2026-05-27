package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a right-angle polyline connector between a
 * parent and a child node.
 *
 * <p>The link consists of three line segments forming an elbow shape: one segment
 * travels halfway along the gap axis, a second crosses the perpendicular axis to
 * align with the child, and a third completes the connection. The bend point is
 * placed at the midpoint of the gap between parent and child. A directional arrow
 * is added at the child end.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class PolyLineLink<T> extends AbstractLinkStrategy<T> {

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

        Node arrow = createSimpleArrow();

        switch (direction) {
            case TOP_TO_BOTTOM:
                path.getElements().addAll(
                        new LineTo(startX, startY + vgap / 2),
                        new LineTo(endX, startY + vgap / 2),
                        new LineTo(endX, endY)
                );
                arrow.setRotate(90);
                break;

            case BOTTOM_TO_TOP:
                path.getElements().addAll(
                        new LineTo(startX, startY - vgap / 2),
                        new LineTo(endX, startY - vgap / 2),
                        new LineTo(endX, endY)
                );
                arrow.setRotate(-90);
                break;

            case LEFT_TO_RIGHT:
                path.getElements().addAll(
                        new LineTo(startX + hgap / 2, startY),
                        new LineTo(startX + hgap / 2, endY),
                        new LineTo(endX, endY)
                );
                arrow.setRotate(0);
                break;

            case RIGHT_TO_LEFT:
                path.getElements().addAll(
                        new LineTo(startX - hgap / 2, startY),
                        new LineTo(startX - hgap / 2, endY),
                        new LineTo(endX, endY)
                );
                arrow.setRotate(180);
                break;
        }

        path.getStyleClass().addAll("link-line");
        return new ArrayList<>(List.of(path, arrow));
    }
}


