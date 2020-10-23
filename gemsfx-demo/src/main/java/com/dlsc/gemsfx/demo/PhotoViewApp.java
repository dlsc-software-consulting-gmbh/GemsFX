package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PhotoViewApp extends Application {

    @Override
    public void start(Stage stage) {
        PhotoView photoView = new PhotoView();
        VBox vBox = new VBox(40, photoView);
        VBox.setVgrow(photoView, Priority.ALWAYS);

        ComboBox<ClipShape> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ClipShape.values());
        comboBox.valueProperty().bindBidirectional(photoView.clipShapeProperty());

        CheckBox editableBox = new CheckBox("Editable");
        editableBox.selectedProperty().bindBidirectional(photoView.editableProperty());

        HBox hBox = new HBox(20, comboBox, editableBox);
        hBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(hBox);

        StackPane stackPane = new StackPane(vBox);
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);
        stage.setTitle("Photo View Demo");
        stage.setScene(scene);
        stage.setWidth(250);
        stage.setHeight(350);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
