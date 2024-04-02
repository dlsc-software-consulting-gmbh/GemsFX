/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyListProperty;
import javafx.beans.property.ReadOnlyListWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;

import java.util.Objects;
import java.util.function.Predicate;

public class ColumnBrowser<S> extends Control {

    private final InvalidationListener filterListener = evt -> filter();

    public ColumnBrowser() {
        getStylesheets().add(Objects.requireNonNull(ColumnBrowser.class.getResource("column-browser-view.css")).toExternalForm());

        tableView.addListener(evt -> getTableView().setItems(getFilteredItems()));

        getColumnValuesLists().addListener((ListChangeListener<ColumnValuesList<S, ?>>) c -> {
            while (c.next()) {
                for (ColumnValuesList<S, ?> list : c.getAddedSubList()) {
                    list.getListView().getSelectionModel().selectedIndexProperty().addListener(filterListener);
                }
                for (ColumnValuesList<S, ?> list : c.getRemoved()) {
                    list.getListView().getSelectionModel().selectedIndexProperty().removeListener(filterListener);
                }
            }
        });

        filter();

        items.addListener((Observable evt) -> filter());
    }

    public ColumnBrowser(TableView<S> tableView) {
        this();

        setTableView(tableView);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColumnBrowserSkin<>(this);
    }

    private ObservableList<S> items = FXCollections.observableArrayList();

    public final ObservableList<S> getItems() {
        return items;
    }

    private ReadOnlyListWrapper<S> filteredItems = new ReadOnlyListWrapper<>(
            FXCollections.observableArrayList());

    public final ReadOnlyListProperty<S> getFilteredItems() {
        return filteredItems.getReadOnlyProperty();
    }

    private final ObjectProperty<TableView<S>> tableView = new SimpleObjectProperty<>(this, "tableView");

    public final ObjectProperty<TableView<S>> tableViewProperty() {
        return tableView;
    }

    public final TableView<S> getTableView() {
        return tableView.get();
    }

    public final void setTableView(TableView<S> table) {
        tableView.set(table);
    }

    private final ObservableList<ColumnValuesList<S, ?>> columnValuesLists = FXCollections.observableArrayList();

    public final ObservableList<ColumnValuesList<S, ?>> getColumnValuesLists() {
        return columnValuesLists;
    }

    private void filter() {
        Predicate<S> predicate = createPredicate();
        if (predicate == null) {
            filteredItems.setAll(items);
        } else {
            filteredItems.setAll(items.filtered(predicate));
        }
    }

    private Predicate<S> createPredicate() {
        Predicate<S> predicate = null;

        for (ColumnValuesList<S, ?> list : getColumnValuesLists()) {
            if (predicate == null) {
                predicate = list.getPredicate();
            } else {
                predicate = predicate.and(list.getPredicate());
            }
        }

        return predicate;
    }

    ColumnValuesList<S, ?> getParentValuesList(ColumnValuesList<S, ?> list) {
        int index = columnValuesLists.indexOf(list);
        if (index > 0) {
            return columnValuesLists.get(index - 1);
        }

        return null;
    }
}
