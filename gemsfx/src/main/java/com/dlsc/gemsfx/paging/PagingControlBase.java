package com.dlsc.gemsfx.paging;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.text.MessageFormat;
import java.util.Objects;

public abstract class PagingControlBase extends Control {

    public PagingControlBase() {
        setMessageLabelProvider(view -> {
            if (getPageCount() == 0) {
                return "No items";
            }

            if (getPageCount() == 1) {
                int total = getTotalItemCount();
                if (total == 1) {
                    return "Showing the only item.";
                }

                return MessageFormat.format("Showing all {0} items.", getTotalItemCount());
            }

            int startIndex = (view.getPage() * getPageSize()) + 1;
            int endIndex = startIndex + getPageSize() - 1;

            endIndex = Math.min(endIndex, getTotalItemCount());
            return "Showing items " + startIndex + " to " + endIndex + " of " + getTotalItemCount() + ".";
        });

        pageCount.bind(Bindings.createIntegerBinding(() -> {
            int count = getTotalItemCount() / getPageSize();
            if (getTotalItemCount() % getPageSize() > 0) {
                count++;
            }
            return count;
        }, totalItemCountProperty(), pageSizeProperty()));

        pageCount.addListener(it -> {
            if (getPageCount() <= getPage()) {
                setPage(Math.max(0, getPageCount() - 1));
            }
        });

        Region firstPageDivider = new Region();
        firstPageDivider.getStyleClass().addAll("page-divider", "first-page-divider");
        setFirstPageDivider(firstPageDivider);

        Region lastPageDivider = new Region();
        lastPageDivider.getStyleClass().addAll("page-divider", "last-page-divider");
        setLastPageDivider(lastPageDivider);

        addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            // important to check, otherwise this might get called / executed more than once
            if (evt.isConsumed()) {
                return;
            }

            if (Objects.equals(evt.getCode(), KeyCode.RIGHT) || Objects.equals(evt.getCode(), KeyCode.PAGE_DOWN)) {
                evt.consume();
                nextPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.LEFT) || Objects.equals(evt.getCode(), KeyCode.PAGE_UP)) {
                evt.consume();
                previousPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.HOME)) {
                evt.consume();
                firstPage();
            } else if (Objects.equals(evt.getCode(), KeyCode.END)) {
                evt.consume();
                lastPage();
            }
        });
    }

    private final StringProperty pageSizeSelectorLabel = new SimpleStringProperty(this, "pageSizeSelectorLabel", "Results per page");

    public final String getPageSizeSelectorLabel() {
        return pageSizeSelectorLabel.get();
    }

    /**
     * The text / label shown in front of the page size selector control.
     *
     * @see #showPageSizeSelectorProperty()
     * @see #availablePageSizesProperty()
     *
     * @return the label property for the page size selector
     */
    public final StringProperty pageSizeSelectorLabelProperty() {
        return pageSizeSelectorLabel;
    }

    public final void setPageSizeSelectorLabel(String pageSizeSelectorLabel) {
        this.pageSizeSelectorLabel.set(pageSizeSelectorLabel);
    }

    private final ListProperty<Integer> availablePageSizes = new SimpleListProperty<>(this, "availablePageSizes", FXCollections.observableArrayList(5, 10, 15, 20, 30, 40, 50));

    public final ObservableList<Integer> getAvailablePageSizes() {
        return availablePageSizes.get();
    }

    /**
     * A list of available page sizes that will be shown by the page size selector.
     *
     * @see #showPageSizeSelectorProperty()
     * @see #pageSizeSelectorLabelProperty()
     *
     * @return a list of available page sizes
     */
    public final ListProperty<Integer> availablePageSizesProperty() {
        return availablePageSizes;
    }

    public final void setAvailablePageSizes(ObservableList<Integer> availablePageSizes) {
        this.availablePageSizes.set(availablePageSizes);
    }

    private final BooleanProperty showPageSizeSelector = new SimpleBooleanProperty(this, "showPageSizeSelector", true);

    public final boolean isShowPageSizeSelector() {
        return showPageSizeSelector.get();
    }

    /**
     * Determines if the control will show a selector UI for choosing different page sizes.
     *
     * @see #availablePageSizesProperty()
     * @see #pageSizeSelectorLabelProperty()
     *
     * @return a flag used to determine if the page size selector should be shown or not
     */
    public final BooleanProperty showPageSizeSelectorProperty() {
        return showPageSizeSelector;
    }

    public final void setShowPageSizeSelector(boolean showPageSizeSelector) {
        this.showPageSizeSelector.set(showPageSizeSelector);
    }

    private final BooleanProperty sameWidthPageButtons = new SimpleBooleanProperty(this, "sameWidthPageButtons", false);

    public final boolean isSameWidthPageButtons() {
        return sameWidthPageButtons.get();
    }

    /**
     * A flag used to signal whether the individual page buttons will all have the same width or if their
     * width can be different based on the page number that they are showing (e.g. the button for page "1" would
     * be substantially less wide than the button for page "999").
     *
     * @return a flag to control the width of the page buttons
     */
    public final BooleanProperty sameWidthPageButtonsProperty() {
        return sameWidthPageButtons;
    }

    public final void setSameWidthPageButtons(boolean sameWidthPageButtons) {
        this.sameWidthPageButtons.set(sameWidthPageButtons);
    }

    private final BooleanProperty showPreviousNextPageButton = new SimpleBooleanProperty(this, "showPreviousNextButton", true);

    public final boolean isShowPreviousNextPageButton() {
        return showPreviousNextPageButton.get();
    }

    /**
     * A flag used to determine whether the control will display arrow buttons to
     * go to the next or the previous page.
     *
     * @return a boolean property to control the visibility of the previous / next buttons
     */
    public final BooleanProperty showPreviousNextPageButtonProperty() {
        return showPreviousNextPageButton;
    }

    public final void setShowPreviousNextPageButton(boolean showPreviousNextPageButton) {
        this.showPreviousNextPageButton.set(showPreviousNextPageButton);
    }

    /**
     * A list of possible strategies for showing / hiding the message label.
     *
     * @see #messageLabelStrategyProperty()
     */
    public enum MessageLabelStrategy {

        /**
         * Always hide the message label.
         */
        HIDE,

        /**
         * Show the message label when needed, usually when the total item count is larger than zero.
         */
        SHOW_WHEN_NEEDED,

        /**
         * Always show the message label, even when there are no items in the controlled view.
         */
        ALWAYS_SHOW
    }

    private final ObjectProperty<PagingControls.MessageLabelStrategy> messageLabelStrategy = new SimpleObjectProperty<>(this, "messageLabelStrategy", MessageLabelStrategy.ALWAYS_SHOW);

    public final PagingControls.MessageLabelStrategy getMessageLabelStrategy() {
        return messageLabelStrategy.get();
    }

    /**
     * The message label strategy controls whether the message label will appear in
     * certain situations, for example, when there are no items currently in the view
     * that is being controlled by these pagination controls.
     *
     * @return the strategy used to show or hide the message label
     */
    public final ObjectProperty<PagingControls.MessageLabelStrategy> messageLabelStrategyProperty() {
        return messageLabelStrategy;
    }

    public final void setMessageLabelStrategy(PagingControls.MessageLabelStrategy messageLabelStrategy) {
        this.messageLabelStrategy.set(messageLabelStrategy);
    }

    /**
     * An enum listing the different ways the control will display or
     * not display controls to quickly go to the first or the last page.
     */
    public enum FirstLastPageDisplayMode {

        /**
         * Do not show controls for jumping to the first or last page.
         */
        HIDE,

        /**
         * Show separate controls in front and after the page buttons to
         * perform the jump.
         */
        SHOW_ARROW_BUTTONS,

        /**
         * Show extra page buttons to perform the jump (1 ... 5 6 7 8 ... 20).
         */
        SHOW_PAGE_BUTTONS
    }

    private final ObjectProperty<PagingControls.FirstLastPageDisplayMode> firstLastPageDisplayMode = new SimpleObjectProperty<>(this, "firstLastPageStrategy", PagingControls.FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS);

    public final PagingControls.FirstLastPageDisplayMode getFirstLastPageDisplayMode() {
        return firstLastPageDisplayMode.get();
    }

    public final ObjectProperty<PagingControls.FirstLastPageDisplayMode> firstLastPageDisplayModeProperty() {
        return firstLastPageDisplayMode;
    }

    public final void setFirstLastPageDisplayMode(PagingControls.FirstLastPageDisplayMode firstLastPageDisplayMode) {
        this.firstLastPageDisplayMode.set(firstLastPageDisplayMode);
    }

    private final ObjectProperty<Node> firstPageDivider = new SimpleObjectProperty<>(this, "firstPageDivider");

    public final Node getFirstPageDivider() {
        return firstPageDivider.get();
    }

    /**
     * Stores the node that will be placed between the regular page buttons and the page button
     * that represents the "first" page. This is usually a label showing "...".
     *
     * @return a node for separating the "first page" button, usually a label showing "..."
     */
    public final ObjectProperty<Node> firstPageDividerProperty() {
        return firstPageDivider;
    }

    public final void setFirstPageDivider(Node firstPageDivider) {
        this.firstPageDivider.set(firstPageDivider);
    }

    private final ObjectProperty<Node> lastPageDivider = new SimpleObjectProperty<>(this, "firstPageDivider");

    public final Node getLastPageDivider() {
        return lastPageDivider.get();
    }

    /**
     * Stores the node that will be placed between the regular page buttons and the page button
     * that represents the "last" page. This is usually a label showing "...".
     *
     * @return a node for separating the "last page" button, usually a label showing "..."
     */
    public final ObjectProperty<Node> lastPageDividerProperty() {
        return lastPageDivider;
    }

    public final void setLastPageDivider(Node lastPageDivider) {
        this.lastPageDivider.set(lastPageDivider);
    }

    private final ObjectProperty<Callback<PagingControls, String>> messageLabelProvider = new SimpleObjectProperty<>(this, "messageLabelProvider");

    public final Callback<PagingControls, String> getMessageLabelProvider() {
        return messageLabelProvider.get();
    }

    /**
     * A message label provider is used to customize the messages shown by the message label
     * of this control, for example, "Showing items 11 to 20 of a total of 1000 items".
     *
     * @return the message label provider
     */
    public final ObjectProperty<Callback<PagingControls, String>> messageLabelProviderProperty() {
        return messageLabelProvider;
    }

    public final void setMessageLabelProvider(Callback<PagingControls, String> messageLabelProvider) {
        this.messageLabelProvider.set(messageLabelProvider);
    }

    /**
     * Sets the page index to zero.
     */
    public void firstPage() {
        setPage(0);
    }

    /**
     * Sets the page index to the page count minus one.
     *
     * @see #pageProperty()
     * @see #pageCountProperty()
     */
    public void lastPage() {
        setPage(getPageCount() - 1);
    }

    /**
     * Increments the page index.
     *
     * @see #pageProperty()
     */
    public void nextPage() {
        setPage(Math.max(0, Math.min(getPageCount() - 1, getPage() + 1)));
    }

    /**
     * Decrements the page index.
     *
     * @see #pageProperty()
     */
    public void previousPage() {
        setPage(Math.min(getTotalItemCount() / getPageSize(), Math.max(0, getPage() - 1)));
    }

    private final IntegerProperty totalItemCount = new SimpleIntegerProperty(this, "totalItemCount");

    public final int getTotalItemCount() {
        return totalItemCount.get();
    }

    /**
     * The total number of items (rows) displayed by the control that utilizes
     * this pagination control for paging.
     *
     * @return the total number of items in the view
     */
    public final IntegerProperty totalItemCountProperty() {
        return totalItemCount;
    }

    public final void setTotalItemCount(int totalItemCount) {
        this.totalItemCount.set(totalItemCount);
    }

    final ReadOnlyIntegerWrapper pageCount = new ReadOnlyIntegerWrapper(this, "pageCount");

    public final int getPageCount() {
        return pageCount.get();
    }

    /**
     * A read-only property that stores the number of pages that are required for the given
     * number of items and the given page size.
     *
     * @return a read-only integer property storing the number of required pages
     * @see #totalItemCountProperty()
     * @see #pageSizeProperty()
     */
    public final ReadOnlyIntegerProperty pageCountProperty() {
        return pageCount.getReadOnlyProperty();
    }

    private final IntegerProperty maxPageIndicatorsCount = new SimpleIntegerProperty(this, "maxPageIndicatorsCount", 5);

    public final int getMaxPageIndicatorsCount() {
        return maxPageIndicatorsCount.get();
    }

    /**
     * The maximum number of page indicators / buttons that will be shown at any time
     * by this control.
     *
     * @return the number of page buttons shown by the control
     */
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

    /**
     * The index of the currently showing page.
     *
     * @return the number of the currently showing page
     */
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

    /**
     * The number of items shown per page of the control that is being controlled
     * by the pagination control.
     *
     * @return the number of items per page
     */
    public final IntegerProperty pageSizeProperty() {
        return pageSize;
    }

    public final void setPageSize(int pageSize) {
        this.pageSize.set(pageSize);
    }

    private final ObjectProperty<HPos> alignment = new SimpleObjectProperty<>(this, "alignment", HPos.RIGHT);

    public final HPos getAlignment() {
        return alignment.get();
    }

    /**
     * The alignment property controls where in the view the paging buttons will appear: left,
     * center, middle.
     *
     * @return the alignment / the position of the paging buttons
     */
    public final ObjectProperty<HPos> alignmentProperty() {
        return alignment;
    }

    public final void setAlignment(HPos alignment) {
        this.alignment.set(alignment);
    }

    private final StringProperty firstPageText = new SimpleStringProperty(this, "firstPageText", "First");

    public final String getFirstPageText() {
        return firstPageText.get();
    }

    /**
     * The text that will be shown by the "first page" button.
     *
     * @return the text of the "first page" button
     */
    public final StringProperty firstPageTextProperty() {
        return firstPageText;
    }

    public final void setFirstPageText(String firstPageText) {
        this.firstPageText.set(firstPageText);
    }

    private final StringProperty lastPageText = new SimpleStringProperty(this, "lastPageText", "Last");

    public final String getLastPageText() {
        return lastPageText.get();
    }

    /**
     * The text that will be shown by the "last page" button.
     *
     * @return the text of the "last page" button
     */
    public final StringProperty lastPageTextProperty() {
        return lastPageText;
    }

    public final void setLastPageText(String lastPageText) {
        this.lastPageText.set(lastPageText);
    }

    private final StringProperty previousPageText = new SimpleStringProperty(this, "previousPageText", "Previous");

    public final String getPreviousPageText() {
        return previousPageText.get();
    }

    /**
     * The text that will be shown by the "previous page" button.
     *
     * @return the text of the "previous page" button
     */
    public final StringProperty previousPageTextProperty() {
        return previousPageText;
    }

    public final void setPreviousPageText(String previousPageText) {
        this.previousPageText.set(previousPageText);
    }

    private final StringProperty nextPageText = new SimpleStringProperty(this, "nextPageText", "Next");

    public final String getNextPageText() {
        return nextPageText.get();
    }

    /**
     * The text that will be shown by the "next page" button.
     *
     * @return the text of the "next page" button
     */
    public final StringProperty nextPageTextProperty() {
        return nextPageText;
    }

    public final void setNextPageText(String nextPageText) {
        this.nextPageText.set(nextPageText);
    }

    /**
     * Triggers an explicit reload of the control.
     */
    public abstract void reload();
}
