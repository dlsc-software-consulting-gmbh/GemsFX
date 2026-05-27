package com.dlsc.gemsfx.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;

import java.util.Objects;

/**
 * Utility class providing focus-related helper methods for JavaFX nodes.
 *
 * <p>This class contains static methods for requesting and delegating keyboard
 * focus in a safe way, ensuring that focus requests are deferred until the target
 * node is actually showing in the scene graph.
 */
public class FocusUtil {

    private FocusUtil() {
    }

    /**
     * Requests focus on the specified node if the node is visible in the tree hierarchy.
     * The node will receive the keyboard focus if it is currently showing.
     *
     * @param node The node to request focus on.
     */
    public static void requestFocus(Node node) {
        TreeShowing.treeShowing(node).addListener(it -> {
            if (TreeShowing.isTreeShowing(node)) {
                Platform.runLater(node::requestFocus);
            }
        });
        if (TreeShowing.isTreeShowing(node)) {
            Platform.runLater(node::requestFocus);
        }
    }

    /**
     * Delegates the focus from one node to another.
     *
     * @param receiver the node receiving the initial focus
     * @param delegate the node that should receive the delegated focus
     */
    public static void delegateFocus(Node receiver, Node delegate) {
        Objects.requireNonNull(receiver, "receiving node can not be null");
        Objects.requireNonNull(receiver, "delegate node can not be null");
        receiver.focusedProperty().addListener(it -> {
            if (receiver.isFocused() && TreeShowing.isTreeShowing(delegate)) {
                Platform.runLater(delegate::requestFocus);
            }
        });
    }

    /**
     * Finds the first FocusTraversable node in the tree hierarchy of a given node.
     *
     * @param node The node to start the search from.
     * @return The first focusable node found, or null if no focusable node is found.
     */
    public static Node findFirstFocusableNode(Node node) {
        if (node.isFocusTraversable()) {
            return node;
        }

        if (node instanceof Parent) {
            Parent parent = (Parent) node;
            for (Node tempNode : parent.getChildrenUnmodifiable()) {
                if (tempNode.isFocusTraversable()) {
                    return tempNode;
                } else if (tempNode instanceof Parent) {
                    Node firstFocusableNode = findFirstFocusableNode(tempNode);
                    if (firstFocusableNode != null) {
                        return firstFocusableNode;
                    }
                }
            }
        }

        return null;
    }

    /**
     * Requests focus on the first focusable node below the given node.
     *
     * @param node the node used to start the search
     */
    public static void focusOnFirstFocusableNode(Node node) {
        Node firstFocusableNode = findFirstFocusableNode(node);
        if (firstFocusableNode != null) {
            Platform.runLater(firstFocusableNode::requestFocus);
        }
    }
}
