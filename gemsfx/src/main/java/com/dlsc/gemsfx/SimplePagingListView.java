package com.dlsc.gemsfx;

import com.dlsc.gemsfx.paging.PagingListView;
import com.dlsc.gemsfx.paging.PagingLoadRequest;
import com.dlsc.gemsfx.paging.PagingLoadResponse;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.List;
import java.util.Objects;

/**
 * A simple version of the paging list view that is completely based on a list of items, just like a normal
 * list view would be. The view uses an internal data loader that accesses the list to retrieve the items of the
 * current page.
 *
 * @param <T> the type of items to show in the list view
 */
public class SimplePagingListView<T> extends PagingListView<T> {

    private boolean internal;

    /**
     * Constructs a new list view and sets a loader that uses the data list.
     */
    public SimplePagingListView() {
        setLoader(new SimpleLoader(this, itemsProperty()));
        setLoadDelayInMillis(10);
        setCommitLoadStatusDelay(400);
        loaderProperty().addListener(it -> {
            throw new UnsupportedOperationException("a custom loader can not be used for this list view");
        });
    }

    /**
     * Ensures that the given item becomes visible within the list view. This method will only succeed if the
     * given item is a member of the {@link #getItems()}.
     *
     * @param item the item to show
     */
    public final void show(T item) {
        ObservableList<T> items = getItems();
        if (items != null) {
            int index = items.indexOf(item);
            if (index != -1) {
                setPage(index / getPageSize());
            }
        }
    }

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public final ObservableList<T> getItems() {
        return items.get();
    }

    /**
     * Stores the data structure to be used by the list view. The internal data loader will simply retrieve the page
     * items from this list.
     *
     * @return the data model feeding the list view
     */
    public final ListProperty<T> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList<T> items) {
        this.items.set(items);
    }

    /*
     * A convenience class to easily provide a loader for paging when the data is given as an
     * observable list.
     *
     * @param <T> the type of the items
     */
    private final class SimpleLoader implements Callback<PagingLoadRequest, PagingLoadResponse<T>> {

        private final ObservableList<T> data;
        private final PagingListView<T> listView;

        /**
         * Constructs a new simple loader for the given list view and the given data.
         *
         * @param listView the list view where the loader will be used
         * @param data     the observable list that is providing the data / the items
         */
        public SimpleLoader(PagingListView<T> listView, ListProperty<T> data) {
            this.listView = Objects.requireNonNull(listView);
            this.data = Objects.requireNonNull(data);
            this.listView.totalItemCountProperty().bind(Bindings.size(data));
            this.data.addListener((Observable it) -> {
                ObservableList<T> list = getData();
                listView.setPage(Math.min(listView.getTotalItemCount() / listView.getPageSize(), listView.getPage()));
                listView.reload();
            });
        }

        @Override
        public PagingLoadResponse<T> call(PagingLoadRequest param) {
            int page = param.getPage();
            int pageSize = param.getPageSize();
            int offset = page * pageSize;
            return new PagingLoadResponse<>(data.subList(offset, Math.min(data.size(), offset + pageSize)), getData().size());
        }

        public PagingListView<T> getListView() {
            return listView;
        }

        public ObservableList<T> getData() {
            return data;
        }
    }
}
