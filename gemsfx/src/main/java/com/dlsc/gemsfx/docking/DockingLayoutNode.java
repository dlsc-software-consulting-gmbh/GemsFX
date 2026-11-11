package com.dlsc.gemsfx.docking;

import javafx.geometry.Orientation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Interface for immutable node tree structures that represent the layout of docking areas.
 * 
 * A DockingLayoutNode can be either:
 * <ul>
 *   <li>Empty - representing an empty pane</li>
 *   <li>Tabs - representing a tabbed container with multiple dockable items</li>
 *   <li>Split - representing a split layout with divider</li>
 * </ul>
 */
public interface DockingLayoutNode {
    
    /**
     * Represents an empty pane with no content.
     */
    final class Empty implements DockingLayoutNode {
        /**
         * Constructs an empty layout node.
         */
        public Empty() {
        }
    }
    
    /**
     * Represents a tabbed container with multiple dockable items.
     */
    final class Tabs implements DockingLayoutNode {
        private final List<String> dockableIds;
        private final String selectedId;
        
        /**
         * Constructs a Tabs layout node.
         *
         * @param dockableIds list of dockable IDs in this tab container, must not be null or empty, and all elements must be non-null and non-blank
         * @param selectedId  the ID of the currently selected tab, must not be null or blank, and must exist in dockableIds
         * @throws IllegalArgumentException if dockableIds is null/empty, contains null/blank elements, or selectedId is not in dockableIds
         * @throws NullPointerException if selectedId is null
         */
        public Tabs(List<String> dockableIds, String selectedId) {
            Objects.requireNonNull(dockableIds, "dockableIds must not be null");
            if (dockableIds.isEmpty()) {
                throw new IllegalArgumentException("dockableIds must not be empty");
            }
            
            this.dockableIds = new ArrayList<>(dockableIds.size());
            for (String id : dockableIds) {
                if (id == null) {
                    throw new IllegalArgumentException("dockableIds must not contain null elements");
                }
                if (id.isBlank()) {
                    throw new IllegalArgumentException("dockableIds must not contain blank elements");
                }
                this.dockableIds.add(id);
            }
            
            Objects.requireNonNull(selectedId, "selectedId must not be null");
            if (selectedId.isBlank()) {
                throw new IllegalArgumentException("selectedId must not be blank");
            }
            if (!this.dockableIds.contains(selectedId)) {
                throw new IllegalArgumentException("selectedId must be one of the dockableIds");
            }
            this.selectedId = selectedId;
        }
        
        /**
         * Returns an immutable copy of the dockable IDs.
         *
         * @return the list of dockable IDs
         */
        public List<String> getDockableIds() {
            return new ArrayList<>(dockableIds);
        }
        
        /**
         * Returns the ID of the currently selected tab.
         *
         * @return the selected ID
         */
        public String getSelectedId() {
            return selectedId;
        }
    }
    
    /**
     * A split with ordered children and normalized divider positions.
     */
    final class Split implements DockingLayoutNode {
        private final Orientation orientation;
        private final List<DockingLayoutNode> children;
        private final List<Double> dividerPositions;

        /**
         * Constructs an immutable split layout node.
         *
         * @param orientation the non-null split orientation
         * @param children at least two non-null children in layout order
         * @param dividerPositions one fewer positions than children, finite and nondecreasing within [0, 1]
         * @throws IllegalArgumentException if the child count or positions are invalid
         * @throws NullPointerException if an argument or child is null
         */
        public Split(Orientation orientation, List<DockingLayoutNode> children, List<Double> dividerPositions) {
            this.orientation = Objects.requireNonNull(orientation, "orientation must not be null");
            this.children = List.copyOf(Objects.requireNonNull(children, "children must not be null"));
            if (children.size() < 2) {
                throw new IllegalArgumentException("A split must contain at least two children");
            }
            Objects.requireNonNull(dividerPositions, "dividerPositions must not be null");
            if (dividerPositions.size() != children.size() - 1) {
                throw new IllegalArgumentException("A split must have one fewer divider positions than children");
            }
            double previous = 0;
            for (Double position : dividerPositions) {
                if (position == null || !(position >= previous && position <= 1)) {
                    throw new IllegalArgumentException("Divider positions must be finite, ordered, and within [0, 1]");
                }
                previous = position;
            }
            this.dividerPositions = List.copyOf(dividerPositions);
        }

        /**
         * Returns the orientation of this split.
         *
         * @return the orientation
         */
        public Orientation getOrientation() {
            return orientation;
        }

        /**
         * Returns immutable normalized divider positions. Equal positions and endpoints allow collapsed areas.
         *
         * @return the ordered divider positions
         */
        public List<Double> getDividerPositions() {
            return dividerPositions;
        }

        /**
         * Returns the immutable child nodes in layout order.
         *
         * @return the ordered children
         */
        public List<DockingLayoutNode> getChildren() {
            return children;
        }
    }
}
