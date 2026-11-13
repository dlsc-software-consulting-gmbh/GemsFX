package com.dlsc.gemsfx.docking;

import javafx.geometry.HPos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DockingFloatingGroupTest {

    private final DockingLayoutNode.Tabs tabs = new DockingLayoutNode.Tabs(List.of("a", "b"), "b");

    @Test
    public void retainsNearestEdgesWithoutSwitchingWhenPaneResizes() {
        DockingFloatingGroup group = DockingFloatingGroup.atBounds(tabs,
                new Rectangle2D(650, 400, 300, 180), 1000, 600);
        assertEquals(HPos.RIGHT, group.getHorizontalAnchor());
        assertEquals(VPos.BOTTOM, group.getVerticalAnchor());
        assertEquals(new Rectangle2D(850, 600, 300, 180), group.resolveBounds(1200, 800));
        assertEquals(new Rectangle2D(50, 20, 300, 180), group.resolveBounds(400, 220));
        assertEquals(new Rectangle2D(650, 400, 300, 180), group.resolveBounds(1000, 600));
    }

    @Test
    public void temporaryClampingDoesNotForgetSizeOrGaps() {
        DockingFloatingGroup group = DockingFloatingGroup.atBounds(tabs,
                new Rectangle2D(25, 30, 300, 180), 1000, 600);
        assertEquals(new Rectangle2D(0, 0, 100, 80), group.resolveBounds(100, 80));
        assertEquals(new Rectangle2D(25, 30, 300, 180), group.resolveBounds(1000, 600));
        assertEquals(new Rectangle2D(0, 0, 0, 0), group.resolveBounds(0, 0));
    }

    @Test
    public void rejectsInvalidAnchorsAndGeometry() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingFloatingGroup(tabs, HPos.CENTER, VPos.TOP, 10, 10, 300, 180));
        assertThrows(IllegalArgumentException.class, () ->
                new DockingFloatingGroup(tabs, HPos.LEFT, VPos.CENTER, 10, 10, 300, 180));
        assertThrows(IllegalArgumentException.class, () ->
                new DockingFloatingGroup(tabs, HPos.LEFT, VPos.TOP, -1, 10, 300, 180));
        assertThrows(IllegalArgumentException.class, () ->
                new DockingFloatingGroup(tabs, HPos.LEFT, VPos.TOP, 10, 10, Double.NaN, 180));
        assertThrows(IllegalArgumentException.class, () ->
                new DockingFloatingGroup(tabs, HPos.LEFT, VPos.TOP, 10, 10, 0, 180));
    }

    @Test
    public void boundsSpecifiedBeforeFirstLayoutStartAtRequestedTopLeftPosition() {
        DockingFloatingGroup group = DockingFloatingGroup.atBounds(tabs,
                new Rectangle2D(40, 50, 300, 180), 0, 0);
        assertEquals(new Rectangle2D(40, 50, 300, 180), group.resolveBounds(1000, 600));
    }
}
