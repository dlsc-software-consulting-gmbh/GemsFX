package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import javafx.collections.FXCollections;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link SimplePagingGridTableView}, which uses a {@link SimpleLoader} internally
 * to page through an observable list.
 */
public class SimplePagingGridTableViewTest extends FxTestBase {

    private SimplePagingGridTableView<String> create() {
        return invoke(SimplePagingGridTableView::new);
    }

    @Test
    public void testLoaderIsSetAutomatically() {
        SimplePagingGridTableView<String> view = create();
        assertNotNull(view.getLoader());
    }

    @Test
    public void testSetLoaderIsRejected() {
        SimplePagingGridTableView<String> view = create();
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
        SimplePagingGridTableView<String> view = create();
        runFx(() -> view.getItems().addAll("a", "b", "c"));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testPageCountAfterAddingItems() {
        SimplePagingGridTableView<String> view = create();
        runFx(() -> {
            view.setPageSize(4);
            view.getItems().addAll("a", "b", "c", "d", "e");
        });
        assertPageCountEventually(2, view::getPageCount);
    }

    @Test
    public void testShowMethod() {
        SimplePagingGridTableView<String> view = create();
        runFx(() -> {
            view.setPageSize(3);
            view.getItems().addAll("a", "b", "c", "d", "e");
            view.show("d"); // index 3 → page 1
        });
        assertEquals(1, view.getPage());
    }

    @Test
    public void testSetItems() {
        SimplePagingGridTableView<String> view = create();
        runFx(() -> view.setItems(FXCollections.observableArrayList("x", "y", "z")));
        assertEquals(3, view.getItems().size());
    }

    @Test
    public void testLayout() {
        SimplePagingGridTableView<String> view = layout(invoke(SimplePagingGridTableView::new));
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
