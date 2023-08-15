package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

public class SimpleCatmullRomLink<T> extends AbstractLinkStrategy<T> {

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
