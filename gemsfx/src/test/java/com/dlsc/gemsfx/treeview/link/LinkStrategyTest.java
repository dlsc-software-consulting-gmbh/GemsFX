package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.FxTestBase;
import com.dlsc.gemsfx.treeview.TreeNode;
import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Verifies that all {@link LinkStrategy} implementations shipped with the tree node view
 * produce usable connection nodes for every supported layout direction.
 */
public class LinkStrategyTest extends FxTestBase {

    private static final Point2D PARENT_POINT = new Point2D(100, 50);
    private static final Point2D CHILD_POINT = new Point2D(220, 180);

    private static List<Supplier<AbstractLinkStrategy<String>>> strategies() {
        return Arrays.asList(
                StraightLineLink::new,
                CurvedLineLink::new,
                PolyLineLink::new,
                QuadCurveLink::new,
                LogarithmicLink::new,
                SineWaveDecayLink::new,
                SimpleCatmullRomLink::new,
                ClockHandLinkStrategy::new);
    }

    private static ArrayList<Node> draw(AbstractLinkStrategy<String> strategy, TreeNodeView.LayoutDirection direction) {
        return invoke(() -> {
            TreeNode<String> parent = new TreeNode<>("parent");
            TreeNode<String> child = new TreeNode<>("child");
            parent.getChildren().add(child);
            return strategy.drawNodeLink(direction, 200, parent, PARENT_POINT, 80, 30,
                    child, CHILD_POINT, 80, 30, 5, 20, 10);
        });
    }

    @Test
    public void everyStrategyDrawsLinksForEveryDirection() {
        for (Supplier<AbstractLinkStrategy<String>> supplier : strategies()) {
            for (TreeNodeView.LayoutDirection direction : TreeNodeView.LayoutDirection.values()) {
                AbstractLinkStrategy<String> strategy = supplier.get();
                ArrayList<Node> nodes = draw(strategy, direction);

                String name = strategy.getClass().getSimpleName() + " / " + direction;
                assertNotNull("No nodes returned by " + name, nodes);
                assertFalse("No nodes returned by " + name, nodes.isEmpty());
                for (Node node : nodes) {
                    assertNotNull("Null node returned by " + name, node);
                }
            }
        }
    }

    @Test
    public void strategiesCreateFreshNodesForEveryInvocation() {
        for (Supplier<AbstractLinkStrategy<String>> supplier : strategies()) {
            AbstractLinkStrategy<String> strategy = supplier.get();
            ArrayList<Node> first = draw(strategy, TreeNodeView.LayoutDirection.TOP_TO_BOTTOM);
            ArrayList<Node> second = draw(strategy, TreeNodeView.LayoutDirection.TOP_TO_BOTTOM);

            assertFalse(first.isEmpty());
            assertFalse(second.isEmpty());
            // a node can only ever have one parent, hence every call must return new nodes
            assertTrue("Strategy " + strategy.getClass().getSimpleName() + " reuses nodes",
                    first.get(0) != second.get(0));
        }
    }

    @Test
    public void endPointsDependOnTheLayoutDirection() {
        StraightLineLink<String> strategy = new StraightLineLink<>();

        strategy.calculateEndPoints(TreeNodeView.LayoutDirection.TOP_TO_BOTTOM,
                PARENT_POINT, 80, 30, CHILD_POINT, 80, 30, 5);
        double topToBottomStartY = strategy.startY;

        strategy.calculateEndPoints(TreeNodeView.LayoutDirection.BOTTOM_TO_TOP,
                PARENT_POINT, 80, 30, CHILD_POINT, 80, 30, 5);
        double bottomToTopStartY = strategy.startY;

        // top to bottom starts below the parent, bottom to top starts above it
        assertTrue(topToBottomStartY > bottomToTopStartY);
    }
}
