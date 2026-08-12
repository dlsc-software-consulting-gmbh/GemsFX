package com.dlsc.gemsfx;

import javafx.scene.control.SelectionMode;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SelectionBox}: style class, stylesheet, items, selection model,
 * readOnly, promptText, autoHideOnSelection, and skin creation.
 */
public class SelectionBoxTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        assertTrue(box.getStyleClass().contains("selection-box"));
        assertTrue(box.getStyleClass().contains("combo-box-base"));
        assertTrue(box.getStyleClass().contains("combo-box"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        assertNotNull(box.getUserAgentStylesheet());
    }

    @Test
    public void testItemsFromVarargs() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        assertEquals(3, box.getItems().size());
        assertTrue(box.getItems().contains("A"));
    }

    @Test
    public void testItemsFromCollection() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>(Arrays.asList("X", "Y")));
        assertEquals(2, box.getItems().size());
    }

    @Test
    public void testSelectionModelNotNull() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        assertNotNull(box.getSelectionModel());
    }

    @Test
    public void testSelectItem() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        runFx(() -> box.getSelectionModel().select("B"));
        assertEquals("B", box.getSelectionModel().getSelectedItem());
    }

    @Test
    public void testClearSelection() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        runFx(() -> {
            box.getSelectionModel().select("A");
            box.getSelectionModel().clearSelection();
        });
        assertNull(box.getSelectionModel().getSelectedItem());
    }

    @Test
    public void testReadOnlyDefault() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        assertFalse(box.isReadOnly());
    }

    @Test
    public void testSetReadOnly() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        runFx(() -> box.setReadOnly(true));
        assertTrue(box.isReadOnly());
    }

    @Test
    public void testAutoHideOnSelectionDefault() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        // default true
        assertTrue(box.isAutoHideOnSelection());
    }

    @Test
    public void testSetPromptText() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        runFx(() -> box.setPromptText("Choose..."));
        assertEquals("Choose...", box.getPromptText());
    }

    @Test
    public void testCurrentSelectionModeNotNull() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B"));
        assertNotNull(box.getCurrentSelectionMode());
    }

    @Test
    public void testSkinCreation() {
        SelectionBox<String> box = invoke(() -> new SelectionBox<>("A", "B", "C"));
        layout(box);
        assertNotNull(box.getSkin());
    }
}
