package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.UIUtil;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
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
 * <p>Customization options include enabling or disabling the history popup, and configuring
 * how the history items are displayed and managed within the popup. The button's style and behavior
 * can be extensively customized through CSS properties and dynamic property bindings.</p>
 *
 * <p>Usage scenarios include search fields, form inputs, or any other component where users might benefit
 * from being able to see and interact with their previous entries. The generic type {@code T} allows for
 * flexibility, making it suitable for various data types that can represent user input history.</p>
 *
 * @param <T> the type of the objects that this button manages in its history
 */
public class HistoryButton<T> extends Button {

    private static final String DEFAULT_STYLE_CLASS = "history-button";
    private static final boolean DEFAULT_ROUND_POPUP = false;
    private static final boolean DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN = false;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");
    private static final PseudoClass HISTORY_POPUP_SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("history-popup-showing");

    private HistoryPopup historyPopup;

    /**
     * Creates a new instance of the history button.
     */
    public HistoryButton() {
        getStyleClass().addAll(DEFAULT_STYLE_CLASS);
        setGraphic(new FontIcon(MaterialDesign.MDI_HISTORY));
        setOnAction(evt -> showPopup());
        setHistoryPlaceholder(new Label("No items."));
        setCellFactory(view -> new RemovableListCell<>((listView, item) -> {
            HistoryManager<T> historyManager = getHistoryManager();
            if (historyManager != null) {
                historyManager.remove(item);
            }
        }));
    }

//    /**
//     * Creates a new instance of the history button.
//     *
//     * @param popupOwner the owner of the button, used for focus management.
//     */
//    public HistoryButton(Region popupOwner) {
//        this();
//        setOwner(popupOwner);
//    }

    public void showPopup() {
//        Node owner = getOwner();
//
//        if (owner != null && owner != this && !owner.isFocused() && getFocusPopupOwnerOnOpen()) {
//           //owner.requestFocus();
//        }

        if (getHistoryManager() == null) {
            return;
        }

        if (historyPopup == null) {
            historyPopup = new HistoryPopup();
            UIUtil.toggleClassBasedOnObservable(historyPopup, "round", roundProperty());

            // basic settings
            historyPopupShowing.bind(historyPopup.showingProperty());
        }

        if (historyPopup.isShowing()) {
            hidePopup();
        } else {
            historyPopup.show(this);
        }
    }

    /**
     * Hides the popup that is showing the history items.
     */
    public void hidePopup() {
        if (historyPopup != null) {
            historyPopup.hide();
        }
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

    private ObjectProperty<Consumer<T>> onHistoryItemSelected;

    public final Consumer<T> getOnHistoryItemSelected() {
        return onHistoryItemSelected == null ? null : onHistoryItemSelected.get();
    }

    /**
     * Returns the property representing the callback function to be executed when a history item within the ListView
     * is either clicked directly or selected via the ENTER key press. This property enables setting a custom callback
     * function that will be invoked with the text of the clicked or selected history item as the argument.
     *
     * @return the property storing the "on history item confirmed" callback function.
     */
    public final ObjectProperty<Consumer<T>> onHistoryItemSelectedProperty() {
        if (onHistoryItemSelected == null) {
            onHistoryItemSelected = new SimpleObjectProperty<>(this, "onHistoryItemConfirmed");
        }
        return onHistoryItemSelected;
    }

    public final void setOnHistoryItemSelected(Consumer<T> onHistoryItemSelected) {
        onHistoryItemSelectedProperty().set(onHistoryItemSelected);
    }

    private BooleanProperty focusPopupOwnerOnOpen;

    /**
     * Controls whether the Popup Owner should gain focus when the popup is displayed after a button click.
     * <p>
     * This property determines whether the Popup Owner, which is the reference component for the popup's position,
     * should receive focus when the popup is opened. If set to true, the Popup Owner will be focused
     * when the popup becomes visible. If set to false, the Popup Owner will retain its current focus state.
     * <p>
     * The default value is false.
     *
     * @return the BooleanProperty that enables or disables focus on the Popup Owner when the popup opens
     */
    public final BooleanProperty focusPopupOwnerOnOpenProperty() {
        if (focusPopupOwnerOnOpen == null) {
            focusPopupOwnerOnOpen = new StyleableBooleanProperty(DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN) {
                @Override
                public Object getBean() {
                    return this;
                }

                @Override
                public String getName() {
                    return "popupOwnerFocusOnClick";
                }

                @Override
                public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
                    return StyleableProperties.FOCUS_POPUP_OWNER_ON_OPEN;
                }
            };
        }
        return focusPopupOwnerOnOpen;
    }

