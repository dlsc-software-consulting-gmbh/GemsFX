package com.dlsc.gemsfx.paging;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A paging loader implementation that can be used in combination with a paging list view or a
 * paging grid table view. This loader works based on top of an observable list property. This allows applications
 * to use a paging control even though all the data is already there. Normally a paging control is used when data
 * resides on the server and needs to be loaded page by page.
 *
 * @see PagingListView#setLoader(Callback)
 * @see PagingGridTableView#setLoader(Callback)
 * @see SimplePagingListView
 * @see SimplePagingGridTableView
 *
 * @param <T> the type of the items
 */
public class SimpleLoader<T> implements Callback<PagingLoadRequest, PagingLoadResponse<T>> {

    private final ListProperty<T> items;

    /**
     * Constructs a new simple loader for the given list view and the given data.
     *
     * @param control the control where the loader will be used
     * @param data    the observable list that is providing the data / the items
     */
    public SimpleLoader(ItemPagingControlBase<T> control, ListProperty<T> data) {
        this.items = Objects.requireNonNull(data);
        this.items.addListener((Observable it) -> {
            ObservableList<T> list = getItems();
            control.setPage(Math.min(control.getTotalItemCount() / control.getPageSize(), control.getPage()));
            control.reload();
        });
    }

    @Override
    public PagingLoadResponse<T> call(PagingLoadRequest param) {
        int page = param.getPage();
        int pageSize = param.getPageSize();
        int offset = page * pageSize;

        // copy into new list to avoid concurrent modification exception
        return new PagingLoadResponse<>(new ArrayList<>(items.subList(offset, Math.min(items.size(), offset + pageSize))), getItems().size());
    }

    public ListProperty<T> getItems() {
        return items;
    }
}