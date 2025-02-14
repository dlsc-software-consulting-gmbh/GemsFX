package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ThreeItemsPane;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

public class ThreeItemsPaneApp extends Application {

    private final ThreeItemsPane pane = new ThreeItemsPane();

    @Override
    public void start(Stage primaryStage) throws Exception {
        pane.setStyle("-fx-background-color: white; -fx-padding: 5px; -fx-border-color: black;");
        pane.setMaxHeight(Region.USE_PREF_SIZE);
        pane.setSpacing(20);

        Label item1 = new Label("Senapt CRM Desktop UI");
        item1.setStyle("-fx-border-color: red;");
        pane.setItem1(item1);

        Label item2 = new Label("Center");
        item2.setStyle("-fx-border-color: red;");
        pane.setItem2(item2);

        Label item3 = new Label("User Avatar");
        item3.setStyle("-fx-border-color: red;");
        pane.setItem3(item3);

        StackPane stackPane = new StackPane(pane);
        stackPane.setPadding(new Insets(20));
        stackPane.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        HBox.setHgrow(stackPane, Priority.ALWAYS);

        HBox box = new HBox(10, stackPane, getControlPanel());

        Scene scene = new Scene(box);
        primaryStage.setScene(scene);
        primaryStage.setTitle("ThreeItemsPane");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(800);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public Node getControlPanel() {
        ComboBox<Orientation> box = new ComboBox<>();
        box.getItems().setAll(Orientation.values());
        box.valueProperty().bindBidirectional(pane.orientationProperty());

        Button scenicView = new Button("ScenicView");
        scenicView.setOnAction(evt -> ScenicView.show(scenicView.getScene()));

        return new VBox(10, box, scenicView);
    }
}
