package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.Spacer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SpacerApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        VBox root = new VBox(10);
        root.setStyle("-fx-padding: 10px;-fx-alignment: top_center;");

        Spacer spacer1 = new Spacer();
        spacer1.setStyle("-fx-background-color: rgba(255,192,203,0.3);");
        HBox topBox = new HBox(new Label("Hello"), spacer1, new Label("World"));

        Spacer spacer2 = new Spacer();
        spacer2.setStyle("-fx-background-color: rgba(134,139,220,0.3);");
        VBox centerBox = new VBox(new Label("Hello"), spacer2, new Label("World"));
        centerBox.setMinHeight(280);

        Spacer spacer3 = new Spacer();
        spacer3.setStyle("-fx-background-color: rgba(0,255,127,0.3);");
        Spacer spacer4 = new Spacer();
        spacer4.setStyle("-fx-background-color: rgba(255,255,0,0.3);");
        HBox bottomBox = new HBox(new Label("Hello"), spacer3, new Label("World"), spacer4, new Label("!~"));

        CheckBox checkBox = new CheckBox("Spacer Active");
        checkBox.setSelected(true);
        spacer1.activeProperty().bind(checkBox.selectedProperty());
        spacer2.activeProperty().bind(checkBox.selectedProperty());
        spacer3.activeProperty().bind(checkBox.selectedProperty());
        spacer4.activeProperty().bind(checkBox.selectedProperty());

        root.getChildren().addAll(topBox, centerBox, bottomBox, checkBox);

        stage.setScene(new Scene(root, 380, 380));
        stage.setTitle("Spacer Demo");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
