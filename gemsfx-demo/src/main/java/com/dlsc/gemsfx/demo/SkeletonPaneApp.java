package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.AvatarView;
import com.dlsc.gemsfx.Skeleton;
import com.dlsc.gemsfx.Skeleton.Variant;
import com.dlsc.gemsfx.SkeletonPane;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Objects;

/**
 * Demonstrates {@link SkeletonPane} with composed {@link Skeleton} placeholders.
 */
public class SkeletonPaneApp extends GemApplication {

    @Override
    public void start(Stage stage) {
        super.start(stage);

        VBox root = new VBox(createProfileCardDemo());
        root.setPadding(new Insets(32.0));
        root.setAlignment(Pos.CENTER);
        root.getStyleClass().add("demo-content");

        Scene scene = new Scene(root, 620.0, 320.0);
        scene.getStylesheets().add(Objects.requireNonNull(SkeletonPaneApp.class.getResource("skeleton-pane-app.css")).toExternalForm());
        CSSFX.start(scene);

        stage.setScene(scene);
        stage.setTitle("SkeletonPane");
        stage.show();
    }

    private Node createProfileCardDemo() {
        SkeletonPane pane = new SkeletonPane(createProfileSkeleton(), createProfileContent(), true);
        pane.getStyleClass().add("social-card");
        pane.setPrefWidth(520.0);

        Button toggleButton = new Button("Toggle loading");
        toggleButton.setOnAction(event -> pane.setLoading(!pane.isLoading()));

        Button devToolsButton = configureDevToolsButton(new Button());
        devToolsButton.setMaxWidth(Double.MAX_VALUE);

        HBox controls = new HBox(8.0, toggleButton, devToolsButton);
        controls.setAlignment(Pos.CENTER_LEFT);
        return new VBox(12.0, pane, controls);
    }

    private Node createProfileSkeleton() {
        Skeleton avatar = new Skeleton(Variant.CIRCULAR);
        avatar.setPrefSize(48.0, 48.0);
        avatar.setMaxSize(48.0, 48.0);

        Skeleton title = new Skeleton(Variant.ROUNDED_RECTANGLE);
        title.setPrefSize(132.0, 14.0);
        title.setMaxWidth(132.0);

        Skeleton paragraph = new Skeleton(Variant.TEXT);
        paragraph.setLineCount(2);
        paragraph.setLineHeight(10.0);
        paragraph.setLineSpacing(6.0);
        paragraph.setLastLineFillPercent(68.0);

        VBox textColumn = new VBox(8.0, title, paragraph);
        HBox.setHgrow(textColumn, Priority.ALWAYS);

        HBox row = new HBox(14.0, avatar, textColumn);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("card-body");
        return row;
    }

    private Node createProfileContent() {
        AvatarView avatar = createTextAvatar("DL", "real-avatar", 48.0);
        avatar.setAvatarShape(AvatarView.AvatarShape.ROUND);

        Label name = new Label("Dirk Lemmermann");
        name.getStyleClass().add("real-name");

        Label body = new Label("Building JavaFX controls and tools for polished desktop applications.");
        body.getStyleClass().add("real-body");
        body.setWrapText(true);

        VBox textColumn = new VBox(6.0, name, body);
        HBox.setHgrow(textColumn, Priority.ALWAYS);

        HBox row = new HBox(14.0, avatar, textColumn);
        row.setAlignment(Pos.TOP_LEFT);
        row.getStyleClass().add("card-body");
        return row;
    }

    private AvatarView createTextAvatar(String text, String styleClass, double size) {
        AvatarView avatar = new AvatarView(text);
        avatar.getStyleClass().add(styleClass);
        avatar.setSize(size);
        return avatar;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
