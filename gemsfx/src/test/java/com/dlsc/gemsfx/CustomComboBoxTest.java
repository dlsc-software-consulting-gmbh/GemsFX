package com.dlsc.gemsfx;

import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CustomComboBox}.
 * Uses a minimal concrete subclass because CustomComboBox extends the abstract ComboBoxBase.
 */
public class CustomComboBoxTest extends FxTestBase {

    /** Minimal concrete subclass for testing purposes only. */
    static class ConcreteComboBox extends CustomComboBox<String> {
        ConcreteComboBox() {
            super();
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new SkinBase<ConcreteComboBox>(this) {
            };
        }
    }

    @Test
    public void testDefaultConstruction() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        assertNotNull(combo);
    }

    @Test
    public void testDefaultButtonDisplay() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        assertEquals(CustomComboBox.ButtonDisplay.RIGHT, combo.getButtonDisplay());
    }

    @Test
    public void testSetButtonDisplayLeft() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        runFx(() -> combo.setButtonDisplay(CustomComboBox.ButtonDisplay.LEFT));
        assertEquals(CustomComboBox.ButtonDisplay.LEFT, combo.getButtonDisplay());
    }

    @Test
    public void testSetButtonDisplayButtonOnly() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        runFx(() -> combo.setButtonDisplay(CustomComboBox.ButtonDisplay.BUTTON_ONLY));
        assertEquals(CustomComboBox.ButtonDisplay.BUTTON_ONLY, combo.getButtonDisplay());
    }

    @Test
    public void testButtonDisplayPropertyNotNull() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        assertNotNull(combo.buttonDisplayProperty());
    }

    @Test
    public void testSetValue() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        runFx(() -> combo.setValue("hello"));
        assertEquals("hello", combo.getValue());
    }

    @Test
    public void testButtonDisplayEnumValues() {
        assertNotNull(CustomComboBox.ButtonDisplay.LEFT);
        assertNotNull(CustomComboBox.ButtonDisplay.RIGHT);
        assertNotNull(CustomComboBox.ButtonDisplay.BUTTON_ONLY);
    }

    @Test
    public void testCreateDefaultSkin() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        layout(combo);
        assertNotNull(combo.getSkin());
    }

    @Test
    public void testPropertyListenerFires() {
        ConcreteComboBox combo = invoke(ConcreteComboBox::new);
        boolean[] fired = {false};
        runFx(() -> combo.buttonDisplayProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> combo.setButtonDisplay(CustomComboBox.ButtonDisplay.LEFT));
        assertTrue(fired[0]);
    }
}
