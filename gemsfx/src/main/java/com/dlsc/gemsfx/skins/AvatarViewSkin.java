package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.AvatarView;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.scene.Group;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Border;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Text;

public class AvatarViewSkin extends SkinBase<AvatarView> {

    private final Group imageWrapper;
    private final ImageView innerImage;
    private final StackPane textWrapper;

    private final InvalidationListener invalidationListener = it -> updateView();

    private final ChangeListener<Number> progressChangeListener = (ob, ov, nv) -> {
        if (nv.intValue() == 1) {
            updateView();
        }
    };

    private final ChangeListener<Image> imageChangeListener = (ob, oldImage, newImage) -> {
        if (oldImage != null) {
            oldImage.progressProperty().removeListener(progressChangeListener);
        }
        if (newImage != null) {
            newImage.progressProperty().addListener(progressChangeListener);
        }
        updateView();
    };

    public AvatarViewSkin(AvatarView avatar) {
        super(avatar);
        // Image Avatar
        innerImage = new ImageView();
        innerImage.getStyleClass().add("inner-image");
        innerImage.imageProperty().bind(avatar.imageProperty());
        innerImage.setSmooth(true);
        innerImage.setPreserveRatio(true);

        imageWrapper = new Group();
        imageWrapper.getStyleClass().add("image-wrapper");
        imageWrapper.getChildren().setAll(innerImage);

        // Text Avatar
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

        // add listeners
        avatar.imageProperty().addListener(imageChangeListener);
        avatar.roundSizeProperty().addListener(invalidationListener);
        avatar.clipTypeProperty().addListener(invalidationListener);
        avatar.sizeProperty().addListener(invalidationListener);
        if (avatar.getImage() != null) {
            avatar.getImage().progressProperty().addListener(progressChangeListener);
        }

        updateView();
    }

    private void updateView() {
        AvatarView avatarView = getSkinnable();
        AvatarView.ClipType type = avatarView.getClipType();
        Shape clipShape;
        if (type == AvatarView.ClipType.CIRCLE) {
            clipShape = new Circle(avatarView.getSize() / 2, avatarView.getSize() / 2, avatarView.getSize() / 2);
        } else { // type == Avatar.ClipType.SQUARE
            clipShape = new Rectangle(0, 0, avatarView.getSize(), avatarView.getSize());
            ((Rectangle) clipShape).setArcWidth(avatarView.getRoundSize());
            ((Rectangle) clipShape).setArcHeight(avatarView.getRoundSize());
        }

        Image img = avatarView.getImage();
        if (img == null || Double.compare(img.getProgress(), 1.0) != 0) {

            textWrapper.setClip(clipShape);
            getChildren().setAll(textWrapper);
            return;
        }

        innerImage.setFitHeight(0);
        innerImage.setFitWidth(0);

        double imgWidth = img.getWidth();
        double imgHeight = img.getHeight();

        double r = avatarView.getSize() / 2;
        if (Double.compare(imgWidth, imgHeight) > 0) {
            innerImage.setFitHeight(r * 2);
            double scale = (r * 2) / img.getHeight();
            innerImage.setFitWidth(imgWidth * scale);
        } else {
            innerImage.setFitWidth(r * 2);
            double scale = (r * 2) / img.getWidth();
            innerImage.setFitHeight(imgHeight * scale);
        }

        innerImage.setClip(clipShape);
        getChildren().setAll(imageWrapper);
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

    @Override
    public void dispose() {
        AvatarView avatarView = getSkinnable();
        if (avatarView.getImage() != null) {
            avatarView.getImage().progressProperty().removeListener(progressChangeListener);
        }

        avatarView.imageProperty().removeListener(imageChangeListener);
        avatarView.roundSizeProperty().removeListener(invalidationListener);
        avatarView.clipTypeProperty().removeListener(invalidationListener);
        avatarView.sizeProperty().removeListener(invalidationListener);
        getChildren().clear();
        super.dispose();
    }

}
