/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.util.Callback;

import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;
import static javafx.scene.control.SelectionMode.MULTIPLE;

public class ColumnValuesList<S, T> extends Control {

	private TableColumn<S, T> column;

	private ListView<T> listView;

	private ColumnBrowser<S> browser;

	public ColumnValuesList(ColumnBrowser<S> browser, TableColumn<S, T> column) {
		requireNonNull(browser);
		requireNonNull(column);

		this.browser = browser;
		this.column = column;
		this.listView = new ListView<>();
		this.listView.getSelectionModel().setSelectionMode(MULTIPLE);
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ColumnValuesListSkin<>(this);
	}

	public final ColumnBrowser<S> getColumnBrowser() {
		return browser;
	}

	public final TableColumn<S, T> getColumn() {
		return column;
	}

	public final ListView<T> getListView() {
		return listView;
	}

	public final Predicate<S> getPredicate() {
		Predicate<S> predicate = new Predicate<S>() {
			@Override
			public boolean test(S item) {
				TableView<S> table = column.getTableView();
				Callback<CellDataFeatures<S, T>, ObservableValue<T>> valueFactory = column
						.getCellValueFactory();
				ObservableValue<T> value = valueFactory
						.call(new CellDataFeatures<S, T>(table, column, item));

				MultipleSelectionModel<T> selectionModel = listView
						.getSelectionModel();

				if (selectionModel.isEmpty()) {
					return true;
				}

				return selectionModel.getSelectedItems().contains(
						value.getValue());
			}
		};

		return predicate;
	}
}
