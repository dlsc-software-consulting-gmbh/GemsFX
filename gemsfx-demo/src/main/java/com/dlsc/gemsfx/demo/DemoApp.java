package com.dlsc.gemsfx.demo;

import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DemoApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        Scene scene = new Scene(new Label("No demos, yet."));
        stage.setTitle("UnitFX Demo");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    private Node wrap(String title, Node node) {
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 16px;");
        VBox box = new VBox(titleLabel, node);
        box.setStyle("-fx-background-color: black, white; -fx-background-insets: 0, 1; -fx-padding: 10px; -fx-spacing: 20;");
        return box;
    }

    public static void main(String[] args) {
        launch();
    }
}
