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
     * Represents a split layout dividing two child nodes.
     */
    final class Split implements DockingLayoutNode {
        private final Orientation orientation;
        private final double dividerPosition;
        private final DockingLayoutNode first;
        private final DockingLayoutNode second;
        
        /**
         * Constructs a Split layout node.
         *
         * @param orientation     the orientation of the split (HORIZONTAL or VERTICAL), must not be null
         * @param dividerPosition the divider position (typically 0.0 to 1.0), must be a finite number
         * @param first           the first child node, must not be null
         * @param second          the second child node, must not be null
         * @throws IllegalArgumentException if any parameter is null or dividerPosition is not finite
         */
        public Split(Orientation orientation, double dividerPosition, DockingLayoutNode first, DockingLayoutNode second) {
            this.orientation = Objects.requireNonNull(orientation, "orientation must not be null");
            
            if (!Double.isFinite(dividerPosition)) {
                throw new IllegalArgumentException("dividerPosition must be finite");
            }
            this.dividerPosition = dividerPosition;
            
            this.first = Objects.requireNonNull(first, "first must not be null");
            this.second = Objects.requireNonNull(second, "second must not be null");
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
         * Returns the divider position.
         *
         * @return the divider position
         */
        public double getDividerPosition() {
            return dividerPosition;
        }
        
        /**
         * Returns the first child node.
         *
         * @return the first node
         */
        public DockingLayoutNode getFirst() {
            return first;
        }
        
        /**
         * Returns the second child node.
         *
         * @return the second node
         */
        public DockingLayoutNode getSecond() {
            return second;
        }
    }
}
