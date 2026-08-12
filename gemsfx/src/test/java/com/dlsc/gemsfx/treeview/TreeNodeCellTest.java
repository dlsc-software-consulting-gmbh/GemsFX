package com.dlsc.gemsfx.treeview;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TreeNodeCell}.
 */
public class TreeNodeCellTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        assertNotNull(cell);
    }

    @Test
    public void testStyleClass() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        assertTrue(cell.getStyleClass().contains("tree-node-cell"));
    }

    @Test
    public void testConstructionWithItem() {
        TreeNodeCell<String> cell = invoke(() -> new TreeNodeCell<>("hello"));
        assertEquals("hello", cell.getItem());
    }

    @Test
    public void testItemRoundTrip() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        runFx(() -> cell.setItem("world"));
        assertEquals("world", cell.getItem());
    }

    @Test
    public void testDefaultExpandedIsTrue() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        assertTrue(cell.isExpanded());
    }

    @Test
    public void testExpandedRoundTrip() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        runFx(() -> cell.setExpanded(false));
        assertFalse(cell.isExpanded());
    }

    @Test
    public void testDefaultTreeNodeIsNull() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        assertNull(cell.getTreeNode());
    }

    @Test
    public void testTextRoundTrip() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        runFx(() -> cell.setText("label"));
        assertEquals("label", cell.getText());
    }

    @Test
    public void testGraphicCanBeSet() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        runFx(() -> cell.setGraphic(new javafx.scene.shape.Circle(5)));
        assertNotNull(cell.getGraphic());
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        TreeNodeCell<String> cell = invoke(TreeNodeCell::new);
        assertNotNull(cell.getUserAgentStylesheet());
    }

    @Test
    public void testTreeNodeBindsExpanded() {
        TreeNode<String> node = new TreeNode<>("n");
        node.setExpanded(false);
        TreeNodeCell<String> cell = invoke(() -> {
            TreeNodeCell<String> c = new TreeNodeCell<>();
            c.setTreeNode(node);
            return c;
        });
        assertFalse(cell.isExpanded());
    }
}
