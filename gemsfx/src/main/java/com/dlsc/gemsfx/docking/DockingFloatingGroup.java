package com.dlsc.gemsfx.docking;

import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;

import java.util.Objects;

/**
 * Immutable snapshot of an internally floating tab group. Geometry is relative to the
 * hosting pane's content area. Requested size and edge gaps survive temporary clamping
 * when that area becomes too small.
 */
public final class DockingFloatingGroup {

    private final DockingLayoutNode.Tabs tabs;
    private final HPos horizontalAnchor;
    private final VPos verticalAnchor;
    private final double horizontalGap;
    private final double verticalGap;
    private final double width;
    private final double height;

    /**
     * Creates a saved floating group.
     *
     * @param tabs tab order and selection
     * @param horizontalAnchor LEFT or RIGHT
     * @param verticalAnchor TOP or BOTTOM
     * @param horizontalGap finite, nonnegative distance from the horizontal anchor
     * @param verticalGap finite, nonnegative distance from the vertical anchor
     * @param width finite, positive requested width
     * @param height finite, positive requested height
     */
    public DockingFloatingGroup(DockingLayoutNode.Tabs tabs, HPos horizontalAnchor, VPos verticalAnchor,
                               double horizontalGap, double verticalGap, double width, double height) {
        this.tabs = Objects.requireNonNull(tabs, "tabs must not be null");
        if (horizontalAnchor != HPos.LEFT && horizontalAnchor != HPos.RIGHT) {
            throw new IllegalArgumentException("horizontalAnchor must be LEFT or RIGHT");
        }
        if (verticalAnchor != VPos.TOP && verticalAnchor != VPos.BOTTOM) {
            throw new IllegalArgumentException("verticalAnchor must be TOP or BOTTOM");
        }
        if (!Double.isFinite(horizontalGap) || horizontalGap < 0
                || !Double.isFinite(verticalGap) || verticalGap < 0
                || !Double.isFinite(width) || width <= 0 || !Double.isFinite(height) || height <= 0) {
            throw new IllegalArgumentException("Gaps must be finite and nonnegative; dimensions must be finite and positive");
        }
        this.horizontalAnchor = horizontalAnchor;
        this.verticalAnchor = verticalAnchor;
        this.horizontalGap = horizontalGap;
        this.verticalGap = verticalGap;
        this.width = width;
        this.height = height;
    }

    static DockingFloatingGroup atBounds(DockingLayoutNode.Tabs tabs, Rectangle2D bounds,
                                         double paneWidth, double paneHeight) {
        Objects.requireNonNull(bounds, "bounds must not be null");
        if (!Double.isFinite(bounds.getMinX()) || !Double.isFinite(bounds.getMinY())) {
            throw new IllegalArgumentException("Coordinates must be finite");
        }
        double x = Math.max(0, paneWidth == 0 ? bounds.getMinX()
                : Math.min(bounds.getMinX(), paneWidth - bounds.getWidth()));
        double y = Math.max(0, paneHeight == 0 ? bounds.getMinY()
                : Math.min(bounds.getMinY(), paneHeight - bounds.getHeight()));
        double right = Math.max(0, paneWidth - x - bounds.getWidth());
        double bottom = Math.max(0, paneHeight - y - bounds.getHeight());
        boolean left = paneWidth == 0 || x <= right;
        boolean top = paneHeight == 0 || y <= bottom;
        return new DockingFloatingGroup(tabs, left ? HPos.LEFT : HPos.RIGHT, top ? VPos.TOP : VPos.BOTTOM,
                left ? x : right, top ? y : bottom, bounds.getWidth(), bounds.getHeight());
    }

    DockingFloatingGroup withTabs(DockingLayoutNode.Tabs value) {
        return new DockingFloatingGroup(value, horizontalAnchor, verticalAnchor,
                horizontalGap, verticalGap, width, height);
    }

    /**
     * Resolves the visible bounds without modifying saved geometry.
     *
     * @param paneWidth finite, nonnegative content-area width
     * @param paneHeight finite, nonnegative content-area height
     * @return bounds constrained to the content area
     */
    public Rectangle2D resolveBounds(double paneWidth, double paneHeight) {
        if (!Double.isFinite(paneWidth) || paneWidth < 0 || !Double.isFinite(paneHeight) || paneHeight < 0) {
            throw new IllegalArgumentException("Pane dimensions must be finite and nonnegative");
        }
        double w = Math.min(width, paneWidth);
        double h = Math.min(height, paneHeight);
        double x = horizontalAnchor == HPos.LEFT ? horizontalGap : paneWidth - horizontalGap - w;
        double y = verticalAnchor == VPos.TOP ? verticalGap : paneHeight - verticalGap - h;
        return new Rectangle2D(Math.max(0, Math.min(x, paneWidth - w)),
                Math.max(0, Math.min(y, paneHeight - h)), w, h);
    }

    /**
     * Returns the tab order and selection.
     * @return the tabs
     */
    public DockingLayoutNode.Tabs getTabs() {
        return tabs;
    }

    /**
     * Returns the horizontal anchor.
     * @return LEFT or RIGHT
     */
    public HPos getHorizontalAnchor() {
        return horizontalAnchor;
    }

    /**
     * Returns the vertical anchor.
     * @return TOP or BOTTOM
     */
    public VPos getVerticalAnchor() {
        return verticalAnchor;
    }

    /**
     * Returns the saved horizontal edge gap.
     * @return the gap
     */
    public double getHorizontalGap() {
        return horizontalGap;
    }

    /**
     * Returns the saved vertical edge gap.
     * @return the gap
     */
    public double getVerticalGap() {
        return verticalGap;
    }

    /**
     * Returns the requested width before clamping.
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the requested height before clamping.
     * @return the height
     */
    public double getHeight() {
        return height;
    }
}
