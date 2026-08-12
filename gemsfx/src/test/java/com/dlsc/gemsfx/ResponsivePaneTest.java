package com.dlsc.gemsfx;

import javafx.geometry.Side;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ResponsivePane}.
 */
public class ResponsivePaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertTrue(pane.getStyleClass().contains("responsive-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultLargeSidebarCoversSmall() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertFalse(pane.isLargeSidebarCoversSmall());
    }

    @Test
    public void defaultForceLargeSidebarDisplay() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertFalse(pane.isForceLargeSidebarDisplay());
    }

    @Test
    public void defaultSide() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertEquals(Side.LEFT, pane.getSide());
    }

    @Test
    public void defaultGap() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        assertEquals(0.0, pane.getGap(), 1e-9);
    }

    @Test
    public void contentPropertyRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        Label content = new Label("Content");
        runFx(() -> pane.setContent(content));
        assertSame(content, pane.getContent());
    }

    @Test
    public void smallSidebarPropertyRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        Label sidebar = new Label("Small");
        runFx(() -> pane.setSmallSidebar(sidebar));
        assertSame(sidebar, pane.getSmallSidebar());
    }

    @Test
    public void largeSidebarPropertyRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        Label sidebar = new Label("Large");
        runFx(() -> pane.setLargeSidebar(sidebar));
        assertSame(sidebar, pane.getLargeSidebar());
    }

    @Test
    public void sidePropertyRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        runFx(() -> pane.setSide(Side.RIGHT));
        assertEquals(Side.RIGHT, pane.getSide());
    }

    @Test
    public void forceLargeSidebarDisplayRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        runFx(() -> pane.setForceLargeSidebarDisplay(true));
        assertTrue(pane.isForceLargeSidebarDisplay());
    }

    @Test
    public void gapPropertyRoundTrip() {
        ResponsivePane pane = invoke(ResponsivePane::new);
        runFx(() -> pane.setGap(10));
        assertEquals(10.0, pane.getGap(), 1e-9);
    }
}
