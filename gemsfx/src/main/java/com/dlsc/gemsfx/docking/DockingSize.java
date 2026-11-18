package com.dlsc.gemsfx.docking;

/**
 * An immutable preferred size for a dockable when it creates a side split.
 * The size describes width for left/right docking and height for top/bottom docking.
 * Weights refer to the entire destination {@link DockingPane}, not the target tab group.
 */
public final class DockingSize {

    /**
     * Units supported by a preferred docking size.
     */
    public enum Unit {
        /** A fraction of the destination pane's width or height. */
        WEIGHT,
        /** An initial size in JavaFX logical pixels. */
        PIXELS
    }

    private final double value;
    private final Unit unit;

    private DockingSize(double value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }

    /**
     * Creates a size relative to the entire destination pane.
     *
     * @param weight a finite value in [0, 1], from no space to the entire pane dimension
     * @return the preferred size
     * @throws IllegalArgumentException if the weight is outside [0, 1] or not finite
     */
    public static DockingSize weight(double weight) {
        if (!Double.isFinite(weight) || weight < 0 || weight > 1) {
            throw new IllegalArgumentException("weight must be finite and within [0, 1]");
        }
        return new DockingSize(weight, Unit.WEIGHT);
    }

    /**
     * Creates an initial size in JavaFX logical pixels.
     *
     * @param pixels a finite, nonnegative size
     * @return the preferred size
     * @throws IllegalArgumentException if the size is negative or not finite
     */
    public static DockingSize pixels(double pixels) {
        if (!Double.isFinite(pixels) || pixels < 0) {
            throw new IllegalArgumentException("pixels must be finite and nonnegative");
        }
        return new DockingSize(pixels, Unit.PIXELS);
    }

    /**
     * Returns the requested size in {@link #getUnit() the specified unit}.
     *
     * @return the size
     */
    public double getValue() {
        return value;
    }

    /**
     * Returns the unit of the requested size.
     *
     * @return the unit
     */
    public Unit getUnit() {
        return unit;
    }
}
