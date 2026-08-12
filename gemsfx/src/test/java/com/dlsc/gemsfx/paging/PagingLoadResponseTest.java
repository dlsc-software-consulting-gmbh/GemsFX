package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link PagingLoadResponse} value object.
 */
public class PagingLoadResponseTest extends FxTestBase {

    @Test
    public void testGetItems() {
        List<String> items = Arrays.asList("a", "b", "c");
        PagingLoadResponse<String> response = new PagingLoadResponse<>(items, 100);
        assertEquals(items, response.getItems());
    }

    @Test
    public void testGetTotalItemCount() {
        PagingLoadResponse<String> response = new PagingLoadResponse<>(Collections.emptyList(), 42);
        assertEquals(42, response.getTotalItemCount());
    }

    @Test
    public void testEmptyResponse() {
        PagingLoadResponse<String> response = PagingLoadResponse.emptyResponse();
        assertNotNull(response);
        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalItemCount());
    }

    @Test(expected = NullPointerException.class)
    public void testNullItemsThrows() {
        new PagingLoadResponse<>(null, 0);
    }

    @Test
    public void testZeroTotalCount() {
        PagingLoadResponse<Integer> response = new PagingLoadResponse<>(Collections.emptyList(), 0);
        assertEquals(0, response.getTotalItemCount());
        assertTrue(response.getItems().isEmpty());
    }
}
