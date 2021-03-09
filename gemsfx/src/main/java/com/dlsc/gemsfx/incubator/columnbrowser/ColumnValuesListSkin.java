/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;

import java.util.*;
import java.util.function.Predicate;

public class ColumnValuesListSkin<S, T> extends
		SkinBase<ColumnValuesList<S, T>> {

	private Label label;

	public ColumnValuesListSkin(ColumnValuesList<S, T> valuesList) {
		super(valuesList);

		label = new Label();
		label.getStyleClass().add("list-header");
		ListView<T> listView = valuesList.getListView();
		label.setOnMouseClicked(evt -> listView.getSelectionModel()
				.clearSelection());

		label.setMinWidth(0);
		label.setMaxWidth(Double.MAX_VALUE);

		listView.setMinSize(0, 0);
		listView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(label);
		borderPane.setCenter(listView);

		TableColumn<S, T> column = valuesList.getColumn();
		column.textProperty().addListener(evt -> updateLabelText());

		TableView<S> tableView = column.getTableView();
		if (tableView != null) {
			attachToTable(tableView);
		}

		column.tableViewProperty().addListener(tableChangedListener);

		updateLabelText();
		updateListValues();

		getChildren().add(borderPane);
	}

	private ChangeListener<TableView<S>> tableChangedListener = new ChangeListener<TableView<S>>() {
		@Override
		public void changed(ObservableValue<? extends TableView<S>> observable,
				TableView<S> oldTable, TableView<S> newTable) {
			removeFromTable(oldTable);
			attachToTable(newTable);
		}
	};

	private InvalidationListener itemsChangedListener = new InvalidationListener() {

		@Override
		public void invalidated(Observable observable) {
			updateListValues();
		}
	};

	private void removeFromTable(TableView<S> tableView) {
		if (tableView != null) {
			tableView.getItems().removeListener(itemsChangedListener);
		}
	}

	private void attachToTable(TableView<S> tableView) {
		if (tableView != null) {
			tableView.getItems().addListener(itemsChangedListener);
		}
	}

	private void updateLabelText() {
		ColumnValuesList<S, T> browserList = getSkinnable();
		TableColumn<S, T> column = browserList.getColumn();
		label.setText(column.getText());
	}

	private List<T> sort(Set<T> items) {
		ColumnValuesList<S, T> browserList = getSkinnable();
		TableColumn<S, T> column = browserList.getColumn();
		Comparator<T> comparator = column.getComparator();

		List<T> l = new ArrayList<>(items);
		l.sort(comparator);
		return l;
	}

	private void updateListValues() {
		ColumnValuesList<S, T> valuesList = getSkinnable();
		TableColumn<S, T> column = valuesList.getColumn();
		TableView<S> table = column.getTableView();
		ColumnBrowser<S> columnBrowser = valuesList.getColumnBrowser();
		ObservableList<S> items = columnBrowser.getItems();

		ColumnValuesList<S, ?> parentValuesList = columnBrowser
				.getParentValuesList(valuesList);

		Set<T> newSet = new HashSet<>();
		for (S value : items) {

			if (!isIncludedByParentValueLists(columnBrowser, parentValuesList,
					value)) {
				continue;
			}

			Callback<CellDataFeatures<S, T>, ObservableValue<T>> valueFactory = column
					.getCellValueFactory();
			ObservableValue<T> result = valueFactory
					.call(new CellDataFeatures<S, T>(table, column, value));
			if (result != null && result.getValue() != null) {
				newSet.add(result.getValue());
			}
		}

		ListView<T> listView = valuesList.getListView();

		List<T> newList = sort(newSet);
		List<T> oldList = listView.getItems();

		Iterator<T> iter = oldList.iterator();
		while (iter.hasNext()) {
			T item = iter.next();
			if (!(newList.contains(item))) {
				iter.remove();
			}
		}

		for (T item : newList) {
			if (!(oldList.contains(item))) {
				oldList.add(item);
			}
		}

		// MultipleSelectionModel<T> selectionModel =
		// listView.getSelectionModel();
		// for (T item : selectionModel.getSelectedItems()) {
		// if (!oldList.contains(selectionModel)) {
		// selectionModel.select(obj);
		// }
		// }
	}

	private boolean isIncludedByParentValueLists(
			ColumnBrowser<S> columnBrowser,
			ColumnValuesList<S, ?> parentValuesList, S value) {

		while (parentValuesList != null) {

			/*
			 * Don't bother looking at this item, if the values list to the left
			 * already says it is not included in the result set.
			 */
			Predicate<S> predicate = parentValuesList.getPredicate();
			if (!predicate.test(value)) {
				return false;
			}

			parentValuesList = columnBrowser
					.getParentValuesList(parentValuesList);
		}

		return true;
	}
}
