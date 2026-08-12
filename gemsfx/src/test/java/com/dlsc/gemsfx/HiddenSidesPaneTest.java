package com.dlsc.gemsfx;

import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link HiddenSidesPane}.
 */
public class HiddenSidesPaneTest extends FxTestBase {

    @Test
    public void defaultTriggerDistance() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        assertEquals(16.0, pane.getTriggerDistance(), 1e-9);
    }

    @Test
    public void defaultPinnedSideNull() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        assertNull(pane.getPinnedSide());
    }

    @Test
    public void defaultAnimationDelay() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        assertEquals(Duration.millis(300), pane.getAnimationDelay());
    }

    @Test
    public void defaultAnimationDuration() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        assertEquals(Duration.millis(200), pane.getAnimationDuration());
    }

    @Test
    public void contentPropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        Label content = new Label("Content");
        runFx(() -> pane.setContent(content));
        assertSame(content, pane.getContent());
    }

    @Test
    public void topPropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        Label top = new Label("Top");
        runFx(() -> pane.setTop(top));
        assertSame(top, pane.getTop());
    }

    @Test
    public void rightPropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        Label right = new Label("Right");
        runFx(() -> pane.setRight(right));
        assertSame(right, pane.getRight());
    }

    @Test
    public void bottomPropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        Label bottom = new Label("Bottom");
        runFx(() -> pane.setBottom(bottom));
        assertSame(bottom, pane.getBottom());
    }

    @Test
    public void leftPropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        Label left = new Label("Left");
        runFx(() -> pane.setLeft(left));
        assertSame(left, pane.getLeft());
    }

    @Test
    public void pinnedSidePropertyRoundTrip() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        runFx(() -> pane.setPinnedSide(Side.LEFT));
        assertEquals(Side.LEFT, pane.getPinnedSide());
    }

    @Test
    public void triggerDistancePropertyListener() {
        HiddenSidesPane pane = invoke(HiddenSidesPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.triggerDistanceProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setTriggerDistance(32));
        assertTrue(fired[0]);
    }
}
