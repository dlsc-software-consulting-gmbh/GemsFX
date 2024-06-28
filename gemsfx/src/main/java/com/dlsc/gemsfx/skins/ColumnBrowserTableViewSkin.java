package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ColumnBrowserListView;
import com.dlsc.gemsfx.ColumnBrowserTableView;
import javafx.beans.Observable;
import javafx.geometry.Orientation;
import javafx.scene.control.SkinBase;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class ColumnBrowserTableViewSkin<S> extends SkinBase<ColumnBrowserTableView<S>> {

	private final HBox box;

	public ColumnBrowserTableViewSkin(ColumnBrowserTableView<S> browser) {
		super(browser);

		box = new HBox();
		box.getStyleClass().add("list-views-box");

		SplitPane splitPane = new SplitPane();
		splitPane.setOrientation(Orientation.VERTICAL);
		splitPane.setDividerPosition(0, browser.getDividerPosition());
		splitPane.getItems().setAll(box, browser.getTableView());

		browser.dividerPositionProperty().addListener((obs, oldValue, newValue) -> splitPane.setDividerPosition(0, browser.getDividerPosition()));
		getChildren().add(splitPane);

		buildLists();

		browser.getColumnValuesLists().addListener((Observable evt) -> buildLists());
	}

	private void buildLists() {
		box.getChildren().clear();

		for (ColumnBrowserListView<?, ?> list : getSkinnable().getColumnValuesLists()) {
			HBox.setHgrow(list, Priority.ALWAYS);
			box.getChildren().add(list);
		}
	}
}
