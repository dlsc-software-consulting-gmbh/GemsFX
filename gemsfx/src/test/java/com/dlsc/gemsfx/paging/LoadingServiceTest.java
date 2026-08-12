package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests for {@link LoadingService} property defaults and configuration.
 * Async loading behavior is not tested to avoid flakiness.
 */
public class LoadingServiceTest extends FxTestBase {

    private LoadingService<String> create() {
        return invoke(LoadingService::new);
    }

    @Test
    public void testDefaultPageIsUndefined() {
        LoadingService<String> service = create();
        assertEquals(LoadingService.UNDEFINED, service.getPage());
    }

    @Test
    public void testDefaultPageSizeIsUndefined() {
        LoadingService<String> service = create();
        assertEquals(LoadingService.UNDEFINED, service.getPageSize());
    }

    @Test
    public void testDefaultLoadDelayInMillis() {
        LoadingService<String> service = create();
        assertEquals(200L, service.getLoadDelayInMillis());
    }

    @Test
    public void testDefaultLoaderIsNull() {
        LoadingService<String> service = create();
        assertNull(service.getLoader());
    }

    @Test
    public void testSetPage() {
        LoadingService<String> service = create();
        runFx(() -> service.pageProperty().set(3));
        assertEquals(3, service.getPage());
    }

    @Test
    public void testSetPageSize() {
        LoadingService<String> service = create();
        runFx(() -> service.pageSizeProperty().set(25));
        assertEquals(25, service.getPageSize());
    }

    @Test
    public void testSetLoadDelayInMillis() {
        LoadingService<String> service = create();
        runFx(() -> service.setLoadDelayInMillis(100L));
        assertEquals(100L, service.getLoadDelayInMillis());
    }

    @Test
    public void testSetLoader() {
        LoadingService<String> service = create();
        runFx(() -> service.loaderProperty().set(req -> PagingLoadResponse.emptyResponse()));
        assertEquals(LoadingService.UNDEFINED, service.getPage()); // page unchanged
    }
}
