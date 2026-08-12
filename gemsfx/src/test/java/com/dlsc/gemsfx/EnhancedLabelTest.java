package com.dlsc.gemsfx;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link EnhancedLabel}.
 */
public class EnhancedLabelTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        assertNotNull(label);
        assertTrue(label.getText().isEmpty());
    }

    @Test
    public void testConstructorWithText() {
        EnhancedLabel label = invoke(() -> new EnhancedLabel("hello"));
        assertEquals("hello", label.getText());
    }

    @Test
    public void testConstructorWithTextAndNode() {
        Label graphic = invoke(() -> new Label("G"));
        EnhancedLabel label = invoke(() -> new EnhancedLabel("hi", graphic));
        assertEquals("hi", label.getText());
        assertEquals(graphic, label.getGraphic());
    }

    @Test
    public void testStyleClass() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        assertTrue(label.getStyleClass().contains("enhanced-label"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        assertNotNull(label.getUserAgentStylesheet());
    }

    @Test
    public void testSelectedPropertyDefault() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        assertFalse(label.isSelected());
    }

    @Test
    public void testSelectedProperty() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        runFx(() -> label.setSelected(true));
        assertTrue(label.isSelected());
        assertTrue(label.selectedProperty().get());
    }

    @Test
    public void testCopyMenuItemText() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        runFx(() -> label.setCopyMenuItemText("Copy"));
        assertEquals("Copy", label.getCopyMenuItemText());
    }

    @Test
    public void testOnCopyActionProperty() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        boolean[] fired = {false};
        runFx(() -> label.setOnCopyAction(e -> fired[0] = true));
        runFx(() -> label.getOnCopyAction().handle(new ActionEvent()));
        assertTrue(fired[0]);
    }

    @Test
    public void testCopyContentSupplierProperty() {
        EnhancedLabel label = invoke(EnhancedLabel::new);
        runFx(() -> label.setCopyContentSupplier(() -> "custom content"));
        assertEquals("custom content", label.getCopyContentSupplier().get());
    }
}