    public final boolean getFocusPopupOwnerOnOpen() {
        return focusPopupOwnerOnOpen == null ? DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN : focusPopupOwnerOnOpen.get();
    }

    public final void setFocusPopupOwnerOnOpen(boolean focusPopupOwnerOnOpen) {
        focusPopupOwnerOnOpenProperty().set(focusPopupOwnerOnOpen);
    }

    private final ReadOnlyBooleanWrapper historyPopupShowing = new ReadOnlyBooleanWrapper(this, "historyPopupShowing") {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(HISTORY_POPUP_SHOWING_PSEUDO_CLASS, get());
        }
    };

    public final boolean isHistoryPopupShowing() {
        return historyPopupShowing.get();
    }

    private ObjectProperty<HistoryManager<T>> historyManager;

    /**
     * The history manager that is used to manage the history of the HistoryButton.
     * <p>
     * If its value is null, clicking the button will not display the history popup.
     * <p>
     * If its value is not null, clicking the button will display the history popup.
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

//    private ObjectProperty<Node> owner;
//
//    /**
//     * The (optional) "owner" of a history button can be a textfield where the button is shown inside the field.
//     *
//     * @return
//     */
//    public final ObjectProperty<Node> ownerProperty() {
//        if (owner == null) {
//            owner = new SimpleObjectProperty<>(this, "owner");
//        }
//        return owner;
//    }
//
//    public final Node getOwner() {
//        return owner == null ? null : owner.get();
//    }
//
//    public final void setOwner(Node owner) {
//        ownerProperty().set(owner);
//    }

    private BooleanProperty round;

    /**
     * Determines whether the text field should have round corners.
     *
     * @return true if the text field should have round corners, false otherwise
     */
    public final BooleanProperty roundProperty() {
        if (round == null) {
            round = new SimpleBooleanProperty(this, "round", DEFAULT_ROUND_POPUP);
        }
        return round;
    }

    public final boolean isRound() {
        return round == null ? DEFAULT_ROUND_POPUP : round.get();
    }

    public final void setRound(boolean round) {
        roundProperty().set(round);
    }

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
    public final ReadOnlyBooleanProperty historyPopupShowingProperty() {
        return historyPopupShowing.getReadOnlyProperty();
    }

    private static class StyleableProperties {

        private static final CssMetaData<HistoryButton, Boolean> FOCUS_POPUP_OWNER_ON_OPEN = new CssMetaData<>(
                "-fx-focus-popup-owner-on-open", BooleanConverter.getInstance(), DEFAULT_FOCUS_POPUP_OWNER_ON_OPEN) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(HistoryButton control) {
                return (StyleableProperty<Boolean>) control.focusPopupOwnerOnOpenProperty();
            }

            @Override
            public boolean isSettable(HistoryButton control) {
                return control.focusPopupOwnerOnOpen == null || !control.focusPopupOwnerOnOpen.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Button.getClassCssMetaData());
            styleables.add(FOCUS_POPUP_OWNER_ON_OPEN);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return HistoryButton.StyleableProperties.STYLEABLES;
    }

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
     */
    public class HistoryPopup extends CustomPopupControl {

        public static final String DEFAULT_STYLE_CLASS = "history-popup";

        public HistoryPopup() {
            getStyleClass().addAll(DEFAULT_STYLE_CLASS);

            setAutoFix(true);
            setAutoHide(true);
            setHideOnEscape(true);
        }

        protected Skin<?> createDefaultSkin() {
            return new HistoryPopupSkin(this);
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
    }

    /**
     * Provides a concrete implementation of a skin for {@link HistoryPopup}, defining the visual representation
     * and interaction handling of the popup. This skin layout includes a {@link ListView} that displays the history
     * items, which can be interacted with via mouse or keyboard.
     *
     * <p>The skin binds various properties from the {@link HistoryPopup} to configure and customize the layout
     * and behavior of the popup elements, including the arrangement of nodes around the central list view (top,
     * bottom, left, right).</p>
     *
     * <p>Interactions such as mouse clicks and keyboard inputs are handled to select and confirm history items,
     * allowing for a seamless user experience. The history items are displayed using a configurable cell factory,
     * and the skin reacts to changes in the popup's properties to update the UI accordingly.</p>
     *
     * <p>This skin ensures that the popup's visual structure is maintained in alignment with the popup's configuration,
     * supporting dynamic changes to the content and layout.</p>
     */
    public class HistoryPopupSkin implements Skin<HistoryPopup> {

        private final HistoryPopup popup;
        private final BorderPane root;
        private final ListView<T> listView;

        public HistoryPopupSkin(HistoryPopup popup) {
            this.popup = popup;

            root = new BorderPane() {
                @Override
                public String getUserAgentStylesheet() {
                    return Objects.requireNonNull(SearchField.class.getResource("history-popup.css")).toExternalForm();
                }
            };

            root.getStyleClass().add("content-pane");

            listView = createHistoryListView();
            root.setCenter(listView);

            root.leftProperty().bind(popup.leftProperty());
            root.rightProperty().bind(popup.rightProperty());
            root.topProperty().bind(popup.topProperty());
            root.bottomProperty().bind(popup.bottomProperty());
        }

        private ListView<T> createHistoryListView() {
            ListView<T> listView = new ListView<>();
            listView.getStyleClass().add("history-list-view");

            HistoryManager<T> historyManager = getHistoryManager();
            if (historyManager != null) {
                Bindings.bindContent(listView.getItems(), historyManager.getAll());
            }

            historyManagerProperty().addListener((observable, oldValue, newValue) -> {
                if (oldValue != null) {
                    Bindings.unbindContent(listView.getItems(), oldValue.getAll());
                }
                if (newValue != null) {
                    Bindings.bindContent(listView.getItems(), newValue.getAll());
                }
            });

            listView.cellFactoryProperty().bind(cellFactoryProperty());
            listView.placeholderProperty().bind(historyPlaceholderProperty());

            // handle mouse clicks on the listView item
            listView.addEventHandler(MouseEvent.MOUSE_PRESSED, mouseEvent -> {
                if (isPrimarySingleClick(mouseEvent) && !mouseEvent.isConsumed()) {
                    handlerHistoryItemConfirmed(listView);
                    mouseEvent.consume();
                }
            });

            // handle keyboard events on the listView
            listView.addEventFilter(KeyEvent.KEY_RELEASED, keyEvent -> {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    handlerHistoryItemConfirmed(listView);
                    keyEvent.consume();
                }
            });

            return listView;
        }

        private void handlerHistoryItemConfirmed(ListView<T> listView) {
            T historyItem = listView.getSelectionModel().getSelectedItem();
            Optional.ofNullable(getOnHistoryItemSelected()).ifPresent(onItemSelected -> onItemSelected.accept(historyItem));
        }

        private boolean isPrimarySingleClick(MouseEvent mouseEvent) {
            return mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1;
        }

        public final ListView<T> getListView() {
            return listView;
        }

        public Node getNode() {
            return root;
        }

        public HistoryPopup getSkinnable() {
            return popup;
        }

        public void dispose() {
            HistoryManager<T> historyManager = getHistoryManager();
            if (historyManager != null) {
                Bindings.unbindContent(listView.getItems(), historyManager.getAll());
            }

            listView.prefWidthProperty().unbind();
            listView.maxWidthProperty().unbind();
            listView.minWidthProperty().unbind();

            listView.cellFactoryProperty().unbind();
            listView.placeholderProperty().unbind();
        }
    }
}
