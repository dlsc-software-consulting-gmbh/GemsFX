package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SearchTextField;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SearchTextFieldApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        SearchTextField field1 = new SearchTextField();
        SearchTextField field2 = new SearchTextField(true);

        VBox vbox = new VBox(20, new Label("Standard"), field1, new Label("Round"), field2);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);
        primaryStage.setTitle("Search Text Field");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
