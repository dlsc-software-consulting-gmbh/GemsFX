package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.StretchingTilePane;
import com.dlsc.gemsfx.util.StageManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class StretchingTilePaneApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);
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
        StageManager.install(stage, "stretching.tile.pane.app");

        stage.show();
    }
        @Override
    public String getDescription() {
        return """
                ### StretchingTilePane
                
                A specialized pane that can be used to display a list of tiles (nodes) in one or more rows. The pane first calculates
                how many tiles fit next to each other in a row and then arranges them in a grid. The tiles are stretched to fill
                the entire width of each row. All tiles have the same height and width.
                
                Note: the main difference to the standard JavaFX TilePane is that the tiles are stretched to fill the entire width of
                each row.
                """;
    }

}
