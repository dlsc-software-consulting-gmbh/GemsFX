package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.Line;

import java.util.ArrayList;
import java.util.List;

public class StraightLineLink<T> extends AbstractLinkStrategy<T> {

    @Override
    protected ArrayList<Node> drawLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, double startX, double startY, double endX, double endY, double vgap, double hgap) {
        Line line = new Line(startX, startY, endX, endY);
        line.getStyleClass().add("link-line");

        Node arrow = createSimpleArrow();
        arrow.setRotate(calculateAngle());

        return new ArrayList<>(List.of(line, arrow));
    }

}
