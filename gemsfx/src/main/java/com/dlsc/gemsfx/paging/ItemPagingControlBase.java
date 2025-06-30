package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.LoadingPane;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.util.Callback;

import java.util.List;
import java.util.function.Consumer;

/**
 * The abstract superclass for {@link PagingListView} and {@link PagingGridTableView}. It manages the common attributes
 * of these paging controls, such as the loading service, the loader, the loading status, the load delay, the "fill rows"
 * attribute, etc ...
 *
 * @param <T> the type of the items shown in the control
 */
public abstract class ItemPagingControlBase<T> extends PagingControlBase {

    private final ObservableList<T> itemsOnCurrentPage = FXCollections.observableArrayList();

    private boolean processingService;

    /**
     * Constructs an instance of ItemPagingControlBase and initializes the paging and loading service behavior.
     * This constructor sets up listeners and bindings for various properties to control
     * the behavior of the pagination mechanism and the underlying loading service. Specifically:
     * <ul>
     *  <li>Automatically unbinds and resets the state of the previous loading service when a new one is set.</li>
     * <li>Binds the new loading service's properties such as page, page size, load delay, and loader
     *   to their corresponding properties in this control.</li>
     * <li>Updates the loading status based on the state of the new loading service.</li>
     * <li>Sets up listeners for reloading or refreshing the data whenever relevant
     *   properties, such as page or page size, are changed.</li>
     * <li>Adds validation for certain operations, ensuring that invalid configurations such
     *   as negative load delays or unsupported paging control locations throw exceptions
     *   with descriptive error messages.</li>
     * </ul>
     * Additionally, the constructor initializes the loading service with a default {@link LoadingService}, applies
     * delay validation, and specifies constraints for the paging controls' location.
     */
    protected ItemPagingControlBase() {
        loadingServiceProperty().addListener((obs, oldService, newService) ->  {
            if (oldService != null) {
                oldService.pageProperty().unbind();
                oldService.pageSizeProperty().unbind();
                oldService.loadDelayInMillisProperty().unbind();
                oldService.loaderProperty().unbind();
                oldService.setOnSucceeded(null);
                oldService.setOnRunning(null);
                oldService.setOnFailed(null);
            }

            if (newService != null) {
                processService(newService);

                newService.pageProperty().bind(pageProperty());
                newService.pageSizeProperty().bind(pageSizeProperty());
                newService.loadDelayInMillisProperty().bind(loadDelayInMillisProperty());
                newService.loaderProperty().bind(loaderProperty());

                // new service might have already run ... let's update the loading status based on its state
                Worker.State state = newService.getState();
                if (state == Worker.State.RUNNING) {
                    loadingStatus.set(LoadingPane.Status.LOADING);
                } else if (state == Worker.State.FAILED) {
                    loadingStatus.set(LoadingPane.Status.ERROR);
                } else {
                    loadingStatus.set(LoadingPane.Status.OK);
                }

                newService.setOnSucceeded(evt -> {
                    loadingStatus.set(LoadingPane.Status.OK);
                    processService(newService);
                });

                newService.setOnRunning(evt -> loadingStatus.set(LoadingPane.Status.LOADING));
                newService.setOnFailed(evt -> loadingStatus.set(LoadingPane.Status.ERROR));
            }
        });

        InvalidationListener loadListener = it -> {
            if (!processingService) {
                reload();
            }
        };

        pageProperty().addListener(loadListener);
        pageSizeProperty().addListener(loadListener);
        loaderProperty().addListener(loadListener);

        InvalidationListener refreshListener = (Observable it) -> refresh();

        getItemsOnCurrentPage().addListener(refreshListener);
        fillLastPageProperty().addListener(refreshListener);

        setLoadingService(new LoadingService<>());

        loadDelayInMillis.addListener(it -> {
            if (getLoadDelayInMillis() < 0) {
                throw new IllegalArgumentException("load delay must be >= 0");
            }
        });

        pagingControlsLocation.addListener((it, oldLocation, newLocation) -> {
            if (newLocation.equals(Side.LEFT) || newLocation.equals(Side.RIGHT)) {
                setPagingControlsLocation(Side.BOTTOM);
                throw new IllegalArgumentException("unsupported location for the paging controls: " + newLocation);
            }
        });
    }

