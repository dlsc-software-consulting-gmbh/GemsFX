package com.dlsc.gemsfx.util;

import com.github.weisj.jsvg.SVGDocument;
import com.github.weisj.jsvg.attributes.ViewBox;
import com.github.weisj.jsvg.geometry.size.FloatSize;
import com.github.weisj.jsvg.parser.SVGLoader;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Note for SVGUtil:
 * Currently, due to the limitation that weisj can only render BufferedImage from SVG,
 * SVGUtil does not support usage in native packaging scenarios.
 */
public class SVGUtil {

    private SVGUtil() {
    }

    public static Image parserSVGFromFile(String svgFilePath) {
        return parserSVGFromFile(new File(svgFilePath));
    }

    public static Image parserSVGFromFile(File svgFile) {
        return parserSVGFromFile(svgFile, -1, -1);
    }

    public static Image parserSVGFromFile(File svgFile, double prefWidth, double prefHeight) {
        return toImage(loadSVGDocument(svgFile), prefWidth, prefHeight, 1, 1);
    }

    public static Image parserSVGFromUrl(String urlString) {
        try {
            URL url = new URI(urlString).toURL();
            return parserSVGFromUrl(url);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image parserSVGFromUrl(URL url) {
        return parserSVGFromUrl(url, -1, -1);
    }

    public static Image parserSVGFromUrl(URL url, double prefWidth, double prefHeight) {
        return toImage(loadSVGDocument(url), prefWidth, prefHeight, 1, 1);
    }

    private static SVGDocument loadSVGDocument(File svgFile) {
        URL svgUrl;
        try {
            svgUrl = svgFile.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return loadSVGDocument(svgUrl);
    }

    private static SVGDocument loadSVGDocument(URL url) {
        SVGLoader loader = new SVGLoader();
        return loader.load(url);
    }

    private static SVGDocument loadSVGDocument(InputStream is) {
        SVGLoader loader = new SVGLoader();
        return loader.load(is);
    }

    public static Image toImage(InputStream is, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        return toImage(loadSVGDocument(is), requestedWidth, requestedHeight, outputScaleX, outputScaleY);
    }

    public static Image toImage(URL url, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        return toImage(loadSVGDocument(url), requestedWidth, requestedHeight, outputScaleX, outputScaleY);
    }

    public static Image toImage(String urlStr, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        try {
            return toImage(new URI(urlStr).toURL(), requestedWidth, requestedHeight, outputScaleX, outputScaleY);
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image toImage(File svgFile, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        return toImage(loadSVGDocument(svgFile), requestedWidth, requestedHeight, outputScaleX, outputScaleY);
    }

    private static Image toImage(SVGDocument svgDocument, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        if (svgDocument == null) {
            return null;
        }
        BufferedImage image = renderImage(svgDocument, requestedWidth, requestedHeight, outputScaleX, outputScaleY);
        return SwingFXUtils.toFXImage(image, null);
    }

    private static BufferedImage renderImage(SVGDocument svgDocument, double requestedWidth, double requestedHeight, double outputScaleX, double outputScaleY) {
        FloatSize size = svgDocument.size();
        double width = size.width;
        double height = size.height;

        double aspectRatio = width / height;
        if (requestedWidth > 0) {
            requestedHeight = requestedWidth / aspectRatio;
        } else if (requestedHeight > 0) {
            requestedWidth = requestedHeight * aspectRatio;
        }

        width = requestedWidth > 0 ? requestedWidth : width * outputScaleX;
        height = requestedHeight > 0 ? requestedHeight : height * outputScaleY;

        BufferedImage image = new BufferedImage((int) width, (int) height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);

        svgDocument.render(null, g2d, new ViewBox(0, 0, (float) width, (float) height));
        g2d.dispose();
        return image;
    }
}
