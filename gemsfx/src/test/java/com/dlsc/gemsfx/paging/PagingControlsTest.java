package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

/**
 * Tests for {@link PagingControls} — page-count computation, navigation, and property defaults.
 * Navigation is tested via the concrete {@link PagingControls} class which inherits from
 * {@link PagingControlBase}.
 */
public class PagingControlsTest extends FxTestBase {

    private PagingControls create() {
        return invoke(PagingControls::new);
    }

    @Test
    public void testDefaultPage() {
        PagingControls pc = create();
        assertEquals(0, pc.getPage());
    }

    @Test
    public void testDefaultPageSize() {
        PagingControls pc = create();
        assertEquals(10, pc.getPageSize());
    }

    @Test
    public void testPageCountComputation() {
        PagingControls pc = create();
        runFx(() -> pc.setTotalItemCount(55));
        // pageSize=10, 55 items → 6 pages
        assertEquals(6, pc.getPageCount());
    }

    @Test
    public void testPageCountExact() {
        PagingControls pc = create();
        runFx(() -> pc.setTotalItemCount(50));
        assertEquals(5, pc.getPageCount());
    }

    @Test
    public void testPageCountZero() {
        PagingControls pc = create();
        runFx(() -> pc.setTotalItemCount(0));
        assertEquals(0, pc.getPageCount());
    }

    @Test
    public void testNextPage() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(30);
            pc.setPage(0);
            pc.nextPage();
        });
        assertEquals(1, pc.getPage());
    }

    @Test
    public void testPreviousPage() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(30);
            pc.setPage(2);
            pc.previousPage();
        });
        assertEquals(1, pc.getPage());
    }

    @Test
    public void testFirstPage() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(100);
            pc.setPage(5);
            pc.firstPage();
        });
        assertEquals(0, pc.getPage());
    }

    @Test
    public void testLastPage() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(55);
            pc.setPage(0);
            pc.lastPage();
        });
        assertEquals(5, pc.getPage());
    }

    @Test
    public void testNextPageClampsAtBoundary() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(30);
            pc.setPage(2); // already last page (pages 0,1,2)
            pc.nextPage();
        });
        assertEquals(2, pc.getPage());
    }

    @Test
    public void testPreviousPageClampsAtZero() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(30);
            pc.setPage(0);
            pc.previousPage();
        });
        assertEquals(0, pc.getPage());
    }

    @Test
    public void testPageReducedWhenPageCountShrinks() {
        PagingControls pc = create();
        runFx(() -> {
            pc.setTotalItemCount(100);
            pc.setPage(9); // 10 pages total (0-9)
            pc.setTotalItemCount(30); // now 3 pages (0-2), page should clamp to 2
        });
        assertTrue(pc.getPage() <= 2);
    }

    @Test
    public void testDefaultShowPageSizeSelector() {
        PagingControls pc = create();
        assertTrue(pc.isShowPageSizeSelector());
    }

    @Test
    public void testDefaultShowPreviousNextPageButton() {
        PagingControls pc = create();
        assertTrue(pc.isShowPreviousNextPageButton());
    }

    @Test
    public void testDefaultSameWidthPageButtons() {
        PagingControls pc = create();
        assertFalse(pc.isSameWidthPageButtons());
    }
}
