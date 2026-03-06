package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import com.dlsc.gemsfx.util.StageManager;
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

public class PhotoViewApp extends GemApplication {

    @Override
    public void start(Stage stage) { super.start(stage);
        PhotoView photoView = new PhotoView();

        StackPane photoViewWrapper = new StackPane(photoView);
        photoViewWrapper.setStyle("-fx-padding: 20px; -fx-background-color: white; -fx-border-color: grey;");

        VBox.setVgrow(photoViewWrapper, Priority.ALWAYS);

        ComboBox<ClipShape> comboBox = new ComboBox<>();
        comboBox.getItems().setAll(ClipShape.values());
        comboBox.valueProperty().bindBidirectional(photoView.clipShapeProperty());

        CheckBox editableBox = new CheckBox("Editable");
        editableBox.selectedProperty().bindBidirectional(photoView.editableProperty());

        HBox hBox = new HBox(20, comboBox, editableBox);
        hBox.setAlignment(Pos.CENTER);

        CheckBox createCroppedImage = new CheckBox("Create cropped image");
        createCroppedImage.disableProperty().bind(photoView.photoProperty().isNull());
        createCroppedImage.selectedProperty().bindBidirectional(photoView.createCroppedImageProperty());

        Button useCroppedImage = new Button("Use Cropped Image");
        useCroppedImage.disableProperty().bind(createCroppedImage.selectedProperty().not().or(photoView.photoProperty().isNull()));
        useCroppedImage.setOnAction(evt -> photoView.setPhoto(photoView.getCroppedImage()));
        useCroppedImage.setMaxWidth(Double.MAX_VALUE);

        VBox leftSide = new VBox(40, photoViewWrapper, hBox, createCroppedImage, useCroppedImage);
        leftSide.setAlignment(Pos.TOP_CENTER);

        ImageView originalImageView = new ImageView();
        originalImageView.setPreserveRatio(true);
        originalImageView.imageProperty().bind(photoView.photoProperty());
        originalImageView.setFitWidth(100);
        originalImageView.setFitHeight(100);
        StackPane originalImageWrapper = new StackPane(originalImageView);
        originalImageWrapper.setPrefSize(100, 100);
        VBox.setVgrow(originalImageWrapper, Priority.ALWAYS);

        ImageView croppedImageView = new ImageView();
        croppedImageView.setPreserveRatio(true);
        croppedImageView.imageProperty().bind(photoView.croppedImageProperty());
        croppedImageView.setFitWidth(100);
        croppedImageView.setFitHeight(100);

        StackPane croppedImageWrapper = new StackPane(croppedImageView);
        croppedImageWrapper.setPrefSize(100, 100);
        VBox.setVgrow(croppedImageWrapper, Priority.ALWAYS);

        Label originalLabel = new Label("Original");
        Label croppedLabel = new Label("Cropped");

        VBox.setMargin(croppedLabel, new Insets(20, 0, 0, 0));
        VBox rightSide = new VBox(10, originalLabel, originalImageWrapper, croppedLabel, croppedImageWrapper);

        HBox mainBox = new HBox(20, leftSide, rightSide);
        mainBox.setFillHeight(false);

        mainBox.setAlignment(Pos.TOP_CENTER);

        StackPane stackPane = new StackPane(mainBox);
        stackPane.setPadding(new Insets(20));

        Scene scene = new Scene(stackPane);

        stage.setTitle("Photo View Demo");
        stage.setScene(scene);
        stage.sizeToScene();
        StageManager.install(stage, "photo.view.app");

        stage.show();
    }

        @Override
    public String getDescription() {
        return """
                ### PhotoView
                
                The photo view is mostly used to display a user profile picture.
                Features
                
                    - control can be used as read-only view or as an editor (see `editableProperty()`)
                    - picture can moved around by dragging it
                    - picture can be resized by pinch zoom (touch) or via scroll wheel
                    - control provides a cropped "read only" version of the original image (see @{`croppedImageProperty()`}). This is ideal
                    for saving memory when saving the image to the server / a database
                    - applications can set a custom "photo supplier" to replace the built-in file chooser (see `photoSupplierProperty()`)
                    - drag and drop an image file onto the view
                    - circular and rectangle shape (see `setClipShape(ClipShape)`)
                    - customizable maximum zoom value
                    - keyboard support: backspace and delete keys delete the picture, space or enter trigger the file supplier (default: show the file chooser)
                    - pseudo class support: "empty" if the `photoProperty()` is null
                    - an effect can be applied directly to the image (see `photoEffectProperty()`)
                
                **Note: the values for the zoom and translate properties will all be reset when a new photo is set.**
                """;
    }

    public static void main(String[] args) {
        launch();
    }
}
