package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SVGImageView;
import com.dlsc.gemsfx.util.SVGUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

import java.net.URI;

public class SVGImageViewSkin extends SkinBase<SVGImageView> {

    private Service<Image> imageService;
    private final ImageView imageView = new ImageView();
    private final InvalidationListener listener = it -> loadingImage();
    private final WeakInvalidationListener weakListener = new WeakInvalidationListener(listener);

    public SVGImageViewSkin(SVGImageView svgImageView) {
        super(svgImageView);

        svgImageView.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        imageView.fitWidthProperty().bind(svgImageView.fitWidthProperty());
        imageView.fitHeightProperty().bind(svgImageView.fitHeightProperty());
        imageView.preserveRatioProperty().bind(svgImageView.preserveRatioProperty());
        imageView.smoothProperty().bind(svgImageView.smoothProperty());

        svgImageView.svgUrlProperty().addListener(weakListener);
        svgImageView.fitWidthProperty().addListener(weakListener);
        svgImageView.fitHeightProperty().addListener(weakListener);

        loadingImage();
        getChildren().add(imageView);

        registerWindowScaleListeners(svgImageView);
    }

    /**
     * Registers listeners on the current Scene and its Window for render-scale changes.
     * <p>
     * This ensures that when the {@link Scene} or its {@link javafx.stage.Window} changes (e.g., moving
     * from a normal DPI display to a high DPI/4K display), the renderScaleX and renderScaleY
     * property listeners will be added or removed appropriately.
     *
     * @param svgImageView the SVGImageView whose Scene and Window changes need to be tracked.
     */
    private void registerWindowScaleListeners(SVGImageView svgImageView) {
        Scene scene = svgImageView.getScene();
        if (scene != null && scene.getWindow() != null) {
            scene.getWindow().renderScaleXProperty().addListener(weakListener);
            scene.getWindow().renderScaleYProperty().addListener(weakListener);
        }

        svgImageView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null && oldScene.getWindow() != null) {
                oldScene.getWindow().renderScaleXProperty().removeListener(weakListener);
                oldScene.getWindow().renderScaleYProperty().removeListener(weakListener);
            }
            if (newScene != null && newScene.getWindow() != null) {
                newScene.getWindow().renderScaleXProperty().addListener(weakListener);
                newScene.getWindow().renderScaleYProperty().addListener(weakListener);
            }
        });
    }

    private void loadingImage() {
        if (imageService != null && imageService.isRunning()) {
            imageService.cancel();
            imageView.setImage(null);
        }

        SVGImageView skinnable = getSkinnable();
        String url = skinnable.getSvgUrl();
        if (url == null || url.isEmpty()) {
            return;
        }

        double sX = 1.0;
        double sY = 1.0;
        if (skinnable.getScene() != null && skinnable.getScene().getWindow() != null) {
            sX = skinnable.getScene().getWindow().getRenderScaleX();
            sY = skinnable.getScene().getWindow().getRenderScaleY();
        }

        if (!skinnable.isBackgroundLoading()) {
            try {
                Image image = SVGUtil.parseSVGFromUrl(new URI(url).toURL(), skinnable.getFitWidth() * sX, skinnable.getFitHeight() * sY);
                imageView.setImage(image);
            } catch (Exception e) {
                imageView.setImage(null);
            }
        } else {
            if (imageService == null) {
                imageService = createLoadingImageService();
                imageService.setOnSucceeded(evt -> imageView.setImage(imageService.getValue()));
            }
            imageService.restart();
        }
    }

    private Service<Image> createLoadingImageService() {
        SVGImageView skinnable = getSkinnable();
        return new Service<>() {
            @Override
            protected Task<Image> createTask() {
                return new Task<>() {
                    @Override
                    protected Image call() {
                        if (isCancelled()) {
                            return null;
                        }
                        try {
                            return SVGUtil.parseSVGFromUrl(new URI(skinnable.getSvgUrl()).toURL(), skinnable.getFitWidth(), skinnable.getFitHeight());
                        } catch (Exception e) {
                            return null;
                        }
                    }
                };
            }
        };
    }
}
