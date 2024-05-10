package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SearchTextFieldHistoryPopup;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventTarget;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A custom text field specifically designed for search functionality. This class enhances a text field with features
 * such as a history of search terms, an optional history popup, and custom icons for search and clear operations.
 * <p>
 * By default, when the field loses its focus or the user presses the "enter" key (triggering the onAction event), the
 * text is added to the history. This behavior can be disabled by setting the {@link #addingItemToHistoryOnEnterProperty()}
 * and / or the {@link #addingItemToHistoryOnEnterProperty()} to false.
 * <br>
 * Additionally, history can be manually added based on user actions, such as after typing text and selecting an item
 * from a ListView or TableView that displays results, or through other interactions, by calling the {@link #addHistory}
 * method to add the current text to the history.
 */
public class SearchTextField extends CustomTextField {

    private static final Logger LOG = Logger.getLogger(SearchTextField.class.getName());

    private static final int DEFAULT_MAX_HISTORY_SIZE = 30;
    private static final boolean ENABLE_HISTORY_POPUP = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST = true;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");
    private static final PseudoClass HISTORY_POPUP_SHOWING_PSEUDO_CLASS = PseudoClass.getPseudoClass("history-popup-showing");

    private SearchTextFieldHistoryPopup historyPopup;
    private final StackPane searchIconWrapper;

    /**
     * Constructs a new text field customized for search operations.
     */
    public SearchTextField() {
        this(false);
    }

    /**
     * Constructs a new text field customized for search operations. The look and feel can be
     * adjusted to feature rounded corners / sides.
     *
     * @param round if true the sides of the field will be round
     */
    public SearchTextField(boolean round) {
        if (round) {
            getStyleClass().add("round");
        }

        getStyleClass().add("search-text-field");

        setPromptText("Search...");

        Label placeholder = new Label("No history available.");
        placeholder.getStyleClass().add("default-placeholder");
        setHistoryPlaceholder(placeholder);

        searchIconWrapper = createLeftNode();

        setLeft(searchIconWrapper);
        setRight(createRightNode());

        addEventHandlers();
        addPropertyListeners();

        setHistoryCellFactory(param -> new RemovableListCell<>((listView, item) -> removeHistory(item)));

        focusedProperty().addListener(it -> {
            if (!isFocused() && isAddingItemToHistoryOnFocusLost()) {
                addHistory(getText());
            }
        });

        getUnmodifiableHistory().addListener((Observable it) -> {
            if (getPreferences() != null) {
                storeHistory();
            }
        });

        InvalidationListener loadHistoryListener = it -> {
            if (getPreferences() != null) {
                loadHistory();
            }
        };

        preferencesProperty().addListener(loadHistoryListener);
    }

    private void storeHistory() {
        Preferences preferences = getPreferences();
        if (preferences != null) {
            preferences.put("search-items", String.join(",", getUnmodifiableHistory()));
        }
    }

    private void loadHistory() {
        Preferences preferences = getPreferences();
        if (preferences != null) {
            String items = preferences.get("search-items", "");
            if (StringUtils.isNotEmpty(items)) {
                history.setAll(items.split(","));
            }
            // else { history.clear(); }
        }
    }

    private void addEventHandlers() {
        // On Action event, add the text to the history
        addEventHandler(ActionEvent.ANY, e -> {
            if (isAddingItemToHistoryOnEnter()) {
                addHistory(getText());
            }
        });

        // On mouse pressed, hide the history popup
        addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            EventTarget target = e.getTarget();
            boolean clickHistoryButton = (target instanceof Node && searchIconWrapper.getChildren().contains(target)) || searchIconWrapper.equals(target);
            if (!clickHistoryButton) {
                hideHistoryPopup();
            }
        });

        // On key pressed, hide the history popup. Consume the UP and DOWN key events.
        addEventHandler(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                e.consume();
            } else {
                hideHistoryPopup();
            }
        });
    }

    private void addPropertyListeners() {
        focusedProperty().addListener(it -> hideHistoryPopup());

        maxHistorySizeProperty().addListener(it -> {
            // Check if the max history size is negative. If so, log a warning.
            if (getMaxHistorySize() < 0) {
                LOG.warning("Max history size must be greater than or equal to 0. ");
            }

            int max = Math.max(0, getMaxHistorySize());
            if (history.size() > max) {
                history.remove(max, history.size());
            }
        });
    }

    private StackPane createRightNode() {
        FontIcon clearIcon = new FontIcon(MaterialDesign.MDI_CLOSE);
        clearIcon.getStyleClass().add("clear-icon");
        clearIcon.setCursor(Cursor.DEFAULT);
        clearIcon.setOnMouseClicked(evt -> setText(""));
        clearIcon.visibleProperty().bind(textProperty().isNotEmpty());
        clearIcon.managedProperty().bind(textProperty().isNotEmpty());

        StackPane clearIconWrapper = new StackPane(clearIcon);
        clearIconWrapper.getStyleClass().addAll("wrapper", "clear-icon-wrapper");
        return clearIconWrapper;
    }

    private StackPane createLeftNode() {
        Region searchIcon = new Region();
        searchIcon.getStyleClass().add("search-icon");
        searchIcon.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        StackPane searchIconWrapper = new StackPane(searchIcon);
        searchIconWrapper.getStyleClass().addAll("wrapper", "search-icon-wrapper");
        searchIconWrapper.setOnMouseClicked(this::clickIconWrapperHandler);
        return searchIconWrapper;
    }

    /*
     * Handles the click event on the icon wrapper of the search text field.
     */
    private void clickIconWrapperHandler(MouseEvent event) {
        if (!isFocused()) {
            requestFocus();
        }

        if (event.getButton() != MouseButton.PRIMARY || !isEnableHistoryPopup()) {
            return;
        }

        if (historyPopup == null) {
            historyPopup = new SearchTextFieldHistoryPopup(this);
            historyPopupShowing.bind(historyPopup.showingProperty());
        }

        if (historyPopup.isShowing()) {
            historyPopup.hide();
        } else {
            historyPopup.show(this);
        }

        positionCaret(textProperty().getValueSafe().length());
    }

    private void hideHistoryPopup() {
        if (historyPopup != null && historyPopup.isShowing()) {
            historyPopup.hide();
        }
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SearchTextField.class.getResource("search-text-field.css")).toExternalForm();
    }

    private final ObservableList<String> history = FXCollections.observableArrayList();

    /**
     * Sets the history of the search text field. The given list of Strings will be processed to guarantee unique
     * entries.
     *
     * @param history the list of strings representing the history
     */
    public final void setHistory(List<String> history) {
        this.history.setAll(convertToUniqueList(history));
    }

    /**
     * Adds the given item to the history. The method ensures that duplicates will not be added.
     *
     * @param item the item to add
     */
    public final void addHistory(String item) {
        if (StringUtils.isNotEmpty(item)) {
            history.remove(item);
            history.add(0, item);
        }

        int max = Math.max(0, getMaxHistorySize());
        if (history.size() > max) {
            history.remove(max, history.size());
        }
    }

    /**
     * Adds the given items to the history.
     *
     * @param items the items to add
     */
    public final void addHistory(List<String> items) {
        List<String> uniqueItems = convertToUniqueList(items);
        for (String item : uniqueItems) {
            addHistory(item);
        }
    }

    /**
     * Removes the given item from the history.
     *
     * @param item the item to remove
     * @return true if the item was removed, false otherwise
     */
    public final boolean removeHistory(String item) {
        return history.remove(item);
    }

    /**
     * Removes the given items from the history.
     *
     * @param items the items to remove
     */
    public final void removeHistory(List<String> items) {
        history.removeAll(items);
    }

    /**
     * Clears the history.
     */
    public final void clearHistory() {
        history.clear();
    }

    private final ObservableList<String> unmodifiableHistory = FXCollections.unmodifiableObservableList(history);

    /**
     * Returns an unmodifiable list of the history.
     */
    public final ObservableList<String> getUnmodifiableHistory() {
        return unmodifiableHistory;
    }

    private IntegerProperty maxHistorySize;

    /**
     * Returns the property representing the maximum history size of the search text field.
     *
     * @return the maximum history size property
     */
    public final IntegerProperty maxHistorySizeProperty() {
        if (maxHistorySize == null) {
            maxHistorySize = new SimpleIntegerProperty(this, "maxHistorySize", DEFAULT_MAX_HISTORY_SIZE);
        }
        return maxHistorySize;
    }

    public final int getMaxHistorySize() {
        return maxHistorySize == null ? DEFAULT_MAX_HISTORY_SIZE : maxHistorySize.get();
    }

    public final void setMaxHistorySize(int maxHistorySize) {
        maxHistorySizeProperty().set(maxHistorySize);
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

    private ObjectProperty<Callback<ListView<String>, ListCell<String>>> historyCellFactory;

    public final Callback<ListView<String>, ListCell<String>> getHistoryCellFactory() {
        return historyCellFactory == null ? null : historyCellFactory.get();
    }

    /**
     * The cell factory for the history popup list view.
     *
     * @return the cell factory
     */
    public final ObjectProperty<Callback<ListView<String>, ListCell<String>>> historyCellFactoryProperty() {
        if (historyCellFactory == null) {
            historyCellFactory = new SimpleObjectProperty<>(this, "historyCellFactory");
        }
        return historyCellFactory;
    }

    public final void setHistoryCellFactory(Callback<ListView<String>, ListCell<String>> historyCellFactory) {
        historyCellFactoryProperty().set(historyCellFactory);
    }

    private BooleanProperty enableHistoryPopup;

    /**
     * Indicates whether the history popup should be enabled.
     *
     * @return true if the history popup should be enabled, false otherwise
     */
    public final BooleanProperty enableHistoryPopupProperty() {
        if (enableHistoryPopup == null) {
            enableHistoryPopup = new SimpleBooleanProperty(this, "enableHistoryPopup", ENABLE_HISTORY_POPUP) {
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DISABLED_POPUP_PSEUDO_CLASS, !get());
                }
            };
        }
        return enableHistoryPopup;
    }

    public final boolean isEnableHistoryPopup() {
        return enableHistoryPopup == null ? ENABLE_HISTORY_POPUP : enableHistoryPopup.get();
    }

    public final void setEnableHistoryPopup(boolean enableHistoryPopup) {
        enableHistoryPopupProperty().set(enableHistoryPopup);
    }

    // add on enter

    private BooleanProperty addingItemToHistoryOnEnter;

    /**
     * Determines whether the text of the text field should be added to the history when the user presses the Enter key.
     *
     * @return true if the text should be added to the history on Enter, false otherwise
     */
    public final BooleanProperty addingItemToHistoryOnEnterProperty() {
        if (addingItemToHistoryOnEnter == null) {
            addingItemToHistoryOnEnter = new SimpleBooleanProperty(this, "addingItemToHistoryOnEnter", DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER);
        }
        return addingItemToHistoryOnEnter;
    }

    public final boolean isAddingItemToHistoryOnEnter() {
        return addingItemToHistoryOnEnter == null ? DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER : addingItemToHistoryOnEnter.get();
    }

    public final void setAddingItemToHistoryOnEnter(boolean addingItemToHistoryOnEnter) {
        addingItemToHistoryOnEnterProperty().set(addingItemToHistoryOnEnter);
    }

    // add on focus lost

    private BooleanProperty addingItemToHistoryOnFocusLost;

    /**
     * Determines whether the text of the text field should be added to the history when the field losses its focus.
     *
     * @return true if the text should be added to the history on focus lost, false otherwise
     */
    public final BooleanProperty addingItemToHistoryOnFocusLostProperty() {
        if (addingItemToHistoryOnFocusLost == null) {
            addingItemToHistoryOnFocusLost = new SimpleBooleanProperty(this, "addingItemToHistoryOnFocusLost", DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST);
        }
        return addingItemToHistoryOnFocusLost;
    }

    public final boolean isAddingItemToHistoryOnFocusLost() {
        return addingItemToHistoryOnFocusLost == null ? DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST : addingItemToHistoryOnFocusLost.get();
    }

    public final void setAddingItemToHistoryOnFocusLost(boolean addingItemToHistoryOnFocusLost) {
        addingItemToHistoryOnFocusLostProperty().set(addingItemToHistoryOnFocusLost);
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

    /**
     * Indicates whether the history popup is showing. This is a read-only property.
     *
     * @return true if the history popup is showing, false otherwise
     */
    public final ReadOnlyBooleanProperty historyPopupShowingProperty() {
        return historyPopupShowing.getReadOnlyProperty();
    }

    private final ObjectProperty<Preferences> preferences = new SimpleObjectProperty<>(this, "preferences");

    /**
     * Stores a preferences object that will be used for persisting the search history of the field.
     *
     * @return the preferences used for persisting the search history
     */
    public final ObjectProperty<Preferences> preferencesProperty() {
        return preferences;
    }

    public final Preferences getPreferences() {
        return preferences.get();
    }

    public final void setPreferences(Preferences preferences) {
        this.preferences.set(preferences);
    }

    /**
     * Converts a given list of strings to a unique list of strings. Filters out empty strings.
     *
     * @param history the list of strings to convert
     * @return the converted unique list of strings
     */
    private List<String> convertToUniqueList(List<String> history) {
        return history.stream().distinct().filter(StringUtils::isNotEmpty).limit(Math.max(0, getMaxHistorySize())).toList();
    }
}
