package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class PhotoViewSkin extends SkinBase<PhotoView> {

    private final Slider slider;
    private final ImageView imageView;
    private final StackPane imageWrapper;

    private double startY;
    private double startX;

    public PhotoViewSkin(PhotoView view) {
        super(view);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.imageProperty().bind(view.photoProperty());
        imageView.scaleXProperty().bind(view.photoZoomProperty());
        imageView.scaleYProperty().bind(view.photoZoomProperty());
        imageView.translateXProperty().bind(view.photoTranslateXProperty());
        imageView.translateYProperty().bind(view.photoTranslateYProperty());
        imageView.setCursor(Cursor.MOVE);

        slider = new Slider(1,4,1);
        slider.setPrefWidth(200);
        slider.setMaxWidth(Region.USE_PREF_SIZE);
        slider.valueProperty().bindBidirectional(view.photoZoomProperty());
        StackPane.setAlignment(slider, Pos.BOTTOM_CENTER);

        imageWrapper = new StackPane(imageView);
        imageWrapper.getStyleClass().add("image-wrapper");

        NumberBinding min = Bindings.min(imageWrapper.widthProperty(), imageWrapper.heightProperty());

        imageView.fitWidthProperty().bind(min);
        imageView.fitHeightProperty().bind(min);

        imageWrapper.setOnMousePressed(evt -> {
            startX = evt.getX();
            startY = evt.getY();
        });

        imageWrapper.setOnMouseDragged(evt -> {
            double deltaX = startX - evt.getX();
            double deltaY = startY - evt.getY();

            view.setPhotoTranslateX(view.getPhotoTranslateX() - deltaX);
            view.setPhotoTranslateY(view.getPhotoTranslateY() - deltaY);

            startX = evt.getX();
            startY = evt.getY();
        });

        Circle circle = new Circle();
        circle.getStyleClass().add("border-circle");
        circle.setManaged(false);
        circle.radiusProperty().bind(Bindings.min(imageWrapper.widthProperty().divide(2),imageWrapper.heightProperty().divide(2)));
        circle.centerXProperty().bind(imageWrapper.widthProperty().divide(2));
        circle.centerYProperty().bind(imageWrapper.heightProperty().divide(2));
        circle.setEffect(new DropShadow());
        imageWrapper.getChildren().add(circle);

        VBox.setVgrow(imageWrapper, Priority.ALWAYS);
        VBox controlsWrapper = new VBox(imageWrapper, slider);
        controlsWrapper.getStyleClass().add("controls-wrapper");
        controlsWrapper.setAlignment(Pos.BOTTOM_CENTER);

        getChildren().setAll(controlsWrapper);

        view.clipShapeProperty().addListener(it -> updateClip());
        updateClip();
    }

    private void updateClip() {
        if (getSkinnable().getClipShape().equals(ClipShape.CIRCLE)) {
            updateCircleClip();
        } else {
            updateRectangleClip();
        }
    }

    private void updateRectangleClip() {
    }

    private void updateCircleClip() {
        Circle circle = new Circle();
        circle.radiusProperty().bind(Bindings.min(imageWrapper.widthProperty().divide(2),imageWrapper.heightProperty().divide(2)));
        circle.centerXProperty().bind(imageWrapper.widthProperty().divide(2));
        circle.centerYProperty().bind(imageWrapper.heightProperty().divide(2));
        circle.setEffect(new InnerShadow());

        circle.centerXProperty().addListener(it -> System.out.println("cx: " + circle.getCenterX()));
        imageWrapper.setClip(circle);
    }
}
