package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.AvatarView;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.Group;
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

    private final Group imageWrapper;
    private final ImageView image;
    private final StackPane textWrapper;
    private final StackPane iconWrapper;

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
        initialsText.getStyleClass().add("inner-text");
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

        avatar.imageProperty().addListener(it -> updateView());
        avatar.initialsProperty().addListener(it -> updateView());

        createClipBinding();
        updateView();
    }

    private void createClipBinding() {
        AvatarView avatarView = getSkinnable();
        avatarView.clipProperty().bind(Bindings.createObjectBinding(() -> {
            AvatarView.ClipType type = avatarView.getClipType();
            if (type == AvatarView.ClipType.CIRCLE) {
                DoubleBinding sizeBinding = avatarView.sizeProperty().divide(2);
                Circle clipShape = new Circle();
                clipShape.centerXProperty().bind(sizeBinding);
                clipShape.centerYProperty().bind(sizeBinding);
                clipShape.radiusProperty().bind(sizeBinding);
                return clipShape;
            } else { // type == Avatar.ClipType.SQUARE
                Rectangle rectangle = new Rectangle();
                rectangle.widthProperty().bind(avatarView.widthProperty());
                rectangle.heightProperty().bind(avatarView.heightProperty());
                rectangle.arcWidthProperty().bind(avatarView.roundSizeProperty());
                rectangle.arcHeightProperty().bind(avatarView.roundSizeProperty());
                return rectangle;
            }
        }, avatarView.clipTypeProperty()));
    }

    private void updateView() {
        AvatarView avatarView = getSkinnable();
        Image img = avatarView.getImage();

        // if there is an image, then we show that
        if (img != null) {
            image.fitWidthProperty().bind(avatarView.sizeProperty());
            image.fitHeightProperty().bind(avatarView.sizeProperty());
            getChildren().setAll(imageWrapper);
        } else if (StringUtils.isNotBlank(avatarView.getInitials())) {
            // if there are initials then show those
            getChildren().setAll(textWrapper);
        } else {
            // no image, no initials, show icon
            getChildren().setAll(iconWrapper);
        }
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefWidth(height, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        Border border = getSkinnable().getBorder();
        double borderInsets = border == null ? 0 : border.getInsets().getLeft() + border.getInsets().getRight();
        return super.computePrefWidth(height, topInset, rightInset, bottomInset, leftInset) + borderInsets;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        Border border = getSkinnable().getBorder();
        double borderInsets = border == null ? 0 : border.getInsets().getTop() + border.getInsets().getBottom();
        return super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset) + borderInsets;
    }
}
