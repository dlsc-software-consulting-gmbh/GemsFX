package com.dlsc.gemsfx;

import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class ThreeItemsPane extends Pane {

    public ThreeItemsPane() {
        InvalidationListener updateChildrenListener = it -> updateChildren();
        item1.addListener(updateChildrenListener);
        item2.addListener(updateChildrenListener);
        item3.addListener(updateChildrenListener);

        orientation.addListener(it -> requestLayout());
    }

    private void updateChildren() {
        getChildren().clear();

        if (getItem1() != null) {
            getChildren().add(getItem1());
        }
        if (getItem2() != null) {
            getChildren().add(getItem2());
        }
        if (getItem3() != null) {
            getChildren().add(getItem3());
        }
    }

    private final ObjectProperty<Orientation> orientation = new SimpleObjectProperty<>(Orientation.HORIZONTAL);

    public Orientation getOrientation() {
        return orientation.get();
    }

    public ObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    private final ObjectProperty<Node> item1 = new SimpleObjectProperty<>(this, "item1");

    public Node getItem1() {
        return item1.get();
    }

    public ObjectProperty<Node> item1Property() {
        return item1;
    }

    public void setItem1(Node item1) {
        this.item1.set(item1);
    }

    private final ObjectProperty<Node> item2 = new SimpleObjectProperty<>(this, "item2");

    public Node getItem2() {
        return item2.get();
    }

    public ObjectProperty<Node> item2Property() {
        return item2;
    }

    public void setItem2(Node item2) {
        this.item2.set(item2);
    }

    private final ObjectProperty<Node> item3 = new SimpleObjectProperty<>(this, "item3");

    public Node getItem3() {
        return item3.get();
    }

    public ObjectProperty<Node> item3Property() {
        return item3;
    }

    public void setItem3(Node item3) {
        this.item3.set(item3);
    }

    private final DoubleProperty spacing = new SimpleDoubleProperty(this, "spacing", 0);

    public double getSpacing() {
        return spacing.get();
    }

    public DoubleProperty spacingProperty() {
        return spacing;
    }

    public void setSpacing(double spacing) {
        this.spacing.set(spacing);
    }

    @Override
    protected void layoutChildren() {
        Insets insets = getInsets();
        double w = getWidth();
        double h = getHeight();

        if (getOrientation() == Orientation.HORIZONTAL) {
            layoutChildrenHorizontally(insets, w, h, getItem1(), getItem2(), getItem3());
        } else {
            layoutChildrenVertically(insets, w, h, getItem1(), getItem2(), getItem3());
        }
    }

    @Override
    protected double computePrefHeight(double width) {
        double h1 = 0;
        double h2 = 0;
        double h3 = 0;

        if (getItem1() != null) {
            h1 = getItem1().prefHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem2() != null) {
            h2 = getItem2().prefHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem3() != null) {
            h3 = getItem3().prefHeight(width - getInsets().getLeft() - getInsets().getRight());
        }

        if (getOrientation() == Orientation.HORIZONTAL) {
            return Math.max(h1, Math.max(h2, h3)) + getInsets().getTop() + getInsets().getBottom();
        } else {
            return Math.max(0, h1 + h2 + h3 + ((getChildren().size() - 1) * getSpacing()) + getInsets().getTop() + getInsets().getBottom());
        }
    }

    @Override
    protected double computeMinHeight(double width) {
        double h1 = 0;
        double h2 = 0;
        double h3 = 0;

        if (getItem1() != null) {
            h1 = getItem1().minHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem2() != null) {
            h2 = getItem2().minHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem3() != null) {
            h3 = getItem3().minHeight(width - getInsets().getLeft() - getInsets().getRight());
        }

        if (getOrientation() == Orientation.HORIZONTAL) {
            return Math.max(h1, Math.max(h2, h3)) + getInsets().getTop() + getInsets().getBottom();
        } else {
            return Math.max(0, h1 + h2 + h3 + ((getChildren().size() - 1) * getSpacing()) + getInsets().getTop() + getInsets().getBottom());
        }
    }

    @Override
    protected double computeMaxHeight(double width) {
        double h1 = 0;
        double h2 = 0;
        double h3 = 0;

        if (getItem1() != null) {
            h1 = getItem1().maxHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem2() != null) {
            h2 = getItem2().maxHeight(width - getInsets().getLeft() - getInsets().getRight());
        }
        if (getItem3() != null) {
            h3 = getItem3().maxHeight(width - getInsets().getLeft() - getInsets().getRight());
        }

        if (getOrientation() == Orientation.HORIZONTAL) {
            return Math.max(h1, Math.min(h2, h3)) + getInsets().getTop() + getInsets().getBottom();
        } else {
            return Double.MAX_VALUE;
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        double w1 = 0;
        double w2 = 0;
        double w3 = 0;

        if (getItem1() != null) {
            w1 = getItem1().prefWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem2() != null) {
            w2 = getItem2().prefWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem3() != null) {
            w3 = getItem3().prefWidth(height - getInsets().getTop() - getInsets().getBottom());
        }

        if (getOrientation() == Orientation.VERTICAL) {
            return Math.max(w1, Math.max(w2, w3)) + getInsets().getLeft() + getInsets().getRight();
        } else {
            return Math.max(0, w1 + w2 + w3 + (getChildren().size() - 1 * getSpacing()) + getInsets().getLeft() + getInsets().getRight());
        }
    }

    @Override
    protected double computeMinWidth(double height) {
        double w1 = 0;
        double w2 = 0;
        double w3 = 0;

        if (getItem1() != null) {
            w1 = getItem1().minWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem2() != null) {
            w2 = getItem2().minWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem3() != null) {
            w3 = getItem3().minWidth(height - getInsets().getTop() - getInsets().getBottom());
        }

        if (getOrientation() == Orientation.VERTICAL) {
            return Math.max(w1, Math.max(w2, w3)) + getInsets().getLeft() + getInsets().getRight();
        } else {
            return Math.max(0, w1 + w2 + w3 + (getChildren().size() - 1 * getSpacing()) + getInsets().getLeft() + getInsets().getRight());
        }
    }

    @Override
    protected double computeMaxWidth(double height) {
        double w1 = 0;
        double w2 = 0;
        double w3 = 0;

        if (getItem1() != null) {
            w1 = getItem1().maxWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem2() != null) {
            w2 = getItem2().maxWidth(height - getInsets().getTop() - getInsets().getBottom());
        }
        if (getItem3() != null) {
            w3 = getItem3().maxWidth(height - getInsets().getTop() - getInsets().getBottom());
        }

        if (getOrientation() == Orientation.VERTICAL) {
            return Math.max(w1, Math.max(w2, w3)) + getInsets().getLeft() + getInsets().getRight();
        } else {
            return Double.MAX_VALUE;
        }
    }

    private void layoutChildrenHorizontally(Insets insets, double width, double height, Node item1, Node item2, Node item3) {
        double minimumX = 0;
        if (item1 != null) {
            double prefWidth = item1.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item1.prefHeight(width - insets.getLeft() - insets.getRight());
            item1.resizeRelocate(insets.getLeft(), height / 2 - prefHeight / 2, prefWidth, prefHeight);
            minimumX = insets.getLeft() + prefWidth + getSpacing();
        }

        if (item2 != null) {
            double prefWidth = item2.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item2.prefHeight(width - insets.getLeft() - insets.getRight());
            double mx = width / 2;
            double x = Math.max(minimumX, mx - prefWidth / 2);
            item2.resizeRelocate(x, height / 2 - prefHeight / 2, prefWidth, prefHeight);
            minimumX = x + prefWidth + getSpacing();
        }

        if (item3 != null) {
            double prefWidth = item3.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item3.prefHeight(width - insets.getLeft() - insets.getRight());
            double x = Math.max(minimumX, width - insets.getRight() - prefWidth);
            item3.resizeRelocate(x, height / 2 - prefHeight / 2, prefWidth, prefHeight);
        }
    }

    private void layoutChildrenVertically(Insets insets, double width, double height, Node item1, Node item2, Node item3) {
        double minimumY = 0;
        if (item1 != null) {
            double prefWidth = item1.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item1.prefHeight(width - insets.getLeft() - insets.getRight());
            item1.resizeRelocate(width / 2 - prefWidth / 2, insets.getTop(), prefWidth, prefHeight);
            minimumY = insets.getTop() + prefHeight + getSpacing();
        }

        if (item2 != null) {
            double prefWidth = item2.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item2.prefHeight(width - insets.getLeft() - insets.getRight());
            double my = height / 2;
            double y = Math.max(minimumY, my - prefHeight / 2);
            item2.resizeRelocate(width / 2 - prefWidth / 2, y, prefWidth, prefHeight);
            minimumY = y + prefHeight + getSpacing();
        }

        if (item3 != null) {
            double prefWidth = item3.prefWidth(height - insets.getTop() - insets.getBottom());
            double prefHeight = item3.prefHeight(width - insets.getLeft() - insets.getRight());
            double y = Math.max(minimumY, height - insets.getBottom() - prefHeight);
            item3.resizeRelocate(width / 2 - prefWidth / 2, y, prefWidth, prefHeight);
        }
    }
}
