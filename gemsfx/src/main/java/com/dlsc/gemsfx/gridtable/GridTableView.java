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
}
