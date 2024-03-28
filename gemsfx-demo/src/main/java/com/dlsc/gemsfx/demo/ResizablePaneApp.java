package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ResizablePane;
import com.dlsc.gemsfx.YearView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ResizablePaneApp extends Application {

    @Override
    public void start(Stage stage) {
        Label content = new Label("Content");
        content.setMouseTransparent(false);
        content.setStyle("-fx-background-color: orange;");
        content.setPrefSize(250, 250);
        content.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        content.setAlignment(Pos.CENTER);

        ResizablePane view = new ResizablePane(content);
        view.setStyle("-fx-background-color: blue;");
        view.setPadding(new Insets(10));

        StackPane stackPane = new StackPane(view);
        stackPane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(stackPane);
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
