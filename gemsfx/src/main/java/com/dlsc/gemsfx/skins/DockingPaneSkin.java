package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingPosition;
import com.dlsc.gemsfx.docking.model.DockingTree;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
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
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.robot.Robot;
import javafx.scene.transform.Transform;
import javafx.stage.Window;

/**
 * Skin of {@link DockingPane}. Structural changes rebuild the rendered scene graph from the current
 * {@link DockingLayoutNode} snapshot; selection-only changes update existing tab groups in place.
 */
public class DockingPaneSkin extends SkinBase<DockingPane> {

    private static final DataFormat DOCKABLE_ID_FORMAT = new DataFormat("application/x-gemsfx-dockable-id");
    private static final DataFormat TAB_GROUP_FORMAT = new DataFormat("application/x-gemsfx-dockable-tab-group");
    private static final PseudoClass ACTIVE = PseudoClass.getPseudoClass("active");
    private static final PseudoClass FOCUS_HIGHLIGHTED = PseudoClass.getPseudoClass("focus-highlighted");
    private static final List<String> DROP_ZONE_STYLES = List.of(
            "drop-zone-center", "drop-zone-left", "drop-zone-right", "drop-zone-top", "drop-zone-bottom");

    private boolean syncingDivider;
    private DockingTree.ResolvedDropTarget previewTarget;
    private final StackPane host = new StackPane();
    private final StackPane emptyPane = new StackPane();
    private final List<StackPane> contentHosts = new ArrayList<>();
    private final List<Label> graphicLabels = new ArrayList<>();
    private final List<DockingTabPane> tabGroups = new ArrayList<>();
    private Scene focusScene;
    private Window focusWindow;
    private final Region dropLeafPreview = previewRegion("docking-drop-leaf-preview");
    private final Region dropZonePreview = previewRegion("docking-drop-zone-preview");
    private final Map<DockingPosition, StackPane> paneEdgeTargets = new EnumMap<>(DockingPosition.class);
    // Eagerly validate snapshots even when divider synchronization skips rebuilding.
    private final ChangeListener<DockingLayoutNode> layoutListener = (obs, oldLayout, newLayout) -> {
        if (!syncingDivider && !updateSelection(oldLayout, newLayout)) {
            rebuild();
        }
    };
    private final ChangeListener<Node> placeholderListener = (obs, oldPlaceholder, newPlaceholder) -> {
        if (getSkinnable().getLayoutRoot() instanceof DockingLayoutNode.Empty) {
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
    private boolean dragWholeGroup;
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
        emptyPane.getStyleClass().add("docking-empty");
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
        pane.placeholderProperty().addListener(placeholderListener);
        pane.addEventFilter(DragEvent.DRAG_OVER, dragOverHandler);
        pane.addEventFilter(DragEvent.DRAG_DROPPED, dragDroppedHandler);
        pane.addEventFilter(DragEvent.DRAG_EXITED, dragExitedHandler);
        pane.addEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        registerChangeListener(pane.showFocusBorderProperty(), obs -> updateFocusBorders());
        registerChangeListener(pane.getDockingContext().tabFactoryProperty(), obs -> rebuild());
        registerChangeListener(pane.sceneProperty(), obs -> observeFocusScene(pane.getScene()));
        observeFocusScene(pane.getScene());
        rebuild();
    }

    private void observeFocusScene(Scene scene) {
        if (focusScene != null) {
            unregisterChangeListeners(focusScene.focusOwnerProperty());
            unregisterChangeListeners(focusScene.windowProperty());
        }
        focusScene = scene;
        if (scene != null) {
            registerChangeListener(scene.focusOwnerProperty(), obs -> updateFocusBorders());
            registerChangeListener(scene.windowProperty(), obs -> observeFocusWindow(scene.getWindow()));
        }
        observeFocusWindow(scene == null ? null : scene.getWindow());
    }

    private void observeFocusWindow(Window window) {
        if (focusWindow != null) {
            unregisterChangeListeners(focusWindow.focusedProperty());
        }
        focusWindow = window;
        if (window != null) {
            registerChangeListener(window.focusedProperty(), obs -> updateFocusBorders());
        }
        updateFocusBorders();
    }

    private void updateFocusBorders() {
        DockingTabPane focusedGroup = null;
        if (getSkinnable().isShowFocusBorder() && focusScene != null
                && focusWindow != null && focusWindow.isFocused()) {
            for (Node node = focusScene.getFocusOwner(); node != null; node = node.getParent()) {
                if (node instanceof DockingTabPane && tabGroups.contains(node)) {
                    focusedGroup = (DockingTabPane) node;
                    break;
                }
            }
        }
        for (DockingTabPane group : tabGroups) {
            group.pseudoClassStateChanged(FOCUS_HIGHLIGHTED, group == focusedGroup);
        }
    }

    private boolean updateSelection(DockingLayoutNode oldLayout, DockingLayoutNode newLayout) {
        List<String> selectedIds = new ArrayList<>();
        if (!collectSelectionOnlyChanges(oldLayout, newLayout, selectedIds)) {
            return false;
        }
        for (int i = 0; i < tabGroups.size(); i++) {
            tabGroups.get(i).updateSelection(selectedIds.get(i));
        }
        updateFocusBorders();
        return true;
    }

    private static boolean collectSelectionOnlyChanges(DockingLayoutNode oldLayout, DockingLayoutNode newLayout,
                                                       List<String> selectedIds) {
        if (oldLayout instanceof DockingLayoutNode.Tabs && newLayout instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs oldTabs = (DockingLayoutNode.Tabs) oldLayout;
            DockingLayoutNode.Tabs newTabs = (DockingLayoutNode.Tabs) newLayout;
            if (!oldTabs.getDockableIds().equals(newTabs.getDockableIds())) {
                return false;
            }
            selectedIds.add(newTabs.getSelectedId());
            return true;
        }
        if (oldLayout instanceof DockingLayoutNode.Split && newLayout instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split oldSplit = (DockingLayoutNode.Split) oldLayout;
            DockingLayoutNode.Split newSplit = (DockingLayoutNode.Split) newLayout;
            return oldSplit.getOrientation() == newSplit.getOrientation()
                    && oldSplit.getDividerPosition() == newSplit.getDividerPosition()
                    && collectSelectionOnlyChanges(oldSplit.getFirst(), newSplit.getFirst(), selectedIds)
                    && collectSelectionOnlyChanges(oldSplit.getSecond(), newSplit.getSecond(), selectedIds);
        }
        return oldLayout instanceof DockingLayoutNode.Empty && newLayout instanceof DockingLayoutNode.Empty;
    }

    private void rebuild() {
        DockingPane pane = getSkinnable();
        String focusedId = focusedDockableId();
        detachContents();
        emptyPane.getChildren().clear();
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
        updateFocusBorders();
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

    // Unskinned SplitPane items are not reachable through scene-graph lookup.
    private void detachContents() {
        for (Label label : graphicLabels) {
            label.graphicProperty().unbind();
            label.setGraphic(null);
        }
        graphicLabels.clear();
        tabGroups.clear();
        for (StackPane content : contentHosts) {
            content.getChildren().clear();
        }
        contentHosts.clear();
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
            tabGroups.add(tabs);
            contentHosts.add((StackPane) tabs.lookup(".docking-tab-content"));
            for (Node label : tabs.lookupAll(".docking-tab-title")) {
                if (label instanceof Label) {
                    graphicLabels.add((Label) label);
                }
            }
            installTitleDragHandlers(tabs);
            return tabs;
        }
        if (node instanceof DockingLayoutNode.Split) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            SplitPane splitPane = new SplitPane();
            splitPane.getStyleClass().add("docking-split");
            splitPane.setOrientation(split.getOrientation());
            List<Double> positions = new ArrayList<>();
            List<SplitRange> ranges = new ArrayList<>();
            collectSplitItems(splitPane, split, path, 0, 1, positions, ranges);
            splitPane.setDividerPositions(positions.stream().mapToDouble(Double::doubleValue).toArray());
            for (SplitPane.Divider divider : splitPane.getDividers()) {
                divider.positionProperty().addListener((obs, old, value) -> syncDividers(splitPane, ranges));
            }
            return splitPane;
        }
        Node placeholder = getSkinnable().getPlaceholder();
        if (placeholder != null) {
            emptyPane.getChildren().setAll(placeholder);
        }
        return emptyPane;
    }

