package com.dlsc.gemsfx;

import javafx.geometry.Orientation;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DockingLayoutTest extends FxTestBase {

    @Test
    public void dockingLayoutRetainsIdentityAndPanes() {
        DockingLayout layout = new DockingLayout(
                "primary",
                Collections.emptyList()
        );

        assertEquals("primary", layout.getPrimaryPaneId());
        assertNotNull(layout.getPanes());
        assertTrue(layout.getPanes().isEmpty());
    }

    @Test
    public void dockingLayoutCopiesPanesListToPreventExternalMutation() {
        List<DockingLayout.PaneLayout> panes = new ArrayList<>(Arrays.asList(
                new DockingLayout.PaneLayout("pane1", new DockingLayoutNode.Empty(), null)
        ));
        DockingLayout layout = new DockingLayout("primary", panes);

        // Original list should be unchanged when we access layout's panes
        assertEquals(1, layout.getPanes().size());
        assertTrue(layout.getPanes().get(0).getPaneId().equals("pane1"));
        
        // Mutate the original list - should not affect the layout
        panes.add(new DockingLayout.PaneLayout("pane2", new DockingLayoutNode.Empty(), null));
        
        // Layout should still have only one pane
        assertEquals(1, layout.getPanes().size());
    }

    @Test
    public void dockingLayoutRejectsNullPrimaryPaneId() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayout(null, Collections.emptyList())
        );
    }

    @Test
    public void dockingLayoutRejectsBlankPrimaryPaneId() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout("   ", Collections.emptyList())
        );
    }

    @Test
    public void dockingLayoutRejectsNullPanesList() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayout("primary", null)
        );
    }

    @Test
    public void dockingLayoutRejectsNullElementInPanesList() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout("primary", Arrays.asList(
                        new DockingLayout.PaneLayout("pane1", new DockingLayoutNode.Empty(), null),
                        null
                ))
        );
    }

    @Test
    public void paneLayoutRetainsIdentityAndContent() {
        DockingLayoutNode root = new DockingLayoutNode.Empty();
        DockingLayout.PaneLayout pane = new DockingLayout.PaneLayout("pane1", root, null);

        assertEquals("pane1", pane.getPaneId());
        assertSame(root, pane.getRoot());
        assertNull(pane.getStageBounds());
    }

    @Test
    public void paneLayoutRejectsNullPaneId() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayout.PaneLayout(null, new DockingLayoutNode.Empty(), null)
        );
    }

    @Test
    public void paneLayoutRejectsBlankPaneId() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.PaneLayout("   ", new DockingLayoutNode.Empty(), null)
        );
    }

    @Test
    public void paneLayoutRejectsNullRoot() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayout.PaneLayout("pane1", null, null)
        );
    }

    @Test
    public void stageBoundsRetainsCoordinatesAndDimensions() {
        DockingLayout.StageBounds bounds = new DockingLayout.StageBounds(10.5, 20.5, 640.0, 480.0);

        assertEquals(10.5, bounds.getX(), 0.01);
        assertEquals(20.5, bounds.getY(), 0.01);
        assertEquals(640.0, bounds.getWidth(), 0.01);
        assertEquals(480.0, bounds.getHeight(), 0.01);
    }

    @Test
    public void stageBoundsRejectsNonFiniteX() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(Double.POSITIVE_INFINITY, 20.0, 640.0, 480.0)
        );

        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(Double.NaN, 20.0, 640.0, 480.0)
        );
    }

    @Test
    public void stageBoundsRejectsNonFiniteY() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(10.0, Double.NEGATIVE_INFINITY, 640.0, 480.0)
        );
    }

    @Test
    public void stageBoundsRejectsNegativeWidth() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(10.0, 20.0, -640.0, 480.0)
        );
    }

    @Test
    public void stageBoundsRejectsZeroWidth() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(10.0, 20.0, 0.0, 480.0)
        );
    }

    @Test
    public void stageBoundsRejectsNegativeHeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(10.0, 20.0, 640.0, -480.0)
        );
    }

    @Test
    public void stageBoundsRejectsZeroHeight() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayout.StageBounds(10.0, 20.0, 640.0, 0.0)
        );
    }

    @Test
    public void dockingLayoutNodeEmptyIsValid() {
        DockingLayoutNode.Empty empty = new DockingLayoutNode.Empty();
        assertNotNull(empty);
    }

    @Test
    public void dockingLayoutNodeTabsRetainsDockableIdsAndSelected() {
        List<String> ids = Arrays.asList("doc1", "doc2", "doc3");
        DockingLayoutNode.Tabs tabs = new DockingLayoutNode.Tabs(ids, "doc2");

        assertEquals(3, tabs.getDockableIds().size());
        assertEquals("doc2", tabs.getSelectedId());
        assertTrue(tabs.getDockableIds().contains("doc1"));
        assertTrue(tabs.getDockableIds().contains("doc2"));
        assertTrue(tabs.getDockableIds().contains("doc3"));
    }

    @Test
    public void dockingLayoutNodeTabsCopiesDockableIdsToPreventMutation() {
        List<String> ids = new ArrayList<>(Arrays.asList("doc1", "doc2"));
        DockingLayoutNode.Tabs tabs = new DockingLayoutNode.Tabs(ids, "doc1");

        // Mutate original list - should not affect tabs
        ids.add("doc3");

        // Tabs should still have only two ids
        assertEquals(2, tabs.getDockableIds().size());
        assertTrue(tabs.getDockableIds().contains("doc1"));
        assertTrue(tabs.getDockableIds().contains("doc2"));
    }

    @Test
    public void dockingLayoutNodeTabsRejectsNullDockableIds() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayoutNode.Tabs(null, "doc1")
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsNullSelectedId() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayoutNode.Tabs(Arrays.asList("doc1", "doc2"), null)
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsBlankSelectedId() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Tabs(Arrays.asList("doc1", "doc2"), "   ")
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsEmptyDockableIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Tabs(Collections.emptyList(), "doc1")
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsNullElementInDockableIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Tabs(Arrays.asList("doc1", null, "doc3"), "doc1")
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsBlankElementInDockableIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Tabs(Arrays.asList("doc1", "   ", "doc3"), "doc1")
        );
    }

    @Test
    public void dockingLayoutNodeTabsRejectsSelectedIdNotInDockableIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Tabs(Arrays.asList("doc1", "doc2"), "doc3")
        );
    }

    @Test
    public void dockingLayoutNodeSplitRetainsOrientationAndPosition() {
        DockingLayoutNode first = new DockingLayoutNode.Empty();
        DockingLayoutNode second = new DockingLayoutNode.Empty();
        DockingLayoutNode.Split split = new DockingLayoutNode.Split(
                Orientation.HORIZONTAL, 0.5, first, second
        );

        assertEquals(Orientation.HORIZONTAL, split.getOrientation());
        assertEquals(0.5, split.getDividerPosition(), 0.01);
        assertSame(first, split.getFirst());
        assertSame(second, split.getSecond());
    }

    @Test
    public void dockingLayoutNodeSplitRejectsNullOrientation() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayoutNode.Split(null, 0.5, new DockingLayoutNode.Empty(), new DockingLayoutNode.Empty())
        );
    }

    @Test
    public void dockingLayoutNodeSplitRejectsNullFirstNode() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayoutNode.Split(Orientation.VERTICAL, 0.5, null, new DockingLayoutNode.Empty())
        );
    }

    @Test
    public void dockingLayoutNodeSplitRejectsNullSecondNode() {
        assertThrows(NullPointerException.class, () ->
                new DockingLayoutNode.Split(Orientation.HORIZONTAL, 0.5, new DockingLayoutNode.Empty(), null)
        );
    }

    @Test
    public void dockingLayoutNodeSplitRejectsNonFiniteDividerPosition() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Split(Orientation.HORIZONTAL, Double.POSITIVE_INFINITY, 
                        new DockingLayoutNode.Empty(), new DockingLayoutNode.Empty())
        );

        assertThrows(IllegalArgumentException.class, () ->
                new DockingLayoutNode.Split(Orientation.HORIZONTAL, Double.NaN, 
                        new DockingLayoutNode.Empty(), new DockingLayoutNode.Empty())
        );
    }

    @Test
    public void dockingRestoreResultRetainsUnresolvedIds() {
        List<String> unresolvedIds = Arrays.asList("missing1", "missing2");
        DockingRestoreResult result = new DockingRestoreResult(unresolvedIds);

        assertEquals(2, result.getUnresolvedDockableIds().size());
        assertTrue(result.getUnresolvedDockableIds().contains("missing1"));
        assertTrue(result.getUnresolvedDockableIds().contains("missing2"));
    }

    @Test
    public void dockingRestoreResultCopiesUnresolvedIdsToPreventMutation() {
        List<String> unresolvedIds = new ArrayList<>(Arrays.asList("missing1"));
        DockingRestoreResult result = new DockingRestoreResult(unresolvedIds);

        // Mutate original list - should not affect result
        unresolvedIds.add("missing2");

        // Result should still have only one id
        assertEquals(1, result.getUnresolvedDockableIds().size());
        assertTrue(result.getUnresolvedDockableIds().contains("missing1"));
    }

    @Test
    public void dockingRestoreResultRejectsNullUnresolvedIds() {
        assertThrows(NullPointerException.class, () ->
                new DockingRestoreResult(null)
        );
    }

    @Test
    public void dockingRestoreResultRejectsNullElementInUnresolvedIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingRestoreResult(Arrays.asList("missing1", null, "missing2"))
        );
    }

    @Test
    public void dockingRestoreResultRejectsBlankElementInUnresolvedIds() {
        assertThrows(IllegalArgumentException.class, () ->
                new DockingRestoreResult(Arrays.asList("missing1", "   ", "missing2"))
        );
    }

    @Test
    public void dockingPositionEnumHasRequiredValues() {
        assertNotNull(DockingPosition.CENTER);
        assertNotNull(DockingPosition.LEFT);
        assertNotNull(DockingPosition.RIGHT);
        assertNotNull(DockingPosition.TOP);
        assertNotNull(DockingPosition.BOTTOM);
    }
}
