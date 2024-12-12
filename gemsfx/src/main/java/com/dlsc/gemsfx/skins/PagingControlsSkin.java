package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.Spacer;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class PagingControlsSkin extends SkinBase<PagingControls> {

    private static final String PAGE_BUTTON = "page-button";

    private final IntegerProperty startPage = new SimpleIntegerProperty();

    private final HBox pageButtonsBox = new HBox();

    private Button lastPageButton;
    private Button nextButton;
    private Button previousButton;
    private Button firstPageButton;
    private Label messageLabel;
    private HBox pageSizeSelectorContainer;
    private GridPane pageButtonsGridPane;
    private int column;

    public PagingControlsSkin(PagingControls view) {
        super(view);

        createStaticElements();

        pageButtonsBox.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
        pageButtonsBox.managedProperty().bind(view.pageCountProperty().greaterThan(1));

        InvalidationListener buildViewListener = it -> updateView();

        view.pageProperty().addListener(buildViewListener);
        view.pageCountProperty().addListener(buildViewListener);
        view.pageSizeProperty().addListener(buildViewListener);
        view.showPageSizeSelectorProperty().addListener(buildViewListener);
        view.maxPageIndicatorsCountProperty().addListener(buildViewListener);
        view.firstLastPageDisplayModeProperty().addListener(buildViewListener);
        view.alignmentProperty().addListener(buildViewListener);
        view.firstPageDividerProperty().addListener(buildViewListener);
        view.sameWidthPageButtonsProperty().addListener(buildViewListener);

        startPage.addListener(buildViewListener);

        view.pageProperty().addListener((obs, oldPage, newPage) -> {
            int startPage = this.startPage.get();
            int maxPageIndicatorCount = view.getMaxPageIndicatorsCount();

            if (newPage.intValue() < startPage) {
                this.startPage.set(Math.min(newPage.intValue(), Math.max(0, startPage - maxPageIndicatorCount)));
            } else if (newPage.intValue() > startPage + maxPageIndicatorCount - 1) {
                this.startPage.set(Math.max(newPage.intValue(), startPage + maxPageIndicatorCount));
            }

            updateView();
        });

        updateView();
    }

    private void createStaticElements() {
        PagingControls view = getSkinnable();

        pageButtonsGridPane = new GridPane();
        pageButtonsGridPane.getStyleClass().add("grid-pane");
        pageButtonsBox.getChildren().add(pageButtonsGridPane);

        ChoiceBox<Integer> pageSizeSelector = new ChoiceBox<>();
        pageSizeSelector.getStyleClass().addAll("element", "page-size-choice-box");
        pageSizeSelector.setMinWidth(Region.USE_PREF_SIZE);
        pageSizeSelector.setItems(view.availablePageSizesProperty());
        pageSizeSelector.setValue(view.getPageSize());
        pageSizeSelector.valueProperty().addListener(it -> view.setPageSize(pageSizeSelector.getValue()));
        view.pageSizeProperty().addListener(it -> pageSizeSelector.setValue(view.getPageSize()));

        Label pageSizeSelectorLabel = new Label();
        pageSizeSelectorLabel.getStyleClass().add("page-size-label");
        pageSizeSelectorLabel.setMinWidth(Region.USE_PREF_SIZE);
        pageSizeSelectorLabel.textProperty().bind(view.pageSizeSelectorLabelProperty());
        pageSizeSelectorLabel.visibleProperty().bind(pageSizeSelectorLabel.textProperty().isNotEmpty());
        pageSizeSelectorLabel.managedProperty().bind(pageSizeSelectorLabel.textProperty().isNotEmpty());

        /*
         * We do not want to see the page size selector if the page sizes shown inside the selector are all bigger
         * than the total amount of items.
         */
        BooleanBinding moreItemsThanMinimumPageSize = Bindings.createBooleanBinding(() -> {
            int smallestAvailablePageSize = view.getAvailablePageSizes().stream()
                    .min(Integer::compareTo)
                    .orElse(1);
            int totalItemCount = view.getTotalItemCount();
            return totalItemCount > smallestAvailablePageSize;
        }, view.totalItemCountProperty(), view.availablePageSizesProperty());

        pageSizeSelectorContainer = new HBox(pageSizeSelectorLabel, pageSizeSelector);
        pageSizeSelectorContainer.getStyleClass().add("page-size-container");
        pageSizeSelectorContainer.visibleProperty().bind(view.showPageSizeSelectorProperty().and(view.totalItemCountProperty().greaterThan(0)).and(moreItemsThanMinimumPageSize));
        pageSizeSelectorContainer.managedProperty().bind(pageSizeSelectorContainer.visibleProperty());

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
        messageLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        messageLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getMessageLabelProvider().call(view), view.messageLabelProviderProperty(), view.totalItemCountProperty(), view.pageProperty(), view.pageSizeProperty(), view.pageCountProperty()));
        messageLabel.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            PagingControls.MessageLabelStrategy messageLabelStrategy = view.getMessageLabelStrategy();
            return switch (messageLabelStrategy) {
                case ALWAYS_SHOW -> true;
                case HIDE -> false;
                case SHOW_WHEN_NEEDED -> view.getTotalItemCount() > view.getPageSize();
            };
        }, view.messageLabelStrategyProperty(), view.totalItemCountProperty(), view.pageSizeProperty()));
        messageLabel.managedProperty().bind(messageLabel.visibleProperty());

        Region firstPageButtonRegion = new Region();
        firstPageButtonRegion.getStyleClass().add("icon");

        Region lastPageButtonRegion = new Region();
        lastPageButtonRegion.getStyleClass().add("icon");

        Region previousPageRegion = new Region();
        previousPageRegion.getStyleClass().add("icon");

        Region nextPageRegion = new Region();
        nextPageRegion.getStyleClass().add("icon");

        firstPageButton = new Button();
        firstPageButton.textProperty().bind(view.firstPageTextProperty());
        firstPageButton.setGraphic(wrapIcon(firstPageButtonRegion));
        firstPageButton.getStyleClass().addAll("element", "navigation-button", "first-page-button");
        firstPageButton.setMinWidth(Region.USE_PREF_SIZE);
        firstPageButton.managedProperty().bind(firstPageButton.visibleProperty());
        firstPageButton.disableProperty().bind(view.pageProperty().greaterThan(0).not());
        firstPageButton.visibleProperty().bind(view.firstLastPageDisplayModeProperty().isEqualTo(PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS).and(view.pageCountProperty().greaterThan(1)));
        firstPageButton.setOnMouseClicked(evt -> {
            view.setPage(0);
            startPage.set(0);
        });

        previousButton = new Button();
        previousButton.textProperty().bind(view.previousPageTextProperty());
        previousButton.setGraphic(wrapIcon(previousPageRegion));
        previousButton.getStyleClass().addAll("element", "navigation-button", "previous-page-button");
        previousButton.setOnMouseClicked(evt -> view.setPage(Math.max(0, view.getPage() - 1)));
        previousButton.setMinWidth(Region.USE_PREF_SIZE);
        previousButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1).and(view.showPreviousNextPageButtonProperty()));
        previousButton.managedProperty().bind(view.showPreviousNextPageButtonProperty());
        previousButton.disableProperty().bind(view.pageProperty().greaterThan(0).not());

        nextButton = new Button();
        nextButton.textProperty().bind(view.nextPageTextProperty());
        nextButton.setGraphic(wrapIcon(nextPageRegion));
        nextButton.getStyleClass().addAll("element", "navigation-button", "next-page-button");
        nextButton.setOnMouseClicked(evt -> view.setPage(Math.min(view.getPageCount() - 1, view.getPage() + 1)));
        nextButton.setMinWidth(Region.USE_PREF_SIZE);
        nextButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1).and(view.showPreviousNextPageButtonProperty()));
        nextButton.managedProperty().bind(view.showPreviousNextPageButtonProperty());
        nextButton.disableProperty().bind(view.pageProperty().lessThan(view.pageCountProperty().subtract(1)).not());

        lastPageButton = new Button();
        lastPageButton.textProperty().bind(view.lastPageTextProperty());
        lastPageButton.setGraphic(wrapIcon(lastPageButtonRegion));
        lastPageButton.setMinWidth(Region.USE_PREF_SIZE);
        lastPageButton.getStyleClass().addAll("element", "navigation-button", "last-page-button");
        lastPageButton.managedProperty().bind(lastPageButton.visibleProperty());
        lastPageButton.disableProperty().bind(view.pageProperty().add(view.getMaxPageIndicatorsCount()).lessThan(view.getPageCount()).not());
        lastPageButton.visibleProperty().bind(view.firstLastPageDisplayModeProperty().isEqualTo(PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS).and(view.pageCountProperty().greaterThan(1)));
        lastPageButton.setOnMouseClicked(evt -> view.setPage(view.getPageCount() - 1));

        view.visibleProperty()
                .bind(pageSizeSelectorContainer.visibleProperty()
                        .or(pageButtonsBox.visibleProperty())
                        .or(firstPageButton.visibleProperty())
                        .or(previousButton.visibleProperty())
                        .or(nextButton.visibleProperty())
                        .or(lastPageButton.visibleProperty())
                        .or(messageLabel.visibleProperty())
                );

        view.managedProperty().bind(view.visibleProperty());
    }

    private Node wrapIcon(Region region) {
        StackPane stackPane = new StackPane(region);
        stackPane.getStyleClass().add("icon-wrapper");
        return stackPane;
    }

    private void updateView() {
        PagingControls view = getSkinnable();

        Pane pane;

        HPos alignment = view.getAlignment();

        if (alignment.equals(HPos.CENTER)) {
            pane = new VBox(pageButtonsBox, messageLabel, pageSizeSelectorContainer);
            pane.getStyleClass().add("vertical");
        } else {
            pane = new HBox();
            if (alignment.equals(HPos.RIGHT)) {
                pane.getChildren().setAll(messageLabel, new Spacer(), pageSizeSelectorContainer, pageButtonsBox);
            } else {
                pane.getChildren().setAll(pageButtonsBox, pageSizeSelectorContainer, new Spacer(), messageLabel);
            }
            pane.getStyleClass().add("horizontal");
        }

        pane.getStyleClass().add("pane");

        getChildren().setAll(pane);

        pageButtonsBox.getStyleClass().add("buttons-container");
        pageButtonsBox.setMaxWidth(Region.USE_PREF_SIZE);
        pageButtonsBox.managedProperty().bind(pageButtonsBox.visibleProperty());

        pageButtonsBox.getChildren().setAll(firstPageButton, previousButton, pageButtonsGridPane);

        int startIndex = startPage.get();
        int endIndex = Math.min(view.getPageCount(), startIndex + view.getMaxPageIndicatorsCount());

        if (endIndex - startIndex < view.getMaxPageIndicatorsCount()) {
            startIndex = Math.max(0, endIndex - view.getMaxPageIndicatorsCount());
        }

        column = 0;
        pageButtonsGridPane.getChildren().clear();

        addFirstPageButton(view, startIndex);
        addPageButtons(startIndex, endIndex, view);
        addLastPageButton(view, endIndex);

        pageButtonsGridPane.getColumnConstraints().clear();

        double percentageWidth = 100d / (double) column;
        System.out.println("column: " + column + ", percentage width: " + percentageWidth);

        for (int i = 0; i < column; i++) {
            ColumnConstraints con = new ColumnConstraints();

            if (view.isSameWidthPageButtons()) {
                con.setPercentWidth(percentageWidth);
            } else {
                con.setMinWidth(Region.USE_PREF_SIZE);
                con.setPrefWidth(Region.USE_COMPUTED_SIZE);
                con.setMaxWidth(Region.USE_PREF_SIZE);
            }

            pageButtonsGridPane.getColumnConstraints().add(con);
        }

        pageButtonsBox.getChildren().addAll(nextButton, lastPageButton);

        // might have been updated above
        startPage.set(startIndex);
    }

    private void addPageButtons(int startIndex, int endIndex, PagingControls view) {
        int pageIndex;
        for (pageIndex = startIndex; pageIndex < endIndex; pageIndex++) {
            Button pageButton = createPageButton(pageIndex);
            pageButton.setFocusTraversable(false);
            if (pageIndex == view.getPage()) {
                pageButton.getStyleClass().add("current");
            }
            addToGridPane(pageButton);
        }
    }

    private void addLastPageButton(PagingControls view, int endIndex) {
        if (view.getFirstLastPageDisplayMode().equals(PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS) && endIndex < view.getPageCount()) {
            // we need to show the "max page" button
            Button pageButton = createPageButton(view.getPageCount() - 1);
            pageButton.setFocusTraversable(false);
            pageButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
            Node dividerNode = view.getLastPageDivider();
            dividerNode.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
            dividerNode.setFocusTraversable(false);
            addToGridPane(dividerNode);
            addToGridPane(pageButton);
        }
    }

    private void addFirstPageButton(PagingControls view, int startIndex) {
        if (view.getFirstLastPageDisplayMode().equals(PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS) && startIndex > 1) {
            // we need to show the "max page" button
            Button pageButton = createPageButton(0);
            pageButton.setFocusTraversable(false);
            pageButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
            Node dividerNode = view.getFirstPageDivider();
            dividerNode.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
            dividerNode.setFocusTraversable(false);
            addToGridPane(pageButton);
            addToGridPane(dividerNode);
        }
    }

    private void addToGridPane(Node node) {
        pageButtonsGridPane.add(node, column, 0);
        GridPane.setHgrow(node, Priority.ALWAYS);
        GridPane.setVgrow(node, Priority.ALWAYS);
        GridPane.setFillHeight(node, true);
        column++;
    }

    protected Button createPageButton(int page) {
        Button pageButton = new Button(Integer.toString(page + 1));
        pageButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pageButton.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        pageButton.getStyleClass().addAll("element", PAGE_BUTTON);
        pageButton.setOnAction(evt -> getSkinnable().setPage(page));
        return pageButton;
    }
}
