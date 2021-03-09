/**
 * Copyright (C) 2014, 2015 Dirk Lemmermann Software & Consulting (dlsc.com)
 *
 * This file is part of FlexGanttFX.
 */
package com.dlsc.gemsfx.incubator.columnbrowser;

import javafx.beans.Observable;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ColumnBrowserSkin<S> extends SkinBase<ColumnBrowser<S>> {
	private HBox box;

	public ColumnBrowserSkin(ColumnBrowser<S> browser) {
		super(browser);

		browser.setPrefHeight(250);

		box = new HBox();
		getChildren().add(box);

		buildLists();

		browser.getColumnValuesLists().addListener((Observable evt) -> buildLists());
	}

	private void buildLists() {
		box.getChildren().clear();

		for (ColumnValuesList<S, ?> list : getSkinnable().getColumnValuesLists()) {
			HBox.setHgrow(list, Priority.ALWAYS);
			box.getChildren().add(list);
		}
	}
}
