package com.dlsc.gemsfx.docking;

import javafx.stage.Stage;

/**
 * Creates the stage that hosts a docking pane, e.g. for floating windows.
 */
@FunctionalInterface
public interface DockingStageFactory {

    /**
     * Creates a stage hosting the supplied pane.
     *
     * @param pane the pane to host
     * @return the stage hosting the pane
     */
    Stage create(DockingPane pane);
}
