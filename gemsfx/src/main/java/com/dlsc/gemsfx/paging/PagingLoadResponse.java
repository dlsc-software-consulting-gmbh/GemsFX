package com.dlsc.gemsfx.paging;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Constructs a new load response containing the items to show for the requested page
 * and the total item count.
 *
 * @param <T> the type of the items that were queried
 */
public class PagingLoadResponse<T> {

    private final List<T> items;
    private final int totalItemCount;

    /**
     * Constructs a new load response containing the items to display for the requested page
     * and the total number of available items.
     *
     * @param items the list of items of type T to be included in the response; must not be null
     * @param totalItemCount the total number of items available in the data source
     */
    public PagingLoadResponse(List<T> items, int totalItemCount) {
        this.items = Objects.requireNonNull(items, "items list can not be null");
        this.totalItemCount = totalItemCount;
    }

    /**
     * Retrieves the list of items contained in the response.
     *
     * @return a list of items of type T
     */
    public final List<T> getItems() {
        return items;
    }

    /**
     * Retrieves the total count of items contained in the response.
     *
     * @return the total number of items
     */
    public final int getTotalItemCount() {
        return totalItemCount;
    }

    /**
     * An empty response with no items and no total item count. This is useful
     * when some preconditions for running a query are not met. Then the loader can
     * simply return this empty response.
     *
     * @return an empty paging load response
     * @param <T> the type of the items in the empty response
     */
    public static <T> PagingLoadResponse<T> emptyResponse() {
        return new PagingLoadResponse<>(Collections.emptyList(), 0);
    }
}