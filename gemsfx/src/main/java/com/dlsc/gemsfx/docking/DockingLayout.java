package com.dlsc.gemsfx.docking;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable data transfer object representing a complete docking layout snapshot.
 * 
 * A DockingLayout captures the current structure of all docking panes and floating windows
 * in the application.
 */
public final class DockingLayout {
    
    private final String primaryPaneId;
    private final List<PaneLayout> panes;
    
    /**
     * Constructs a DockingLayout.
     *
     * @param primaryPaneId the ID of the primary (non-floating) pane, must not be null or blank
     * @param panes         list of pane layouts, must not be null (may be empty), and must not contain null elements
     * @throws IllegalArgumentException if primaryPaneId is null/blank or panes contains null elements
     * @throws NullPointerException if panes is null
     */
    public DockingLayout(String primaryPaneId, List<PaneLayout> panes) {
        this.primaryPaneId = Objects.requireNonNull(primaryPaneId, "primaryPaneId must not be null");
        if (primaryPaneId.isBlank()) {
            throw new IllegalArgumentException("primaryPaneId must not be blank");
        }
        
        Objects.requireNonNull(panes, "panes must not be null");
        this.panes = new ArrayList<>(panes.size());
        Set<String> paneIds = new LinkedHashSet<>();
        for (PaneLayout pane : panes) {
            if (pane == null) {
                throw new IllegalArgumentException("panes must not contain null elements");
            }
            if (!paneIds.add(pane.getPaneId())) {
                throw new IllegalArgumentException("panes must not contain duplicate pane IDs: " + pane.getPaneId());
            }
            this.panes.add(pane);
        }
    }
    
    /**
     * Returns the ID of the primary pane.
     *
     * @return the primary pane ID
     */
    public String getPrimaryPaneId() {
        return primaryPaneId;
    }
    
    /**
     * Returns an immutable copy of the pane layouts.
     *
     * @return the list of pane layouts
     */
    public List<PaneLayout> getPanes() {
        return new ArrayList<>(panes);
    }
    
    /**
     * Immutable value object representing the layout and bounds of a single docking pane.
     */
    public static final class PaneLayout {
        private final String paneId;
        private final DockingLayoutNode root;
        private final StageBounds stageBounds;
        
        /**
         * Constructs a PaneLayout.
         *
         * @param paneId      the ID of this pane, must not be null or blank
         * @param root        the root layout node, must not be null
         * @param stageBounds the bounds of the stage (null for primary pane, non-null for floating panes)
         * @throws IllegalArgumentException if paneId is null/blank or root is null
         */
        public PaneLayout(String paneId, DockingLayoutNode root, StageBounds stageBounds) {
            this.paneId = Objects.requireNonNull(paneId, "paneId must not be null");
            if (paneId.isBlank()) {
                throw new IllegalArgumentException("paneId must not be blank");
            }
            this.root = Objects.requireNonNull(root, "root must not be null");
            this.stageBounds = stageBounds;
        }
        
        /**
         * Returns the ID of this pane.
         *
         * @return the pane ID
         */
        public String getPaneId() {
            return paneId;
        }
        
        /**
         * Returns the root layout node of this pane.
         *
         * @return the root node
         */
        public DockingLayoutNode getRoot() {
            return root;
        }
        
        /**
         * Returns the stage bounds of this pane, or null if this is the primary pane.
         *
         * @return the stage bounds, or null
         */
        public StageBounds getStageBounds() {
            return stageBounds;
        }
    }
    
    /**
     * Immutable value object representing the bounds and dimensions of a floating stage.
     */
    public static final class StageBounds {
        private final double x;
        private final double y;
        private final double width;
        private final double height;
        
        /**
         * Constructs StageBounds.
         *
         * @param x      the x coordinate, must be finite
         * @param y      the y coordinate, must be finite
         * @param width  the width, must be positive (> 0)
         * @param height the height, must be positive (> 0)
         * @throws IllegalArgumentException if coordinates/dimensions are non-finite or dimensions are non-positive
         */
        public StageBounds(double x, double y, double width, double height) {
            if (!Double.isFinite(x)) {
                throw new IllegalArgumentException("x must be finite");
            }
            if (!Double.isFinite(y)) {
                throw new IllegalArgumentException("y must be finite");
            }
            if (!Double.isFinite(width) || width <= 0) {
                throw new IllegalArgumentException("width must be finite and positive");
            }
            if (!Double.isFinite(height) || height <= 0) {
                throw new IllegalArgumentException("height must be finite and positive");
            }
            
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        /**
         * Returns the x coordinate.
         *
         * @return the x coordinate
         */
        public double getX() {
            return x;
        }
        
        /**
         * Returns the y coordinate.
         *
         * @return the y coordinate
         */
        public double getY() {
            return y;
        }
        
        /**
         * Returns the width.
         *
         * @return the width
         */
        public double getWidth() {
            return width;
        }
        
        /**
         * Returns the height.
         *
         * @return the height
         */
        public double getHeight() {
            return height;
        }
    }
}
