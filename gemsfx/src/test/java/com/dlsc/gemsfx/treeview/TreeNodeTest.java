package com.dlsc.gemsfx.treeview;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TreeNode}.
 */
public class TreeNodeTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        TreeNode<String> node = new TreeNode<>();
        assertNull(node.getValue());
    }

    @Test
    public void testConstructionWithValue() {
        TreeNode<String> node = new TreeNode<>("root");
        assertEquals("root", node.getValue());
    }

    @Test
    public void testValueRoundTrip() {
        TreeNode<String> node = new TreeNode<>();
        node.setValue("hello");
        assertEquals("hello", node.getValue());
    }

    @Test
    public void testDefaultParentIsNull() {
        TreeNode<String> node = new TreeNode<>("child");
        assertNull(node.getParent());
    }

    @Test
    public void testChildGetsParentSet() {
        TreeNode<String> parent = new TreeNode<>("parent");
        TreeNode<String> child = new TreeNode<>("child");
        parent.getChildren().add(child);
        assertEquals(parent, child.getParent());
    }

    @Test
    public void testChildParentClearedOnRemove() {
        TreeNode<String> parent = new TreeNode<>("parent");
        TreeNode<String> child = new TreeNode<>("child");
        parent.getChildren().add(child);
        parent.getChildren().remove(child);
        assertNull(child.getParent());
    }

    @Test
    public void testDefaultChildrenIsEmpty() {
        TreeNode<String> node = new TreeNode<>();
        assertTrue(node.getChildren().isEmpty());
    }

    @Test
    public void testMultipleChildren() {
        TreeNode<String> root = new TreeNode<>("root");
        root.getChildren().add(new TreeNode<>("a"));
        root.getChildren().add(new TreeNode<>("b"));
        assertEquals(2, root.getChildren().size());
    }

    @Test
    public void testDefaultExpandedIsTrue() {
        TreeNode<String> node = new TreeNode<>();
        assertTrue(node.isExpanded());
    }

    @Test
    public void testExpandedRoundTrip() {
        TreeNode<String> node = new TreeNode<>();
        node.setExpanded(false);
        assertFalse(node.isExpanded());
    }

    @Test
    public void testLinkedNodesDefaultEmpty() {
        TreeNode<String> node = new TreeNode<>();
        assertTrue(node.getLinkedNodes().isEmpty());
    }

    @Test
    public void testLinkedNodesCanBeAdded() {
        TreeNode<String> node = new TreeNode<>("n");
        TreeNode<String> other = new TreeNode<>("m");
        node.getLinkedNodes().add(other);
        assertEquals(1, node.getLinkedNodes().size());
    }

    @Test
    public void testNameRoundTrip() {
        TreeNode<String> node = new TreeNode<>();
        node.setName("myNode");
        assertEquals("myNode", node.getName());
    }

    @Test
    public void testDefaultWidthAndHeight() {
        TreeNode<String> node = new TreeNode<>();
        assertEquals(TreeNode.USE_TREE_CELL_SIZE, node.getWidth(), 0.001);
        assertEquals(TreeNode.USE_TREE_CELL_SIZE, node.getHeight(), 0.001);
    }
}
