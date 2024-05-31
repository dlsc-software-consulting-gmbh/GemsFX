package com.dlsc.gemsfx;

import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import com.dlsc.gemsfx.util.UIUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Objects;

/**
 * A custom text field specifically designed for search functionality. This class enhances a text field with features
 * such as a history of search terms, an optional history popup, and custom icons for search and clear operations.
 * <p>
 * The history manager is disabled by default, but it can be enabled using the {@link #setHistoryManager(HistoryManager)} method.
 * We have implemented a local history manager, {@link StringHistoryManager}, which uses the Java Preferences API to store history records.
 * You can enable it via the {@link #setHistoryManager(HistoryManager)} method.
 * <p>
 * By default, when the field loses its focus or the user presses the "enter" key (triggering the onAction event), the
 * text is added to the history. This behavior can be disabled by setting the {@link #addingItemToHistoryOnEnterProperty()}
 * and / or the {@link #addingItemToHistoryOnEnterProperty()} to false.
 * <br>
 * Additionally, history can be manually added based on user actions, such as after typing text and selecting an item
 * from a ListView or TableView that displays results, or through other interactions, by calling the {@link #getHistoryManager()}
 * method to access the {@link StringHistoryManager} instance. then calling the {@link StringHistoryManager#add(Object)}} method.
 */
public class SearchTextField extends CustomTextField {

    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST = true;
    private static final boolean DEFAULT_ROUND = false;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");

    private final HistoryButton<String> historyButton;

    /**
     * Constructs a new text field customized for search operations.
     * <p>
     * The history manager is initialized with default values.
     */
    public SearchTextField() {
        getStyleClass().add("search-text-field");
        UIUtil.toggleClassBasedOnObservable(this, "round", roundProperty());

        setPromptText("Search...");

        Label placeholder = new Label("No items.");
        placeholder.getStyleClass().add("default-placeholder");
        setHistoryPlaceholder(placeholder);

        setLeft(historyButton = createHistoryButton());
        setRight(createRightNode());

        addEventHandlers();

        focusedProperty().addListener(it -> {
            if (!isFocused()) {
                if (isAddingItemToHistoryOnFocusLost()) {
                    addToHistory();
                }
                historyButton.hidePopup();
            }
        });
    }

    private void addToHistory() {
        HistoryManager<String> historyManager = getHistoryManager();
        if (historyManager != null) {
            String text = getText();
            if (StringUtils.isNotBlank(text)) {
                historyManager.add(text);
            }
        }
    }

    private HistoryButton<String> createHistoryButton() {
        HistoryButton<String> historyButton = new HistoryButton<>(this);
        historyButton.placeholderProperty().bind(historyPlaceholderProperty());
        historyButton.historyManagerProperty().bind(historyManagerProperty());
        historyButton.setOnItemSelected(value -> {
            if (StringUtils.isNotBlank(value)) {
                setText(value);
            }
            historyButton.hidePopup();
        });

        // Create the graphic
        Region graphic = new Region();
        graphic.getStyleClass().add("icon");
        historyButton.setGraphic(graphic);

        // Configure the history button
        historyButton.setFocusTraversable(false);

        return historyButton;
    }

    private void addEventHandlers() {
        // On Action event, add the text to the history
        addEventHandler(ActionEvent.ANY, e -> {
            if (isAddingItemToHistoryOnEnter()) {
                addToHistory();
            }
        });

        // On key released, hide the history popup if the up or down key is pressed
        addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            if (e.getCode() == KeyCode.UP || e.getCode() == KeyCode.DOWN) {
                e.consume();
            } else {
                historyButton.hidePopup();
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

    private BooleanProperty round;

    /**
     * Determines whether the text field should have round corners.
     *
     * @return true if the text field should have round corners, false otherwise
     */
    public final BooleanProperty roundProperty() {
        if (round == null) {
            round = new SimpleBooleanProperty(this, "round", DEFAULT_ROUND);
        }
        return round;
    }

    public final boolean isRound() {
        return round == null ? DEFAULT_ROUND : round.get();
    }

    public final void setRound(boolean round) {
        roundProperty().set(round);
    }

    private ObjectProperty<HistoryManager<String>> historyManager;

    /**
     * The history manager that is used to manage the history of the search text field.
     * <p>
     * If its value is null, the history feature will not be enabled, which means only
     * the magnifying glass icon will be displayed, and the dropdown arrow next to the
     * magnifying glass will not be shown.
     * <p>
     * If its value is not null, the history feature will be enabled, meaning that both
     * the magnifying glass icon and the dropdown arrow will be displayed. Clicking the
     * magnifying glass icon button will display the history popup.
     * <p>
     * To enable the history feature, you need to set an instance of {@link HistoryManager}.
     * Typically, you would use an instance of {@link StringHistoryManager}, which is an
     * implementation of {@link HistoryManager} that manages string-type history records.
     *
     * @return the property representing the history manager
     */
    public final ObjectProperty<HistoryManager<String>> historyManagerProperty() {
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

    public final HistoryManager<String> getHistoryManager() {
        return historyManager == null ? null : historyManager.get();
    }

    public final void setHistoryManager(HistoryManager<String> historyManager) {
        historyManagerProperty().set(historyManager);
    }
}
