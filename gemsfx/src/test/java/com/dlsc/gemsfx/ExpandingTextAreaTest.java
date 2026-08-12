package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ExpandingTextArea}.
 */
public class ExpandingTextAreaTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        ExpandingTextArea area = invoke(ExpandingTextArea::new);
        assertNotNull(area);
        assertTrue(area.getText().isEmpty());
    }

    @Test
    public void testConstructorWithText() {
        ExpandingTextArea area = invoke(() -> new ExpandingTextArea("hello"));
        assertEquals("hello", area.getText());
    }

    @Test
    public void testStyleClass() {
        ExpandingTextArea area = invoke(ExpandingTextArea::new);
        assertTrue(area.getStyleClass().contains("expanding-text-area"));
    }

    @Test
    public void testWrapTextDefault() {
        ExpandingTextArea area = invoke(ExpandingTextArea::new);
        assertTrue(area.isWrapText());
    }

    @Test
    public void testSetText() {
        ExpandingTextArea area = invoke(ExpandingTextArea::new);
        runFx(() -> area.setText("new text"));
        assertEquals("new text", area.getText());
    }

    @Test
    public void testCreateDefaultSkin() {
        ExpandingTextArea area = invoke(ExpandingTextArea::new);
        layout(area);
        assertNotNull(area.getSkin());
    }
}
