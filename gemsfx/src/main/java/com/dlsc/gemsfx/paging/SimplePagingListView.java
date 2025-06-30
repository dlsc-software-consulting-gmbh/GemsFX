package com.dlsc.gemsfx.paging;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
        setLoader(new SimpleLoader<>(this, itemsProperty()));
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
}
