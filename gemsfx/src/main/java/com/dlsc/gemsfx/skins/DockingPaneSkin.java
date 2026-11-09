package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DockingLayoutNode;
import com.dlsc.gemsfx.DockingPane;
import com.dlsc.gemsfx.DockingPosition;
import com.dlsc.gemsfx.docking.internal.DockingTree;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.SkinBase;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Window;

/**
 * Skin of {@link DockingPane}. The tree recreates its nodes on every mutation, so the rendered scene graph is rebuilt
 * from the current {@link DockingLayoutNode} snapshot whenever it changes.
 */
public class DockingPaneSkin extends SkinBase<DockingPane> {

    private static final DataFormat DOCKABLE_ID_FORMAT = new DataFormat("application/x-gemsfx-dockable-id");
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final List<String> DROP_ZONE_STYLES = List.of(
            "drop-zone-center", "drop-zone-left", "drop-zone-right", "drop-zone-top", "drop-zone-bottom");

    private boolean syncingDivider;
    private DockingTree.ResolvedDropTarget previewTarget;
    private final StackPane host = new StackPane();
    private final Region dropLeafPreview = previewRegion("docking-drop-leaf-preview");
    private final Region dropZonePreview = previewRegion("docking-drop-zone-preview");
    private final Map<DockingPosition, StackPane> paneEdgeTargets = new EnumMap<>(DockingPosition.class);
    // Eagerly validate snapshots even when divider synchronization skips rebuilding.
    private final ChangeListener<DockingLayoutNode> layoutListener = (obs, oldLayout, newLayout) -> {
        if (!syncingDivider) {
            rebuild();
        }
    };
    private final EventHandler<DragEvent> dragOverHandler = this::handleDragOver;
    private final EventHandler<DragEvent> dragDroppedHandler = this::handleDragDropped;
    private final EventHandler<DragEvent> dragExitedHandler = event -> {
        clearDropPreview();
        hidePaneEdgeTargets();
    };
    private final EventHandler<DragEvent> sceneDragOverHandler = this::handleSceneDragOver;
    private final EventHandler<DragEvent> sceneDragDroppedHandler = this::handleSceneDragDropped;
    private final EventHandler<KeyEvent> keyPressedHandler = this::handleKeyPressed;
    private String activeDragDockableId;
    private boolean dragCancelledByEscape;
    private boolean unsuccessfulDropObserved;
    private Point2D outsideContextDropPosition;
    private Scene activeDragScene;


    /**
     * Creates a skin for the specified docking pane.
     *
     * @param pane the pane to render
     */
    public DockingPaneSkin(DockingPane pane) {
        super(pane);
        for (DockingPosition edge : List.of(DockingPosition.LEFT, DockingPosition.RIGHT,
                DockingPosition.TOP, DockingPosition.BOTTOM)) {
            Region icon = new Region();
            icon.getStyleClass().add("icon");
            StackPane target = new StackPane(icon);
            target.getStyleClass().addAll("docking-pane-edge-target", "pane-edge-" + edge.name().toLowerCase(java.util.Locale.ROOT));
            target.setManaged(false);
            target.setMouseTransparent(true);
            target.setVisible(false);
            paneEdgeTargets.put(edge, target);
        }
        pane.layoutRootProperty().addListener(layoutListener);
        pane.addEventHandler(DragEvent.DRAG_OVER, dragOverHandler);
        pane.addEventHandler(DragEvent.DRAG_DROPPED, dragDroppedHandler);
        pane.addEventHandler(DragEvent.DRAG_EXITED, dragExitedHandler);
        pane.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        rebuild();
    }

