package com.dlsc.gemsfx.treeview.link;

import com.dlsc.gemsfx.treeview.TreeNode;
import com.dlsc.gemsfx.treeview.TreeNodeView;
import javafx.geometry.Point2D;
import javafx.scene.Node;

import java.util.ArrayList;

/**
 * Strategy interface for rendering visual links between {@link com.dlsc.gemsfx.treeview.TreeNode} instances.
 * <p>
 * Implementations receive the already computed node positions and dimensions and return JavaFX nodes
 * that represent the connection between a parent node and a child or additionally linked node.
 *
 * @param <T> the type of the value stored in each tree node
 */
public interface LinkStrategy<T> {
    /**
     * Draw node/path connections
     *
     * @param direction   the layout direction
     * @param maxDimensionInLine max dimension in line; if left-to-right or right-to-left, it is width; if top-to-bottom or bottom-to-top, it is height
     * @param parent      parent node
     * @param parentPoint parent node position
     * @param parentW     parent node width
     * @param parentH     parent node height
     * @param child       child node
     * @param childPoint  child node position
     * @param childW      child node width
     * @param childH      child node height
     * @param nodeLineGap node connections gap
     * @param vgap        vertical gap: distance between parent and child
     * @param hgap        horizontal gap: distance between child and child (same level/row)
     * @return Node/Path connections; these nodes may be lines and arrows; (The returned nodes should have both layoutX and layoutY already set.)
     */
    ArrayList<Node> drawNodeLink(TreeNodeView.LayoutDirection direction, double maxDimensionInLine, TreeNode<T> parent, Point2D parentPoint, double parentW, double parentH, TreeNode<T> child, Point2D childPoint, double childW, double childH, double nodeLineGap, double vgap, double hgap);

}