package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.keyboard.KeyboardView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class KeyboardApp extends Application {

    @Override
    public void start(Stage stage) {
        KeyboardView view =  new KeyboardView();
        view.setPrefSize(1000, 340);
        Scene scene = new Scene(view);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.setTitle("JavaFX On-Screen Keyboard");
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
