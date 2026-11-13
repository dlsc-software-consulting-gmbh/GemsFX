package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DockingEventTest extends FxTestBase {

    private static DockingContext newContext() {
        return new DockingContext(id -> Optional.of(new Dockable(id, id, new Label(id))), pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    @Test
    public void openingNotifiesTypedAndAnyHandlersAndSupportsRemoval() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            List<DockingEvent> typed = new ArrayList<>();
            List<DockingEvent> all = new ArrayList<>();
            EventHandler<DockingEvent> handler = typed::add;
            context.addEventHandler(DockingEvent.OPENED, handler);
            context.addEventHandler(DockingEvent.ANY, all::add);

            Dockable first = context.openDockable(pane, "a", DockingPosition.CENTER).get();
            assertEquals(1, typed.size());
            assertEquals(1, all.size());
            assertSame(first, typed.get(0).getDockable());
            assertSame(pane, typed.get(0).getNewPane());
            assertNull(typed.get(0).getOldPane());

            context.removeEventHandler(DockingEvent.OPENED, handler);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            assertEquals(1, typed.size());
            assertEquals(2, all.size());
        });
    }

    @Test
    public void movesAndCloseReportPanesAndVetoDoesNotNotify() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane source = context.createPane();
            DockingPane target = context.createPane();
            Dockable a = context.openDockable(source, "a", DockingPosition.CENTER).get();
            context.openDockable(source, "b", DockingPosition.CENTER);
            context.openDockable(target, "x", DockingPosition.CENTER);
            List<DockingEvent> events = new ArrayList<>();
            context.addEventHandler(DockingEvent.ANY, events::add);

            assertTrue(source.moveDockable("a", "b", DockingPosition.CENTER));
            assertTrue(target.moveDockable("a", "x", DockingPosition.CENTER));
            assertEquals(2, events.size());
            assertSame(source, events.get(0).getOldPane());
            assertSame(source, events.get(0).getNewPane());
            assertSame(source, events.get(1).getOldPane());
            assertSame(target, events.get(1).getNewPane());

            a.setOnCloseRequest(() -> false);
            assertFalse(context.requestCloseDockable(a));
            assertEquals(2, events.size());
            a.setOnCloseRequest(() -> true);
            assertTrue(context.requestCloseDockable(a));
            assertEquals(3, events.size());
            assertSame(DockingEvent.CLOSED, events.get(2).getEventType());
            assertSame(target, events.get(2).getOldPane());
            assertNull(events.get(2).getNewPane());
        });
    }

    @Test
    public void groupMoveStopsIfHandlerClosesAnUnmovedMember() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane source = context.createPane();
            DockingPane target = context.createPane();
            context.openDockable(source, "a", DockingPosition.CENTER);
            Dockable b = context.openDockable(source, "b", DockingPosition.CENTER).get();
            context.openDockable(target, "x", DockingPosition.CENTER);
            List<DockingEvent> moved = new ArrayList<>();
            context.addEventHandler(DockingEvent.MOVED, event -> {
                moved.add(event);
                if (event.getDockable().getId().equals("a")) {
                    context.requestCloseDockable(b);
                }
            });

            assertFalse(context.moveTabGroup(target, "a", "x", DockingPosition.CENTER));
            assertEquals(1, moved.size());
            assertEquals("a", moved.get(0).getDockable().getId());
            assertEquals(2, context.getDockables().size());
        });
    }

    @Test
    public void restoringLayoutDoesNotEmitDockingEvents() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            DockingLayout layout = context.getLayout();
            context.openDockable(pane, "b", DockingPosition.CENTER);
            List<Object> events = new ArrayList<>();
            context.addEventHandler(DockingEvent.ANY, events::add);
            context.restoreLayout(layout);
            assertTrue(events.isEmpty());
        });
    }

    @Test
    public void handlerFailureDoesNotPreventCloseNotificationOrDisposal() {
        runFx(() -> {
            AtomicBoolean disposed = new AtomicBoolean();
            AtomicBoolean observedBeforeDisposal = new AtomicBoolean();
            AtomicInteger laterHandlers = new AtomicInteger();
            DockingContext context = new DockingContext(id -> Optional.of(new Dockable(id, id, new Label()) {
                @Override
                protected void dispose() {
                    disposed.set(true);
                }
            }), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane));
                return stage;
            });
            DockingPane pane = context.createPane();
            Dockable dockable = context.openDockable(pane, "a", DockingPosition.CENTER).get();
            context.addEventHandler(DockingEvent.CLOSED, event -> {
                observedBeforeDisposal.set(!disposed.get() && !context.getDockables().contains(dockable));
                throw new IllegalStateException("test handler failure");
            });
            context.addEventHandler(DockingEvent.CLOSED, event -> laterHandlers.incrementAndGet());
            assertTrue(context.requestCloseDockable(dockable));
            assertTrue(observedBeforeDisposal.get());
            assertTrue(disposed.get());
            assertEquals(1, laterHandlers.get());
        });
    }

    @Test
    public void movingToCurrentPositionDoesNotEmitMoveEvent() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            List<DockingEvent> events = new ArrayList<>();
            context.addEventHandler(DockingEvent.MOVED, events::add);
            assertTrue(pane.moveDockable("b", "a", DockingPosition.CENTER));
            assertTrue(events.isEmpty());
        });
    }

    @Test
    public void groupMoveStopsIfHandlerMovesRemainingMemberWithinSourcePane() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "x", DockingPosition.RIGHT);
            context.openDockable(pane, "y", DockingPosition.RIGHT);
            context.addEventHandler(DockingEvent.MOVED, event -> {
                if (event.getDockable().getId().equals("a")) {
                    pane.moveDockable("b", "y", DockingPosition.CENTER);
                }
            });
            assertFalse(context.moveTabGroup(pane, "a", "x", DockingPosition.CENTER));
        });
    }

    @Test
    public void closeHandlerCannotReopenTheInstanceBeingDisposed() {
        runFx(() -> {
            AtomicBoolean disposed = new AtomicBoolean();
            AtomicBoolean rejected = new AtomicBoolean();
            Dockable dockable = new Dockable("a", "a", new Label()) {
                @Override
                protected void dispose() {
                    disposed.set(true);
                }
            };
            DockingContext context = new DockingContext(id -> Optional.of(dockable), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(pane));
                return stage;
            });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.addEventHandler(DockingEvent.CLOSED, event -> {
                try {
                    context.openDockable(pane, "a", DockingPosition.CENTER);
                } catch (IllegalStateException ex) {
                    rejected.set(true);
                }
            });
            assertTrue(context.requestCloseDockable(dockable));
            assertTrue(rejected.get());
            assertTrue(disposed.get());
            assertTrue(context.getDockables().isEmpty());
        });
    }

    @Test
    public void floatingNotifiesMoveWithSourceAndDestination() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            Dockable dockable = context.openDockable(pane, "a", DockingPosition.CENTER).get();
            List<DockingEvent> events = new ArrayList<>();
            context.addEventHandler(DockingEvent.MOVED, events::add);
            DockingPane floating = context.floatDockable("a", 30, 40);
            assertEquals(1, events.size());
            assertSame(dockable, events.get(0).getDockable());
            assertSame(pane, events.get(0).getOldPane());
            assertSame(floating, events.get(0).getNewPane());
            assertTrue(context.requestCloseDockable(dockable));
        });
    }

    @Test
    public void groupMoveStopsIfHandlerMovesTheLastTabElsewhere() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "x", DockingPosition.RIGHT);
            context.openDockable(pane, "y", DockingPosition.RIGHT);
            AtomicBoolean moved = new AtomicBoolean();
            context.addEventHandler(DockingEvent.MOVED, event -> {
                if (event.getDockable().getId().equals("b") && moved.compareAndSet(false, true)) {
                    pane.moveDockable("b", "y", DockingPosition.CENTER);
                }
            });
            assertFalse(context.moveTabGroup(pane, "a", "x", DockingPosition.CENTER));
        });
    }
}
