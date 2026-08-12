package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.InMemoryHistoryManager;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HistoryButton}.
 */
public class HistoryButtonTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button);
    }

    @Test
    public void testDefaultHistoryManagerIsNull() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNull(button.getHistoryManager());
    }

    @Test
    public void testSetHistoryManager() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        InMemoryHistoryManager<String> manager = new InMemoryHistoryManager<>();
        runFx(() -> button.setHistoryManager(manager));
        assertNotNull(button.getHistoryManager());
        assertSame(manager, button.getHistoryManager());
    }

    @Test
    public void testHistoryManagerAddItem() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        InMemoryHistoryManager<String> manager = new InMemoryHistoryManager<>();
        runFx(() -> {
            button.setHistoryManager(manager);
            manager.add("item1");
        });
        assertEquals(1, manager.getAllUnmodifiable().size());
        assertEquals("item1", manager.getAllUnmodifiable().get(0));
    }

    @Test
    public void testPopupNotShowingByDefault() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertFalse(button.isPopupShowing());
    }

    @Test
    public void testPopupShowingProperty() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button.popupShowingProperty());
    }

    @Test
    public void testPlaceholderProperty() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button.placeholderProperty());
    }

    @Test
    public void testOnItemSelectedProperty() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button.onItemSelectedProperty());
        runFx(() -> button.setOnItemSelected(item -> { /* no-op */ }));
        assertNotNull(button.getOnItemSelected());
    }

    @Test
    public void testCellFactoryProperty() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button.cellFactoryProperty());
    }

    @Test
    public void testHistoryManagerPropertyNotNull() {
        HistoryButton<String> button = invoke(HistoryButton::new);
        assertNotNull(button.historyManagerProperty());
    }

    @Test
    public void testMaxHistorySizeRespected() {
        InMemoryHistoryManager<String> manager = new InMemoryHistoryManager<>();
        runFx(() -> {
            manager.setMaxHistorySize(3);
            manager.add("a");
            manager.add("b");
            manager.add("c");
            manager.add("d");
        });
        waitForFxEvents();
        assertTrue(manager.getAllUnmodifiable().size() <= 3);
    }
}
