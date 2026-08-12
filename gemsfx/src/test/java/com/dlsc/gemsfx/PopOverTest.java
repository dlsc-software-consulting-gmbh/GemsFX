package com.dlsc.gemsfx;

import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PopOver}.
 * Note: show() is never called; only model/property behaviour is tested.
 */
public class PopOverTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        PopOver popOver = invoke(PopOver::new);
        assertNotNull(popOver);
    }

    @Test
    public void testDefaultDetachable() {
        PopOver popOver = invoke(PopOver::new);
        assertFalse(popOver.isDetachable());
    }

    @Test
    public void testSetDetachable() {
        PopOver popOver = invoke(PopOver::new);
        runFx(() -> popOver.setDetachable(false));
        assertFalse(popOver.isDetachable());
    }

    @Test
    public void testDefaultDetached() {
        PopOver popOver = invoke(PopOver::new);
        assertFalse(popOver.isDetached());
    }

    @Test
    public void testSetDetached() {
        PopOver popOver = invoke(PopOver::new);
        runFx(() -> popOver.setDetached(true));
        assertTrue(popOver.isDetached());
    }

    @Test
    public void testDefaultArrowLocation() {
        PopOver popOver = invoke(PopOver::new);
        assertNotNull(popOver.getArrowLocation());
    }

    @Test
    public void testSetArrowLocation() {
        PopOver popOver = invoke(PopOver::new);
        runFx(() -> popOver.setArrowLocation(PopOver.ArrowLocation.TOP_LEFT));
        assertEquals(PopOver.ArrowLocation.TOP_LEFT, popOver.getArrowLocation());
    }

    @Test
    public void testDefaultArrowSizePositive() {
        PopOver popOver = invoke(PopOver::new);
        assertTrue(popOver.getArrowSize() >= 0);
    }

    @Test
    public void testSetArrowSize() {
        PopOver popOver = invoke(PopOver::new);
        runFx(() -> popOver.setArrowSize(12));
        assertEquals(12.0, popOver.getArrowSize(), 0.001);
    }

    @Test
    public void testDefaultCornerRadius() {
        PopOver popOver = invoke(PopOver::new);
        assertTrue(popOver.getCornerRadius() >= 0);
    }

    @Test
    public void testSetCornerRadius() {
        PopOver popOver = invoke(PopOver::new);
        runFx(() -> popOver.setCornerRadius(8));
        assertEquals(8.0, popOver.getCornerRadius(), 0.001);
    }

    @Test
    public void testAnimatedProperty() {
        PopOver popOver = invoke(PopOver::new);
        assertNotNull(popOver.animatedProperty());
    }

    @Test
    public void testFadeInDuration() {
        PopOver popOver = invoke(PopOver::new);
        assertNotNull(popOver.getFadeInDuration());
        runFx(() -> popOver.setFadeInDuration(Duration.millis(200)));
        assertEquals(200, popOver.getFadeInDuration().toMillis(), 0.001);
    }

    @Test
    public void testContentNode() {
        PopOver popOver = invoke(PopOver::new);
        assertNotNull(popOver.contentNodeProperty());
    }
}
