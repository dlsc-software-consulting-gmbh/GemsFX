package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.PagingGridTableView;
import com.dlsc.gemsfx.PagingListView;
import com.dlsc.gemsfx.gridtable.GridTableView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PagingGridTableViewSkin<T> extends SkinBase<PagingGridTableView<T>> {

    public static final String USING_SCROLL_PANE = "using-scroll-pane";

    private final VBox content = new VBox();

    private final PagingControls pagingControls = new PagingControls();

    private final StackPane stackPane = new StackPane();

    private final GridTableView<T> gridTableView;

    public PagingGridTableViewSkin(PagingGridTableView<T> pagingGridTableView) {
        super(pagingGridTableView);

        gridTableView = pagingGridTableView.getGridTableView();
        VBox.setVgrow(gridTableView, Priority.ALWAYS);

        pagingControls.pageProperty().bindBidirectional(pagingGridTableView.pageProperty());
        pagingControls.totalItemCountProperty().bindBidirectional(pagingGridTableView.totalItemCountProperty());
        pagingControls.pageSizeProperty().bindBidirectional(pagingGridTableView.pageSizeProperty());
        pagingControls.maxPageIndicatorsCountProperty().bindBidirectional(pagingGridTableView.maxPageIndicatorsCountProperty());
        pagingControls.messageLabelStrategyProperty().bindBidirectional(pagingGridTableView.messageLabelStrategyProperty());
        pagingControls.showPreviousNextPageButtonProperty().bindBidirectional(pagingGridTableView.showPreviousNextPageButtonProperty());
        pagingControls.alignmentProperty().bindBidirectional(pagingGridTableView.alignmentProperty());
        pagingControls.firstLastPageDisplayModeProperty().bindBidirectional(pagingGridTableView.firstLastPageDisplayModeProperty());
        pagingControls.firstPageDividerProperty().bindBidirectional(pagingGridTableView.firstPageDividerProperty());
        pagingControls.visibleProperty().bind(pagingControls.neededProperty().and(pagingGridTableView.showPagingControlsProperty()));
        pagingControls.managedProperty().bind(pagingControls.neededProperty().and(pagingGridTableView.showPagingControlsProperty()));

        content.getStyleClass().add("content");

        pagingGridTableView.usingScrollPaneProperty().addListener(it -> updateStyleClass());

        InvalidationListener updateViewListener = it -> updateView();
        pagingGridTableView.usingScrollPaneProperty().addListener(updateViewListener);
        pagingGridTableView.placeholderProperty().addListener(updateViewListener);
        pagingGridTableView.pagingControlsLocationProperty().addListener(updateViewListener);

        gridTableView.onOpenItemProperty().bind(pagingGridTableView.onOpenItemProperty());
        gridTableView.loadingStatusProperty().bindBidirectional(pagingGridTableView.loadingStatusProperty());
        gridTableView.commitLoadStatusDelayProperty().bindBidirectional(pagingGridTableView.commitLoadStatusDelayProperty());
        gridTableView.placeholderProperty().bind(pagingGridTableView.placeholderProperty());

        stackPane.getStyleClass().add("stack-pane");

        getChildren().setAll(stackPane);

        updateStyleClass();
        updateView();
    }

    private void updateStyleClass() {
        if (getSkinnable().isUsingScrollPane()) {
            if (!getSkinnable().getStyleClass().contains(USING_SCROLL_PANE)) {
                content.getStyleClass().add(USING_SCROLL_PANE);
            }
        } else {
            content.getStyleClass().remove(USING_SCROLL_PANE);
        }
    }

    private void updateView() {
        PagingGridTableView<T> tableView = getSkinnable();

        if (tableView.getPagingControlsLocation() == Side.BOTTOM) {
            if (tableView.isUsingScrollPane()) {
                content.getChildren().setAll(wrapInScrollPane(gridTableView), pagingControls);
            } else {
                content.getChildren().setAll(gridTableView, pagingControls);
            }
        } else {
            if (tableView.isUsingScrollPane()) {
                content.getChildren().setAll(pagingControls, wrapInScrollPane(gridTableView));
            } else {
                content.getChildren().setAll(pagingControls, gridTableView);
            }
        }

        stackPane.getChildren().setAll(content);
    }

    private Node wrapInScrollPane(Node node) {
        ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.prefHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.minHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.maxHeight(width) + topInset + bottomInset;
    }

    @Override
    protected double computeMinWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.minWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.prefWidth(height) + leftInset + rightInset;
    }

    @Override
    protected double computeMaxWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        return stackPane.maxWidth(height) + leftInset + rightInset;
    }
}