    private void processService(LoadingService<T> service) {
        processingService = true;
        try {
            setPageSize(service.getPageSize());
            setPage(service.getPage());

            PagingLoadResponse<T> response = service.getValue();

            if (response != null) {
                // update the total item count
                setTotalItemCount(response.getTotalItemCount());

                List<T> newList = response.getItems();
                if (newList != null) {
                    itemsOnCurrentPage.setAll(newList);
                } else {
                    itemsOnCurrentPage.clear();
                }
            } else {
                itemsOnCurrentPage.clear();
            }
        } finally {
            processingService = false;
        }
    }

    private final ObjectProperty<LoadingPane.Status> loadingStatus = new SimpleObjectProperty<>(this, "loadingStatus", LoadingPane.Status.OK);

    public final LoadingPane.Status getLoadingStatus() {
        return loadingStatus.get();
    }

    /**
     * The loading status used for the wrapped {@link LoadingPane}. The loading pane will appear if the
     * loader takes a long time to return the new page items.
     *
     * @return the loading status
     */
    public final ObjectProperty<LoadingPane.Status> loadingStatusProperty() {
        return loadingStatus;
    }

    public final void setLoadingStatus(LoadingPane.Status loadingStatus) {
        this.loadingStatus.set(loadingStatus);
    }

    private final LongProperty loadDelayInMillis = new SimpleLongProperty(this, "loadDelayInMillis", 50L);

    public final long getLoadDelayInMillis() {
        return loadDelayInMillis.get();
    }

    /**
     * The delay in milliseconds before the loading service will actually try to retrieve the data from (for example)
     * a backend. This delay is around a few hundred milliseconds by default. Delaying the loading has the advantage
     * that sudden property changes will not trigger multiple backend queries but will get batched together in a single
     * reload operation.
     *
     * @return the delay before data will actually be loaded
     */
    public final LongProperty loadDelayInMillisProperty() {
        return loadDelayInMillis;
    }

    public final void setLoadDelayInMillis(long loadDelayInMillis) {
        this.loadDelayInMillis.set(loadDelayInMillis);
    }

    private final ObjectProperty<LoadingService<T>> loadingService = new SimpleObjectProperty<>(this, "loadingService");

    /**
     * Returns the service responsible for executing the actual loading of the data on a background
     * thread.
     *
     * @return the loading service
     */
    public final ObjectProperty<LoadingService<T>> loadingServiceProperty() {
        return loadingService;
    }

    public final void setLoadingService(LoadingService<T> loadingService) {
        this.loadingService.set(loadingService);
    }

    public final LoadingService<T> getLoadingService() {
        return loadingService.get();
    }

    private final LongProperty commitLoadStatusDelay = new SimpleLongProperty(this, "commitLoadStatusDelay", 400L);

    public final long getCommitLoadStatusDelay() {
        return commitLoadStatusDelay.get();
    }

    /**
     * The delay in milliseconds before the list view will display the progress indicator for long-running
     * load operations.
     *
     * @return the commit delay for the nested loading pane
     * @see LoadingPane#commitDelayProperty()
     */
    public final LongProperty commitLoadStatusDelayProperty() {
        return commitLoadStatusDelay;
    }

    public final void setCommitLoadStatusDelay(long commitLoadStatusDelay) {
        this.commitLoadStatusDelay.set(commitLoadStatusDelay);
    }

    private final ObjectProperty<Callback<PagingLoadRequest, PagingLoadResponse<T>>> loader = new SimpleObjectProperty<>(this, "loader");

    public final Callback<PagingLoadRequest, PagingLoadResponse<T>> getLoader() {
        return loader.get();
    }

    /**
     * Provides access to the loader property, which is a callback used to handle loading
     * of paginated data. The callback is responsible for processing {@link PagingLoadRequest}
     * and returning the corresponding {@link PagingLoadResponse}.
     *
     * @return the loader property
     */
    public final ObjectProperty<Callback<PagingLoadRequest, PagingLoadResponse<T>>> loaderProperty() {
        return loader;
    }

    public final void setLoader(Callback<PagingLoadRequest, PagingLoadResponse<T>> loader) {
        this.loader.set(loader);
    }

    private final BooleanProperty fillLastPage = new SimpleBooleanProperty(this, "fillLastPage", false);

    public final boolean isFillLastPage() {
        return fillLastPage.get();
    }

    /**
     * The control might not have enough data to fill its last page with items / cells. This flag can be used
     * to control whether we want the control to become smaller because of missing items or if we want the view to
     * fill the page with empty cells.
     *
     * @return a flag used to control whether the last page will be filled with empty cells if needed
     */
    public final BooleanProperty fillLastPageProperty() {
        return fillLastPage;
    }

