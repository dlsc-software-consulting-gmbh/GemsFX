package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhotoViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.effect.Effect;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.function.Supplier;

public class PhotoView extends Control {

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    public enum ClipShape {
        CIRCLE,
        RECTANGLE
    }

    private FileChooser fileChooser;

    public PhotoView() {
        getStyleClass().add("photo-view");

        setPhoto(new Image(PhotoView.class.getResource("dirk.jpg").toExternalForm()));

        setFocusTraversable(true);

        pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, true);

        photo.addListener(it -> {
            setPhotoZoom(1);
            setPhotoTranslateX(0);
            setPhotoTranslateY(0);
            requestLayout();
            pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getPhoto() == null);
        });

        FontIcon fontIcon = new FontIcon(MaterialDesign.MDI_UPLOAD);
        fontIcon.getStyleClass().add("upload-icon");

        Label placeholder = new Label("DROP PHOTO\nOR CLICK TO ADD");
        placeholder.setTextAlignment(TextAlignment.CENTER);
        placeholder.setGraphic(fontIcon);
        placeholder.setContentDisplay(ContentDisplay.TOP);
        placeholder.getStyleClass().add("placeholder");
        setPlaceholder(placeholder);

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

        setOnDragOver(evt -> {
            if (isEditable()) {
                evt.acceptTransferModes(TransferMode.ANY);
            }
        });

        setOnDragDropped(evt -> {
            if (isEditable()) {
                Dragboard dragboard = evt.getDragboard();
                List<File> files = dragboard.getFiles();

                if (files != null) {
                    try {
                        File file = files.get(0);
                        final BufferedImage image = ImageIO.read(file);
                        if (image != null) {
                            setPhoto(SwingFXUtils.toFXImage(image, new WritableImage(image.getWidth(), image.getHeight())));
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        setOnKeyPressed(evt -> {
            if (isEditable()) {
                switch (evt.getCode()) {
                    case BACK_SPACE:
                    case DELETE:
                        setPhoto(null);
                        break;
                    case SPACE:
                    case ENTER:
                        getPhotoSupplier().get();
                }
            }
        });

        setOnMouseClicked(evt -> requestFocus());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhotoViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return PhotoView.class.getResource("photo-view.css").toExternalForm();
    }

    // photo effect

    private final ObjectProperty<Effect> photoEffect = new SimpleObjectProperty<>(this, "photoEffect");

    public final Effect getPhotoEffect() {
        return photoEffect.get();
    }

    public final ObjectProperty<Effect> photoEffectProperty() {
        return photoEffect;
    }

    public final void setPhotoEffect(Effect photoEffect) {
        this.photoEffect.set(photoEffect);
    }

    // placeholder support

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder");

    public final Node getPlaceholder() {
        return placeholder.get();
    }

    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final void setPlaceholder(Node placeholder) {
        this.placeholder.set(placeholder);
    }

    // editable support

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

    // clip shape support

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

    // photo support

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

    // photo zoom

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

    // photo translate x

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

    // photo translate y

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

    // max zoom

    private final DoubleProperty maxZoom = new SimpleDoubleProperty(this, "maxZoom", 5);

    public final double getMaxZoom() {
        return maxZoom.get();
    }

    public final DoubleProperty maxZoomProperty() {
        return maxZoom;
    }

    public final void setMaxZoom(double maxZoom) {
        this.maxZoom.set(maxZoom);
    }
}
