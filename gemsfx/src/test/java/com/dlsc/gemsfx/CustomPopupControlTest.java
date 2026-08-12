package com.dlsc.gemsfx;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CustomPopupControl}.
 * Note: show() is never called because it requires the node to be in a scene/window.
 */
public class CustomPopupControlTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        assertNotNull(popup);
    }

    @Test
    public void testIsInstanceOfPopupControl() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        assertTrue(popup instanceof javafx.scene.control.PopupControl);
    }

    @Test
    public void testNotShowingByDefault() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        assertFalse(popup.isShowing());
    }

    @Test
    public void testShowThrowsWhenNodeNotInScene() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        javafx.scene.control.Label label = invoke(() -> new javafx.scene.control.Label("test"));
        // label has no scene -> show() must throw IllegalStateException
        try {
            runFx(() -> popup.show(label));
            fail("Expected IllegalStateException");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testAutoHideProperty() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        // just assert the property is accessible and can be mutated
        runFx(() -> popup.setAutoHide(true));
        assertTrue(popup.isAutoHide());
    }

    @Test
    public void testConsumeAutoHidingEventsProperty() {
        CustomPopupControl popup = invoke(CustomPopupControl::new);
        runFx(() -> popup.setConsumeAutoHidingEvents(false));
        assertFalse(popup.getConsumeAutoHidingEvents());
    }
}
