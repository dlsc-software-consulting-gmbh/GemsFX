package com.dlsc.gemsfx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Window;

/**
 * Provides access to a derived {@code "treeShowing"} property for any {@link Node}.
 *
 * <p>JavaFX does not expose a public API that tells you whether a node is
 * currently attached to a visible window. This class fills that gap: the
 * {@link BooleanProperty} returned by {@link #treeShowing(Node)} is
 * {@code true} whenever the node's scene is non-null, the scene has a
 * non-null window, and that window is showing.
 *
 * <p>A single property instance is created per node and cached in the node's
 * {@linkplain Node#getProperties() property map}, so repeated calls for the
 * same node are cheap.
 *
 * <p>Typical use-case: stop animations or background services as soon as a
 * node is removed from the scene graph, preventing memory leaks.
 *
 * <pre>{@code
 * TreeShowing.treeShowing(myNode).addListener((obs, wasShowing, isShowing) -> {
 *     if (isShowing) {
 *         myTimeline.play();
 *     } else {
 *         myTimeline.stop();
 *     }
 * });
 * if (TreeShowing.isTreeShowing(myNode)) {
 *     myTimeline.play();
 * }
 * }</pre>
 */
public final class TreeShowing {

    private static final Object KEY = new Object();

    private TreeShowing() {
    }

    /**
     * Returns the cached {@code treeShowing} property for {@code node},
     * creating it on first access.
     *
     * @param node the node to observe; must not be {@code null}
     * @return a {@link BooleanProperty} that reflects whether the node is
     *         part of a visible window
     */
    public static BooleanProperty treeShowing(Node node) {
        BooleanProperty prop = (BooleanProperty) node.getProperties().get(KEY);
        if (prop == null) {
            prop = new TreeShowingProperty(node);
            node.getProperties().put(KEY, prop);
        }
        return prop;
    }

    /**
     * Convenience method that checks the current state without creating a
     * long-lived property object.
     *
     * @param node the node to check; must not be {@code null}
     * @return {@code true} if the node is attached to a showing window
     */
    public static boolean isTreeShowing(Node node) {
        return node.getScene() != null
                && node.getScene().getWindow() != null
                && node.getScene().getWindow().isShowing();
    }

    // -------------------------------------------------------------------------
    // Property implementation
    // -------------------------------------------------------------------------

    private static final class TreeShowingProperty extends SimpleBooleanProperty {

        private Scene trackedScene;
        private Window trackedWindow;

        TreeShowingProperty(Node node) {
            super(node, "treeShowing", false);

            Runnable updateValue = () -> {
                Scene s = node.getScene();
                set(s != null && s.getWindow() != null && s.getWindow().isShowing());
            };

            ChangeListener<Boolean> onShowingChanged = (p, o, v) -> updateValue.run();

            ChangeListener<Window> onWindowChanged = (p, o, ignored) -> {
                Scene s = node.getScene();
                Window newWindow = (s == null) ? null : s.getWindow();

                if (trackedWindow != null) {
                    trackedWindow.showingProperty().removeListener(onShowingChanged);
                }
                trackedWindow = newWindow;

                if (newWindow != null) {
                    newWindow.showingProperty().addListener(onShowingChanged);
                    onShowingChanged.changed(null, null, newWindow.isShowing());
                } else {
                    updateValue.run();
                }
            };

            ChangeListener<Scene> onSceneChanged = (p, o, ignored) -> {
                Scene newScene = node.getScene();

                if (trackedScene != null) {
                    trackedScene.windowProperty().removeListener(onWindowChanged);
                }
                trackedScene = newScene;

                if (newScene != null) {
                    newScene.windowProperty().addListener(onWindowChanged);
                    onWindowChanged.changed(null, null, newScene.getWindow());
                } else {
                    onWindowChanged.changed(null, null, null);
                    updateValue.run();
                }
            };

            node.sceneProperty().addListener(onSceneChanged);
            onSceneChanged.changed(null, null, node.getScene());
        }
    }
}
