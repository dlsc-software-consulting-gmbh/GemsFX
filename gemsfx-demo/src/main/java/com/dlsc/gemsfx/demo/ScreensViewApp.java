package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ScreensView;
import com.dlsc.gemsfx.util.SessionManager;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.prefs.Preferences;

public class ScreensViewApp extends Application {

    @Override
    public void start(Stage stage) {
        ScreensView screensView = new ScreensView();

        SessionManager sessionManager = new SessionManager(Preferences.userNodeForPackage(ScreensViewApp.class));
        sessionManager.register("screens.view.app.window.dragging", this.enableWindowDragging);
        sessionManager.register("screens.view.app.reflection", this.showReflection);
        sessionManager.register("screens.view.app.shadow", this.showShadow);
        sessionManager.register("screens.view.app.wallpaper", this.showWallpaper);
        sessionManager.register("screens.view.app.windows", this.showWindows);

        screensView.enableWindowDraggingProperty().bindBidirectional(this.enableWindowDragging);
        screensView.showReflectionProperty().bindBidirectional(this.showReflection);
        screensView.showShadowProperty().bindBidirectional(this.showShadow);
        screensView.showWallpaperProperty().bindBidirectional(this.showWallpaper);
        screensView.showWindowsProperty().bindBidirectional(this.showWindows);

        CheckBox enableWindowDragging = new CheckBox("Draggable Windows");
        enableWindowDragging.selectedProperty().bindBidirectional(this.enableWindowDragging);
        enableWindowDragging.disableProperty().bind(this.showWindows.not());
        CheckBox showReflection = new CheckBox("Reflection");
        showReflection.selectedProperty().bindBidirectional(this.showReflection);

        CheckBox showShadow = new CheckBox("Shadow");
        showShadow.selectedProperty().bindBidirectional(this.showShadow);

        CheckBox showWallpaper = new CheckBox("Wallpaper");
        showWallpaper.selectedProperty().bindBidirectional(this.showWallpaper);

        CheckBox showWindows = new CheckBox("Windows");
        showWindows.selectedProperty().bindBidirectional(this.showWindows);

        HBox controls = new HBox(10, enableWindowDragging, showShadow, showReflection, showWindows, showWallpaper);
        controls.setStyle("-fx-background-color: white");
        controls.setPadding(new Insets(10, 10, 10, 10));
        controls.setAlignment(Pos.CENTER_RIGHT);

        VBox.setVgrow(screensView, Priority.ALWAYS);
        VBox vBox = new VBox(screensView, new Separator(), controls);

        Scene scene = new Scene(vBox);

        Stage stage2 = new Stage();
        stage2.setX(500);
        stage2.setY(500);
        stage2.setTitle("Stage 2");
        stage2.setScene(new Scene(new Label("Hello World")));
        stage2.setAlwaysOnTop(true);
        stage2.initOwner(stage);
        StageManager.install(stage2, "screens.view.app.stage2");
        stage2.show();

        stage.setTitle("Screens View Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        StageManager.install(stage, "screens.view.app.stage1");
        stage.show();
    }

    private final BooleanProperty enableWindowDragging = new SimpleBooleanProperty(true);
    private final BooleanProperty showShadow = new SimpleBooleanProperty(true);
    private final BooleanProperty showReflection = new SimpleBooleanProperty(true);
    private final BooleanProperty showWindows = new SimpleBooleanProperty(true);
    private final BooleanProperty showWallpaper = new SimpleBooleanProperty(true);

    public static void main(String[] args) {
        launch();
    }
}
