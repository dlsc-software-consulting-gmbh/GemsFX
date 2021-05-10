package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.BeforeAfterView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BeforeAfterViewApp extends Application {

    @Override
    public void start(Stage stage) {
        BeforeAfterView beforeAfterView = new BeforeAfterView();
        beforeAfterView.setBefore(new ImageView(BeforeAfterViewApp.class.getResource("berlin/before1.png").toExternalForm()));
        beforeAfterView.setAfter(new ImageView(BeforeAfterViewApp.class.getResource("berlin/after1.png").toExternalForm()));

        Slider slider = new Slider(0,1,0);
        slider.valueProperty().bindBidirectional(beforeAfterView.dividerPositionProperty());

        VBox box = new VBox(20, beforeAfterView, slider);

        Scene scene = new Scene(box);
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
