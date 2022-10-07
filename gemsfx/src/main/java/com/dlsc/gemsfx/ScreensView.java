package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ScreensViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class ScreensView extends Control {

    public ScreensView() {
        getStyleClass().add("screens-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ScreensViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return ScreensView.class.getResource("screens-view.css").toExternalForm();
    }

    private final BooleanProperty showWindows = new SimpleBooleanProperty(this, "showWindows", true);

    public final boolean isShowWindows() {
        return showWindows.get();
    }

    public final BooleanProperty showWindowsProperty() {
        return showWindows;
    }

    public final void setShowWindows(boolean showWindows) {
        this.showWindows.set(showWindows);
    }
}
