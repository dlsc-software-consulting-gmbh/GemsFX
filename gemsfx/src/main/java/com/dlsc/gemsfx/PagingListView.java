package com.dlsc.gemsfx;

import com.dlsc.gemsfx.LoadingPane.Status;
import com.dlsc.gemsfx.skins.InnerListViewSkin;
import com.dlsc.gemsfx.skins.PagingListViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PagingListView<T> extends PagingControlBase {

    private final LoadingService loadingService = new LoadingService();

    private final ObservableList<T> items = FXCollections.observableArrayList();

    private final ObservableList<T> unmodifiableItems = FXCollections.unmodifiableObservableList(items);

    private final ListView<T> listView = new ListView<>(items) {
        @Override
        protected Skin<?> createDefaultSkin() {
            return new InnerListViewSkin<>(this, PagingListView.this);
        }
    };

    private final InvalidationListener updateListener = (Observable it) -> refresh();

    private final WeakInvalidationListener weakUpdateListener = new WeakInvalidationListener(updateListener);

    public PagingListView() {
        getStyleClass().add("paging-list-view");

        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.cellFactoryProperty().bind(cellFactoryProperty());

        selectionModelProperty().bindBidirectional(listView.selectionModelProperty());

        loadingService.setOnSucceeded(evt -> {
            loadingStatus.set(Status.OK);
            List<T> newList = loadingService.getValue();
            if (newList != null) {
                items.setAll(newList);
            } else {
                items.clear();
            }
        });

        loadingService.setOnRunning(evt -> loadingStatus.set(Status.LOADING));
        loadingService.setOnFailed(evt -> loadingStatus.set(Status.ERROR));

        InvalidationListener loadListener = it -> loadingService.restart();

        pageProperty().addListener(loadListener);
        pageSizeProperty().addListener(loadListener);
        totalItemCountProperty().addListener(loadListener);
        loaderProperty().addListener(loadListener);

        setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null) {
                    setText(item.toString());
                } else {
                    setText("");
                }
            }
        });

        unmodifiableItems.addListener(weakUpdateListener);

        pageSizeProperty().addListener(weakUpdateListener);
        pageProperty().addListener(weakUpdateListener);
        cellFactoryProperty().addListener(weakUpdateListener);
        fillLastPageProperty().addListener(weakUpdateListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingListViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingListView.class.getResource("paging-list-view.css")).toExternalForm();
    }

    public final ListView<T> getListView() {
        return listView;
    }

    private final BooleanProperty fillLastPage = new SimpleBooleanProperty(this, "fillLastPage", false);

    public final boolean isFillLastPage() {
        return fillLastPage.get();
    }

    /**
     * The list view might not have enough data to fill its last page with items / cells. This flag can be used
     * to control whether we want the view to become smaller because of missing items or if we want the view to
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

    private final ObjectProperty<Status> loadingStatus = new SimpleObjectProperty<>(this, "loadingStatus", Status.OK);

    public final Status getLoadingStatus() {
        return loadingStatus.get();
    }

    public final ObjectProperty<Status> loadingStatusProperty() {
        return loadingStatus;
    }

    public final void setLoadingStatus(Status loadingStatus) {
        this.loadingStatus.set(loadingStatus);
    }

    private class LoadingService extends Service<List<T>> {

        @Override
        protected Task<List<T>> createTask() {
            return new Task<>() {
                @Override
                protected List<T> call() {
                    if (!isCancelled()) {
                        Callback<PagingListView<T>, List<T>> loader = PagingListView.this.loader.get();
                        if (loader == null) {
                            throw new IllegalArgumentException("data loader can not be null");
                        }
                        return loader.call(PagingListView.this);
                    }
                    return Collections.emptyList();
                }
            };
        }
    }

    public final void reload() {
        loadingService.restart();
    }

    public final ObservableList<T> getUnmodifiableItems() {
        return unmodifiableItems;
    }

    private final ObjectProperty<Callback<PagingListView<T>, List<T>>> loader = new SimpleObjectProperty<>(this, "loader");

    public Callback<PagingListView<T>, List<T>> getLoader() {
        return loader.get();
    }

    public ObjectProperty<Callback<PagingListView<T>, List<T>>> loaderProperty() {
        return loader;
    }

    public void setLoader(Callback<PagingListView<T>, List<T>> loader) {
        this.loader.set(loader);
    }

    /**
     * The {@code Node} to show to the user when the {@code ListView} has no content to show.
     * This happens when the list model has no data or when a filter has been applied to the list model, resulting in
     * there being nothing to show the user.
     *
     * @since JavaFX 8.0
     */
    private ObjectProperty<Node> placeholder;

    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<>(this, "placeholder");
        }
        return placeholder;
    }

    public final void setPlaceholder(Node value) {
        placeholderProperty().set(value);
    }

    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
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
     * to select single or multiple items within a ListView, as  well as inspect
     * which items have been selected by the user. Note that it has a generic
     * type that must match the type of the ListView itself.
     *
     * @return the selectionModel property
     */
    public final ObjectProperty<MultipleSelectionModel<T>> selectionModelProperty() {
        return selectionModel;
    }

    // --- Cell Factory
    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;

    /**
     * Sets a new cell factory to use in the ListView. This forces all old
     * {@link ListCell}'s to be thrown away, and new ListCell's created with
     * the new cell factory.
     *
     * @param value cell factory to use in this ListView
     */
    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> value) {
        cellFactoryProperty().set(value);
    }

    /**
     * Returns the current cell factory.
     *
     * @return the current cell factory
     */
    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    /**
     * <p>Setting a custom cell factory has the effect of deferring all cell
     * creation, allowing for total customization of the cell. Internally, the
     * ListView is responsible for reusing ListCells - all that is necessary
     * is for the custom cell factory to return from this function a ListCell
     * which might be usable for representing any item in the ListView.
     *
     * <p>Refer to the {@link Cell} class documentation for more detail.
     *
     * @return the cell factory property
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        return cellFactory;
    }

    public void refresh() {
        getProperties().remove("refresh-items");
        getProperties().put("refresh-items", true);
    }
}
