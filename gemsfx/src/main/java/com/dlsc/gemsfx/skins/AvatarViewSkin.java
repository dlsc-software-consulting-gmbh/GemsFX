package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.AvatarView;
import com.dlsc.gemsfx.AvatarView.AvatarShape;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

public class AvatarViewSkin extends SkinBase<AvatarView> {

    private final Group imageWrapper;
    private final ImageView image;
    private final StackPane textWrapper;
    private final StackPane iconWrapper;

    private final ChangeListener<Number> progressChangeListener = (ob, ov, nv) -> {
        if (nv.intValue() == 1) {
            updateView();
        }
    };

    public AvatarViewSkin(AvatarView avatar) {
        super(avatar);

        // blank avatar
        Region icon = new Region();
        icon.getStyleClass().add("icon");

        iconWrapper = new StackPane(icon);
        iconWrapper.getStyleClass().add("icon-wrapper");

        // image avatar
        image = new ImageView();
        image.getStyleClass().add("inner-image");
        image.imageProperty().bind(avatar.imageProperty());
        image.setSmooth(true);
        image.setPreserveRatio(true);

        imageWrapper = new Group();
        imageWrapper.getStyleClass().add("image-wrapper");
        imageWrapper.getChildren().setAll(image);

        // text avatar
        Text initialsText = new Text();
        initialsText.getStyleClass().add("initials-text");
        initialsText.textProperty().bind(avatar.initialsProperty());

        textWrapper = new StackPane(initialsText);
        textWrapper.getStyleClass().add("text-wrapper");
        textWrapper.getChildren().setAll(initialsText);
        textWrapper.prefWidthProperty().bind(avatar.sizeProperty());
        textWrapper.prefHeightProperty().bind(avatar.sizeProperty());
        textWrapper.minWidthProperty().bind(avatar.sizeProperty());
        textWrapper.minHeightProperty().bind(avatar.sizeProperty());
        textWrapper.maxWidthProperty().bind(avatar.sizeProperty());
        textWrapper.maxHeightProperty().bind(avatar.sizeProperty());

        ChangeListener<Image> imageChangeListener = (ob, oldImage, newImage) -> {
            if (oldImage != null) {
                oldImage.progressProperty().removeListener(progressChangeListener);
            }

            if (newImage != null) {
                newImage.progressProperty().addListener(progressChangeListener);
            }

            updateView();
        };

        avatar.imageProperty().addListener(imageChangeListener);

        Image image = avatar.getImage();
        if (image != null && image.isBackgroundLoading()) {
            image.progressProperty().addListener(progressChangeListener);
        }

        InvalidationListener updateViewListener = it -> updateView();
        avatar.imageProperty().addListener(updateViewListener);
        avatar.initialsProperty().addListener(updateViewListener);
        avatar.sizeProperty().addListener(it -> updateImageWrapperClip());
        avatar.avatarShapeProperty().addListener(it -> updateImageWrapperClip());

        createClipBinding(iconWrapper);
        createClipBinding(textWrapper);

        updateView();
    }

    private void createClipBinding(Node node) {
        AvatarView avatarView = getSkinnable();
        node.clipProperty().bind(Bindings.createObjectBinding(() -> {
            AvatarShape shape = avatarView.getAvatarShape();
            if (shape == AvatarShape.ROUND) {
                DoubleBinding sizeBinding = avatarView.sizeProperty().divide(2);
                Circle clipShape = new Circle();
                clipShape.centerXProperty().bind(sizeBinding);
                clipShape.centerYProperty().bind(sizeBinding);
                clipShape.radiusProperty().bind(sizeBinding);
                return clipShape;
            } else { // shape == Avatar.AvatarShape.SQUARE
                Rectangle rectangle = new Rectangle();
                rectangle.widthProperty().bind(avatarView.widthProperty());
                rectangle.heightProperty().bind(avatarView.heightProperty());
                rectangle.arcWidthProperty().bind(avatarView.arcSizeProperty());
                rectangle.arcHeightProperty().bind(avatarView.arcSizeProperty());
                return rectangle;
            }
        }, avatarView.avatarShapeProperty()));
    }

    private void updateView() {
        AvatarView avatarView = getSkinnable();
        Image img = avatarView.getImage();

        // if there is an image and it has been fully loaded, then we show that
        if (img != null && (!img.isBackgroundLoading() || img.getProgress() >= 1)) {
            updateImageWrapperClip();
            toggleContentNode(imageWrapper);
        } else if (StringUtils.isNotBlank(avatarView.getInitials())) {
            // if there are initials then show those
            toggleContentNode(textWrapper);
        } else {
            // no image, no initials, show icon
            toggleContentNode(iconWrapper);
        }
    }

    private void toggleContentNode(Node content) {
        if (getChildren().isEmpty() || getChildren().get(0) != content) {
            getChildren().setAll(content);
        }
    }

    private boolean isImageLoaded() {
        Image img = getSkinnable().getImage();
        return img != null && (!img.isBackgroundLoading() || img.getProgress() >= 1);
    }

    private void updateImageWrapperClip() {
        AvatarView avatarView = getSkinnable();
        Image img = avatarView.getImage();
        if (isImageLoaded()) {
            image.setFitHeight(-1);
            image.setFitWidth(-1);
            double width = img.getWidth();
            double height = img.getHeight();
            double avatarSize = avatarView.getSize();
            boolean isWidthGreaterThanHeight = width > height;
            double scale = isWidthGreaterThanHeight ? avatarSize / height : avatarSize / width;
            if (isWidthGreaterThanHeight) {
                image.setFitHeight(avatarSize);
            } else {
                image.setFitWidth(avatarSize);
            }

            if (avatarView.getAvatarShape() == AvatarView.AvatarShape.SQUARE) {
                Rectangle rectangle = new Rectangle();
                rectangle.widthProperty().bind(avatarView.sizeProperty());
                rectangle.heightProperty().bind(avatarView.sizeProperty());
                rectangle.arcWidthProperty().bind(avatarView.arcSizeProperty());
                rectangle.arcHeightProperty().bind(avatarView.arcSizeProperty());

                if (isWidthGreaterThanHeight) {
                    rectangle.setX((width * scale - avatarSize) / 2);
                } else {
                    rectangle.setY((height * scale - avatarSize) / 2);
                }
                imageWrapper.setClip(rectangle);
            } else if (avatarView.getAvatarShape() == AvatarView.AvatarShape.ROUND) {
                Circle circle = new Circle();
                circle.radiusProperty().bind(avatarView.sizeProperty().divide(2));
                if (isWidthGreaterThanHeight) {
                    circle.setCenterX(width * scale / 2);
                    circle.setCenterY(avatarSize / 2);
                } else {
                    circle.setCenterX(avatarSize / 2);
                    circle.setCenterY(height * scale / 2);
                }
                imageWrapper.setClip(circle);
            }
        } else {
            imageWrapper.setClip(null);
        }
    }

}
