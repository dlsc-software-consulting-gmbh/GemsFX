package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.IntegerRange;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link LimitedTextArea}.
 */
public class LimitedTextAreaTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertNotNull(area);
    }

    @Test
    public void testStyleClass() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertTrue(area.getStyleClass().contains("limited-text-area"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertNotNull(area.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        layout(area);
        assertNotNull(area.getSkin());
    }

    @Test
    public void testDefaultShowBottom() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertTrue(area.isShowBottom());
    }

    @Test
    public void testShowBottomProperty() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        runFx(() -> area.setShowBottom(false));
        assertFalse(area.isShowBottom());
    }

    @Test
    public void testCharacterRangeLimitDefault() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertNull(area.getCharacterRangeLimit());
    }

    @Test
    public void testCharacterRangeLimitProperty() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        IntegerRange range = new IntegerRange(0, 100);
        runFx(() -> area.setCharacterRangeLimit(range));
        IntegerRange result = area.getCharacterRangeLimit();
        assertNotNull(result);
        assertEquals(0, result.getMin());
        assertEquals(100, result.getMax());
    }

    @Test
    public void testOutOfRangeFalseWhenNoLimit() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        runFx(() -> area.setText("some text"));
        waitForFxEvents();
        assertFalse(area.isOutOfRange());
    }

    @Test
    public void testDefaultLengthDisplayMode() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        assertEquals(LimitedTextArea.LengthDisplayMode.AUTO, area.getLengthDisplayMode());
    }

    @Test
    public void testLengthDisplayModeProperty() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        runFx(() -> area.setLengthDisplayMode(LimitedTextArea.LengthDisplayMode.ALWAYS_SHOW));
        assertEquals(LimitedTextArea.LengthDisplayMode.ALWAYS_SHOW, area.getLengthDisplayMode());
    }

    @Test
    public void testTipsProperty() {
        LimitedTextArea area = invoke(LimitedTextArea::new);
        runFx(() -> area.setTips("Enter text here"));
        assertEquals("Enter text here", area.getTips());
    }
}
