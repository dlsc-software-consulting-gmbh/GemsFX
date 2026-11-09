package com.dlsc.gemsfx.docking.internal;

import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockingLayoutNode;
import com.dlsc.gemsfx.DockingPosition;
import com.dlsc.gemsfx.FxTestBase;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class DockingTreeTest extends FxTestBase {

    private static Dockable dockable(String id) {
        return invoke(() -> new Dockable(id, id, new Label(id)));
    }

    private static DockingTree treeWith(String... ids) {
        DockingTree tree = new DockingTree();
        for (String id : ids) {
            tree.addToRoot(dockable(id));
        }
        return tree;
    }

    @Test
    public void newTreeHasEmptyRoot() {
        DockingTree tree = new DockingTree();
        assertTrue(tree.dockableIds().isEmpty());
        assertTrue(tree.root() instanceof DockingTabGroup);
        assertTrue(tree.snapshot() instanceof DockingLayoutNode.Empty);
    }

    @Test
    public void centerInsertionAddsSelectedTabs() {
        DockingTree tree = treeWith("a", "b");
        assertEquals(List.of("a", "b"), tree.dockableIds());
        DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) tree.snapshot();
        assertEquals("b", tabs.getSelectedId());

        tree.splitAt("a", dockable("c"), DockingPosition.CENTER, 0.5);
        assertEquals(List.of("a", "b", "c"), tree.dockableIds());
    }

    @Test
    public void splitEdgesProduceOrientationAndOrder() {
        Object[][] cases = {
                {DockingPosition.LEFT, Orientation.HORIZONTAL, true},
                {DockingPosition.RIGHT, Orientation.HORIZONTAL, false},
                {DockingPosition.TOP, Orientation.VERTICAL, true},
                {DockingPosition.BOTTOM, Orientation.VERTICAL, false}};
        for (Object[] c : cases) {
            DockingTree tree = treeWith("a");
            tree.splitAt("a", dockable("n"), (DockingPosition) c[0], 0.3);
            DockingSplit split = (DockingSplit) tree.root();
            assertEquals(c[1], split.getOrientation());
            assertEquals(0.3, split.getDividerPosition(), 0);
            String firstId = ((DockingTabGroup) split.getFirst()).getDockableIds().get(0);
            assertEquals((boolean) c[2], firstId.equals("n"));
        }
    }

    @Test
    public void selectAndReorderTabs() {
        DockingTree tree = treeWith("a", "b", "c");
        tree.select("a");
        tree.moveTab("a", 2);
        DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) tree.snapshot();
        assertEquals(List.of("b", "c", "a"), tabs.getDockableIds());
        assertEquals("a", tabs.getSelectedId());
    }

    @Test
    public void removingLastItemCollapsesItsTabGroupAndSplit() {
        DockingTree tree = treeWith("left");
        tree.splitAt("left", dockable("right"), DockingPosition.RIGHT, 0.5);

        tree.remove("left");

        assertEquals(List.of("right"), tree.dockableIds());
        assertFalse(tree.root() instanceof DockingSplit);
    }

    @Test
    public void removingSelectedTabSelectsNeighbour() {
        DockingTree tree = treeWith("a", "b", "c");
        tree.select("b");
        tree.remove("b");
        assertEquals("c", ((DockingLayoutNode.Tabs) tree.snapshot()).getSelectedId());
    }

    @Test
    public void removingLastDockableLeavesEmptyRoot() {
        DockingTree tree = treeWith("a");
        assertTrue(tree.remove("a"));
        assertFalse(tree.remove("a"));
        assertTrue(tree.snapshot() instanceof DockingLayoutNode.Empty);
    }

    @Test
    public void moveRelocatesDockableAndCollapsesSource() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        tree.move("a", "b", DockingPosition.CENTER, 0.5);
        assertFalse(tree.root() instanceof DockingSplit);
        assertEquals(List.of("b", "a"), tree.dockableIds());
    }

    @Test
    public void rejectsInvalidInputWithoutChangingTree() {
        DockingTree tree = treeWith("a", "b");
        assertThrows(IllegalArgumentException.class, () -> tree.addToRoot(dockable("a")));
        assertThrows(IllegalArgumentException.class, () -> tree.splitAt("a", dockable("c"), DockingPosition.LEFT, -0.1));
        assertThrows(IllegalArgumentException.class, () -> tree.splitAt("a", dockable("c"), DockingPosition.LEFT, 1.1));
        assertThrows(IllegalArgumentException.class, () -> tree.splitAt("a", dockable("c"), DockingPosition.LEFT, Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> tree.splitAt("zzz", dockable("c"), DockingPosition.LEFT, 0.5));
        assertThrows(IllegalArgumentException.class, () -> tree.move("a", "a", DockingPosition.LEFT, 0.5));
        assertEquals(List.of("a", "b"), tree.dockableIds());
        assertFalse(tree.root() instanceof DockingSplit);
    }

    @Test
    public void snapshotReflectsNestedSplits() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.4);
        tree.splitAt("b", dockable("c"), DockingPosition.BOTTOM, 0.6);
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(Orientation.HORIZONTAL, split.getOrientation());
        DockingLayoutNode.Split inner = (DockingLayoutNode.Split) split.getSecond();
        assertEquals(Orientation.VERTICAL, inner.getOrientation());
        assertEquals(List.of("a", "b", "c"), tree.dockableIds());
    }

    @Test
    public void dividerPositionUpdatesNestedSplitsInSnapshot() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        tree.splitAt("b", dockable("c"), DockingPosition.BOTTOM, 0.5);

        tree.setDividerPosition(List.of(1), 0.3);
        tree.setDividerPosition(List.of(), 0.7);

        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(0.7, root.getDividerPosition(), 1e-9);
        assertEquals(0.3, ((DockingLayoutNode.Split) root.getSecond()).getDividerPosition(), 1e-9);
    }

    @Test
    public void invalidDividerUpdateIsRejected() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);

        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPosition(List.of(), 1.1));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPosition(List.of(), -0.1));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPosition(List.of(), Double.NaN));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPosition(List.of(), Double.POSITIVE_INFINITY));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPosition(List.of(0), 0.4));
        assertEquals(0.5, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPosition(), 1e-9);
    }

    @Test
    public void dividerPositionAcceptsClosedIntervalEndpoints() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);

        tree.setDividerPosition(List.of(), 0.0);
        assertEquals(0.0, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPosition(), 0);
        tree.setDividerPosition(List.of(), 1.0);
        assertEquals(1.0, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPosition(), 0);
    }
}
