package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ScreensView;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class StageManagerApp extends Application {

    @Override
    public void start(Stage stage) {

        Label label = new Label("Stage Manager Test");
        StackPane stackPane = new StackPane(label);
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        stage.setTitle("Stage Manager App");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(800);
        stage.centerOnScreen();

        StageManager.install(stage, "stage.manager.demo.application", 100, 100);

        stage.show();

        ScreensView screensView = ScreensView.show();
        screensView.setShowWindows(true);
    }

    public static void main(String[] args) {
        launch();
    }
}
