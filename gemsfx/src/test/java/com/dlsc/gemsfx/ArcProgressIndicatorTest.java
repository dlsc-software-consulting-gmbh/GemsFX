package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ArcProgressIndicator}.
 * Uses {@link CircleProgressIndicator} as a concrete instance because ArcProgressIndicator is abstract
 * and its skin classes are also abstract (typed per subclass).
 */
public class ArcProgressIndicatorTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator);
    }

    @Test
    public void testStyleClass() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertTrue(indicator.getStyleClass().contains("arc-progress-indicator"));
    }

    @Test
    public void testCreateDefaultSkin() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        layout(indicator);
        assertNotNull(indicator.getSkin());
    }

    @Test
    public void testDefaultIsIndeterminate() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertTrue(indicator.isIndeterminate());
    }

    @Test
    public void testSetProgress() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        runFx(() -> indicator.setProgress(0.4));
        assertEquals(0.4, indicator.getProgress(), 0.001);
        assertFalse(indicator.isIndeterminate());
    }

    @Test
    public void testConstructorWithProgress() {
        ArcProgressIndicator indicator = invoke(() -> new CircleProgressIndicator(0.8));
        assertEquals(0.8, indicator.getProgress(), 0.001);
    }

    @Test
    public void testConverterNotNull() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.getConverter());
        assertNotNull(indicator.converterProperty());
    }

    @Test
    public void testSetConverterToNull() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        // just verify no exception is thrown
        runFx(() -> indicator.setConverter(null));
    }

    @Test
    public void testGraphicPropertyNotNull() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.graphicProperty());
    }

    @Test
    public void testProgressArcTypePropertyNotNull() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.progressArcTypeProperty());
    }

    @Test
    public void testTrackArcTypePropertyNotNull() {
        ArcProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.trackArcTypeProperty());
    }
}
