package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import javafx.util.Callback;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link PagingListView} focusing on property defaults, loader setup,
 * and page-count computation inherited from {@link PagingControlBase}.
 */
public class PagingListViewTest extends FxTestBase {

    private PagingListView<String> create() {
        return invoke(PagingListView::new);
    }

    @Test
    public void testDefaultPageSize() {
        PagingListView<String> view = create();
        assertEquals(10, view.getPageSize());
    }

    @Test
    public void testDefaultPage() {
        PagingListView<String> view = create();
        assertEquals(0, view.getPage());
    }

    @Test
    public void testDefaultLoaderIsNull() {
        PagingListView<String> view = create();
        assertNull(view.getLoader());
    }

    @Test
    public void testSetLoader() {
        PagingListView<String> view = create();
        Callback<PagingLoadRequest, PagingLoadResponse<String>> loader = req ->
                new PagingLoadResponse<>(Arrays.asList("x"), 1);
        runFx(() -> view.setLoader(loader));
        assertNotNull(view.getLoader());
    }

    @Test
    public void testPageCountWithLoader() {
        PagingListView<String> view = create();
        runFx(() -> {
            view.setPageSize(5);
            view.setTotalItemCount(23);
        });
        assertEquals(5, view.getPageCount());
    }

    @Test
    public void testSetPageSize() {
        PagingListView<String> view = create();
        runFx(() -> view.setPageSize(20));
        assertEquals(20, view.getPageSize());
    }

    @Test
    public void testDefaultShowPagingControls() {
        PagingListView<String> view = create();
        assertTrue(view.isShowPagingControls());
    }

    @Test
    public void testDefaultFillLastPage() {
        PagingListView<String> view = create();
        assertFalse(view.isFillLastPage());
    }

    @Test
    public void testGetItemsOnCurrentPageIsNotNull() {
        PagingListView<String> view = create();
        assertNotNull(view.getItemsOnCurrentPage());
    }

    @Test
    public void testLayout() {
        PagingListView<String> view = layout(invoke(PagingListView::new));
        assertNotNull(view.getSkin());
    }
}
