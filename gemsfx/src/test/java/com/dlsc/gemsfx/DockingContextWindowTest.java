package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.DockingPaneSkin;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.Assume;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
        Event.fireEvent(title, new MouseEvent(MouseEvent.DRAG_DETECTED,
                5, 5, 105, 105, MouseButton.PRIMARY, 1,
                false, false, false, false, true, false, false,
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
            context.openDockable(host, "b", DockingPosition.CENTER);
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
            assertEquals(List.of("a", "b"), ids(host));
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
                fireDragEvent(host, DragEvent.DRAG_OVER, sourceTitle,
                        x, y, 200, 200, TransferMode.MOVE, b.getContent());
                assertTrue(host.lookup(".docking-drop-zone-preview").isVisible());
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
