package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AvatarView;
import com.dlsc.gemsfx.AvatarView.AvatarShape;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.scenicview.ScenicView;

import java.util.Objects;

public class AvatarViewApp extends Application {

    private AvatarView avatarView;

    private static final Image AVATAR_IMAGE = new Image(Objects.requireNonNull(AvatarViewApp.class.getResource("dirk.jpg")).toExternalForm());

 //   private static final Image AVATAR_IMAGE = new Image("https://wallpapers.com/images/featured-full/4k-oaax18kaapkokaro.jpg", true);

    private static final String INITIALS = "LD";

    @Override
    public void start(Stage primaryStage) throws Exception {

        Tab tabA = createTabA();
        Tab tabB = createTabB();

        TabPane tabPane = new TabPane(tabA, tabB);

        Scene scene = new Scene(tabPane, Color.WHITE);
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.setTitle("AvatarView");
        primaryStage.show();
    }

    private Tab createTabA() {
        avatarView = new AvatarView();

        StackPane avatarViewWrapper = new StackPane(avatarView);
        avatarViewWrapper.setStyle(" -fx-background-color: white; -fx-pref-width: 200px");

        HBox wrapper = new HBox(50, avatarViewWrapper, getControlPanel());
        wrapper.setAlignment(Pos.CENTER);
        wrapper.setStyle(" -fx-background-color: white;");
        HBox.setHgrow(avatarViewWrapper, Priority.ALWAYS);

        return new Tab("Image / Text / Blank", wrapper);
    }

    private Tab createTabB() {
        Node hBox1 = createColorBox("Singles", AvatarShape.SQUARE, false, "A", "B", "C", "D", "E");
        Node hBox2 = createColorBox("Doubles", AvatarShape.SQUARE, false, "AA", "BB", "CC", "DD", "EE");
        Node hBox3 = createColorBox("Singles (Round)", AvatarShape.ROUND, false, "A", "B", "C", "D", "E");
        Node hBox4 = createColorBox("Doubles (Round)", AvatarShape.ROUND, false, "AA", "BB", "CC", "DD", "EE");

        Node hBox1b = createColorBox("Singles (Shadow)", AvatarShape.SQUARE, true, "A", "B", "C", "D", "E");
        Node hBox2b = createColorBox("Doubles (Shadow)", AvatarShape.SQUARE, true, "AA", "BB", "CC", "DD", "EE");
        Node hBox3b = createColorBox("Singles (Round, Shadow)", AvatarShape.ROUND, true, "A", "B", "C", "D", "E");
        Node hBox4b = createColorBox("Doubles (Round, Shadow)", AvatarShape.ROUND, true, "AA", "BB", "CC", "DD", "EE");

        VBox vBox = new VBox(50, hBox1, hBox2, hBox3, hBox4, hBox1b, hBox2b, hBox3b, hBox4b);
        vBox.setAlignment(Pos.CENTER);
        vBox.setPadding(new Insets(50));
        vBox.setStyle("-fx-background-color: white;");

        ScrollPane scrollPane = new ScrollPane(vBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(520);
        return new Tab("Colors", scrollPane);
    }

    private Node createColorBox(String label, AvatarShape shape, boolean dropShadow, String... initials) {
        HBox hBox = new HBox(20);
        hBox.setAlignment(Pos.CENTER);
        hBox.setMaxHeight(Double.MAX_VALUE);
        Label l = new Label(label);
        l.setPrefWidth(200);
        hBox.getChildren().add(l);

        for (String initial : initials) {
            AvatarView view = new AvatarView(initial);
            view.setAvatarShape(shape);
            if (dropShadow) {
                view.setEffect(new DropShadow());
            }
            hBox.getChildren().add(view);
        }

        return hBox;
    }

    public Node getControlPanel() {
        // clip type
        Label shapeLabel = new Label("Shape:");
        ComboBox<AvatarShape> shapeComboBox = new ComboBox<>();
        shapeComboBox.getItems().addAll(AvatarShape.values());
        shapeComboBox.setValue(AvatarShape.SQUARE);
        shapeComboBox.setMaxWidth(Double.MAX_VALUE);
        avatarView.avatarShapeProperty().bind(shapeComboBox.valueProperty());

        // Image or Initials
        Label contentType = new Label("Content:");
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
        Label arcSizeLabel = new Label("Arc Size:");
        arcSizeLabel.managedProperty().bind(arcSizeLabel.visibleProperty());
        arcSizeLabel.visibleProperty().bind(shapeComboBox.valueProperty().isEqualTo(AvatarShape.SQUARE));

        Spinner<Integer> arcSizeSpinner = new Spinner<>(0, 100, 10, 5);
        avatarView.arcSizeProperty().bind(arcSizeSpinner.valueProperty());
        arcSizeSpinner.managedProperty().bind(arcSizeSpinner.visibleProperty());
        arcSizeSpinner.visibleProperty().bind(arcSizeLabel.visibleProperty());

        // size
        Label sizeLabel = new Label("Size:");

        Spinner<Integer> sizeSpinner = new Spinner<>(35, 200, avatarView.getSize(), 5);
        avatarView.sizeProperty().bind(sizeSpinner.valueProperty());

        VBox vBox = new VBox(20, shapeLabel, shapeComboBox, contentType, contentComboBox, initialsLabel, initialsTextField, arcSizeLabel, arcSizeSpinner, sizeLabel, sizeSpinner);
        vBox.setStyle("-fx-background-color: #e0e0e0; -fx-padding: 20px;");
        HBox.setHgrow(vBox, Priority.NEVER);
        vBox.setAlignment(Pos.CENTER_LEFT);
        return vBox;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
