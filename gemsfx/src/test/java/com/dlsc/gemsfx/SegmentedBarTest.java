package com.dlsc.gemsfx;

import javafx.geometry.Orientation;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link SegmentedBar} and its inner {@link SegmentedBar.Segment} class.
 */
public class SegmentedBarTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertNotNull(bar);
    }

    @Test
    public void testStyleClass() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertTrue(bar.getStyleClass().contains("segmented-bar"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertNotNull(bar.getUserAgentStylesheet());
    }

    @Test
    public void testSkinCreation() {
        SegmentedBar<SegmentedBar.Segment> bar = layout(invoke(SegmentedBar::new));
        assertNotNull(bar.getSkin());
    }

    @Test
    public void testDefaultSegmentsEmpty() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertTrue(bar.getSegments().isEmpty());
    }

    @Test
    public void testTotalIsZeroWhenNoSegments() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertEquals(0.0, bar.getTotal(), 0.001);
    }

    @Test
    public void testTotalUpdatesWhenSegmentsAdded() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        runFx(() -> {
            bar.getSegments().add(new SegmentedBar.Segment(10, "Ten"));
            bar.getSegments().add(new SegmentedBar.Segment(20, "Twenty"));
            bar.getSegments().add(new SegmentedBar.Segment(30, "Thirty"));
        });
        waitForFxEvents();
        assertEquals(60.0, bar.getTotal(), 0.001);
    }

    @Test
    public void testTotalUpdatesWhenSegmentValueChanges() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        SegmentedBar.Segment seg = invoke(() -> {
            SegmentedBar.Segment s = new SegmentedBar.Segment(10);
            bar.getSegments().add(s);
            return s;
        });
        runFx(() -> seg.setValue(50));
        waitForFxEvents();
        assertEquals(50.0, bar.getTotal(), 0.001);
    }

    @Test
    public void testSegmentTextRoundTrip() {
        SegmentedBar.Segment seg = new SegmentedBar.Segment(5, "Hello");
        assertEquals("Hello", seg.getText());
        seg.setText("World");
        assertEquals("World", seg.getText());
    }

    @Test
    public void testSegmentNegativeValueThrows() {
        try {
            new SegmentedBar.Segment(-1);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            // ok
        }
    }

    @Test
    public void testOrientationDefault() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertEquals(Orientation.VERTICAL, bar.getOrientation());
    }

    @Test
    public void testOrientationRoundTrip() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        runFx(() -> bar.setOrientation(Orientation.HORIZONTAL));
        assertEquals(Orientation.HORIZONTAL, bar.getOrientation());
    }

    @Test
    public void testMinSegmentSizeDefault() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertEquals(5.0, bar.getMinSegmentSize(), 0.001);
    }

    @Test
    public void testMinSegmentSizeRoundTrip() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        runFx(() -> bar.setMinSegmentSize(15.0));
        assertEquals(15.0, bar.getMinSegmentSize(), 0.001);
    }

    @Test
    public void testSegmentViewFactoryNotNull() {
        SegmentedBar<SegmentedBar.Segment> bar = invoke(SegmentedBar::new);
        assertNotNull(bar.getSegmentViewFactory());
    }
}
