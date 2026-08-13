package com.dlsc.gemsfx;

import javafx.collections.FXCollections;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TagsField}.
 */
public class TagsFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        TagsField<String> field = invoke(TagsField::new);
        assertNotNull(field);
        assertNotNull(field.getTags());
        assertTrue(field.getTags().isEmpty());
    }

    @Test
    public void testStyleClass() {
        TagsField<String> field = invoke(TagsField::new);
        assertTrue(field.getStyleClass().contains("tags-field"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        TagsField<String> field = invoke(TagsField::new);
        assertNotNull(field.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        TagsField<String> field = invoke(TagsField::new);
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testAddTags() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.addTags("alpha", "beta"));
        waitForFxEvents();
        assertEquals(2, field.getTags().size());
        assertTrue(field.getTags().contains("alpha"));
        assertTrue(field.getTags().contains("beta"));
    }

    @Test
    public void testRemoveTags() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.addTags("alpha", "beta", "gamma"));
        waitForFxEvents();
        runFx(() -> field.removeTags("beta"));
        waitForFxEvents();
        assertFalse(field.getTags().contains("beta"));
        assertEquals(2, field.getTags().size());
    }

    @Test
    public void testClearTags() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.addTags("a", "b", "c"));
        waitForFxEvents();
        runFx(field::clearTags);
        waitForFxEvents();
        assertTrue(field.getTags().isEmpty());
    }

    @Test
    public void testSetTagsList() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.setTags(FXCollections.observableArrayList("x", "y")));
        waitForFxEvents();
        assertEquals(2, field.getTags().size());
    }

    @Test
    public void testTagSelectionModelNotNull() {
        TagsField<String> field = invoke(TagsField::new);
        assertNotNull(field.getTagSelectionModel());
    }

    @Test
    public void testTagViewFactory() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.setTagViewFactory(Label::new));
        assertNotNull(field.getTagViewFactory());
    }

    @Test
    public void testEditorMinWidthProperty() {
        TagsField<String> field = invoke(TagsField::new);
        runFx(() -> field.setEditorMinWidth(50.0));
        assertEquals(50.0, field.getEditorMinWidth(), 0.001);
    }
}
