package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import javafx.event.Event;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.DragEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class DockingSizingTest extends FxTestBase {

    private static DockingContext context() {
        return new DockingContext(id -> Optional.of(new Dockable(id, id, new Region())), pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    private static Dockable open(DockingContext context, DockingPane pane, String id) {
        return context.openDockable(pane, id, DockingPosition.CENTER).orElseThrow();
    }

    private static DockingLayoutNode.Split split(DockingPane pane) {
        return (DockingLayoutNode.Split) pane.getLayoutRoot();
    }

    @Test
    public void rejectsInvalidSizes() {
        for (double value : new double[]{-0.1, 1.1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> DockingSize.weight(value));
        }
        for (double value : new double[]{-1, Double.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> DockingSize.pixels(value));
        }
    }

    @Test
    public void sideOpeningUsesPaneWeightAndCenterOpeningDoesNotResize() {
        runFx(() -> {
            for (DockingPosition side : List.of(DockingPosition.LEFT, DockingPosition.RIGHT,
                    DockingPosition.TOP, DockingPosition.BOTTOM)) {
                DockingContext context = new DockingContext(id -> {
                    Dockable dockable = new Dockable(id, id, new Region());
                    dockable.setPreferredDockingSize(DockingSize.weight(0.2));
                    return Optional.of(dockable);
                }, pane -> new Stage());
                DockingPane pane = context.createPane();
                open(context, pane, "a");
                context.openDockable(pane, "b", side);
                assertEquals(side == DockingPosition.LEFT || side == DockingPosition.TOP ? 0.2 : 0.8,
                        split(pane).getDividerPositions().get(0), 0.0001);
                List<Double> before = split(pane).getDividerPositions();
                open(context, pane, "c");
                assertEquals(before, split(pane).getDividerPositions());
            }
        });
    }

    @Test
    public void nestedSplitUsesWeightOfWholePaneWithoutResizingOtherGroups() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            open(context, pane, "a");
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            Dockable c = open(context, pane, "c");
            c.setPreferredDockingSize(DockingSize.weight(0.2));
            assertTrue(pane.moveDockable("c", "b", DockingPosition.RIGHT));
            assertEquals(List.of(0.5, 0.8), split(pane).getDividerPositions());
            c.setPreferredDockingSize(DockingSize.weight(0.3));
            assertTrue(pane.moveDockableToPaneEdge("c", DockingPosition.LEFT));
            assertEquals(0.3, split(pane).getDividerPositions().get(0), 0.0001);
            assertEquals(3, split(pane).getChildren().size());
        });
    }

    @Test
    public void pixelSizeIsAppliedWhenAnInitiallyUnmeasuredPaneGetsItsSize() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            open(context, pane, "a");
            Dockable b = open(context, pane, "b");
            b.setPreferredDockingSize(DockingSize.pixels(240));
            assertTrue(pane.moveDockableToPaneEdge("b", DockingPosition.RIGHT));
            pane.resize(1200, 800);
            assertEquals(0.8, split(pane).getDividerPositions().get(0), 0.0001);
            pane.resize(1600, 900);
            assertEquals("Pixels specify the initial size, not a permanent constraint",
                    0.8, split(pane).getDividerPositions().get(0), 0.0001);
        });
    }

    @Test
    public void nestedPerpendicularSplitsStillMeasureWeightAgainstTheEntirePane() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            open(context, pane, "a");
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            Dockable d = open(context, pane, "d");
            d.setPreferredDockingSize(DockingSize.weight(0.2));
            assertTrue(pane.moveDockable("d", "c", DockingPosition.LEFT));
            DockingLayoutNode.Split right = (DockingLayoutNode.Split) split(pane).getChildren().get(1);
            DockingLayoutNode.Split bottom = (DockingLayoutNode.Split) right.getChildren().get(1);
            assertEquals(List.of(0.5), split(pane).getDividerPositions());
            assertEquals(List.of(0.5), right.getDividerPositions());
            assertEquals(0.4, bottom.getDividerPositions().get(0), 0.0001);
        });
    }

    @Test
    public void crossPaneMovesAndExternalDropsUseDestinationDimensions() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane source = context.createPane();
            source.resize(400, 300);
            Dockable a = open(context, source, "a");
            a.setPreferredDockingSize(DockingSize.pixels(200));
            DockingPane target = context.createPane();
            target.resize(1000, 800);
            open(context, target, "b");
            assertTrue(target.moveDockable("a", "b", DockingPosition.LEFT));
            assertEquals(List.of(0.2), split(target).getDividerPositions());
            context.setDockableDropPredicate(event -> true);
            context.setDockableDropFactory(event -> {
                Dockable dockable = new Dockable("dropped", "Dropped", new Region());
                dockable.setPreferredDockingSize(DockingSize.pixels(160));
                return dockable;
            });
            DragEvent drop = new DragEvent(DragEvent.DRAG_DROPPED, null, 100, 100, 100, 100,
                    TransferMode.COPY, new Group(), null, new PickResult(null, 100, 100));
            assertTrue(context.dropDockable(target, drop, null, DockingPosition.BOTTOM).isPresent());
            assertEquals(Orientation.VERTICAL, split(target).getOrientation());
            assertEquals(List.of(0.8), split(target).getDividerPositions());
        });
    }

    @Test
    public void internalDragPreviewAndDropUsePreferredWidth() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            Dockable a = open(context, pane, "a");
            Dockable b = open(context, pane, "b");
            b.setPreferredDockingSize(DockingSize.pixels(240));
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            Node source = pane.lookupAll(".docking-tab").stream()
                    .filter(node -> "b".equals(node.getProperties().get("docking.dockable-id")))
                    .findFirst().orElseThrow();
            Node target = b.getContent();
            fireDrag(source, target, DragEvent.DRAG_OVER, 100, 300);
            Region preview = (Region) pane.lookup(".docking-drop-zone-preview");
            assertTrue(preview.isVisible());
            assertEquals(240, preview.getWidth(), 0.01);
            assertTrue(fireDrag(source, target, DragEvent.DRAG_DROPPED, 100, 300).isDropCompleted());
            assertEquals(0.24, split(pane).getDividerPositions().get(0), 0.0001);
            assertSame(a.getDockingPane(), b.getDockingPane());
        });
    }

    @Test
    public void wholeGroupUsesSelectedTabsPreferenceRatherThanFirstTabsPreference() {
        runFx(() -> {
            for (DockingSize selectedSize : new DockingSize[]{null, DockingSize.weight(0.3)}) {
                DockingContext context = context();
                DockingPane source = context.createPane();
                Dockable a = open(context, source, "a");
                a.setPreferredDockingSize(DockingSize.weight(0.1));
                Dockable b = open(context, source, "b");
                b.setPreferredDockingSize(selectedSize);
                DockingPane target = context.createPane();
                open(context, target, "x");
                assertTrue(context.moveTabGroup(target, "a", null, DockingPosition.LEFT));
                assertEquals(selectedSize == null ? 0.5 : 0.3,
                        split(target).getDividerPositions().get(0), 0.0001);
                assertEquals(List.of("a", "b"),
                        ((DockingLayoutNode.Tabs) split(target).getChildren().get(0)).getDockableIds());
                assertSame(b, target.getActiveDockable());
            }
        });
    }

    @Test
    public void paneEdgeIndicatorPreviewUsesPreferredSize() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            open(context, pane, "a");
            Dockable b = open(context, pane, "b");
            b.setPreferredDockingSize(DockingSize.weight(0.3));
            Dockable c = context.openDockable(pane, "c", DockingPosition.RIGHT).orElseThrow();
            StackPane root = new StackPane(pane);
            new Scene(root, 1000, 600);
            root.applyCss();
            root.layout();
            Node source = pane.lookupAll(".docking-tab").stream()
                    .filter(node -> "b".equals(node.getProperties().get("docking.dockable-id")))
                    .findFirst().orElseThrow();
            fireDrag(source, c.getContent(), DragEvent.DRAG_OVER, 600, 300);
            Node indicator = pane.lookup(".docking-pane-edge-target.pane-edge-right");
            assertTrue(indicator.isVisible());
            Point2D point = indicator.localToScene(indicator.getLayoutBounds().getCenterX(),
                    indicator.getLayoutBounds().getCenterY());
            fireDrag(source, indicator, DragEvent.DRAG_OVER, point.getX(), point.getY());
            Region preview = (Region) pane.lookup(".docking-drop-zone-preview");
            assertEquals(300, preview.getWidth(), 0.01);
            assertEquals(700, preview.getLayoutX(), 0.01);
            assertTrue(fireDrag(source, indicator, DragEvent.DRAG_DROPPED, point.getX(), point.getY()).isDropCompleted());
            assertEquals(0.7, split(pane).getDividerPositions().get(1), 0.0001);
        });
    }

    @Test
    public void boundaryWeightsAndOversizedPixelsAreClampedToAvailableSpace() {
        runFx(() -> {
            for (DockingSize size : new DockingSize[]{DockingSize.weight(0), DockingSize.weight(1),
                    DockingSize.pixels(0), DockingSize.pixels(2000)}) {
                DockingContext context = context();
                DockingPane pane = context.createPane();
                pane.resize(1000, 600);
                open(context, pane, "a");
                context.openDockable(pane, "b", DockingPosition.RIGHT);
                Dockable c = open(context, pane, "c");
                c.setPreferredDockingSize(size);
                assertTrue(pane.moveDockable("c", "b", DockingPosition.LEFT));
                assertEquals(0.5, split(pane).getDividerPositions().get(0), 0.0001);
                assertEquals(size.getValue() == 0 ? 0.5 : 1,
                        split(pane).getDividerPositions().get(1), 0.0001);
            }
        });
    }

    @Test
    public void restorePreservesSavedDividersRatherThanReapplyingPreferences() {
        runFx(() -> {
            DockingContext context = context();
            DockingPane pane = context.createPane();
            open(context, pane, "a");
            Dockable b = open(context, pane, "b");
            b.setPreferredDockingSize(DockingSize.pixels(240));
            pane.moveDockableToPaneEdge("b", DockingPosition.RIGHT);
            pane.updateDividerPositions(List.of(), List.of(0.65));
            DockingLayout saved = context.getLayout();
            assertTrue(context.restoreLayout(saved).getUnresolvedDockableIds().isEmpty());
            pane.resize(1200, 800);
            assertEquals(List.of(0.65), split(pane).getDividerPositions());
        });
    }

    private static DragEvent fireDrag(Node source, Node target,
                                      javafx.event.EventType<DragEvent> type, double x, double y) {
        DragEvent event = new DragEvent(source, target, type, null, x, y, x, y, TransferMode.MOVE,
                source, target, new PickResult(target, x, y));
        Event.fireEvent(target, event);
        return event;
    }
}
