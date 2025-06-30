package com.dlsc.gemsfx.paging;

import com.dlsc.gemsfx.gridtable.GridTableColumn;
import com.dlsc.gemsfx.gridtable.GridTableView;
import com.dlsc.gemsfx.skins.PagingGridTableViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

import java.util.Objects;

/**
 * The {@code PagingGridTableView} class is a control that combines a grid table with paging capabilities.
 * This class facilitates the display of paginated data in a table format, supporting customizable columns,
 * adjustable page size, and options to fill the last page with empty rows.
 *
 * @param <T> the type of items displayed in the table view
 */
public class PagingGridTableView<T> extends ItemPagingControlBase<T> {

    private final GridTableView<T> gridTableView = new GridTableView<>();

    /**
     * Default constructor for the {@code PagingGridTableView} class. This initializes the control and binds
     * necessary properties to manage paging and display of items.
     * The constructor sets up the following behavior:
     * - Adds a specific style class named "paging-grid-table-view" to the control.
     * - Binds the columns of the internal {@code GridTableView} to the {@code columnsProperty} of this control.
     * - Sets the items of the internal {@code GridTableView} to the items provided by the current page.
     * - Configures the `minNumberOfRowsProperty` of the internal {@code GridTableView} to ensure the proper
     *   number of rows are displayed, including empty rows if the last page should be filled.
     * - Registers a listener on the `minNumberOfRowsProperty` to trigger a refresh when the property changes.
     * Internally, this class uses the following additional methods and properties:
     * - {@code columnsProperty()}: Provides access to the grid table's columns.
     * - {@code getItemsOnCurrentPage()}: Supplies the items to display on the current page.
     * - {@code fillLastPageProperty()}: Indicates whether the last page should be filled with empty rows if needed.
     * - {@code pageSizeProperty()}: Defines the number of items per page.
     * - {@code refresh()}: Rebuilds the view without reloading data.
     */
    public PagingGridTableView() {
        getStyleClass().add("paging-grid-table-view");

        gridTableView.columnsProperty().bind(columnsProperty());
        gridTableView.setItems(getItemsOnCurrentPage());
        gridTableView.minNumberOfRowsProperty().bind(Bindings.createIntegerBinding(() -> {
            if (isFillLastPage()) {
                return getPageSize();
            }
            return 0;
        }, fillLastPageProperty(), pageSizeProperty()));

        gridTableView.minNumberOfRowsProperty().addListener(it -> refresh());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingGridTableViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingGridTableView.class.getResource("paging-grid-table-view.css")).toExternalForm();
    }

    private final ListProperty<GridTableColumn<T, ?>> columns = new SimpleListProperty<>(this, "columns", FXCollections.observableArrayList());

    /**
     * Provides access to the list property containing the columns of the grid table.
     *
     * @return the list property of {@code GridTableColumn<T, ?>} objects representing the columns
     */
    public final ListProperty<GridTableColumn<T, ?>> columnsProperty() {
        return this.columns;
    }

    public final ObservableList<GridTableColumn<T, ?>> getColumns() {
        return this.columns.get();
    }

    public final void setColumns(ObservableList<GridTableColumn<T, ?>> columns) {
        this.columns.set(columns);
    }

    /**
     * Returns the wrapped table view.
     *
     * @return the table view
     */
    public final GridTableView<T> getGridTableView() {
        return gridTableView;
    }

    /**
     * Triggers a rebuild of the view without reloading data.
     */
    @Override
    public final void refresh() {
        gridTableView.refresh();
    }
}
