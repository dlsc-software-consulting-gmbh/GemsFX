package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link EnhancedPasswordField}.
 */
public class EnhancedPasswordFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertNotNull(field);
    }

    @Test
    public void testConstructorWithText() {
        EnhancedPasswordField field = invoke(() -> new EnhancedPasswordField("secret"));
        assertEquals("secret", field.getText());
    }

    @Test
    public void testStyleClass() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertTrue(field.getStyleClass().contains("enhanced-password-field"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertNotNull(field.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testShowPasswordDefault() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertFalse(field.isShowPassword());
    }

    @Test
    public void testShowPasswordProperty() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        runFx(() -> field.setShowPassword(true));
        assertTrue(field.isShowPassword());
        assertTrue(field.showPasswordProperty().get());
    }

    @Test
    public void testDefaultEchoChar() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertEquals(EnhancedPasswordField.DEFAULT_ECHO_CHAR, (char) field.getEchoChar());
    }

    @Test
    public void testEchoCharProperty() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        runFx(() -> field.setEchoChar('*'));
        assertEquals('*', (char) field.getEchoChar());
    }

    @Test
    public void testLeftProperty() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        assertNull(field.getLeft());
        javafx.scene.control.Label lbl = invoke(() -> new javafx.scene.control.Label("L"));
        runFx(() -> field.setLeft(lbl));
        assertEquals(lbl, field.getLeft());
    }

    @Test
    public void testRightProperty() {
        EnhancedPasswordField field = invoke(EnhancedPasswordField::new);
        // the control installs its own "reveal password" node on the right side
        assertNotNull(field.getRight());
        javafx.scene.control.Label lbl = invoke(() -> new javafx.scene.control.Label("R"));
        runFx(() -> field.setRight(lbl));
        assertEquals(lbl, field.getRight());
        runFx(() -> field.setRight(null));
        assertNull(field.getRight());
    }
}
