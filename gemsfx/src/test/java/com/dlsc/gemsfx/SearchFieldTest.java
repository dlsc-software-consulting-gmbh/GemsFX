package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import javafx.util.StringConverter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SearchField}.
 */
public class SearchFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SearchField<String> field = invoke(SearchField::new);
        assertNotNull(field);
        assertNull(field.getSelectedItem());
    }

    @Test
    public void testStyleClass() {
        SearchField<String> field = invoke(SearchField::new);
        assertTrue(field.getStyleClass().contains("search-field"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        SearchField<String> field = invoke(SearchField::new);
        assertNotNull(field.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        SearchField<String> field = invoke(SearchField::new);
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testEditorNotNullAfterLayout() {
        SearchField<String> field = invoke(SearchField::new);
        layout(field);
        assertNotNull(field.getEditor());
    }

    @Test
    public void testSelectedItemProperty() {
        SearchField<String> field = invoke(SearchField::new);
        runFx(() -> field.setSelectedItem("hello"));
        assertEquals("hello", field.getSelectedItem());
        assertEquals("hello", field.selectedItemProperty().get());
    }

    @Test
    public void testSuggestions() {
        SearchField<String> field = invoke(SearchField::new);
        // the suggestions list is filled by the search service and exposed read-only
        assertTrue(field.getSuggestions().isEmpty());
        try {
            runFx(() -> field.getSuggestions().add("alpha"));
            fail("the suggestions list must be unmodifiable");
        } catch (UnsupportedOperationException e) {
            // expected
        }
    }

    @Test
    public void testDefaultAddingItemToHistoryOnEnter() {
        SearchField<String> field = invoke(SearchField::new);
        assertTrue(field.isAddingItemToHistoryOnEnter());
    }

    @Test
    public void testDefaultAddingItemToHistoryOnFocusLost() {
        SearchField<String> field = invoke(SearchField::new);
        assertTrue(field.isAddingItemToHistoryOnFocusLost());
    }

    @Test
    public void testDefaultAddingItemToHistoryOnCommit() {
        SearchField<String> field = invoke(SearchField::new);
        assertTrue(field.isAddingItemToHistoryOnCommit());
    }

    @Test
    public void testConverterNotNull() {
        SearchField<String> field = invoke(SearchField::new);
        assertNotNull(field.getConverter());
    }

    @Test
    public void testCustomConverter() {
        SearchField<String> field = invoke(SearchField::new);
        StringConverter<String> conv = new StringConverter<String>() {
            @Override
            public String toString(String object) {
                return object == null ? "" : object.toUpperCase();
            }

            @Override
            public String fromString(String string) {
                return string;
            }
        };
        runFx(() -> field.setConverter(conv));
        assertEquals(conv, field.getConverter());
    }

    @Test
    public void testSearchingFalseByDefault() {
        SearchField<String> field = invoke(SearchField::new);
        assertFalse(field.isSearching());
    }

    @Test
    public void testClear() {
        SearchField<String> field = invoke(SearchField::new);
        runFx(() -> {
            field.getEditor().setText("test");
            field.clear();
        });
        waitForFxEvents();
        assertEquals("", field.getEditor().getText());
    }
}
