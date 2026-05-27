package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link LinkStrategy} that draws a link whose path follows a sine-wave easing
 * curve (specifically a quarter-sine from 0 to π/2).
 *
 * <p>The path is approximated by 10 line segments whose positions are computed using
 * {@code sin(π/2 · fraction)} along the primary axis. This produces a smooth,
 * ease-in-out-style path that starts slowly from the parent, accelerates through
 * the middle, and arrives smoothly at the child. A directional arrow is added at the
 * child end.
 *
 * @param <T> the type of the data value stored in each tree node
 */
public class SineWaveDecayLink<T> extends AbstractLinkStrategy<T> {

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

        switch (direction) {
            case TOP_TO_BOTTOM:
            case BOTTOM_TO_TOP:
                for (int i = 1; i <= 10; i++) {
                    double fraction = i * 1.0 / 10;
                    double x = startX + fraction * (endX - startX);
                    double y = startY + (Math.sin(Math.PI / 2 * fraction) * (endY - startY));
                    path.getElements().add(new LineTo(x, y));
                }
                break;
            case LEFT_TO_RIGHT:
            case RIGHT_TO_LEFT:
                for (int i = 1; i <= 10; i++) {
                    double fraction = i * 1.0 / 10;
                    double x = startX + (Math.sin(Math.PI / 2 * fraction) * (endX - startX));
                    double y = startY + fraction * (endY - startY);
                    path.getElements().add(new LineTo(x, y));
                }
                break;
        }

        LineTo p2 = (LineTo) path.getElements().get(path.getElements().size() - 1);
        LineTo p1 = (LineTo) path.getElements().get(path.getElements().size() - 2);
        double angle = calculateAngle(p1, p2);

        Node arrow = createSimpleArrow();
        arrow.setRotate(angle);

        return new ArrayList<>(List.of(path, arrow));
    }
}
