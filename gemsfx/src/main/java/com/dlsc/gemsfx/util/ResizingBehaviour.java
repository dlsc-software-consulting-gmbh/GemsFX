package com.dlsc.gemsfx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * This class implements interactive resizing behavior for a {@link Region}. It allows
 * the user to resize the region by pressed and dragging the region's edges. The resizing
 * behavior can be attached to any Region, modifying its preferred width and height as well
 * as its layout coordinates based on user interactions.
 */
public class ResizingBehaviour {

    private static final String RESIZE_BEHAVIOUR_INSTALLED = "resizeBehaviourInstalled";

    private final EventHandler<MouseEvent> mouseMovedHandler;
    private final EventHandler<MouseEvent> mousePressedHandler;
    private final EventHandler<MouseEvent> mouseReleasedHandler;
    private final EventHandler<MouseEvent> mouseDraggedHandler;

    private final Region region;

    private double startX;
    private double startY;

    /*
     * List of possible resizing operations.
     */
    public enum Operation {
        RESIZE_N,
        RESIZE_S,
        RESIZE_W,
        RESIZE_E,
        RESIZE_NW,
        RESIZE_NE,
        RESIZE_SW,
        RESIZE_SE
    }

    private Operation operation;

    /**
     * Installs the resizing behaviour on the given region. Once installed
     * the user will be able to resize the given region (depending on the container
     * the region lives in and the layout algorithms used by that container).
     *
     * @param region the region to support resizing
     * @return the installed behaviour
     */
    public static ResizingBehaviour install(Region region) {
        Objects.requireNonNull(region, "Region cannot be null.");

        if (isInstalled(region)) {
            throw new IllegalStateException("ResizingBehaviour is already installed on this region.");
        }

        ResizingBehaviour behaviour = new ResizingBehaviour(region);
        region.getProperties().put(RESIZE_BEHAVIOUR_INSTALLED, Boolean.TRUE);
        return behaviour;
    }

