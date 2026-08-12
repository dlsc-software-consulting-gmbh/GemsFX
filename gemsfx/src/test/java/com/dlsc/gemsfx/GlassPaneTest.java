package com.dlsc.gemsfx;

import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GlassPane}.
 */
public class GlassPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        GlassPane pane = invoke(GlassPane::new);
        assertTrue(pane.getStyleClass().contains("glass-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        GlassPane pane = invoke(GlassPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultBlockingOpacity() {
        GlassPane pane = invoke(GlassPane::new);
        assertEquals(0.5, pane.getBlockingOpacity(), 1e-9);
    }

    @Test
    public void defaultFadeInOut() {
        GlassPane pane = invoke(GlassPane::new);
        assertFalse(pane.isFadeInOut());
    }

    @Test
    public void defaultHide() {
        GlassPane pane = invoke(GlassPane::new);
        assertTrue(pane.isHide());
    }

    @Test
    public void defaultFadeInOutDuration() {
        GlassPane pane = invoke(GlassPane::new);
        assertEquals(Duration.millis(100), pane.getFadeInOutDuration());
    }

    @Test
    public void setBlockingOpacity() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setBlockingOpacity(0.8));
        assertEquals(0.8, pane.getBlockingOpacity(), 1e-9);
    }

    @Test
    public void setFadeInOut() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setFadeInOut(true));
        assertTrue(pane.isFadeInOut());
    }

    @Test
    public void setHide() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setHide(false));
        assertFalse(pane.isHide());
    }

    @Test
    public void blockingOpacityPropertyListener() {
        GlassPane pane = invoke(GlassPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.blockingOpacityProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setBlockingOpacity(0.3));
        assertTrue(fired[0]);
    }
}
