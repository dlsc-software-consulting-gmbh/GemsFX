package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.Spacer;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class PagingControlsSkin extends SkinBase<PagingControls> {

    private static final String PAGE_BUTTON = "page-button";

    private final IntegerProperty startPage = new SimpleIntegerProperty();

    private final HBox pageButtonsBox = new HBox();

    private Button lastPageButton;
    private Button nextButton;
    private Button previousButton;
    private Button firstPageButton;
    private Label messageLabel;

    public PagingControlsSkin(PagingControls view) {
        super(view);

        createButtons();

        pageButtonsBox.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
        pageButtonsBox.managedProperty().bind(view.pageCountProperty().greaterThan(1));

        InvalidationListener buildViewListener = it -> updateView();

        view.pageProperty().addListener(buildViewListener);
        view.pageCountProperty().addListener(buildViewListener);
        view.maxPageIndicatorsCountProperty().addListener(buildViewListener);
        view.firstLastPageDisplayModeProperty().addListener(buildViewListener);
        view.alignmentProperty().addListener(buildViewListener);
        view.firstPageDividerProperty().addListener(buildViewListener);
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

    private void createButtons() {
        PagingControls view = getSkinnable();

        messageLabel = new Label();
        messageLabel.getStyleClass().add("message-label");
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

        firstPageButton = createFirstPageButton();
        firstPageButton.setFocusTraversable(false);
        firstPageButton.setGraphic(new FontIcon(MaterialDesign.MDI_PAGE_FIRST));
        firstPageButton.getStyleClass().addAll("navigation-button", "first-page-button");
        firstPageButton.managedProperty().bind(firstPageButton.visibleProperty());
        firstPageButton.disableProperty().bind(startPage.greaterThan(0).not());
        firstPageButton.visibleProperty().bind(view.firstLastPageDisplayModeProperty().isEqualTo(PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS).and(view.pageCountProperty().greaterThan(1)));
        firstPageButton.setOnAction(evt -> {
            view.setPage(0);
            startPage.set(0);
        });

        previousButton = createPreviousPageButton();
        previousButton.setFocusTraversable(false);
        previousButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_LEFT));
        previousButton.getStyleClass().addAll("navigation-button", "previous-page-button");
        previousButton.setOnAction(evt -> view.setPage(Math.max(0, view.getPage() - 1)));
        previousButton.setMinWidth(Region.USE_PREF_SIZE);
        previousButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1).and(view.showPreviousNextPageButtonProperty()));
        previousButton.managedProperty().bind(view.showPreviousNextPageButtonProperty());
        previousButton.disableProperty().bind(view.pageProperty().greaterThan(0).not());

        nextButton = createNextPageButton();
        nextButton.setFocusTraversable(false);
        nextButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT));
        nextButton.getStyleClass().addAll("navigation-button", "next-page-button");
        nextButton.setOnAction(evt -> view.setPage(Math.min(view.getPageCount() - 1, view.getPage() + 1)));
        nextButton.setMinWidth(Region.USE_PREF_SIZE);
        nextButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1).and(view.showPreviousNextPageButtonProperty()));
        nextButton.managedProperty().bind(view.showPreviousNextPageButtonProperty());
        nextButton.disableProperty().bind(view.pageProperty().lessThan(view.pageCountProperty().subtract(1)).not());

        lastPageButton = createLastPageButton();
        lastPageButton.setFocusTraversable(false);
        lastPageButton.setGraphic(new FontIcon(MaterialDesign.MDI_PAGE_LAST));
        lastPageButton.getStyleClass().addAll("navigation-button", "last-page-button");
        lastPageButton.managedProperty().bind(lastPageButton.visibleProperty());
        lastPageButton.disableProperty().bind(startPage.add(view.getMaxPageIndicatorsCount()).lessThan(view.getPageCount()).not());
        lastPageButton.visibleProperty().bind(view.firstLastPageDisplayModeProperty().isEqualTo(PagingControls.FirstLastPageDisplayMode.SHOW_ARROW_BUTTONS).and(view.pageCountProperty().greaterThan(1)));
        lastPageButton.setOnAction(evt -> view.setPage(view.getPageCount() - 1));
    }

    private void updateView() {
        PagingControls view = getSkinnable();

        Pane pane;

        HPos alignment = view.getAlignment();

        if (alignment.equals(HPos.CENTER)) {
            pane = new VBox(pageButtonsBox, messageLabel);
            pane.getStyleClass().add("vertical");
        } else {
            pane = new HBox();
            if (alignment.equals(HPos.RIGHT)) {
                pane.getChildren().setAll(messageLabel, new Spacer(), pageButtonsBox);
            } else {
                pane.getChildren().setAll(pageButtonsBox, new Spacer(), messageLabel);
            }
            pane.getStyleClass().add("horizontal");
        }

        pane.getStyleClass().add("pane");

        getChildren().setAll(pane);

        pageButtonsBox.getStyleClass().add("page-buttons-container");
        pageButtonsBox.setMaxWidth(Region.USE_PREF_SIZE);
        pageButtonsBox.managedProperty().bind(pageButtonsBox.visibleProperty());

        pageButtonsBox.getChildren().setAll(firstPageButton, previousButton);

        int startIndex = startPage.get();
        int endIndex = Math.min(view.getPageCount(), startIndex + view.getMaxPageIndicatorsCount());

        if (endIndex - startIndex < view.getMaxPageIndicatorsCount()) {
            startIndex = Math.max(0, endIndex - view.getMaxPageIndicatorsCount());
        }

        addFirstPageButton(view, startIndex);
        addPageButtons(startIndex, endIndex, view);
        addLastPageButton(view, endIndex);

        pageButtonsBox.getChildren().addAll(nextButton, lastPageButton);

        // might have been updated above
        startPage.set(startIndex);
    }

    private void addPageButtons(int startIndex, int endIndex, PagingControls view) {
        int pageIndex;
        for (pageIndex = startIndex; pageIndex < endIndex; pageIndex++) {
            Button pageButton = createPageButton(pageIndex);
            pageButton.setFocusTraversable(false);
            pageButton.visibleProperty().bind(view.pageCountProperty().greaterThan(1));
            if (pageIndex == view.getPage()) {
                pageButton.getStyleClass().add("current");
            }
            pageButtonsBox.getChildren().add(pageButton);
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
            pageButtonsBox.getChildren().addAll(dividerNode, pageButton);
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
            pageButtonsBox.getChildren().addAll(pageButton, dividerNode);
        }
    }

    protected Button createFirstPageButton() {
        return new Button();
    }

    protected Button createLastPageButton() {
        return new Button();
    }

    protected Button createPreviousPageButton() {
        return new Button();
    }

    protected Button createNextPageButton() {
        return new Button();
    }

    protected Button createPageButton(int page) {
        Button pageButton = new Button(Integer.toString(page + 1));
        pageButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        pageButton.getStyleClass().add(PAGE_BUTTON);
        pageButton.setOnAction(evt -> getSkinnable().setPage(page));
        return pageButton;
    }
}
