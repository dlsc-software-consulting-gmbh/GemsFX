package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public abstract class GemApplication extends Application {

    public String getDescription() {
        try (InputStream inputStream = getClass().getResourceAsStream(getClass().getSimpleName() + ".md")) {
            if (inputStream == null) {
                return "";
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "";
        }
    }

    static {
        if (Boolean.getBoolean("atlantafx")) {
            setUserAgentStylesheet(new CupertinoDark().getUserAgentStylesheet());
        }
    }

    public GemApplication() {
    }

    @Override
    public void start(Stage stage) {
        if (Boolean.getBoolean("atlantafx")) {
            stage.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    newScene.getStylesheets().add(Objects.requireNonNull(GemApplication.class.getResource("atlantafx.css")).toExternalForm());
                }
            });
        }
    }
}
