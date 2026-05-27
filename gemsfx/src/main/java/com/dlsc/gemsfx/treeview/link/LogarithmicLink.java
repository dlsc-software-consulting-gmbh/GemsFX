package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a link whose path follows a logarithmic curve.
 *
 * <p>The path is approximated by 10 line segments whose positions are computed using
 * {@code log₂(1 + fraction)} along the primary axis. This produces a curve that
 * accelerates quickly near the parent node and then levels off as it approaches the
 * child node. A small directional arrow is added at the child end.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class LogarithmicLink<T> extends AbstractLinkStrategy<T> {

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

        if (direction == TreeNodeView.LayoutDirection.TOP_TO_BOTTOM || direction == TreeNodeView.LayoutDirection.BOTTOM_TO_TOP) {
            for (int i = 1; i <= 10; i++) {
                double fraction = i * 1.0 / 10;
                double x = startX + fraction * (endX - startX);
                double y = startY + Math.log(1 + fraction) / Math.log(2) * (endY - startY);
                path.getElements().add(new LineTo(x, y));
            }
        } else {
            for (int i = 1; i <= 10; i++) {
                double fraction = i * 1.0 / 10;
                double x = startX + Math.log(1 + fraction) / Math.log(2) * (endX - startX);
                double y = startY + fraction * (endY - startY);
                path.getElements().add(new LineTo(x, y));
            }
        }

        Node arrow = createSimpleArrow();
        arrow.setRotate(calculateAngle(startX, startY, endX, endY));
        return new ArrayList<>(List.of(path, arrow));
    }

}
