package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhotoViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.function.Supplier;

public class PhotoView extends Control {

    public enum ClipShape {
        CIRCLE,
        RECTANGLE
    }

    private FileChooser fileChooser;

    public PhotoView() {
        getStyleClass().add("photo-view");
        //setPhoto(new Image(PhotoView.class.getResource("dirk.jpg").toExternalForm()));

        photo.addListener(it -> {
            setPhotoZoom(1);
            setPhotoTranslateX(0);
            setPhotoTranslateY(0);
        });

        /*
         * We need to also add the stylesheet directly as otherwise the styling for the
         * ikonli font icon will not work. Bug in Ikonli?
         */
        getStylesheets().add(PhotoView.class.getResource("photo-view.css").toExternalForm());

        setPhotoSupplier(() -> {
            if (fileChooser == null) {
                fileChooser = new FileChooser();
                fileChooser.setTitle("Load Image File");

                ExtensionFilter imageFileFilter = new ExtensionFilter("Image Files", "*.png", "*.gif", "*.jpg", "*.jpeg");
                fileChooser.getExtensionFilters().add(imageFileFilter);
                fileChooser.setSelectedExtensionFilter(imageFileFilter);
            }

            File file = fileChooser.showOpenDialog(getScene().getWindow());
            if (file != null) {
                try (FileInputStream stream = new FileInputStream(file)) {
                    return new Image(stream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            return null;
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhotoViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return PhotoView.class.getResource("photo-view.css").toExternalForm();
    }

    private final BooleanProperty editable = new SimpleBooleanProperty(this, "editable", true);

    public final boolean isEditable() {
        return editable.get();
    }

    public final BooleanProperty editableProperty() {
        return editable;
    }

    public final void setEditable(boolean editable) {
        this.editable.set(editable);
    }

    // photo supplier support, e.g. a file chooser

    private final ObjectProperty<Supplier<Image>> photoSupplier = new SimpleObjectProperty<>(this, "photoSupplier");

    public Supplier<Image> getPhotoSupplier() {
        return photoSupplier.get();
    }

    public ObjectProperty<Supplier<Image>> photoSupplierProperty() {
        return photoSupplier;
    }

    public void setPhotoSupplier(Supplier<Image> photoSupplier) {
        this.photoSupplier.set(photoSupplier);
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
