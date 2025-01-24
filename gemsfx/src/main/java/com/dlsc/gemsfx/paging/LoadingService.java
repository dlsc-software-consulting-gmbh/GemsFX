package com.dlsc.gemsfx.paging;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.util.Callback;

import java.util.Collections;

/**
 * A service that performs the actual loading of items for a paging control such as the {@link PagingListView} or the
 * {@link PagingGridTableView}. The service delays the actual loading by a couple of milliseconds so that it can be
 * restarted (cancel, start) when the user changes the page index quickly. This avoids redundant updates.
 * The service binds all of its properties to the paging control that uses it.
 *
 * @see #pageProperty()
 * @see #pageSizeProperty()
 * @see #loaderProperty()
 * @see #loadDelayInMillisProperty()
 *
 * @param <T> the type of items to be shown by the UI
 */
public class LoadingService<T> extends Service<PagingLoadResponse<T>> {

    private final PagingLoadResponse<T> EMPTY_RESPONSE = new PagingLoadResponse<>(Collections.emptyList(), 0);

    @Override
    protected Task<PagingLoadResponse<T>> createTask() {
        return new Task<>() {

            final PagingLoadRequest loadRequest = new PagingLoadRequest(getPage(), getPageSize());

            @Override
            protected PagingLoadResponse<T> call() {
                try {
                    Thread.sleep(getLoadDelayInMillis());
                } catch (InterruptedException e) {
                    // do nothing
                }

                if (!isCancelled()) {
                    Callback<PagingLoadRequest, PagingLoadResponse<T>> loader = getLoader();
                    if (loader != null) {

                        /*
                         * Important to wrap in a list, otherwise we can get a concurrent modification
                         * exception when the result gets applied to the "items" list in the service
                         * event handler for "success". Not sure why this fixes that issue.
                         */
                        return loader.call(loadRequest);
                    }
                }

                return EMPTY_RESPONSE;
            }
        };
    }

    private final IntegerProperty page = new SimpleIntegerProperty(this, "page");

    public final int getPage() {
        return page.get();
    }

    /**
     * The index of the currently showing page.
     *
     * @return the number of the currently showing page
     */
    public final IntegerProperty pageProperty() {
        return page;
    }

    private final IntegerProperty pageSize = new SimpleIntegerProperty(this, "pageSize", 10);

    public final int getPageSize() {
        return pageSize.get();
    }

    /**
     * The number of items shown per page of the control that is being controlled
     * by the pagination control.
     *
     * @return the number of items per page
     */
    public final IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    private final LongProperty loadDelayInMillis = new SimpleLongProperty(this, "loadDelayInMillis", 200L);

    public final long getLoadDelayInMillis() {
        return loadDelayInMillis.get();
    }

    /**
     * The delay in milliseconds before the loading service will actually try to retrieve the data from (for example)
     * a backend. This delay is around a few hundred milliseconds by default. Delaying the loading has the advantage
     * that sudden property changes will not trigger multiple backend queries but will get batched together to a single
     * reload operation.
     *
     * @return the delay before data will actually be loaded
     */
    public final LongProperty loadDelayInMillisProperty() {
        return loadDelayInMillis;
    }

    private final ObjectProperty<Callback<PagingLoadRequest, PagingLoadResponse<T>>> loader = new SimpleObjectProperty<>(this, "loader");

    public final Callback<PagingLoadRequest, PagingLoadResponse<T>> getLoader() {
        return loader.get();
    }

    public final ObjectProperty<Callback<PagingLoadRequest, PagingLoadResponse<T>>> loaderProperty() {
        return loader;
    }
}