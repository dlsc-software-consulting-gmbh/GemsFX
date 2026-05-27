package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a simple straight line between a parent and a
 * child node, with a directional arrow at the child end.
 *
 * <p>This is the most basic link strategy and is suitable as a default when no
 * special visual emphasis on the connection shape is needed.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class StraightLineLink<T> extends AbstractLinkStrategy<T> {

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
        Line line = new Line(startX, startY, endX, endY);
        line.getStyleClass().add("link-line");

        Node arrow = createSimpleArrow();
        arrow.setRotate(calculateAngle());

        return new ArrayList<>(List.of(line, arrow));
    }

}