    private void rebuild() {
        DockingPane pane = getSkinnable();
        String focusedId = focusedDockableId();
        detachContents();
        Node root = render(pane.getLayoutRoot(), new ArrayList<>());
        host.getChildren().setAll(root, dropLeafPreview, dropZonePreview);
        host.getChildren().addAll(paneEdgeTargets.values());
        getChildren().setAll(host);
        clearDropPreview();
        if (focusedId != null) {
            for (Node title : root.lookupAll(".docking-tab")) {
                if (focusedId.equals(title.getProperties().get(DockingTabPane.DOCKABLE_ID_KEY))) {
                    title.requestFocus();
                }
            }
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        host.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
        double size = Math.min(36, Math.min(contentWidth, contentHeight) / 3);
        double inset = Math.min(12, size / 3);
        for (Map.Entry<DockingPosition, StackPane> entry : paneEdgeTargets.entrySet()) {
            double x = (contentWidth - size) / 2;
            double y = (contentHeight - size) / 2;
            switch (entry.getKey()) {
                case LEFT:
                    x = inset;
                    break;
                case RIGHT:
                    x = contentWidth - inset - size;
                    break;
                case TOP:
                    y = inset;
                    break;
                case BOTTOM:
                    y = contentHeight - inset - size;
                    break;
                default:
                    break;
            }
            entry.getValue().resizeRelocate(x, y, size, size);
        }
    }

    // Dockable contents are reused across rebuilds, so release them from the stale scene graph first.
    private void detachContents() {
        for (Node old : getChildren()) {
            for (Node content : old.lookupAll(".docking-tab-content")) {
                ((StackPane) content).getChildren().clear();
            }
        }
    }

    private String focusedDockableId() {
        if (getSkinnable().getScene() == null) {
            return null;
        }
        for (Node node = getSkinnable().getScene().getFocusOwner(); node != null; node = node.getParent()) {
            Object id = node.getProperties().get(DockingTabPane.DOCKABLE_ID_KEY);
            if (id != null) {
                return node.getStyleClass().contains("docking-tab") ? (String) id : null;
            }
        }
        return null;
    }

    private Node render(DockingLayoutNode node, List<Integer> path) {
        if (node instanceof DockingLayoutNode.Tabs) {
            DockingTabPane tabs = new DockingTabPane(getSkinnable(), (DockingLayoutNode.Tabs) node);
            installTitleDragHandlers(tabs);
            return tabs;
        }
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            SplitPane splitPane = new SplitPane(render(split.getFirst(), child(path, 0)),
                    render(split.getSecond(), child(path, 1)));
            splitPane.getStyleClass().add("docking-split");
            splitPane.setOrientation(split.getOrientation());
            splitPane.setDividerPositions(split.getDividerPosition());
            splitPane.getDividers().get(0).positionProperty().addListener((obs, old, value) -> syncDivider(path, value.doubleValue()));
            return splitPane;
        }
        StackPane empty = new StackPane();
        empty.getStyleClass().add("docking-empty");
        return empty;
    }

    private static List<Integer> child(List<Integer> path, int index) {
        List<Integer> child = new ArrayList<>(path);
        child.add(index);
        return child;
    }

    // The model is updated while dragging; the visible split already shows the position, so it must not be rebuilt.
    private void syncDivider(List<Integer> path, double position) {
        if (!(position >= 0 && position <= 1)) {
            return;
        }
        syncingDivider = true;
        try {
            getSkinnable().updateDividerPosition(path, position);
        } finally {
            syncingDivider = false;
        }
    }

    @Override
    public void dispose() {
        clearDropPreview();
        hidePaneEdgeTargets();
        uninstallSceneDragHandlers();
        DockingPane pane = getSkinnable();
        pane.removeEventHandler(DragEvent.DRAG_OVER, dragOverHandler);
        pane.removeEventHandler(DragEvent.DRAG_DROPPED, dragDroppedHandler);
        pane.removeEventHandler(DragEvent.DRAG_EXITED, dragExitedHandler);
        pane.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        getSkinnable().layoutRootProperty().removeListener(layoutListener);
        super.dispose();
    }

