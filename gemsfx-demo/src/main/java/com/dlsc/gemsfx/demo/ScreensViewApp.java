package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ScreensView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
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

public class ScreensViewApp extends Application {

    @Override
    public void start(Stage stage) {
        ScreensView screensView = new ScreensView();

        CheckBox showWallpaper = new CheckBox("Show Wallpaper");
        showWallpaper.selectedProperty().bindBidirectional(screensView.showWallpaperProperty());

        CheckBox showWindows = new CheckBox("Show Windows");
        showWindows.selectedProperty().bindBidirectional(screensView.showWindowsProperty());

        HBox controls = new HBox(10, showWindows, showWallpaper);
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
        stage2.show();

        stage.show();

        CSSFX.start(scene);
        stage.setTitle("Screens View Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
