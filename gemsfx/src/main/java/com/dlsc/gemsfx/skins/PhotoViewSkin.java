package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PhotoView;
import com.dlsc.gemsfx.PhotoView.ClipShape;
import javafx.beans.InvalidationListener;
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
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

import java.util.function.Supplier;

public class PhotoViewSkin extends SkinBase<PhotoView> {

    public PhotoViewSkin(PhotoView view) {
        super(view);

        Slider slider = new Slider();
        slider.setMin(1);
        slider.maxProperty().bind(view.maxZoomProperty());
        slider.setValue(1);
        slider.setPrefWidth(200);
        slider.setMaxWidth(Region.USE_PREF_SIZE);
        slider.valueProperty().bindBidirectional(view.photoZoomProperty());
        slider.visibleProperty().bind(view.editableProperty());
        slider.managedProperty().bind(view.editableProperty());
        StackPane.setAlignment(slider, Pos.BOTTOM_CENTER);

        ImageBox imageBox = new ImageBox(view);
        imageBox.getStyleClass().add("image-box");
        VBox.setVgrow(imageBox, Priority.ALWAYS);

        VBox controlsWrapper = new VBox(imageBox, slider);
        controlsWrapper.getStyleClass().add("box");
        controlsWrapper.setAlignment(Pos.TOP_CENTER);

        controlsWrapper.setOnMouseClicked(evt -> {
            if (view.getPhoto() == null) {
                Supplier<Image> imageSupplier = view.getPhotoSupplier();
                if (imageSupplier != null) {
                    view.setPhoto(imageSupplier.get());
                }
            }
        });

        controlsWrapper.setOnScroll(evt -> view.setPhotoZoom(Math.min(view.getMaxZoom(), Math.max(1, view.getPhotoZoom() + Math.signum(evt.getDeltaY()) * .1))));
        controlsWrapper.setOnZoom(evt -> view.setPhotoZoom(Math.min(view.getMaxZoom(), Math.max(1, view.getPhotoZoom() * evt.getZoomFactor()))));

        getChildren().setAll(controlsWrapper);
    }

    public class ImageBox extends StackPane {

        private final ImageView imageView;
        private final Circle circle;
        private final Rectangle rectangle;

        private double startY;
        private double startX;

        public ImageBox(PhotoView view) {
            imageView = new ImageView();
            imageView.setPreserveRatio(true);
            imageView.imageProperty().bind(view.photoProperty());
            imageView.scaleXProperty().bind(view.photoZoomProperty());
            imageView.scaleYProperty().bind(view.photoZoomProperty());
            imageView.translateXProperty().bind(Bindings.createDoubleBinding(() -> view.getPhotoTranslateX() * imageView.getFitWidth(), view.photoTranslateXProperty(), imageView.fitWidthProperty()));
            imageView.translateYProperty().bind(Bindings.createDoubleBinding(() -> view.getPhotoTranslateY() * imageView.getFitHeight(), view.photoTranslateYProperty(), imageView.fitHeightProperty()));
            imageView.setCursor(Cursor.MOVE);
            imageView.effectProperty().bind(view.photoEffectProperty());
            imageView.setManaged(false);

            view.photoProperty().addListener(it -> requestLayout());

            setOnMousePressed(evt -> {
                if (view.isEditable()) {
                    startX = evt.getX();
                    startY = evt.getY();
                }
            });

            setOnMouseDragged(evt -> {
                if (view.isEditable()) {
                    double deltaX = (startX - evt.getX()) / imageView.getFitWidth();
                    double deltaY = (startY - evt.getY()) / imageView.getFitHeight();

                    view.setPhotoTranslateX(view.getPhotoTranslateX() - deltaX);
                    view.setPhotoTranslateY(view.getPhotoTranslateY() - deltaY);

                    startX = evt.getX();
                    startY = evt.getY();
                }
            });

            circle = new Circle();
            circle.getStyleClass().add("border-circle");
            circle.setManaged(false);
            circle.radiusProperty().bind(Bindings.min(widthProperty().divide(2), heightProperty().divide(2)));
            circle.centerXProperty().bind(widthProperty().divide(2));
            circle.centerYProperty().bind(heightProperty().divide(2));
            circle.setEffect(new DropShadow());
            circle.setMouseTransparent(true);

            rectangle = new Rectangle();
            rectangle.getStyleClass().add("border-rectangle");
            rectangle.setManaged(false);
            rectangle.widthProperty().bind(Bindings.min(widthProperty(), heightProperty()));
            rectangle.heightProperty().bind(Bindings.min(widthProperty(), heightProperty()));
            rectangle.layoutXProperty().bind(widthProperty().divide(2).subtract(rectangle.widthProperty().divide(2)));
            rectangle.layoutYProperty().bind(heightProperty().divide(2).subtract(rectangle.heightProperty().divide(2)));
            rectangle.setEffect(new DropShadow());
            rectangle.setMouseTransparent(true);

            view.clipShapeProperty().addListener(it -> {
                updateBorderShape();
                updateClip();
            });

            view.placeholderProperty().addListener((obs, oldPlaceholder, newPlaceholder) -> {
                updatePlaceholder(oldPlaceholder, newPlaceholder);
            });

            updateBorderShape();
            updateClip();
            updatePlaceholder(null, view.getPlaceholder());

            InvalidationListener cropListener = it -> crop();

            view.photoProperty().addListener(cropListener);
            view.photoZoomProperty().addListener(cropListener);
            view.photoTranslateXProperty().addListener(cropListener);
            view.photoTranslateYProperty().addListener(cropListener);
        }

