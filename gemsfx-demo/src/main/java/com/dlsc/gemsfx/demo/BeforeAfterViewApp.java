package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.BeforeAfterView;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.image.Image;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

public class BeforeAfterViewApp extends Application {

    @Override
    public void start(Stage stage) {
        Image beforeImage = new Image(Objects.requireNonNull(BeforeAfterViewApp.class.getResource("berlin/before1.png")).toExternalForm());
        Image afterImage = new Image(Objects.requireNonNull(BeforeAfterViewApp.class.getResource("berlin/after1.png")).toExternalForm());

        BeforeAfterView beforeAfterView = new BeforeAfterView(beforeImage, afterImage);
        beforeAfterView.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        ComboBox<Orientation> orientationComboBox = new ComboBox<>();
        orientationComboBox.getItems().addAll(Orientation.values());
        orientationComboBox.valueProperty().bindBidirectional(beforeAfterView.orientationProperty());

        VBox vBox = new VBox(40, beforeAfterView, orientationComboBox);

        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
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
