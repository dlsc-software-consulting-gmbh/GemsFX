package com.dlsc.gemsfx;

import java.util.Optional;

/**
 * Resolves dockables by ID. Invoked only on explicit open requests, never on construction.
 */
@FunctionalInterface
public interface DockableFactory {

    /**
     * Creates the dockable for the given ID.
     *
     * @param id the requested dockable ID
     * @return the dockable, whose ID must equal the requested ID, or empty if the ID is unknown
     */
    Optional<Dockable> create(String id);
}
