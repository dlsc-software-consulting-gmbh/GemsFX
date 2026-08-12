package com.dlsc.gemsfx;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ThreeItemsPane}.
 */
public class ThreeItemsPaneTest extends FxTestBase {

    @Test
    public void defaultOrientation() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        assertEquals(Orientation.HORIZONTAL, pane.getOrientation());
    }

    @Test
    public void defaultSpacing() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        assertEquals(0.0, pane.getSpacing(), 1e-9);
    }

    @Test
    public void defaultItemsNull() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        assertNull(pane.getItem1());
        assertNull(pane.getItem2());
        assertNull(pane.getItem3());
    }

    @Test
    public void item1PropertyRoundTrip() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        Label label = new Label("Item1");
        runFx(() -> pane.setItem1(label));
        assertSame(label, pane.getItem1());
    }

    @Test
    public void item2PropertyRoundTrip() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        Label label = new Label("Item2");
        runFx(() -> pane.setItem2(label));
        assertSame(label, pane.getItem2());
    }

    @Test
    public void item3PropertyRoundTrip() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        Label label = new Label("Item3");
        runFx(() -> pane.setItem3(label));
        assertSame(label, pane.getItem3());
    }

    @Test
    public void setAndGetSpacing() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        runFx(() -> pane.setSpacing(10));
        assertEquals(10.0, pane.getSpacing(), 1e-9);
    }

    @Test
    public void setAndGetOrientation() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        runFx(() -> pane.setOrientation(Orientation.VERTICAL));
        assertEquals(Orientation.VERTICAL, pane.getOrientation());
    }

    @Test
    public void settingItemsAddsToChildren() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        Label l1 = new Label("1");
        Label l2 = new Label("2");
        Label l3 = new Label("3");
        runFx(() -> {
            pane.setItem1(l1);
            pane.setItem2(l2);
            pane.setItem3(l3);
        });
        assertEquals(3, pane.getChildren().size());
    }

    @Test
    public void layoutCompletesWithoutError() {
        ThreeItemsPane pane = invoke(ThreeItemsPane::new);
        runFx(() -> {
            pane.setItem1(new Label("Left"));
            pane.setItem2(new Label("Center"));
            pane.setItem3(new Label("Right"));
        });
        layout(pane);
        // No exception means success
        assertEquals(3, pane.getChildren().size());
    }
}
