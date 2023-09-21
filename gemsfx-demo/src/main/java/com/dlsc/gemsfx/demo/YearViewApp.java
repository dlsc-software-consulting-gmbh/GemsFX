package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.YearView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class YearViewApp extends Application {

    @Override
    public void start(Stage stage) {
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
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
