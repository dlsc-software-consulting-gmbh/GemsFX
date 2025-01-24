package com.dlsc.gemsfx.paging;

/**
 * The input parameter for the loader callback of paging controls.
 *
 * @see PagingListView#loaderProperty()
 * @see PagingGridTableView#loaderProperty()
 */
public class PagingLoadRequest {

    private final int page;
    private final int pageSize;

    /**
     * Constructs a new load request for the given page and page size.
     *
     * @param page     the index of the page (starts with 0)
     * @param pageSize the size of the page (number of items per page)
     */
    public PagingLoadRequest(int page, int pageSize) {
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * The index of the page.
     *
     * @return the page index
     */
    public final int getPage() {
        return page;
    }

    /**
     * The size of the page.
     *
     * @return the page size
     */
    public final int getPageSize() {
        return pageSize;
    }
}

