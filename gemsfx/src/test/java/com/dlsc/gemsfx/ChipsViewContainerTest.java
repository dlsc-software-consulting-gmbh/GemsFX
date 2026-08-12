package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ChipsViewContainer}.
 */
public class ChipsViewContainerTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        assertNotNull(container);
        assertNotNull(container.getChips());
        assertTrue(container.getChips().isEmpty());
    }

    @Test
    public void testStyleClass() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        assertTrue(container.getStyleClass().contains("chips-view-container"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        assertNotNull(container.getUserAgentStylesheet());
    }

    @Test
    public void testAddChip() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        ChipView<String> chip = invoke(ChipView::new);
        runFx(() -> container.getChips().add(chip));
        assertEquals(1, container.getChips().size());
    }

    @Test
    public void testClearTextProperty() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        runFx(() -> container.setClearText("Remove all"));
        assertEquals("Remove all", container.getClearText());
    }

    @Test
    public void testOnClearProperty() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        boolean[] fired = {false};
        runFx(() -> container.setOnClear(() -> fired[0] = true));
        runFx(() -> {
            Runnable r = container.getOnClear();
            if (r != null) r.run();
        });
        assertTrue(fired[0]);
    }

    @Test
    public void testSetChipsList() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        ChipView<String> chip1 = invoke(ChipView::new);
        ChipView<String> chip2 = invoke(ChipView::new);
        runFx(() -> container.setChips(FXCollections.observableArrayList(chip1, chip2)));
        assertEquals(2, container.getChips().size());
    }

    @Test
    public void testChipsProperty() {
        ChipsViewContainer container = invoke(ChipsViewContainer::new);
        assertNotNull(container.chipsProperty());
    }
}
