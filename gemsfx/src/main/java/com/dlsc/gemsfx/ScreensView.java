package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ScreensViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.scene.shape.Shape;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Callback;

/**
 * A view for displaying the geometry of the currently available screens. This
 * view displays each screen in its correct size. Big screens will be big, small
 * ones will be smaller. In addition, the view can also display the currently
 * showing windows of the application. For debugging purposes the view is also
 * capable of showing arbitrary shapes inside the within the unified bounds of
 * all screens.
 */
public class ScreensView extends Control {

    private final static Image DEFAULT_WALLPAPER = new Image(ScreensView.class.getResource("wallpaper.jpg").toExternalForm(), false);

    /**
     * Constructs a new view.
     */
    public ScreensView() {
        getStyleClass().add("screens-view");
        setWallpaperProvider(screen -> DEFAULT_WALLPAPER);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ScreensViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return ScreensView.class.getResource("screens-view.css").toExternalForm();
    }

    /**
     * Utility method to quickly bring up an instance of this view.
     *
     * @return the view that was created by the methhod call (can be used for further configuration)
     */
    public static ScreensView show() {
        ScreensView view = new ScreensView();
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.setScene(new Scene(view));
        stage.setX(10);
        stage.setY(20);
        stage.setTitle("Screens");
        stage.show();
        return view;
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

    private final BooleanProperty showReflection = new SimpleBooleanProperty(this, "showReflection", true);

    public final boolean isShowReflection() {
        return showReflection.get();
    }

    /**
     * Determines if a reflection effect will be applied to the miniature screens.
     *
     * @return true if a reflection effect shall be shown
     */
    public final BooleanProperty showReflectionProperty() {
        return showReflection;
    }

    public final void setShowReflection(boolean showReflection) {
        this.showReflection.set(showReflection);
    }

    private final BooleanProperty showWallpaper = new SimpleBooleanProperty(this, "showWallpaper", true);

    public final boolean isShowWallpaper() {
        return showWallpaper.get();
    }

    /**
     * Determines if the miniature screens should show a wallpaper or not. The wallpapers
     * are controlled via the {@link #wallpaperProviderProperty()}. If no wallpapers are shown
     * then the appearance of the miniature screens solely depends on CSS.
     *
     * @return true if the view should display wallpapers on the miniature screens
     */
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

    /**
     * Determines if the view should also show the currently active windows of
     * the running application.
     *
     * @return true if the windows should also be showing
     */
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

    /**
     * A callback used for looking up images that will be used as wallpapers on the miniature
     * screens.
     *
     * @return a callback for determining which wallpaper to use for which screen
     */
    public final ObjectProperty<Callback<Screen, Image>> wallpaperProviderProperty() {
        return wallpaperProvider;
    }

    public final void setWallpaperProvider(Callback<Screen, Image> wallpaperProvider) {
        this.wallpaperProvider.set(wallpaperProvider);
    }
}
