package com.dlsc.gemsfx.treeview;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Objects;

/**
 * Default visual cell used by {@link TreeNodeView} to render a {@link TreeNode}.
 * <p>
 * The cell displays text and an optional graphic, binds its expanded state to the
 * associated tree node, and shows a disclosure arrow when children are available.
 *
 * @param <T> the type of the item represented by this cell
 */
public class TreeNodeCell<T> extends BorderPane {

    private static final boolean DEFAULT_EXPANDED = true;

    private static final String DEFAULT_STYLE_CLASS = "tree-node-cell";
    private static final PseudoClass EXPANDED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass COLLAPSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("collapsed");
    private InvalidationListener invalidationListener;
    private final Label innerLabel;
    private final StackPane arrowWrapper;

    /**
     * Creates an empty tree node cell.
     */
    public TreeNodeCell() {
        getStyleClass().setAll(DEFAULT_STYLE_CLASS);
        innerLabel = new Label();
        innerLabel.getStyleClass().add("tree-node-cell-label");
        innerLabel.graphicProperty().bind(graphicProperty());
        innerLabel.textProperty().bind(textProperty());
        innerLabel.setMaxWidth(Double.MAX_VALUE);

        Region arrow = new Region();
        arrow.getStyleClass().setAll("disclosure-arrow");

        arrowWrapper = new StackPane(arrow);
        arrowWrapper.getStyleClass().setAll("arrow-wrapper");
        arrowWrapper.managedProperty().bind(arrowWrapper.visibleProperty());
        arrowWrapper.visibleProperty().bind(Bindings.createBooleanBinding(() ->
                getTreeNode() != null && !getTreeNode().getChildren().isEmpty(), treeNodeProperty()));

        arrowWrapper.setOnMousePressed(event -> {
            if (getTreeNode() != null && !getTreeNode().getChildren().isEmpty()) {
                setExpanded(!isExpanded());
            }
            event.consume();
        });

        setCenter(innerLabel);
        setRight(arrowWrapper);

        itemProperty().addListener((ob, ov, nv) -> updateItem(nv));
        treeNodeProperty().addListener((ob, oldTreeNode, newTreeNode) -> {
            if (oldTreeNode != null && invalidationListener != null) {
                expandedProperty().unbindBidirectional(oldTreeNode.expandedProperty());
                oldTreeNode.getChildren().removeListener(invalidationListener);
            }
            if (newTreeNode != null) {
                expandedProperty().bindBidirectional(newTreeNode.expandedProperty());
                invalidationListener = it -> updateItem(getItem());
                newTreeNode.getChildren().addListener(invalidationListener);
            }
            updateItem(getItem());
        });

        expandedProperty().addListener((ob, ov, newExpanded) -> {
            pseudoClassStateChanged(EXPANDED_PSEUDOCLASS_STATE, newExpanded);
            pseudoClassStateChanged(COLLAPSED_PSEUDOCLASS_STATE, !newExpanded);
            updateItem(getItem());
        });

    }

    /**
     * Creates a tree node cell for the given item.
     *
     * @param item the item displayed by this cell
     */
    public TreeNodeCell(T item) {
        this();
        setItem(item);
    }

    private final ObjectProperty<T> item = new SimpleObjectProperty<>(this, "item");

    public final T getItem() {
        return item.get();
    }

    /**
     * The item property.
     *
     * @return the item property
     */
    public final ObjectProperty<T> itemProperty() {
        return item;
    }

    public final void setItem(T item) {
        this.item.set(item);
    }

    private final ReadOnlyObjectWrapper<TreeNode<T>> treeNode = new ReadOnlyObjectWrapper<>(this, "treeNode");

    public final TreeNode<T> getTreeNode() {
        return treeNode.get();
    }

    /**
     * The read-only tree node property.
     *
     * @return the tree node property
     */
    public final ReadOnlyObjectProperty<TreeNode<T>> treeNodeProperty() {
        return treeNode.getReadOnlyProperty();
    }

    protected final void setTreeNode(TreeNode<T> treeNode) {
        this.treeNode.set(treeNode);
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

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public final Node getGraphic() {
        return graphic.get();
    }

    /**
     * The graphic property.
     *
     * @return the graphic property
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public final String getText() {
        return text.get();
    }

    /**
     * The text property.
     *
     * @return the text property
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    /**
     * Updates this cell's displayed content for the given item.
     *
     * @param item the new item value
     */
    protected void updateItem(T item) {
        if (item != null) {
            setText(item.toString());
        } else {
            setText("");
        }
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(TreeNodeCell.class.getResource("tree-view.css")).toExternalForm();
    }
}
