package com.dlsc.gemsfx.docking.model;

import javafx.geometry.Orientation;

import java.util.List;
import java.util.Objects;

/**
 * A split with ordered children and normalized divider positions.
 */
public final class DockingSplit implements DockingTree.Node {

    private final Orientation orientation;
    private List<Double> dividerPositions;
    private final List<DockingTree.Node> children;

    DockingSplit(Orientation orientation, List<DockingTree.Node> children, List<Double> dividerPositions) {
        this.orientation = Objects.requireNonNull(orientation);
        this.children = List.copyOf(children);
        if (children.size() < 2) {
            throw new IllegalArgumentException("A split must contain at least two children");
        }
        setDividerPositions(dividerPositions);
    }

    void setDividerPositions(List<Double> positions) {
        Objects.requireNonNull(positions, "positions must not be null");
        if (positions.size() != children.size() - 1) {
            throw new IllegalArgumentException("A split must have one fewer divider positions than children");
        }
        double previous = 0;
        for (Double position : positions) {
            if (position == null || !(position >= previous && position <= 1)) {
                throw new IllegalArgumentException("Divider positions must be finite, ordered, and within [0, 1]");
            }
            previous = position;
        }
        dividerPositions = List.copyOf(positions);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public List<Double> getDividerPositions() {
        return dividerPositions;
    }

    public List<DockingTree.Node> getChildren() {
        return children;
    }
}
