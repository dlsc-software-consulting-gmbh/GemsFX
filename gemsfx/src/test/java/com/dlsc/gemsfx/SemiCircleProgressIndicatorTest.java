package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SemiCircleProgressIndicator}.
 */
public class SemiCircleProgressIndicatorTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertNotNull(indicator);
    }

    @Test
    public void testStyleClasses() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertTrue(indicator.getStyleClass().contains("arc-progress-indicator"));
        assertTrue(indicator.getStyleClass().contains("semi-circle-progress-indicator"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertNotNull(indicator.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        layout(indicator);
        assertNotNull(indicator.getSkin());
    }

    @Test
    public void testDefaultIsIndeterminate() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertTrue(indicator.isIndeterminate());
    }

    @Test
    public void testSetProgress() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        runFx(() -> indicator.setProgress(0.3));
        assertEquals(0.3, indicator.getProgress(), 0.001);
        assertFalse(indicator.isIndeterminate());
    }

    @Test
    public void testConstructorWithProgress() {
        SemiCircleProgressIndicator indicator = invoke(() -> new SemiCircleProgressIndicator(0.6));
        assertEquals(0.6, indicator.getProgress(), 0.001);
    }

    @Test
    public void testConverterNotNull() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertNotNull(indicator.getConverter());
    }

    @Test
    public void testGraphicPropertyNotNull() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        assertNotNull(indicator.graphicProperty());
    }

    @Test
    public void testProgressPropertyRoundTrip() {
        SemiCircleProgressIndicator indicator = invoke(SemiCircleProgressIndicator::new);
        runFx(() -> indicator.setProgress(1.0));
        assertEquals(1.0, indicator.getProgress(), 0.001);
    }
}
