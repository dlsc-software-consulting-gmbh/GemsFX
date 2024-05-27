package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.util.HistoryManager;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.function.Consumer;

/**
 * Represents a custom popup control tailored to display and manage history items of type T.
 * This control integrates with a {@link HistoryManager} to provide a user interface for viewing,
 * selecting, and managing historical entries directly through a popup window.
 *
 * <p>The popup is highly customizable, supporting the addition of custom nodes to its top, bottom,
 * left, and right regions. It also allows setting a placeholder for situations where no history items
 * are available. The appearance and behavior of the history items can be customized via a cell factory.</p>
 *
 * <p>Key features include:</p>
 * <ul>
 * <li>Automatic binding to a {@link HistoryManager} for dynamic history item management.</li>
 * <li>Customizable regions (top, bottom, left, right) for additional UI components or decorations.</li>
 * <li>Configurable callbacks for item selection and confirmation actions, enhancing interactive capabilities.</li>
 * <li>Support for CSS styling to match the application's design requirements.</li>
 * </ul>
 *
 * @param <T> the type of the items managed in the history
 */
public class HistoryPopup<T> extends CustomPopupControl {

    public static final String DEFAULT_STYLE_CLASS = "history-popup";

    public HistoryPopup() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);

        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);

        setHistoryPlaceholder(new Label("No history items available."));
    }

    protected Skin<?> createDefaultSkin() {
        return new HistoryPopupSkin<>(this);
    }

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final Node getLeft() {
        return leftProperty().get();
    }

    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    public final Node getRight() {
        return rightProperty().get();
    }

    public final void setRight(Node right) {
        rightProperty().set(right);
    }

    private final ObjectProperty<Node> top = new SimpleObjectProperty<>(this, "top");

    public final ObjectProperty<Node> topProperty() {
        return top;
    }

    public final Node getTop() {
        return topProperty().get();
    }

    public final void setTop(Node top) {
        topProperty().set(top);
    }

    private final ObjectProperty<Node> bottom = new SimpleObjectProperty<>(this, "bottom");

    public final ObjectProperty<Node> bottomProperty() {
        return bottom;
    }

    public final Node getBottom() {
        return bottomProperty().get();
    }

    public final void setBottom(Node bottom) {
        bottomProperty().set(bottom);
    }

    private ObjectProperty<Node> historyPlaceholder = new SimpleObjectProperty<>(this, "historyPlaceholder");

    /**
     * Returns the property representing the history placeholder node.
     *
     * @return the property representing the history placeholder node
     */
    public final ObjectProperty<Node> historyPlaceholderProperty() {
        if (historyPlaceholder == null) {
            historyPlaceholder = new SimpleObjectProperty<>(this, "historyPlaceholder");
        }
        return historyPlaceholder;
    }

    public final Node getHistoryPlaceholder() {
        return historyPlaceholder == null ? null : historyPlaceholder.get();
    }

    public final void setHistoryPlaceholder(Node historyPlaceholder) {
        historyPlaceholderProperty().set(historyPlaceholder);
    }

    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> historyCellFactory;

    public final Callback<ListView<T>, ListCell<T>> getHistoryCellFactory() {
        return historyCellFactory == null ? null : historyCellFactory.get();
    }

    /**
     * The cell factory for the history popup list view.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> historyCellFactoryProperty() {
        if (historyCellFactory == null) {
            historyCellFactory = new SimpleObjectProperty<>(this, "historyCellFactory");
        }
        return historyCellFactory;
    }

    public final void setHistoryCellFactory(Callback<ListView<T>, ListCell<T>> historyCellFactory) {
        historyCellFactoryProperty().set(historyCellFactory);
    }

    private ObjectProperty<Consumer<T>> onHistoryItemConfirmed;

    public final Consumer<T> getOnHistoryItemConfirmed() {
        return onHistoryItemConfirmed == null ? null : onHistoryItemConfirmed.get();
    }

    /**
     * Returns the property representing the callback function to be executed
     * when a history item within the ListView is either clicked directly or selected via the ENTER key press.
     * This property enables setting a custom callback function that will be invoked with the text of the
     * clicked or selected history item as the argument.
     * <p>
     * This callback is specifically designed to handle actions that confirm the selection of a history item,
     * differentiating it from other interactions such as mere selection or highlighting within the list.
     * It is particularly useful for integrating user-initiated actions that imply a final decision on a list item.
     * </p>
     *
     * @return the property representing the onHistoryItemConfirmed callback function.
     */
    public final ObjectProperty<Consumer<T>> onHistoryItemConfirmedProperty() {
        if (onHistoryItemConfirmed == null) {
            onHistoryItemConfirmed = new SimpleObjectProperty<>(this, "onHistoryItemConfirmed");
        }
        return onHistoryItemConfirmed;
    }

    public final void setOnHistoryItemConfirmed(Consumer<T> onHistoryItemConfirmed) {
        onHistoryItemConfirmedProperty().set(onHistoryItemConfirmed);
    }

    private ObjectProperty<Consumer<T>> onHistoryItemSelected;

    public final Consumer<T> getOnHistoryItemSelected() {
        return onHistoryItemSelected == null ? null : onHistoryItemSelected.get();
    }

    /**
     * Returns the property representing the callback function to be executed
     * when a history item within the ListView is selected.
     * This property enables setting a custom callback function that will be invoked with the text of the
     * selected history item as the argument.
     *
     * @return the property representing the onSelectedHistoryItem callback function.
     */
    public final ObjectProperty<Consumer<T>> onHistoryItemSelectedProperty() {
        if (onHistoryItemSelected == null) {
            onHistoryItemSelected = new SimpleObjectProperty<>(this, "onHistoryItemSelected");
        }
        return onHistoryItemSelected;
    }

    public final void setOnHistoryItemSelected(Consumer<T> onHistoryItemSelected) {
        onHistoryItemSelectedProperty().set(onHistoryItemSelected);
    }

    private ObjectProperty<HistoryManager<T>> historyManager;

    public final HistoryManager<T> getHistoryManager() {
        return historyManager == null ? null : historyManager.get();
    }

    public final ObjectProperty<HistoryManager<T>> historyManagerProperty() {
        if (historyManager == null) {
            historyManager = new SimpleObjectProperty<>(this, "historyManager");
        }
        return historyManager;
    }

    public final void setHistoryManager(HistoryManager<T> historyManager) {
        historyManagerProperty().set(historyManager);
    }

}
