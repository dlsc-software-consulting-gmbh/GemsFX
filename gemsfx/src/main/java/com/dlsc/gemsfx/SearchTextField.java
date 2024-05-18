package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.StringHistoryManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;

/**
 * A custom text field specifically designed for search functionality. This class enhances a text field with features
 * such as a history of search terms, an optional history popup, and custom icons for search and clear operations.
 * <p>
 * By default, when the field loses its focus or the user presses the "enter" key (triggering the onAction event), the
 * text is added to the history. This behavior can be disabled by setting the {@link #addingItemToHistoryOnEnterProperty()}
 * and / or the {@link #addingItemToHistoryOnEnterProperty()} to false.
 * <br>
 * Additionally, history can be manually added based on user actions, such as after typing text and selecting an item
 * from a ListView or TableView that displays results, or through other interactions, by calling the {@link #getHistoryManager()}
 * method to access the {@link StringHistoryManager} instance. then calling the {@link StringHistoryManager#add(String)} method.
 *
 * <p>
 * History management is enabled by default and can be accessed and controlled through a history button integrated into the search text field.
 * Users can interact with their search history, revisit previous queries, or clear historical entries. The history functionality is managed by
 * a {@link StringHistoryManager}, accessible via {@code getHistoryManager()}, allowing programmatic manipulation of history records.
 * If {@code setPreferences(Preferences preferences)} is not set on the {@link StringHistoryManager}, history records are only stored temporarily in memory
 * and are not persisted locally. This means that history data will not be retained after the application is closed.
 * </p>
 *
 */
public class SearchTextField extends CustomTextField {

    private static final boolean DEFAULT_ENABLE_HISTORY_POPUP = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST = true;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");

    private final StringHistoryManager historyManager;
    private final HistoryButton<String> historyButton;

    /**
     * Constructs a new text field customized for search operations.
     * <p>
     * The history manager is initialized with default values.
     */
    public SearchTextField() {
        this(false, new StringHistoryManager());
    }

    /**
     * Constructs a new text field customized for search operations.
     */
    public SearchTextField(StringHistoryManager historyManager) {
        this(false, historyManager);
    }

    /**
     * Constructs a new text field customized for search operations. The look and feel can be
     * adjusted to feature rounded corners / sides.
     * <p>
     * The history manager is initialized with default values.
     *
     * @param round if true the sides of the field will be round
     */
    public SearchTextField(boolean round) {
        this(round, new StringHistoryManager());
    }

    /**
     * Constructs a new text field customized for search operations. The look and feel can be
     * adjusted to feature rounded corners / sides.
     *
     * @param round if true the sides of the field will be round
     */
    public SearchTextField(boolean round, StringHistoryManager historyManager) {
        if (round) {
            getStyleClass().add("round");
        }

        getStyleClass().add("search-text-field");

        setPromptText("Search...");

        Label placeholder = new Label("No history available.");
        placeholder.getStyleClass().add("default-placeholder");
        setHistoryPlaceholder(placeholder);

        this.historyManager = historyManager;
        setHistoryCellFactory(view -> new RemovableListCell<>((listView, item) -> historyManager.remove(item)));

        historyButton = createLeftNode(round);
        setLeft(historyButton);

        setRight(createRightNode());

        addEventHandlers();

        focusedProperty().addListener(it -> {
            if (!isFocused() && isAddingItemToHistoryOnFocusLost()) {
                historyManager.add(getText());
            }
            historyButton.hideHistoryPopup();
        });
    }

    private HistoryButton<String> createLeftNode(boolean round) {
        HistoryButton<String> historyButton = new HistoryButton<>(this, historyManager);

        // Create the graphic
        Region graphic = new Region();
        graphic.getStyleClass().add("icon");
        historyButton.setGraphic(graphic);

        // Configure the history button
        historyButton.setFocusTraversable(false);
        historyButton.setFocusPopupOwnerOnOpen(true);
        historyButton.enableHistoryPopupProperty().bind(enableHistoryPopupProperty());
        historyButton.setConfigureHistoryPopup(historyPopup -> {
            if (round) {
                historyPopup.getStyleClass().add("round");
            }

            historyPopup.historyPlaceholderProperty().bind(historyPlaceholderProperty());
            historyPopup.historyCellFactoryProperty().bind(historyCellFactoryProperty());

            historyPopup.setOnHistoryItemConfirmed(history -> {
                if (history != null) {
                    // replace text
                    int oldTextLen = textProperty().getValueSafe().length();
                    replaceText(0, oldTextLen, history);
                }

                // hide popup
                historyPopup.hide();
            });
        });

        return historyButton;
    }

    private void addEventHandlers() {
        // On Action event, add the text to the history
        addEventHandler(ActionEvent.ANY, e -> {
            if (isAddingItemToHistoryOnEnter()) {
                historyManager.add(getText());
            }
        });

        // On key released, hide the history popup if the up or down key is pressed
        addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                e.consume();
            } else {
                historyButton.hideHistoryPopup();
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

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SearchTextField.class.getResource("search-text-field.css")).toExternalForm();
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
            enableHistoryPopup = new SimpleBooleanProperty(this, "enableHistoryPopup", DEFAULT_ENABLE_HISTORY_POPUP) {
                @Override
                protected void invalidated() {
                    pseudoClassStateChanged(DISABLED_POPUP_PSEUDO_CLASS, !get());
                }
            };
        }
        return enableHistoryPopup;
    }

    public final boolean isEnableHistoryPopup() {
        return enableHistoryPopup == null ? DEFAULT_ENABLE_HISTORY_POPUP : enableHistoryPopup.get();
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

    /**
     * If we want to manually add history records, delete history records, clear history records, then please get the HistoryManager object through this method.
     *
     * @return the history manager
     */
    public StringHistoryManager getHistoryManager() {
        return historyManager;
    }

}
