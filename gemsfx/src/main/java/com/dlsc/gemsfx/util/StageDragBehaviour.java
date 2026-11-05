package com.dlsc.gemsfx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

/**
 * Allows dragging a node's containing {@link Stage} with the primary mouse button.
 * The stage is resolved when a drag starts, so the behaviour can be installed before
 * the node is attached to a scene or window. Nodes in non-stage windows are ignored.
 * Event filters are used without consuming events or replacing existing handlers.
 *
 * <pre>{@code
 * StageDragBehaviour dragging = StageDragBehaviour.install(titleBar);
 * dragging.enabledProperty().bind(draggingAllowed);
 * // When no longer needed:
 * dragging.uninstall();
 * }</pre>
 *
 * <p>Install, configure, and uninstall the behaviour on the JavaFX application thread.
 */
public class StageDragBehaviour {

    private static final Object INSTALLATION_KEY = new Object();

    private final Node node;
    private final EventHandler<MouseEvent> mousePressedHandler = this::onMousePressed;
    private final EventHandler<MouseEvent> mouseDraggedHandler = this::onMouseDragged;
    private final EventHandler<MouseEvent> mouseReleasedHandler = event -> {
        if (event.getButton() == MouseButton.PRIMARY) {
            clearDragState();
        }
    };
    private final ChangeListener<Scene> sceneListener = (obs, oldScene, newScene) -> clearDragState();
    private final ChangeListener<Window> windowListener = (obs, oldWindow, newWindow) -> clearDragState();
    private final WeakChangeListener<Window> weakWindowListener = new WeakChangeListener<>(windowListener);

    private final BooleanProperty enabled = new SimpleBooleanProperty(this, "enabled", true) {
        @Override
        protected void invalidated() {
            if (!get()) {
                clearDragState();
            }
        }
    };

    private Stage draggedStage;
    private Scene draggedScene;
    private double dragStartScreenX;
    private double dragStartScreenY;
    private double dragStartStageX;
    private double dragStartStageY;

    /**
     * Installs stage dragging on the given node, enabled by default.
     *
     * @param node the node to use as a drag handle
     * @return the installed behaviour
     * @throws NullPointerException if the node is null
     * @throws IllegalStateException if stage dragging is already installed on the node
     */
    public static StageDragBehaviour install(Node node) {
        Objects.requireNonNull(node, "Node cannot be null.");
        if (isInstalled(node)) {
            throw new IllegalStateException("StageDragBehaviour is already installed on this node.");
        }

        StageDragBehaviour behaviour = new StageDragBehaviour(node);
        node.getProperties().put(INSTALLATION_KEY, behaviour);
        return behaviour;
    }

    private StageDragBehaviour(Node node) {
        this.node = node;
        node.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        node.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        node.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        node.sceneProperty().addListener(sceneListener);
    }

    private void onMousePressed(MouseEvent event) {
        if (isEnabled() && event.getButton() == MouseButton.PRIMARY
                && node.getScene() != null && node.getScene().getWindow() instanceof Stage) {
            clearDragState();
            draggedScene = node.getScene();
            draggedScene.windowProperty().addListener(weakWindowListener);
            draggedStage = (Stage) node.getScene().getWindow();
            dragStartScreenX = event.getScreenX();
            dragStartScreenY = event.getScreenY();
            dragStartStageX = draggedStage.getX();
            dragStartStageY = draggedStage.getY();
        }
    }

    private void onMouseDragged(MouseEvent event) {
        if (draggedStage == null) {
            return;
        }

        if (isEnabled() && event.isPrimaryButtonDown()) {
            draggedStage.setX(dragStartStageX + event.getScreenX() - dragStartScreenX);
            draggedStage.setY(dragStartStageY + event.getScreenY() - dragStartScreenY);
        } else {
            clearDragState();
        }
    }

    private void clearDragState() {
        if (draggedScene != null) {
            draggedScene.windowProperty().removeListener(weakWindowListener);
            draggedScene = null;
        }
        draggedStage = null;
    }

    /**
     * Determines whether mouse dragging moves the containing stage.
     * The default value is {@code true}. Disabling cancels any active drag.
     *
     * @return the enabled property
     */
    public final BooleanProperty enabledProperty() {
        return enabled;
    }

    public final boolean isEnabled() {
        return enabled.get();
    }

    public final void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    /**
     * Checks whether stage dragging is installed on the given node.
     *
     * @param node the node to check
     * @return true if stage dragging is installed
     */
    public static boolean isInstalled(Node node) {
        return node.getProperties().containsKey(INSTALLATION_KEY);
    }

    /**
     * Checks whether this behaviour is still installed.
     *
     * @return true if this behaviour is installed
     */
    public boolean isInstalled() {
        return node.getProperties().get(INSTALLATION_KEY) == this;
    }

    /**
     * Removes this behaviour's event filters and cancels any active drag.
     * Calling this method more than once has no effect.
     */
    public void uninstall() {
        node.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        node.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
        node.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        node.sceneProperty().removeListener(sceneListener);
        clearDragState();
        if (isInstalled()) {
            node.getProperties().remove(INSTALLATION_KEY);
        }
    }
}
