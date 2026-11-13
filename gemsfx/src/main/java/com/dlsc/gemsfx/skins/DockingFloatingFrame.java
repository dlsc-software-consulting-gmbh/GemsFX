package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.docking.DockingFloatingGroup;
import com.dlsc.gemsfx.docking.DockingPane;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * In-pane frame for a floating tab group. Gestures change geometry only, never docking mode.
 */
final class DockingFloatingFrame extends StackPane {

    private static final double GRIP = 5;
    private final DockingPane pane;
    private final DockingTabPane tabs;
    private DockingFloatingGroup group;
    private Point2D press;
    private Rectangle2D start;
    private int edges;

    DockingFloatingFrame(DockingPane pane, DockingTabPane tabs, DockingFloatingGroup group) {
        super(tabs);
        this.pane = pane;
        this.tabs = tabs;
        this.group = group;
        getStyleClass().add("docking-floating-frame");
        setManaged(false);
        setMinSize(0, 0);
        tabs.setMinSize(0, 0);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);
        addEventFilter(MouseEvent.MOUSE_PRESSED, this::pressed);
        addEventFilter(MouseEvent.MOUSE_DRAGGED, this::dragged);
        addEventFilter(MouseEvent.MOUSE_RELEASED, this::released);
        addEventFilter(MouseEvent.DRAG_DETECTED, event -> {
            if (press != null) {
                event.consume();
            }
        });
        addEventFilter(MouseEvent.MOUSE_MOVED, event -> {
            Point2D point = sceneToLocal(event.getSceneX(), event.getSceneY());
            setCursor(cursor(edgesAt(point)));
        });
        addEventHandler(MouseEvent.MOUSE_EXITED, event -> {
            if (press == null) {
                setCursor(Cursor.DEFAULT);
            }
        });
    }

    DockingFloatingGroup getGroup() {
        return group;
    }

    void update(DockingFloatingGroup group) {
        this.group = group;
        tabs.updateSelection(group.getTabs().getSelectedId());
    }

    void layoutIn(double width, double height) {
        if (press == null) {
            Rectangle2D bounds = group.resolveBounds(width, height);
            resizeRelocate(bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight());
        }
    }

    private void pressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY) {
            return;
        }
        Point2D point = sceneToLocal(event.getSceneX(), event.getSceneY());
        edges = edgesAt(point);
        boolean header = false;
        for (Node node = event.getTarget() instanceof Node ? (Node) event.getTarget() : null;
             node != null && node != this; node = node.getParent()) {
            if (node instanceof ButtonBase || node instanceof TextInputControl) {
                pane.selectDockable(group.getTabs().getSelectedId());
                return;
            }
            if (node.getStyleClass().contains("docking-tab-strip")) {
                header = true;
            }
        }
        pane.selectDockable(group.getTabs().getSelectedId());
        if (edges == 0 && !header) {
            return;
        }
        press = getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
        start = new Rectangle2D(getLayoutX(), getLayoutY(), getWidth(), getHeight());
        if (edges != 0) {
            event.consume();
        }
    }

    private void dragged(MouseEvent event) {
        if (press == null) {
            return;
        }
        Point2D point = getParent().sceneToLocal(event.getSceneX(), event.getSceneY());
        double dx = point.getX() - press.getX();
        double dy = point.getY() - press.getY();
        double availableWidth = getParent().getLayoutBounds().getWidth();
        double availableHeight = getParent().getLayoutBounds().getHeight();
        double x = start.getMinX();
        double y = start.getMinY();
        double right = start.getMaxX();
        double bottom = start.getMaxY();
        if (edges == 0) {
            x = clamp(x + dx, 0, availableWidth - start.getWidth());
            y = clamp(y + dy, 0, availableHeight - start.getHeight());
            resizeRelocate(x, y, start.getWidth(), start.getHeight());
        } else {
            double minWidth = Math.min(120, availableWidth);
            double minHeight = Math.min(80, availableHeight);
            if ((edges & 1) != 0) {
                x = clamp(x + dx, 0, right - Math.min(minWidth, start.getWidth()));
            }
            if ((edges & 2) != 0) {
                right = clamp(right + dx, x + Math.min(minWidth, availableWidth - x), availableWidth);
            }
            if ((edges & 4) != 0) {
                y = clamp(y + dy, 0, bottom - Math.min(minHeight, start.getHeight()));
            }
            if ((edges & 8) != 0) {
                bottom = clamp(bottom + dy, y + Math.min(minHeight, availableHeight - y), availableHeight);
            }
            resizeRelocate(x, y, right - x, bottom - y);
        }
        event.consume();
    }

    private void released(MouseEvent event) {
        if (press == null) {
            return;
        }
        press = null;
        boolean changed = start.getMinX() != getLayoutX() || start.getMinY() != getLayoutY()
                || start.getWidth() != getWidth() || start.getHeight() != getHeight();
        if (changed && getWidth() > 0 && getHeight() > 0) {
            pane.updateFloatingGroupBounds(group.getTabs().getDockableIds().get(0),
                    new Rectangle2D(getLayoutX(), getLayoutY(),
                            edges == 0 ? group.getWidth() : getWidth(),
                            edges == 0 ? group.getHeight() : getHeight()));
            event.consume();
        }
        setCursor(Cursor.DEFAULT);
    }

    private int edgesAt(Point2D point) {
        return (point.getX() < GRIP ? 1 : point.getX() >= getWidth() - GRIP ? 2 : 0)
                | (point.getY() < GRIP ? 4 : point.getY() >= getHeight() - GRIP ? 8 : 0);
    }

    private static Cursor cursor(int edges) {
        switch (edges) {
            case 1: return Cursor.W_RESIZE;
            case 2: return Cursor.E_RESIZE;
            case 4: return Cursor.N_RESIZE;
            case 8: return Cursor.S_RESIZE;
            case 5: return Cursor.NW_RESIZE;
            case 6: return Cursor.NE_RESIZE;
            case 9: return Cursor.SW_RESIZE;
            case 10: return Cursor.SE_RESIZE;
            default: return Cursor.DEFAULT;
        }
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(value, Math.max(min, max)));
    }
}
