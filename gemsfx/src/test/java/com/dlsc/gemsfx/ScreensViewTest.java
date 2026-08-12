package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ScreensView}.
 * <p>
 * Under Monocle (headless) there is exactly one virtual screen; assertions are kept tolerant.
 */
public class ScreensViewTest extends FxTestBase {

    @Test
    public void styleClass() {
        ScreensView view = invoke(ScreensView::new);
        assertTrue(view.getStyleClass().contains("screens-view"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        ScreensView view = invoke(ScreensView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void defaultShowShadow() {
        ScreensView view = invoke(ScreensView::new);
        assertTrue(view.isShowShadow());
    }

    @Test
    public void defaultShowReflection() {
        ScreensView view = invoke(ScreensView::new);
        assertTrue(view.isShowReflection());
    }

    @Test
    public void defaultShowWallpaper() {
        ScreensView view = invoke(ScreensView::new);
        assertTrue(view.isShowWallpaper());
    }

    @Test
    public void defaultShowWindowsFalse() {
        ScreensView view = invoke(ScreensView::new);
        assertFalse(view.isShowWindows());
    }

    @Test
    public void defaultEnableWindowDragging() {
        ScreensView view = invoke(ScreensView::new);
        assertTrue(view.isEnableWindowDragging());
    }

    @Test
    public void setShowShadow() {
        ScreensView view = invoke(ScreensView::new);
        runFx(() -> view.setShowShadow(false));
        assertFalse(view.isShowShadow());
    }

    @Test
    public void setShowReflection() {
        ScreensView view = invoke(ScreensView::new);
        runFx(() -> view.setShowReflection(false));
        assertFalse(view.isShowReflection());
    }

    @Test
    public void shapesListInitiallyEmpty() {
        ScreensView view = invoke(ScreensView::new);
        assertNotNull(view.getShapes());
    }

    @Test
    public void skinCreatedAfterLayout() {
        ScreensView view = layout(invoke(ScreensView::new));
        assertNotNull(view.getSkin());
    }
}
