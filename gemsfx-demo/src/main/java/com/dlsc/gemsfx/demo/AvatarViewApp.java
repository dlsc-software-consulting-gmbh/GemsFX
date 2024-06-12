package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AvatarView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AvatarViewApp extends Application {

    private AvatarView avatarView;

    // User avatar
    private static final Image AVATAR_IMAGE = new Image("https://avatars.githubusercontent.com/u/9534301?v=4", true);
    // Username initials
    private static final String INITIALS = "DL";

    @Override
    public void start(Stage primaryStage) throws Exception {

        //If no avatar image is provided or the image has not finished loading,
        //the initials of the name will be displayed
        avatarView = new AvatarView();
        // avatarView.setImage(avatarImage);
        // avatarView.setInitials(initials);

        StackPane avatarViewWrapper = new StackPane(avatarView);
        avatarViewWrapper.setStyle(" -fx-background-color: white;-fx-pref-width: 200px");

        HBox wrapper = new HBox(50, avatarViewWrapper, getControlPanel());
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle(" -fx-background-color: white;");
        HBox.setHgrow(wrapper, Priority.ALWAYS);

        Scene scene = new Scene(wrapper, 400, 380, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("Hello AvatarView");
        primaryStage.show();
    }

    public Node getControlPanel() {
        // clip type
        Label clipTypeLabel = new Label("Clip Type:");

        ComboBox<AvatarView.ClipType> clipTypeComboBox = new ComboBox<>();
        clipTypeComboBox.getItems().addAll(AvatarView.ClipType.values());
        clipTypeComboBox.setValue(AvatarView.ClipType.SQUARE);
        avatarView.clipTypeProperty().bind(clipTypeComboBox.valueProperty());
        clipTypeComboBox.setMaxWidth(Double.MAX_VALUE);

        // Image or Initials
        Label contentType = new Label("Content Type:");
        ComboBox<String> contentComboBox = new ComboBox<>();
        contentComboBox.getItems().addAll("Image", "Initials");
        contentComboBox.setValue("Image");
        contentComboBox.setMaxWidth(Double.MAX_VALUE);
        contentComboBox.valueProperty().subscribe(it -> {
            if (it.equals("Image")) {
                avatarView.setImage(AVATAR_IMAGE);
            } else {
                avatarView.setImage(null);
                avatarView.setInitials(INITIALS);
            }
        });

        // round size
        Label roundSizeLabel = new Label("Round Size:");
        roundSizeLabel.managedProperty().bind(roundSizeLabel.visibleProperty());
        roundSizeLabel.visibleProperty().bind(clipTypeComboBox.valueProperty().isEqualTo(AvatarView.ClipType.SQUARE));

        Spinner<Integer> roundSizeSpinner = new Spinner<>(10, 100, 10, 5);
        avatarView.roundSizeProperty().bind(roundSizeSpinner.valueProperty());
        roundSizeSpinner.managedProperty().bind(roundSizeSpinner.visibleProperty());
        roundSizeSpinner.visibleProperty().bind(roundSizeLabel.visibleProperty());

        // size
        Label sizeLabel = new Label("Size:");

        Spinner<Integer> sizeSpinner = new Spinner<>(35, 100, 50, 5);
        avatarView.sizeProperty().bind(sizeSpinner.valueProperty());

        VBox vBox = new VBox(20, clipTypeLabel, clipTypeComboBox, contentType, contentComboBox, roundSizeLabel, roundSizeSpinner, sizeLabel, sizeSpinner);
        vBox.setStyle("-fx-background-color: #e0e0e0;-fx-padding: 20px;");
        HBox.setHgrow(vBox, Priority.ALWAYS);
        vBox.setAlignment(Pos.CENTER_LEFT);
        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }

}
