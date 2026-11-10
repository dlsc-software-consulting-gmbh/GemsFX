package com.dlsc.gemsfx.docking;

/**
 * Enumeration defining the possible docking positions for a pane or content.
 */
public enum DockingPosition {
    /** Adds the dockable to the target tab group. */
    CENTER,
    /** Creates a split with the dockable before the target, horizontally. */
    LEFT,
    /** Creates a split with the dockable after the target, horizontally. */
    RIGHT,
    /** Creates a split with the dockable before the target, vertically. */
    TOP,
    /** Creates a split with the dockable after the target, vertically. */
    BOTTOM
}
