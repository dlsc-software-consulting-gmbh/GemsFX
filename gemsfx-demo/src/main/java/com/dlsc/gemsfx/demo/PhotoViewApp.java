package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PhotoViewApp extends Application {

    @Override
    public void start(Stage stage) {
        PhotoView photoView = new PhotoView();
        VBox.setVgrow(photoView, Priority.ALWAYS);

        ComboBox<ClipShape> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ClipShape.values());
        comboBox.valueProperty().bindBidirectional(photoView.clipShapeProperty());

        CheckBox editableBox = new CheckBox("Editable");
        editableBox.selectedProperty().bindBidirectional(photoView.editableProperty());

        HBox hBox = new HBox(20, comboBox, editableBox);
        hBox.setAlignment(Pos.CENTER);

        Button useCroppedImage = new Button("Use Cropped Image");
        useCroppedImage.setOnAction(evt -> photoView.setPhoto(photoView.getCroppedImage()));
        useCroppedImage.setMaxWidth(Double.MAX_VALUE);

        VBox leftSide = new VBox(40, photoView, hBox, useCroppedImage);
        leftSide.setAlignment(Pos.TOP_CENTER);

        ImageView originalImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        originalImageView.imageProperty().bind(photoView.photoProperty());
        originalImageView.setFitWidth(100);
        originalImageView.setFitHeight(100);
        VBox.setVgrow(originalImageView, Priority.ALWAYS);

        ImageView croppedImageView = new ImageView();
        croppedImageView.setPreserveRatio(true);
        croppedImageView.imageProperty().bind(photoView.croppedImageProperty());
        croppedImageView.setFitWidth(100);
        croppedImageView.setFitHeight(100);
        VBox.setVgrow(croppedImageView, Priority.ALWAYS);

        Label originalLabel = new Label("Original");
        Label croppedLabel = new Label("Cropped");

        VBox.setMargin(croppedLabel, new Insets(20, 0, 0, 0));
        VBox rightSide = new VBox(10, originalLabel, originalImageView, croppedLabel, croppedImageView);

        HBox mainBox = new HBox(20, leftSide, rightSide);
        mainBox.setFillHeight(false);

        mainBox.setAlignment(Pos.TOP_CENTER);

        StackPane stackPane = new StackPane(mainBox);
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);
        stage.setTitle("Photo View Demo");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
