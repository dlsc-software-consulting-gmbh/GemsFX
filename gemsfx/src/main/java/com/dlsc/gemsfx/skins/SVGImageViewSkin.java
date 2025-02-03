package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SVGImageView;
import com.dlsc.gemsfx.util.SVGUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.stage.Window;
import one.jpro.platform.utils.TreeShowing;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SVGImageViewSkin extends SkinBase<SVGImageView> {

    private final Logger LOG = Logger.getLogger(SVGImageViewSkin.class.getName());

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

        initializeTreeShowingScaleListeners();
    }

    private void initializeTreeShowingScaleListeners() {
        BooleanProperty treeShowing = TreeShowing.treeShowing(getSkinnable());
        if (treeShowing.get()) {
            attachWindowScaleListeners();
        }

        treeShowing.addListener((obs, wasShowing, isNowShowing) -> {
            if (isNowShowing) {
                attachWindowScaleListeners();
            } else {
                detachWindowScaleListeners();
            }
        });
    }

    private void attachWindowScaleListeners() {
        SVGImageView svgImageView = getSkinnable();
        Scene currentScene = svgImageView.getScene();
        if (currentScene == null) {
            return;
        }

        currentScene.windowProperty().addListener(windowChangeListener);

        Window window = currentScene.getWindow();
        if (window != null) {
            attachScaleListeners(window);
        }

        svgImageView.sceneProperty().addListener(sceneChangeListener);
    }

    private void detachWindowScaleListeners() {
        SVGImageView svgImageView = getSkinnable();
        Scene currentScene = svgImageView.getScene();
        if (currentScene != null) {
            currentScene.windowProperty().removeListener(windowChangeListener);

            Window window = currentScene.getWindow();
            if (window != null) {
                detachScaleListeners(window);
            }
        }
        svgImageView.sceneProperty().removeListener(sceneChangeListener);
    }

    private final ChangeListener<Window> windowChangeListener = (observable, oldWindow, newWindow) -> {
        if (oldWindow != null) {
            detachScaleListeners(oldWindow);
        }
        if (newWindow != null) {
            attachScaleListeners(newWindow);

            // Force immediate image reload to reflect the new Window's render scale.
            loadingImage();
        }
    };

    private final ChangeListener<Scene> sceneChangeListener = (observable, oldScene, newScene) -> {
        if (oldScene != null) {
            oldScene.windowProperty().removeListener(windowChangeListener);
            Window oldWindow = oldScene.getWindow();
            if (oldWindow != null) {
                detachScaleListeners(oldWindow);
            }
        }
        if (newScene != null) {
            newScene.windowProperty().addListener(windowChangeListener);
            Window newWindow = newScene.getWindow();
            if (newWindow != null) {
                attachScaleListeners(newWindow);
            }
        }
    };

    private void attachScaleListeners(Window window) {
        // ensure no duplicate
        detachScaleListeners(window);
        window.renderScaleXProperty().addListener(weakListener);
        window.renderScaleYProperty().addListener(weakListener);
    }

    private void detachScaleListeners(Window window) {
        window.renderScaleXProperty().removeListener(weakListener);
        window.renderScaleYProperty().removeListener(weakListener);
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
                LOG.log(Level.WARNING, "Failed to load image: " + e.getMessage(), e);
            }
        } else {
            if (imageService == null) {
                imageService = createLoadingImageService(sX, sY);
                imageService.setOnSucceeded(evt -> imageView.setImage(imageService.getValue()));
            }
            imageService.restart();
        }
    }

    private Service<Image> createLoadingImageService(double sX, double sY) {
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
                            return SVGUtil.parseSVGFromUrl(new URI(skinnable.getSvgUrl()).toURL(), skinnable.getFitWidth() * sX, skinnable.getFitHeight() * sY);
                        } catch (Exception e) {
                            LOG.log(Level.WARNING, "Failed to load image: " + e.getMessage(), e);
                            return null;
                        }
                    }
                };
            }
        };
    }
}
