package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.LoadingPane.Status;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class LoadingPaneApp extends GemApplication {

    @Override
    public void start(Stage stage) { super.start(stage);
        Label node = new Label("Some content goes here...");
        node.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        node.setStyle("-fx-background-color: white;");
        node.setAlignment(Pos.CENTER);

        LoadingPane loadingPane = new LoadingPane(node);
        loadingPane.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        loadingPane.setStyle("-fx-border-color: black;");
        loadingPane.setError("Some error message...");

        ComboBox<Status> statusBox = new ComboBox<>();
        statusBox.getItems().addAll(Status.values());
        statusBox.valueProperty().bindBidirectional(loadingPane.statusProperty());

        ComboBox<LoadingPane.Size> sizeBox = new ComboBox<>();
        sizeBox.getItems().addAll(LoadingPane.Size.values());
        sizeBox.valueProperty().bindBidirectional(loadingPane.sizeProperty());

        Button simulateLoading = new Button("Load");
        simulateLoading.setOnAction(evt -> {
            loadingPane.setStatus(Status.OK);
            Thread thread = new Thread(() -> {
                loadingPane.load();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                for (int i = 0; i <= 100; i++) {
                    final double value = i;
                    Platform.runLater(() -> {
                        loadingPane.setProgress(value / 100d);
                    });
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (Math.random() > .5) {
                    loadingPane.error("something went wrong");
                } else {
                    loadingPane.ok();
                }
            });
            thread.setDaemon(true);
            thread.start();
        });

        Button scenicViewButton = new Button("Scenic View");
        scenicViewButton.setOnAction(event -> ScenicView.show(loadingPane.getScene()));

        HBox hBox = new HBox(10, statusBox, sizeBox, simulateLoading, scenicViewButton);
        VBox vBox = new VBox(20, loadingPane, hBox);

        vBox.setPadding(new Insets(20));
        vBox.setAlignment(Pos.CENTER);

        Scene scene = new Scene(vBox);
        CSSFX.start(scene);

        stage.setTitle("Loading Pane");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
