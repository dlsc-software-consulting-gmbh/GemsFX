package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link InfoCenterPane} focusing on model-level properties such as pinned,
 * autoHide, showInfoCenter, and the embedded InfoCenterView.
 */
public class InfoCenterPaneTest extends FxTestBase {

    private InfoCenterPane create() {
        return invoke(InfoCenterPane::new);
    }

    @Test
    public void testInfoCenterViewIsNotNull() {
        InfoCenterPane pane = create();
        assertNotNull(pane.getInfoCenterView());
    }

    @Test
    public void testDefaultPinnedIsFalse() {
        InfoCenterPane pane = create();
        assertFalse(pane.isPinned());
    }

    @Test
    public void testSetPinned() {
        InfoCenterPane pane = create();
        runFx(() -> pane.setPinned(true));
        assertTrue(pane.isPinned());
    }

    @Test
    public void testDefaultAutoHideIsTrue() {
        InfoCenterPane pane = create();
        assertTrue(pane.isAutoHide());
    }

    @Test
    public void testSetAutoHide() {
        InfoCenterPane pane = create();
        runFx(() -> pane.setAutoHide(false));
        assertFalse(pane.isAutoHide());
    }

    @Test
    public void testDefaultShowInfoCenterIsFalse() {
        InfoCenterPane pane = create();
        assertFalse(pane.isShowInfoCenter());
    }

    @Test
    public void testSetShowInfoCenter() {
        InfoCenterPane pane = create();
        runFx(() -> pane.setShowInfoCenter(true));
        assertTrue(pane.isShowInfoCenter());
    }

    @Test
    public void testDefaultContentIsNull() {
        InfoCenterPane pane = create();
        assertNull(pane.getContent());
    }

    @Test
    public void testSetContent() {
        InfoCenterPane pane = create();
        runFx(() -> pane.setContent(new javafx.scene.control.Label("hello")));
        assertNotNull(pane.getContent());
    }

    @Test
    public void testAutoHideDurationDefaultNotNull() {
        InfoCenterPane pane = create();
        assertNotNull(pane.getAutoHideDuration());
    }
}
