package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SearchTextField}.
 */
public class SearchTextFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SearchTextField field = invoke(SearchTextField::new);
        assertNotNull(field);
        assertTrue(field.getText().isEmpty());
    }

    @Test
    public void testDefaultRound() {
        SearchTextField field = invoke(SearchTextField::new);
        assertFalse(field.isRound());
    }

    @Test
    public void testDefaultAddingItemToHistoryOnEnter() {
        SearchTextField field = invoke(SearchTextField::new);
        assertTrue(field.isAddingItemToHistoryOnEnter());
    }

    @Test
    public void testDefaultAddingItemToHistoryOnFocusLost() {
        SearchTextField field = invoke(SearchTextField::new);
        assertTrue(field.isAddingItemToHistoryOnFocusLost());
    }

    @Test
    public void testRoundProperty() {
        SearchTextField field = invoke(SearchTextField::new);
        runFx(() -> field.setRound(true));
        assertTrue(field.isRound());
        assertTrue(field.roundProperty().get());
    }

    @Test
    public void testAddingItemToHistoryOnEnterProperty() {
        SearchTextField field = invoke(SearchTextField::new);
        runFx(() -> field.setAddingItemToHistoryOnEnter(false));
        assertFalse(field.isAddingItemToHistoryOnEnter());
    }

    @Test
    public void testAddingItemToHistoryOnFocusLostProperty() {
        SearchTextField field = invoke(SearchTextField::new);
        runFx(() -> field.setAddingItemToHistoryOnFocusLost(false));
        assertFalse(field.isAddingItemToHistoryOnFocusLost());
    }

    @Test
    public void testGetUserAgentStylesheet() {
        SearchTextField field = invoke(SearchTextField::new);
        assertNotNull(field.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        SearchTextField field = invoke(SearchTextField::new);
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testStyleClass() {
        SearchTextField field = invoke(SearchTextField::new);
        assertTrue(field.getStyleClass().contains("custom-text-field"));
    }

    @Test
    public void testHistoryManagerDefaultNull() {
        SearchTextField field = invoke(SearchTextField::new);
        assertNull(field.getHistoryManager());
    }
}
