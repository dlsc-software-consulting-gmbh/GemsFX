package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Slider;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.util.function.Supplier;

public class PhotoViewSkin extends SkinBase<PhotoView> {

    private static final double MIN_ZOOM = 1;
    private static final double MAX_ZOOM = 10;

    private final Slider slider;
    private final ImageView imageView;
    private final StackPane imageBox;

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
        imageView.setManaged(false);

        slider = new Slider(MIN_ZOOM, MAX_ZOOM, 1);
        slider.setPrefWidth(200);
        slider.setMaxWidth(Region.USE_PREF_SIZE);
        slider.valueProperty().bindBidirectional(view.photoZoomProperty());
        slider.visibleProperty().bind(view.editableProperty());
        slider.managedProperty().bind(view.editableProperty());
        StackPane.setAlignment(slider, Pos.BOTTOM_CENTER);

        imageBox = new StackPane(imageView);
        imageBox.getStyleClass().add("image-box");

        imageBox.setOnMousePressed(evt -> {
            startX = evt.getX();
            startY = evt.getY();
        });

        imageBox.setOnMouseDragged(evt -> {
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
        circle.radiusProperty().bind(Bindings.min(imageBox.widthProperty().divide(2), imageBox.heightProperty().divide(2)));
        circle.centerXProperty().bind(imageBox.widthProperty().divide(2));
        circle.centerYProperty().bind(imageBox.heightProperty().divide(2));
        circle.setEffect(new DropShadow());
        imageBox.getChildren().add(circle);

        VBox.setVgrow(imageBox, Priority.ALWAYS);
        VBox controlsWrapper = new VBox(imageBox, slider);
        controlsWrapper.getStyleClass().add("box");
        controlsWrapper.setAlignment(Pos.BOTTOM_CENTER);

        controlsWrapper.setOnMouseClicked(evt -> {
            if (view.getPhoto() == null) {
                Supplier<Image> imageSupplier = view.getPhotoSupplier();
                if (imageSupplier != null) {
                    view.setPhoto(imageSupplier.get());
                }
            }
        });

        controlsWrapper.setOnScroll(evt -> view.setPhotoZoom(Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, view.getPhotoZoom() + Math.signum(evt.getDeltaY()) * .1))));
        controlsWrapper.setOnZoom(evt -> view.setPhotoZoom(Math.min(MAX_ZOOM, Math.max(MIN_ZOOM, view.getPhotoZoom() * evt.getZoomFactor()))));

        getChildren().setAll(controlsWrapper);

        view.clipShapeProperty().addListener(it -> updateClip());
        updateClip();

        view.placeholderProperty().addListener((obs, oldPlaceholder, newPlaceholder) -> updatePlaceholder(oldPlaceholder, newPlaceholder));
        updatePlaceholder(null, view.getPlaceholder());
    }

    private void updatePlaceholder(Node oldPlaceholder, Node newPlaceholder) {
        PhotoView view = getSkinnable();

        if (oldPlaceholder != null) {
            imageBox.getChildren().remove(oldPlaceholder);
            oldPlaceholder.visibleProperty().unbind();
        }

        if (newPlaceholder != null) {
            imageBox.getChildren().add(newPlaceholder);
            newPlaceholder.visibleProperty().bind(view.photoSupplierProperty().isNotNull().and(view.photoProperty().isNull()).and(view.editableProperty()));
        }
    }

    private void updateClip() {
        if (getSkinnable().getClipShape().equals(ClipShape.CIRCLE)) {
            updateCircleClip();
        } else {
            updateRectangleClip();
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        double mw = contentX + contentWidth / 2;
        double mh = contentY + contentHeight / 2;

        final Image image = imageView.getImage();

        if (image != null) {
            double iw = image.getWidth();
            double ih = image.getHeight();

            double sx = contentWidth / iw;
            double sy = contentHeight / ih;

            double s = Math.max(sx, sy);

            double pw = s * iw;
            double ph = s * ih;

            imageView.setFitWidth(pw);
            imageView.setFitHeight(ph);

            imageView.resizeRelocate(mw - pw / 2, mh - ph / 2, pw, ph);
        }
    }

    private void updateRectangleClip() {
    }

    private void updateCircleClip() {
        Circle circle = new Circle();
        circle.radiusProperty().bind(Bindings.min(imageBox.widthProperty().divide(2), imageBox.heightProperty().divide(2)));
        circle.centerXProperty().bind(imageBox.widthProperty().divide(2));
        circle.centerYProperty().bind(imageBox.heightProperty().divide(2));
        circle.setEffect(new InnerShadow());

        circle.centerXProperty().addListener(it -> System.out.println("cx: " + circle.getCenterX()));
        imageBox.setClip(circle);
    }
}
