/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.*;

import java.util.Comparator;
import java.util.function.Predicate;

import static javafx.scene.control.SelectionMode.MULTIPLE;

public class ColumnValuesList<S, T> extends Control {

	private final ListView<T> listView;

	public ColumnValuesList() {
		SortedList<T> sortedList = new SortedList<>(itemsProperty());
		sortedList.comparatorProperty().bind(comparatorProperty());

		this.listView = createListView();
		this.listView.setItems(sortedList);
		this.listView.getSelectionModel().setSelectionMode(MULTIPLE);
	}

	public ColumnValuesList(String text) {
		this();
		setText(text);
	}

	protected ListView<T> createListView() {
		return new ListView<>();
	}

	@Override
	protected Skin<?> createDefaultSkin() {
		return new ColumnValuesListSkin<>(this);
	}

	private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>(this, "comparator", Comparator.comparing(Object::toString));

	public final Comparator<T> getComparator() {
		return comparator.get();
	}

	public final ObjectProperty<Comparator<T>> comparatorProperty() {
		return comparator;
	}

	public final void setComparator(Comparator<T> comparator) {
		this.comparator.set(comparator);
	}

	public final ListView<T> getListView() {
		return listView;
	}

	private final ListProperty<T> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

	public final ObservableList<T> getItems() {
		return items.get();
	}

	public final ListProperty<T> itemsProperty() {
		return items;
	}

	public final void setItems(ObservableList<T> items) {
		this.items.set(items);
	}

	private final StringProperty text = new SimpleStringProperty(this, "text", "Column");

	public final String getText() {
		return text.get();
	}

	public final StringProperty textProperty() {
		return text;
	}

	public final void setText(String text) {
		this.text.set(text);
	}

	private final ObjectProperty<Predicate<S>> predicate = new SimpleObjectProperty<>(this, "predicate", item -> true);

	public final Predicate<S> getPredicate() {
		return predicate.get();
	}

	public final ObjectProperty<Predicate<S>> predicateProperty() {
		return predicate;
	}

	public final void setPredicate(Predicate<S> predicate) {
		this.predicate.set(predicate);
	}
}
