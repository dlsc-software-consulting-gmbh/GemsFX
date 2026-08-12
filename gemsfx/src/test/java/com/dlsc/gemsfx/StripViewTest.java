package com.dlsc.gemsfx;

import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link StripView}.
 */
public class StripViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        StripView<String> view = invoke(StripView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.getStyleClass().contains("strip-view"));
    }

    @Test
    public void testDefaultItemsIsEmpty() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.getItems().isEmpty());
    }

    @Test
    public void testItemsCanBeAdded() {
        StripView<String> view = invoke(StripView::new);
        runFx(() -> view.getItems().addAll("A", "B", "C"));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testSelectedItemDefaultNull() {
        StripView<String> view = invoke(StripView::new);
        assertNull(view.getSelectedItem());
    }

    @Test
    public void testSelectedItemRoundTrip() {
        StripView<String> view = invoke(StripView::new);
        runFx(() -> {
            view.getItems().addAll("X", "Y");
            view.setSelectedItem("X");
        });
        assertEquals("X", view.getSelectedItem());
    }

    @Test
    public void testAlwaysCenterDefault() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.isAlwaysCenter());
    }

    @Test
    public void testAlwaysCenterRoundTrip() {
        StripView<String> view = invoke(StripView::new);
        runFx(() -> view.setAlwaysCenter(false));
        assertFalse(view.isAlwaysCenter());
    }

    @Test
    public void testAnimateScrollingDefault() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.isAnimateScrolling());
    }

    @Test
    public void testLoopSelectionDefault() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.isLoopSelection());
    }

    @Test
    public void testAutoScrollingDefault() {
        StripView<String> view = invoke(StripView::new);
        assertTrue(view.isAutoScrolling());
    }

    @Test
    public void testAnimationDurationDefault() {
        StripView<String> view = invoke(StripView::new);
        assertEquals(Duration.millis(200), view.getAnimationDuration());
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        StripView<String> view = invoke(StripView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testSkinCreation() {
        StripView<String> view = layout(invoke(StripView::new));
        assertNotNull(view.getSkin());
    }

    @Test
    public void testCellFactoryDefaultNotNull() {
        StripView<String> view = invoke(StripView::new);
        assertNotNull(view.getCellFactory());
    }

    @Test
    public void testStripCellSelectedState() {
        StripView<String> view = invoke(StripView::new);
        StripView.StripCell<String> cell = invoke(() -> {
            StripView.StripCell<String> c = new StripView.StripCell<>();
            c.setStripView(view);
            c.setItem("hello");
            view.getItems().add("hello");
            view.setSelectedItem("hello");
            return c;
        });
        assertTrue(cell.isSelected());
    }
}
