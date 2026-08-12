package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link AutoscrollListView}.
 * Animation and drag-event behaviour is not tested (requires display); only the model/API is verified.
 */
public class AutoscrollListViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        AutoscrollListView<String> view = invoke(AutoscrollListView::new);
        assertNotNull(view);
    }

    @Test
    public void testDefaultItemsListIsEmpty() {
        AutoscrollListView<String> view = invoke(AutoscrollListView::new);
        assertTrue(view.getItems().isEmpty());
    }

    @Test
    public void testConstructionWithItems() {
        AutoscrollListView<String> view = invoke(() ->
                new AutoscrollListView<>(FXCollections.observableArrayList("a", "b", "c")));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testItemsCanBeAdded() {
        AutoscrollListView<String> view = invoke(AutoscrollListView::new);
        runFx(() -> view.getItems().addAll("x", "y"));
        assertEquals(2, view.getItems().size());
    }

    @Test
    public void testItemsCanBeRemoved() {
        AutoscrollListView<String> view = invoke(() ->
                new AutoscrollListView<>(FXCollections.observableArrayList("a", "b")));
        runFx(() -> view.getItems().remove("a"));
        assertEquals(1, view.getItems().size());
        assertEquals("b", view.getItems().get(0));
    }

    @Test
    public void testInheritsListViewBehaviour() {
        AutoscrollListView<String> view = invoke(AutoscrollListView::new);
        assertTrue(view instanceof javafx.scene.control.ListView);
    }

    @Test
    public void testProximityConstant() {
        AutoscrollListView<String> view = invoke(AutoscrollListView::new);
        assertEquals(20.0, view.proximity, 0.001);
    }
}
