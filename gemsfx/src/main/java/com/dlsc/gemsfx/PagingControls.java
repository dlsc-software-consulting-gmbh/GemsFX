package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.PagingControlsSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.Objects;

public class PagingControls extends Control {

    private static final String DEFAULT_STYLE_CLASS = "paging-view";
    private static final String PAGE_BUTTON = "page-button";

    private final IntegerProperty startPage = new SimpleIntegerProperty();

    public PagingControls() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setMessageLabelProvider(view -> {
            if (getPageCount() == 1) {
                return "Showing all items";
            }

            int startIndex = (view.getPage() * getPageSize()) + 1;
            int endIndex = startIndex + getPageSize() - 1;

            return "Showing items " +  startIndex + " to " + endIndex + " of " + getTotalItemCount();
        });

        addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (Objects.equals(evt.getCode(), KeyCode.RIGHT)) {
                nextPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.LEFT)) {
                previousPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.HOME)) {
                firstPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.END)) {
                lastPage();
            }
        });

        pageCount.bind(Bindings.createIntegerBinding(() -> {
            int count = getTotalItemCount() / getPageSize();
            if (getTotalItemCount() % getPageSize() > 0) {
                count++;
            }
            return count;
        }, totalItemCountProperty(), pageSizeProperty()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingControlsSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingControls.class.getResource("paging-view.css")).toExternalForm();
    }

    public enum MessageLabelStrategy {
        HIDE,
        SHOW_WHEN_NEEDED,
        ALWAYS_SHOW
    }

    private final ObjectProperty<MessageLabelStrategy> messageLabelStrategy = new SimpleObjectProperty<>(this, "messageLabelStrategy", MessageLabelStrategy.SHOW_WHEN_NEEDED);

    public final MessageLabelStrategy getMessageLabelStrategy() {
        return messageLabelStrategy.get();
    }

    public final ObjectProperty<MessageLabelStrategy> messageLabelStrategyProperty() {
        return messageLabelStrategy;
    }

    public final void setMessageLabelStrategy(MessageLabelStrategy messageLabelStrategy) {
        this.messageLabelStrategy.set(messageLabelStrategy);
    }

    private final BooleanProperty showMaxPage = new SimpleBooleanProperty(this, "showMaxButton");

    public final boolean isShowMaxPage() {
        return showMaxPage.get();
    }

    public final BooleanProperty showMaxPageProperty() {
        return showMaxPage;
    }

    public final void setShowMaxPage(boolean showMaxPage) {
        this.showMaxPage.set(showMaxPage);
    }

    private final ObjectProperty<Node> maxPageDividerNode = new SimpleObjectProperty<>(this, "maxPageDividerNode", new Label("..."));

    public final Node getMaxPageDividerNode() {
        return maxPageDividerNode.get();
    }

    public final ObjectProperty<Node> maxPageDividerNodeProperty() {
        return maxPageDividerNode;
    }

    public final void setMaxPageDividerNode(Node maxPageDividerNode) {
        this.maxPageDividerNode.set(maxPageDividerNode);
    }

    private final ObjectProperty<Callback<PagingControls, String>> messageLabelProvider = new SimpleObjectProperty<>(this, "messageLabelProvider");

    public final Callback<PagingControls, String> getMessageLabelProvider() {
        return messageLabelProvider.get();
    }

    public final ObjectProperty<Callback<PagingControls, String>> messageLabelProviderProperty() {
        return messageLabelProvider;
    }

    public final void setMessageLabelProvider(Callback<PagingControls, String> messageLabelProvider) {
        this.messageLabelProvider.set(messageLabelProvider);
    }

    public void firstPage() {
        setPage(0);
    }

    public void lastPage() {
        setPage(getPageCount() - 1);
    }

    public void nextPage() {
        setPage(Math.min(getPageCount() - 1, getPage() + 1));
    }

    public void previousPage() {
        setPage(Math.max(0, getPage() - 1));
    }

    private final IntegerProperty totalItemCount = new SimpleIntegerProperty(this, "totalItemCount");

    public final int getTotalItemCount() {
        return totalItemCount.get();
    }

    public final IntegerProperty totalItemCountProperty() {
        return totalItemCount;
    }

    public final void setTotalItemCount(int totalItemCount) {
        this.totalItemCount.set(totalItemCount);
    }

    private final ReadOnlyIntegerWrapper pageCount = new ReadOnlyIntegerWrapper(this, "pageCount", 0);

    public final int getPageCount() {
        return pageCount.get();
    }

    public final ReadOnlyIntegerProperty pageCountProperty() {
        return pageCount.getReadOnlyProperty();
    }

    private final IntegerProperty maxPageIndicatorsCount = new SimpleIntegerProperty(this, "maxPageIndicatorsCount", 5);

    public final int getMaxPageIndicatorsCount() {
        return maxPageIndicatorsCount.get();
    }

    public final IntegerProperty maxPageIndicatorsCountProperty() {
        return maxPageIndicatorsCount;
    }

    public final void setMaxPageIndicatorsCount(int maxPageIndicatorsCount) {
        this.maxPageIndicatorsCount.set(maxPageIndicatorsCount);
    }

    private final IntegerProperty page = new SimpleIntegerProperty(this, "page");

    public final int getPage() {
        return page.get();
    }

    public final IntegerProperty pageProperty() {
        return page;
    }

    public final void setPage(int page) {
        this.page.set(page);
    }

    private final IntegerProperty pageSize = new SimpleIntegerProperty(this, "pageSize", 10);

    public final int getPageSize() {
        return pageSize.get();
    }

    public final IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public final void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    public void refresh() {
        startPage.set(0);
    }

    private final BooleanProperty showGotoFirstPageButton = new SimpleBooleanProperty(this, "showGotoFirstPageButton", true);

    public final boolean isShowGotoFirstPageButton() {
        return showGotoFirstPageButton.get();
    }

    public final BooleanProperty showGotoFirstPageButtonProperty() {
        return showGotoFirstPageButton;
    }

    public final void setShowGotoFirstPageButton(boolean showGotoFirstPageButton) {
        this.showGotoFirstPageButton.set(showGotoFirstPageButton);
    }

    private final BooleanProperty showGotoLastPageButton = new SimpleBooleanProperty(this, "showGotoLastPageButton", true);

    public final boolean isShowGotoLastPageButton() {
        return showGotoLastPageButton.get();
    }

    public final BooleanProperty showGotoLastPageButtonProperty() {
        return showGotoLastPageButton;
    }

    public final void setShowGotoLastPageButton(boolean showGotoLastPageButton) {
        this.showGotoLastPageButton.set(showGotoLastPageButton);
    }
}