    public final void setFillLastPage(boolean fillLastPage) {
        this.fillLastPage.set(fillLastPage);
    }

    /**
     * Returns an observable list with the items shown by the current page.
     *
     * @return the currently shown items
     */
    public final ObservableList<T> getItemsOnCurrentPage() {
        return itemsOnCurrentPage;
    }

    private final ObjectProperty<Side> pagingControlsLocation = new SimpleObjectProperty<>(this, "pagingControlsLocation", Side.BOTTOM);

    public final Side getPagingControlsLocation() {
        return pagingControlsLocation.get();
    }

    /**
     * Controls on which side the paging controls should be located. Currently only {@link Side#TOP} and
     * {@link Side#BOTTOM} are supported.
     *
     * @return the location where the paging controls will be shown
     */
    public final ObjectProperty<Side> pagingControlsLocationProperty() {
        return pagingControlsLocation;
    }

    public final void setPagingControlsLocation(Side pagingControlsLocation) {
        this.pagingControlsLocation.set(pagingControlsLocation);
    }

    private final BooleanProperty showPagingControls = new SimpleBooleanProperty(this, "showPagingControls", true);

    public final boolean isShowPagingControls() {
        return showPagingControls.get();
    }

    /**
     * A flag used to control the visibility of the paging controls (page buttons, previous, next, etc...).
     *
     * @return a property that is true if the paging controls should be visible
     */
    public final BooleanProperty showPagingControlsProperty() {
        return showPagingControls;
    }

    public final void setShowPagingControls(boolean showPagingControls) {
        this.showPagingControls.set(showPagingControls);
    }

    private ObjectProperty<Node> placeholder;

    /**
     * The {@code Node} to show to the user when the {@code PagingListView} has no content to show.
     * This happens when the list model has no data or when a filter has been applied to the list model, resulting in
     * there being nothing to show the user.
     */
    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<>(this, "placeholder", new Label("No items"));
        }
        return placeholder;
    }

    public final void setPlaceholder(Node value) {
        placeholderProperty().set(value);
    }

    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
    }

    private final ObjectProperty<Consumer<T>> onOpenItem = new SimpleObjectProperty<>(this, "onOpenItem");

    public final Consumer<T> getOnOpenItem() {
        return onOpenItem.get();
    }

    /**
     * A callback for opening an item represented by a row in the table view.
     *
     * @return a callback for opening table items
     */
    public final ObjectProperty<Consumer<T>> onOpenItemProperty() {
        return onOpenItem;
    }

    public final void setOnOpenItem(Consumer<T> onOpenItem) {
        this.onOpenItem.set(onOpenItem);
    }

    private final BooleanProperty usingScrollPane = new SimpleBooleanProperty(this, "usingScrollPane", false);

    public final boolean isUsingScrollPane() {
        return usingScrollPane.get();
    }

    public final BooleanProperty usingScrollPaneProperty() {
        return usingScrollPane;
    }

    public final void setUsingScrollPane(boolean usingScrollPane) {
        this.usingScrollPane.set(usingScrollPane);
    }

    // --- Selection Model
    private final ObjectProperty<MultipleSelectionModel<T>> selectionModel = new SimpleObjectProperty<>(this, "selectionModel");

    /**
     * Sets the {@link MultipleSelectionModel} to be used in the ListView.
     * Despite a ListView requiring a <b>Multiple</b>SelectionModel, it is possible
     * to configure it to only allow single selection (see
     * {@link MultipleSelectionModel#setSelectionMode(javafx.scene.control.SelectionMode)}
     * for more information).
     *
     * @param value the MultipleSelectionModel to be used in this ListView
     */
    public final void setSelectionModel(MultipleSelectionModel<T> value) {
        selectionModelProperty().set(value);
    }

    /**
     * Returns the currently installed selection model.
     *
     * @return the currently installed selection model
     */
    public final MultipleSelectionModel<T> getSelectionModel() {
        return selectionModel == null ? null : selectionModel.get();
    }

    /**
     * The SelectionModel provides the API through which it is possible
     * to select single or multiple items within a ListView, as well as inspect
     * which items have been selected by the user. Note that it has a generic
     * type that must match the type of the ListView itself.
     *
     * @return the selectionModel property
     */
    public final ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }

    /**
     * Triggers an explicit refresh of the control. Refreshing the view does not trigger data loading.
     */
    public abstract void refresh();

    /**
     * Triggers an explicit reload of the list view.
     */
    public final void reload() {
        getLoadingService().restart();
    }
}
