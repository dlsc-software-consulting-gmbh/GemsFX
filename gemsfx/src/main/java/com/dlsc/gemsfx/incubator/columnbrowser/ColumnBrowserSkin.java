package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.Observable;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ColumnBrowserSkin<S> extends SkinBase<ColumnBrowser<S>> {

	private final HBox box;

	public ColumnBrowserSkin(ColumnBrowser<S> browser) {
		super(browser);

		box = new HBox();
		box.setPrefHeight(250);

		VBox vbox = new VBox(10, box, browser.getTableView());
		VBox.setVgrow(browser.getTableView(), Priority.ALWAYS);

		getChildren().add(vbox);

		buildLists();

		browser.getColumnValuesLists().addListener((Observable evt) -> buildLists());
	}

	private void buildLists() {
		box.getChildren().clear();

		for (ColumnValuesList<?, ?> list : getSkinnable().getColumnValuesLists()) {
			HBox.setHgrow(list, Priority.ALWAYS);
			box.getChildren().add(list);
		}
	}
}
