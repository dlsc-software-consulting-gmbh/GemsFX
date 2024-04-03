package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PhotoViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.MapChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.effect.Effect;
import javafx.scene.effect.SepiaTone;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The photo view is mostly used to display a user profile picture.
 * <h3>Features</h3>
 * <ul>
 *     <li>control can be used as read-only view or as an editor (see {@link #editableProperty()})</li>
 *     <li>picture can moved around by dragging it</li>
 *     <li>picture can be resized by pinch zoom (touch) or via scroll wheel</li>
 *     <li>control provides a cropped "read only" version of the original image (see @{{@link #croppedImageProperty()}}). This is ideal
 *     for saving memory when saving the image to the server / a database</li>
 *     <li>applications can set a custom "photo supplier" to replace the built-in file chooser (see {@link #photoSupplierProperty()})</li>
 *     <li>drag and drop an image file onto the view</li>
 *     <li>circular and rectangle shape (see {@link #setClipShape(ClipShape)})</li>
 *     <li>customizable maximum zoom value</li>
 *     <li>keyboard support: backspace and delete keys delete the picture, space or enter trigger the file supplier (default: show the file chooser)</li>
 *     <li>pseudo class support: "empty" if the {@link #photoProperty()} is null</li>
 *     <li>an effect can be applied directly to the image (see {@link #photoEffectProperty()})</li>
 * </ul>
 * <b>Note: the values for the zoom and translate properties will all be reset when a new photo is set.</b>
 */
public class PhotoView extends Control {

    private final Logger LOG = Logger.getLogger(PhotoView.class.getName());

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    private static final String[] SUPPORTED_EXTENSIONS = {".bmp", ".png", ".gif", ".jpg", ".jpeg"};

    public enum ClipShape {
        CIRCLE,
        RECTANGLE
    }

    private FileChooser fileChooser;

    /**
     * Constructs a new photo view.
     */
    public PhotoView() {
        getStyleClass().add("photo-view");

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

        Label placeholder = new Label("DROP IMAGE FILE\nOR CLICK TO ADD");
        placeholder.setTextAlignment(TextAlignment.CENTER);
        placeholder.setGraphic(fontIcon);
        placeholder.setContentDisplay(ContentDisplay.TOP);
        placeholder.getStyleClass().add("placeholder");
        setPlaceholder(placeholder);

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
                } catch (IOException e) {
                    LOG.log(Level.SEVERE, "error when trying to load selected image file", e);
                }
            }

            return null;
        });

        setOnDragOver(evt -> {
            if (isEditable() && evt.getDragboard().hasFiles()) {
                List<File> files = evt.getDragboard().getFiles();
                if (files == null) {
                    return;
                }
                // check if any of the files has a supported extension
                boolean hasSupportedExtension = files.stream()
                        .anyMatch(file -> Arrays.stream(SUPPORTED_EXTENSIONS)
                                .anyMatch(extension -> file.getName().endsWith(extension)));
                if (hasSupportedExtension) {
                    evt.acceptTransferModes(TransferMode.ANY);
                }
            }
        });

        setOnDragDropped(evt -> {
            if (isEditable() && evt.getDragboard().hasFiles()) {
                Dragboard dragboard = evt.getDragboard();
                List<File> files = dragboard.getFiles();

                if (files != null) {
                    // find the first file that has a supported extension
                    files.stream().filter(file -> Arrays.stream(SUPPORTED_EXTENSIONS)
                            .anyMatch(extension -> file.getName().endsWith(extension)))
                            .findFirst()
                            .ifPresentOrElse(supportedFile -> {
                                try {
                                    setPhoto(new Image(supportedFile.toURI().toURL().toExternalForm(), true));
                                    evt.setDropCompleted(true);
                                } catch (IOException e) {
                                    LOG.log(Level.SEVERE, "error when trying to use dropped image file", e);
                                    evt.setDropCompleted(false);
                                }
                            }, () -> evt.setDropCompleted(false));
                } else {
                    evt.setDropCompleted(false);
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

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("cropped.image")) {
                    croppedImage.set((Image) change.getValueAdded());
                }
            }
        };

        getProperties().addListener(propertiesListener);

        createCroppedImageProperty().addListener(it -> {
            if (!isCreateCroppedImage()) {
                croppedImage.set(null);
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PhotoViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PhotoView.class.getResource("photo-view.css")).toExternalForm();
    }

    // cropped image support

    public final BooleanProperty createCroppedImage = new SimpleBooleanProperty(this, "createCroppedImage", true);

    public final boolean isCreateCroppedImage() {
        return createCroppedImage.get();
    }

    /**
     * Specifies whether the view should constantly create a cropped image version of the
     * original image whenever the user edits the original. Creating a cropped image can have
     * a performance impact on slower hardware (e.g. embedded).
     *
     * @see #croppedImageProperty()
     * @return true if the view should create the cropped image
     */
    public final BooleanProperty createCroppedImageProperty() {
        return createCroppedImage;
    }

    public final void setCreateCroppedImage(boolean createCroppedImage) {
        this.createCroppedImage.set(createCroppedImage);
    }

    private final ReadOnlyObjectWrapper<Image> croppedImage = new ReadOnlyObjectWrapper<>(this, "croppedImage");

    public final Image getCroppedImage() {
        return croppedImage.get();
    }

    /**
     * A read-only property that contains the cropped version of the original image. An image
     * becomes cropped when the user moves it around or zooms into it. The cropped image is a good
     * candidate for saving it to the server or database. However, applications can choose freely
     * whether they prefer to store the original image or not.
     *
     * @return the cropped image version of the original image
     */
    public final ReadOnlyObjectProperty<Image> croppedImageProperty() {
        return croppedImage.getReadOnlyProperty();
    }

    // photo effect

    private final ObjectProperty<Effect> photoEffect = new SimpleObjectProperty<>(this, "photoEffect");

    public final Effect getPhotoEffect() {
        return photoEffect.get();
    }

    /**
     * An effect that will be applied to the image, not the whole control. Applications
     * could for example apply a sepia effect (see {@link SepiaTone}).
     *
     * @return the photo effect
     */
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

    /**
     * A placeholder that will be shown when no photo / image has been specified. The default
     * placeholder prompts the user to "click to add or drop a file".
     *
     * @return the placeholder node
     */
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

    /**
     * Controls whether the user will be able to edit the photo / image. If editable
     * the control will display a slider below the photo for zooming. The user will also
     * be able to zoom in via pinch zoom or scroll wheel. Via mouse dragging the user can
     * move the photo around.
     *
     * @return "true" if the view is editable
     */
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

    /**
     * The photo supplier will be invoked when the user clicks on the control (or hits space oder
     * enter keys). The default photo supplier registered on the control will bring up a file chooser
     * so that the user can select the image file.
     *
     * @return the photo supplier
     */
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

    /**
     * The clip shape determines whether the control will clip the photo via a circle
     * or a rectangle shape.
     *
     * @return the clip shape (circle, rectangle)
     */
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

    /**
     * Stores the original photo.
     *
     * @return the photo
     */
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

    /**
     * The zoom applied to the photo so that the photo's content fills out the
     * view as good as possible.
     *
     * @return the photo zoom
     */
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

    /**
     * Stores the percentage-based amount of the horizontal translation. We can not use pixel-based
     * translation as the view might change its size. This value changes when the user drags the photo
     * left or right.
     *
     * @return the translate x value (in percent)
     */
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

    /**
     * Stores the percentage-based amount of the vertical translation. We can not use pixel-based
     * translation as the view might change its size. This value changes when the user drags the photo
     * up or down.
     *
     * @return the translate y value (in percent)
     */
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

    /**
     * Stores the maximum amount that the user will be allowed to zoom into the view.
     * The default value is 5.
     *
     * @return the maximum zoom value (default 5)
     */
    public final DoubleProperty maxZoomProperty() {
        return maxZoom;
    }

    public final void setMaxZoom(double maxZoom) {
        this.maxZoom.set(maxZoom);
    }
}
