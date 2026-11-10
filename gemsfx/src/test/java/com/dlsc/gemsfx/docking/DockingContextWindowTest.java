package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import com.dlsc.gemsfx.skins.DockingPaneSkin;
import javafx.animation.PauseTransition;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Duration;
import org.junit.Assume;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class DockingContextWindowTest extends FxTestBase {

    private static Dockable dockable(String id) {
        return new Dockable(id, "Title " + id, new Label("Content " + id));
    }

    private static DockableFactory factoryFor(List<Dockable> dockables, AtomicInteger calls) {
        return id -> {
            calls.incrementAndGet();
            for (Dockable dockable : dockables) {
                if (dockable.getId().equals(id)) {
                    return Optional.of(dockable);
                }
            }
            return Optional.empty();
        };
    }

    private static List<String> ids(DockingPane pane) {
        DockingLayoutNode layout = pane.getLayoutRoot();
        if (layout instanceof DockingLayoutNode.Empty) {
            return List.of();
        }
        if (layout instanceof DockingLayoutNode.Tabs) {
            return ((DockingLayoutNode.Tabs) layout).getDockableIds();
        }
        throw new AssertionError("expected tabs or empty");
    }

    private static List<String> allIds(DockingLayoutNode layout) {
        if (layout instanceof DockingLayoutNode.Empty) {
            return List.of();
        }
        if (layout instanceof DockingLayoutNode.Tabs) {
            return ((DockingLayoutNode.Tabs) layout).getDockableIds();
        }
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) layout;
        List<String> ids = new ArrayList<>(allIds(split.getFirst()));
        ids.addAll(allIds(split.getSecond()));
        return ids;
    }

    private static Map<?, ?> contextMap(DockingContext context, String name) {
        try {
            Field field = DockingContext.class.getDeclaredField(name);
            field.setAccessible(true);
            return (Map<?, ?>) field.get(context);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static DockingPane renderWithEventSkin(DockingPane pane) {
        pane.setSkin(new EventTestDockingPaneSkin(pane));
        return layout(pane);
    }

    private static Node title(DockingPane pane, String id) {
        for (Node node : pane.lookupAll(".docking-tab")) {
            if (id.equals(node.getProperties().get("docking.dockable-id"))) {
                return node;
            }
        }
        throw new AssertionError("No rendered title for " + id);
    }

    private static void fireDragDetected(Node title) {
        fireDragDetected(title, false);
    }

    private static void fireDragDetected(Node title, boolean shiftDown) {
        Event.fireEvent(title, new MouseEvent(MouseEvent.DRAG_DETECTED,
                5, 5, 105, 105, MouseButton.PRIMARY, 1,
                shiftDown, false, false, false, true, false, false,
                false, false, true, new PickResult(title, 5, 5)));
    }

    private static Point2D paneEdgeTargetCenter(DockingPane pane, String selector) {
        Node target = pane.lookup(".docking-pane-edge-target" + selector);
        assertNotNull("The pane-wide drop indicator must exist", target);
        assertTrue("The pane-wide drop indicator must be visible during a drag", target.isVisible());
        Bounds bounds = pane.sceneToLocal(target.localToScene(target.getBoundsInLocal()));
        return new Point2D(bounds.getCenterX(), bounds.getCenterY());
    }

    private static DragEvent fireDragEvent(Node target, EventType<DragEvent> type, Node gestureSource,
                                           double x, double y, double screenX, double screenY,
                                           TransferMode transferMode, Node intersectedNode) {
        DragEvent event = new DragEvent(gestureSource, target, type, null, x, y, screenX, screenY, transferMode,
                gestureSource, target, new PickResult(intersectedNode == null ? target : intersectedNode, x, y));
        Event.fireEvent(target, event);
        return event;
    }

    private static final class EventTestDockingPaneSkin extends DockingPaneSkin {

        private EventTestDockingPaneSkin(DockingPane pane) {
            super(pane);
        }

        @Override
        protected boolean startDockableDrag(Node title, String dockableId) {
            return true;
        }
    }

    @Test
    public void customTabHeadersCanBeDraggedBetweenPanes() {
        runFx(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(dockable(id)), pane -> new Stage());
            context.setTabFactory(dockable -> new Label("Custom " + dockable.getTitle()));
            DockingPane source = context.createPane();
            context.openDockable(source, "a", DockingPosition.CENTER);
            renderWithEventSkin(source);
            DockingPane target = context.createPane();
            context.openDockable(target, "x", DockingPosition.CENTER);
            layout(target);
            Node dragged = title(source, "a");

            fireDragDetected(dragged);
            DragEvent drop = fireDragEvent(target, DragEvent.DRAG_DROPPED, dragged,
                    target.getWidth() / 2, target.getHeight() / 2, 400, 300, TransferMode.MOVE, title(target, "x"));
            fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 400, 300, TransferMode.MOVE, dragged);

            assertTrue(drop.isDropCompleted());
            assertTrue(source.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(List.of("x", "a"), ids(target));
            assertEquals("Custom Title a", ((Label) title(target, "a")).getText());
        });
    }

    @Test
    public void shiftDragMergesTheWholeGroupIntoAnotherPane() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("x"));
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> new Stage());
            DockingPane source = context.createPane();
            context.openDockable(source, "a", DockingPosition.CENTER);
            context.openDockable(source, "b", DockingPosition.CENTER);
            renderWithEventSkin(source);
            DockingPane target = context.createPane();
            context.openDockable(target, "x", DockingPosition.CENTER);
            layout(target);
            Node dragged = title(source, "a");

            fireDragDetected(dragged, true);
            DragEvent drop = fireDragEvent(target, DragEvent.DRAG_DROPPED, dragged,
                    target.getWidth() / 2, target.getHeight() / 2, 400, 300, TransferMode.MOVE, title(target, "x"));
            fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 400, 300, TransferMode.MOVE, dragged);

            assertTrue(drop.isDropCompleted());
            assertTrue(source.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(List.of("x", "a", "b"), ids(target));
            assertEquals("b", target.getActiveDockable().getId());
            return null;
        });
    }

    @Test
    public void shiftDragIntoEmptyPaneShowsPreviewAndMovesTheGroup() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"));
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> new Stage());
            DockingPane source = context.createPane();
            context.openDockable(source, "a", DockingPosition.CENTER);
            context.openDockable(source, "b", DockingPosition.CENTER);
            renderWithEventSkin(source);
            DockingPane target = layout(context.createPane());
            Node dragged = title(source, "a");
            double x = target.getWidth() / 2;
            double y = target.getHeight() / 2;

            fireDragDetected(dragged, true);
            fireDragEvent(target, DragEvent.DRAG_OVER, dragged, x, y, 400, 300, null, target);
            assertTrue(target.lookup(".docking-drop-zone-preview").isVisible());
            DragEvent drop = fireDragEvent(target, DragEvent.DRAG_DROPPED, dragged,
                    x, y, 400, 300, TransferMode.MOVE, target);
            fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 400, 300, TransferMode.MOVE, dragged);

            assertTrue(drop.isDropCompleted());
            assertTrue(source.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(List.of("a", "b"), ids(target));
            assertEquals("b", target.getActiveDockable().getId());
            assertFalse(target.lookup(".docking-drop-zone-preview").isVisible());
            return null;
        });
    }

    @Test
    public void shiftDragToPaneEdgeKeepsGroupTogetherAndDoesNotAffectNextNormalDrag() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("x"));
            AtomicReference<DockingPane> floatingPane = new AtomicReference<>();
            AtomicReference<Stage> floatingStage = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane, 300, 200));
                floatingPane.set(pane);
                floatingStage.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            context.openDockable(host, "x", DockingPosition.RIGHT);
            host.selectDockable("b");
            renderWithEventSkin(host);
            Node dragged = title(host, "a");

            fireDragDetected(dragged, true);
            Point2D point = paneEdgeTargetCenter(host, ".pane-edge-bottom");
            DragEvent drop = fireDragEvent(host, DragEvent.DRAG_DROPPED, dragged,
                    point.getX(), point.getY(), 400, 300, TransferMode.MOVE, host);
            fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 400, 300, TransferMode.MOVE, dragged);
            assertTrue(drop.isDropCompleted());
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) host.getLayoutRoot();
            assertEquals(List.of("x"), ((DockingLayoutNode.Tabs) root.getFirst()).getDockableIds());
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) root.getSecond()).getDockableIds());
            assertEquals("b", host.getActiveDockable().getId());

            host.applyCss();
            host.layout();
            Node next = title(host, "a");
            fireDragDetected(next);
            fireDragEvent(next, DragEvent.DRAG_DONE, next, 5, 5, 777, 888, null, next);
            assertEquals(List.of("a"), ids(floatingPane.get()));
            assertEquals(List.of("x", "b"), allIds(host.getLayoutRoot()));
            floatingStage.get().close();
            return null;
        });
    }

    @Test
    public void shiftDragOutsideFloatsOneGroupAndKeepsItsSelection() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
            AtomicReference<DockingPane> floatingPane = new AtomicReference<>();
            AtomicReference<Stage> floatingStage = new AtomicReference<>();
            AtomicInteger stageCalls = new AtomicInteger();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                stageCalls.incrementAndGet();
                Stage stage = new Stage();
                stage.setScene(new Scene(pane, 300, 200));
                floatingPane.set(pane);
                floatingStage.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            context.openDockable(host, "c", DockingPosition.RIGHT);
            host.selectDockable("b");
            renderWithEventSkin(host);
            Node dragged = title(host, "a");

            fireDragDetected(dragged, true);
            fireDragEvent(dragged, DragEvent.DRAG_DONE, dragged, 5, 5, 777, 888, null, dragged);

            assertEquals(1, stageCalls.get());
            assertEquals(List.of("a", "b"), ids(floatingPane.get()));
            assertEquals("b", floatingPane.get().getActiveDockable().getId());
            assertEquals(List.of("c"), ids(host));
            assertEquals(777d, floatingStage.get().getX(), 0d);
            assertEquals(888d, floatingStage.get().getY(), 0d);
            floatingStage.get().close();
            return null;
        });
    }

    @Test
    public void floatDockableUsesStageFactoryAndDropCoordinates() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicInteger calls = new AtomicInteger();
            AtomicReference<DockingPane> floatingPaneRef = new AtomicReference<>();
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, calls), pane -> {
                floatingPaneRef.set(pane);
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);

            DockingPane floatingPane = context.floatDockable("a", 321, 654);

            assertSame(floatingPaneRef.get(), floatingPane);
            assertNotSame(host, floatingPane);
            assertEquals(List.of("a"), ids(floatingPane));
            assertEquals(List.of("b"), ids(host));
            assertEquals(321, floatingStageRef.get().getX(), 0);
            assertEquals(654, floatingStageRef.get().getY(), 0);
            assertEquals(2, context.getDockables().size());
            assertEquals(2, calls.get());
            return null;
        });
    }

    @Test
    public void dockableCanMoveAcrossPanesInSameContext() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane));
                return stage;
            });
            DockingPane leftPane = context.createPane();
            DockingPane rightPane = context.createPane();
            context.openDockable(leftPane, "a", DockingPosition.CENTER);
            context.openDockable(rightPane, "b", DockingPosition.CENTER);

            assertTrue(rightPane.moveDockable("a", "b", DockingPosition.CENTER));
            assertTrue(leftPane.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(List.of("b", "a"), ids(rightPane));
            assertEquals("a", rightPane.getActiveDockable().getId());
            assertEquals(2, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void closeAndHelpButtonsFadeWithTabAndContentAreaHover() throws InterruptedException {
        DockingPane pane = invoke(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(dockable(id)), floatingPane -> new Stage());
            DockingPane created = context.createPane();
            context.openDockable(created, "a", DockingPosition.CENTER).orElseThrow().setOnHelp(() -> {});
            context.openDockable(created, "b", DockingPosition.CENTER).orElseThrow().setOnHelp(() -> {});
            context.openDockable(created, "c", DockingPosition.RIGHT).orElseThrow().setOnHelp(() -> {});
            for (Dockable dockable : context.getDockables()) {
                dockable.getMenuItems().add(new MenuItem("Inspect"));
            }
            return layout(created);
        });

        for (String target : List.of("a", "b", "content")) {
            String id = target.equals("content") ? "b" : target;
            Node hoverTarget = invoke(() -> target.equals("content")
                    ? pane.getDockables().stream().filter(dockable -> dockable.getId().equals(id))
                            .findFirst().orElseThrow().getContent().getParent()
                    : title(pane, id));
            CountDownLatch fadedIn = new CountDownLatch(1);
            CountDownLatch fadedOut = new CountDownLatch(1);
            AtomicBoolean fadingOut = new AtomicBoolean();
            AtomicBoolean intermediateOpacity = new AtomicBoolean();
            AtomicBoolean intermediateHelpOpacity = new AtomicBoolean();
            AtomicBoolean intermediateFadeOut = new AtomicBoolean();
            double width = invoke(() -> {
                Node tab = title(pane, id);
                Node close = tab.lookup(".docking-tab-close-button");
                Node help = tab.lookup(".docking-tab-help-button");
                Node menu = tab.lookup(".docking-tab-menu-button");
                assertEquals(0, close.getOpacity(), 0);
                assertEquals(0, help.getOpacity(), 0);
                assertEquals(0, menu.getOpacity(), 0);
                help.opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
                    if (newOpacity.doubleValue() > 0 && newOpacity.doubleValue() < 1) {
                        intermediateHelpOpacity.set(true);
                    }
                });
                close.opacityProperty().addListener((obs, oldOpacity, newOpacity) -> {
                    if (newOpacity.doubleValue() > 0 && newOpacity.doubleValue() < 1) {
                        intermediateOpacity.set(true);
                        if (fadingOut.get()) {
                            intermediateFadeOut.set(true);
                        }
                    }
                    if (newOpacity.doubleValue() == 1) {
                        fadedIn.countDown();
                    }
                    if (newOpacity.doubleValue() == 0) {
                        fadedOut.countDown();
                    }
                });
                fireHover(hoverTarget, true);
                pane.applyCss();
                assertEquals("Hover must start a fade, not instantly reveal the button", 0, close.getOpacity(), 0);
                return tab.getBoundsInParent().getWidth();
            });

            assertTrue("Close button did not finish fading in", fadedIn.await(5, TimeUnit.SECONDS));
            runFx(() -> {
                Node tab = title(pane, id);
                Node close = tab.lookup(".docking-tab-close-button");
                Node help = tab.lookup(".docking-tab-help-button");
                Node menu = tab.lookup(".docking-tab-menu-button");
                assertTrue("The fade must include intermediate opacity values", intermediateOpacity.get());
                assertTrue("Help must fade in with Close", intermediateHelpOpacity.get());
                assertEquals(1, close.getOpacity(), 0);
                assertEquals(1, help.getOpacity(), 0);
                assertEquals(1, menu.getOpacity(), 0);
                assertEquals(width, tab.getBoundsInParent().getWidth(), 0);
                for (String otherId : List.of("a", "b", "c")) {
                    if (!otherId.equals(id)) {
                        Node otherTab = title(pane, otherId);
                        assertEquals(0, otherTab.lookup(".docking-tab-close-button").getOpacity(), 0);
                        assertEquals(0, otherTab.lookup(".docking-tab-help-button").getOpacity(), 0);
                        assertEquals(0, otherTab.lookup(".docking-tab-menu-button").getOpacity(), 0);
                    }
                }
                if (!target.equals("content")) {
                    Node otherGroupTitle = title(pane, "c");
                    Event.fireEvent(tab, new MouseEvent(MouseEvent.MOUSE_CLICKED,
                            5, 5, 105, 105, MouseButton.PRIMARY, 1,
                            false, false, false, false, false, false, false,
                            false, false, true, new PickResult(tab, 5, 5)));
                    pane.applyCss();
                    pane.layout();
                    assertSame("Selection must retain the hovered tab header", tab, title(pane, id));
                    assertSame(otherGroupTitle, title(pane, "c"));
                    assertEquals(id, pane.getActiveDockable().getId());
                    assertTrue(tab.getStyleClass().contains("selected"));
                    assertEquals(1, close.getOpacity(), 0);
                    assertEquals(1, help.getOpacity(), 0);
                    assertEquals(1, menu.getOpacity(), 0);
                    assertSame(tab.getParent().getParent(),
                            pane.getActiveDockable().getContent().getParent().getParent());
                }

                if (target.equals("content")) {
                    fireHover(tab, true);
                    fireHover(hoverTarget, false);
                    assertEquals(1, close.getOpacity(), 0);
                    fireHover(hoverTarget, true);
                    fireHover(tab, false);
                    assertEquals(1, close.getOpacity(), 0);
                }
                fadingOut.set(true);
                fireHover(hoverTarget, false);
                pane.applyCss();
                assertEquals("Leaving must fade the button out, not instantly hide it", 1, close.getOpacity(), 0);
                assertEquals(1, help.getOpacity(), 0);
                assertEquals(1, menu.getOpacity(), 0);
            });
            assertTrue("Close button did not finish fading out", fadedOut.await(5, TimeUnit.SECONDS));
            runFx(() -> {
                Node tab = title(pane, id);
                Node close = tab.lookup(".docking-tab-close-button");
                Node help = tab.lookup(".docking-tab-help-button");
                Node menu = tab.lookup(".docking-tab-menu-button");
                assertTrue("The fade-out must include intermediate opacity values", intermediateFadeOut.get());
                assertEquals(0, close.getOpacity(), 0);
                assertEquals(0, help.getOpacity(), 0);
                assertEquals(0, menu.getOpacity(), 0);
            });
        }
    }

    @Test
    public void menuKeepsActionsVisibleWhileOpenAndClosesWhenItemsAreCleared() throws InterruptedException {
        DockingPane pane = invoke(() -> {
            DockingContext context = new DockingContext(id -> {
                Dockable dockable = dockable(id);
                dockable.getMenuItems().add(new MenuItem("Inspect"));
                return Optional.of(dockable);
            }, floatingPane -> new Stage());
            DockingPane created = context.createPane();
            context.openDockable(created, "a", DockingPosition.CENTER);
            context.openDockable(created, "b", DockingPosition.CENTER);
            return layout(created);
        });
        CountDownLatch fadedIn = new CountDownLatch(1);
        CountDownLatch fadedOut = new CountDownLatch(1);
        try {
            runFx(() -> {
                Stage stage = (Stage) pane.getScene().getWindow();
                stage.show();
                Node tab = title(pane, "a");
                Node close = tab.lookup(".docking-tab-close-button");
                close.opacityProperty().addListener((obs, oldValue, value) -> {
                    if (value.doubleValue() == 1) {
                        fadedIn.countDown();
                    } else if (value.doubleValue() == 0) {
                        fadedOut.countDown();
                    }
                });
                ((MenuButton) tab.lookup(".docking-tab-menu-button")).show();
            });
            assertTrue("An open menu must reveal its tab actions", fadedIn.await(5, TimeUnit.SECONDS));
            CountDownLatch popupPulse = new CountDownLatch(1);
            runFx(() -> {
                fireHover(title(pane, "a"), false);
                PauseTransition pause = new PauseTransition(Duration.millis(200));
                pause.setOnFinished(event -> popupPulse.countDown());
                pause.play();
            });
            assertTrue(popupPulse.await(5, TimeUnit.SECONDS));
            runFx(() -> {
                Node tab = title(pane, "a");
                MenuButton menu = (MenuButton) tab.lookup(".docking-tab-menu-button");
                assertTrue(menu.isShowing());
                assertEquals(1, menu.getOpacity(), 0);
                assertEquals(1, tab.lookup(".docking-tab-close-button").getOpacity(), 0);
                assertEquals("b", pane.getActiveDockable().getId());
                pane.getDockables().stream().filter(dockable -> dockable.getId().equals("a"))
                        .findFirst().orElseThrow().getMenuItems().clear();
                assertFalse(menu.isShowing());
                assertFalse(menu.isManaged());
            });
            assertTrue("Actions must fade out when the menu closes", fadedOut.await(5, TimeUnit.SECONDS));
        } finally {
            runFx(() -> ((Stage) pane.getScene().getWindow()).close());
        }
    }

    private static void fireHover(Node node, boolean hovered) {
        Event.fireEvent(node, new MouseEvent(hovered ? MouseEvent.MOUSE_ENTERED : MouseEvent.MOUSE_EXITED,
                5, 5, 105, 105, MouseButton.NONE, 0,
                false, false, false, false, false, false, false,
                false, false, true, new PickResult(node, 5, 5)));
    }

    @Test
    public void titleTooltipsFollowTruncationResizingAndRenaming() {
        runFx(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(dockable(id)), pane -> new Stage());
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            StackPane root = (StackPane) pane.getParent();

            for (String id : List.of("a", "b")) {
                Dockable dockable = pane.getDockables().stream()
                        .filter(candidate -> id.equals(candidate.getId())).findFirst().orElseThrow();
                Label label = (Label) title(pane, id).lookup(".docking-tab-title");
                assertNull(label.getTooltip());

                String fullTitle = "Mission Control ".repeat(10);
                dockable.setTitle(fullTitle);
                root.applyCss();
                root.layout();
                assertNotNull("Truncated titles need a full-name tooltip", label.getTooltip());
                assertEquals(fullTitle, label.getTooltip().getText());

                dockable.setTitle(fullTitle + "Renamed");
                root.layout();
                assertEquals(fullTitle + "Renamed", label.getTooltip().getText());

                root.resize(4000, 600);
                root.layout();
                assertNull("A fully visible title must not have a tooltip", label.getTooltip());

                root.resize(300, 600);
                root.layout();
                assertNotNull(label.getTooltip());
                dockable.setTitle("Short");
                root.layout();
                assertNull("Title width " + label.getWidth() + ", preferred width " + label.prefWidth(-1),
                        label.getTooltip());
            }
        });
    }

    @Test
    public void lastTabHasNoRightSeparatorAfterSelectionAndClosing() {
        runFx(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(dockable(id)), pane -> new Stage());
            DockingPane pane = context.createPane();
            Dockable first = context.openDockable(pane, "a", DockingPosition.CENTER).orElseThrow();
            Dockable second = context.openDockable(pane, "b", DockingPosition.CENTER).orElseThrow();
            Dockable last = context.openDockable(pane, "c", DockingPosition.CENTER).orElseThrow();
            layout(pane);

            assertEquals(1, ((Region) title(pane, "a")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);
            assertEquals(0, ((Region) title(pane, "c")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);
            pane.selectDockable(first.getId());
            pane.applyCss();
            assertEquals(0, ((Region) title(pane, "c")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);

            context.requestCloseDockable(last);
            pane.applyCss();
            assertEquals(1, ((Region) title(pane, "a")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);
            assertEquals(0, ((Region) title(pane, "b")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);
            context.requestCloseDockable(second);
            pane.applyCss();
            assertEquals(0, ((Region) title(pane, "a")).getBorder().getStrokes().get(0).getWidths().getRight(), 0);
        });
    }

    @Test
    public void changingDockableTitleUpdatesTabAndAccessibilityText() {
        invoke(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = new DockingContext(id -> Optional.of(dockable), pane -> new Stage());
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            Node tab = title(pane, "a");
            Label titleLabel = (Label) tab.lookup(".docking-tab-title");
            Node closeButton = tab.lookup(".docking-tab-close-button");
            assertNotNull(titleLabel);
            assertNotNull(closeButton);

            dockable.setTitle("Renamed");

            assertEquals("Renamed", titleLabel.getText());
            assertEquals("Renamed", tab.getAccessibleText());
            assertTrue(closeButton.getAccessibleText().contains("Renamed"));
            return null;
        });
    }

    @Test
    public void changingDockableGraphicUpdatesUnselectedTabWithoutRebuildingLayout() {
        invoke(() -> {
            Label initial = new Label("Initial icon");
            Label replacement = new Label("Replacement icon");
            Dockable a = new Dockable("a", "Title a", initial, new Label("Content a"));
            Dockable b = dockable("b");
            DockingContext context = new DockingContext(factoryFor(List.of(a, b), new AtomicInteger()), pane -> new Stage());
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node tab = title(pane, "a");
            Label label = (Label) tab.lookup(".docking-tab-title");
            DockingLayoutNode originalLayout = pane.getLayoutRoot();

            a.setGraphic(replacement);
            assertSame(replacement, label.getGraphic());
            pane.applyCss();
            pane.layout();
            assertNull(initial.getParent());

            a.setGraphic(null);
            assertNull(label.getGraphic());
            pane.applyCss();
            pane.layout();
            assertNull(replacement.getParent());
            assertSame(tab, title(pane, "a"));
            assertSame(originalLayout, pane.getLayoutRoot());
            assertSame(b, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void graphicBindingsFollowFloatingAndRedockingAndAreReleasedOnDisposal() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = new DockingContext(factoryFor(List.of(a, b), new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 400, 300));
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            layout(host);
            Label originalLabel = (Label) title(host, "a").lookup(".docking-tab-title");
            Label initial = new Label("Initial icon");
            a.setGraphic(initial);
            assertSame(initial, originalLabel.getGraphic());

            DockingPane floating = context.floatDockable("a", 10, 20);
            try {
                floating.getScene().getRoot().applyCss();
                floating.getScene().getRoot().layout();
                Label floatingLabel = (Label) title(floating, "a").lookup(".docking-tab-title");
                assertNull(originalLabel.getGraphic());
                assertSame(initial, floatingLabel.getGraphic());

                Label replacement = new Label("Floating icon");
                a.setGraphic(replacement);
                assertSame(replacement, floatingLabel.getGraphic());
                assertNull(originalLabel.getGraphic());

                assertTrue(host.moveDockable("a", "b", DockingPosition.CENTER));
                Label restoredLabel = (Label) title(host, "a").lookup(".docking-tab-title");
                assertNull(floatingLabel.getGraphic());
                assertSame(replacement, restoredLabel.getGraphic());
                a.setGraphic(null);
                assertNull(restoredLabel.getGraphic());

                a.setGraphic(initial);
                host.setSkin(null);
                assertNull(restoredLabel.getGraphic());
                a.setGraphic(replacement);
                assertNull(restoredLabel.getGraphic());
            } finally {
                context.requestCloseDockable(a);
            }
            return null;
        });
    }

    @Test
    public void emptyFloatingPaneClosesAutomaticallyWhenLastDockableLeaves() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            DockingPane floating = context.floatDockable("a", 10, 20);
            Stage floatingStage = floatingStageRef.get();
            floatingStage.show();
            assertTrue(floatingStage.isShowing());

            assertTrue(host.moveDockable("a", "b", DockingPosition.CENTER));

            assertTrue(floating.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertFalse(floatingStage.isShowing());
            assertEquals(List.of("b", "a"), ids(host));
            return null;
        });
    }

    @Test
    public void closingHostWindowClosesItsFloatingStages() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            Stage hostStage = new Stage();
            hostStage.setScene(new Scene(host, 600, 400));
            hostStage.show();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            context.floatDockable("a", 80, 90);
            Stage floatingStage = floatingStageRef.get();
            floatingStage.show();
            assertTrue(floatingStage.isShowing());

            hostStage.close();

            assertFalse(hostStage.isShowing());
            assertFalse(floatingStage.isShowing());
            assertEquals(List.of(b), context.getDockables());
            DockingLayout layout = context.getLayout();
            assertEquals(1, layout.getPanes().size());
            assertEquals(List.of("b"), ids(layout.getPanes().get(0).getRoot()));
            return null;
        });
    }

    @Test
    public void closingFloatingStageRemovesListenerFromItsRegisteredHost() {
        invoke(() -> {
            Dockable a = dockable("a");
            Stage unrelatedOwner = new Stage();
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(id -> Optional.of(a), pane -> {
                Stage floating = new Stage();
                floating.initOwner(unrelatedOwner);
                floating.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingStageRef.set(floating);
                return floating;
            });
            DockingPane hostPane = context.createPane();
            Stage host = new Stage();
            host.setScene(new Scene(hostPane, 600, 400));
            host.show();
            context.openDockable(hostPane, "a", DockingPosition.CENTER);
            DockingPane floatingPane = context.floatDockable("a", 80, 90);
            Stage floatingStage = floatingStageRef.get();
            floatingStage.show();

            floatingStage.close();

            assertTrue(floatingPane.getDockables().isEmpty());
            assertTrue(contextMap(context, "hostListeners").isEmpty());
            assertTrue(contextMap(context, "floatingStagesByHost").isEmpty());
            host.close();
            unrelatedOwner.close();
            return null;
        });
    }

    @Test
    public void stageFactoryFailurePreservesSourcePaneAndBubblesTheError() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                throw new IllegalStateException("boom");
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            DockingLayoutNode before = host.getLayoutRoot();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> context.floatDockable("a", 5, 8));

            assertEquals("boom", ex.getMessage());
            assertSame(before, host.getLayoutRoot());
            assertEquals(List.of("a", "b"), ids(host));
            assertEquals(2, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void stageFactoryFailureDoesNotLeaveCandidatePaneInContext() {
        invoke(() -> {
            Dockable a = dockable("a");
            DockingContext context = new DockingContext(id -> Optional.of(a), pane -> {
                throw new IllegalStateException("stage-failure");
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);

            assertThrows(IllegalStateException.class, () -> context.floatDockable("a", 10, 20));

            DockingLayout layout = context.getLayout();
            assertEquals(1, layout.getPanes().size());
            assertEquals(List.of("a"), ids(layout.getPanes().get(0).getRoot()));
            return null;
        });
    }

    @Test
    public void invalidFloatingStageIsClosedAndCandidatePaneIsRemoved() {
        invoke(() -> {
            Dockable a = dockable("a");
            AtomicReference<Stage> candidate = new AtomicReference<>();
            DockingContext context = new DockingContext(id -> Optional.of(a), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new Label("not the pane")));
                stage.show();
                candidate.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);

            assertThrows(IllegalStateException.class, () -> context.floatDockable("a", 10, 20));

            assertFalse(candidate.get().isShowing());
            assertEquals(1, context.getLayout().getPanes().size());
            return null;
        });
    }

    private static List<String> ids(DockingLayoutNode node) {
        if (node instanceof DockingLayoutNode.Empty) {
            return List.of();
        }
        if (node instanceof DockingLayoutNode.Tabs) {
            return ((DockingLayoutNode.Tabs) node).getDockableIds();
        }
        DockingLayoutNode.Split split = (DockingLayoutNode.Split) node;
        List<String> ids = new ArrayList<>(ids(split.getFirst()));
        ids.addAll(ids(split.getSecond()));
        return ids;
    }

    @Test
    public void closeVetoKeepsFloatingStageAndAllDockables() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            Dockable c = dockable("c");
            List<Dockable> all = List.of(a, b, c);
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            AtomicReference<DockingPane> floatingPaneRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                floatingPaneRef.set(pane);
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            context.openDockable(host, "c", DockingPosition.CENTER);
            context.floatDockable("a", 20, 30);
            assertTrue(floatingPaneRef.get().moveDockable("b", "a", DockingPosition.CENTER));
            Stage floatingStage = floatingStageRef.get();
            floatingStage.show();
            context.setCanCloseDockable(d -> !"b".equals(d.getId()));

            WindowEvent event = new WindowEvent(floatingStage, WindowEvent.WINDOW_CLOSE_REQUEST);
            floatingStage.fireEvent(event);

            assertTrue(event.isConsumed());
            assertTrue(floatingStage.isShowing());
            assertEquals(List.of("a", "b"), ids(floatingPaneRef.get()));
            assertEquals(List.of("c"), ids(host));
            assertEquals(3, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void escapeCanceledDragDoesNotCreateFloatingPane() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicInteger stageFactoryCalls = new AtomicInteger();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                stageFactoryCalls.incrementAndGet();
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.RIGHT);
            renderWithEventSkin(host);
            Node title = title(host, "a");

            fireDragDetected(title);
            for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                assertTrue(indicator.isVisible());
            }
            Event.fireEvent(host, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));
            for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                assertFalse(indicator.isVisible());
            }
            fireDragEvent(title, DragEvent.DRAG_DONE, title,
                    5, 5, 400, 500, null, title);

            assertEquals(0, stageFactoryCalls.get());
            assertEquals(List.of("a", "b"), allIds(host.getLayoutRoot()));
            assertEquals(2, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void unsuccessfulNonEscapeDragDoesNotCreateFloatingPane() {
        invoke(() -> {
            Dockable a = dockable("a");
            List<Dockable> all = List.of(a);
            AtomicInteger stageFactoryCalls = new AtomicInteger();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                stageFactoryCalls.incrementAndGet();
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            renderWithEventSkin(host);
            Node title = title(host, "a");

            for (double x : new double[]{host.getWidth() / 2, host.getWidth() - 2}) {
                fireDragDetected(title);
                DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, title,
                        x, host.getHeight() / 2, 470, 520, null, title);
                fireDragEvent(title, DragEvent.DRAG_DONE, title,
                        5, 5, 470, 520, null, title);

                assertFalse(dropped.isDropCompleted());
            }
            assertEquals(0, stageFactoryCalls.get());
            assertEquals(List.of("a"), ids(host));
            assertEquals(1, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void outsideDropStillFloatsAtExactCoordinates() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicReference<DockingPane> floatingPaneRef = new AtomicReference<>();
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                floatingPaneRef.set(pane);
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            renderWithEventSkin(host);
            Node title = title(host, "a");

            fireDragDetected(title);
            fireDragEvent(title, DragEvent.DRAG_DONE, title,
                    5, 5, 777, 888, null, title);

            assertNotNull(floatingPaneRef.get());
            assertNotSame(host, floatingPaneRef.get());
            assertEquals(List.of("a"), ids(floatingPaneRef.get()));
            assertEquals(List.of("b"), ids(host));
            assertEquals(777d, floatingStageRef.get().getX(), 0d);
            assertEquals(888d, floatingStageRef.get().getY(), 0d);
            return null;
        });
    }

    @Test
    public void nativeDragDoneWithZeroCoordinatesFloatsAtThePointerPosition() {
        Assume.assumeTrue("Keep pointer movement inside the headless test toolkit",
                "Monocle".equals(System.getProperty("glass.platform")));
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"));
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane, 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            renderWithEventSkin(host);
            Node source = title(host, "a");
            Robot robot = new Robot();
            Point2D originalPosition = robot.getMousePosition();
            try {
                fireDragDetected(source);
                robot.mouseMove(620, 430);
                fireDragEvent(source, DragEvent.DRAG_DONE, source, 0, 0, 0, 0, null, source);

                assertNotNull(floatingStageRef.get());
                assertEquals(620, floatingStageRef.get().getX(), 0);
                assertEquals(430, floatingStageRef.get().getY(), 0);
                assertEquals(List.of("b"), ids(host));
                return null;
            } finally {
                robot.mouseMove(originalPosition);
            }
        });
    }

    @Test
    public void externalDropCoordinatesSurviveZeroCoordinateDragDone() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"));
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane, 200, 120));
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            host.setSkin(new EventTestDockingPaneSkin(host));
            StackPane outside = new StackPane();
            layout(new HBox(host, outside));
            Node source = title(host, "a");

            fireDragDetected(source);
            DragEvent dropped = fireDragEvent(outside, DragEvent.DRAG_DROPPED, source,
                    5, 5, -320, 240, TransferMode.MOVE, outside);
            fireDragEvent(source, DragEvent.DRAG_DONE, source, 0, 0, 0, 0, TransferMode.MOVE, source);

            assertTrue(dropped.isDropCompleted());
            assertNotNull(floatingStageRef.get());
            assertEquals(-320, floatingStageRef.get().getX(), 0);
            assertEquals(240, floatingStageRef.get().getY(), 0);
            assertEquals(List.of("b"), ids(host));
            return null;
        });
    }

    @Test
    public void titlesInsideNestedSplitsCanFloatBeforeAndAfterTreeRebuilds() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
            AtomicReference<DockingPane> floatingPaneRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                floatingPaneRef.set(pane);
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.RIGHT);
            context.openDockable(host, "c", DockingPosition.BOTTOM);
            renderWithEventSkin(host);

            for (String id : List.of("c", "b")) {
                Node sourceTitle = title(host, id);
                Scene scene = sourceTitle.getScene();
                Event.fireEvent(sourceTitle, new MouseEvent(MouseEvent.MOUSE_PRESSED,
                        5, 5, 105, 105, MouseButton.PRIMARY, 1,
                        false, false, false, false, true, false, false,
                        false, false, true, new PickResult(sourceTitle, 5, 5)));
                assertSame(scene, sourceTitle.getScene());

                fireDragDetected(sourceTitle);
                fireDragEvent(sourceTitle, DragEvent.DRAG_DONE, sourceTitle,
                        5, 5, 777, 888, null, sourceTitle);

                assertNotNull("Dragging a title inside a split must create a floating pane", floatingPaneRef.get());
                assertEquals(List.of(id), ids(floatingPaneRef.get()));
                assertFalse(allIds(host.getLayoutRoot()).contains(id));
                floatingPaneRef.set(null);
                host.applyCss();
                host.layout();
            }

            assertEquals(List.of("a"), ids(host));
            assertEquals(3, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void acceptedInPaneDropDoesNotAlsoCreateFloatingPane() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicInteger stageFactoryCalls = new AtomicInteger();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                stageFactoryCalls.incrementAndGet();
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.CENTER);
            renderWithEventSkin(host);
            Node sourceTitle = title(host, "a");
            Node targetTitle = title(host, "b");

            fireDragDetected(sourceTitle);
            DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, sourceTitle,
                    host.getWidth() / 2, host.getHeight() / 2,
                    200, 200, TransferMode.MOVE, targetTitle);
            fireDragEvent(sourceTitle, DragEvent.DRAG_DONE, sourceTitle,
                    5, 5, 200, 200, TransferMode.MOVE, sourceTitle);

            assertTrue(dropped.isDropCompleted());
            assertEquals(List.of("b", "a"), ids(host));
            assertEquals(0, stageFactoryCalls.get());
            return null;
        });
    }

    @Test
    public void selectedTabCanBeDraggedToEachEdgeOfItsOwnGroup() {
        Object[][] cases = {
                {0.02, 0.5, Orientation.HORIZONTAL, List.of("b"), List.of("a", "c")},
                {0.98, 0.5, Orientation.HORIZONTAL, List.of("a", "c"), List.of("b")},
                {0.5, 0.02, Orientation.VERTICAL, List.of("b"), List.of("a", "c")},
                {0.5, 0.98, Orientation.VERTICAL, List.of("a", "c"), List.of("b")}
        };
        for (Object[] testCase : cases) {
            invoke(() -> {
                Dockable b = dockable("b");
                List<Dockable> all = List.of(dockable("a"), b, dockable("c"));
                DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                    throw new AssertionError("An in-pane split must not create a floating stage");
                });
                DockingPane host = context.createPane();
                for (String id : List.of("a", "b", "c")) {
                    context.openDockable(host, id, DockingPosition.CENTER);
                }
                host.selectDockable("b");
                renderWithEventSkin(host);
                Node sourceTitle = title(host, "b");
                double x = (double) testCase[0] * host.getWidth();
                double y = (double) testCase[1] * host.getHeight();

                fireDragDetected(sourceTitle);
                for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                    assertFalse("A single tab group must use its local edge drop zones", indicator.isVisible());
                }
                fireDragEvent(host, DragEvent.DRAG_OVER, sourceTitle,
                        x, y, 200, 200, TransferMode.MOVE, b.getContent());
                assertTrue(host.lookup(".docking-drop-zone-preview").isVisible());
                for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                    assertFalse(indicator.isVisible());
                }
                DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, sourceTitle,
                        x, y, 200, 200, TransferMode.MOVE, b.getContent());
                fireDragEvent(sourceTitle, DragEvent.DRAG_DONE, sourceTitle,
                        x, y, 200, 200, TransferMode.MOVE, sourceTitle);

                assertTrue(dropped.isDropCompleted());
                DockingLayoutNode.Split split = (DockingLayoutNode.Split) host.getLayoutRoot();
                assertEquals(testCase[2], split.getOrientation());
                assertEquals(testCase[3], allIds(split.getFirst()));
                assertEquals(testCase[4], allIds(split.getSecond()));
                assertSame(b, host.getActiveDockable());
                return null;
            });
        }
    }

    @Test
    public void dockingDragEventsAreHandledBeforeContentCanConsumeThem() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = new DockingContext(factoryFor(List.of(a, b), new AtomicInteger()), pane -> {
                throw new AssertionError("An in-pane drop must not create a floating stage");
            });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            pane.selectDockable("b");
            renderWithEventSkin(pane);

            Node content = b.getContent();
            AtomicInteger contentDragEvents = new AtomicInteger();
            content.addEventHandler(DragEvent.DRAG_OVER, event -> {
                contentDragEvents.incrementAndGet();
                event.consume();
            });
            content.addEventHandler(DragEvent.DRAG_DROPPED, event -> {
                contentDragEvents.incrementAndGet();
                event.consume();
            });

            Node sourceTitle = title(pane, "a");
            fireDragDetected(sourceTitle);
            double x = content.getBoundsInLocal().getWidth() / 2;
            double y = content.getBoundsInLocal().getHeight() / 2;
            fireDragEvent(content, DragEvent.DRAG_OVER, sourceTitle,
                    x, y, 200, 200, TransferMode.MOVE, content);

            assertTrue(pane.lookup(".docking-drop-zone-preview").isVisible());
            assertEquals(0, contentDragEvents.get());

            DragEvent dropped = fireDragEvent(content, DragEvent.DRAG_DROPPED, sourceTitle,
                    x, y, 200, 200, TransferMode.MOVE, content);
            fireDragEvent(sourceTitle, DragEvent.DRAG_DONE, sourceTitle,
                    x, y, 200, 200, TransferMode.MOVE, sourceTitle);

            assertTrue(dropped.isDropCompleted());
            assertEquals(0, contentDragEvents.get());
            return null;
        });
    }

    @Test
    public void outerEdgeIndicatorsAreVisibleForVerticalSplits() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                throw new AssertionError("Starting a drag must not create a floating stage");
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.BOTTOM);
            context.openDockable(host, "c", DockingPosition.CENTER);
            renderWithEventSkin(host);

            fireDragDetected(title(host, "c"));

            for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                assertTrue(indicator.isVisible());
            }
            Event.fireEvent(host, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", KeyCode.ESCAPE,
                    false, false, false, false));
            return null;
        });
    }

    @Test
    public void outerEdgeIndicatorsWrapAllGroupsAndPreviewTheWholePane() {
        Object[][] cases = {
                {".pane-edge-left", Orientation.HORIZONTAL, true, 0.5, 1.0},
                {".pane-edge-right", Orientation.HORIZONTAL, false, 0.5, 1.0},
                {".pane-edge-top", Orientation.VERTICAL, true, 1.0, 0.5},
                {".pane-edge-bottom", Orientation.VERTICAL, false, 1.0, 0.5}
        };
        for (Object[] testCase : cases) {
            invoke(() -> {
                List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
                DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                    throw new AssertionError("An outer-edge drop must stay in the target pane");
                });
                DockingPane host = context.createPane();
                context.openDockable(host, "a", DockingPosition.CENTER);
                context.openDockable(host, "b", DockingPosition.RIGHT);
                context.openDockable(host, "c", DockingPosition.CENTER);
                renderWithEventSkin(host);
                Node source = title(host, "c");

                fireDragDetected(source);
                host.applyCss();
                host.layout();
                Point2D point = paneEdgeTargetCenter(host, (String) testCase[0]);
                fireDragEvent(host, DragEvent.DRAG_OVER, source,
                        point.getX(), point.getY(), 200, 200, TransferMode.MOVE, host);
                Node preview = host.lookup(".docking-drop-zone-preview");
                assertTrue(preview.isVisible());
                assertEquals(host.getWidth() * (double) testCase[3],
                        preview.getBoundsInParent().getWidth(), 1);
                assertEquals(host.getHeight() * (double) testCase[4],
                        preview.getBoundsInParent().getHeight(), 1);

                DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, source,
                        point.getX(), point.getY(), 200, 200, TransferMode.MOVE, host);
                fireDragEvent(source, DragEvent.DRAG_DONE, source,
                        point.getX(), point.getY(), 200, 200, TransferMode.MOVE, source);

                assertTrue(dropped.isDropCompleted());
                DockingLayoutNode.Split root = (DockingLayoutNode.Split) host.getLayoutRoot();
                assertEquals(testCase[1], root.getOrientation());
                DockingLayoutNode moved = (boolean) testCase[2] ? root.getFirst() : root.getSecond();
                DockingLayoutNode.Split remainder = (DockingLayoutNode.Split)
                        ((boolean) testCase[2] ? root.getSecond() : root.getFirst());
                assertEquals(List.of("c"), allIds(moved));
                assertEquals(Orientation.HORIZONTAL, remainder.getOrientation());
                assertEquals(List.of("a"), allIds(remainder.getFirst()));
                assertEquals(List.of("b"), allIds(remainder.getSecond()));
                for (Node indicator : host.lookupAll(".docking-pane-edge-target")) {
                    assertFalse(indicator.isVisible());
                }
                return null;
            });
        }
    }

    @Test
    public void droppingAwayFromOuterIndicatorsStillSplitsOnlyTheHoveredGroup() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                throw new AssertionError("A local split must not create a floating stage");
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.RIGHT);
            context.openDockable(host, "c", DockingPosition.CENTER);
            renderWithEventSkin(host);
            Node source = title(host, "c");

            fireDragDetected(source);
            DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, source,
                    host.getWidth() * 0.8, host.getHeight() - 2, 200, 200, TransferMode.MOVE, host);
            fireDragEvent(source, DragEvent.DRAG_DONE, source, 5, 5, 200, 200, TransferMode.MOVE, source);

            assertTrue(dropped.isDropCompleted());
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) host.getLayoutRoot();
            assertEquals(Orientation.HORIZONTAL, root.getOrientation());
            assertEquals(List.of("a"), allIds(root.getFirst()));
            DockingLayoutNode.Split right = (DockingLayoutNode.Split) root.getSecond();
            assertEquals(Orientation.VERTICAL, right.getOrientation());
            assertEquals(List.of("b"), allIds(right.getFirst()));
            assertEquals(List.of("c"), allIds(right.getSecond()));
            return null;
        });
    }

    @Test
    public void floatingTabCanDropBelowAllMainGroupsAndCloseItsEmptyWindow() {
        invoke(() -> {
            List<Dockable> all = List.of(dockable("a"), dockable("b"), dockable("c"));
            AtomicReference<Stage> floatingStageRef = new AtomicReference<>();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane, 300, 200));
                stage.show();
                floatingStageRef.set(stage);
                return stage;
            });
            DockingPane host = context.createPane();
            context.openDockable(host, "a", DockingPosition.CENTER);
            context.openDockable(host, "b", DockingPosition.RIGHT);
            context.openDockable(host, "c", DockingPosition.CENTER);
            renderWithEventSkin(host);
            DockingPane floating = context.floatDockable("c", 900, 100);
            Stage stage = floatingStageRef.get();
            try {
                floating.setSkin(new EventTestDockingPaneSkin(floating));
                floating.applyCss();
                floating.layout();
                host.applyCss();
                host.layout();
                Node source = title(floating, "c");

                fireDragDetected(source);
                fireDragEvent(host, DragEvent.DRAG_OVER, source,
                        host.getWidth() / 2, host.getHeight() / 2, 200, 200, TransferMode.MOVE, host);
                Point2D point = paneEdgeTargetCenter(host, ".pane-edge-bottom");
                DragEvent dropped = fireDragEvent(host, DragEvent.DRAG_DROPPED, source,
                        point.getX(), point.getY(), 200, 200, TransferMode.MOVE, host);
                fireDragEvent(source, DragEvent.DRAG_DONE, source,
                        point.getX(), point.getY(), 200, 200, TransferMode.MOVE, source);

                assertTrue(dropped.isDropCompleted());
                assertFalse(stage.isShowing());
                assertTrue(floating.getLayoutRoot() instanceof DockingLayoutNode.Empty);
                DockingLayoutNode.Split root = (DockingLayoutNode.Split) host.getLayoutRoot();
                assertEquals(Orientation.VERTICAL, root.getOrientation());
                assertEquals(List.of("a", "b"), allIds(root.getFirst()));
                assertEquals(List.of("c"), allIds(root.getSecond()));
                assertEquals(3, context.getDockables().size());
                return null;
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void acceptedCrossPaneDropDoesNotAlsoCreateFloatingPane() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            List<Dockable> all = List.of(a, b);
            AtomicInteger stageFactoryCalls = new AtomicInteger();
            DockingContext context = new DockingContext(factoryFor(all, new AtomicInteger()), pane -> {
                stageFactoryCalls.incrementAndGet();
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 200, 120));
                return stage;
            });
            DockingPane sourcePane = context.createPane();
            DockingPane targetPane = context.createPane();
            context.openDockable(sourcePane, "a", DockingPosition.CENTER);
            context.openDockable(targetPane, "b", DockingPosition.CENTER);
            sourcePane.setSkin(new EventTestDockingPaneSkin(sourcePane));
            targetPane.setSkin(new EventTestDockingPaneSkin(targetPane));
            layout(new HBox(sourcePane, targetPane));
            Node sourceTitle = title(sourcePane, "a");
            Node targetTitle = title(targetPane, "b");

            fireDragDetected(sourceTitle);
            DragEvent dropped = fireDragEvent(targetPane, DragEvent.DRAG_DROPPED, sourceTitle,
                    targetPane.getWidth() / 2, targetPane.getHeight() / 2,
                    600, 200, TransferMode.MOVE, targetTitle);
            fireDragEvent(sourceTitle, DragEvent.DRAG_DONE, sourceTitle,
                    5, 5, 600, 200, TransferMode.MOVE, sourceTitle);

            assertTrue(dropped.isDropCompleted());
            assertTrue(sourcePane.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(2, allIds(targetPane.getLayoutRoot()).size());
            assertTrue(allIds(targetPane.getLayoutRoot()).containsAll(List.of("a", "b")));
            assertEquals(0, stageFactoryCalls.get());
            return null;
        });
    }
}
