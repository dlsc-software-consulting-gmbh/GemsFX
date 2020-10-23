package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhotoView;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class PhotoViewApp extends Application {

    @Override
    public void start(Stage stage) {
        PhotoView photoView = new PhotoView();

        StackPane stackPane = new StackPane(photoView);
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);
        stage.setTitle("Photo View Demo");
        stage.setScene(scene);
        stage.setWidth(250);
        stage.setHeight(250);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
