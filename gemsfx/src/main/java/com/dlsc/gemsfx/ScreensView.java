package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ScreensViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.util.Callback;

public class ScreensView extends Control {

    public ScreensView() {
        getStyleClass().add("screens-view");
        Image wallpaper = new Image(ScreensView.class.getResource("wallpaper.jpg").toExternalForm(), false);
        setWallpaperProvider(screen -> wallpaper);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ScreensViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return ScreensView.class.getResource("screens-view.css").toExternalForm();
    }

    private final ObservableList<Shape> shapes = FXCollections.observableArrayList();

    /**
     * A list of arbitrary shapes that can be added to this view.
     *
     * @return list of shapes to add to this view.
     */
    public final ObservableList<Shape> getShapes() {
        return shapes;
    }

    private final BooleanProperty showWallpaper = new SimpleBooleanProperty(this, "showWallpaper", true);

    public final boolean isShowWallpaper() {
        return showWallpaper.get();
    }

    public final BooleanProperty showWallpaperProperty() {
        return showWallpaper;
    }

    public final void setShowWallpaper(boolean showWallpaper) {
        this.showWallpaper.set(showWallpaper);
    }

    private final BooleanProperty showWindows = new SimpleBooleanProperty(this, "showWindows", false);

    public final boolean isShowWindows() {
        return showWindows.get();
    }

    public final BooleanProperty showWindowsProperty() {
        return showWindows;
    }

    public final void setShowWindows(boolean showWindows) {
        this.showWindows.set(showWindows);
    }

    private final ObjectProperty<Callback<Screen, Image>> wallpaperProvider = new SimpleObjectProperty(this, "wallpaperProvider");

    public final Callback<Screen, Image> getWallpaperProvider() {
        return wallpaperProvider.get();
    }

    public final ObjectProperty<Callback<Screen, Image>> wallpaperProviderProperty() {
        return wallpaperProvider;
    }

    public final void setWallpaperProvider(Callback<Screen, Image> wallpaperProvider) {
        this.wallpaperProvider.set(wallpaperProvider);
    }
}
