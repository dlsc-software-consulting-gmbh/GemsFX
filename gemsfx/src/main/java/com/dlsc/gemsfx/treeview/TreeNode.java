package com.dlsc.gemsfx.treeview;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A model node used by {@link TreeNodeView} to describe a tree hierarchy.
 * <p>
 * Each node stores a value, parent-child relationships, optional additional linked nodes,
 * expansion state, and optional dimensions used by the view when laying out cells.
 *
 * @param <T> the type of the value stored in the node
 */
public class TreeNode<T> {

    private static final boolean DEFAULT_EXPANDED = true;
    /**
     * Sentinel value indicating that the view's default cell width or height should be used.
     */
    public static final double USE_TREE_CELL_SIZE = Double.NEGATIVE_INFINITY;
    private static final double DEFAULT_WIDTH = USE_TREE_CELL_SIZE;
    private static final double DEFAULT_HEIGHT = USE_TREE_CELL_SIZE;

    /**
     * Creates an empty tree node.
     */
    public TreeNode() {
        children.addListener((ListChangeListener.Change<? extends TreeNode<T>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(each -> each.setParent(this));
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(each -> each.setParent(null));
                }
            }
        });
    }

    /**
     * Creates a tree node with the given value.
     *
     * @param value the value stored in this node
     */
    public TreeNode(T value) {
        this();
        setValue(value);
    }

    /**
     * Represents the name identifier for this node.
     * ------------------------------------------------
     * The primary purpose of the 'name' is to assist in determining the style of the node and its links.
     * For instance:
     * 1. If the node's name is 'n', then the style class for the node would be 'node-n'.
     * 2. For a regular parent-child relationship, if a node with name 'n' has a parent named 'm',
     *    the link connecting them will have a style class 'link-m-n'.
     * 3. For any extra links, say from node 'p' to node 'n', the style class for the link would be 'link-extra-p-n'.
     * ------------------------------------------------
     * Important considerations:
     * - If the 'name' is null, all the above rules become invalid.
     * - Both the relevant node and the current node must possess non-null 'name' values for these styling rules to apply.
     * ------------------------------------------------
     * This naming convention aids in providing a systematic approach for styling, making it directly
     * relatable to the node's relationship and connection type.
     */
    private String name;

    /**
     * Returns the optional name used for styling this node and its links.
     *
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the optional name used for styling this node and its links.
     *
     * @param name the node name
     */
    public void setName(String name) {
        this.name = name;
    }

    private final ReadOnlyObjectWrapper<TreeNode<T>> parent = new ReadOnlyObjectWrapper<>(this, "parent", null);

    /**
     * The read-only parent property.
     *
     * @return the parent property
     */
    public final ReadOnlyObjectProperty<TreeNode<T>> parentProperty() {
        return parent.getReadOnlyProperty();
    }

    protected final void setParent(TreeNode<T> parent) {
        this.parent.set(parent);
    }

    public final TreeNode<T> getParent() {
        return parentProperty().get();
    }


    /**
     * This list holds the child nodes of the current node in the tree hierarchy.
     * Each child node in this list is directly connected to and descends from the current node.
     * It represents the default connectivity in the tree structure, where parent nodes
     * are intrinsically linked to their children.
     */
    private final ObservableList<TreeNode<T>> children = FXCollections.observableArrayList();

    /**
     * Returns the observable list of child nodes.
     *
     * @return the child nodes
     */
    public ObservableList<TreeNode<T>> getChildren() {
        return children;
    }

    /**
     * This list holds references to nodes that are directly linked or associated with the current node.
     * Unlike the 'children' list, which represents child nodes in a hierarchical structure,
     * the 'linkedNodes' list represents peers or other related nodes that have a specific connection
     * or relationship with this node, but are not necessarily its descendants in the tree hierarchy.
     */
    private final ObservableList<TreeNode<T>> linkedNodes = FXCollections.observableArrayList();

    /**
     * Returns the observable list of additional nodes linked to this node.
     *
     * @return the linked nodes
     */
    public ObservableList<TreeNode<T>> getLinkedNodes() {
        return linkedNodes;
    }

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", DEFAULT_EXPANDED);

    public final boolean isExpanded() {
        return expanded.get();
    }

    /**
     * The expanded property.
     *
     * @return the expanded property
     */
    public final BooleanProperty expandedProperty() {
        return expanded;
    }

    public final void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value", null);

    public final T getValue() {
        return valueProperty().get();
    }

    /**
     * The value property.
     *
     * @return the value property
     */
    public final ObjectProperty<T> valueProperty() {
        return value;
    }

    public final void setValue(T value) {
        valueProperty().set(value);
    }

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", DEFAULT_WIDTH);

    public final double getWidth() {
        return width.get();
    }

    /**
     * The preferred width property.
     * <p>
     * A value of {@link #USE_TREE_CELL_SIZE} lets the view use its default cell width.
     *
     * @return the width property
     */
    public final DoubleProperty widthProperty() {
        return width;
    }

    public final void setWidth(double width) {
        this.width.set(width);
    }

    private final DoubleProperty height = new SimpleDoubleProperty(this, "height", DEFAULT_HEIGHT);

    public final double getHeight() {
        return height.get();
    }

    /**
     * The preferred height property.
     * <p>
     * A value of {@link #USE_TREE_CELL_SIZE} lets the view use its default cell height.
     *
     * @return the height property
     */
    public final DoubleProperty heightProperty() {
        return height;
    }

    public final void setHeight(double height) {
        this.height.set(height);
    }

    /**
     * Sets the preferred width and height of this node.
     *
     * @param width the preferred node width
     * @param height the preferred node height
     */
    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * Checks whether this node has no children.
     *
     * @return true if the node is a leaf node
     */
    public boolean isLeaf() {
        return this.children.isEmpty();
    }

    /**
     * Get the depth of the node in the tree. The depth of the root node is 0.
     *
     * @return the depth of the node in the tree
     */
    public int getDepth() {
        if (this.getParent() == null) {
            return 0;
        } else {
            return 1 + this.getParent().getDepth();
        }
    }

    /**
     * Returns the level of this node in the tree.
     *
     * @return the level of this node, equivalent to {@link #getDepth()}
     */
    public int getLevel() {
        return getDepth();
    }

    /**
     * If there is no parent node, it is the root node
     *
     * @return true if the node is the root node of the tree
     */
    public boolean isRoot() {
        return this.getParent() == null;
    }

    /**
     * Returns the last child node.
     *
     * @return the last node in the child node list, or {@code null} if this node has no children
     */
    public TreeNode<T> getLastChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    /**
     * Checks whether this node is the last child of its parent.
     *
     * @return true if this node is the last node of the parent
     */
    public boolean isLastChild() {
        if (getParent() == null || getParent().getChildren().isEmpty()) {
            return false;
        }
        return this == getParent().getLastChild();
    }

    /**
     * Checks whether this node is the first child of its parent.
     *
     * @return true if this node is the first node of the parent
     */
    public boolean isFirstChild() {
        if (getParent() == null || getParent().getChildren().isEmpty()) {
            return false;
        }
        return this == getParent().getFirstChild();
    }

    /**
     * Returns the first child node.
     *
     * @return the first node in the child node list, or {@code null} if this node has no children
     */
    public TreeNode<T> getFirstChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    /**
     * Checks whether any ancestor of this node is collapsed.
     *
     * @return true if one of this node's ancestors is collapsed
     */
    public boolean isAncestorCollapsed() {
        TreeNode<T> parentNode = this.getParent();
        while (parentNode != null) {
            //collapsed
            if (!parentNode.isExpanded()) {
                return true;
            }
            parentNode = parentNode.getParent();
        }
        return false;
    }

    /**
     * Creates a breadth-first stream starting with this node.
     *
     * @return a stream traversing this node and its descendants
     */
    public Stream<TreeNode<T>> stream() {
        return StreamSupport.stream(new TreeNodeSpliterator<>(this), false);
    }

    private static class TreeNodeSpliterator<T> implements Spliterator<TreeNode<T>> {
        private final Queue<TreeNode<T>> queue = new LinkedList<>();

        TreeNodeSpliterator(TreeNode<T> root) {
            queue.add(root);
        }

        @Override
        public boolean tryAdvance(Consumer<? super TreeNode<T>> action) {
            TreeNode<T> node = queue.poll();
            if (node != null) {
                action.accept(node);
                queue.addAll(node.getChildren());
                return true;
            }
            return false;
        }

        @Override
        public Spliterator<TreeNode<T>> trySplit() {
            return null;
        }

        @Override
        public long estimateSize() {
            return Long.MAX_VALUE;
        }

        @Override
        public int characteristics() {
            return ORDERED;
        }
    }

    @Override
    public String toString() {
        return "TreeNode{"
                + "value="
                + getValue()
                + '}';
    }

}
