package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

public class LogarithmicLink<T> extends AbstractLinkStrategy<T> {

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
