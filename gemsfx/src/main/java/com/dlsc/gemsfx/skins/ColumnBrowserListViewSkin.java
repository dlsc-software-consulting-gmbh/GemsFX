package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ColumnBrowserListView;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

public class ColumnBrowserListViewSkin<S, T> extends SkinBase<ColumnBrowserListView<S, T>> {

    public ColumnBrowserListViewSkin(ColumnBrowserListView<S, T> valuesList) {
		super(valuesList);

		ListView<T> listView = valuesList.getListView();
		listView.setMinSize(0, 0);
		listView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

		BorderPane pane = new BorderPane(listView);
		pane.topProperty().bind(valuesList.headerProperty());
		pane.getStyleClass().add("border-pane");

		getChildren().add(pane);
	}
}
