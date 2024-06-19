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
import javafx.scene.layout.Border;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import org.apache.commons.lang3.StringUtils;

public class AvatarViewSkin extends SkinBase<AvatarView> {

    private final StackPane imageWrapper;
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

        iconWrapper = createWrapperStackPane(icon);
        iconWrapper.getStyleClass().add("icon-wrapper");

        // image avatar
        ImageView imageView = new ImageView();
        imageView.getStyleClass().add("inner-image");
        imageView.imageProperty().bind(avatar.imageProperty());
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);

        DoubleBinding scale = Bindings.createDoubleBinding(() -> {
            Image image = avatar.getImage();
            if (image != null) {
                return avatar.getSize() / Math.min(image.getWidth(), image.getHeight());
            }
            return 1d;
        }, avatar.sizeProperty(), avatar.imageProperty());

        imageView.scaleXProperty().bind(scale);
        imageView.scaleYProperty().bind(scale);

        Group imageGroup = new Group(imageView);

        imageWrapper = createWrapperStackPane(imageGroup);
        imageWrapper.getStyleClass().add("image-wrapper");

        // text avatar
        Text initialsText = new Text();
        initialsText.getStyleClass().add("initials-text");
        initialsText.textProperty().bind(avatar.initialsProperty());

        textWrapper = createWrapperStackPane(initialsText);
        textWrapper.getStyleClass().add("text-wrapper");

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

        createClipBinding(iconWrapper);
        createClipBinding(textWrapper);
        createClipBinding(imageWrapper);

        updateView();
    }

    private StackPane createWrapperStackPane(Node child) {
        AvatarView avatar = getSkinnable();
        StackPane stackPane = new StackPane(child);
        stackPane.getStyleClass().add("wrapper-stack-pane");
        stackPane.prefWidthProperty().bind(avatar.sizeProperty());
        stackPane.prefWidthProperty().bind(avatar.sizeProperty());
        stackPane.minWidthProperty().bind(avatar.sizeProperty());
        stackPane.minHeightProperty().bind(avatar.sizeProperty());
        stackPane.maxWidthProperty().bind(avatar.sizeProperty());
        stackPane.maxHeightProperty().bind(avatar.sizeProperty());
        return stackPane;
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
        Image image = avatarView.getImage();

        if (image != null && (!image.isBackgroundLoading() || image.getProgress() >= 1)) {
            // if there is an image, and it has been fully loaded, then we show the image
            getChildren().setAll(imageWrapper);
        } else if (StringUtils.isNotBlank(avatarView.getInitials())) {
            // if there are initials then show those
            getChildren().setAll(textWrapper);
        } else {
            // no image, no initials, show icon
            getChildren().setAll(iconWrapper);
        }
    }
}
