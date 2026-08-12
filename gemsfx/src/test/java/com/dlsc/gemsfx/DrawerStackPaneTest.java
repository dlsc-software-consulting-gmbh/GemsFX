package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DrawerStackPane}.
 */
public class DrawerStackPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertTrue(pane.getStyleClass().contains("drawer-stackpane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultShowDrawerFalse() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertFalse(pane.isShowDrawer());
    }

    @Test
    public void defaultFadeInOut() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertTrue(pane.isFadeInOut());
    }

    @Test
    public void defaultAutoHide() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertTrue(pane.isAutoHide());
    }

    @Test
    public void defaultAnimationDuration() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        assertEquals(Duration.millis(250), pane.getAnimationDuration());
    }

    @Test
    public void setShowDrawerTrue() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        runFx(() -> pane.setShowDrawer(true));
        assertTrue(pane.isShowDrawer());
    }

    @Test
    public void drawerContentPropertyRoundTrip() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        Label content = new Label("Drawer");
        runFx(() -> pane.setDrawerContent(content));
        assertSame(content, pane.getDrawerContent());
    }

    @Test
    public void showDrawerPropertyListener() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.showDrawerProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setShowDrawer(true));
        assertTrue(fired[0]);
    }

    @Test
    public void drawerTitlePropertyRoundTrip() {
        DrawerStackPane pane = invoke(DrawerStackPane::new);
        runFx(() -> pane.setDrawerTitle("My Drawer"));
        assertEquals("My Drawer", pane.getDrawerTitle());
    }
}
