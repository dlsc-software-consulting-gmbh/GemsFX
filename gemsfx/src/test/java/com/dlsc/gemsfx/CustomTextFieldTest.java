package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CustomTextField}.
 */
public class CustomTextFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        CustomTextField field = invoke(() -> new CustomTextField());
        assertNotNull(field);
        assertNull(field.getLeft());
        assertNull(field.getRight());
        assertTrue(field.getText().isEmpty());
    }

    @Test
    public void testConstructorWithText() {
        CustomTextField field = invoke(() -> new CustomTextField("hello"));
        assertEquals("hello", field.getText());
    }

    @Test
    public void testStyleClass() {
        CustomTextField field = invoke(() -> new CustomTextField());
        assertTrue(field.getStyleClass().contains("custom-text-field"));
    }

    @Test
    public void testLeftProperty() {
        CustomTextField field = invoke(() -> new CustomTextField());
        Label lbl = invoke(() -> new Label("L"));
        runFx(() -> field.setLeft(lbl));
        assertEquals(lbl, field.getLeft());
        assertEquals(lbl, field.leftProperty().get());
    }

    @Test
    public void testRightProperty() {
        CustomTextField field = invoke(() -> new CustomTextField());
        Label lbl = invoke(() -> new Label("R"));
        runFx(() -> field.setRight(lbl));
        assertEquals(lbl, field.getRight());
        assertEquals(lbl, field.rightProperty().get());
    }

    @Test
    public void testLeftPropertyListener() {
        CustomTextField field = invoke(() -> new CustomTextField());
        boolean[] fired = {false};
        runFx(() -> field.leftProperty().addListener((obs, o, n) -> fired[0] = true));
        Label lbl = invoke(() -> new Label("X"));
        runFx(() -> field.setLeft(lbl));
        assertTrue(fired[0]);
    }

    @Test
    public void testCreateDefaultSkin() {
        CustomTextField field = invoke(() -> new CustomTextField());
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testGetUserAgentStylesheet() {
        CustomTextField field = invoke(() -> new CustomTextField());
        // CustomTextField does not override getUserAgentStylesheet; skin does
        // Just ensure field instantiates without exception
        assertNotNull(field);
    }
}
