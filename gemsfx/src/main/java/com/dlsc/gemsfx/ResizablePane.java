package com.dlsc.gemsfx;

import javafx.beans.property.*;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;

import java.util.function.BiConsumer;

/**
 * A stack pane that can be resized interactively by the user. Resize operations (mouse pressed and
 * dragged) modify the preferred width and height of the pane and also the layout x and y coordinates.
 */
public class ResizablePane extends StackPane {

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

    private Operation operation;

    /**
     * Constructs a new pane without any children. Those have to be added later
     * via the {@link StackPane#getChildren()} method.
     */
    public ResizablePane() {
        this(new Node[]{});
    }

    /**
     * Constructs a new pane with the given children.
     */
    public ResizablePane(Node... children) {
        super(children);

        getStyleClass().add("resizable-pane");

        EventHandler<MouseEvent> mouseMovedHandler = evt -> {
            if (!isResizable()) {
                return;
            }

            double x = evt.getX();
            double y = evt.getY();

            final double offset = getOffset();

            if (x < offset) {
                if (y < offset) {
                    setCursor(Cursor.NW_RESIZE);
                } else if (y > getHeight() - offset) {
                    setCursor(Cursor.SW_RESIZE);
                } else {
                    setCursor(Cursor.W_RESIZE);
                }
            } else if (x > getWidth() - offset) {
                if (y < offset) {
                    setCursor(Cursor.NE_RESIZE);
                } else if (y > getHeight() - offset) {
                    setCursor(Cursor.SE_RESIZE);
                } else {
                    setCursor(Cursor.E_RESIZE);
                }
            } else if (y < offset) {
                setCursor(Cursor.N_RESIZE);
            } else if (y > getHeight() - offset) {
                setCursor(Cursor.S_RESIZE);
            } else {
                setCursor(Cursor.DEFAULT);
            }
        };

        addEventFilter(MouseEvent.MOUSE_MOVED, mouseMovedHandler);
        addEventFilter(MouseEvent.MOUSE_ENTERED, mouseMovedHandler);
        addEventFilter(MouseEvent.MOUSE_ENTERED_TARGET, mouseMovedHandler);

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
                } else if (y > getHeight() - offset) {
                    operation = Operation.RESIZE_SW;
                } else {
                    operation = Operation.RESIZE_W;
                }
            } else if (x > getWidth() - offset) {
                if (y < offset) {
                    operation = Operation.RESIZE_NE;
                } else if (y > getHeight() - offset) {
                    operation = Operation.RESIZE_SE;
                } else {
                    operation = Operation.RESIZE_E;
                }
            } else if (y < offset) {
                operation = Operation.RESIZE_N;
            } else if (y > getHeight() - offset) {
                operation = Operation.RESIZE_S;
            } else {
                operation = Operation.NONE;
            }
        };

        addEventFilter(MouseEvent.MOUSE_PRESSED, mousePressedHandler);
        addEventFilter(MouseEvent.MOUSE_RELEASED, evt -> operation = Operation.NONE);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, evt -> {
            double x = evt.getScreenX();
            double y = evt.getScreenY();

            double deltaX = (evt.getScreenX() - startX) * 4;
            double deltaY = (evt.getScreenY() - startY) * 4;

            double width = getWidth();
            double height = getHeight();

            double minHeight = minHeight(width);
            double maxHeight = maxHeight(width);

            double minWidth = minWidth(height);
            double maxWidth = maxWidth(height);

            double newHeight;
            double newWidth;

            switch (operation) {

                case NONE:
                    break;
                case RESIZE_N:
                    newHeight = height - deltaY;
                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setLayoutY(y);
                        setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                        startX = x;
                        startY = y;
                    }
                    evt.consume();
                    break;
                case RESIZE_S:
                    newHeight = height + deltaY;
                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                        startX = x;
                        startY = y;
                    }
                    evt.consume();
                    break;
                case RESIZE_W:
                    newWidth = width - deltaX;
                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setLayoutX(x);
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                        startX = x;
                        startY = y;
                    }
                    evt.consume();
                    break;
                case RESIZE_E:
                    newWidth = width + deltaX;
                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                        startX = x;
                        startY = y;
                    }
                    evt.consume();
                    break;
                case RESIZE_NW:
                    newWidth = width - deltaX;
                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setLayoutX(x);
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                        startX = x;
                    }
                    newHeight = height - deltaY;
                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setLayoutY(y);
                        setPrefHeight(Math.min(maxHeight, Math.max(minHeight, newHeight)));
                        startY = y;
                    }
                    evt.consume();
                    break;
                case RESIZE_NE:
                    newWidth = width + deltaX;
                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                        startX = x;
                    }

                    newHeight = height - deltaY;
                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setLayoutY(y);
                        setPrefHeight(Math.min(maxHeight, Math.max(minHeight, height - deltaY)));
                        startY = y;
                    }

                    evt.consume();
                    break;
                case RESIZE_SW:
                    newWidth = width - deltaX;
                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setLayoutX(x);
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, width - deltaX)));
                        startX = x;
                    }

                    newHeight = height + deltaY;
                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setPrefHeight(Math.min(newHeight, Math.max(minHeight, height + deltaY)));
                        startY = y;
                    }

                    evt.consume();
                    break;
                case RESIZE_SE:
                    newWidth = width + deltaX;

                    if (newWidth >= minWidth && newWidth <= maxWidth) {
                        setLayoutX(x);
                        setPrefWidth(Math.min(maxWidth, Math.max(minWidth, newWidth)));
                        startX = x;
                    }

                    newHeight = height + deltaY;

                    if (newHeight >= minHeight && newHeight <= maxHeight) {
                        setPrefHeight(Math.max(minHeight, newHeight));
                        startY = y;
                    }

                    evt.consume();
                    break;
            }

            BiConsumer<Double, Double> onResize = getOnResize();
            if (onResize != null) {
                onResize.accept(getWidth(), getHeight());
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
