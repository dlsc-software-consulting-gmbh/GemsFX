package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.BeforeAfterView;
import com.dlsc.gemsfx.demo.fake.SettingsPane;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.Objects;

public class BeforeAfterViewApp2 extends Application {

    @Override
    public void start(Stage stage) {
        SettingsPane pane1 = new SettingsPane();
        pane1.setPrefWidth(400);
        pane1.setPadding(new Insets(20));

        SettingsPane pane2 = new SettingsPane();
        pane2.setPrefWidth(400);
        pane2.setPadding(new Insets(20));
        pane2.getStyleClass().add("styled-settings-pane");

        pane1.getStylesheets().add(Objects.requireNonNull(SettingsPane.class.getResource("styles.css")).toExternalForm());
        pane2.getStylesheets().add(Objects.requireNonNull(SettingsPane.class.getResource("styles.css")).toExternalForm());

        BeforeAfterView beforeAfterView = new BeforeAfterView(pane1, pane2);

        beforeAfterView.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        beforeAfterView.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane stackPane = new StackPane(beforeAfterView);
        stackPane.setPadding(new Insets(20));
        stackPane.setAlignment(Pos.CENTER);

        Scene scene = new Scene(stackPane);
        CSSFX.start();

        stage.setTitle("BeforeAfterView");
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
