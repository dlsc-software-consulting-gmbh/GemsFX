package com.dlsc.gemsfx;

import javafx.scene.paint.Color;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TextView}.
 */
public class TextViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        TextView view = invoke(TextView::new);
        assertNotNull(view);
        assertNull(view.getText());
    }

    @Test
    public void testConstructorWithText() {
        TextView view = invoke(() -> new TextView("hello"));
        assertEquals("hello", view.getText());
    }

    @Test
    public void testStyleClass() {
        TextView view = invoke(TextView::new);
        assertTrue(view.getStyleClass().contains("text-view"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        TextView view = invoke(TextView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        TextView view = invoke(TextView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testTextProperty() {
        TextView view = invoke(TextView::new);
        runFx(() -> view.setText("world"));
        assertEquals("world", view.getText());
        assertEquals("world", view.textProperty().get());
    }

    @Test
    public void testDisableTextSelectionByMouseClicksDefault() {
        TextView view = invoke(TextView::new);
        assertFalse(view.isDisableTextSelectionByMouseClicks());
    }

    @Test
    public void testDisableTextSelectionByMouseClicksProperty() {
        TextView view = invoke(TextView::new);
        runFx(() -> view.setDisableTextSelectionByMouseClicks(true));
        assertTrue(view.isDisableTextSelectionByMouseClicks());
    }

    @Test
    public void testHighlightFillProperty() {
        TextView view = invoke(TextView::new);
        runFx(() -> view.setHighlightFill(Color.RED));
        assertEquals(Color.RED, view.getHighlightFill());
    }

    @Test
    public void testHighlightStrokeProperty() {
        TextView view = invoke(TextView::new);
        runFx(() -> view.setHighlightStroke(Color.BLUE));
        assertEquals(Color.BLUE, view.getHighlightStroke());
    }

    @Test
    public void testHighlightTextFillProperty() {
        TextView view = invoke(TextView::new);
        runFx(() -> view.setHighlightTextFill(Color.GREEN));
        assertEquals(Color.GREEN, view.getHighlightTextFill());
    }
}
