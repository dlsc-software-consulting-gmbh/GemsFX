package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.skins.InnerListViewSkin;
import com.dlsc.gemsfx.skins.PagingListViewSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.Cell;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.util.Objects;

/**
 * PagingListView is a custom control that extends the functionality of a standard ListView
 * to include paging capabilities. It is designed to handle large datasets efficiently by
 * splitting the data into manageable pages and allowing navigation between them.
 *
 * @param <T> the type of items to be displayed in the list view
 */
public class PagingListView<T> extends ItemPagingControlBase<T> {

    private final ObservableList<T> items = FXCollections.observableArrayList();

    private final ObservableList<T> unmodifiableItems = FXCollections.unmodifiableObservableList(items);

    private final ListView<T> listView = new ListView<>(items) {

        @Override
        protected Skin<?> createDefaultSkin() {
            return new InnerListViewSkin<>(this, PagingListView.this);
        }
    };

    private boolean processingService;

    /**
     * Constructs a new PagingListView instance. The PagingListView is a custom control
     * that provides paging functionality for a list view, allowing for efficient display
     * and navigation of large datasets across multiple pages.
     * This constructor initializes the PagingListView by performing the following steps:
     * - Adds a custom style class ("paging-list-view") to the control.
     * - Configures the internal ListView, including setting a custom style class ("inner-list-view"),
     *   enabling "multiple selection" mode, and establishing bindings for the cell factory and items
     *   displayed on the current page.
     * - Binds the selection model of the PagingListView to the internal ListView for consistent
     *   selection behavior.
     * - Sets a default cell factory to customize the rendering of items in the list.
     * - Listens to changes in the cell factory property and triggers a refresh when modifications occur.
     */
    public PagingListView() {
        getStyleClass().add("paging-list-view");

        focusedProperty().subscribe(focused -> {
            if (focused) {
                listView.requestFocus();
            }
        });

        listView.getStyleClass().addAll("inner-list-view");
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.cellFactoryProperty().bind(cellFactoryProperty());
        listView.setFixedCellSize(Region.USE_COMPUTED_SIZE);
        listView.setItems(getItemsOnCurrentPage());

        selectionModelProperty().bindBidirectional(listView.selectionModelProperty());

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

        InvalidationListener refreshListener = (Observable it) -> refresh();
        cellFactoryProperty().addListener(refreshListener);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingListViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingListView.class.getResource("paging-list-view.css")).toExternalForm();
    }

    /**
     * Returns the wrapped list view.
     *
     * @return the list view
     */
    public final ListView<T> getListView() {
        return listView;
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

    /**
     * Triggers a rebuild of the view without reloading data.
     */
    @Override
    public final void refresh() {
        getProperties().remove("refresh-items");
        getProperties().put("refresh-items", true);
    }
}
