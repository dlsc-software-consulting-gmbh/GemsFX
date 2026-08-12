package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link SimpleFilterView}.
 */
public class SimpleFilterViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertTrue(view.getStyleClass().contains("simple-filter-view"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultLayoutMode() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertEquals(SimpleFilterView.LayoutMode.STANDARD, view.getLayoutMode());
    }

    @Test
    public void testLayoutModeRoundTrip() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        runFx(() -> view.setLayoutMode(SimpleFilterView.LayoutMode.COMPACT));
        assertEquals(SimpleFilterView.LayoutMode.COMPACT, view.getLayoutMode());
    }

    @Test
    public void testLayoutModePropertyAccessor() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertNotNull(view.layoutModeProperty());
        assertEquals(SimpleFilterView.LayoutMode.STANDARD, view.layoutModeProperty().get());
    }

    @Test
    public void testExtendsHBox() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertTrue(view instanceof javafx.scene.layout.HBox);
    }

    @Test
    public void testDefaultChildrenIsEmpty() {
        SimpleFilterView view = invoke(SimpleFilterView::new);
        assertTrue(view.getChildren().isEmpty());
    }
}
