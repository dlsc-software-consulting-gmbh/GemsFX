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
    public void resolvesEveryMultiChildIntervalIncludingAfterACollapsedGroup() {
        List<DockingLayoutNode> children = List.of(
                new DockingLayoutNode.Tabs(List.of("a"), "a"),
                new DockingLayoutNode.Tabs(List.of("collapsed"), "collapsed"),
                new DockingLayoutNode.Tabs(List.of("b"), "b"),
                new DockingLayoutNode.Tabs(List.of("c"), "c"));
        double[] starts = {0, 40, 140};
        double[] sizes = {40, 100, 60};
        for (Orientation orientation : Orientation.values()) {
            DockingLayoutNode.Split root = new DockingLayoutNode.Split(orientation, children, List.of(0.2, 0.2, 0.7));
            for (int i = 0; i < starts.length; i++) {
                boolean horizontal = orientation == Orientation.HORIZONTAL;
                double point = starts[i] + sizes[i] / 2;
                DockingTree.ResolvedDropTarget target = DockingTree.resolveDropTarget(root,
                        horizontal ? point : 100, horizontal ? 100 : point, 200, 200, null);
                assertEquals(List.of("a", "b", "c").get(i), target.getTargetId());
                assertEquals(DockingPosition.CENTER, target.getPosition());
                assertEquals(starts[i], horizontal ? target.getLeafX() : target.getLeafY(), 1e-9);
                assertEquals(sizes[i], horizontal ? target.getLeafWidth() : target.getLeafHeight(), 1e-9);
            }
        }
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
        assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) topRoot.getChildren().get(0)).getDockableIds());
        assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) topRoot.getChildren().get(1)).getDockableIds());

        DockingTree bottomTree = new DockingTree();
        bottomTree.addToRoot(dockable("a"));
        bottomTree.splitAt("a", dockable("b"), DockingPosition.RIGHT, 0.5);
        assertTrue(bottomTree.tryMove("a", new DockingTree.DropTarget("b", DockingPosition.BOTTOM), 0.5));
        DockingLayoutNode.Split bottomRoot = (DockingLayoutNode.Split) bottomTree.snapshot();
        assertEquals(Orientation.VERTICAL, bottomRoot.getOrientation());
        assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) bottomRoot.getChildren().get(0)).getDockableIds());
        assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) bottomRoot.getChildren().get(1)).getDockableIds());
    }
}
