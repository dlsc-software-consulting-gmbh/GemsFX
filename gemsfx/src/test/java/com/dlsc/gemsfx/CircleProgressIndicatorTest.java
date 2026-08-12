package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CircleProgressIndicator}.
 */
public class CircleProgressIndicatorTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator);
    }

    @Test
    public void testStyleClasses() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertTrue(indicator.getStyleClass().contains("arc-progress-indicator"));
        assertTrue(indicator.getStyleClass().contains("circle-progress-indicator"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        layout(indicator);
        assertNotNull(indicator.getSkin());
    }

    @Test
    public void testDefaultIsIndeterminate() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertTrue(indicator.isIndeterminate());
    }

    @Test
    public void testSetProgress() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        runFx(() -> indicator.setProgress(0.5));
        assertEquals(0.5, indicator.getProgress(), 0.001);
        assertFalse(indicator.isIndeterminate());
    }

    @Test
    public void testConstructorWithProgress() {
        CircleProgressIndicator indicator = invoke(() -> new CircleProgressIndicator(0.75));
        assertEquals(0.75, indicator.getProgress(), 0.001);
    }

    @Test
    public void testConverterNotNull() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.getConverter());
        assertNotNull(indicator.converterProperty());
    }

    @Test
    public void testGraphicPropertyNotNull() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.graphicProperty());
    }

    @Test
    public void testStartAngleProperty() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        assertNotNull(indicator.startAngleProperty());
        // default start angle is 90 degrees
        assertEquals(90.0, indicator.getStartAngle(), 0.001);
    }

    @Test
    public void testSetStartAngle() {
        CircleProgressIndicator indicator = invoke(CircleProgressIndicator::new);
        runFx(() -> indicator.setStartAngle(0));
        assertEquals(0.0, indicator.getStartAngle(), 0.001);
    }
}
