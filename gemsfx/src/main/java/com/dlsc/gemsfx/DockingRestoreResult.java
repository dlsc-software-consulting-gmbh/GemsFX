package com.dlsc.gemsfx;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable value object representing the result of a docking layout restore operation.
 * 
 * Contains the IDs of any dockables that could not be resolved during restoration.
 */
public final class DockingRestoreResult {
    
    private final Set<String> unresolvedDockableIds;
    
    /**
     * Constructs a DockingRestoreResult.
     *
     * @param unresolvedDockableIds list of dockable IDs that could not be resolved, must not be null, and must not contain null or blank elements
     * @throws IllegalArgumentException if unresolvedDockableIds is null or contains null/blank elements
     */
    public DockingRestoreResult(List<String> unresolvedDockableIds) {
        Objects.requireNonNull(unresolvedDockableIds, "unresolvedDockableIds must not be null");
        this.unresolvedDockableIds = new LinkedHashSet<>(unresolvedDockableIds.size());
        for (String id : unresolvedDockableIds) {
            if (id == null) {
                throw new IllegalArgumentException("unresolvedDockableIds must not contain null elements");
            }
            if (id.isBlank()) {
                throw new IllegalArgumentException("unresolvedDockableIds must not contain blank elements");
            }
            this.unresolvedDockableIds.add(id);
        }
    }
    
    /**
     * Returns an immutable copy of the unresolved dockable IDs.
     *
     * @return the list of unresolved IDs
     */
    public Set<String> getUnresolvedDockableIds() {
        return new LinkedHashSet<>(unresolvedDockableIds);
    }
}
