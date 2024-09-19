package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.PagingControls;
import com.dlsc.gemsfx.Spacer;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class PagingControlsSkin extends SkinBase<PagingControls> {

    private static final String PAGE_BUTTON = "page-button";

    private final IntegerProperty startPage = new SimpleIntegerProperty();

    private final HBox hBox = new HBox();
    private Button lastPageButton;
    private Button nextButton;
    private Button previousButton;
    private Button firstPageButton;
    private Label counterLabel;

    public PagingControlsSkin(PagingControls view) {
        super(view);

        createButtons();

        InvalidationListener buildViewListener = it -> updateView();

        view.pageProperty().addListener(buildViewListener);
        view.pageCountProperty().addListener(buildViewListener);
        view.maxPageIndicatorsCountProperty().addListener(buildViewListener);
        view.showMessageLabelProperty().addListener(buildViewListener);
        view.showMaxPageProperty().addListener(buildViewListener);
        startPage.addListener(buildViewListener);

        view.pageProperty().addListener((obs, oldPage, newPage) -> {
            int startPage = this.startPage.get();
            int totalPages = view.getPageCount();
            int maxPageIndicatorCount = view.getMaxPageIndicatorsCount();

            if (newPage.intValue() < startPage) {
                this.startPage.set(Math.min(newPage.intValue(), Math.max(0, startPage - maxPageIndicatorCount)));
            } else if (newPage.intValue() > startPage + maxPageIndicatorCount - 1) {
                this.startPage.set(Math.max(newPage.intValue(), startPage + maxPageIndicatorCount));
            }

            updateView();
        });

        updateView();

        hBox.getStyleClass().add("hbox");
        getChildren().add(hBox);
    }

    private void createButtons() {
        PagingControls view = getSkinnable();

        counterLabel = new Label();
        counterLabel.getStyleClass().add("counter-label");
        counterLabel.textProperty().bind(Bindings.createStringBinding(() -> view.getMessageLabelProvider().call(view), view.messageLabelProviderProperty(), view.totalItemCountProperty(), view.pageProperty(), view.pageSizeProperty(), view.pageCountProperty()));
        counterLabel.visibleProperty().bind(view.showMessageLabelProperty());
        counterLabel.managedProperty().bind(view.showMessageLabelProperty());

        firstPageButton = createFirstPageButton();
        firstPageButton.setGraphic(new FontIcon(MaterialDesign.MDI_PAGE_FIRST));
        firstPageButton.getStyleClass().addAll("nav-button", "first");
        firstPageButton.managedProperty().bind(firstPageButton.visibleProperty());
        firstPageButton.disableProperty().bind(startPage.greaterThan(0).not());
        firstPageButton.visibleProperty().bind(view.showGotoFirstPageButtonProperty());
        firstPageButton.setOnAction(evt -> {
            view.setPage(0);
            startPage.set(0);
        });

        previousButton = createPreviousPageButton();
        previousButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_LEFT));
        previousButton.getStyleClass().addAll("nav-button", "previous-button");
        previousButton.setOnAction(evt -> view.setPage(Math.max(0, view.getPage() - 1)));
        previousButton.setMinWidth(Region.USE_PREF_SIZE);
        previousButton.disableProperty().bind(view.pageProperty().greaterThan(0).not());

        nextButton = createNextPageButton();
        nextButton.setGraphic(new FontIcon(MaterialDesign.MDI_CHEVRON_RIGHT));
        nextButton.getStyleClass().addAll("nav-button", "next-button");
        nextButton.setOnAction(evt -> view.setPage(Math.min(view.getPageCount() - 1, view.getPage() + 1)));
        nextButton.setMinWidth(Region.USE_PREF_SIZE);
        nextButton.disableProperty().bind(view.pageProperty().lessThan(view.getPageCount() - 1).not());

        lastPageButton = createLastPageButton();
        lastPageButton.setGraphic(new FontIcon(MaterialDesign.MDI_PAGE_LAST));
        lastPageButton.getStyleClass().addAll("nav-button", "last");
        lastPageButton.managedProperty().bind(lastPageButton.visibleProperty());
        lastPageButton.disableProperty().bind(startPage.add(view.getMaxPageIndicatorsCount()).lessThan(view.getPageCount()).not());
        lastPageButton.visibleProperty().bind(view.showGotoLastPageButtonProperty());
        lastPageButton.setOnAction(evt -> view.setPage(view.getPageCount() - 1));
    }

    private void updateView() {
        PagingControls view = getSkinnable();

        hBox.getChildren().setAll(counterLabel, new Spacer(), firstPageButton, previousButton);

        int pageIndex;
        int startIndex = startPage.get();
        int endIndex = Math.min(view.getPageCount(), startIndex + view.getMaxPageIndicatorsCount());

        if (endIndex - startIndex < view.getMaxPageIndicatorsCount()) {
            startIndex = Math.max(0, endIndex - view.getMaxPageIndicatorsCount());
            startPage.set(startIndex);
        }

        for (pageIndex = startIndex; pageIndex < endIndex; pageIndex++) {
            Button pageButton = createPageButton(pageIndex);
            if (pageIndex == view.getPage()) {
                pageButton.getStyleClass().add("current");
            }
            hBox.getChildren().add(pageButton);
        }

        if (view.isShowMaxPage() && endIndex < view.getPageCount()) {
            // we need to show the "max page" button
            Button pageButton = createPageButton(view.getPageCount() - 1);
            hBox.getChildren().addAll(view.getMaxPageDividerNode(), pageButton);
        }


        hBox.getChildren().addAll(nextButton, lastPageButton);
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
