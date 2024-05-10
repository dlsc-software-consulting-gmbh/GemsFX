package com.dlsc.gemsfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputControl;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.prefs.Preferences;

public interface SearchHistorySupport {

   int DEFAULT_MAX_HISTORY_SIZE = 30;

   boolean ENABLE_HISTORY_POPUP = true;

   boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER = true;

   boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST = true;

    Region getNode();

    /**
     * Returns the text input control that is associated with this history support.
     *
     * @return the text input control
     */
    TextInputControl getTextInputControl();

    /**
     * Sets the history of the search text field. The given list of Strings will be processed to guarantee unique
     * entries.
     *
     * @param history the list of strings representing the history
     */
    void setHistory(List<String> history);

    /**
     * Adds the given item to the history. The method ensures that duplicates will not be added.
     *
     * @param item the item to add
     */
    void addHistory(String item);

    /**
     * Adds the given items to the history.
     *
     * @param items the items to add
     */
    void addHistory(List<String> items);

    /**
     * Removes the given item from the history.
     *
     * @param item the item to remove
     * @return true if the item was removed, false otherwise
     */
    boolean removeHistory(String item);

    /**
     * Removes the given items from the history.
     *
     * @param items the items to remove
     */
    void removeHistory(List<String> items);

    /**
     * Clears the history.
     */
    void clearHistory();

    /**
     * Returns an unmodifiable list of the history.
     */
    ObservableList<String> getUnmodifiableHistory();

    /**
     * Returns the property representing the maximum history size of the search text field.
     *
     * @return the maximum history size property
     */
    IntegerProperty maxHistorySizeProperty();

    int getMaxHistorySize();

    void setMaxHistorySize(int maxHistorySize);

    /**
     * Returns the property representing the history placeholder node.
     *
     * @return the property representing the history placeholder node
     */
    ObjectProperty<Node> historyPlaceholderProperty();

    Node getHistoryPlaceholder();

    void setHistoryPlaceholder(Node historyPlaceholder);

    /**
     * The cell factory for the history popup list view.
     *
     * @return the cell factory
     */
    ObjectProperty<Callback<ListView<String>, ListCell<String>>> historyCellFactoryProperty();

    void setHistoryCellFactory(Callback<ListView<String>, ListCell<String>> historyCellFactory);

    Callback<ListView<String>, ListCell<String>> getHistoryCellFactory();

    /**
     * Indicates whether the history popup should be enabled.
     *
     * @return true if the history popup should be enabled, false otherwise
     */
    BooleanProperty enableHistoryPopupProperty();

    boolean isEnableHistoryPopup();

    void setEnableHistoryPopup(boolean enableHistoryPopup);

    /**
     * Determines whether the text of the text field should be added to the history when the user presses the Enter key.
     *
     * @return true if the text should be added to the history on Enter, false otherwise
     */
    BooleanProperty addingItemToHistoryOnEnterProperty();

    boolean isAddingItemToHistoryOnEnter();

    void setAddingItemToHistoryOnEnter(boolean addingItemToHistoryOnEnter);

    /**
     * Determines whether the text of the text field should be added to the history when the field losses its focus.
     *
     * @return true if the text should be added to the history on focus lost, false otherwise
     */
    BooleanProperty addingItemToHistoryOnFocusLostProperty();

    boolean isAddingItemToHistoryOnFocusLost();

    void setAddingItemToHistoryOnFocusLost(boolean addingItemToHistoryOnFocusLost);

    /**
     * Indicates whether the history popup is showing. This is a read-only property.
     *
     * @return true if the history popup is showing, false otherwise
     */
    ReadOnlyBooleanProperty historyPopupShowingProperty();

    boolean isHistoryPopupShowing();

    /**
     * Stores a preferences object that will be used for persisting the search history of the field.
     *
     * @return the preferences used for persisting the search history
     */
    ObjectProperty<Preferences> preferencesProperty();

    Preferences getPreferences();

    void setPreferences(Preferences preferences);

    /**
     * Converts a given list of strings to a unique list of strings. Filters out empty strings.
     *
     * @param history the list of strings to convert
     * @return the converted unique list of strings
     */
    default List<String> convertToUniqueList(List<String> history) {
        return history.stream().distinct().filter(StringUtils::isNotEmpty).limit(Math.max(0, getMaxHistorySize())).toList();
    }

}
