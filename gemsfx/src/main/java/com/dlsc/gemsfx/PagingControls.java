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
import javafx.geometry.HPos;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.util.Objects;

/**
 * A control for navigating paged information, for example, a paged database table view.
 */
public class PagingControls extends Control {

    private static final String DEFAULT_STYLE_CLASS = "paging-controls";

    /**
     * Constructs a new instance.
     */
    public PagingControls() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> requestFocus());
        setMessageLabelProvider(view -> {
            if (getPageCount() == 0) {
                return "No items";
            }

            if (getPageCount() == 1) {
                return "Showing all items";
            }

            int startIndex = (view.getPage() * getPageSize()) + 1;
            int endIndex = startIndex + getPageSize() - 1;

            endIndex = Math.min(endIndex, getTotalItemCount());
            return "Showing items " + startIndex + " to " + endIndex + " of " + getTotalItemCount();
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

        Label firstPageDivider = new Label("...");
        firstPageDivider.getStyleClass().addAll("page-divider", "first-page-divider");
        setFirstPageDivider(firstPageDivider);

        Label lastPageDivider = new Label("...");
        lastPageDivider.getStyleClass().addAll("page-divider", "first-page-divider");
        setLastPageDivider(lastPageDivider);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new PagingControlsSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(PagingControls.class.getResource("paging-controls.css")).toExternalForm();
    }

    private final BooleanProperty showPreviousNextPageButton = new SimpleBooleanProperty(this, "showPreviousNextButton", true);

    public final boolean getShowPreviousNextPageButton() {
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

    private final ObjectProperty<MessageLabelStrategy> messageLabelStrategy = new SimpleObjectProperty<>(this, "messageLabelStrategy", MessageLabelStrategy.SHOW_WHEN_NEEDED);

    public final MessageLabelStrategy getMessageLabelStrategy() {
        return messageLabelStrategy.get();
    }

    /**
     * The message label strategy controls whether the message label will appear in
     * certain situations, for example, when there are no items currently in the view
     * that is being controlled by these pagination controls.
     *
     * @return the strategy used to show or hide the message label
     */
    public final ObjectProperty<MessageLabelStrategy> messageLabelStrategyProperty() {
        return messageLabelStrategy;
    }

    public final void setMessageLabelStrategy(MessageLabelStrategy messageLabelStrategy) {
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

    private final ObjectProperty<FirstLastPageDisplayMode> firstLastPageDisplayMode = new SimpleObjectProperty<>(this, "firstLastPageStrategy", FirstLastPageDisplayMode.SHOW_PAGE_BUTTONS);

    public final FirstLastPageDisplayMode getFirstLastPageDisplayMode() {
        return firstLastPageDisplayMode.get();
    }

    public final ObjectProperty<FirstLastPageDisplayMode> firstLastPageDisplayModeProperty() {
        return firstLastPageDisplayMode;
    }

    public final void setFirstLastPageDisplayMode(FirstLastPageDisplayMode firstLastPageDisplayMode) {
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
        setPage(Math.min(getPageCount() - 1, getPage() + 1));
    }

    /**
     * Decrements the page index.
     *
     * @see #pageProperty()
     */
    public void previousPage() {
        setPage(Math.max(0, getPage() - 1));
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

    private final ReadOnlyIntegerWrapper pageCount = new ReadOnlyIntegerWrapper(this, "pageCount");

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
}
