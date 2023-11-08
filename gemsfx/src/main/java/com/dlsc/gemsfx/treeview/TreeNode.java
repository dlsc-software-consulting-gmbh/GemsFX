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

public class TreeNode<T> {

    private static final boolean DEFAULT_EXPANDED = true;
    public static final double USE_TREE_CELL_SIZE = Double.NEGATIVE_INFINITY;
    private static final double DEFAULT_WIDTH = USE_TREE_CELL_SIZE;
    private static final double DEFAULT_HEIGHT = USE_TREE_CELL_SIZE;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private final ReadOnlyObjectWrapper<TreeNode<T>> parent = new ReadOnlyObjectWrapper<>(this, "parent", null);

    public ReadOnlyObjectProperty<TreeNode<T>> parentProperty() {
        return parent.getReadOnlyProperty();
    }

    protected void setParent(TreeNode<T> parent) {
        this.parent.set(parent);
    }

    public TreeNode<T> getParent() {
        return parentProperty().get();
    }


    /**
     * This list holds the child nodes of the current node in the tree hierarchy.
     * Each child node in this list is directly connected to and descends from the current node.
     * It represents the default connectivity in the tree structure, where parent nodes
     * are intrinsically linked to their children.
     */
    private final ObservableList<TreeNode<T>> children = FXCollections.observableArrayList();

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

    public ObservableList<TreeNode<T>> getLinkedNodes() {
        return linkedNodes;
    }

    private final BooleanProperty expanded = new SimpleBooleanProperty(this, "expanded", DEFAULT_EXPANDED);

    public boolean isExpanded() {
        return expanded.get();
    }

    public BooleanProperty expandedProperty() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded.set(expanded);
    }

    private final ObjectProperty<T> value = new SimpleObjectProperty<>(this, "value", null);

    public T getValue() {
        return valueProperty().get();
    }

    public ObjectProperty<T> valueProperty() {
        return value;
    }

    public void setValue(T value) {
        valueProperty().set(value);
    }

    private final DoubleProperty width = new SimpleDoubleProperty(this, "width", DEFAULT_WIDTH);

    public double getWidth() {
        return width.get();
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    private final DoubleProperty height = new SimpleDoubleProperty(this, "height", DEFAULT_HEIGHT);

    public double getHeight() {
        return height.get();
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public void setSize(double width, double height) {
        setWidth(width);
        setHeight(height);
    }

    /**
     * @return true: if the node is a leaf node
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
     * @return The last node in the child node list.
     */
    public TreeNode<T> getLastChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(this.children.size() - 1);
        }
        return null;
    }

    /**
     * @return true: if this node is the last node of the parent
     */
    public boolean isLastChild() {
        if (getParent() == null || getParent().getChildren().isEmpty()) {
            return false;
        }
        return this == getParent().getLastChild();
    }

    /**
     * @return true: if this node is the first node of the parent
     */
    public boolean isFirstChild() {
        if (getParent() == null || getParent().getChildren().isEmpty()) {
            return false;
        }
        return this == getParent().getFirstChild();
    }

    /**
     * @return The first node in the child node list.
     */
    public TreeNode<T> getFirstChild() {
        if (!this.children.isEmpty()) {
            return this.children.get(0);
        }
        return null;
    }

    /**
     * @return true if the node is parent is collapsed
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
     * Convenient for traversing tree nodes.
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
