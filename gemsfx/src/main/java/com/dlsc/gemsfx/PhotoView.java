package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhotoViewSkin;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

public class PhotoView extends Control {

    public enum ClipShape {
        CIRCLE,
        RECTANGLE
    }

    public PhotoView() {
        getStyleClass().add("photo-view");
        setPhoto(new Image(PhotoView.class.getResource("dirk.jpg").toExternalForm()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhotoViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return PhotoView.class.getResource("photo-view.css").toExternalForm();
    }

    private final ObjectProperty<ClipShape> clipShape = new SimpleObjectProperty<>(this, "clipShape", ClipShape.CIRCLE);

    public ClipShape getClipShape() {
        return clipShape.get();
    }

    public ObjectProperty<ClipShape> clipShapeProperty() {
        return clipShape;
    }

    public void setClipShape(ClipShape clipShape) {
        this.clipShape.set(clipShape);
    }

    private final ObjectProperty<Image> photo = new SimpleObjectProperty<>(this, "image");

    public final Image getPhoto() {
        return photo.get();
    }

    public final ObjectProperty<Image> photoProperty() {
        return photo;
    }

    public final void setPhoto(Image photo) {
        this.photo.set(photo);
    }

    private final DoubleProperty photoZoom = new SimpleDoubleProperty(this, "photoZoom", 1);

    public final double getPhotoZoom() {
        return photoZoom.get();
    }

    public final DoubleProperty photoZoomProperty() {
        return photoZoom;
    }

    public final void setPhotoZoom(double photoZoom) {
        this.photoZoom.set(photoZoom);
    }

    private final DoubleProperty photoTranslateX = new SimpleDoubleProperty(this, "photoTranslateX");

    public final double getPhotoTranslateX() {
        return photoTranslateX.get();
    }

    public final DoubleProperty photoTranslateXProperty() {
        return photoTranslateX;
    }

    public final void setPhotoTranslateX(double photoTranslateX) {
        this.photoTranslateX.set(photoTranslateX);
    }

    private final DoubleProperty photoTranslateY = new SimpleDoubleProperty(this, "photoTranslateY");

    public final double getPhotoTranslateY() {
        return photoTranslateY.get();
    }

    public final DoubleProperty photoTranslateYProperty() {
        return photoTranslateY;
    }

    public final void setPhotoTranslateY(double photoTranslateY) {
        this.photoTranslateY.set(photoTranslateY);
    }
}
