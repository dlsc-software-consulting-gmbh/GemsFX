package com.dlsc.gemsfx;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class DockingContextTest extends FxTestBase {

    private static final DockingStageFactory STAGE_FACTORY = pane -> {
        Stage stage = new Stage();
        stage.setScene(new Scene(pane));
        return stage;
    };

    private static DockableFactory factoryFor(Dockable dockable, AtomicInteger calls) {
        return id -> {
            calls.incrementAndGet();
            return id.equals(dockable.getId()) ? Optional.of(dockable) : Optional.empty();
        };
    }

    @Test
    public void contextStoresFactoriesAndCreatesAttachedPanes() {
        invoke(() -> {
            DockableFactory dockableFactory = id -> Optional.empty();
            AtomicInteger stageCalls = new AtomicInteger();
            DockingStageFactory stageFactory = pane -> {
                stageCalls.incrementAndGet();
                return null;
            };
            DockingContext context = new DockingContext(dockableFactory, stageFactory);
            DockingPane pane = context.createPane();

            assertSame(dockableFactory, context.getDockableFactory());
            assertSame(stageFactory, context.getStageFactory());
            assertSame(context, pane.getDockingContext());
            assertNull(pane.getActiveDockable());
            assertTrue(context.getDockables().isEmpty());
            assertEquals(0, stageCalls.get());
            return null;
        });
    }

    @Test
    public void openingAnExistingIdActivatesRatherThanDuplicates() {
        invoke(() -> {
            AtomicInteger calls = new AtomicInteger();
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            Dockable other = new Dockable("other", "Other", new Label());
            DockingContext context = new DockingContext(id -> {
                calls.incrementAndGet();
                return Optional.of(id.equals("other") ? other : editor);
            }, STAGE_FACTORY);
            DockingPane pane = context.createPane();

            assertEquals(0, calls.get());
            assertSame(editor, context.openDockable(pane, "editor:main", DockingPosition.CENTER).orElseThrow());
            context.openDockable(pane, "other", DockingPosition.CENTER);
            assertSame(other, pane.getActiveDockable());
            assertSame(editor, context.openDockable(pane, "editor:main", DockingPosition.CENTER).orElseThrow());

            assertEquals(2, calls.get());
            assertEquals(2, context.getDockables().size());
            assertSame(editor, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void paneFromAnotherContextIsRejected() {
        invoke(() -> {
            AtomicInteger calls = new AtomicInteger();
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            DockingContext context = new DockingContext(factoryFor(editor, calls), STAGE_FACTORY);
            DockingContext foreign = new DockingContext(id -> Optional.empty(), STAGE_FACTORY);

            assertThrows(IllegalArgumentException.class,
                    () -> context.openDockable(foreign.createPane(), "editor:main", DockingPosition.CENTER));
            assertEquals(0, calls.get());
            assertTrue(context.getDockables().isEmpty());
            return null;
        });
    }

    @Test
    public void mismatchedFactoryIdIsRejectedWithoutStateChange() {
        invoke(() -> {
            Dockable wrong = new Dockable("wrong", "Wrong", new Label());
            DockingContext context = new DockingContext(id -> Optional.of(wrong), STAGE_FACTORY);
            DockingPane pane = context.createPane();

            assertThrows(IllegalStateException.class,
                    () -> context.openDockable(pane, "editor:main", DockingPosition.CENTER));
            assertTrue(context.getDockables().isEmpty());
            assertNull(pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void factoryMissReturnsEmptyWithoutStateChange() {
        invoke(() -> {
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            DockingContext context = new DockingContext(factoryFor(editor, new AtomicInteger()), STAGE_FACTORY);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor:main", DockingPosition.CENTER);

            assertTrue(context.openDockable(pane, "missing", DockingPosition.CENTER).isEmpty());
            assertEquals(1, context.getDockables().size());
            assertSame(editor, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void closePolicyDefaultsToAllowedAndIsStored() {
        invoke(() -> {
            DockingContext context = new DockingContext(id -> Optional.empty(), STAGE_FACTORY);
            Dockable dockable = new Dockable("a", "A", new Label());

            assertTrue(context.canCloseDockable(dockable));
            context.setCanCloseDockable(d -> false);
            assertFalse(context.canCloseDockable(dockable));
            assertThrows(NullPointerException.class, () -> context.setCanCloseDockable(null));
            return null;
        });
    }

    @Test
    public void requestCloseRemovesApprovedDockable() {
        invoke(() -> {
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            DockingContext context = new DockingContext(factoryFor(editor, new AtomicInteger()), STAGE_FACTORY);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor:main", DockingPosition.CENTER);

            assertTrue(context.requestCloseDockable(editor));

            assertTrue(context.getDockables().isEmpty());
            assertNull(pane.getActiveDockable());
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertFalse(context.requestCloseDockable(editor));
            return null;
        });
    }

    @Test
    public void requestCloseVetoLeavesEverythingUnchanged() {
        invoke(() -> {
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            DockingContext context = new DockingContext(factoryFor(editor, new AtomicInteger()), STAGE_FACTORY);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor:main", DockingPosition.CENTER);
            DockingLayoutNode before = pane.getLayoutRoot();
            context.setCanCloseDockable(d -> false);

            assertFalse(context.requestCloseDockable(editor));

            assertEquals(1, context.getDockables().size());
            assertSame(editor, pane.getActiveDockable());
            assertSame(before, pane.getLayoutRoot());
            return null;
        });
    }

    @Test
    public void closedIdCanBeOpenedAgain() {
        invoke(() -> {
            Dockable editor = new Dockable("editor:main", "Editor", new Label());
            DockingContext context = new DockingContext(factoryFor(editor, new AtomicInteger()), STAGE_FACTORY);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor:main", DockingPosition.CENTER);
            context.requestCloseDockable(editor);

            assertSame(editor, context.openDockable(pane, "editor:main", DockingPosition.LEFT).orElseThrow());
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Tabs);
            return null;
        });
    }

    @Test
    public void modelChangesOffTheFxThreadAreRejected() {
        DockingContext context = invoke(() -> new DockingContext(id -> Optional.empty(), STAGE_FACTORY));
        DockingPane pane = invoke(context::createPane);

        assertThrows(IllegalStateException.class,
                () -> context.openDockable(pane, "a", DockingPosition.CENTER));
        assertThrows(IllegalStateException.class, context::createPane);
    }
}
