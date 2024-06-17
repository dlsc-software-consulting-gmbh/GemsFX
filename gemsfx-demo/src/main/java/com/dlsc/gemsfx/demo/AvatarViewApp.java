package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AvatarView;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class AvatarViewApp extends Application {

    private AvatarView avatarView;

    private static final Image AVATAR_IMAGE = new Image("https://avatars.githubusercontent.com/u/9534301?v=4", true);
    private static final String INITIALS = "LD";

    @Override
    public void start(Stage primaryStage) throws Exception {

        avatarView = new AvatarView();

        StackPane avatarViewWrapper = new StackPane(avatarView);
        avatarViewWrapper.setStyle(" -fx-background-color: white; -fx-pref-width: 200px");

        HBox wrapper = new HBox(50, avatarViewWrapper, getControlPanel());
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle(" -fx-background-color: white;");
        HBox.setHgrow(avatarViewWrapper, Priority.ALWAYS);

        Scene scene = new Scene(wrapper, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("AvatarView");
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
        contentComboBox.getItems().addAll("Image", "Initials", "Blank");
        contentComboBox.setValue("Image");
        contentComboBox.setMaxWidth(Double.MAX_VALUE);
        contentComboBox.valueProperty().subscribe(it -> {
            if (it.equals("Image")) {
                avatarView.setImage(AVATAR_IMAGE);
                avatarView.setInitials(null);
            } else if (it.equals("Initials")){
                avatarView.setImage(null);
                avatarView.setInitials(INITIALS);
            } else {
                avatarView.setImage(null);
                avatarView.setInitials(null);
            }
        });

        // initials
        Label initialsLabel = new Label("Initials");
        TextField initialsTextField = new TextField();
        avatarView.initialsProperty().bindBidirectional(initialsTextField.textProperty());

        // round size
        Label roundSizeLabel = new Label("Round Size:");
        roundSizeLabel.managedProperty().bind(roundSizeLabel.visibleProperty());
        roundSizeLabel.visibleProperty().bind(clipTypeComboBox.valueProperty().isEqualTo(AvatarView.ClipType.SQUARE));

        Spinner<Integer> roundSizeSpinner = new Spinner<>(0, 100, 10, 5);
        avatarView.roundSizeProperty().bind(roundSizeSpinner.valueProperty());
        roundSizeSpinner.managedProperty().bind(roundSizeSpinner.visibleProperty());
        roundSizeSpinner.visibleProperty().bind(roundSizeLabel.visibleProperty());

        // size
        Label sizeLabel = new Label("Size:");

        Spinner<Integer> sizeSpinner = new Spinner<>(35, 200, avatarView.getSize(), 5);
        avatarView.sizeProperty().bind(sizeSpinner.valueProperty());

        VBox vBox = new VBox(20, clipTypeLabel, clipTypeComboBox, contentType, contentComboBox, initialsLabel, initialsTextField, roundSizeLabel, roundSizeSpinner, sizeLabel, sizeSpinner);
        vBox.setStyle("-fx-background-color: #e0e0e0;-fx-padding: 20px;");
        HBox.setHgrow(vBox, Priority.NEVER);
        vBox.setAlignment(Pos.CENTER_LEFT);
        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
