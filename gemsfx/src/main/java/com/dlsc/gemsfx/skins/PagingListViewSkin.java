package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.paging.PagingControls;
import com.dlsc.gemsfx.paging.PagingListView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SkinBase;
import javafx.scene.control.skin.ScrollPaneSkin;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PagingListViewSkin<T> extends SkinBase<PagingListView<T>> {

    public static final String USING_SCROLL_PANE = "using-scroll-pane";

    private final VBox content = new VBox();

    private final PagingControls pagingControls = new PagingControls();

    private final StackPane stackPane = new StackPane();

    private final ListView<T> innerListView;

    public PagingListViewSkin(PagingListView<T> pagingListView) {
        super(pagingListView);

        innerListView = pagingListView.getListView();
        VBox.setVgrow(innerListView, Priority.ALWAYS);

        pagingControls.pageProperty().bindBidirectional(pagingListView.pageProperty());
        pagingControls.totalItemCountProperty().bindBidirectional(pagingListView.totalItemCountProperty());
        pagingControls.pageSizeProperty().bindBidirectional(pagingListView.pageSizeProperty());
        pagingControls.maxPageIndicatorsCountProperty().bindBidirectional(pagingListView.maxPageIndicatorsCountProperty());
        pagingControls.messageLabelStrategyProperty().bindBidirectional(pagingListView.messageLabelStrategyProperty());;
        pagingControls.showPreviousNextPageButtonProperty().bindBidirectional(pagingListView.showPreviousNextPageButtonProperty());
        pagingControls.alignmentProperty().bindBidirectional(pagingListView.alignmentProperty());
        pagingControls.firstLastPageDisplayModeProperty().bindBidirectional(pagingListView.firstLastPageDisplayModeProperty());
        pagingControls.firstPageDividerProperty().bindBidirectional(pagingListView.firstPageDividerProperty());
        pagingControls.visibleProperty().bind(pagingControls.neededProperty().and(pagingListView.showPagingControlsProperty()));
        pagingControls.managedProperty().bind(pagingControls.neededProperty().and(pagingListView.showPagingControlsProperty()));

        content.getStyleClass().add("content");

        pagingListView.usingScrollPaneProperty().addListener(it -> updateStyleClass());

        pagingListView.placeholderProperty().addListener((obs, oldPlaceholder, newPlaceholder) -> bindPlaceholder(oldPlaceholder, newPlaceholder));
        bindPlaceholder(null, pagingListView.getPlaceholder());

        // when the underlying data list changes, then we have to recreate the binding for the placeholder
        pagingListView.getUnmodifiableItems().addListener((Observable it) -> bindPlaceholder(null, pagingListView.getPlaceholder()));

        InvalidationListener updateViewListener = it -> updateView();
        pagingListView.usingScrollPaneProperty().addListener(updateViewListener);
        pagingListView.placeholderProperty().addListener(updateViewListener);
        pagingListView.pagingControlsLocationProperty().addListener(updateViewListener);

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

    private void bindPlaceholder(Node oldPlaceholder, Node newPlaceholder) {
        PagingListView<T> listView = getSkinnable();

        if (oldPlaceholder != null) {
            oldPlaceholder.visibleProperty().unbind();
            oldPlaceholder.managedProperty().unbind();
        }

        if (newPlaceholder != null) {
            newPlaceholder.visibleProperty().bind(Bindings.createBooleanBinding(() -> listView.getUnmodifiableItems().isEmpty(), listView.getUnmodifiableItems()));
            newPlaceholder.managedProperty().bind(newPlaceholder.visibleProperty());
        }
    }

    private void updateView() {
        PagingListView<T> listView = getSkinnable();

        if (listView.getPagingControlsLocation() == Side.BOTTOM) {
            if (listView.isUsingScrollPane()) {
                content.getChildren().setAll(wrapInScrollPane(innerListView), pagingControls);
            } else {
                content.getChildren().setAll(innerListView, pagingControls);
            }
        } else {
            if (listView.isUsingScrollPane()) {
                content.getChildren().setAll(pagingControls, wrapInScrollPane(innerListView));
            } else {
                content.getChildren().setAll(pagingControls, innerListView);
            }
        }

        Node placeholder = listView.getPlaceholder();
        if (placeholder != null) {
            if (!placeholder.getStyleClass().contains("placeholder")) {
                placeholder.getStyleClass().add("placeholder");
            }
            stackPane.getChildren().setAll(content, placeholder);
        } else {
            stackPane.getChildren().setAll(content);
        }
    }

    private Node wrapInScrollPane(Node node) {
        ScrollPane scrollPane = new ScrollPane(node);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setSkin(new ScrollPaneSkin(scrollPane) {
            {
                listenToScrollBar(getVerticalScrollBar(), true);
                listenToScrollBar(getHorizontalScrollBar(), false);
            }
        });
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        return scrollPane;
    }

    private void listenToScrollBar(ScrollBar scrollBar, boolean vertical) {
        PagingListView<T> listView = getSkinnable();
        if (vertical) {
            scrollBar.visibleProperty().addListener((obs, oldVisible, newVisible) -> {
                listView.pseudoClassStateChanged(PseudoClass.getPseudoClass("vbar-showing"), newVisible);
            });
            listView.pseudoClassStateChanged(PseudoClass.getPseudoClass("vbar-showing"), scrollBar.isVisible());
        } else {
            scrollBar.visibleProperty().addListener((obs, oldVisible, newVisible) -> {
                listView.pseudoClassStateChanged(PseudoClass.getPseudoClass("hbar-showing"), newVisible);
            });
            listView.pseudoClassStateChanged(PseudoClass.getPseudoClass("hbar-showing"), scrollBar.isVisible());
        }
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
}
