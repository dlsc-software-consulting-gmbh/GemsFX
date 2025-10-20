package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SegmentedBar;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.util.Callback;

import java.util.*;

public class SegmentedBarSkin<T extends SegmentedBar.Segment> extends SkinBase<SegmentedBar<T>> {

    private final Map<T, Node> segmentNodes = new HashMap<>();

    private final InvalidationListener buildListener = it -> buildSegments();

    private final WeakInvalidationListener weakBuildListener = new WeakInvalidationListener(buildListener);

    private final InvalidationListener layoutListener = it -> getSkinnable().requestLayout();

    private final WeakInvalidationListener weakLayoutListener = new WeakInvalidationListener(layoutListener);

    public SegmentedBarSkin(SegmentedBar<T> bar) {
        super(bar);

        bar.segmentViewFactoryProperty().addListener(weakBuildListener);
        bar.getSegments().addListener(weakBuildListener);
        bar.orientationProperty().addListener(weakLayoutListener);
        bar.minSegmentSizeProperty().addListener(weakLayoutListener);
        bar.totalProperty().addListener(weakBuildListener);
        buildSegments();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            OptionalDouble maxHeight = getChildren().stream().mapToDouble(node -> node.prefHeight(-1)).max();
            if (maxHeight.isPresent()) {
                return maxHeight.getAsDouble();
            }
        }

        return getSkinnable().getPrefHeight();
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            OptionalDouble maxWidth = getChildren().stream().mapToDouble(node -> node.prefWidth(height)).max();
            if (maxWidth.isPresent()) {
                return maxWidth.getAsDouble();
            }
        }

        return getSkinnable().getPrefWidth();
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return 0;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        }

        return 0;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
            return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
        }

        return Double.MAX_VALUE;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        if (getSkinnable().getOrientation().equals(Orientation.VERTICAL)) {
            return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
        }

        return Double.MAX_VALUE;
    }

    private void buildSegments() {
        segmentNodes.clear();
        getChildren().clear();

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        Callback<T, Node> cellFactory = getSkinnable().getSegmentViewFactory();

        for (int i = 0; i < size; i++) {
            T segment = segments.get(i);
            Node segmentNode = cellFactory.call(segment);
            segmentNodes.put(segment, segmentNode);
            getChildren().add(segmentNode);

            segmentNode.getStyleClass().add("segment");

            if (i == 0) {
                if (size == 1) {
                    segmentNode.getStyleClass().add("only-segment");
                } else {
                    segmentNode.getStyleClass().add("first-segment");
                }
            } else if (i == size - 1) {
                segmentNode.getStyleClass().add("last-segment");
            } else {
                segmentNode.getStyleClass().add("middle-segment");
            }
        }

        getSkinnable().requestLayout();
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        double total = getSkinnable().getTotal();

        List<T> segments = getSkinnable().getSegments();
        int size = segments.size();

        double x = contentX;
        double y = contentY + contentHeight;

        // we have to ensure that very small segments are also visible
        double minSegmentSize = getSkinnable().getMinSegmentSize();

        for (SegmentedBar.Segment segment : segments) {
            Node segmentNode = segmentNodes.get(segment);
            double segmentValue = segment.getValue();

            if (getSkinnable().getOrientation().equals(Orientation.HORIZONTAL)) {
                double segmentWidth = segmentValue / total * contentWidth;
                if (segmentWidth <= minSegmentSize) {
                    double stolenWidth = minSegmentSize - segmentWidth;
                    contentWidth -= stolenWidth;
                    segmentWidth = minSegmentSize;
                }
                segmentNode.resizeRelocate(x, contentY, segmentWidth, contentHeight);
                x += segmentWidth;
            } else {
                double segmentHeight = segmentValue / total * contentHeight;
                if (segmentHeight <= minSegmentSize) {
                    double stolenHeight = minSegmentSize - segmentHeight;
                    contentHeight -= stolenHeight;
                    segmentHeight = minSegmentSize;
                }
                segmentNode.resizeRelocate(contentX, y - segmentHeight, contentWidth, segmentHeight);
                y -= segmentHeight;
            }
        }
    }
}
