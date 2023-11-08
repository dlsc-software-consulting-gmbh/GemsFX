package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SVGImageView;
import com.dlsc.gemsfx.util.SVGUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.WeakInvalidationListener;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
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

        if (!skinnable.isBackgroundLoading()) {
            try {
                Image image = SVGUtil.parseSVGFromUrl(new URI(url).toURL(), skinnable.getFitWidth(), skinnable.getFitHeight());
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
