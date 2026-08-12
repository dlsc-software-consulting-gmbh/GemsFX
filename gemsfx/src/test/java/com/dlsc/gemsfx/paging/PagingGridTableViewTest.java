package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import com.dlsc.gemsfx.gridtable.GridTableColumn;
import javafx.util.Callback;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link PagingGridTableView} focusing on property defaults, columns, and page computation.
 */
public class PagingGridTableViewTest extends FxTestBase {

    private PagingGridTableView<String> create() {
        return invoke(PagingGridTableView::new);
    }

    @Test
    public void testDefaultPage() {
        PagingGridTableView<String> view = create();
        assertEquals(0, view.getPage());
    }

    @Test
    public void testDefaultPageSize() {
        PagingGridTableView<String> view = create();
        assertEquals(10, view.getPageSize());
    }

    @Test
    public void testDefaultLoaderIsNull() {
        PagingGridTableView<String> view = create();
        assertNull(view.getLoader());
    }

    @Test
    public void testColumnsInitiallyEmpty() {
        PagingGridTableView<String> view = create();
        assertTrue(view.getColumns().isEmpty());
    }

    @Test
    public void testAddColumn() {
        PagingGridTableView<String> view = create();
        runFx(() -> {
            GridTableColumn<String, String> col = new GridTableColumn<>("Name");
            view.getColumns().add(col);
        });
        assertEquals(1, view.getColumns().size());
    }

    @Test
    public void testPageCountComputation() {
        PagingGridTableView<String> view = create();
        runFx(() -> {
            view.setPageSize(10);
            view.setTotalItemCount(45);
        });
        assertEquals(5, view.getPageCount());
    }

    @Test
    public void testSetLoader() {
        PagingGridTableView<String> view = create();
        Callback<PagingLoadRequest, PagingLoadResponse<String>> loader =
                req -> new PagingLoadResponse<>(Arrays.asList("a"), 1);
        runFx(() -> view.setLoader(loader));
        assertNotNull(view.getLoader());
    }

    @Test
    public void testGetGridTableView() {
        PagingGridTableView<String> view = create();
        assertNotNull(view.getGridTableView());
    }

    @Test
    public void testLayout() {
        PagingGridTableView<String> view = layout(invoke(PagingGridTableView::new));
        assertNotNull(view.getSkin());
    }
}
