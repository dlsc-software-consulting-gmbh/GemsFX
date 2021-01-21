package com.dlsc.gemsfx.skins.dialog;

import javafx.scene.control.ProgressIndicator;

public class BusyIndicator extends CircularProgressIndicator {

    public BusyIndicator() {
        getStyleClass().add("busy-indicator");
    }

    public void stop() {
        setProgress(0);
    }

    public void start() {
        setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
    }
}
