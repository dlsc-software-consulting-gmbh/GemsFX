package com.dlsc.gemsfx;

import javafx.event.Event;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.input.DragEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DockingContextDropTest extends FxTestBase {

    private static DockingContext context() {
        return new DockingContext(id -> Optional.of(dockable(id)), pane -> {
            throw new AssertionError("A drop must not create a floating window");
        });
    }

    private static Dockable dockable(String id) {
        return new Dockable(id, id, new Group());
    }

    private static DragEvent dropEvent() {
        return new DragEvent(DragEvent.DRAG_DROPPED, null, 100, 100, 100, 100,
                TransferMode.COPY, new Group(), null, new PickResult(null, 100, 100));
    }

    @Test
    public void acceptedDropCreatesAndRegistersOneSelectedDockableInAnEmptyPane() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            DragEvent event = dropEvent();
            Dockable created = dockable("new");
            AtomicInteger calls = new AtomicInteger();
            context.setDockableDropPredicate(candidate -> candidate == event);
            context.setDockableDropFactory(candidate -> {
                assertSame(event, candidate);
                calls.incrementAndGet();
                return created;
            });

            assertTrue(context.canAcceptDockableDrop(event));
            assertTrue(context.canAcceptDockableDrop(event));
            assertEquals(0, calls.get());
            assertEquals(Optional.of(created), context.dropDockable(pane, event, null, DockingPosition.CENTER));
            assertEquals(1, calls.get());
            assertEquals(List.of(created), context.getDockables());
            assertEquals(List.of(created), pane.getDockables());
            assertSame(created, pane.getActiveDockable());
        });
    }

    @Test
    public void droppedDockablesCanJoinOrSplitTheHoveredGroupAndSpanThePane() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            AtomicInteger nextId = new AtomicInteger();
            context.setDockableDropPredicate(event -> true);
            context.setDockableDropFactory(event -> dockable("new-" + nextId.incrementAndGet()));

            context.dropDockable(pane, dropEvent(), "a", DockingPosition.CENTER);
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(List.of("a", "new-1"), ((DockingLayoutNode.Tabs) root.getFirst()).getDockableIds());
            context.dropDockable(pane, dropEvent(), "b", DockingPosition.BOTTOM);
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertTrue(root.getSecond() instanceof DockingLayoutNode.Split);
            context.dropDockable(pane, dropEvent(), null, DockingPosition.TOP);
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(List.of("new-3"), ((DockingLayoutNode.Tabs) root.getFirst()).getDockableIds());
            DockingLayoutNode.Split previousLayout = (DockingLayoutNode.Split) root.getSecond();
            assertEquals(List.of("a", "new-1"), ((DockingLayoutNode.Tabs) previousLayout.getFirst()).getDockableIds());
            DockingLayoutNode.Split previousRightGroup = (DockingLayoutNode.Split) previousLayout.getSecond();
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) previousRightGroup.getFirst()).getDockableIds());
            assertEquals(List.of("new-2"), ((DockingLayoutNode.Tabs) previousRightGroup.getSecond()).getDockableIds());
            assertEquals("new-3", pane.getActiveDockable().getId());
        });
    }

    @Test
    public void rejectedOrInvalidDropsNeverCallTheFactory() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            AtomicInteger calls = new AtomicInteger();
            context.setDockableDropFactory(event -> {
                calls.incrementAndGet();
                return dockable("new");
            });
            assertFalse(context.canAcceptDockableDrop(dropEvent()));
            assertTrue(context.dropDockable(pane, dropEvent(), null, DockingPosition.CENTER).isEmpty());
            context.setDockableDropPredicate(event -> true);
            assertTrue(context.dropDockable(pane, dropEvent(), "missing", DockingPosition.CENTER).isEmpty());
            assertTrue(context.dropDockable(context().createPane(), dropEvent(), null, DockingPosition.CENTER).isEmpty());
            assertEquals(0, calls.get());
            assertTrue(context.getDockables().isEmpty());
        });
    }

    @Test
    public void decliningOrDuplicateFactoryResultsLeaveTheLayoutUnchanged() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            Dockable existing = context.openDockable(pane, "existing", DockingPosition.CENTER).orElseThrow();
            DockingLayoutNode before = pane.getLayoutRoot();
            context.setDockableDropPredicate(event -> true);
            context.setDockableDropFactory(event -> null);
            assertTrue(context.dropDockable(pane, dropEvent(), "existing", DockingPosition.RIGHT).isEmpty());
            context.setDockableDropFactory(event -> dockable("existing"));
            assertThrows(IllegalStateException.class,
                    () -> context.dropDockable(pane, dropEvent(), "existing", DockingPosition.RIGHT));
            assertEquals(before, pane.getLayoutRoot());
            assertEquals(List.of(existing), context.getDockables());
        });
    }

    @Test
    public void acceptedDragPreviewsWithoutCreatingAndDropsBeforeContentCanConsumeIt() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            Dockable existing = context.openDockable(pane, "existing", DockingPosition.CENTER).orElseThrow();
            layout(pane);
            Group sidebar = new Group();
            AtomicInteger creations = new AtomicInteger();
            AtomicInteger contentEvents = new AtomicInteger();
            context.setDockableDropPredicate(event -> event.getGestureSource() == sidebar);
            context.setDockableDropFactory(event -> dockable("new-" + creations.incrementAndGet()));
            existing.getContent().addEventHandler(DragEvent.ANY, event -> {
                contentEvents.incrementAndGet();
                event.consume();
            });
            Node content = existing.getContent();

            fireDrag(pane, content, sidebar, false);
            assertTrue(pane.lookup(".docking-drop-zone-preview").isVisible());
            assertEquals(0, creations.get());
            assertEquals(0, contentEvents.get());
            DragEvent dropped = fireDrag(pane, content, sidebar, true);

            assertTrue(dropped.isDropCompleted());
            assertEquals(1, creations.get());
            assertEquals(0, contentEvents.get());
            assertEquals("new-1", pane.getActiveDockable().getId());
        });
    }

    @Test
    public void unrelatedContentDragsRemainAvailableWhenDropFactoryIsConfigured() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            Dockable existing = context.openDockable(pane, "existing", DockingPosition.CENTER).orElseThrow();
            layout(pane);
            context.setDockableDropPredicate(event -> false);
            context.setDockableDropFactory(event -> {
                throw new AssertionError("Unrelated content drags must not create dockables");
            });
            AtomicInteger contentEvents = new AtomicInteger();
            existing.getContent().addEventHandler(DragEvent.ANY, event -> contentEvents.incrementAndGet());

            fireDrag(pane, existing.getContent(), new Group(), false);
            fireDrag(pane, existing.getContent(), new Group(), true);

            assertEquals(2, contentEvents.get());
            assertEquals(List.of(existing), pane.getDockables());
        });
    }

    @Test
    public void externalDropsWorkInEmptyPanesAndOnOuterEdgeIndicators() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            layout(pane);
            AtomicInteger creations = new AtomicInteger();
            context.setDockableDropPredicate(event -> true);
            context.setDockableDropFactory(event -> dockable("new-" + creations.incrementAndGet()));
            Group sidebar = new Group();
            fireDrag(pane, pane, sidebar, false);
            assertTrue(pane.lookup(".docking-drop-zone-preview").isVisible());
            assertTrue(fireDrag(pane, pane, sidebar, true).isDropCompleted());
            assertEquals("new-1", pane.getActiveDockable().getId());

            context.openDockable(pane, "other", DockingPosition.RIGHT);
            pane.getScene().getRoot().layout();
            fireDrag(pane, pane, sidebar, false);
            Node indicator = pane.lookup(".docking-pane-edge-target.pane-edge-bottom");
            assertTrue(indicator.isVisible());
            javafx.geometry.Bounds bounds = pane.sceneToLocal(indicator.localToScene(indicator.getBoundsInLocal()));
            DragEvent drop = new DragEvent(sidebar, pane, DragEvent.DRAG_DROPPED, null,
                    bounds.getCenterX(), bounds.getCenterY(), 200, 200, TransferMode.COPY, sidebar, pane,
                    new PickResult(pane, bounds.getCenterX(), bounds.getCenterY()));
            Event.fireEvent(pane, drop);

            assertTrue(drop.isDropCompleted());
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(List.of("new-2"), ((DockingLayoutNode.Tabs) root.getSecond()).getDockableIds());
            assertTrue(root.getFirst() instanceof DockingLayoutNode.Split);
            assertEquals(2, creations.get());
            assertFalse(pane.lookup(".docking-drop-zone-preview").isVisible());
            assertFalse(pane.lookup(".docking-pane-edge-target.pane-edge-bottom").isVisible());
        });
    }

    private static DragEvent fireDrag(DockingPane pane, Node target, Node source, boolean dropped) {
        DragEvent event = new DragEvent(source, target, dropped ? DragEvent.DRAG_DROPPED : DragEvent.DRAG_OVER,
                null, pane.getWidth() / 2, pane.getHeight() / 2, 200, 200, TransferMode.COPY,
                source, target, new PickResult(target, pane.getWidth() / 2, pane.getHeight() / 2));
        Event.fireEvent(target, event);
        return event;
    }
}
