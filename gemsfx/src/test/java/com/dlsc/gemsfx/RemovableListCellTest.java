package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Tests for {@link RemovableListCell}.
 */
public class RemovableListCellTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        assertNotNull(cell);
    }

    @Test
    public void testStyleClass() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        assertTrue(cell.getStyleClass().contains("removable-list-cell"));
    }

    @Test
    public void testDefaultOnRemoveIsNull() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        assertNull(cell.getOnRemove());
    }

    @Test
    public void testOnRemoveCanBeSet() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        runFx(() -> cell.setOnRemove((lv, item) -> {}));
        assertNotNull(cell.getOnRemove());
    }

    @Test
    public void testConstructorWithOnRemoveHandler() {
        AtomicReference<String> removed = new AtomicReference<>();
        RemovableListCell<String> cell = invoke(() ->
                new RemovableListCell<>((lv, item) -> removed.set(item)));
        assertNotNull(cell.getOnRemove());
    }

    @Test
    public void testUpdateItemSetsGraphic() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        runFx(() -> cell.updateItem("hello", false));
        assertNotNull(cell.getGraphic());
        assertNull(cell.getText());
    }

    @Test
    public void testUpdateItemEmptyOrNull() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        runFx(() -> cell.updateItem(null, true));
        assertNull(cell.getGraphic());
    }

    @Test
    public void testUpdateItemThenEmpty() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        runFx(() -> {
            cell.updateItem("hello", false);
            cell.updateItem(null, true);
        });
        assertNull(cell.getGraphic());
        assertNull(cell.getText());
    }

    @Test
    public void testOnRemovePropertyAccessor() {
        RemovableListCell<String> cell = invoke(RemovableListCell::new);
        assertNotNull(cell.onRemoveProperty());
    }

    @Test
    public void testCellInsideListView() {
        ListView<String> listView = invoke(() -> {
            ListView<String> lv = new ListView<>(FXCollections.observableArrayList("a", "b"));
            lv.setCellFactory(lv2 -> new RemovableListCell<>());
            return lv;
        });
        layout(listView);
        assertNotNull(listView.getSkin());
    }
}
