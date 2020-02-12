package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DrawerStackPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class DrawerApp extends Application {

    @Override
    public void start(Stage stage) {
        DrawerStackPane drawerStackPane = new DrawerStackPane();

        WebView webView = new WebView();
        webView.getEngine().load("https://openjfx.io");
        drawerStackPane.setDrawerContent(webView);

        Button showButton = new Button("Show Drawer");
        drawerStackPane.getChildren().add(showButton);
        showButton.setOnAction(evt -> drawerStackPane.setShowDrawer(true));
        Scene scene = new Scene(drawerStackPane);
        stage.setTitle("Drawer Demo");
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
