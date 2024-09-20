package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.skins.GridTableViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;

import java.util.Objects;

/**
 * A simple table view implementation based on GridPane.
 *
 * @param <S> item type
 */
public class GridTableView<S> extends Control {

    private static final String DEFAULT_STYLE_CLASS = "grid-table-view";

    public GridTableView() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new GridTableViewSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(GridTableView.class.getResource("grid-table-view.css")).toExternalForm();
    }

    // items

    private final ListProperty<S> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    public final ListProperty<S> itemsProperty() {
        return items;
    }

    public final ObservableList<S> getItems() {
        return items.get();
    }

    public final void setItems(ObservableList<S> items) {
        this.items.set(items);
    }

    // columns

    private final ListProperty<GridTableColumn<S, ?>> columns = new SimpleListProperty<>(this, "columns", FXCollections.observableArrayList());

    public final ListProperty<GridTableColumn<S, ?>> columnsProperty() {
        return this.columns;
    }

    public final ObservableList<GridTableColumn<S, ?>> getColumns() {
        return this.columns.get();
    }

    public final void setColumns(ObservableList<GridTableColumn<S, ?>> columns) {
        this.columns.set(columns);
    }

    // placeholder

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder", new Label("No items"));

    public final Node getPlaceholder() {
        return placeholder.get();
    }

    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final void setPlaceholder(Node placeholder) {
        this.placeholder.set(placeholder);
    }

    // min rows

    private final IntegerProperty minNumberOfRows = new SimpleIntegerProperty(this, "minNumberOfRows", 0);

    public final int getMinNumberOfRows() {
        return minNumberOfRows.get();
    }

    public final IntegerProperty minNumberOfRowsProperty() {
        return minNumberOfRows;
    }

    public final void setMinNumberOfRows(int minNumberOfRows) {
        this.minNumberOfRows.set(minNumberOfRows);
    }

    /**
     * Possible ways for the list view to calculate the total number of pages
     * available.
     */
    public enum PageCountCalculationStrategy {

        /**
         * The total number of pages is provided by an explicit call from outside the control
         * to the {@link #setPageCount(int)} method.
         */
        EXTERNAL,

        /**
         * The total number of pages shown by the list view is based on the number of items in
         * the data and the number of visible rows.
         */
        INTERNAL
    }

    private final ObjectProperty<PageCountCalculationStrategy> pageCountCalculationStrategy = new SimpleObjectProperty<>(this, "pageCountCalculationStrategy", PageCountCalculationStrategy.INTERNAL);

    public final PageCountCalculationStrategy getPageCountCalculationStrategy() {
        return pageCountCalculationStrategy.get();
    }

    public final ObjectProperty<PageCountCalculationStrategy> pageCountCalculationStrategyProperty() {
        return pageCountCalculationStrategy;
    }

    public final void setPageCountCalculationStrategy(PageCountCalculationStrategy pageCountCalculationStrategy) {
        this.pageCountCalculationStrategy.set(pageCountCalculationStrategy);
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

    private final BooleanProperty showItemCounter = new SimpleBooleanProperty(this, "showItemCounter", true);

    public boolean isShowItemCounter() {
        return showItemCounter.get();
    }

    public final BooleanProperty showItemCounterProperty() {
        return showItemCounter;
    }

    public final void setShowItemCounter(boolean showItemCounter) {
        this.showItemCounter.set(showItemCounter);
    }

    private final IntegerProperty pageCount = new SimpleIntegerProperty(this, "pageCount", 0);

    public final int getPageCount() {
        return pageCount.get();
    }

    public final IntegerProperty pageCountProperty() {
        return pageCount;
    }

    public final void setPageCount(int pageCount) {
        this.pageCount.set(pageCount);
    }

    private final IntegerProperty maxPageIndicatorCount = new SimpleIntegerProperty(this, "maxPageIndicatorCount", 5);

    public final int getMaxPageIndicatorCount() {
        return maxPageIndicatorCount.get();
    }

    public final IntegerProperty maxPageIndicatorCountProperty() {
        return maxPageIndicatorCount;
    }

    public void setMaxPageIndicatorCount(int maxPageIndicatorCount) {
        this.maxPageIndicatorCount.set(maxPageIndicatorCount);
    }

    private final IntegerProperty page = new SimpleIntegerProperty(this, "page", 0);

    public final int getPage() {
        return page.get();
    }

    public final IntegerProperty pageProperty() {
        return page;
    }

    public void setPage(int page) {
        this.page.set(page);
    }

    private final IntegerProperty visibleRowCount = new SimpleIntegerProperty(this, "visibleRowCount", 5);

    public final int getVisibleRowCount() {
        return visibleRowCount.get();
    }

    public final IntegerProperty visibleRowCountProperty() {
        return visibleRowCount;
    }

    public final void setVisibleRowCount(int visibleRowCount) {
        this.visibleRowCount.set(visibleRowCount);
    }
}
