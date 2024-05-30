package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.HistoryManager;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.*;
import java.util.function.Consumer;

/**
 * A custom button that manages and displays a history of entries based on a generic type {@code T}.
 * This button integrates directly with a {@link HistoryManager} to present a list of previously used
 * items to the user. The user can then pick one of those items to set a value on an input field.
 *
 * <p>Usage scenarios include search fields, form inputs, or any other component where users might benefit
 * from being able to see and interact with their previous entries. The generic type {@code T} allows for
 * flexibility, making it suitable for various data types that can represent user input history.</p>
 *
 * @param <T> the type of the objects that this button manages in its history
 */
public class HistoryButton<T> extends Button {

    private static final String DEFAULT_STYLE_CLASS = "history-button";

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");
    private static final PseudoClass POPUP_SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("popup-showing");

    private HistoryPopup popup;

    /**
     * Creates a new instance of the history button.
     */
    public HistoryButton() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);

        setGraphic(new FontIcon(MaterialDesign.MDI_HISTORY));
        setOnAction(evt -> showPopup());
        setCellFactory(view -> new RemovableListCell<>((listView, item) -> {
            HistoryManager<T> historyManager = getHistoryManager();
            if (historyManager != null) {
                historyManager.remove(item);
            }
        }));
    }

    /**
     * Creates a new instance of the history button where the button will be used in combination with
     * the given node.
     *
     * @param owner the owner of the button, used for focus management.
     */
    public HistoryButton(Node owner) {
        this();
        setOwner(owner);
    }

    /**
     * Shows the popup that includes the list view with the items stored by the history manager.
     */
    public void showPopup() {
        Node owner = getOwner();

        if (owner != null && owner != this && !owner.isFocused()) {
            owner.requestFocus();
        }

        if (getHistoryManager() == null) {
            return;
        }

        if (popup == null) {
            popup = new HistoryPopup();
            popupShowing.bind(popup.showingProperty());
        }

        if (popup.isShowing()) {
            hidePopup();
        } else {
            popup.show(this);
        }
    }

    /**
     * Hides the popup that is showing the history items.
     */
    public void hidePopup() {
        if (popup != null) {
            popup.hide();
        }
    }

    // placeholder

    private final ObjectProperty<Node> placeholder = new SimpleObjectProperty<>(this, "placeholder");

    /**
     * Returns the property representing the history placeholder node.
     *
     * @return the property representing the history placeholder node
     */
    public final ObjectProperty<Node> placeholderProperty() {
        return placeholder;
    }

    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
    }

    public final void setPlaceholder(Node placeholder) {
        placeholderProperty().set(placeholder);
    }

    private ObjectProperty<Consumer<T>> onItemSelected;

    public final Consumer<T> getOnItemSelected() {
        return onItemSelected == null ? null : onItemSelected.get();
    }

    /**
     * Returns the property representing the callback function to be executed when a history item within the list view
     * is either clicked directly or selected via the ENTER key press. This property enables setting a custom callback
     * function that will be invoked with the text of the clicked or selected history item as the argument.
     *
     * @return the property storing the "on history item confirmed" callback function.
     */
    public final ObjectProperty<Consumer<T>> onItemSelectedProperty() {
        if (onItemSelected == null) {
            onItemSelected = new SimpleObjectProperty<>(this, "onItemSelectedProperty");
        }
        return onItemSelected;
    }

    public final void setOnItemSelected(Consumer<T> onItemSelected) {
        onItemSelectedProperty().set(onItemSelected);
    }

    // popup showing

    private final ReadOnlyBooleanWrapper popupShowing = new ReadOnlyBooleanWrapper(this, "popupShowing") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(POPUP_SHOWING_PSEUDO_CLASS, get());
        }
    };

    public final boolean isPopupShowing() {
        return popupShowing.get();
    }

    // history manager

    private ObjectProperty<HistoryManager<T>> historyManager;

    /**
     * The history manager that is used for persisting the history of the button.
     * <p>
     * If its value is null, clicking the button will not display the history popup.
     * <p>
     * If its value is not null, clicking the button will display a popup showing the items previously stored
     * for this button.
     *
     * @return the property representing the history manager
     */
    public final ObjectProperty<HistoryManager<T>> historyManagerProperty() {
        if (historyManager == null) {
            historyManager = new SimpleObjectProperty<>(this, "historyManager") {
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DISABLED_POPUP_PSEUDO_CLASS, get() == null);
                }
            };
        }
        return historyManager;
    }

    public final void setHistoryManager(HistoryManager<T> historyManager) {
        historyManagerProperty().set(historyManager);
    }

    public final HistoryManager<T> getHistoryManager() {
        return historyManager == null ? null : historyManager.get();
    }

    // owner

    private ObjectProperty<Node> owner;

    /**
     * The (optional) "owner" of a history button can be a textfield where the button is shown inside the field.
     *
     * @return the owning node, e.g. a text field.
     */
    public final ObjectProperty<Node> ownerProperty() {
        if (owner == null) {
            owner = new SimpleObjectProperty<>(this, "owner");
        }
        return owner;
    }

    public final Node getOwner() {
        return owner == null ? null : owner.get();
    }

    public final void setOwner(Node owner) {
        ownerProperty().set(owner);
    }

    // decoration left

    private final ObjectProperty<Node> listDecorationLeft = new SimpleObjectProperty<>(this, "listDecorationLeft");

    public final Node getListDecorationLeft() {
        return listDecorationLeft.get();
    }

    /**
     * The list used by the popup to show previously used items can be easily decorated by specifying nodes for its
     * left, right, top, and / or bottom sides. This property stores an optional node for the left side.
     *
     * @return the node shown to the left of the list view
     */
    public final ObjectProperty<Node> listDecorationLeftProperty() {
        return listDecorationLeft;
    }

    public final void setListDecorationLeft(Node listDecorationLeft) {
        this.listDecorationLeft.set(listDecorationLeft);
    }

    // decoration right

    private final ObjectProperty<Node> listDecorationRight = new SimpleObjectProperty<>(this, "listDecorationRight");

    public final Node getListDecorationRight() {
        return listDecorationRight.get();
    }

    /**
     * The list used by the popup to show previously used items can be easily decorated by specifying nodes for its
     * left, right, top, and / or bottom sides. This property stores an optional node for the right side.
     *
     * @return the node shown to the right of the list view
     */
    public final ObjectProperty<Node> listDecorationRightProperty() {
        return listDecorationRight;
    }

    public final void setListDecorationRight(Node listDecorationRight) {
        this.listDecorationRight.set(listDecorationRight);
    }

    // decoration top

    private final ObjectProperty<Node> listDecorationTop = new SimpleObjectProperty<>(this, "listDecorationTop");

    public final Node getListDecorationTop() {
        return listDecorationTop.get();
    }

    /**
     * The list used by the popup to show previously used items can be easily decorated by specifying nodes for its
     * left, right, top, and / or bottom sides. This property stores an optional node for the top side.
     *
     * @return the node shown at the top of the list view
     */
    public final ObjectProperty<Node> listDecorationTopProperty() {
        return listDecorationTop;
    }

    public final void setListDecorationTop(Node listDecorationTop) {
        this.listDecorationTop.set(listDecorationTop);
    }

    // decoration bottom

    private final ObjectProperty<Node> listDecorationBottom = new SimpleObjectProperty<>(this, "listDecorationBottom");

    public final Node getListDecorationBottom() {
        return listDecorationBottom.get();
    }

    /**
     * The list used by the popup to show previously used items can be easily decorated by specifying nodes for its
     * left, right, top, and / or bottom sides. This property stores an optional node for the bottom side.
     *
     * @return the node shown on the bottom of the list view
     */
    public final ObjectProperty<Node> listDecorationBottomProperty() {
        return listDecorationBottom;
    }

    public final void setListDecorationBottom(Node listDecorationBottom) {
        this.listDecorationBottom.set(listDecorationBottom);
    }

    // cell factory

    private ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory;

    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cellFactory == null ? null : cellFactory.get();
    }

    /**
     * The cell factory for the history popup list view.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        if (cellFactory == null) {
            cellFactory = new SimpleObjectProperty<>(this, "cellFactory");
        }
        return cellFactory;
    }

    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> cellFactory) {
        cellFactoryProperty().set(cellFactory);
    }

    /**
     * Indicates whether the history popup is showing. This is a read-only property.
     *
     * @return true if the history popup is showing, false otherwise
     */
    public final ReadOnlyBooleanProperty popupShowingProperty() {
        return popupShowing.getReadOnlyProperty();
    }

    /**
     * The popup used by the {@link HistoryButton} to display a list view with the previously used
     * items.
     */
    public class HistoryPopup extends CustomPopupControl {

        public static final String DEFAULT_STYLE_CLASS = "history-popup";

        public HistoryPopup() {
            getStyleClass().addAll(DEFAULT_STYLE_CLASS);

            setAutoFix(true);
            setAutoHide(true);
            setHideOnEscape(true);
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new HistoryPopupSkin(this);
        }
    }

    /**
     * The skin used for the {@link HistoryPopup}.
     */
    public class HistoryPopupSkin implements Skin<HistoryPopup> {

        private final HistoryPopup popup;
        private final BorderPane root;

        public HistoryPopupSkin(HistoryPopup popup) {
            this.popup = popup;

            root = new BorderPane() {
                @Override
                public String getUserAgentStylesheet() {
                    return Objects.requireNonNull(HistoryButton.class.getResource("history-button.css")).toExternalForm();
                }
            };

            root.getStyleClass().add("content-pane");
            root.setCenter(createListView());

            root.leftProperty().bind(listDecorationLeftProperty());
            root.rightProperty().bind(listDecorationRightProperty());
            root.topProperty().bind(listDecorationTopProperty());
            root.bottomProperty().bind(listDecorationBottomProperty());
        }

        private ListView<T> createListView() {
            ListView<T> listView = new ListView<>();
            listView.getStyleClass().add("history-list-view");

            HistoryManager<T> historyManager = getHistoryManager();
            if (historyManager != null) {
                Bindings.bindContent(listView.getItems(), historyManager.getAllUnmodifiable());
            }

            historyManagerProperty().addListener((observable, oldManager, newManager) -> {
                if (oldManager != null) {
                    Bindings.unbindContent(listView.getItems(), oldManager.getAllUnmodifiable());
                }
                if (newManager != null) {
                    Bindings.bindContent(listView.getItems(), newManager.getAllUnmodifiable());
                }
            });

            listView.cellFactoryProperty().bind(cellFactoryProperty());
            listView.placeholderProperty().bind(placeholderProperty());

            // handle mouse clicks on the listView item
            listView.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1 && !mouseEvent.isConsumed()) {
                    handleItemSelection(listView);
                    mouseEvent.consume();
                }
            });

            // handle keyboard events on the listView
            listView.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    handleItemSelection(listView);
                    keyEvent.consume();
                }
            });

            return listView;
        }

        private void handleItemSelection(ListView<T> listView) {
            T historyItem = listView.getSelectionModel().getSelectedItem();
            Optional.ofNullable(getOnItemSelected()).ifPresent(onItemSelected -> onItemSelected.accept(historyItem));
        }

        @Override
        public Node getNode() {
            return root;
        }

        @Override
        public HistoryPopup getSkinnable() {
            return popup;
        }

        @Override
        public void dispose() {
        }
    }
}
