package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link SimpleLoader} helper that serves items from an observable list.
 */
public class SimpleLoaderTest extends FxTestBase {

    private SimplePagingListView<String> createView(List<String> data) {
        return invoke(() -> {
            SimplePagingListView<String> view = new SimplePagingListView<>();
            view.getItems().addAll(data);
            return view;
        });
    }

    @Test
    public void testFirstPage() {
        SimplePagingListView<String> view = createView(Arrays.asList("a", "b", "c", "d", "e"));
        PagingLoadResponse<String> response = invoke(() -> {
            view.setPageSize(3);
            view.setPage(0);
            PagingLoadRequest req = new PagingLoadRequest(0, 3);
            return view.getLoader().call(req);
        });
        assertEquals(Arrays.asList("a", "b", "c"), response.getItems());
        assertEquals(5, response.getTotalItemCount());
    }

    @Test
    public void testSecondPage() {
        SimplePagingListView<String> view = createView(Arrays.asList("a", "b", "c", "d", "e"));
        PagingLoadResponse<String> response = invoke(() -> {
            PagingLoadRequest req = new PagingLoadRequest(1, 3);
            return view.getLoader().call(req);
        });
        assertEquals(Arrays.asList("d", "e"), response.getItems());
        assertEquals(5, response.getTotalItemCount());
    }

    @Test
    public void testEmptyList() {
        SimplePagingListView<String> view = createView(Arrays.asList());
        PagingLoadResponse<String> response = invoke(() -> {
            PagingLoadRequest req = new PagingLoadRequest(0, 10);
            return view.getLoader().call(req);
        });
        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItemCount());
    }

    @Test
    public void testExactPageBoundary() {
        List<String> data = Arrays.asList("a", "b", "c", "d", "e", "f");
        SimplePagingListView<String> view = createView(data);
        PagingLoadResponse<String> response = invoke(() -> {
            PagingLoadRequest req = new PagingLoadRequest(1, 3);
            return view.getLoader().call(req);
        });
        assertEquals(Arrays.asList("d", "e", "f"), response.getItems());
    }
}
