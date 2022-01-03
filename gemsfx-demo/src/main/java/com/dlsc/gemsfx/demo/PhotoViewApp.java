package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import com.jpro.webapi.JProApplication;
import com.jpro.webapi.WebAPI.FileUploader;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.net.MalformedURLException;

public class PhotoViewApp extends JProApplication {

    private FileUploader fileHandler;

    private ProgressBar progressBar;

    @Override
    public void start(Stage stage) {
        PhotoView photoView = new PhotoView();

        StackPane photoViewWrapper = new StackPane(photoView);
        photoViewWrapper.setStyle("-fx-padding: 20px; -fx-background-color: white; -fx-border-color: gray;");

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
        stage.centerOnScreen();

        if (getWebAPI().isBrowser()) {


            /*
             * Replace the placeholder as the default placeholder also says that the
             * user can "click" to add a new photo. In JPRO we can't support the click
             * as otherwise the dragging of the photo won't work and instead the file
             * chooser will keep showing up.
             */
            FontIcon fontIcon = new FontIcon(MaterialDesign.MDI_UPLOAD);
            fontIcon.getStyleClass().add("upload-icon");

            Label placeholder = new Label("DROP IMAGE\nFILE HERE");
            placeholder.setTextAlignment(TextAlignment.CENTER);
            placeholder.setGraphic(fontIcon);
            placeholder.setContentDisplay(ContentDisplay.TOP);
            placeholder.getStyleClass().add("placeholder");
            photoView.setPlaceholder(placeholder);

            progressBar = new ProgressBar();
            progressBar.setVisible(false);
            progressBar.setMaxWidth(Double.MAX_VALUE);

            photoView.setPhotoSupplier(() -> null);
            leftSide.getChildren().add(progressBar);

            fileHandler = getWebAPI().makeFileUploadNode(photoView);

            fileHandler.setSelectFileOnClick(false);
            fileHandler.setSelectFileOnDrop(true);
            fileHandler.fileDragOverProperty().addListener((o, oldV, newV) -> {
                if (newV) {
                    photoView.getStyleClass().add("file-drag");
                } else {
                    photoView.getStyleClass().remove("file-drag");
                }
            });


            fileHandler.setOnFileSelected((file) -> {
                updateText();
                fileHandler.uploadFile();
            });

            fileHandler.progressProperty().addListener((obs, oldV, newV) -> {
                updateText();
            });

            fileHandler.uploadedFileProperty().addListener(it -> {
                File uploadedFile = fileHandler.getUploadedFile();
                if (uploadedFile != null) {
                    try {
                        photoView.setPhoto(new Image(uploadedFile.toURI().toURL().toExternalForm()));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
            });

            progressBar.progressProperty().bind(fileHandler.progressProperty());
        }

        stage.show();

        Platform.runLater(() -> {
            try {
                photoView.setPhoto(new Image((new File("/Users/lemmi/Desktop/test.png").toURI().toURL().toExternalForm())));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }

    private void updateText() {
        int percentages = (int) (fileHandler.getProgress() * 100);
        progressBar.setVisible(percentages < 100);
    }

    public static void main(String[] args) {
        launch();
    }
}
