package com.dlsc.gemsfx.util;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import java.util.function.BiConsumer;

/**
 * A stack pane that can be resized interactively by the user. Resize operations (mouse pressed and
 * dragged) modify the preferred width and height of the pane and also the layout x and y coordinates.
 */
public class ResizingBehaviour {

    private double startX;
    private double startY;

    /*
     * List of possible resizing operations.
     */
    private enum Operation {
        NONE,
        RESIZE_N,
        RESIZE_S,
        RESIZE_W,
        RESIZE_E,
        RESIZE_NW,
        RESIZE_NE,
        RESIZE_SW,
        RESIZE_SE
    }

    private Operation operation = Operation.NONE;

    /**
     * Installs the resizing behaviour on the given region. Once installed
     * the user will be able to resize the given region (depending on the container
     * the region lives in and the layout algorithms used by that container).
     *
     * @param region the region to support resizing
     * @return the installed behaviour
     */
    public static ResizingBehaviour install(Region region) {
        return new ResizingBehaviour(region);
    }

    /**
     * Constructs a new pane with the given children.
     */
    private ResizingBehaviour(Region region) {
        EventHandler<MouseEvent> mouseMovedHandler = evt -> {
            if (!isResizable()) {
                return;
            }

            double x = evt.getX();
            double y = evt.getY();

            final double offset = getOffset();

            if (x < offset) {
                if (y < offset) {
                    region.setCursor(Cursor.NW_RESIZE);
                } else if (y > region.getHeight() - offset) {
                    region.setCursor(Cursor.SW_RESIZE);
                } else {
                    region.setCursor(Cursor.W_RESIZE);
                }
            } else if (x > region.getWidth() - offset) {
                if (y < offset) {
                    region.setCursor(Cursor.NE_RESIZE);
                } else if (y > region.getHeight() - offset) {
                    region.setCursor(Cursor.SE_RESIZE);
                } else {
                    region.setCursor(Cursor.E_RESIZE);
                }
            } else if (y < offset) {
                region.setCursor(Cursor.N_RESIZE);
            } else if (y > region.getHeight() - offset) {
                region.setCursor(Cursor.S_RESIZE);
            } else {
                region.setCursor(Cursor.DEFAULT);
            }
        };

        region.addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        region.addEventFilter(MouseEvent.MOUSE_ENTERED, mouseMovedHandler);
        region.addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, mouseMovedHandler);

        EventHandler<MouseEvent> mousePressedHandler = evt -> {
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
                    operation = Operation.RESIZE_NW;
                } else if (y > region.getHeight() - offset) {
                    operation = Operation.RESIZE_SW;
                } else {
                    operation = Operation.RESIZE_W;
                }
            } else if (x > region.getWidth() - offset) {
                if (y < offset) {
                    operation = Operation.RESIZE_NE;
                } else if (y > region.getHeight() - offset) {
                    operation = Operation.RESIZE_SE;
                } else {
                    operation = Operation.RESIZE_E;
                }
            } else if (y < offset) {
                operation = Operation.RESIZE_N;
            } else if (y > region.getHeight() - offset) {
                operation = Operation.RESIZE_S;
            } else {
                operation = Operation.NONE;
            }
        };

        region.addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        region.addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> operation = Operation.NONE);
        region.addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
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

                case NONE:
                    break;
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
        });
    }

    // resize callback

    private final ObjectProperty<BiConsumer<Double, Double>> onResize = new SimpleObjectProperty<>(this, "onResize");

    public final BiConsumer<Double, Double> getOnResize() {
        return onResize.get();
    }

    /**
     * A callback used to inform interested parties when the width or height of the
     * dialog was changed interactively by the user.
     *
     * @see #resizableProperty()
     * @return the callback / the consumer of the new width and height
     */
    public final ObjectProperty<BiConsumer<Double, Double>> onResizeProperty() {
        return onResize;
    }

    public final void setOnResize(BiConsumer<Double, Double> onResize) {
        this.onResize.set(onResize);
    }

    private final DoubleProperty offset = new SimpleDoubleProperty(this, "offset", 5);

    public final double getOffset() {
        return offset.get();
    }

    /**
     * The offset from the edges in pixels where the user will be able to perform a press
     * and drag to resize the pane. Default is 5.
     */
    public final DoubleProperty offsetProperty() {
        return offset;
    }

    public final void setOffset(double offset) {
        this.offset.set(offset);
    }

    private final BooleanProperty resizable = new SimpleBooleanProperty(this, "resizable", true);

    public final boolean isResizable() {
        return resizable.get();
    }

    /**
     * Determines if the pane can currently be resized or not.
     *
     * @return true if the pane is resizable
     */
    public final BooleanProperty resizableProperty() {
        return resizable;
    }

    public final void setResizable(boolean resizable) {
        this.resizable.set(resizable);
    }
}
