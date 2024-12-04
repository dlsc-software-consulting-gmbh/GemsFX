package com.dlsc.gemsfx;

import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A simple version of the paging list view that is completely based on a list of tiems, just like a normal
 * list view would be.
 *
 * @param <T> the type of items to show in the list view
 */
public class SimplePagingListView<T> extends PagingListView<T> {

    private boolean internal;

    /**
     * Constructs a new list view and sets a loader that uses the data list.
     */
    public SimplePagingListView() {
        setLoader(lv -> {
            int pageSize = getPageSize();
            int index = getPage() * pageSize;
            return getItems().subList(index, Math.min(index + pageSize, getItems().size() - 1));
        });

        loaderProperty().addListener(it -> {
            throw new UnsupportedOperationException("a custom loader can not be used for this list view");
        });

        totalItemCountProperty().addListener(it -> {
            if (!internal) {
                throw new UnsupportedOperationException("the total item count can not be explicitly changed for this list view");

            }
        });

        items.addListener((Observable it) -> {
            ObservableList list = getItems();
            if (list != null) {
                internal = true;
                try {
                    setTotalItemCount(list.size());
                    setPage(Math.min(getTotalItemCount() / getPageSize(), getPage()));
                } finally {
                    internal = false;
                }
            }
        });
    }

    private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public final ObservableList<T> getItems() {
        return items.get();
    }

    public final ListProperty<T> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList<T> items) {
        this.items.set(items);
    }
}
