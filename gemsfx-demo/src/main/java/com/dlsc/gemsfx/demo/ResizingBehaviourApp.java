package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.util.ResizingBehaviour;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class ResizingBehaviourApp extends Application {

    @Override
    public void start(Stage stage) {
        Label content = new Label("Content");
        content.setMouseTransparent(false);
        content.setStyle("-fx-background-color: orange;");
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        content.setAlignment(Pos.CENTER);

        StackPane stackPane = new StackPane(content);
        stackPane.setStyle("-fx-background-color: blue;");
        stackPane.setPadding(new Insets(10));
        stackPane.setPrefSize(250, 250);
        stackPane.setMaxSize(950, 850);

        ResizingBehaviour resizingSupport = ResizingBehaviour.install(stackPane);
        resizingSupport.setResizable(true);
        resizingSupport.setOnResize((width, height) -> System.out.println("width: " + width + ", height: " + height));

        Group group = new Group(stackPane);

        StackPane container = new StackPane(group);
        container.setAlignment(Pos.CENTER);

        Scene scene = new Scene(container);
        CSSFX.start(scene);

        stage.setTitle("Resizable Pane");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(900);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
