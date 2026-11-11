package com.dlsc.gemsfx.docking.model;

import com.dlsc.gemsfx.FxTestBase;
import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPosition;
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
    public void sameAxisInsertionAndRemovalPreserveOtherIntervals() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.2);
        tree.splitAt("b", dockable("c"), DockingPosition.RIGHT, 0.375);
        tree.splitAt("c", dockable("d"), DockingPosition.RIGHT, 0.6);
        tree.setDividerPositions(List.of(), List.of(0.2, 0.5, 0.8));
        tree.splitAt("b", dockable("e"), DockingPosition.RIGHT, 0.5);
        assertEquals(List.of("a", "b", "e", "c", "d"), tree.dockableIds());
        assertEquals(List.of(0.2, 0.35, 0.5, 0.8),
                ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions());
        tree.remove("e");
        assertEquals(List.of(0.2, 0.5, 0.8),
                ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions());
        tree.remove("b");
        assertEquals(List.of("a", "c", "d"), tree.dockableIds());
        assertEquals(List.of(0.5, 0.8),
                ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions());
        tree.remove("a");
        assertEquals(List.of("c", "d"), tree.dockableIds());
        assertEquals(List.of(0.8),
                ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions());
    }

    @Test
    public void dividerVectorsUpdateAtomicallyAndAddressAnyChild() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.3);
        tree.splitAt("b", dockable("c"), DockingPosition.RIGHT, 0.5);
        tree.splitAt("c", dockable("d"), DockingPosition.BOTTOM, 0.4);
        tree.setDividerPositions(List.of(2), List.of(0.7));
        assertThrows(IllegalArgumentException.class,
                () -> tree.setDividerPositions(List.of(), List.of(0.9, 0.1)));
        assertArrayEquals(new double[]{0.3, 0.65},
                ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions().stream()
                        .mapToDouble(Double::doubleValue).toArray(), 1e-9);
        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(List.of(0.7), ((DockingLayoutNode.Split) root.getChildren().get(2)).getDividerPositions());
    }

    @Test
    public void removalFlattensNewlyAdjacentMatchingOrientations() {
        DockingTree tree = new DockingTree();
        DockingLayoutNode.Tabs a = new DockingLayoutNode.Tabs(List.of("a"), "a");
        DockingLayoutNode.Tabs b = new DockingLayoutNode.Tabs(List.of("b"), "b");
        DockingLayoutNode.Tabs c = new DockingLayoutNode.Tabs(List.of("c"), "c");
        DockingLayoutNode.Tabs d = new DockingLayoutNode.Tabs(List.of("d"), "d");
        DockingLayoutNode.Tabs e = new DockingLayoutNode.Tabs(List.of("e"), "e");
        tree.restore(new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(a,
                new DockingLayoutNode.Split(Orientation.VERTICAL, List.of(b,
                        new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(c, d), List.of(0.5))),
                        List.of(0.4)), e), List.of(0.2, 0.8)));
        tree.remove("b");
        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(List.of("a", "c", "d", "e"), tree.dockableIds());
        assertEquals(4, root.getChildren().size());
        assertEquals(List.of(0.2, 0.5, 0.8), root.getDividerPositions());
    }

    @Test
    public void restoreFlattensCollapsedIntervalsAndRejectsDuplicateIdsAtomically() {
        DockingTree tree = treeWith("original");
        DockingLayoutNode.Tabs a = new DockingLayoutNode.Tabs(List.of("a"), "a");
        DockingLayoutNode.Tabs b = new DockingLayoutNode.Tabs(List.of("b"), "b");
        DockingLayoutNode.Tabs c = new DockingLayoutNode.Tabs(List.of("c"), "c");
        assertThrows(IllegalArgumentException.class, () -> tree.restore(
                new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(a, a), List.of(0.5))));
        assertEquals(List.of("original"), tree.dockableIds());
        tree.restore(new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(a,
                new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(b, c), List.of(0.5))), List.of(1.0)));
        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(3, root.getChildren().size());
        assertEquals(List.of(1.0, 1.0), root.getDividerPositions());
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
            assertEquals(0.3, split.getDividerPositions().get(0), 0);
            String firstId = ((DockingTabGroup) split.getChildren().get(0)).getDockableIds().get(0);
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
    public void paneEdgeMovesWrapTheEntireRemainingLayout() {
        Object[][] cases = {
                {DockingPosition.LEFT, Orientation.HORIZONTAL, true},
                {DockingPosition.RIGHT, Orientation.HORIZONTAL, false},
                {DockingPosition.TOP, Orientation.VERTICAL, true},
                {DockingPosition.BOTTOM, Orientation.VERTICAL, false}
        };
        for (Object[] testCase : cases) {
            DockingTree tree = treeWith("left", "tools");
            tree.splitAt("left", dockable("right"), DockingPosition.RIGHT, 0.4);

            assertTrue(tree.moveToPaneEdge("tools", (DockingPosition) testCase[0], 0.5));

            DockingLayoutNode.Split outer = (DockingLayoutNode.Split) tree.snapshot();
            assertEquals(testCase[1], outer.getOrientation());
            boolean first = (boolean) testCase[2];
            DockingLayoutNode moved = outer.getChildren().get(first ? 0 : outer.getChildren().size() - 1);
            assertEquals(List.of("tools"), ((DockingLayoutNode.Tabs) moved).getDockableIds());
            if (outer.getOrientation() == Orientation.HORIZONTAL) {
                assertEquals(3, outer.getChildren().size());
                assertEquals(first ? List.of(0.5, 0.7) : List.of(0.2, 0.5), outer.getDividerPositions());
                assertEquals(List.of("left"), ((DockingLayoutNode.Tabs) outer.getChildren().get(first ? 1 : 0)).getDockableIds());
                assertEquals(List.of("right"), ((DockingLayoutNode.Tabs) outer.getChildren().get(first ? 2 : 1)).getDockableIds());
            } else {
                DockingLayoutNode.Split remainder = (DockingLayoutNode.Split) outer.getChildren().get(first ? 1 : 0);
                assertEquals(Orientation.HORIZONTAL, remainder.getOrientation());
                assertEquals(0.4, remainder.getDividerPositions().get(0), 0);
                assertEquals(List.of("left"), ((DockingLayoutNode.Tabs) remainder.getChildren().get(0)).getDockableIds());
                assertEquals(List.of("right"), ((DockingLayoutNode.Tabs) remainder.getChildren().get(1)).getDockableIds());
            }
            assertEquals(3, tree.dockableIds().size());
        }
    }

    @Test
    public void paneEdgeMoveCollapsesTheOldSourceBranch() {
        DockingTree tree = treeWith("left");
        tree.splitAt("left", dockable("right"), DockingPosition.RIGHT, 0.4);

        assertTrue(tree.moveToPaneEdge("left", DockingPosition.BOTTOM, 0.5));

        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(Orientation.VERTICAL, root.getOrientation());
        assertEquals(List.of("right"), ((DockingLayoutNode.Tabs) root.getChildren().get(0)).getDockableIds());
        assertEquals(List.of("left"), ((DockingLayoutNode.Tabs) root.getChildren().get(1)).getDockableIds());
    }

    @Test
    public void paneEdgeMovesRejectInvalidTargetsWithoutRemovingTheSource() {
        DockingTree tree = treeWith("a");
        assertFalse(tree.moveToPaneEdge("a", DockingPosition.BOTTOM, 0.5));
        tree.addToRoot(dockable("b"));

        assertFalse(tree.moveToPaneEdge("a", DockingPosition.CENTER, 0.5));
        assertFalse(tree.moveToPaneEdge("a", null, 0.5));
        assertFalse(tree.moveToPaneEdge("missing", DockingPosition.BOTTOM, 0.5));
        assertThrows(IllegalArgumentException.class,
                () -> tree.moveToPaneEdge("a", DockingPosition.BOTTOM, Double.NaN));

        assertEquals(List.of("a", "b"), tree.dockableIds());
        assertTrue(tree.snapshot() instanceof DockingLayoutNode.Tabs);
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
        DockingLayoutNode.Split inner = (DockingLayoutNode.Split) split.getChildren().get(1);
        assertEquals(Orientation.VERTICAL, inner.getOrientation());
        assertEquals(List.of("a", "b", "c"), tree.dockableIds());
    }

    @Test
    public void dividerPositionUpdatesNestedSplitsInSnapshot() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        tree.splitAt("b", dockable("c"), DockingPosition.BOTTOM, 0.5);

        tree.setDividerPositions(List.of(1), List.of(0.3));
        tree.setDividerPositions(List.of(), List.of(0.7));

        DockingLayoutNode.Split root = (DockingLayoutNode.Split) tree.snapshot();
        assertEquals(0.7, root.getDividerPositions().get(0), 1e-9);
        assertEquals(0.3, ((DockingLayoutNode.Split) root.getChildren().get(1)).getDividerPositions().get(0), 1e-9);
    }

    @Test
    public void invalidDividerUpdateIsRejected() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);

        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPositions(List.of(), List.of(1.1)));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPositions(List.of(), List.of(-0.1)));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPositions(List.of(), List.of(Double.NaN)));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPositions(List.of(), List.of(Double.POSITIVE_INFINITY)));
        assertThrows(IllegalArgumentException.class, () -> tree.setDividerPositions(List.of(0), List.of(0.4)));
        assertEquals(0.5, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions().get(0), 1e-9);
    }

    @Test
    public void dividerPositionAcceptsClosedIntervalEndpoints() {
        DockingTree tree = treeWith("a");
        tree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);

        tree.setDividerPositions(List.of(), List.of(0.0));
        assertEquals(0.0, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions().get(0), 0);
        tree.setDividerPositions(List.of(), List.of(1.0));
        assertEquals(1.0, ((DockingLayoutNode.Split) tree.snapshot()).getDividerPositions().get(0), 0);
    }
}
