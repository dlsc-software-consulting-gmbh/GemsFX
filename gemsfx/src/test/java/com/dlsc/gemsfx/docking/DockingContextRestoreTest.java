package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;

import javafx.geometry.Orientation;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class DockingContextRestoreTest extends FxTestBase {

    private static Dockable dockable(String id) {
        return new Dockable(id, "Title " + id, new Label("Content " + id));
    }

    private static DockingContext context(Map<String, Dockable> dockables, AtomicInteger stageFactoryCalls,
                                          AtomicReference<List<Stage>> createdStages) {
        return new DockingContext(id -> Optional.ofNullable(dockables.get(id)), pane -> {
            stageFactoryCalls.incrementAndGet();
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(pane), 300, 200));
            createdStages.get().add(stage);
            return stage;
        });
    }

    private static Map<String, DockingLayout.PaneLayout> panesById(DockingLayout layout) {
        Map<String, DockingLayout.PaneLayout> byId = new LinkedHashMap<>();
        for (DockingLayout.PaneLayout pane : layout.getPanes()) {
            byId.put(pane.getPaneId(), pane);
        }
        return byId;
    }

    private static void assertLayoutNodeEquals(DockingLayoutNode expected, DockingLayoutNode actual) {
        assertEquals(expected.getClass(), actual.getClass());
        if (expected instanceof DockingLayoutNode.Empty) {
            return;
        }
        if (expected instanceof DockingLayoutNode.Tabs) {
            DockingLayoutNode.Tabs e = (DockingLayoutNode.Tabs) expected;
            DockingLayoutNode.Tabs a = (DockingLayoutNode.Tabs) actual;
            assertEquals(e.getDockableIds(), a.getDockableIds());
            assertEquals(e.getSelectedId(), a.getSelectedId());
            return;
        }
        DockingLayoutNode.Split e = (DockingLayoutNode.Split) expected;
        DockingLayoutNode.Split a = (DockingLayoutNode.Split) actual;
        assertEquals(e.getOrientation(), a.getOrientation());
        assertEquals(e.getDividerPositions(), a.getDividerPositions());
        assertEquals(e.getChildren().size(), a.getChildren().size());
        for (int i = 0; i < e.getChildren().size(); i++) {
            assertLayoutNodeEquals(e.getChildren().get(i), a.getChildren().get(i));
        }
    }

    private static void assertStageBoundsEquals(DockingLayout.StageBounds expected, DockingLayout.StageBounds actual) {
        if (expected == null || actual == null) {
            assertSame(expected, actual);
            return;
        }
        assertEquals(expected.getX(), actual.getX(), 1e-9);
        assertEquals(expected.getY(), actual.getY(), 1e-9);
        assertEquals(expected.getWidth(), actual.getWidth(), 1e-9);
        assertEquals(expected.getHeight(), actual.getHeight(), 1e-9);
    }

    private static void assertLayoutEquals(DockingLayout expected, DockingLayout actual) {
        assertEquals(expected.getPrimaryPaneId(), actual.getPrimaryPaneId());
        Map<String, DockingLayout.PaneLayout> expectedPanes = panesById(expected);
        Map<String, DockingLayout.PaneLayout> actualPanes = panesById(actual);
        assertEquals(expectedPanes.keySet(), actualPanes.keySet());
        for (String paneId : expectedPanes.keySet()) {
            DockingLayout.PaneLayout expectedPane = expectedPanes.get(paneId);
            DockingLayout.PaneLayout actualPane = actualPanes.get(paneId);
            assertLayoutNodeEquals(expectedPane.getRoot(), actualPane.getRoot());
            assertStageBoundsEquals(expectedPane.getStageBounds(), actualPane.getStageBounds());
        }
    }

    @Test
    public void restoreRoundTripsCompleteMultiPaneLayout() {
        invoke(() -> {
            Map<String, Dockable> sourceDockables = new HashMap<>();
            sourceDockables.put("a", dockable("a"));
            sourceDockables.put("b", dockable("b"));
            sourceDockables.put("c", dockable("c"));
            sourceDockables.put("d", dockable("d"));
            AtomicReference<List<Stage>> sourceStages = new AtomicReference<>(new ArrayList<>());
            DockingContext source = context(sourceDockables, new AtomicInteger(), sourceStages);
            DockingPane primary = source.createPane();
            source.openDockable(primary, "a", DockingPosition.CENTER);
            source.openDockable(primary, "b", DockingPosition.CENTER);
            source.openDockable(primary, "c", DockingPosition.RIGHT);
            primary.selectDockable("a");
            primary.updateDividerPositions(List.of(), List.of(0.3));

            DockingPane floating = source.floatDockable("b", 111, 222);
            source.openDockable(floating, "d", DockingPosition.BOTTOM);
            floating.updateDividerPositions(List.of(), List.of(0.7));
            Stage floatingStage = sourceStages.get().get(0);
            floatingStage.setWidth(555);
            floatingStage.setHeight(333);

            DockingLayout expected = source.getLayout();

            Map<String, Dockable> targetDockables = new HashMap<>();
            targetDockables.put("a", dockable("a"));
            targetDockables.put("b", dockable("b"));
            targetDockables.put("c", dockable("c"));
            targetDockables.put("d", dockable("d"));
            AtomicReference<List<Stage>> targetStages = new AtomicReference<>(new ArrayList<>());
            DockingContext target = context(targetDockables, new AtomicInteger(), targetStages);

            DockingRestoreResult result = target.restoreLayout(expected);

            assertTrue(result.getUnresolvedDockableIds().isEmpty());
            assertLayoutEquals(expected, target.getLayout());
            return null;
        });
    }

    @Test
    public void repeatedRestoreRendersFreshContentInMountedSplitAndTabbedGroups() {
        invoke(() -> {
            Map<String, Dockable> created = new HashMap<>();
            DockingContext context = new DockingContext(id -> {
                Dockable dockable = dockable(id);
                created.put(id, dockable);
                return Optional.of(dockable);
            }, pane -> {
                throw new AssertionError("Restoring a primary pane must not create a floating stage");
            });
            DockingPane primary = context.createPane();
            context.openDockable(primary, "a", DockingPosition.CENTER);
            context.openDockable(primary, "b", DockingPosition.CENTER);
            primary.selectDockable("a");
            context.openDockable(primary, "c", DockingPosition.RIGHT);
            Stage host = new Stage();
            host.setScene(new Scene(new StackPane(primary), 600, 400));
            host.show();
            try {
                DockingLayout snapshot = context.getLayout();
                for (int i = 0; i < 2; i++) {
                    Dockable previous = created.get("a");

                    context.restoreLayout(snapshot);
                    primary.applyCss();
                    primary.layout();

                    assertLayoutEquals(snapshot, context.getLayout());
                    assertEquals(3, primary.lookupAll(".docking-tab").size());
                    assertNotSame(previous, created.get("a"));
                    assertSame(host.getScene(), created.get("a").getContent().getScene());
                    assertSame(host.getScene(), created.get("c").getContent().getScene());
                    assertNull(created.get("b").getContent().getParent());

                    primary.selectDockable("b");
                    primary.applyCss();
                    primary.layout();
                    assertSame(host.getScene(), created.get("b").getContent().getScene());
                    assertNull(created.get("a").getContent().getParent());
                }
            } finally {
                host.close();
            }
            return null;
        });
    }

    @Test
    public void restoredFloatingWindowRendersItsNewContentWhenFactoryShowsTheStage() {
        invoke(() -> {
            Map<String, Dockable> created = new HashMap<>();
            List<Stage> stages = new ArrayList<>();
            DockingContext context = new DockingContext(id -> {
                Dockable dockable = dockable(id);
                created.put(id, dockable);
                return Optional.of(dockable);
            }, pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                stages.add(stage);
                stage.show();
                return stage;
            });
            DockingPane primary = context.createPane();
            context.openDockable(primary, "a", DockingPosition.CENTER);
            context.openDockable(primary, "b", DockingPosition.CENTER);
            try {
                context.floatDockable("b", 100, 100);
                DockingLayout snapshot = context.getLayout();
                Dockable previous = created.get("b");

                context.restoreLayout(snapshot);

                Stage restored = stages.get(stages.size() - 1);
                restored.getScene().getRoot().applyCss();
                restored.getScene().getRoot().layout();
                assertTrue(restored.isShowing());
                assertNotSame(previous, created.get("b"));
                assertSame("The restored window must render the newly resolved dockable, not the old instance",
                        restored.getScene(), created.get("b").getContent().getScene());
                assertFalse(stages.get(0).isShowing());
            } finally {
                for (Stage stage : stages) {
                    stage.close();
                }
            }
            return null;
        });
    }

    @Test
    public void emptyContextSnapshotCanBeRestored() {
        invoke(() -> {
            DockingContext context = new DockingContext(id -> Optional.empty(), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                return stage;
            });

            DockingLayout snapshot = context.getLayout();

            assertEquals(1, snapshot.getPanes().size());
            assertEquals("primary", snapshot.getPrimaryPaneId());
            assertTrue(snapshot.getPanes().get(0).getRoot() instanceof DockingLayoutNode.Empty);
            context.restoreLayout(snapshot);
            assertEquals(1, context.getLayout().getPanes().size());
            assertTrue(context.getLayout().getPanes().get(0).getRoot() instanceof DockingLayoutNode.Empty);
            return null;
        });
    }

    @Test
    public void restoreReportsUnresolvedDockableIdsAndCollapsesEmptyStructures() {
        invoke(() -> {
            DockingContext context = new DockingContext(
                    id -> id.equals("editor:main")
                            ? Optional.of(new Dockable(id, "Editor", new Label()))
                            : Optional.empty(),
                    pane -> {
                        Stage stage = new Stage();
                        stage.setScene(new Scene(new StackPane(pane)));
                        return stage;
                    });
            DockingLayout layout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Split(
                                    Orientation.HORIZONTAL,
                                    List.of(new DockingLayoutNode.Tabs(
                                            List.of("editor:main", "tool:removed"), "editor:main"),
                                    new DockingLayoutNode.Tabs(
                                            List.of("tool:also-removed"), "tool:also-removed")), List.of(0.5)),
                            null)));

            DockingRestoreResult result = context.restoreLayout(layout);

            assertEquals(Set.of("tool:removed", "tool:also-removed"), result.getUnresolvedDockableIds());
            DockingLayout restored = context.getLayout();
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) restored.getPanes().get(0).getRoot();
            assertEquals(List.of("editor:main"), tabs.getDockableIds());
            return null;
        });
    }

    @Test
    public void unresolvedMiddleGroupsGiveTheirSpaceToThePreviousSibling() {
        runFx(() -> {
            DockingContext context = new DockingContext(id -> id.equals("missing")
                    ? Optional.empty() : Optional.of(dockable(id)), pane -> new Stage());
            DockingPane pane = context.createPane();
            DockingLayoutNode.Split split = new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(
                    new DockingLayoutNode.Tabs(List.of("a"), "a"),
                    new DockingLayoutNode.Tabs(List.of("missing"), "missing"),
                    new DockingLayoutNode.Tabs(List.of("c"), "c"),
                    new DockingLayoutNode.Tabs(List.of("d"), "d")), List.of(0.2, 0.5, 0.8));
            DockingRestoreResult result = context.restoreLayout(new DockingLayout("main",
                    List.of(new DockingLayout.PaneLayout("main", split, null))));
            assertEquals(Set.of("missing"), result.getUnresolvedDockableIds());
            DockingLayoutNode.Split restored = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(3, restored.getChildren().size());
            assertEquals(List.of(0.5, 0.8), restored.getDividerPositions());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) restored.getChildren().get(0)).getDockableIds());
        });
    }

    @Test
    public void restoreRejectsDuplicateDockableIdsWithoutChangingCurrentLayout() {
        invoke(() -> {
            Dockable existing = dockable("existing");
            DockingContext context = new DockingContext(id -> {
                if (id.equals("existing")) {
                    return Optional.of(existing);
                }
                return Optional.of(dockable(id));
            }, pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane)));
                return stage;
            });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "existing", DockingPosition.CENTER);
            DockingLayout before = context.getLayout();
            DockingLayout invalid = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Tabs(List.of("dup"), "dup"),
                            null),
                    new DockingLayout.PaneLayout("floating",
                            new DockingLayoutNode.Tabs(List.of("dup"), "dup"),
                            new DockingLayout.StageBounds(10, 20, 300, 200))));

            assertThrows(IllegalArgumentException.class, () -> context.restoreLayout(invalid));
            assertLayoutEquals(before, context.getLayout());
            return null;
        });
    }

    @Test
    public void restoreRejectsDividerOutsideClosedIntervalWithoutChangingCurrentLayout() {
        invoke(() -> {
            Dockable existing = dockable("existing");
            DockingContext context = new DockingContext(id -> {
                if (id.equals("existing")) {
                    return Optional.of(existing);
                }
                return Optional.of(dockable(id));
            }, pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane)));
                return stage;
            });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "existing", DockingPosition.CENTER);
            DockingLayout before = context.getLayout();
            assertThrows(IllegalArgumentException.class, () -> new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(
                                    new DockingLayoutNode.Tabs(List.of("left"), "left"),
                                    new DockingLayoutNode.Tabs(List.of("right"), "right")), List.of(1.1)),
                            null))));

            assertLayoutEquals(before, context.getLayout());
            return null;
        });
    }

    @Test
    public void restorePropagatesDockableFactoryExceptionWithoutChangingCurrentLayout() {
        invoke(() -> {
            Dockable existing = dockable("existing");
            DockingContext context = new DockingContext(id -> {
                if (id.equals("existing")) {
                    return Optional.of(existing);
                }
                if (id.equals("boom")) {
                    throw new IllegalStateException("factory-failure");
                }
                return Optional.of(dockable(id));
            }, pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane)));
                return stage;
            });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "existing", DockingPosition.CENTER);
            DockingLayout before = context.getLayout();
            DockingLayout invalid = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Tabs(List.of("boom"), "boom"),
                            null)));

            IllegalStateException ex = assertThrows(IllegalStateException.class, () -> context.restoreLayout(invalid));
            assertEquals("factory-failure", ex.getMessage());
            assertLayoutEquals(before, context.getLayout());
            return null;
        });
    }

    @Test
    public void restorePropagatesStageFactoryExceptionAndClosesCreatedReplacementStages() {
        invoke(() -> {
            Dockable existing = dockable("existing");
            DockingContext context = new DockingContext(id -> Optional.of(id.equals("existing") ? existing : dockable(id)),
                    pane -> {
                        Stage stage = new Stage();
                        stage.setScene(new Scene(new StackPane(pane)));
                        return stage;
                    });
            DockingPane pane = context.createPane();
            context.openDockable(pane, "existing", DockingPosition.CENTER);
            DockingLayout before = context.getLayout();

            AtomicInteger created = new AtomicInteger();
            AtomicInteger hidden = new AtomicInteger();
            DockingContext restoring = new DockingContext(id -> Optional.of(dockable(id)), restorePane -> {
                int call = created.incrementAndGet();
                if (call == 2) {
                    throw new IllegalStateException("stage-failure");
                }
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(restorePane), 320, 240));
                stage.setOnHidden(evt -> hidden.incrementAndGet());
                stage.show();
                return stage;
            });
            DockingPane restoringPane = restoring.createPane();
            restoring.openDockable(restoringPane, "existing", DockingPosition.CENTER);
            DockingLayout restoreLayout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main", new DockingLayoutNode.Tabs(List.of("a"), "a"), null),
                    new DockingLayout.PaneLayout("floating-1", new DockingLayoutNode.Tabs(List.of("b"), "b"),
                            new DockingLayout.StageBounds(10, 20, 200, 100)),
                    new DockingLayout.PaneLayout("floating-2", new DockingLayoutNode.Tabs(List.of("c"), "c"),
                            new DockingLayout.StageBounds(30, 40, 220, 140))));
            DockingLayout restoringBefore = restoring.getLayout();

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> restoring.restoreLayout(restoreLayout));

            assertEquals("stage-failure", ex.getMessage());
            assertEquals(2, created.get());
            assertEquals(1, hidden.get());
            assertLayoutEquals(restoringBefore, restoring.getLayout());
            assertLayoutEquals(before, context.getLayout());
            return null;
        });
    }

    @Test
    public void restoreUpdatesMountedPrimaryPaneInstanceInVisibleScene() {
        invoke(() -> {
            Map<String, Dockable> dockables = new HashMap<>();
            dockables.put("before", dockable("before"));
            dockables.put("after", dockable("after"));
            DockingContext context = new DockingContext(id -> Optional.ofNullable(dockables.get(id)), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                return stage;
            });

            DockingPane primary = context.createPane();
            context.openDockable(primary, "before", DockingPosition.CENTER);
            Stage host = new Stage();
            StackPane root = new StackPane(primary);
            host.setScene(new Scene(root, 500, 400));
            host.show();

            DockingLayout restoredLayout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Tabs(List.of("after"), "after"),
                            null)));

            context.restoreLayout(restoredLayout);

            assertSame(primary, root.getChildren().get(0));
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) primary.getLayoutRoot();
            assertEquals(List.of("after"), tabs.getDockableIds());
            assertEquals("after", tabs.getSelectedId());
            host.close();
            return null;
        });
    }

    @Test
    public void restoreShowsFloatingStagesAfterSuccessfulCommit() {
        invoke(() -> {
            Map<String, Dockable> dockables = new HashMap<>();
            dockables.put("main", dockable("main"));
            dockables.put("float", dockable("float"));
            AtomicReference<List<Stage>> created = new AtomicReference<>(new ArrayList<>());
            DockingContext context = new DockingContext(id -> Optional.ofNullable(dockables.get(id)), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                created.get().add(stage);
                return stage;
            });

            DockingLayout layout = new DockingLayout("main-pane", List.of(
                    new DockingLayout.PaneLayout("main-pane",
                            new DockingLayoutNode.Tabs(List.of("main"), "main"),
                            null),
                    new DockingLayout.PaneLayout("float-pane",
                            new DockingLayoutNode.Tabs(List.of("float"), "float"),
                            new DockingLayout.StageBounds(10, 20, 250, 150))));

            context.restoreLayout(layout);

            assertEquals(1, created.get().size());
            assertTrue(created.get().get(0).isShowing());
            created.get().get(0).close();
            return null;
        });
    }

    @Test
    public void restoredUnownedFloatingStageClosesWithPrimaryHost() {
        invoke(() -> {
            Map<String, Dockable> dockables = new HashMap<>();
            dockables.put("main", dockable("main"));
            dockables.put("float", dockable("float"));
            AtomicReference<Stage> floatingStage = new AtomicReference<>();
            DockingContext context = new DockingContext(id -> Optional.ofNullable(dockables.get(id)), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                floatingStage.set(stage);
                return stage;
            });
            DockingPane primary = context.createPane();
            Stage host = new Stage();
            host.setScene(new Scene(new StackPane(primary), 500, 400));
            host.show();

            DockingLayout layout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main",
                            new DockingLayoutNode.Tabs(List.of("main"), "main"),
                            null),
                    new DockingLayout.PaneLayout("float",
                            new DockingLayoutNode.Tabs(List.of("float"), "float"),
                            new DockingLayout.StageBounds(10, 20, 250, 150))));

            context.restoreLayout(layout);
            assertTrue(floatingStage.get().isShowing());

            host.close();

            assertFalse(floatingStage.get().isShowing());
            return null;
        });
    }

    @Test
    public void restoreClosesCreatedStageWhenHostValidationFails() {
        invoke(() -> {
            AtomicReference<Stage> created = new AtomicReference<>();
            DockingContext context = new DockingContext(id -> Optional.of(dockable(id)), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(new Label("wrong root")), 300, 200));
                stage.show();
                created.set(stage);
                return stage;
            });
            DockingLayout layout = new DockingLayout("main-pane", List.of(
                    new DockingLayout.PaneLayout("main-pane",
                            new DockingLayoutNode.Tabs(List.of("main"), "main"),
                            null),
                    new DockingLayout.PaneLayout("float-pane",
                            new DockingLayoutNode.Tabs(List.of("float"), "float"),
                            new DockingLayout.StageBounds(10, 20, 250, 150))));

            assertThrows(IllegalStateException.class, () -> context.restoreLayout(layout));

            assertNotNull(created.get());
            assertFalse(created.get().isShowing());
            return null;
        });
    }

    @Test
    public void getLayoutRejectsMultipleNonFloatingPanesExplicitly() {
        invoke(() -> {
            Map<String, Dockable> dockables = new HashMap<>();
            dockables.put("a", dockable("a"));
            dockables.put("b", dockable("b"));
            DockingContext context = new DockingContext(id -> Optional.ofNullable(dockables.get(id)), pane -> {
                Stage stage = new Stage();
                stage.setScene(new Scene(new StackPane(pane), 300, 200));
                return stage;
            });
            DockingPane first = context.createPane();
            DockingPane second = context.createPane();
            context.openDockable(first, "a", DockingPosition.CENTER);
            context.openDockable(second, "b", DockingPosition.CENTER);

            assertThrows(IllegalStateException.class, context::getLayout);
            return null;
        });
    }
}
