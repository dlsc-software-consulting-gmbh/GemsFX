package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for the {@link PagingLoadRequest} value object.
 */
public class PagingLoadRequestTest extends FxTestBase {

    @Test
    public void testGetPage() {
        PagingLoadRequest request = new PagingLoadRequest(3, 10);
        assertEquals(3, request.getPage());
    }

    @Test
    public void testGetPageSize() {
        PagingLoadRequest request = new PagingLoadRequest(3, 10);
        assertEquals(10, request.getPageSize());
    }

    @Test
    public void testZeroPage() {
        PagingLoadRequest request = new PagingLoadRequest(0, 20);
        assertEquals(0, request.getPage());
        assertEquals(20, request.getPageSize());
    }

    @Test
    public void testLargeValues() {
        PagingLoadRequest request = new PagingLoadRequest(999, 500);
        assertEquals(999, request.getPage());
        assertEquals(500, request.getPageSize());
    }
}
