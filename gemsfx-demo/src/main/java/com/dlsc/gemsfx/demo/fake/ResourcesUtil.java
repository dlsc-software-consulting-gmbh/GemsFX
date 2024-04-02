package com.dlsc.gemsfx.demo.fake;

import javafx.scene.image.Image;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ResourcesUtil {

    /**
     * Image Cache
     */
    private static Map<String, SoftReference<Image>> imgCacheMap;

    private ResourcesUtil() {
    }

    public static String toExternalForm(String path) {
        return Objects.requireNonNull(ResourcesUtil.class.getResource(path)).toExternalForm();
    }

    public static Map<String, SoftReference<Image>> getAvatarImgCacheMap() {
        if (imgCacheMap == null) {
            imgCacheMap = new HashMap<>();
        }
        return imgCacheMap;
    }

    /**
     * load image
     * @param path image path
     * @param cache need cache
     * @return image
     */
    public static Image loadImage(String path, boolean cache) {
        return cache ? loadImageInCache(path) : loadImage(path);
    }

    /**
     * load image
     * Applies to images that are not frequently reused;, images that are used occasionally
     */
    public static Image loadImage(String path) {
        SoftReference<Image> reference = getAvatarImgCacheMap().get(path);
        Image temp;
        if (reference != null && (temp = reference.get())!=null) {
            return temp;
        }
        return new Image(toExternalForm(path), true);
    }

    /**
     * Get the picture from the cache, if there is no cache, load the picture in the background
     * After loading the image, store it in the cache
     * Suitable for images that need to be reused frequently
     */
    public static Image loadImageInCache(String path) {
        SoftReference<Image> computeImg = getAvatarImgCacheMap().compute(path, (key, value) -> {
            Image image = null;
            if (value != null) {
                image = value.get();
            }
            if (image == null) {
                try {
                    image = loadImage(path);
                } catch (Exception e) {
                    //Exception
                }
                value = new SoftReference<>(image);
            }
            return value;
        });
        return computeImg.get();
    }

}
