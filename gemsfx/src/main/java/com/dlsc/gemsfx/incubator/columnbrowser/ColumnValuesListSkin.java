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

public class ColumnValuesListSkin<S, T> extends SkinBase<ColumnValuesList<S, T>> {

    public ColumnValuesListSkin(ColumnValuesList<S, T> valuesList) {
		super(valuesList);

		ListView<T> listView = valuesList.getListView();

        Label label = new Label();
		label.getStyleClass().add("list-header");
		label.setOnMouseClicked(evt -> listView.getSelectionModel().clearSelection());

		label.setMinWidth(0);
		label.setMaxWidth(Double.MAX_VALUE);
		label.textProperty().bind(valuesList.textProperty());

		listView.setMinSize(0, 0);
		listView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		BorderPane borderPane = new BorderPane();
		borderPane.setTop(label);
		borderPane.setCenter(listView);

		getChildren().add(borderPane);
	}
}
