package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.MaskedView;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MaskedViewApp extends GemApplication {

    private final DoubleProperty translateX = new SimpleDoubleProperty();

    @Override
    public void start(Stage stage) {
        super.start(stage);

        HBox content = new HBox(10);
        content.setAlignment(Pos.CENTER_LEFT);
        content.setPadding(new Insets(10));

        // the content has to keep its preferred width, otherwise the stack pane inside the
        // masked view would shrink it down to the width of the view and nothing would ever
        // be clipped
        content.setMinWidth(Region.USE_PREF_SIZE);
        content.setMaxWidth(Region.USE_PREF_SIZE);
        StackPane.setAlignment(content, Pos.CENTER_LEFT);

        for (int i = 1; i <= 30; i++) {
            Label label = new Label("Item " + i);
            label.setStyle("-fx-padding: 10px 20px; -fx-background-color: -fx-accent; -fx-text-fill: white; -fx-background-radius: 4px;");
            content.getChildren().add(label);
        }

        MaskedView maskedView = new MaskedView(content);
        maskedView.setMinHeight(Region.USE_PREF_SIZE);

        content.translateXProperty().bind(translateX);

        // scrolling with the mouse wheel or the trackpad
        maskedView.setOnScroll(evt -> scroll(maskedView, content, evt.getDeltaY() + evt.getDeltaX()));

        Button leftButton = new Button("<");
        leftButton.setOnAction(evt -> scroll(maskedView, content, 100));
        leftButton.disableProperty().bind(translateX.greaterThanOrEqualTo(0));

        Button rightButton = new Button(">");
        rightButton.setOnAction(evt -> scroll(maskedView, content, -100));
        rightButton.disableProperty().bind(Bindings.createBooleanBinding(
                () -> translateX.get() + content.getWidth() <= maskedView.getWidth(),
                translateX, content.widthProperty(), maskedView.widthProperty()));

        Slider fadingSizeSlider = new Slider(0, 300, maskedView.getFadingSize());
        fadingSizeSlider.valueProperty().bindBidirectional(maskedView.fadingSizeProperty());

        Label fadingSizeLabel = new Label();
        fadingSizeLabel.textProperty().bind(maskedView.fadingSizeProperty().asString("%.0f px"));

        HBox controls = new HBox(10, leftButton, rightButton, new Label("Fading size:"), fadingSizeSlider, fadingSizeLabel);
        controls.setAlignment(Pos.CENTER_LEFT);

        Label descriptionLabel = new Label("""
                The masked view clips its content on the left and on the right hand side so that \
                items moving out of the view fade out instead of being cut off. Scroll the content \
                with the mouse wheel or with the buttons below.""");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMinHeight(Region.USE_PREF_SIZE);

        VBox root = new VBox(20, descriptionLabel, maskedView, controls);
        root.setPadding(new Insets(20));

        stage.setTitle("Masked View");
        stage.setScene(new Scene(root, 800, 300));
        stage.show();
    }

    /**
     * Moves the content of the masked view by the given delta, making sure that it never gets
     * scrolled beyond its first or its last item.
     */
    private void scroll(MaskedView maskedView, Region content, double delta) {
        double minTranslate = Math.min(0, maskedView.getWidth() - content.getWidth());
        translateX.set(Math.max(minTranslate, Math.min(0, translateX.get() + delta)));
    }

    public static void main(String[] args) {
        launch(args);
    }
}
