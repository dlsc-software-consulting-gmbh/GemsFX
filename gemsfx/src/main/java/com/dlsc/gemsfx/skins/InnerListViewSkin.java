package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PagingListView;
import javafx.geometry.Orientation;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

public class InnerListViewSkin<T> extends ListViewSkin<T> {

    private final VBox content = new VBox() {
        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }
    };

    private final PagingListView<T> pagingListView;

    public InnerListViewSkin(ListView<T> control, PagingListView<T> pagingListView) {
        super(control);
        this.pagingListView = pagingListView;
        content.getStyleClass().add("content");
        getChildren().setAll(content);
        updateItems();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        content.resizeRelocate(x, y, w, h);
    }

    public void updateItems() {
        content.getChildren().clear();

        Callback<ListView<T>, ListCell<T>> cellFactory = pagingListView.getCellFactory();
        if (cellFactory != null) {

            for (int index = 0; index < pagingListView.getPageSize(); index++) {

                ListCell<T> cell = cellFactory.call(getSkinnable());
                cell.setMaxWidth(Double.MAX_VALUE);
                cell.updateListView(getSkinnable());
                if (index < pagingListView.getUnmodifiableItems().size()) {
                    cell.updateIndex(index);
                } else {
                    cell.updateIndex(-1);
                }
                cell.updateSelected(getSkinnable().getSelectionModel().isSelected(index));
                content.getChildren().add(cell);

                if (index == pagingListView.getPageSize() - 1) {
                    cell.getStyleClass().add("last");
                }
            }
        }
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.minHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.prefHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.maxHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.minWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return content.maxWidth(height) + leftInset + rightInset;
    }
}
