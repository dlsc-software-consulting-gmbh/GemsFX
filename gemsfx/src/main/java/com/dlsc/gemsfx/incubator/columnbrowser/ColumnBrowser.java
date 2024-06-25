/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 * <p>
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.TableView;

import java.util.Objects;
import java.util.function.Predicate;

public class ColumnBrowser<S> extends Control {

    private final InvalidationListener filterListener = evt -> updatePredicate();

    private final WeakInvalidationListener weakFilterListener = new WeakInvalidationListener(filterListener);

    private final TableView<S> tableView;
    private final FilteredList<S> filteredItems;

    public ColumnBrowser() {
        getStylesheets().add(Objects.requireNonNull(ColumnBrowser.class.getResource("column-browser-view.css")).toExternalForm());

        filteredItems = new FilteredList<>(itemsProperty());
        filteredItems.predicateProperty().bind(predicateProperty());

        tableView = createTableView();
        tableView.setItems(filteredItems);

        getColumnValuesLists().addListener((ListChangeListener<ColumnValuesList<?, ?>>) c -> {
            while (c.next()) {
                for (ColumnValuesList<?, ?> list : c.getAddedSubList()) {
                    list.getListView().getSelectionModel().selectedIndexProperty().addListener(weakFilterListener);
                }
                for (ColumnValuesList<?, ?> list : c.getRemoved()) {
                    list.getListView().getSelectionModel().selectedIndexProperty().removeListener(weakFilterListener);
                }
            }
        });

        updatePredicate();

       items.addListener((Observable evt) -> updatePredicate());
    }

    protected TableView<S> createTableView() {
        return new TableView<>();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ColumnBrowserSkin<>(this);
    }

    public TableView<S> getTableView() {
        return tableView;
    }

    public final FilteredList<S> getFilteredItems() {
        return filteredItems;
    }

    private final ListProperty<S> items = new SimpleListProperty<>(FXCollections.observableArrayList());

    public final ObservableList<S> getItems() {
        return items.get();
    }

    public final ListProperty<S> itemsProperty() {
        return items;
    }

    public final void setItems(ObservableList<S> items) {
        this.items.set(items);
    }

    private final ObservableList<ColumnValuesList<S, ?>> columnValuesLists = FXCollections.observableArrayList();

    public final ObservableList<ColumnValuesList<S, ?>> getColumnValuesLists() {
        return columnValuesLists;
    }

    private ObjectProperty<Predicate<S>> predicate = new SimpleObjectProperty<>(this, "predicate");

    public Predicate<S> getPredicate() {
        return predicate.get();
    }

    public ObjectProperty<Predicate<S>> predicateProperty() {
        return predicate;
    }

    public void setPredicate(Predicate<S> predicate) {
        this.predicate.set(predicate);
    }

    private void updatePredicate() {
        Predicate<S> predicate = item -> true;
        for (ColumnValuesList<S, ?> columnValuesList : columnValuesLists) {
            predicate = predicate.and(columnValuesList.getPredicate());
        }
        setPredicate(predicate);
    }
}
