package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.jpro.webapi.WebAPI;
import devtoolsfx.gui.GUI;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
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
            setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
        }
    }

    public GemApplication() {
    }

    protected final <T extends Node> T hideInBrowser(T node) {
        boolean visible = !WebAPI.isBrowser();
        node.setVisible(visible);
        node.setManaged(visible);
        return node;
    }

    protected final <T extends ButtonBase> T configureDevToolsButton(T button) {
        button.setText("Dev Tools");
        hideInBrowser(button);
        button.setOnAction(evt -> {
            if (button.getScene() != null && button.getScene().getWindow() instanceof Stage stage) {
                GUI.openToolStage(stage, getHostServices());
            }
        });
        return button;
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