    private void installTitleDragHandlers(Node root) {
        for (Node title : root.lookupAll(".docking-tab")) {
            title.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                String id = dockableId(title);
                if (id == null || !startDockableDrag(title, id)) {
                    return;
                }
                beginDrag(id);
                event.consume();
            });
            title.addEventHandler(DragEvent.DRAG_DONE,
                    event -> completeDrag(dockableId(title), event.getTransferMode(), event.getScreenX(), event.getScreenY()));
        }
    }

    /**
     * Starts the JavaFX drag gesture for a dockable title.
     *
     * @param title      the title node that initiated the drag
     * @param dockableId the ID carried by the drag gesture
     * @return {@code true} if the dragboard accepted the dockable ID
     */
    protected boolean startDockableDrag(Node title, String dockableId) {
        Dragboard dragboard = title.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.put(DOCKABLE_ID_FORMAT, dockableId);
        return dragboard.setContent(content);
    }

    private void handleDragOver(DragEvent event) {
        String draggedId = draggedDockableId(event);
        if (draggedId == null) {
            return;
        }
        DockingPane pane = getSkinnable();
        showPaneEdgeTargets(draggedId);
        DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
        if (paneEdge != null) {
            showPaneEdgePreview(paneEdge);
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
            return;
        }
        DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), draggedId);
        if (target == null || !pane.canMoveDockable(draggedId, target.getTargetId(), target.getPosition())) {
            clearDropPreview();
            return;
        }
        showDropPreview(target);
        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        String draggedId = draggedDockableId(event);
        if (draggedId == null) {
            return;
        }
        DockingPane pane = getSkinnable();
        showPaneEdgeTargets(draggedId);
        DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
        boolean moved;
        if (paneEdge != null) {
            moved = pane.moveDockableToPaneEdge(draggedId, paneEdge);
        } else {
            DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), draggedId);
            moved = target != null && pane.moveDockable(draggedId, target.getTargetId(), target.getPosition());
        }
        if (moved) {
            event.setDropCompleted(true);
            event.consume();
        } else {
            recordUnsuccessfulDrop(draggedId);
            event.setDropCompleted(false);
        }
        clearDropPreview();
        hidePaneEdgeTargets();
    }

    private void showPaneEdgeTargets(String draggedId) {
        for (Map.Entry<DockingPosition, StackPane> entry : paneEdgeTargets.entrySet()) {
            entry.getValue().setVisible(getSkinnable().canMoveDockableToPaneEdge(draggedId, entry.getKey()));
        }
    }

    private void hidePaneEdgeTargets() {
        for (StackPane target : paneEdgeTargets.values()) {
            target.setVisible(false);
            target.pseudoClassStateChanged(ACTIVE, false);
        }
    }

    private DockingPosition resolvePaneEdgeTarget(double x, double y) {
        Point2D point = host.sceneToLocal(getSkinnable().localToScene(x, y));
        for (Map.Entry<DockingPosition, StackPane> entry : paneEdgeTargets.entrySet()) {
            StackPane target = entry.getValue();
            if (target.isVisible() && target.getBoundsInParent().contains(point)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void showPaneEdgePreview(DockingPosition edge) {
        clearDropPreview();
        double width = host.getWidth();
        double height = host.getHeight();
        boolean horizontal = edge == DockingPosition.LEFT || edge == DockingPosition.RIGHT;
        double zoneWidth = horizontal ? width / 2 : width;
        double zoneHeight = horizontal ? height : height / 2;
        double x = edge == DockingPosition.RIGHT ? width / 2 : 0;
        double y = edge == DockingPosition.BOTTOM ? height / 2 : 0;
        dropLeafPreview.resizeRelocate(0, 0, width, height);
        dropLeafPreview.setVisible(true);
        dropZonePreview.getStyleClass().add(previewStyle(edge));
        dropZonePreview.resizeRelocate(x, y, zoneWidth, zoneHeight);
        dropZonePreview.setVisible(true);
        paneEdgeTargets.get(edge).pseudoClassStateChanged(ACTIVE, true);
    }

    private static String dockableId(Node node) {
        Object value = node.getProperties().get(DockingTabPane.DOCKABLE_ID_KEY);
        return value instanceof String ? (String) value : null;
    }

    private static String draggedDockableId(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard != null && dragboard.hasContent(DOCKABLE_ID_FORMAT)) {
            Object value = dragboard.getContent(DOCKABLE_ID_FORMAT);
            return value instanceof String ? (String) value : null;
        }
        Object gestureSource = event.getGestureSource();
        return gestureSource instanceof Node ? dockableId((Node) gestureSource) : null;
    }

    private DockingTree.ResolvedDropTarget resolveDropTarget(double x, double y, Node intersectedNode, String draggedId) {
        DockingPane pane = getSkinnable();
        return DockingTree.resolveDropTarget(pane.getLayoutRoot(), x, y, pane.getWidth(), pane.getHeight(),
                hoveredDockableId(intersectedNode), draggedId);
    }

    private static Node intersectedNode(DragEvent event) {
        PickResult pickResult = event.getPickResult();
        return pickResult == null ? null : pickResult.getIntersectedNode();
    }

    private static String hoveredDockableId(Node intersectedNode) {
        for (Node node = intersectedNode; node != null; node = node.getParent()) {
            String id = dockableId(node);
            if (id != null) {
                return id;
            }
        }
        return null;
    }

    private void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.ESCAPE && activeDragDockableId != null) {
            dragCancelledByEscape = true;
            clearDropPreview();
            hidePaneEdgeTargets();
        }
    }

    private void beginDrag(String dockableId) {
        activeDragDockableId = dockableId;
        dragCancelledByEscape = false;
        unsuccessfulDropObserved = false;
        outsideContextDropPosition = null;
        showPaneEdgeTargets(dockableId);
        installSceneDragHandlers();
    }

    private void installSceneDragHandlers() {
        uninstallSceneDragHandlers();
        Scene scene = getSkinnable().getScene();
        if (scene == null) {
            return;
        }
        scene.addEventFilter(DragEvent.DRAG_OVER, sceneDragOverHandler);
        scene.addEventFilter(DragEvent.DRAG_DROPPED, sceneDragDroppedHandler);
        activeDragScene = scene;
    }

    private void uninstallSceneDragHandlers() {
        if (activeDragScene == null) {
            return;
        }
        activeDragScene.removeEventFilter(DragEvent.DRAG_OVER, sceneDragOverHandler);
        activeDragScene.removeEventFilter(DragEvent.DRAG_DROPPED, sceneDragDroppedHandler);
        activeDragScene = null;
    }

    private void handleSceneDragOver(DragEvent event) {
        if (!isActiveDockableDrag(event) || isInsideContextPane(intersectedNode(event))) {
            return;
        }
        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    private void handleSceneDragDropped(DragEvent event) {
        if (!isActiveDockableDrag(event) || isInsideContextPane(intersectedNode(event))) {
            return;
        }
        if (handleExternalDrop(activeDragDockableId, event.getScreenX(), event.getScreenY())) {
            event.setDropCompleted(true);
            event.consume();
        }
    }

    private boolean isActiveDockableDrag(DragEvent event) {
        String draggedId = draggedDockableId(event);
        return activeDragDockableId != null && activeDragDockableId.equals(draggedId);
    }

    private boolean isInsideContextPane(Node node) {
        DockingPane contextPane = getSkinnable();
        for (Node current = node; current != null; current = current.getParent()) {
            if (current instanceof DockingPane
                    && ((DockingPane) current).getDockingContext() == contextPane.getDockingContext()) {
                return true;
            }
        }
        return false;
    }

    private boolean handleExternalDrop(String dockableId, double screenX, double screenY) {
        if (dockableId == null || !dockableId.equals(activeDragDockableId) || dragCancelledByEscape) {
            return false;
        }
        outsideContextDropPosition = new Point2D(screenX, screenY);
        return true;
    }

    private void completeDrag(String dockableId, TransferMode transferMode, double screenX, double screenY) {
        if (dockableId == null || !dockableId.equals(activeDragDockableId) || dragCancelledByEscape
                || (outsideContextDropPosition == null && (transferMode != null || unsuccessfulDropObserved))) {
            finishDrag();
            return;
        }
        Point2D dropPosition = outsideContextDropPosition;
        if (dropPosition == null) {
            // Native JavaFX drag-done events can report zero coordinates instead of the drop location.
            dropPosition = screenX == 0 && screenY == 0
                    ? new Robot().getMousePosition() : new Point2D(screenX, screenY);
        }
        if (outsideContextDropPosition != null || isOutsideAllContextPanes(dropPosition.getX(), dropPosition.getY())) {
            getSkinnable().getDockingContext().floatDockable(dockableId, dropPosition.getX(), dropPosition.getY());
        }
        finishDrag();
    }

    private void recordUnsuccessfulDrop(String dockableId) {
        if (dockableId != null && dockableId.equals(activeDragDockableId)) {
            unsuccessfulDropObserved = true;
        }
    }

    private boolean isOutsideAllContextPanes(double screenX, double screenY) {
        if (!Double.isFinite(screenX) || !Double.isFinite(screenY)) {
            return false;
        }
        for (Window window : Window.getWindows()) {
            Scene scene = window.getScene();
            if (scene == null || scene.getRoot() == null) {
                continue;
            }
            for (DockingPane pane : contextPanes(scene.getRoot(), getSkinnable().getDockingContext())) {
                Bounds bounds = pane.localToScreen(pane.getBoundsInLocal());
                if (bounds != null && bounds.contains(screenX, screenY)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static List<DockingPane> contextPanes(Node node, com.dlsc.gemsfx.DockingContext context) {
        List<DockingPane> panes = new ArrayList<>();
        collectContextPanes(node, context, panes);
        return panes;
    }

    private static void collectContextPanes(Node node, com.dlsc.gemsfx.DockingContext context, List<DockingPane> panes) {
        if (node instanceof DockingPane && ((DockingPane) node).getDockingContext() == context) {
            panes.add((DockingPane) node);
        }
        if (node instanceof Parent) {
            for (Node child : ((Parent) node).getChildrenUnmodifiable()) {
                collectContextPanes(child, context, panes);
            }
        }
    }

    private void resetDragState() {
        activeDragDockableId = null;
        dragCancelledByEscape = false;
        unsuccessfulDropObserved = false;
        outsideContextDropPosition = null;
    }

    private void finishDrag() {
        resetDragState();
        uninstallSceneDragHandlers();
        clearDropPreview();
        hidePaneEdgeTargets();
    }

    private void showDropPreview(DockingTree.ResolvedDropTarget target) {
        if (target.equals(previewTarget)) {
            return;
        }
        clearDropPreview();
        dropLeafPreview.resizeRelocate(target.getLeafX(), target.getLeafY(), target.getLeafWidth(), target.getLeafHeight());
        dropLeafPreview.setVisible(true);
        dropZonePreview.getStyleClass().add(previewStyle(target.getPosition()));
        dropZonePreview.resizeRelocate(target.getZoneX(), target.getZoneY(), target.getZoneWidth(), target.getZoneHeight());
        dropZonePreview.setVisible(true);
        previewTarget = target;
    }

    private void clearDropPreview() {
        dropLeafPreview.setVisible(false);
        dropZonePreview.setVisible(false);
        dropZonePreview.getStyleClass().removeAll(DROP_ZONE_STYLES);
        for (StackPane target : paneEdgeTargets.values()) {
            target.pseudoClassStateChanged(ACTIVE, false);
        }
        previewTarget = null;
    }

    private static String previewStyle(com.dlsc.gemsfx.DockingPosition position) {
        switch (position) {
            case LEFT:
                return "drop-zone-left";
            case RIGHT:
                return "drop-zone-right";
            case TOP:
                return "drop-zone-top";
            case BOTTOM:
                return "drop-zone-bottom";
            default:
                return "drop-zone-center";
        }
    }

    private static Region previewRegion(String styleClass) {
        Region region = new Region();
        region.getStyleClass().add(styleClass);
        region.setManaged(false);
        region.setMouseTransparent(true);
        region.setVisible(false);
        return region;
    }
}
