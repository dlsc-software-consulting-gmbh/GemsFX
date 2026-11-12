package com.dlsc.gemsfx;

import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingContext;
import com.dlsc.gemsfx.docking.DockingLayout;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingPosition;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class DockingPaneFactoryTest extends FxTestBase {

    private static class CustomPane extends DockingPane {
        CustomPane(DockingContext context) {
            super(context);
        }
    }

    private static DockingContext context() {
        return new DockingContext(id -> Optional.of(new Dockable(id, id, new Label())), pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    @Test
    public void customFactoryCreatesPrimaryAndFloatingPanes() {
        runFx(() -> {
            DockingContext context = context();
            context.setPaneFactory(CustomPane::new);
            DockingPane primary = context.createPane();
            assertTrue(primary instanceof CustomPane);
            context.openDockable(primary, "a", DockingPosition.CENTER);
            DockingPane floating = context.floatDockable("a", 10, 10);
            try {
                assertTrue(floating instanceof CustomPane);
                assertSame(context, floating.getDockingContext());
            } finally {
                ((Stage) floating.getScene().getWindow()).close();
            }
        });
    }

    @Test
    public void customFactoryCreatesRestoredPanes() {
        runFx(() -> {
            DockingContext context = context();
            context.setPaneFactory(CustomPane::new);
            DockingLayout layout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main", new DockingLayoutNode.Tabs(List.of("a"), "a"), null),
                    new DockingLayout.PaneLayout("other", new DockingLayoutNode.Tabs(List.of("b"), "b"),
                            new DockingLayout.StageBounds(10, 10, 200, 200))));
            context.restoreLayout(layout);
            DockingPane primary = context.getDockables().get(0).getDockingPane();
            DockingPane other = context.getDockables().get(1).getDockingPane();
            try {
                assertTrue(primary instanceof CustomPane);
                assertTrue(other instanceof CustomPane);
                assertNotSame(primary, other);
            } finally {
                ((Stage) other.getScene().getWindow()).close();
            }
        });
    }

    @Test
    public void restoringPreservesExistingPrimaryPaneAfterFactoryChanges() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane primary = context.createPane();
            context.openDockable(primary, "a", DockingPosition.CENTER);
            DockingLayout layout = context.getLayout();
            context.setPaneFactory(CustomPane::new);
            context.restoreLayout(layout);
            assertSame(primary, context.getDockables().get(0).getDockingPane());
            assertTrue(context.createPane() instanceof CustomPane);
        });
    }

    @Test
    public void rejectsInvalidFactoryResultsWithoutChangingExistingLayout() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane existing = context.createPane();
            context.openDockable(existing, "a", DockingPosition.CENTER);
            DockingLayout layout = new DockingLayout("main", List.of(
                    new DockingLayout.PaneLayout("main", new DockingLayoutNode.Tabs(List.of("a"), "a"), null),
                    new DockingLayout.PaneLayout("other", new DockingLayoutNode.Tabs(List.of("b"), "b"),
                            new DockingLayout.StageBounds(10, 10, 200, 200))));
            context.setPaneFactory(owner -> null);
            assertThrows(NullPointerException.class, context::createPane);
            assertThrows(NullPointerException.class, () -> context.restoreLayout(layout));
            assertSame(existing, context.getDockables().get(0).getDockingPane());
            context.setPaneFactory(owner -> new CustomPane(context()));
            assertThrows(IllegalArgumentException.class, context::createPane);
            context.setPaneFactory(owner -> existing);
            assertThrows(IllegalArgumentException.class, context::createPane);
        });
    }
}
