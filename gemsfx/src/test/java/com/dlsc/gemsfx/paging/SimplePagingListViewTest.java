package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import javafx.collections.FXCollections;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SimplePagingListView}, which wraps an observable list with transparent
 * paging via an internal {@link SimpleLoader}.
 */
public class SimplePagingListViewTest extends FxTestBase {

    private SimplePagingListView<String> create() {
        return invoke(SimplePagingListView::new);
    }

    @Test
    public void testLoaderIsSetAutomatically() {
        SimplePagingListView<String> view = create();
        assertNotNull(view.getLoader());
    }

    @Test
    public void testSetLoaderIsRejected() {
        SimplePagingListView<String> view = create();
        view.getLoader(); // validates the property so that the invalidation listener fires again

        // JavaFX swallows exceptions thrown by property listeners and forwards them to the
        // uncaught exception handler of the FX thread, hence the handler is used to detect it.
        java.util.concurrent.atomic.AtomicReference<Throwable> caught = new java.util.concurrent.atomic.AtomicReference<>();
        runFx(() -> {
            Thread.UncaughtExceptionHandler previous = Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> caught.set(e));
            try {
                view.setLoader(req -> new PagingLoadResponse<>(Arrays.asList("x"), 1));
            } catch (UnsupportedOperationException e) {
                caught.set(e);
            } finally {
                Thread.currentThread().setUncaughtExceptionHandler(previous);
            }
        });

        assertNotNull("setting a custom loader must be rejected", caught.get());
        assertTrue(caught.get() instanceof UnsupportedOperationException);
    }

    @Test
    public void testAddItems() {
        SimplePagingListView<String> view = create();
        runFx(() -> view.getItems().addAll("a", "b", "c"));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testPageCountAfterAddingItems() {
        SimplePagingListView<String> view = create();
        runFx(() -> {
            view.setPageSize(3);
            view.getItems().addAll("a", "b", "c", "d", "e");
        });
        assertPageCountEventually(2, view::getPageCount);
    }

    @Test
    public void testShowMethod() {
        SimplePagingListView<String> view = create();
        runFx(() -> {
            view.setPageSize(3);
            view.getItems().addAll("a", "b", "c", "d", "e");
            view.show("d"); // "d" is at index 3, page 1
        });
        assertEquals(1, view.getPage());
    }

    @Test
    public void testShowMethodItemNotPresent() {
        SimplePagingListView<String> view = create();
        runFx(() -> {
            view.setPageSize(3);
            view.getItems().addAll("a", "b", "c");
            view.show("z"); // not present, page unchanged
        });
        assertEquals(0, view.getPage());
    }

    @Test
    public void testSetItems() {
        SimplePagingListView<String> view = create();
        runFx(() -> view.setItems(FXCollections.observableArrayList("x", "y")));
        assertEquals(2, view.getItems().size());
    }

    @Test
    public void testLayout() {
        SimplePagingListView<String> view = layout(invoke(SimplePagingListView::new));
        assertNotNull(view.getSkin());
    }

    /**
     * The simple paging views load their items asynchronously, hence the page count only becomes
     * available after the loading service has finished. Polls with an upper bound so that the test
     * fails with a clear message instead of hanging.
     */
    private static void assertPageCountEventually(int expected, java.util.function.IntSupplier pageCount) {
        long deadline = System.currentTimeMillis() + 10_000;
        int actual = pageCount.getAsInt();
        while (actual != expected && System.currentTimeMillis() < deadline) {
            waitForFxEvents();
            actual = pageCount.getAsInt();
        }
        assertEquals("page count was not updated by the asynchronous loader", expected, actual);
    }
}
