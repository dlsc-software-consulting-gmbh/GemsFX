package com.dlsc.gemsfx.docking.model;

import javafx.geometry.Orientation;

import java.util.Objects;

/**
 * A split of the docking tree with exactly two children.
 */
public final class DockingSplit implements DockingTree.Node {

    private final Orientation orientation;
    private double dividerPosition;
    private final DockingTree.Node first;
    private final DockingTree.Node second;

    DockingSplit(Orientation orientation, double dividerPosition, DockingTree.Node first, DockingTree.Node second) {
        this.orientation = Objects.requireNonNull(orientation);
        this.dividerPosition = dividerPosition;
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
    }

    void setDividerPosition(double dividerPosition) {
        this.dividerPosition = dividerPosition;
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public double getDividerPosition() {
        return dividerPosition;
    }

    public DockingTree.Node getFirst() {
        return first;
    }

    public DockingTree.Node getSecond() {
        return second;
    }
}
