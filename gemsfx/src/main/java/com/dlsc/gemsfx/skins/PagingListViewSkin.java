package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.LoadingPane;
import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.PagingListView;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.WeakInvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PagingListViewSkin<T> extends SkinBase<PagingListView<T>> {

    public static final String USING_SCROLL_PANE = "using-scroll-pane";

    private final VBox content = new VBox() {
        /*
         * Very important or the layout inside PaginationListView will not work due
         * to text wrapping inside AdvancedItemView.
         */
        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }
    };

    private final PagingControls pagingControls = new PagingControls();

    private final LoadingPane stackPane = new LoadingPane() {
        /*
         * Very important or the layout inside PaginationListView will not work due
         * to text wrapping inside AdvancedItemView.
         */
        @Override
        public Orientation getContentBias() {
            return Orientation.HORIZONTAL;
        }
    };


    private final ListView<T> innerListView;

    public PagingListViewSkin(PagingListView<T> pagingListView) {
        super(pagingListView);

        innerListView = pagingListView.getListView();

        pagingListView.selectionModelProperty().bindBidirectional(innerListView.selectionModelProperty());

        pagingControls.pageProperty().bindBidirectional(pagingListView.pageProperty());
        pagingControls.totalItemCountProperty().bindBidirectional(pagingListView.totalItemCountProperty());
        pagingControls.pageSizeProperty().bind(pagingListView.pageSizeProperty());
        pagingControls.maxPageIndicatorsCountProperty().bindBidirectional(pagingListView.maxPageIndicatorsCountProperty());
        pagingControls.messageLabelStrategyProperty().bind(pagingListView.messageLabelStrategyProperty());;
        pagingControls.showPreviousNextPageButtonProperty().bind(pagingListView.showPreviousNextPageButtonProperty());
        pagingControls.alignmentProperty().bind(pagingListView.alignmentProperty());
        pagingControls.firstLastPageDisplayModeProperty().bind(pagingListView.firstLastPageDisplayModeProperty());
        pagingControls.firstPageDividerProperty().bind(pagingListView.firstPageDividerProperty());
        pagingControls.maxPageIndicatorsCountProperty().bind(pagingListView.maxPageIndicatorsCountProperty());

        content.getStyleClass().add("content");

        pagingListView.usingScrollPaneProperty().addListener(it -> updateStyleClass());

        pagingListView.placeholderProperty().addListener((obs, oldPlaceholder, newPlaceholder) -> bindPlaceholder(oldPlaceholder, newPlaceholder));
        bindPlaceholder(null, pagingListView.getPlaceholder());

        // when the underlying data list changes, then we have to recreate the binding for the placeholder
        pagingListView.getUnmodifiableItems().addListener((Observable it) -> bindPlaceholder(null, pagingListView.getPlaceholder()));

        pagingListView.usingScrollPaneProperty().addListener(it -> updateView());
        pagingListView.placeholderProperty().addListener(it -> updateView());

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

        if (listView.isUsingScrollPane()) {
            content.getChildren().setAll(wrapInScrollPane(innerListView), pagingControls);
        } else {
            content.getChildren().setAll(innerListView, pagingControls);
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
        return scrollPane;
    }
}
