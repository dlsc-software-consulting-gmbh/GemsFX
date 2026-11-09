package com.dlsc.gemsfx;

import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
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
    public void movingTabGroupMergesInOrderAndPreservesItsSelection() {
        invoke(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(new Dockable(id, id, new Label(id))), STAGE_FACTORY);
            DockingPane source = context.createPane();
            for (String id : List.of("a", "b", "c")) {
                context.openDockable(source, id, DockingPosition.CENTER);
            }
            context.openDockable(source, "d", DockingPosition.RIGHT);
            source.selectDockable("b");
            DockingPane target = context.createPane();
            context.openDockable(target, "x", DockingPosition.CENTER);
            context.openDockable(target, "y", DockingPosition.CENTER);

            assertTrue(context.moveTabGroup(target, "c", "x", DockingPosition.CENTER));

            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) target.getLayoutRoot();
            assertEquals(List.of("x", "y", "a", "b", "c"), tabs.getDockableIds());
            assertEquals("b", tabs.getSelectedId());
            assertEquals("b", target.getActiveDockable().getId());
            assertEquals(List.of("d"), ((DockingLayoutNode.Tabs) source.getLayoutRoot()).getDockableIds());
            assertEquals(6, context.getDockables().size());
            context.openDockable(source, "a", DockingPosition.CENTER);
            assertEquals("a", target.getActiveDockable().getId());
            assertEquals(1, source.getDockables().size());
            return null;
        });
    }

    @Test
    public void tabGroupsStayTogetherAtLeafAndWholePaneEdges() {
        invoke(() -> {
            for (DockingPosition edge : List.of(DockingPosition.LEFT, DockingPosition.RIGHT,
                    DockingPosition.TOP, DockingPosition.BOTTOM)) {
                for (boolean paneEdge : List.of(false, true)) {
                    DockingContext context = new DockingContext(
                            id -> Optional.of(new Dockable(id, id, new Label(id))), STAGE_FACTORY);
                    DockingPane pane = context.createPane();
                    context.openDockable(pane, "a", DockingPosition.CENTER);
                    context.openDockable(pane, "b", DockingPosition.CENTER);
                    context.openDockable(pane, "x", DockingPosition.RIGHT);
                    context.openDockable(pane, "y", DockingPosition.BOTTOM);
                    pane.selectDockable("b");

                    assertTrue(context.moveTabGroup(pane, "a", paneEdge ? null : "x", edge));

                    DockingLayoutNode.Split split = (DockingLayoutNode.Split) pane.getLayoutRoot();
                    if (!paneEdge) {
                        assertEquals(List.of("y"), ((DockingLayoutNode.Tabs) split.getSecond()).getDockableIds());
                        split = (DockingLayoutNode.Split) split.getFirst();
                    }
                    DockingLayoutNode moved = edge == DockingPosition.LEFT || edge == DockingPosition.TOP
                            ? split.getFirst() : split.getSecond();
                    assertTrue("The moved group must not become separate splits", moved instanceof DockingLayoutNode.Tabs);
                    assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) moved).getDockableIds());
                    assertEquals("b", ((DockingLayoutNode.Tabs) moved).getSelectedId());
                    assertEquals("b", pane.getActiveDockable().getId());
                    assertEquals(4, pane.getDockables().size());
                }
            }
            return null;
        });
    }

    @Test
    public void tabGroupRejectsOwnGroupAndForeignTargetsButCanMoveIntoAnEmptyPane() {
        invoke(() -> {
            DockingContext context = new DockingContext(
                    id -> Optional.of(new Dockable(id, id, new Label(id))), STAGE_FACTORY);
            DockingPane source = context.createPane();
            context.openDockable(source, "a", DockingPosition.CENTER);
            context.openDockable(source, "b", DockingPosition.CENTER);
            source.selectDockable("a");
            DockingLayoutNode before = source.getLayoutRoot();
            DockingPane foreign = new DockingContext(id -> Optional.empty(), STAGE_FACTORY).createPane();

            assertFalse(context.canMoveTabGroup(source, "a", "b", DockingPosition.CENTER));
            assertFalse(context.moveTabGroup(source, "a", "b", DockingPosition.LEFT));
            assertFalse(context.moveTabGroup(source, "a", null, DockingPosition.TOP));
            assertFalse(context.moveTabGroup(foreign, "a", null, DockingPosition.CENTER));
            assertFalse(context.moveTabGroup(source, "missing", "b", DockingPosition.CENTER));
            assertSame(before, source.getLayoutRoot());

            DockingPane target = context.createPane();
            assertTrue(context.moveTabGroup(target, "b", null, DockingPosition.CENTER));
            assertTrue(source.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) target.getLayoutRoot()).getDockableIds());
            assertEquals("a", target.getActiveDockable().getId());
            return null;
        });
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
