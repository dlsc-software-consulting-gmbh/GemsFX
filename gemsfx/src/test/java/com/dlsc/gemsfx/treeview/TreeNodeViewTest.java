package com.dlsc.gemsfx.treeview;

import com.dlsc.gemsfx.FxTestBase;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for {@link TreeNodeView}.
 */
public class TreeNodeViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertNotNull(view);
    }

    @Test
    public void testStyleClass() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertTrue(view.getStyleClass().contains("tree-node-view"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testSkinCreation() {
        TreeNode<String> root = invoke(() -> new TreeNode<>("root"));
        TreeNodeView<String> view = layout(invoke(() -> new TreeNodeView<>(root)));
        assertNotNull(view.getSkin());
    }

    @Test
    public void testDefaultRootIsNull() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertNull(view.getRoot());
    }

    @Test
    public void testRootRoundTrip() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        TreeNode<String> root = new TreeNode<>("root");
        runFx(() -> view.setRoot(root));
        assertEquals(root, view.getRoot());
    }

    @Test
    public void testCellFactoryDefaultNotNull() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertNotNull(view.getCellFactory());
    }

    @Test
    public void testCellFactoryRoundTrip() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        runFx(() -> view.setCellFactory(val -> {
            TreeNodeCell<String> cell = new TreeNodeCell<>();
            cell.setText(val == null ? "" : val);
            return cell;
        }));
        assertNotNull(view.getCellFactory());
    }

    @Test
    public void testDefaultCellWidth() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertEquals(60.0, view.getCellWidth(), 0.001);
    }

    @Test
    public void testCellWidthRoundTrip() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        runFx(() -> view.setCellWidth(120.0));
        assertEquals(120.0, view.getCellWidth(), 0.001);
    }

    @Test
    public void testDefaultCellHeight() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertEquals(30.0, view.getCellHeight(), 0.001);
    }

    @Test
    public void testDefaultLayoutDirection() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertEquals(TreeNodeView.LayoutDirection.TOP_TO_BOTTOM, view.getLayoutDirection());
    }

    @Test
    public void testLayoutDirectionRoundTrip() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        runFx(() -> view.setLayoutDirection(TreeNodeView.LayoutDirection.LEFT_TO_RIGHT));
        assertEquals(TreeNodeView.LayoutDirection.LEFT_TO_RIGHT, view.getLayoutDirection());
    }

    @Test
    public void testDefaultRowAlignment() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertEquals(VPos.CENTER, view.getRowAlignment());
    }

    @Test
    public void testDefaultColumnAlignment() {
        TreeNodeView<String> view = invoke(TreeNodeView::new);
        assertEquals(HPos.CENTER, view.getColumnAlignment());
    }
}
