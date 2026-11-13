package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import javafx.geometry.Rectangle2D;
import javafx.geometry.Point2D;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.input.MouseButton;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DockingInternalFloatingTest extends FxTestBase {

    private DockingContext context() {
        return new DockingContext(id -> Optional.of(new Dockable(id, id, new Region())), pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    private DockingPane pane(DockingContext context) {
        DockingPane pane = context.createPane();
        pane.resize(1000, 600);
        context.openDockable(pane, "a", DockingPosition.CENTER);
        context.openDockable(pane, "b", DockingPosition.CENTER);
        context.openDockable(pane, "c", DockingPosition.RIGHT);
        return pane;
    }

    @Test
    public void floatingGroupPreservesOwnershipSelectionAndCanRedock() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            assertFalse(pane.isDockableFloating("a"));
            pane.selectDockable("b");
            context.floatTabGroupInternally("a", new Rectangle2D(650, 400, 300, 180));
            assertTrue(pane.isDockableFloating("a"));
            assertTrue(pane.isDockableFloating("b"));
            assertFalse(pane.isDockableFloating("c"));
            assertFalse(pane.isDockableFloating("unknown"));
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            assertEquals(List.of("a", "b"), pane.getFloatingGroups().get(0).getTabs().getDockableIds());
            assertEquals("b", pane.getFloatingGroups().get(0).getTabs().getSelectedId());
            assertEquals(3, pane.getDockables().size());
            assertSame(pane, context.getDockables().get(0).getDockingPane());
            assertEquals(2, context.getSelectionModel().getSelectedItems().size());
            assertTrue(context.moveTabGroup(pane, "a", "c", DockingPosition.CENTER));
            assertFalse(pane.isDockableFloating("a"));
            assertFalse(pane.isDockableFloating("b"));
            assertTrue(pane.getFloatingGroups().isEmpty());
            assertEquals(List.of("c", "a", "b"),
                    ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            assertEquals("b", pane.getActiveDockable().getId());
        });
    }

    @Test
    public void singleTabRedocksWithoutMovingItsFloatingSiblings() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            context.floatTabGroupInternally("a", new Rectangle2D(25, 30, 300, 180));
            assertTrue(pane.moveDockable("a", "c", DockingPosition.LEFT));
            assertEquals(List.of("b"), pane.getFloatingGroups().get(0).getTabs().getDockableIds());
            assertTrue(context.requestCloseDockable(pane.getDockables().stream()
                    .filter(d -> d.getId().equals("b")).findFirst().orElseThrow()));
            assertTrue(pane.getFloatingGroups().isEmpty());
            assertEquals(2, pane.getDockables().size());
        });
    }

    @Test
    public void restoresFloatingGeometryOrderAndSelectionInSamePane() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            context.floatTabGroupInternally("a", new Rectangle2D(650, 400, 300, 180));
            context.floatDockableInternally("c", new Rectangle2D(20, 25, 200, 150));
            pane.selectDockable("a");
            DockingLayout saved = context.getLayout();
            assertEquals(List.of("a", "b"),
                    saved.getPanes().get(0).getFloatingGroups().get(1).getTabs().getDockableIds());
            assertTrue(context.restoreLayout(saved).getUnresolvedDockableIds().isEmpty());
            assertEquals(2, pane.getFloatingGroups().size());
            DockingFloatingGroup restored = pane.getFloatingGroups().get(1);
            assertEquals("a", restored.getTabs().getSelectedId());
            assertEquals(new Rectangle2D(850, 600, 300, 180), restored.resolveBounds(1200, 800));
            assertTrue(context.moveTabGroup(pane, "a", null, DockingPosition.CENTER));
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
        });
    }

    @Test
    public void duplicateFloatingIdsAreRejectedBeforeChangingLiveLayout() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            context.floatDockableInternally("a", new Rectangle2D(20, 25, 200, 150));
            DockingFloatingGroup group = pane.getFloatingGroups().get(0);
            DockingLayout invalid = new DockingLayout("primary", List.of(new DockingLayout.PaneLayout(
                    "primary", group.getTabs(), null, List.of(group))));
            assertThrows(IllegalArgumentException.class, () -> context.restoreLayout(invalid));
            assertEquals(3, context.getDockables().size());
            assertEquals(1, pane.getFloatingGroups().size());
        });
    }

    @Test
    public void rendersAboveMaximizedGroupsAndKeepsGeometryAcrossPaneResizing() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatTabGroupInternally("a", new Rectangle2D(650, 400, 300, 180));
            root.applyCss();
            root.layout();
            Region frame = (Region) pane.lookup(".docking-floating-frame");
            assertNotNull(frame);
            assertEquals(650, frame.getLayoutX(), 0.01);
            assertEquals(400, frame.getLayoutY(), 0.01);
            pane.setMaximizedDockable(pane.getDockables().stream()
                    .filter(d -> d.getId().equals("c")).findFirst().orElseThrow());
            root.applyCss();
            root.layout();
            frame = (Region) pane.lookup(".docking-floating-frame");
            StackPane host = (StackPane) frame.getParent().getParent();
            assertTrue(host.getChildren().indexOf(frame.getParent())
                    > host.getChildren().indexOf(pane.lookup(".docking-maximized-overlay")));
            pane.selectDockable("a");
            assertNotNull("Activating a floating frame must not restore the maximized background",
                    pane.getMaximizedDockable());
            MenuButton menu = (MenuButton) frame.lookup(".docking-tab-pane-menu-button");
            assertFalse(menu.getItems().stream().anyMatch(item ->
                    item.getStyleClass().contains("docking-group-maximize") && item.isVisible()));
            root.resize(100, 80);
            root.layout();
            assertEquals(100, frame.getWidth(), 0.01);
            assertEquals(80, frame.getHeight(), 0.01);
            root.resize(1200, 800);
            root.layout();
            assertEquals(850, frame.getLayoutX(), 0.01);
            assertEquals(600, frame.getLayoutY(), 0.01);
            assertEquals(300, frame.getWidth(), 0.01);
            assertThrows(IllegalArgumentException.class, () -> pane.setMaximizedDockable(
                    pane.getDockables().stream().filter(d -> d.getId().equals("a")).findFirst().orElseThrow()));
        });
    }

    @Test
    public void headerDragAndCornerResizeUpdateSavedBoundsWithoutRedocking() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatDockableInternally("a", new Rectangle2D(100, 100, 300, 180));
            root.applyCss();
            root.layout();
            Region frame = (Region) pane.lookup(".docking-floating-frame");
            Node header = frame.lookup(".docking-tab-strip");
            gesture(header, MouseEvent.MOUSE_PRESSED, 80, 15);
            gesture(header, MouseEvent.MOUSE_DRAGGED, 130, 55);
            gesture(header, MouseEvent.MOUSE_RELEASED, 130, 55);
            root.layout();
            assertEquals(150, frame.getLayoutX(), 0.01);
            assertEquals(140, frame.getLayoutY(), 0.01);
            gesture(frame, MouseEvent.MOUSE_PRESSED, 299, 179);
            gesture(frame, MouseEvent.MOUSE_DRAGGED, 359, 219);
            gesture(frame, MouseEvent.MOUSE_RELEASED, 359, 219);
            root.layout();
            DockingFloatingGroup group = pane.getFloatingGroups().get(0);
            assertEquals(360, group.getWidth(), 0.01);
            assertEquals(220, group.getHeight(), 0.01);
            assertEquals(List.of("a"), group.getTabs().getDockableIds());
            assertEquals(2, ((DockingLayoutNode.Split) pane.getLayoutRoot()).getChildren().size());
        });
    }

    private static void gesture(Node target, javafx.event.EventType<MouseEvent> type, double x, double y) {
        Point2D scene = target.localToScene(x, y);
        Event.fireEvent(target, new MouseEvent(type, scene.getX(), scene.getY(), scene.getX(), scene.getY(),
                MouseButton.PRIMARY, 1, false, false, false, false, type != MouseEvent.MOUSE_RELEASED,
                false, false, false, false, type == MouseEvent.MOUSE_PRESSED, new PickResult(target, x, y)));
    }

    @Test
    public void mountedFramesCanRedockAndCloseWithoutLeavingContentBehind() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatTabGroupInternally("a", new Rectangle2D(100, 100, 300, 180));
            root.applyCss();
            root.layout();
            assertTrue(pane.moveDockable("a", "c", DockingPosition.CENTER));
            assertEquals(1, pane.lookupAll(".docking-floating-frame").size());
            Dockable b = pane.getDockables().stream().filter(d -> d.getId().equals("b")).findFirst().orElseThrow();
            assertTrue(context.requestCloseDockable(b));
            assertTrue(pane.lookupAll(".docking-floating-frame").isEmpty());
            assertNull(b.getContent().getParent());
        });
    }

    @Test
    public void resizingNarrowFrameAtPaneEdgeStaysWithinPane() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatDockableInternally("a", new Rectangle2D(950, 550, 50, 50));
            root.applyCss();
            root.layout();
            Region frame = (Region) pane.lookup(".docking-floating-frame");
            gesture(frame, MouseEvent.MOUSE_PRESSED, 49, 49);
            gesture(frame, MouseEvent.MOUSE_DRAGGED, 100, 100);
            gesture(frame, MouseEvent.MOUSE_RELEASED, 100, 100);
            assertTrue(frame.getLayoutX() + frame.getWidth() <= 1000);
            assertTrue(frame.getLayoutY() + frame.getHeight() <= 600);
        });
    }

    @Test
    public void restorePrunesUnresolvedFloatingTabsAndEmptyFrames() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            context.floatTabGroupInternally("a", new Rectangle2D(20, 25, 300, 180));
            context.floatDockableInternally("c", new Rectangle2D(600, 400, 200, 150));
            DockingLayout saved = context.getLayout();
            DockingContext restored = new DockingContext(id -> id.equals("a")
                    ? Optional.of(new Dockable(id, id, new Region())) : Optional.empty(), p -> {
                throw new AssertionError("Internal floating must not create stages");
            });
            DockingPane target = restored.createPane();
            assertEquals(Set.of("b", "c"), restored.restoreLayout(saved).getUnresolvedDockableIds());
            assertEquals(1, target.getFloatingGroups().size());
            assertEquals(List.of("a"), target.getFloatingGroups().get(0).getTabs().getDockableIds());
            assertEquals("a", target.getFloatingGroups().get(0).getTabs().getSelectedId());
            assertEquals(1, restored.getSelectionModel().getSelectedItems().size());
        });
    }

    @Test
    public void movingTemporarilyClampedFramePreservesRequestedSize() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatDockableInternally("a", new Rectangle2D(25, 100, 300, 180));
            root.applyCss();
            root.layout();
            root.resize(100, 600);
            root.layout();
            Region frame = (Region) pane.lookup(".docking-floating-frame");
            Node header = frame.lookup(".docking-tab-strip");
            gesture(header, MouseEvent.MOUSE_PRESSED, 15, 15);
            gesture(header, MouseEvent.MOUSE_DRAGGED, 15, 55);
            gesture(header, MouseEvent.MOUSE_RELEASED, 15, 55);
            root.resize(1000, 600);
            root.layout();
            assertEquals(300, frame.getWidth(), 0.01);
            assertEquals(180, frame.getHeight(), 0.01);
            assertEquals(140, frame.getLayoutY(), 0.01);
        });
    }

    @Test
    public void callbacksClosingLaterFloatingTabsDoNotProduceNullMoveEvents() {
        runFx(() -> {
            DockingContext context = context();
            pane(context);
            List<Dockable> moved = new ArrayList<>();
            context.addEventHandler(DockingEvent.MOVED, event -> {
                moved.add(event.getDockable());
                if (event.getDockable() != null && event.getDockable().getId().equals("a")) {
                    context.getDockables().stream().filter(d -> d.getId().equals("b")).findFirst()
                            .ifPresent(context::requestCloseDockable);
                }
            });
            context.floatTabGroupInternally("a", new Rectangle2D(25, 30, 300, 180));
            assertEquals(1, moved.size());
            assertEquals("a", moved.get(0).getId());
        });
    }

    @Test
    public void droppingOverFloatingFrameCannotDockIntoObscuredRegularGroup() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = pane(context);
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            context.floatDockableInternally("a", new Rectangle2D(50, 50, 400, 280));
            root.applyCss();
            root.layout();
            Node source = pane.lookupAll(".docking-tab").stream()
                    .filter(node -> "c".equals(node.getProperties().get("docking.dockable-id")))
                    .findFirst().orElseThrow();
            Node frame = pane.lookup(".docking-floating-frame");
            Point2D point = frame.localToScene(150, 100);
            DragEvent drop = new DragEvent(source, frame, DragEvent.DRAG_DROPPED, null,
                    point.getX(), point.getY(), point.getX(), point.getY(), TransferMode.MOVE,
                    source, frame, new PickResult(frame, 150, 100));
            Event.fireEvent(frame, drop);
            assertFalse(drop.isDropCompleted());
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Split);
        });
    }
}