    /**
     * Constructs a new pane with the given children.
     */
    private ResizingBehaviour(Region region) {
        this.region = region;

        mouseMovedHandler = this::onMouseMove;
        mousePressedHandler = this::onMousePressed;
        mouseReleasedHandler = evt -> operation = null;
        mouseDraggedHandler = this::onMouseDragged;

        region.addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        region.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseMovedHandler);
        region.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, mouseMovedHandler);

        region.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        region.addEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        region.addEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);
    }

    private void onMouseDragged(MouseEvent evt) {
        if (operation == null) {
            return;
        }

        double x = evt.getScreenX();
        double y = evt.getScreenY();

        double deltaX = (evt.getScreenX() - startX) * 4;
        double deltaY = (evt.getScreenY() - startY) * 4;

        double width = region.getWidth();
        double height = region.getHeight();

        double minHeight = region.minHeight(width);
        double maxHeight = region.maxHeight(width);

        double minWidth = region.minWidth(height);
        double maxWidth = region.maxWidth(height);

        double newHeight;
        double newWidth;

        switch (operation) {
            case RESIZE_N:
                newHeight = height - deltaY;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setLayoutY(y);
                    region.setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                    startX = x;
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_S:
                newHeight = height + deltaY;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                    startX = x;
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_W:
                newWidth = width - deltaX;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setLayoutX(x);
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                    startX = x;
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_E:
                newWidth = width + deltaX;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                    startX = x;
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_NW:
                newWidth = width - deltaX;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setLayoutX(x);
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                    startX = x;
                }
                newHeight = height - deltaY;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setLayoutY(y);
                    region.setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_NE:
                newWidth = width + deltaX;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                    startX = x;
                }

                newHeight = height - deltaY;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setLayoutY(y);
                    region.setPrefHeight(Math.min(maxHeight, Math.max(minHeight, height - deltaY)));
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_SW:
                newWidth = width - deltaX;
                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setLayoutX(x);
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, width - deltaX)));
                    startX = x;
                }

                newHeight = height + deltaY;
                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setPrefHeight(Math.min(newHeight, Math.max(minHeight, height + deltaY)));
                    startY = y;
                }
                evt.consume();
                break;
            case RESIZE_SE:
                newWidth = width + deltaX;

                if (newWidth >= minWidth && newWidth <= maxWidth) {
                    region.setLayoutX(x);
                    region.setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                    startX = x;
                }

                newHeight = height + deltaY;

                if (newHeight >= minHeight && newHeight <= maxHeight) {
                    region.setPrefHeight(Math.max(minHeight, newHeight));
                    startY = y;
                }
                evt.consume();
                break;
        }

        BiConsumer<Double, Double> onResize = getOnResize();
        if (onResize != null) {
            onResize.accept(region.getWidth(), region.getHeight());
        }
    }

    private void onMousePressed(MouseEvent evt) {
        if (!isResizable()) {
            return;
        }

        startX = evt.getScreenX();
        startY = evt.getScreenY();

        double x = evt.getX();
        double y = evt.getY();

        final double offset = getOffset();

        if (x < offset) {
            if (y < offset) {
                setOperationIfSupported(Operation.RESIZE_NW);
            } else if (y > region.getHeight() - offset) {
                setOperationIfSupported(Operation.RESIZE_SW);
            } else {
                setOperationIfSupported(Operation.RESIZE_W);
            }
        } else if (x > region.getWidth() - offset) {
            if (y < offset) {
                setOperationIfSupported(Operation.RESIZE_NE);
            } else if (y > region.getHeight() - offset) {
                setOperationIfSupported(Operation.RESIZE_SE);
            } else {
                setOperationIfSupported(Operation.RESIZE_E);
            }
        } else if (y < offset) {
            setOperationIfSupported(Operation.RESIZE_N);
        } else if (y > region.getHeight() - offset) {
            setOperationIfSupported(Operation.RESIZE_S);
        } else {
            operation = null;
        }
    }

    private void setOperationIfSupported(Operation resizeNw) {
        operation = getSupportedOperations().contains(resizeNw) ? resizeNw : null;
    }

    private void onMouseMove(MouseEvent evt) {
        if (!isResizable()) {
            return;
        }

        double x = evt.getX();
        double y = evt.getY();

        final double offset = getOffset();

        if (x < offset) {
            if (y < offset) {
                setCursorIfOperationSupported(Operation.RESIZE_NW, Cursor.NW_RESIZE);
            } else if (y > region.getHeight() - offset) {
                setCursorIfOperationSupported(Operation.RESIZE_SW, Cursor.SW_RESIZE);
            } else {
                setCursorIfOperationSupported(Operation.RESIZE_W, Cursor.W_RESIZE);
            }
        } else if (x > region.getWidth() - offset) {
            if (y < offset) {
                setCursorIfOperationSupported(Operation.RESIZE_NE, Cursor.NE_RESIZE);
            } else if (y > region.getHeight() - offset) {
                setCursorIfOperationSupported(Operation.RESIZE_SE, Cursor.SE_RESIZE);
            } else {
                setCursorIfOperationSupported(Operation.RESIZE_E, Cursor.E_RESIZE);
            }
        } else if (y < offset) {
            setCursorIfOperationSupported(Operation.RESIZE_N, Cursor.N_RESIZE);
        } else if (y > region.getHeight() - offset) {
            setCursorIfOperationSupported(Operation.RESIZE_S, Cursor.S_RESIZE);
        } else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    private void setCursorIfOperationSupported(Operation operation, Cursor cursor) {
        region.setCursor(getSupportedOperations().contains(operation) ? cursor : Cursor.DEFAULT);
    }

    private ObjectProperty<BiConsumer<Double, Double>> onResize;

    public final BiConsumer<Double, Double> getOnResize() {
        return onResize == null ? null : onResize.get();
    }

    /**
     * A callback that will be invoked whenever the region is resized.
     *
     * @return the onResize callback
     */
    public final ObjectProperty<BiConsumer<Double, Double>> onResizeProperty() {
        if (onResize == null) {
            onResize = new SimpleObjectProperty<>(this, "onResize");
        }
        return onResize;
    }

    public final void setOnResize(BiConsumer<Double, Double> onResize) {
        onResizeProperty().set(onResize);
    }

    private DoubleProperty offset;

    public final double getOffset() {
        return offset == null ? 5 : offset.get();
    }

    /**
     * The offset from the edges in pixels where the user will be able to perform a press
     * and drag to resize the pane. Default is 5.
     */
    public final DoubleProperty offsetProperty() {
        if (offset == null) {
            offset = new SimpleDoubleProperty(this, "offset", 5);
        }
        return offset;
    }

    public final void setOffset(double offset) {
        offsetProperty().set(offset);
    }

    private BooleanProperty resizable;

    public final boolean isResizable() {
        return resizable == null || resizable.get();
    }

    /**
     * Determines if the pane can currently be resized or not.
     * <p>
     * Default is true.
     *
     * @return true if the pane is resizable
     */
    public final BooleanProperty resizableProperty() {
        if (resizable == null) {
            resizable = new SimpleBooleanProperty(this, "resizable", true);
        }
        return resizable;
    }

    public final void setResizable(boolean resizable) {
        resizableProperty().set(resizable);
    }

    private final ListProperty<Operation> supportedOperations = new SimpleListProperty<>(this, "supportedOperations", FXCollections.observableArrayList(Operation.values()));

    /**
     * The list of supported operations for resizing the region.
     * <p>
     * By default, all operations are supported.
     *
     * @return the list of supported operations
     */
    public final ListProperty<Operation> supportedOperationsProperty() {
        return supportedOperations;
    }

    public final ObservableList<Operation> getSupportedOperations() {
        return supportedOperations.get();
    }

    public final void setSupportedOperations(ObservableList<Operation> supportedOperations) {
        supportedOperationsProperty().set(supportedOperations);
    }

    /**
     * Checks if a ResizingBehaviour is installed on the provided region.
     *
     * @param region the region to check for installation
     * @return true if the behavior is installed, false otherwise
     */
    public static boolean isInstalled(Region region) {
        return Boolean.TRUE.equals(region.getProperties().get(RESIZE_BEHAVIOUR_INSTALLED));
    }

    /**
     * Returns true if this ResizingBehaviour is installed on the region.
     *
     * @return true if installed, false otherwise
     */
    public boolean isInstalled() {
        return isInstalled(region);
    }

    /**
     * Uninstalls this ResizingBehaviour from the region, cleaning up all event handlers.
     */
    public void uninstall() {
        region.removeEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        region.removeEventFilter(MouseEvent.MOUSE_ENTERED, mouseMovedHandler);
        region.removeEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, mouseMovedHandler);

        region.removeEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        region.removeEventFilter(MouseEvent.MOUSE_RELEASED, mouseReleasedHandler);
        region.removeEventFilter(MouseEvent.MOUSE_DRAGGED, mouseDraggedHandler);

        region.getProperties().remove(RESIZE_BEHAVIOUR_INSTALLED);
    }

}
