package com.dlsc.gemsfx.docking;

import javafx.stage.Stage;

/**
 * Creates the stage that hosts a docking pane, e.g. for floating windows.
 * <p>
 * The factory only builds the stage. The {@link DockingContext} positions and shows it, both when
 * a dockable is floated and when a layout is restored. A factory should therefore not call
 * {@link Stage#show()} itself: a stage shown by the factory appears before it has been positioned,
 * and during a layout restore before the previous layout has been replaced.
 */
@FunctionalInterface
public interface DockingStageFactory {

    /**
     * Creates a stage hosting the supplied pane. The stage must have a scene containing the pane
     * and should not be showing yet.
     *
     * @param pane the pane to host
     * @return the stage hosting the pane
     */
    Stage create(DockingPane pane);
}