    private static List<Integer> child(List<Integer> path, int index) {
        List<Integer> child = new ArrayList<>(path);
        child.add(index);
        return child;
    }

    private void collectSplitItems(SplitPane pane, DockingLayoutNode node, List<Integer> path,
                                   double start, double end, List<Double> positions, List<SplitRange> ranges) {
        if (node instanceof DockingLayoutNode.Split
                && ((DockingLayoutNode.Split) node).getOrientation() == pane.getOrientation()) {
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
            int first = pane.getItems().size();
            double boundary = start + (end - start) * split.getDividerPosition();
            collectSplitItems(pane, split.getFirst(), child(path, 0), start, boundary, positions, ranges);
            int middle = pane.getItems().size();
            positions.add(boundary);
            collectSplitItems(pane, split.getSecond(), child(path, 1), boundary, end, positions, ranges);
            ranges.add(new SplitRange(path, first, middle, pane.getItems().size(), split.getDividerPosition()));
        } else {
            pane.getItems().add(render(node, path));
        }
    }

    // The model is updated while dragging; the visible split already shows the position, so it must not be rebuilt.
    private void syncDividers(SplitPane pane, List<SplitRange> ranges) {
        if (syncingDivider) {
            return;
        }
        double[] positions = pane.getDividerPositions();
        double[] boundaries = new double[positions.length + 2];
        boundaries[boundaries.length - 1] = 1;
        for (int i = 0; i < positions.length; i++) {
            if (!(positions[i] >= boundaries[i] && positions[i] <= 1)) {
                return;
            }
            boundaries[i + 1] = positions[i];
        }
        syncingDivider = true;
        try {
            for (SplitRange range : ranges) {
                double size = boundaries[range.end] - boundaries[range.start];
                // A collapsed subtree has no measurable ratio; retain its last model proportion.
                if (size > 0) {
                    double position = (boundaries[range.middle] - boundaries[range.start]) / size;
                    if (position != range.position) {
                        getSkinnable().updateDividerPosition(range.path, position);
                        range.position = position;
                    }
                }
            }
        } finally {
            syncingDivider = false;
        }
    }

