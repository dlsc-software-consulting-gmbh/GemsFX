package com.dlsc.gemsfx.gridtable;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.skins.GridTableViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

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
        setFocusTraversable(true);
        addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY && evt.isStillSincePress() && evt.getClickCount() == 1) {
                requestFocus();
            }
        });
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

    // loading status

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

    // commit load status delay

    private final LongProperty commitLoadStatusDelay = new SimpleLongProperty(this, "commitLoadStatusDelay", 400L);

    public final long getCommitLoadStatusDelay() {
        return commitLoadStatusDelay.get();
    }

    /**
     * The delay in milliseconds before the list view will display the progress indicator for long running
     * load operations.
     *
     * @see LoadingPane#commitDelayProperty()
     *
     * @return the commit delay for the nested loading pane
     */
    public final LongProperty commitLoadStatusDelayProperty() {
        return commitLoadStatusDelay;
    }

    public final void setCommitLoadStatusDelay(long commitLoadStatusDelay) {
        this.commitLoadStatusDelay.set(commitLoadStatusDelay);
    }

    /**
     * Triggers a rebuild of the view without reloading data.
     */
    public final void refresh() {
        getProperties().remove("refresh-items");
        getProperties().put("refresh-items", true);
    }
}
