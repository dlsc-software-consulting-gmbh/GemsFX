package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.stage.Stage;

import java.util.Objects;

public abstract class GemApplication extends Application {

    public GemApplication() {
        if (Boolean.getBoolean("atlantafx")) {
            setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        }
    }

    @Override
    public void start(Stage stage) {
        if (Boolean.getBoolean("atlantafx")) {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("atlantafx.css")).toExternalForm());
                }
            });
        }
    }
}
