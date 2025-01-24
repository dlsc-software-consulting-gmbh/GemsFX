package com.dlsc.gemsfx.paging;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Callback;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple version of the paging grid table view that is completely based on a list of items, just like a normal
 * table view would be. The view uses an internal data loader that accesses the list to retrieve the items of the
 * current page.
 *
 * @param <T> the type of items to show in the list view
 */
public class SimplePagingGridTableView<T> extends PagingGridTableView<T> {

    private boolean internal;

    /**
     * Constructs a new table view and sets a loader that uses the data list.
     */
    public SimplePagingGridTableView() {
        setLoader(new SimpleLoader(this, itemsProperty()));
        setLoadDelayInMillis(10);
        setCommitLoadStatusDelay(400);
        loaderProperty().addListener(it -> {
            throw new UnsupportedOperationException("a custom loader can not be used for this list view");
        });
    }

    /**
     * Ensures that the given item becomes visible within the table view. This method will only succeed if the
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
     * Stores the data structure to be used by the table view. The internal data loader will simply retrieve the page
     * items from this list.
     *
     * @return the data model feeding the table view
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
        private final PagingGridTableView<T> gridTableView;

        /**
         * Constructs a new simple loader for the given list view and the given data.
         *
         * @param tableView the list view where the loader will be used
         * @param data     the observable list that is providing the data / the items
         */
        public SimpleLoader(PagingGridTableView<T> tableView, ListProperty<T> data) {
            this.gridTableView = Objects.requireNonNull(tableView);
            this.data = Objects.requireNonNull(data);
            this.data.addListener((Observable it) -> {
                ObservableList<T> list = getData();
                tableView.setPage(Math.min(tableView.getTotalItemCount() / tableView.getPageSize(), tableView.getPage()));
                tableView.reload();
            });
        }

        @Override
        public PagingLoadResponse<T> call(PagingLoadRequest param) {
            int page = param.getPage();
            int pageSize = param.getPageSize();
            int offset = page * pageSize;

            // copy into new list to avoid concurrent modification exception
            return new PagingLoadResponse<>(new ArrayList<>(data.subList(offset, Math.min(data.size(), offset + pageSize))), data.size());
        }

        public PagingGridTableView<T> getGridTableView() {
            return gridTableView;
        }

        public ObservableList<T> getData() {
            return data;
        }
    }
}
