package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.YearView;
import com.dlsc.gemsfx.util.StageManager;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class YearViewApp extends GemApplication {

    @Override
    public void start(Stage stage) { super.start(stage);
        YearView view = new YearView();
        view.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        VBox vBox = new VBox(view);

        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        CSSFX.start(scene);

        stage.setTitle("YearView");
        stage.setScene(scene);
        stage.sizeToScene();
        StageManager.install(stage, "year.view.app");

        stage.show();
    }

        @Override
    public String getDescription() {
        return """
                ### YearView
                
                A view for selecting a year.
                """;
    }

    public static void main(String[] args) {
        launch();
    }
}
