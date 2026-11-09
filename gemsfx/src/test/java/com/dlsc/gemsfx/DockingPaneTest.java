package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.DockingPaneSkin;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.stage.Stage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class DockingPaneTest extends FxTestBase {

    private static DockingContext context(Dockable... dockables) {
        return new DockingContext(id -> {
            for (Dockable d : dockables) {
                if (d.getId().equals(id)) {
                    return Optional.of(d);
                }
            }
            return Optional.empty();
        }, pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    private static Dockable dockable(String id) {
        return new Dockable(id, "Title " + id, new Label("Content " + id));
    }

    private static void press(Node node, KeyCode code) {
        Event.fireEvent(node, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void mouse(Node node, EventType<MouseEvent> type, boolean stillSincePress) {
        Event.fireEvent(node, new MouseEvent(type, 5, 5, 105, 105, MouseButton.PRIMARY, 1,
                false, false, false, false, type == MouseEvent.MOUSE_PRESSED, false, false,
                false, false, stillSincePress, new PickResult(node, 5, 5)));
    }

    private static List<Node> titles(DockingPane pane) {
        return new ArrayList<>(pane.lookupAll(".docking-tab"));
    }

    private static Node title(DockingPane pane, String id) {
        for (Node node : titles(pane)) {
            if (id.equals(node.getProperties().get("docking.dockable-id"))) {
                return node;
            }
        }
        throw new AssertionError("no title for " + id);
    }

    private static Object invokeSkinPrivate(DockingPaneSkin skin, String name, Class<?>[] types, Object... args) {
        try {
            Method method = DockingPaneSkin.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(skin, args);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    @Test
    public void dockingPaneCreatesItsCustomSkinRoleAndStylesheet() {
        invoke(() -> {
            DockingPane pane = layout(context().createPane());

            assertTrue(pane.getSkin() instanceof DockingPaneSkin);
            assertEquals(javafx.scene.AccessibleRole.NODE, pane.getAccessibleRole());
            assertNotNull(pane.getAccessibleRoleDescription());
            assertTrue(pane.getUserAgentStylesheet().endsWith("docking-pane.css"));
            assertTrue(pane.getStyleClass().contains("docking-pane"));
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            return null;
        });
    }

    @Test
    public void openedDockablesAreInsertedAtTheRequestedPosition() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();

            context.openDockable(pane, "a", DockingPosition.RIGHT);
            DockingLayoutNode.Tabs root = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a"), root.getDockableIds());

            context.openDockable(pane, "b", DockingPosition.CENTER);
            root = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a", "b"), root.getDockableIds());
            assertEquals("b", pane.getActiveDockable().getId());

            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.VERTICAL, split.getOrientation());
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) split.getFirst()).getDockableIds());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) split.getSecond()).getDockableIds());
            return null;
        });
    }

    @Test
    public void everyDockableGetsItsOwnTitleAndOnlySelectedContentIsAttached() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            assertEquals(2, titles(pane).size());
            assertEquals(2, pane.lookupAll(".docking-tab-close-button").size());
            assertTrue(pane.lookupAll(".tab-pane").isEmpty());
            assertNull(a.getContent().getParent());
            assertNotNull(b.getContent().getParent());

            press(title(pane, "a"), KeyCode.ENTER);
            pane.applyCss();
            pane.layout();

            assertSame(a, pane.getActiveDockable());
            assertNotNull(a.getContent().getParent());
            assertNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void openingDockableAfterPaneIsMountedRendersItsTitleAndContent() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            context.openDockable(pane, "b", DockingPosition.CENTER);
            pane.applyCss();
            pane.layout();

            assertEquals(2, titles(pane).size());
            assertNotNull(title(pane, "b"));
            assertNull(a.getContent().getParent());
            assertNotNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void openingDockableAfterDividerChangeImmediatelyUpdatesSplitLayout() {
        invoke(() -> {
            Dockable search = dockable("search");
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"), search);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            layout(pane);

            javafx.scene.control.SplitPane split =
                    (javafx.scene.control.SplitPane) pane.lookup(".docking-split");
            split.setDividerPositions(0.3);
            context.openDockable(pane, "search", DockingPosition.CENTER);
            pane.applyCss();
            pane.layout();

            assertEquals(4, titles(pane).size());
            assertSame(pane.getScene(), title(pane, "search").getScene());
            assertSame(pane.getScene(), search.getContent().getScene());
            assertSame(search, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void pressingSelectedOrUnselectedTitleKeepsDragSourceInScene() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            for (String id : List.of("b", "a")) {
                Node source = title(pane, id);
                Scene scene = source.getScene();

                mouse(source.lookup(".docking-tab-title"), MouseEvent.MOUSE_PRESSED, true);

                assertSame("The pressed title must stay attached until drag detection", scene, source.getScene());
                assertSame(source, title(pane, id));
            }
            return null;
        });
    }

    @Test
    public void clickingTitleSelectsItsContent() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node label = title(pane, "a").lookup(".docking-tab-title");

            mouse(label, MouseEvent.MOUSE_PRESSED, true);
            mouse(label, MouseEvent.MOUSE_RELEASED, true);
            mouse(label, MouseEvent.MOUSE_CLICKED, true);

            assertSame(a, pane.getActiveDockable());
            assertNotNull(a.getContent().getParent());
            assertNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void clickAfterDraggingDoesNotSelectOrRebuildTheSource() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node source = title(pane, "a");
            DockingLayoutNode before = pane.getLayoutRoot();

            mouse(source, MouseEvent.MOUSE_PRESSED, true);
            mouse(source, MouseEvent.MOUSE_RELEASED, false);
            mouse(source, MouseEvent.MOUSE_CLICKED, false);

            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertSame(source, title(pane, "a"));
            return null;
        });
    }

    @Test
    public void closeButtonClickDoesNotSelectItsTabWhenCloseIsVetoed() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.setCanCloseDockable(d -> false);
            layout(pane);
            Button close = (Button) title(pane, "a").lookup(".docking-tab-close-button");
            DockingLayoutNode before = pane.getLayoutRoot();

            close.fire();
            mouse(close, MouseEvent.MOUSE_CLICKED, true);

            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void skinDoesNotUseJavaFxTabPane() {
        invoke(() -> {
            DockingContext context = context(dockable("a"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            assertFalse(containsTabPane(pane));
            return null;
        });
    }

    private static boolean containsTabPane(Node node) {
        if (node instanceof TabPane) {
            return true;
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                if (containsTabPane(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void arrowKeysMoveSelectionBetweenTitles() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            press(title(pane, "b"), KeyCode.LEFT);
            assertEquals("a", pane.getActiveDockable().getId());
            press(title(pane, "a"), KeyCode.RIGHT);
            assertEquals("b", pane.getActiveDockable().getId());
            return null;
        });
    }

    @Test
    public void keyboardCloseRemovesDockableWhenPolicyAllows() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            press(title(pane, "b"), KeyCode.DELETE);
            pane.applyCss();
            pane.layout();

            assertEquals(1, context.getDockables().size());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            assertEquals("a", pane.getActiveDockable().getId());
            assertEquals(1, titles(pane).size());
            return null;
        });
    }

    @Test
    public void closeButtonHonoursVeto() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            context.setCanCloseDockable(d -> false);
            DockingLayoutNode before = pane.getLayoutRoot();

            ((Button) title(pane, "b").lookup(".docking-tab-close-button")).fire();
            press(title(pane, "b"), KeyCode.DELETE);

            assertEquals(2, context.getDockables().size());
            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, titles(pane).size());

            context.setCanCloseDockable(d -> true);
            ((Button) title(pane, "b").lookup(".docking-tab-close-button")).fire();
            assertEquals(1, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void selectDockableUpdatesSnapshotAndActiveDockable() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);

            pane.selectDockable("a");

            assertEquals("a", ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getSelectedId());
            assertEquals("a", pane.getActiveDockable().getId());
            assertThrows(IllegalArgumentException.class, () -> pane.selectDockable("missing"));
            return null;
        });
    }

    @Test
    public void publicPaneMutationsRejectCallsOffTheFxThread() {
        DockingPane pane = invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane created = context.createPane();
            context.openDockable(created, "a", DockingPosition.CENTER);
            context.openDockable(created, "b", DockingPosition.RIGHT);
            return created;
        });

        assertThrows(IllegalStateException.class, () -> pane.selectDockable("a"));
        assertThrows(IllegalStateException.class, () -> pane.updateDividerPosition(List.of(), 0.7));
    }

    @Test
    public void draggingDividersUpdatesSnapshotWithoutRebuildingTheDraggedSplit() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            layout(pane);

            List<Node> splits = new ArrayList<>(pane.lookupAll(".docking-split"));
            javafx.scene.control.SplitPane outer = (javafx.scene.control.SplitPane) splits.get(0);
            javafx.scene.control.SplitPane inner = (javafx.scene.control.SplitPane) splits.get(1);

            outer.setDividerPositions(0.3);
            inner.setDividerPositions(0.8);

            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(0.3, root.getDividerPosition(), 1e-9);
            assertEquals(0.8, ((DockingLayoutNode.Split) root.getSecond()).getDividerPosition(), 1e-9);
            assertSame(outer, pane.lookup(".docking-split"));
            assertEquals(0.3, outer.getDividerPositions()[0], 1e-9);

            pane.selectDockable("a");
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(0.3, root.getDividerPosition(), 1e-9);
            assertEquals(0.3, ((javafx.scene.control.SplitPane) pane.lookup(".docking-split"))
                    .getDividerPositions()[0], 1e-9);
            return null;
        });
    }

    @Test
    public void draggingDividersToEndpointsPreservesVisibleProportion() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            layout(pane);

            javafx.scene.control.SplitPane split = (javafx.scene.control.SplitPane) pane.lookup(".docking-split");
            for (double endpoint : new double[]{0.0, 1.0}) {
                split.setDividerPositions(endpoint);
                assertEquals(endpoint, ((DockingLayoutNode.Split) pane.getLayoutRoot()).getDividerPosition(), 0);
                assertSame(split, pane.lookup(".docking-split"));
                assertEquals(endpoint, split.getDividerPositions()[0], 0);
            }

            pane.selectDockable("a");
            assertEquals(1.0, ((DockingLayoutNode.Split) pane.getLayoutRoot()).getDividerPosition(), 0);
            assertEquals(1.0, ((javafx.scene.control.SplitPane) pane.lookup(".docking-split"))
                    .getDividerPositions()[0], 0);
            return null;
        });
    }

    @Test
    public void resolverPathReordersWithinSameGroupUsingHoveredTitle() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.CENTER);

            assertTrue(pane.moveDockable("c", 50, 50, 100, 100, "a"));
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a", "c", "b"), tabs.getDockableIds());
            assertEquals("c", tabs.getSelectedId());
            return null;
        });
    }

    @Test
    public void resolverPathMovesToEdgeWithExpectedSplitOrientationAndOrder() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 98, 50, 100, 100, "c"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.HORIZONTAL, root.getOrientation());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) root.getFirst()).getDockableIds());
            DockingLayoutNode.Split inner = (DockingLayoutNode.Split) root.getSecond();
            assertEquals(Orientation.HORIZONTAL, inner.getOrientation());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) inner.getFirst()).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) inner.getSecond()).getDockableIds());
            return null;
        });
    }

    @Test
    public void resolverPathSplitsSelectedTabFromItsOwnGroupAtAnEdge() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);

            assertTrue(pane.moveDockable("b", 2, 50, 100, 100, "b"));

            DockingLayoutNode.Split split = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.HORIZONTAL, split.getOrientation());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) split.getFirst()).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) split.getSecond()).getDockableIds());
            return null;
        });
    }

    @Test
    public void resolverPathMovesToTopAndBottomWithExpectedOrder() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 70, 2, 100, 100, "c"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            DockingLayoutNode.Split topSplit = (DockingLayoutNode.Split) root.getSecond();
            assertEquals(Orientation.VERTICAL, topSplit.getOrientation());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) topSplit.getFirst()).getDockableIds());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) topSplit.getSecond()).getDockableIds());

            assertTrue(pane.moveDockable("a", 70, 98, 100, 100, "c"));
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            DockingLayoutNode.Split bottomSplit = (DockingLayoutNode.Split) root.getSecond();
            assertEquals(Orientation.VERTICAL, bottomSplit.getOrientation());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) bottomSplit.getFirst()).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) bottomSplit.getSecond()).getDockableIds());
            return null;
        });
    }

    @Test
    public void movingToCenterCollapsesEmptyGroupsAndRedundantSplits() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("b", 20, 50, 100, 100, "a"));
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Tabs);
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            return null;
        });
    }

    @Test
    public void edgeMoveAlsoCollapsesSourceGroupAndRedundantSplit() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 70, 2, 100, 100, "b"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.VERTICAL, root.getOrientation());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) root.getFirst()).getDockableIds());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) root.getSecond()).getDockableIds());
            return null;
        });
    }

    @Test
    public void resolverPathKeepsSourceWhenDropIsRejectedOrCancelled() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            DockingLayoutNode beforeRejectedMove = pane.getLayoutRoot();
            assertFalse(pane.moveDockable("c", 75, 50, 100, 100, null));
            assertFalse(pane.moveDockable("a", 50, 50, 0, 100, "b"));
            assertSame(beforeRejectedMove, pane.getLayoutRoot());
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) ((DockingLayoutNode.Split) pane.getLayoutRoot())
                    .getFirst()).getDockableIds());
            return null;
        });
    }

    @Test
    public void skinResolverUsesHoveredPickPathAndDragDoneClearsPreview() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.CENTER);
            layout(pane);

            DockingPaneSkin skin = (DockingPaneSkin) pane.getSkin();
            Node picked = title(pane, "a").lookup(".docking-tab-title");
            Object resolved = invokeSkinPrivate(skin, "resolveDropTarget",
                    new Class<?>[]{double.class, double.class, Node.class, String.class}, 50d, 50d, picked, "c");
            assertNotNull(resolved);
            try {
                String targetId = (String) resolved.getClass().getMethod("getTargetId").invoke(resolved);
                assertEquals("a", targetId);
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }

            invokeSkinPrivate(skin, "showDropPreview", new Class<?>[]{resolved.getClass()}, resolved);
            Node zonePreview = pane.lookup(".docking-drop-zone-preview");
            assertNotNull(zonePreview);
            assertTrue(zonePreview.isVisible());

            DockingLayoutNode before = pane.getLayoutRoot();
            assertFalse(pane.moveDockable("c", 50, 50, 100, 100, null));
            invokeSkinPrivate(skin, "finishDrag", new Class<?>[]{});
            assertFalse(zonePreview.isVisible());
            assertSame(before, pane.getLayoutRoot());
            return null;
        });
    }
}
