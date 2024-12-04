package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.PagingListView;
import javafx.collections.MapChangeListener;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.util.Objects;

public class InnerListViewSkin<T> extends ListViewSkin<T> {

    private final VBox content = new VBox() {
        @Override
        public String getUserAgentStylesheet() {
            return Objects.requireNonNull(PagingListView.class.getResource("paging-list-view.css")).toExternalForm();
        }
    };

    private final PagingListView<T> pagingListView;
    private final LoadingPane loadingPane;

    public InnerListViewSkin(ListView<T> control, PagingListView<T> pagingListView) {
        super(control);

        this.pagingListView = pagingListView;

        content.getStyleClass().add("content");

        loadingPane = new LoadingPane(content);
        loadingPane.statusProperty().bind(pagingListView.loadingStatusProperty());

        pagingListView.getProperties().addListener((MapChangeListener<? super Object, ? super Object>) change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("refresh-items")) {
                    pagingListView.getProperties().remove("refresh-items");
                    refresh();
                }
            }
        });

        getChildren().setAll(loadingPane);
        refresh();
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        loadingPane.resizeRelocate(x, y, w, h);
    }

    public void refresh() {
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
        return loadingPane.minHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return loadingPane.prefHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return loadingPane.maxHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return loadingPane.minWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return loadingPane.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return loadingPane.maxWidth(height) + leftInset + rightInset;
    }
}
