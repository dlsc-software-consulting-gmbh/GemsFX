package com.dlsc.gemsfx.paging;

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
}