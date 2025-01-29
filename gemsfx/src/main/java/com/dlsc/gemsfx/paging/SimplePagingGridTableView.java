package com.dlsc.gemsfx.paging;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

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
        setLoader(new SimpleLoader<>(this, itemsProperty()));
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
}
