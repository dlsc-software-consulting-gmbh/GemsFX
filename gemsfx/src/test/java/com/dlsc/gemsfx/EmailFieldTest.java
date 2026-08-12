package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link EmailField}.
 */
public class EmailFieldTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        EmailField field = invoke(EmailField::new);
        assertNotNull(field);
    }

    @Test
    public void testStyleClass() {
        EmailField field = invoke(EmailField::new);
        assertTrue(field.getStyleClass().contains("email-field"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        EmailField field = invoke(EmailField::new);
        assertNotNull(field.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        EmailField field = invoke(EmailField::new);
        layout(field);
        assertNotNull(field.getSkin());
    }

    @Test
    public void testEmptyAddressIsValidWhenNotRequired() {
        EmailField field = invoke(EmailField::new);
        runFx(() -> {
            field.setRequired(false);
            field.setEmailAddress("");
        });
        waitForFxEvents();
        assertTrue(field.isValid());
    }

    @Test
    public void testValidEmailAddress() {
        EmailField field = invoke(EmailField::new);
        runFx(() -> field.setEmailAddress("test@example.com"));
        waitForFxEvents();
        assertTrue(field.isValid());
    }

    @Test
    public void testInvalidEmailAddress() {
        EmailField field = invoke(EmailField::new);
        runFx(() -> {
            field.setRequired(true);
            field.setEmailAddress("not-an-email");
        });
        waitForFxEvents();
        assertFalse(field.isValid());
    }

    @Test
    public void testEmailAddressProperty() {
        EmailField field = invoke(EmailField::new);
        runFx(() -> field.setEmailAddress("a@b.com"));
        assertEquals("a@b.com", field.getEmailAddress());
    }

    @Test
    public void testRequiredProperty() {
        EmailField field = invoke(EmailField::new);
        assertFalse(field.isRequired());
        runFx(() -> field.setRequired(true));
        assertTrue(field.isRequired());
    }

    @Test
    public void testShowMailIconDefault() {
        EmailField field = invoke(EmailField::new);
        assertTrue(field.isShowMailIcon());
    }

    @Test
    public void testShowValidationIconDefault() {
        EmailField field = invoke(EmailField::new);
        assertTrue(field.isShowValidationIcon());
    }

    @Test
    public void testAutoDomainCompletionEnabledDefault() {
        EmailField field = invoke(EmailField::new);
        assertTrue(field.isAutoDomainCompletionEnabled());
    }

    @Test
    public void testEditorNotNull() {
        EmailField field = invoke(EmailField::new);
        layout(field);
        assertNotNull(field.getEditor());
    }
}