    private static final class SplitRange {
        private final List<Integer> path;
        private final int start;
        private final int middle;
        private final int end;
        private double position;

        private SplitRange(List<Integer> path, int start, int middle, int end, double position) {
            this.path = path;
            this.start = start;
            this.middle = middle;
            this.end = end;
            this.position = position;
        }
    }

    @Override
    public void dispose() {
        observeFocusScene(null);
        clearDropPreview();
        hidePaneEdgeTargets();
        uninstallSceneDragHandlers();
        DockingPane pane = getSkinnable();
        pane.removeEventFilter(DragEvent.DRAG_OVER, dragOverHandler);
        pane.removeEventFilter(DragEvent.DRAG_DROPPED, dragDroppedHandler);
        pane.removeEventFilter(DragEvent.DRAG_EXITED, dragExitedHandler);
        pane.removeEventFilter(KeyEvent.KEY_PRESSED, keyPressedHandler);
        getSkinnable().layoutRootProperty().removeListener(layoutListener);
        pane.placeholderProperty().removeListener(placeholderListener);
        detachContents();
        emptyPane.getChildren().clear();
        super.dispose();
    }

    private void installTitleDragHandlers(Node root) {
        for (Node title : root.lookupAll(".docking-tab")) {
            title.addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
                String id = dockableId(title);
                if (id == null) {
                    return;
                }
                dragWholeGroup = event.isShiftDown();
                title.getProperties().put(TAB_GROUP_FORMAT, dragWholeGroup);
                if (!startDockableDrag(title, id)) {
                    dragWholeGroup = false;
                    title.getProperties().remove(TAB_GROUP_FORMAT);
                    return;
                }
                beginDrag(id);
                event.consume();
            });
            title.addEventHandler(DragEvent.DRAG_DONE, event -> {
                try {
                    completeDrag(dockableId(title), event.getTransferMode(), event.getScreenX(), event.getScreenY());
                } finally {
                    title.getProperties().remove(TAB_GROUP_FORMAT);
                }
            });
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
        Image dragView = createDockableDragView(dockableId);
        Dragboard dragboard = title.startDragAndDrop(TransferMode.MOVE);
        dragboard.setDragView(dragView, dragView.getWidth() / 2, 18);
        ClipboardContent content = new ClipboardContent();
        content.put(DOCKABLE_ID_FORMAT, dockableId);
        if (dragWholeGroup) {
            content.put(TAB_GROUP_FORMAT, true);
        }
        return dragboard.setContent(content);
    }

    private Image createDockableDragView(String dockableId) {
        Dockable dockable = getSkinnable().getDockingContext().getDockables().stream()
                .filter(item -> item.getId().equals(dockableId))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Unknown dockable id: " + dockableId));
        Image content = snapshotDockableContent(dockable.getContent());
        Label title = new Label(dockable.getTitle());
        title.getStyleClass().add("docking-drag-title");
        title.setMnemonicParsing(false);
        title.setMinWidth(0);
        title.setMaxWidth(Double.MAX_VALUE);
        VBox preview = new VBox(title, new ImageView(content));
        preview.getStyleClass().addAll("docking-pane", "docking-drag-preview");
        preview.setPrefWidth(Math.max(180, content.getWidth() + 18));
        configureSnapshotScene(new Scene(preview));
        preview.applyCss();
        preview.autosize();
        preview.layout();
        return snapshot(preview, 320, 240);
    }

    private Image snapshotDockableContent(Node content) {
        if (content.getParent() != null || content.getScene() != null) {
            return snapshot(content, 280, 180);
        }
        Bounds bounds = content.getLayoutBounds();
        double oldX = content.getLayoutX();
        double oldY = content.getLayoutY();
        double width = bounds.getWidth() > 0 ? bounds.getWidth() : content.prefWidth(-1);
        if (!(width > 0)) {
            width = Math.max(1, getSkinnable().getWidth());
        }
        double height = bounds.getHeight() > 0 ? bounds.getHeight() : content.prefHeight(width);
        if (!(height > 0)) {
            height = Math.max(1, getSkinnable().getHeight());
        }
        StackPane temporaryRoot = new StackPane(content);
        temporaryRoot.getStyleClass().add("docking-pane");
        try {
            configureSnapshotScene(new Scene(temporaryRoot, width, height));
            temporaryRoot.applyCss();
            temporaryRoot.resize(width, height);
            temporaryRoot.layout();
            return snapshot(content, 280, 180);
        } finally {
            temporaryRoot.getChildren().clear();
            content.resize(bounds.getWidth(), bounds.getHeight());
            content.setLayoutX(oldX);
            content.setLayoutY(oldY);
        }
    }

    private void configureSnapshotScene(Scene scene) {
        scene.getStylesheets().add(getSkinnable().getUserAgentStylesheet());
        Scene source = getSkinnable().getScene();
        if (source != null) {
            scene.getStylesheets().addAll(source.getStylesheets());
            scene.getStylesheets().addAll(source.getRoot().getStylesheets());
        }
        scene.getStylesheets().addAll(getSkinnable().getStylesheets());
    }

    private Image snapshot(Node node, double maxWidth, double maxHeight) {
        Bounds bounds = node.getBoundsInParent();
        double scale = Math.min(1, Math.min(maxWidth / Math.max(1, bounds.getWidth()),
                maxHeight / Math.max(1, bounds.getHeight())));
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        parameters.setTransform(Transform.scale(scale, scale));
        parameters.setViewport(new Rectangle2D(Math.floor(bounds.getMinX() * scale), Math.floor(bounds.getMinY() * scale),
                Math.max(1, Math.min(maxWidth, Math.ceil(bounds.getWidth() * scale))),
                Math.max(1, Math.min(maxHeight, Math.ceil(bounds.getHeight() * scale)))));
        return node.snapshot(parameters, null);
    }

    private void handleDragOver(DragEvent event) {
        String draggedId = draggedDockableId(event);
        if (draggedId == null) {
            if (getSkinnable().getDockingContext().canAcceptDockableDrop(event)) {
                handleExternalDragOver(event);
            } else {
                clearDropPreview();
                hidePaneEdgeTargets();
            }
            return;
        }
        DockingPane pane = getSkinnable();
        boolean wholeGroup = isTabGroupDrag(event);
        showPaneEdgeTargets(draggedId, wholeGroup);
        if (wholeGroup && pane.getLayoutRoot() instanceof DockingLayoutNode.Empty && isInsidePane(event)
                && pane.getDockingContext().canMoveTabGroup(pane, draggedId, null, DockingPosition.CENTER)) {
            showEmptyPanePreview();
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
            return;
        }
        DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
        if (paneEdge != null) {
            showPaneEdgePreview(paneEdge);
            event.acceptTransferModes(TransferMode.MOVE);
            event.consume();
            return;
        }
        DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), draggedId);
        if (target == null || !(wholeGroup
                ? pane.getDockingContext().canMoveTabGroup(pane, draggedId, target.getTargetId(), target.getPosition())
                : pane.canMoveDockable(draggedId, target.getTargetId(), target.getPosition()))) {
            clearDropPreview();
            event.consume();
            return;
        }
        showDropPreview(target);
        event.acceptTransferModes(TransferMode.MOVE);
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        String draggedId = draggedDockableId(event);
        if (draggedId == null) {
            if (getSkinnable().getDockingContext().canAcceptDockableDrop(event)) {
                handleExternalDragDropped(event);
            }
            return;
        }
        DockingPane pane = getSkinnable();
        boolean wholeGroup = isTabGroupDrag(event);
        showPaneEdgeTargets(draggedId, wholeGroup);
        DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
        boolean moved;
        if (wholeGroup && pane.getLayoutRoot() instanceof DockingLayoutNode.Empty && isInsidePane(event)) {
            moved = pane.getDockingContext().moveTabGroup(pane, draggedId, null, DockingPosition.CENTER);
        } else if (paneEdge != null) {
            moved = wholeGroup ? pane.getDockingContext().moveTabGroup(pane, draggedId, null, paneEdge)
                    : pane.moveDockableToPaneEdge(draggedId, paneEdge);
        } else {
            DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), draggedId);
            moved = target != null && (wholeGroup
                    ? pane.getDockingContext().moveTabGroup(pane, draggedId, target.getTargetId(), target.getPosition())
                    : pane.moveDockable(draggedId, target.getTargetId(), target.getPosition()));
        }
        if (moved) {
            event.setDropCompleted(true);
            event.consume();
        } else {
            recordUnsuccessfulDrop(draggedId);
            event.setDropCompleted(false);
            event.consume();
        }
        clearDropPreview();
        hidePaneEdgeTargets();
    }

    private void handleExternalDragOver(DragEvent event) {
        DockingPane pane = getSkinnable();
        showPaneEdgeTargets(null);
        DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
        if (paneEdge != null) {
            showPaneEdgePreview(paneEdge);
        } else if (pane.getLayoutRoot() instanceof DockingLayoutNode.Empty && isInsidePane(event)) {
            showEmptyPanePreview();
        } else {
            DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), null);
            if (target == null) {
                clearDropPreview();
                event.consume();
                return;
            }
            showDropPreview(target);
        }
        event.acceptTransferModes(TransferMode.COPY, TransferMode.MOVE);
        event.consume();
    }

    private void handleExternalDragDropped(DragEvent event) {
        DockingPane pane = getSkinnable();
        try {
            showPaneEdgeTargets(null);
            DockingPosition paneEdge = resolvePaneEdgeTarget(event.getX(), event.getY());
            boolean dropped;
            if (paneEdge != null) {
                dropped = pane.getDockingContext().dropDockable(pane, event, null, paneEdge).isPresent();
            } else if (pane.getLayoutRoot() instanceof DockingLayoutNode.Empty && isInsidePane(event)) {
                dropped = pane.getDockingContext().dropDockable(pane, event, null, DockingPosition.CENTER).isPresent();
            } else {
                DockingTree.ResolvedDropTarget target = resolveDropTarget(event.getX(), event.getY(), intersectedNode(event), null);
                dropped = target != null && pane.getDockingContext()
                        .dropDockable(pane, event, target.getTargetId(), target.getPosition()).isPresent();
            }
            event.setDropCompleted(dropped);
        } finally {
            event.consume();
            clearDropPreview();
            hidePaneEdgeTargets();
        }
    }

    private boolean isInsidePane(DragEvent event) {
        DockingPane pane = getSkinnable();
        return event.getX() >= 0 && event.getX() <= pane.getWidth()
                && event.getY() >= 0 && event.getY() <= pane.getHeight();
    }

    private void showPaneEdgeTargets(String draggedId) {
        showPaneEdgeTargets(draggedId, false);
    }

    private void showPaneEdgeTargets(String draggedId, boolean wholeGroup) {
        DockingPane pane = getSkinnable();
        boolean splitLayout = pane.getLayoutRoot() instanceof DockingLayoutNode.Split;
        for (Map.Entry<DockingPosition, StackPane> entry : paneEdgeTargets.entrySet()) {
            entry.getValue().setVisible(splitLayout
                    && (draggedId == null || (wholeGroup
                    ? pane.getDockingContext().canMoveTabGroup(pane, draggedId, null, entry.getKey())
                    : pane.canMoveDockableToPaneEdge(draggedId, entry.getKey()))));
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

    private void showEmptyPanePreview() {
        clearDropPreview();
        dropZonePreview.getStyleClass().add(previewStyle(DockingPosition.CENTER));
        dropZonePreview.resizeRelocate(0, 0, host.getWidth(), host.getHeight());
        dropZonePreview.setVisible(true);
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

    private static boolean isTabGroupDrag(DragEvent event) {
        Dragboard dragboard = event.getDragboard();
        if (dragboard != null && Boolean.TRUE.equals(dragboard.getContent(TAB_GROUP_FORMAT))) {
            return true;
        }
        Object source = event.getGestureSource();
        return source instanceof Node && Boolean.TRUE.equals(((Node) source).getProperties().get(TAB_GROUP_FORMAT));
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
        showPaneEdgeTargets(dockableId, dragWholeGroup);
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
        try {
            if (outsideContextDropPosition != null || isOutsideAllContextPanes(dropPosition.getX(), dropPosition.getY())) {
                if (dragWholeGroup) {
                    getSkinnable().getDockingContext().floatTabGroup(dockableId, dropPosition.getX(), dropPosition.getY());
                } else {
                    getSkinnable().getDockingContext().floatDockable(dockableId, dropPosition.getX(), dropPosition.getY());
                }
            }
        } finally {
            finishDrag();
        }
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

    private static List<DockingPane> contextPanes(Node node, com.dlsc.gemsfx.docking.DockingContext context) {
        List<DockingPane> panes = new ArrayList<>();
        collectContextPanes(node, context, panes);
        return panes;
    }

    private static void collectContextPanes(Node node, com.dlsc.gemsfx.docking.DockingContext context, List<DockingPane> panes) {
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
        dragWholeGroup = false;
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

    private static String previewStyle(com.dlsc.gemsfx.docking.DockingPosition position) {
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
