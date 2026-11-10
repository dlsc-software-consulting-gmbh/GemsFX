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

public class DockingDropTargetTest extends FxTestBase {

    private static Dockable dockable(String id) {
        return invoke(() -> new Dockable(id, id, new Label(id)));
    }

    private static DockingTree singleGroupTree() {
        DockingTree tree = new DockingTree();
        tree.addToRoot(dockable("a"));
        tree.addToRoot(dockable("b"));
        tree.addToRoot(dockable("c"));
        return tree;
    }

    @Test
    public void resolvesCenterAndEdgesFromPaneLocalCoordinates() {
        DockingTree tree = singleGroupTree();
        DockingLayoutNode snapshot = tree.snapshot();

        assertEquals(new DockingTree.DropTarget("c", DockingPosition.CENTER),
                DockingTree.resolveDropTarget(snapshot, 50, 50, 100, 100));
        assertEquals(new DockingTree.DropTarget("c", DockingPosition.LEFT),
                DockingTree.resolveDropTarget(snapshot, 5, 50, 100, 100));
        assertEquals(new DockingTree.DropTarget("c", DockingPosition.RIGHT),
                DockingTree.resolveDropTarget(snapshot, 95, 50, 100, 100));
        assertEquals(new DockingTree.DropTarget("c", DockingPosition.TOP),
                DockingTree.resolveDropTarget(snapshot, 50, 5, 100, 100));
        assertEquals(new DockingTree.DropTarget("c", DockingPosition.BOTTOM),
                DockingTree.resolveDropTarget(snapshot, 50, 95, 100, 100));
    }

    @Test
    public void resolvesNestedSplitLeafAndHoveredTitleInLeafGroup() {
        DockingTree tree = singleGroupTree();
        tree.splitAt("c", dockable("d"), DockingPosition.RIGHT, 0.5);
        tree.splitAt("d", dockable("e"), DockingPosition.BOTTOM, 0.4);
        DockingLayoutNode snapshot = tree.snapshot();

        assertEquals(new DockingTree.DropTarget("c", DockingPosition.CENTER),
                DockingTree.resolveDropTarget(snapshot, 20, 50, 100, 100));
        assertEquals(new DockingTree.DropTarget("d", DockingPosition.CENTER),
                DockingTree.resolveDropTarget(snapshot, 70, 20, 100, 100));
        assertEquals(new DockingTree.DropTarget("e", DockingPosition.CENTER),
                DockingTree.resolveDropTarget(snapshot, 70, 80, 100, 100));
        assertEquals(new DockingTree.DropTarget("a", DockingPosition.CENTER),
                DockingTree.resolveDropTarget(snapshot, 20, 50, 100, 100, "a").getDropTarget());
    }

    @Test
    public void cancelledOrRejectedDropRetainsSourceDockable() {
        DockingTree tree = singleGroupTree();
        List<String> beforeIds = tree.dockableIds();
        DockingLayoutNode.Tabs beforeSnapshot = (DockingLayoutNode.Tabs) tree.snapshot();

        assertFalse(tree.tryMove("a", null, 0.5));
        assertFalse(tree.tryMove("a", new DockingTree.DropTarget("missing", DockingPosition.LEFT), 0.5));

        assertEquals(beforeIds, tree.dockableIds());
        assertEquals(beforeSnapshot.getDockableIds(), ((DockingLayoutNode.Tabs) tree.snapshot()).getDockableIds());
    }

    @Test
    public void topAndBottomEdgeMovesUseVerticalOrientationAndExpectedOrder() {
        DockingTree topTree = new DockingTree();
        topTree.addToRoot(dockable("a"));
        topTree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        assertTrue(topTree.tryMove("a", new DockingTree.DropTarget("b", DockingPosition.TOP), 0.5));
        DockingLayoutNode.Split topRoot = (DockingLayoutNode.Split) topTree.snapshot();
        assertEquals(Orientation.VERTICAL, topRoot.getOrientation());
        assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) topRoot.getFirst()).getDockableIds());
        assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) topRoot.getSecond()).getDockableIds());

        DockingTree bottomTree = new DockingTree();
        bottomTree.addToRoot(dockable("a"));
        bottomTree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        assertTrue(bottomTree.tryMove("a", new DockingTree.DropTarget("b", DockingPosition.BOTTOM), 0.5));
        DockingLayoutNode.Split bottomRoot = (DockingLayoutNode.Split) bottomTree.snapshot();
        assertEquals(Orientation.VERTICAL, bottomRoot.getOrientation());
        assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) bottomRoot.getFirst()).getDockableIds());
        assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) bottomRoot.getSecond()).getDockableIds());
    }
}
