package com.dlsc.gemsfx.infocenter;

/**
 * Defines the position of the {@link InfoCenterView} within the {@link InfoCenterPane}.
 * The horizontal component (LEFT or RIGHT) determines which edge the info center slides
 * in from, and the vertical component (TOP, CENTER, or BOTTOM) determines where it is
 * placed along that edge.
 */
public enum InfoCenterViewPos {

    /**
     * Places the info center at the top-left edge of the pane.
     */
    TOP_LEFT,

    /**
     * Places the info center at the top-right edge of the pane.
     */
    TOP_RIGHT,

    /**
     * Places the info center centered on the left edge of the pane.
     */
    CENTER_LEFT,

    /**
     * Places the info center centered on the right edge of the pane.
     */
    CENTER_RIGHT,

    /**
     * Places the info center at the bottom-left edge of the pane.
     */
    BOTTOM_LEFT,

    /**
     * Places the info center at the bottom-right edge of the pane.
     */
    BOTTOM_RIGHT;

    /**
     * Returns {@code true} if this position is on the left side.
     *
     * @return {@code true} if the position is on the left side
     */
    public boolean isLeft() {
        return this == TOP_LEFT || this == CENTER_LEFT || this == BOTTOM_LEFT;
    }

    /**
     * Returns {@code true} if this position is on the right side.
     *
     * @return {@code true} if the position is on the right side
     */
    public boolean isRight() {
        return !isLeft();
    }
}