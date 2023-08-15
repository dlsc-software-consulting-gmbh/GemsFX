package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.scene.Node;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import java.util.ArrayList;
import java.util.List;

public class ClockHandLinkStrategy<T> extends AbstractLinkStrategy<T> {

    @Override
    protected ArrayList<Node> drawLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, double startX, double startY, double endX, double endY, double vgap, double hgap) {
        Path path = new Path();
        path.getStyleClass().add("link-path");

        // Radius of the tail circle
        double tailCircleRadius = 4;
        // The starting width of the link
        double startWidth = 2.5;
        // The end width of the link
        double endWidth = 0.5;

        // create a tail circle
        Circle tailCircle = new Circle(startX, startY, tailCircleRadius);
        tailCircle.getStyleClass().add("link-circle");

        // Calculate the angle of the connecting line to determine the width offset
        double angle = calculateAngle();

        double startOffsetX = startWidth * Math.sin(Math.toRadians(angle));
        double startOffsetY = -startWidth * Math.cos(Math.toRadians(angle));
        double endOffsetX = endWidth * Math.sin(Math.toRadians(angle));
        double endOffsetY = -endWidth * Math.cos(Math.toRadians(angle));

        // Create a polygon pointing to the target
        path.getElements().add(new MoveTo(startX + startOffsetX, startY + startOffsetY));
        path.getElements().add(new LineTo(endX + endOffsetX, endY + endOffsetY));
        path.getElements().add(new LineTo(endX - endOffsetX, endY - endOffsetY));
        path.getElements().add(new LineTo(startX - startOffsetX, startY - startOffsetY));
        path.getElements().add(new ClosePath());

        return new ArrayList<>(List.of(tailCircle, path));
    }
}
