package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ResizableTextArea}.
 */
public class ResizableTextAreaTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        assertNotNull(area);
    }

    @Test
    public void testConstructorWithText() {
        ResizableTextArea area = invoke(() -> new ResizableTextArea("hi"));
        assertEquals("hi", area.getText());
    }

    @Test
    public void testDefaultResizeVertical() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        assertTrue(area.isResizeVertical());
    }

    @Test
    public void testDefaultResizeHorizontal() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        assertFalse(area.isResizeHorizontal());
    }

    @Test
    public void testResizeVerticalProperty() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        runFx(() -> area.setResizeVertical(false));
        assertFalse(area.isResizeVertical());
        assertFalse(area.resizeVerticalProperty().get());
    }

    @Test
    public void testResizeHorizontalProperty() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        runFx(() -> area.setResizeHorizontal(true));
        assertTrue(area.isResizeHorizontal());
        assertTrue(area.resizeHorizontalProperty().get());
    }

    @Test
    public void testGetUserAgentStylesheet() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        assertNotNull(area.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        ResizableTextArea area = invoke(ResizableTextArea::new);
        layout(area);
        assertNotNull(area.getSkin());
    }
}
