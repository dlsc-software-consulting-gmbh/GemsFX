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

public class TreeNodeCell<T> extends BorderPane {

    private static final boolean DEFAULT_EXPANDED = true;

    private static final String DEFAULT_STYLE_CLASS = "tree-node-cell";
    private static final PseudoClass EXPANDED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("expanded");
    private static final PseudoClass COLLAPSED_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("collapsed");
    private InvalidationListener invalidationListener;
    private final Label innerLabel;
    private final StackPane arrowWrapper;

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

    public TreeNodeCell(T item) {
        this();
        setItem(item);
    }

    private final ObjectProperty<T> item = new SimpleObjectProperty<>(this, "item");

    public T getItem() {
        return item.get();
    }

    public ObjectProperty<T> itemProperty() {
        return item;
    }

    public void setItem(T item) {
        this.item.set(item);
    }

    private final ReadOnlyObjectWrapper<TreeNode<T>> treeNode = new ReadOnlyObjectWrapper<>(this, "treeNode");

    public TreeNode<T> getTreeNode() {
        return treeNode.get();
    }

    public ReadOnlyObjectProperty<TreeNode<T>> treeNodeProperty() {
        return treeNode.getReadOnlyProperty();
    }

    protected void setTreeNode(TreeNode<T> treeNode) {
        this.treeNode.set(treeNode);
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

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic");

    public Node getGraphic() {
        return graphic.get();
    }

    public ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public String getText() {
        return text.get();
    }

    public StringProperty textProperty() {
        return text;
    }

    public void setText(String text) {
        this.text.set(text);
    }

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
