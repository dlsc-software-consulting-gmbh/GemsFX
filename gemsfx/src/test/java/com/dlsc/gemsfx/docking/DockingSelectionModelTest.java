package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class DockingSelectionModelTest extends FxTestBase {

    private static DockingContext newContext() {
        return new DockingContext(id -> Optional.of(new Dockable(id, id, new Label(id))), pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    @Test
    public void tracksSelectedTabsAcrossGroupsAndPanes() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane first = context.createPane();
            DockingPane second = context.createPane();
            DockingSelectionModel model = context.getSelectionModel();
            List<List<Dockable>> changes = new ArrayList<>();
            model.getSelectedItems().addListener((ListChangeListener<Dockable>) c ->
                    changes.add(List.copyOf(model.getSelectedItems())));

            Dockable a = context.openDockable(first, "a", DockingPosition.CENTER).get();
            Dockable b = context.openDockable(first, "b", DockingPosition.CENTER).get();
            Dockable c = context.openDockable(first, "c", DockingPosition.RIGHT).get();
            Dockable d = context.openDockable(second, "d", DockingPosition.CENTER).get();
            assertEquals(Set.of(b, c, d), Set.copyOf(model.getSelectedItems()));
            assertFalse(model.isSelected(a));
            assertTrue(model.isSelected(c));

            model.select(a);
            assertEquals(Set.of(a, c, d), Set.copyOf(model.getSelectedItems()));
            assertSame(a, first.getActiveDockable());
            assertTrue(changes.stream().anyMatch(items -> items.contains(a)));
            assertThrows(UnsupportedOperationException.class, () -> model.getSelectedItems().clear());
            assertThrows(IllegalArgumentException.class,
                    () -> model.select(new Dockable("a", "other", new Label())));
        });
    }

    @Test
    public void tracksMovesFloatingAndClose() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane first = context.createPane();
            DockingPane second = context.createPane();
            Dockable a = context.openDockable(first, "a", DockingPosition.CENTER).get();
            Dockable b = context.openDockable(first, "b", DockingPosition.CENTER).get();
            Dockable x = context.openDockable(second, "x", DockingPosition.CENTER).get();
            assertTrue(second.moveDockable("a", "x", DockingPosition.CENTER));
            assertEquals(Set.of(b, a), Set.copyOf(context.getSelectionModel().getSelectedItems()));
            context.floatDockable("a", 30, 40);
            assertEquals(Set.of(a, b, x), Set.copyOf(context.getSelectionModel().getSelectedItems()));
            assertTrue(context.requestCloseDockable(a));
            assertEquals(Set.of(b, x), Set.copyOf(context.getSelectionModel().getSelectedItems()));
        });
    }

    @Test
    public void restoreUpdatesSelectedItemsEvenThoughLifecycleEventsAreSuppressed() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            DockingLayout layout = context.getLayout();
            context.openDockable(pane, "c", DockingPosition.CENTER);
            List<List<Dockable>> changes = new ArrayList<>();
            context.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Dockable>) c ->
                    changes.add(List.copyOf(context.getSelectionModel().getSelectedItems())));
            context.restoreLayout(layout);
            assertEquals(Set.of("a", "b"), context.getSelectionModel().getSelectedItems().stream()
                    .map(Dockable::getId).collect(java.util.stream.Collectors.toSet()));
            assertFalse(changes.isEmpty());
        });
    }

    @Test
    public void nestedSelectionIsVisibleToLifecycleHandlers() {
        runFx(() -> {
            DockingContext context = newContext();
            DockingPane pane = context.createPane();
            Dockable a = context.openDockable(pane, "a", DockingPosition.CENTER).get();
            AtomicBoolean changed = new AtomicBoolean();
            AtomicReference<List<Dockable>> observed = new AtomicReference<>();
            context.getSelectionModel().getSelectedItems().addListener((ListChangeListener<Dockable>) change -> {
                if (context.getSelectionModel().getSelectedItems().stream().anyMatch(d -> d.getId().equals("b"))
                        && changed.compareAndSet(false, true)) {
                    context.getSelectionModel().select(a);
                }
            });
            context.addEventHandler(DockingEvent.OPENED, event -> observed.set(
                    List.copyOf(context.getSelectionModel().getSelectedItems())));
            context.openDockable(pane, "b", DockingPosition.CENTER);
            assertEquals(List.of(a), observed.get());
            assertEquals(List.of(a), context.getSelectionModel().getSelectedItems());
            assertSame(a, pane.getActiveDockable());
        });
    }
}
