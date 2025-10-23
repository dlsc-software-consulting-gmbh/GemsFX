package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.StretchingTilePane;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StretchingTilePaneApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        StretchingTilePane pane = new StretchingTilePane(10, 10);
        pane.setPadding(new Insets(20));
        pane.setPrefSize(800, 600);
        for (int i = 0; i < 12; i++) {
            Label label = new Label("Tile " + (i + 1));
            label.setAlignment(Pos.CENTER);
            label.setPrefSize(150, 100);
            label.setMaxWidth(Double.MAX_VALUE);
            label.setStyle("-fx-border-color: black;");
            pane.getChildren().add(label);
        }

        Scene scene = new Scene(pane);
        stage.setTitle("Stretching Tile Pane");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    }
}
