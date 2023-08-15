package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

public class PolyLineLink<T> extends AbstractLinkStrategy<T> {

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


