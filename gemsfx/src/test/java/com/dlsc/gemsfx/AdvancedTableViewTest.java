package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import javafx.scene.control.TableColumn;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link AdvancedTableView}.
 */
public class AdvancedTableViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        assertNotNull(view);
    }

    @Test
    public void testConstructionWithItems() {
        AdvancedTableView<String> view = invoke(() ->
                new AdvancedTableView<>(FXCollections.observableArrayList("a", "b")));
        assertEquals(2, view.getItems().size());
    }

    @Test
    public void testSkinCreation() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testItemsCanBeAdded() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        runFx(() -> view.getItems().add("hello"));
        assertEquals(1, view.getItems().size());
    }

    @Test
    public void testColumnsCanBeAdded() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        runFx(() -> view.getColumns().add(new TableColumn<>("Name")));
        assertEquals(1, view.getColumns().size());
    }

    @Test
    public void testAutoResizeAllColumnsWithZeroRowsDoesNothing() {
        AdvancedTableView<String> view = layout(invoke(AdvancedTableView::new));
        // Should not throw
        runFx(() -> view.autoResizeAllColumns(0));
    }

    @Test
    public void testAutoResizeAllColumnsWithoutSkinDoesNotThrow() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        // skin not yet created; should silently store the request
        runFx(() -> view.autoResizeAllColumns(10));
    }

    @Test
    public void testInheritsTableViewBehaviour() {
        AdvancedTableView<String> view = invoke(AdvancedTableView::new);
        assertTrue(view instanceof javafx.scene.control.TableView);
    }
}
