package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SVGImageView}.
 */
public class SVGImageViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SVGImageView view = invoke(SVGImageView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        SVGImageView view = invoke(SVGImageView::new);
        assertTrue(view.getStyleClass().contains("svg-image-view"));
    }

    @Test
    public void testCreateDefaultSkin() {
        SVGImageView view = invoke(SVGImageView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testDefaultFitWidth() {
        SVGImageView view = invoke(SVGImageView::new);
        assertEquals(0.0, view.getFitWidth(), 0.001);
    }

    @Test
    public void testSetFitWidth() {
        SVGImageView view = invoke(SVGImageView::new);
        runFx(() -> view.setFitWidth(100));
        assertEquals(100.0, view.getFitWidth(), 0.001);
    }

    @Test
    public void testDefaultFitHeight() {
        SVGImageView view = invoke(SVGImageView::new);
        assertEquals(0.0, view.getFitHeight(), 0.001);
    }

    @Test
    public void testSetFitHeight() {
        SVGImageView view = invoke(SVGImageView::new);
        runFx(() -> view.setFitHeight(80));
        assertEquals(80.0, view.getFitHeight(), 0.001);
    }

    @Test
    public void testDefaultPreserveRatio() {
        SVGImageView view = invoke(SVGImageView::new);
        assertTrue(view.isPreserveRatio());
    }

    @Test
    public void testSetPreserveRatio() {
        SVGImageView view = invoke(SVGImageView::new);
        runFx(() -> view.setPreserveRatio(false));
        assertFalse(view.isPreserveRatio());
    }

    @Test
    public void testDefaultSmooth() {
        SVGImageView view = invoke(SVGImageView::new);
        assertTrue(view.isSmooth());
    }

    @Test
    public void testDefaultBackgroundLoading() {
        SVGImageView view = invoke(SVGImageView::new);
        assertFalse(view.isBackgroundLoading());
    }

    @Test
    public void testDefaultSvgUrlNull() {
        SVGImageView view = invoke(SVGImageView::new);
        assertNull(view.getSvgUrl());
    }

    @Test
    public void testSetSvgUrl() {
        SVGImageView view = invoke(SVGImageView::new);
        runFx(() -> view.setSvgUrl("file:///nonexistent.svg"));
        // the control normalizes the URL, hence only the file part is asserted
        assertNotNull(view.getSvgUrl());
        assertTrue(view.getSvgUrl().endsWith("nonexistent.svg"));
    }

    @Test
    public void testPropertyAccessors() {
        SVGImageView view = invoke(SVGImageView::new);
        assertNotNull(view.fitWidthProperty());
        assertNotNull(view.fitHeightProperty());
        assertNotNull(view.preserveRatioProperty());
        assertNotNull(view.smoothProperty());
        assertNotNull(view.svgUrlProperty());
        assertNotNull(view.backgroundLoadingProperty());
    }
}