        private void updateBorderShape() {
            if (getSkinnable().getClipShape().equals(ClipShape.CIRCLE)) {
                getChildren().remove(rectangle);
                getChildren().add(circle);
            } else {
                getChildren().remove(circle);
                getChildren().add(rectangle);
            }
        }

        private void updatePlaceholder(Node oldPlaceholder, Node newPlaceholder) {
            PhotoView view = getSkinnable();

            if (oldPlaceholder != null) {
                getChildren().remove(oldPlaceholder);
                oldPlaceholder.visibleProperty().unbind();
            }

            if (newPlaceholder != null) {
                if (view.getClipShape().equals(ClipShape.CIRCLE)) {
                    getChildren().setAll(imageView, newPlaceholder, circle);
                } else {
                    getChildren().setAll(imageView, newPlaceholder, rectangle);
                }
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

        public void crop() {
            Image image = getSkinnable().getPhoto();

            if (image == null) {
                getSkinnable().getProperties().put("cropped.image", null);
                return;
            }

            double scale = image.getWidth() / (imageView.getFitWidth() * getSkinnable().getPhotoZoom());
            double moveX = getSkinnable().getPhotoTranslateX() * image.getWidth() / getSkinnable().getPhotoZoom();
            double moveY = getSkinnable().getPhotoTranslateY() * image.getHeight() / getSkinnable().getPhotoZoom();

            int x = (int) (image.getWidth() / 2 - moveX);
            int y = (int) (image.getHeight() / 2 - moveY);
            int w;
            int h;


            if (getSkinnable().getClipShape().equals(ClipShape.CIRCLE)) {
                x -= (circle.getRadius() * scale);
                y -= (circle.getRadius() * scale);
                w = (int) (circle.getRadius() * scale * 2);
                h = (int) (circle.getRadius() * scale * 2);
            } else {
                x = (int) (rectangle.getWidth() / 2);
                y = (int) (rectangle.getHeight() / 2);
                w = (int) (rectangle.getWidth());
                h = (int) (rectangle.getHeight());
            }

            PixelReader reader = getSkinnable().getPhoto().getPixelReader();
            WritableImage croppedImage = new WritableImage(reader, Math.max(0, x), Math.max(0, y), (int) Math.min(image.getWidth(), w), (int) Math.min(image.getHeight(), h));
            getSkinnable().getProperties().put("cropped.image", croppedImage);
        }

        @Override
        protected void layoutChildren() {
            super.layoutChildren();

            double mw = getWidth() / 2;
            double mh = getHeight() / 2;

            Image image = imageView.getImage();

            if (image != null) {
                double iw = image.getWidth();
                double ih = image.getHeight();

                double sx;
                double sy;

                if (getSkinnable().getClipShape().equals(ClipShape.CIRCLE)) {
                    sx = circle.getRadius() * 2 / iw;
                    sy = circle.getRadius() * 2 / ih;
                } else {
                    sx = rectangle.getWidth() / iw;
                    sy = rectangle.getHeight() / ih;
                }

                double s = Math.min(sx, sy);

                double pw = s * iw;
                double ph = s * ih;

                imageView.setFitWidth(pw);
                imageView.setFitHeight(ph);

                imageView.resizeRelocate(mw - pw / 2, mh - ph / 2, pw, ph);
            }
        }

        private void updateRectangleClip() {
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(rectangle.widthProperty());
            clip.heightProperty().bind(rectangle.heightProperty());
            clip.layoutXProperty().bind(rectangle.layoutXProperty());
            clip.layoutYProperty().bind(rectangle.layoutYProperty());
            clip.setEffect(new InnerShadow());
            setClip(clip);
        }

        private void updateCircleClip() {
            Circle clip = new Circle();
            clip.radiusProperty().bind(circle.radiusProperty());
            clip.centerXProperty().bind(circle.centerXProperty());
            clip.centerYProperty().bind(circle.centerYProperty());
            clip.setEffect(new InnerShadow());
            setClip(clip);
        }
    }
}
