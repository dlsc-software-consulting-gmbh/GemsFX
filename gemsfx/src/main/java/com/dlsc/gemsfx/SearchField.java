package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SearchFieldPopup;
import com.dlsc.gemsfx.skins.SearchFieldSkin;
import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * The search field is a standard text field with auto suggest capabilities
 * and a selection model for a specific type of object. This type is defined by the
 * generic type argument. The main difference to other auto-suggest text fields is that
 * the main outcome of this field is an object and not just the text entered by the
 * user. Another difference is how the text field automatically finds and selects the
 * first object that matches the text typed by the user so far. A third feature of
 * this control is its ability to create new instances of the specified object type if
 * no matching object can be found in the list of objects returned by the suggestion
 * provider. This last feature allows an application to let the user either pick an
 * existing object or to create a new one on-the-fly (but only if a new item producer
 * has been set).
 * <p>
 * The search field requires proper configuration to work correctly:
 *
 * <ol>
 * <li><b>Suggestion Provider</b> - a callback that returns a collection of items for a given search field suggestion request. The suggestion provider is invoked asynchronously via JavaFX concurrency API (service & task). The suggestion provider gets invoked slightly delayed whenever the user types some text into the field. If the user types again the current search gets cancelled and a new search gets initiated. As long as the user types fast enough the actual search will not be performed.</li>
 * <li><b>Converter</b> - the converter is used to convert the items found in the suggestions list to text. This is just a standard StringConverter instance (only the toString() method needs to be implemented).</li>
 * <li><b>Cell Factory</b> - a standard list cell factory / callback used for the ListView instance shown in the popup that presents the suggested items. The default cell factory should be sufficient for most use cases. It simply displays the name of the items via the help of the string converter. However, it also underlines the text match in the name.</li>
 * <li><b>Matcher</b> - a function taking two arguments that will be applied to the suggested items to find "perfect matches" for the given search text (entered by the user). The function takes an item and the search text as input and returns a boolean. The first perfect match found will be used to autocomplete the text of the search field.</li>
 * <li><b>New Item Producer</b> - a callback that returns a new item instance of the type supported by the search field. This callback is used if the field is configured to create items "on-the-fly", meaning the typed text does not match anything in the suggested list of items.</li>
 * <li><b>Comparator</b> - a standard comparator used to perform a first sorting of the suggested items. However, internally the field wraps this comparator to place some items higher up in the dropdown list as they are better matches for the current search text.</li>
 * </ol>
 *
 * <p>
 * The history manager is disabled by default, but it can be enabled using the {@link #setHistoryManager(HistoryManager)} method.
 * We have implemented a local history manager, {@link StringHistoryManager}, which uses the Java Preferences API to store history records.
 * You can enable it via the {@link #setHistoryManager(HistoryManager)} method.
 * </p>
 *
 * @param <T> the type of objects to work on
 * @see #setSuggestionProvider(Callback)
 * @see #setConverter(StringConverter)
 * @see #setCellFactory(Callback)
 * @see #setMatcher(BiFunction)
 * @see #setNewItemProducer(Callback)
 * @see #setComparator(Comparator)
 */
public class SearchField<T> extends Control {

    private static final String DEFAULT_STYLE_CLASS = "search-field";

    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_ENTER = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_COMMIT = true;
    private static final boolean DEFAULT_ADDING_ITEM_TO_HISTORY_ON_FOCUS_LOST = true;

    private static final PseudoClass DISABLED_POPUP_PSEUDO_CLASS = PseudoClass.getPseudoClass("disabled-popup");

    private final SearchService searchService = new SearchService();

    private final TextField editor = new TextField();

    private final SearchFieldPopup<T> popup;
    private final HistoryButton<String> historyButton;

    /**
     * Constructs a new spotlight field. The field will set defaults for the
     * matcher, the converter, the cell factory, and the comparator. It will
     * not set a default for the "new item" producer.
     * <p>
     * The history manager is initialized with default values.
     *
     * @see #setNewItemProducer(Callback)
     */
    public SearchField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        historyButton = createHistoryButton();
        setGraphic(historyButton);

        popup = new SearchFieldPopup<>(this);

        editor.textProperty().bindBidirectional(textProperty());
        editor.promptTextProperty().bindBidirectional(promptTextProperty());

        // history listView placeholder
        Label placeholder = new Label("No items.");
        placeholder.getStyleClass().add("history-placeholder");
        setHistoryPlaceholder(placeholder);

        // suggestion listView placeholder
        setPlaceholder(new Label("No items found"));

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                getEditor().requestFocus();
            }
        });

        editor.focusedProperty().addListener(it -> {
            if (!isAutoCommitOnFocusLost()) {
                if (popup.isShowing()) {
                    popup.hide();
                }
                return;
            }

            if (!editor.isFocused()) {
                // Add the current text to the history if the editor lost focus.
                if (isAddingItemToHistoryOnFocusLost()) {
                    addToHistory(editor.getText());
                }
                commit();
                if (getSelectedItem() == null) {
                    editor.setText("");
                } else {
                    invokeCommitHandler();
                }
            }
        });

        addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            KeyCode keyCode = evt.getCode();

            // record the history popup showing status before hide.
            boolean lastHistoryPopupShowing = historyButton.isPopupShowing();

            // On key pressed, hide the history popup if the user pressed keys other than UP or DOWN.
            if (keyCode != KeyCode.UP && keyCode != KeyCode.DOWN) {
                historyButton.hidePopup();
            }

            boolean releasedEnter = keyCode.equals(KeyCode.ENTER);
            // Add the current text to the history if the user pressed the ENTER key.
            if (releasedEnter && isAddingItemToHistoryOnEnter() && !lastHistoryPopupShowing) {
                addToHistory(editor.getText());
            }

            if ((keyCode.equals(KeyCode.RIGHT) || releasedEnter) && !lastHistoryPopupShowing) {
                commit();
                evt.consume();
                invokeCommitHandler();
            } else if (keyCode.equals(KeyCode.LEFT)) {
                editor.positionCaret(Math.max(0, editor.getCaretPosition() - 1));
            } else if (keyCode.equals(KeyCode.ESCAPE)) {
                historyButton.hidePopup();
                cancel();
                evt.consume();
            } else if (KeyCombination.keyCombination("shortcut+a").match(evt)) {
                editor.selectAll();
                evt.consume();
            }
        });

        setMatcher((item, searchText) -> getConverter().toString(item).startsWith(searchText.toLowerCase()));

        setConverter(new StringConverter<>() {
            @Override
            public String toString(T item) {
                if (item != null) {
                    return item.toString();
                }

                return "";
            }

            @Override
            public T fromString(String s) {
                return null;
            }
        });

        setCellFactory(view -> new SearchFieldListCell<>(this));

        setComparator(Comparator.comparing(Object::toString));

        fullText.bind(Bindings.createStringBinding(() -> editor.getText() + getAutoCompletedText(), editor.textProperty(), autoCompletedText));

        editor.textProperty().addListener(it -> {
            if (!committing) {
                if (StringUtils.isNotBlank(editor.getText())) {
                    searchService.restart();
                } else {
                    update(null);
                }
            }
        });

        selectedItem.addListener(it -> {
            T selectedItem = getSelectedItem();
            if (selectedItem != null) {
                String displayName = getConverter().toString(selectedItem);
                String text = editor.getText();

                if (StringUtils.isBlank(text)) {
                    /*
                     * Looks like the "selected item" was set from outside, and not because of a search.
                     * We are using the "committing" flag so that the listener that responds to editor text
                     * property changes will not trigger a new search.
                     */
                    committing = true;
                    try {
                        editor.setText(displayName);
                    } finally {
                        committing = false;
                    }
                } else {
                    if (StringUtils.startsWithIgnoreCase(displayName, text)) {
                        autoCompletedText.set(displayName.substring(text.length()));
                    } else {
                        autoCompletedText.set("");
                    }
                }
            } else {
                autoCompletedText.set("");
            }
        });

        editor.textProperty().addListener(it -> autoCompletedText.set(""));

        converter.addListener(it -> {
            if (getConverter() == null) {
                throw new IllegalArgumentException("converter can not be null");
            }
        });

        cellFactory.addListener(it -> {
            if (getCellFactory() == null) {
                throw new IllegalArgumentException("cell factory can not be null");
            }
        });

        suggestionProvider.addListener(it -> {
            if (getSuggestionProvider() == null) {
                throw new IllegalArgumentException("suggestion provider can not be null");
            }
        });

        comparator.addListener(it -> {
            if (getComparator() == null) {
                throw new IllegalArgumentException("comparator can not be null");
            }
        });

        matcher.addListener(it -> {
            if (getMatcher() == null) {
                throw new IllegalArgumentException("matcher can not be null");
            }
        });

        RotateTransition rotateTransition = new RotateTransition();
        rotateTransition.nodeProperty().bind(busyGraphicProperty());
        rotateTransition.setCycleCount(Animation.INDEFINITE);
        rotateTransition.setByAngle(360);
        rotateTransition.setDuration(Duration.millis(500));

        searching.addListener(it -> {
            if (searching.get() && isShowSearchIcon()) {
                rotateTransition.play();
            } else {
                rotateTransition.stop();
            }
        });

        sceneProperty().addListener(it -> {
            if (getScene() == null) {
                rotateTransition.stop();
            }
        });

        searchService.setOnRunning(evt -> fireEvent(new SearchEvent(SearchEvent.SEARCH_STARTED, searchService.getText())));

        searchService.setOnSucceeded(evt -> {
            update(searchService.getValue());
            fireEvent(new SearchEvent(SearchEvent.SEARCH_FINISHED, searchService.getText()));
        });

        searching.bind(searchService.runningProperty());
    }

    private void onHistoryItemConfirmed(String historyItem) {
        if (historyItem != null) {
            int oldLen = editor.textProperty().getValueSafe().length();
            editor.replaceText(0, oldLen, historyItem);
        }
        historyButton.hidePopup();
    }

    private HistoryButton<String> createHistoryButton() {
        HistoryButton<String> historyButton = new HistoryButton<>(this);
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

    private void invokeCommitHandler() {
        T selectedItem = getSelectedItem();
        if (selectedItem != null) {
            Consumer<T> onCommit = getOnCommit();
            if (onCommit != null) {
                onCommit.accept(selectedItem);
            }
        }
    }

    private boolean committing;

    /**
     * Makes the field commit to the currently selected item and updates the
     * field to show the full text provided by the converter for the item.
     * This method can be called multiple times. For a single event
     * when the user explicitly commits to a value use the {@link #onCommitProperty()}.
     */
    public void commit() {
        committing = true;
        try {
            T selectedItem = getSelectedItem();
            if (selectedItem != null) {
                String text = getConverter().toString(selectedItem);
                if (text != null) {
                    editor.setText(text);
                    editor.positionCaret(text.length());

                    // add on commit
                    if (isAddingItemToHistoryOnCommit()) {
                        addToHistory(text);
                    }
                } else {
                    clear();
                }
            } else {
                clear();
            }

            getProperties().put("committed", "");
        } finally {
            committing = false;
        }
    }

    private void addToHistory(String text) {
        HistoryManager<String> historyManager = getHistoryManager();
        if (historyManager != null && StringUtils.isNotBlank(text)) {
            historyManager.add(text);
        }
    }

    private final ObjectProperty<Consumer<T>> onCommit = new SimpleObjectProperty<>(this, "onCommit");

    public final Consumer<T> getOnCommit() {
        return onCommit.get();
    }

    /**
     * A callback that gets invoked when the user has committed to the selected
     * value.
     * "Committing" means that the user has hit the ENTER key, or the RIGHT arrow,
     * or the field has lost its focus.
     * Or the user has clicked on one of the suggestions in the popup window.
     *
     * @return the commit handler
     */
    public final ObjectProperty<Consumer<T>> onCommitProperty() {
        return onCommit;
    }

    public void setOnCommit(Consumer<T> onCommit) {
        this.onCommit.set(onCommit);
    }

    private class SearchEventHandlerProperty extends SimpleObjectProperty<EventHandler<SearchEvent>> {

        private final EventType<SearchEvent> eventType;

        public SearchEventHandlerProperty(String name, EventType<SearchEvent> eventType) {
            super(SearchField.this, name);
            this.eventType = eventType;
        }

        @Override
        protected void invalidated() {
            setEventHandler(eventType, get());
        }
    }

    private SearchEventHandlerProperty onSearchStarted;

    /**
     * An event handler that can be used to get informed whenever the field starts a search.
     * This event gets fired often while the user is still typing as the search gets reset
     * with every keystroke.
     *
     * @return the "search started" event handler
     */
    public final ObjectProperty<EventHandler<SearchEvent>> onSearchStartedProperty() {
        if (onSearchStarted == null) {
            onSearchStarted = new SearchEventHandlerProperty("onSearchStartedProperty", SearchEvent.SEARCH_STARTED);
        }

        return onSearchStarted;
    }

    public final void setOnSearchStarted(EventHandler<SearchEvent> value) {
        onSearchStartedProperty().set(value);
    }

    public final EventHandler<SearchEvent> getOnSearchStarted() {
        return onSearchStarted == null ? null : onSearchStartedProperty().get();
    }

    private SearchEventHandlerProperty onSearchFinished;

    /**
     * An event handler that can be used to get informed whenever the field finishes a search.
     *
     * @return the "search finished" event handler
     */
    public final ObjectProperty<EventHandler<SearchEvent>> onSearchFinishedProperty() {
        if (onSearchFinished == null) {
            onSearchFinished = new SearchEventHandlerProperty("onSearchFinishedProperty", SearchEvent.SEARCH_FINISHED);
        }

        return onSearchFinished;
    }

    public final void setOnSearchFinished(EventHandler<SearchEvent> value) {
        onSearchFinishedProperty().set(value);
    }

    public final EventHandler<SearchEvent> getOnSearchFinished() {
        return onSearchFinished == null ? null : onSearchFinishedProperty().get();
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(this, "graphic", new FontIcon(MaterialDesign.MDI_MAGNIFY));

    public final Node getGraphic() {
        return graphic.get();
    }

    /**
     * Stores a node that will be shown on the field's right-hand side whenever the field is idle.
     *
     * @return the field's graphic
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    public final void setGraphic(Node graphic) {
        this.graphic.set(graphic);
    }

    private final ObjectProperty<Node> busyGraphic = new SimpleObjectProperty<>(this, "busyGraphic", new FontIcon(MaterialDesign.MDI_CACHED));

    public final Node getBusyGraphic() {
        return busyGraphic.get();
    }

    /**
     * Stores a node that will be shown on the field's right side whenever a search is ongoing.
     *
     * @return the busy graphic
     */
    public final ObjectProperty<Node> busyGraphicProperty() {
        return busyGraphic;
    }

    public final void setBusyGraphic(Node busyGraphic) {
        this.busyGraphic.set(busyGraphic);
    }

    private final ReadOnlyBooleanWrapper searching = new ReadOnlyBooleanWrapper(this, "searching");

    public final boolean isSearching() {
        return searching.get();
    }

    private final BooleanProperty hidePopupWithSingleChoice = new SimpleBooleanProperty(this, "hidePopupWithSingleChoice", false);

    public final boolean isHidePopupWithSingleChoice() {
        return hidePopupWithSingleChoice.get();
    }

    /**
     * Hides the popup window with the suggestion list if the list only contains a single
     * elements. The default is "false".
     *
     * @return true if the popup showing the list of suggestions will not appear if only a single choice is available
     */
    public final BooleanProperty hidePopupWithSingleChoiceProperty() {
        return hidePopupWithSingleChoice;
    }

    public final void setHidePopupWithSingleChoice(boolean hidePopupWithSingleChoice) {
        this.hidePopupWithSingleChoice.set(hidePopupWithSingleChoice);
    }

    private final BooleanProperty hidePopupWithNoChoice = new SimpleBooleanProperty(this, "hidePopupWithNoChoice", false);

    public final boolean isHidePopupWithNoChoice() {
        return hidePopupWithNoChoice.get();
    }

    /**
     * Determines whether to hide the popup window when there are no choices available in the suggestion list.
     * The default value is "false", indicating that the popup does not hide automatically under this condition.
     *
     * @return true if the popup should not be shown when there are no suggestions to display.
     */
    public final BooleanProperty hidePopupWithNoChoiceProperty() {
        return hidePopupWithNoChoice;
    }

    public final void setHidePopupWithNoChoice(boolean hidePopupWithNoChoice) {
        this.hidePopupWithNoChoice.set(hidePopupWithNoChoice);
    }

    /**
     * A flag indicating whether the asynchronous search is currently in progress.
     * This flag can be used to animate something that expresses that the search is
     * ongoing.
     *
     * @return true if the search is currently in progress
     */
    public final ReadOnlyBooleanProperty searchingProperty() {
        return searching.getReadOnlyProperty();
    }

    /**
     * Returns the text field control used for editing the text.
     *
     * @return the text field editor control
     */
    public final TextField getEditor() {
        return editor;
    }

    /**
     * Selects the given item and sets the editor's text to the string
     * provided by the converter for the item.
     *
     * @param item the selected item
     */
    public void select(T item) {
        setSelectedItem(item);
        commit();
    }

    private class SearchService extends Service<Collection<T>> {

        private String text;

        @Override
        protected Task<Collection<T>> createTask() {
            text = editor.getText();
            return new SearchTask(text);
        }

        public String getText() {
            return text;
        }
    }

    private class SearchTask extends Task<Collection<T>> {

        private final String searchText;

        public SearchTask(String searchText) {
            this.searchText = searchText;
        }

        @Override
        protected Collection<T> call() throws Exception {
            Thread.sleep(250);

            if (!isCancelled() && StringUtils.isNotBlank(searchText)) {
                return getSuggestionProvider().call(new SearchFieldSuggestionRequest() {
                    @Override
                    public boolean isCancelled() {
                        return SearchTask.this.isCancelled();
                    }

                    @Override
                    public String getUserText() {
                        return searchText;
                    }
                });
            }

            return Collections.emptyList();
        }
    }

    /**
     * Cancels the current search in progress.
     */
    public final void cancel() {
        searchService.cancel();
        getProperties().put("cancelled", "");
        setSelectedItem(null);
        setText("");
    }

    /**
     * Updates the control with the newly found list of suggestions. The suggestions
     * are provided by a background search service.
     *
     * @param newSuggestions the new suggestions to use for the field
     */
    protected void update(Collection<T> newSuggestions) {
        if (newSuggestions == null) {
            suggestions.clear();
            return;
        }

        suggestions.setAll(newSuggestions);

        String searchText = editor.getText();
        if (StringUtils.isNotBlank(searchText)) {
            try {
                BiFunction<T, String, Boolean> matcher = getMatcher();

                newItem.set(false);

                newSuggestions.stream().filter(item -> matcher.apply(item, searchText)).findFirst().ifPresentOrElse(selectedItem::set, () -> {
                    if (StringUtils.isNotBlank(searchText)) {
                        Callback<String, T> itemProducer = getNewItemProducer();
                        if (itemProducer != null) {
                            newItem.set(true);
                            selectedItem.set(itemProducer.call(searchText));
                        } else {
                            selectedItem.set(null);
                        }
                    } else {
                        selectedItem.set(null);
                    }
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            selectedItem.set(null);
        }
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SearchFieldSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(SearchField.class.getResource("search-field.css")).toExternalForm();
    }

    /**
     * Convenience method to invoke clear() on the text field.
     */
    public void clear() {
        getEditor().clear();
    }

    private final ListProperty<T> suggestions = new SimpleListProperty<>(this, "suggestions", FXCollections.observableArrayList());

    private final ObservableList<T> readOnlySuggestions = FXCollections.unmodifiableObservableList(suggestions);

    /**
     * Returns a read-only (unmodifiable) list of the current suggestions.
     *
     * @return the list of suggestions
     * @see #suggestionProviderProperty()
     */
    public final ObservableList<T> getSuggestions() {
        return readOnlySuggestions;
    }

    private final ReadOnlyBooleanWrapper newItem = new ReadOnlyBooleanWrapper(this, "newItem");

    public final boolean isNewItem() {
        return newItem.get();
    }

    /**
     * Determines if the selected item has been created on-the-fly via the {@link #newItemProducer}. This
     * will only ever happen if a new item producer has been specified.
     *
     * @return true if the selected item was not part of the suggestion list and has been created on-the-fly
     */
    public final ReadOnlyBooleanProperty newItemProperty() {
        return newItem.getReadOnlyProperty();
    }

    private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    public final Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    /**
     * A cell factory that can be used by a list view to visualize the list of suggestions.
     *
     * @return the cell factory used by the suggestion list view
     * @see #getSuggestions()
     */
    public final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public final void setCellFactory(Callback<ListView<T>, ListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>(this, "comparator");

    public final Comparator<T> getComparator() {
        return comparator.get();
    }

    /**
     * A comparator used to sort the list of suggestions. The field will try to find a first best match
     * inside the sorted list. Internally the control uses an "inner" comparator to ensure that suggestions
     * appear based on the entered text, which means that a perfect match will always show up first and then
     * the suggests that "start" with the search string.
     *
     * @return the sorting comparator used for the suggestions list
     */
    public final ObjectProperty<Comparator<T>> comparatorProperty() {
        return comparator;
    }

    public final void setComparator(Comparator<T> comparator) {
        this.comparator.set(comparator);
    }

    private final ObjectProperty<Callback<String, T>> newItemProducer = new SimpleObjectProperty<>(this, "newItemProducer");

    public final Callback<String, T> getNewItemProducer() {
        return newItemProducer.get();
    }

    /**
     * A callback used for creating a new object on-the-fly if no item matches the search
     * text.
     *
     * @return the callback for producing a new object of the field supported object type
     */
    public final ObjectProperty<Callback<String, T>> newItemProducerProperty() {
        return newItemProducer;
    }

    public final void setNewItemProducer(Callback<String, T> newItemProducer) {
        this.newItemProducer.set(newItemProducer);
    }

    private final DoubleProperty autoCompletionGap = new SimpleDoubleProperty(this, "autoCompletionGap", 1);

    public final double getAutoCompletionGap() {
        return autoCompletionGap.get();
    }

    /**
     * Defines the gap (in pixels) between the user typed text and the autocompleted text.
     *
     * @return the gap (in pixels) between the user typed text and the autocompleted text
     */
    public final DoubleProperty autoCompletionGapProperty() {
        return autoCompletionGap;
    }

    public final void setAutoCompletionGap(double autoCompletionGap) {
        this.autoCompletionGap.set(autoCompletionGap);
    }

    private final ReadOnlyStringWrapper fullText = new ReadOnlyStringWrapper(this, "fullText");

    public final String getFullText() {
        return fullText.get();
    }

    /**
     * A read-only property containing the concatenation of the regular text of the text field and
     * the autocompleted text.
     *
     * @return the full text shown by the text field
     * @see #getText()
     * @see #getAutoCompletedText()
     */
    public final ReadOnlyStringProperty fullTextProperty() {
        return fullText.getReadOnlyProperty();
    }

    private final StringProperty text = new SimpleStringProperty(this, "text", "");

    public final String getText() {
        return text.get();
    }

    /**
     * A convenience property bound to the editor's text property.
     *
     * @return the text shown by the field
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    private final StringProperty promptText = new SimpleStringProperty(this, "promptText", "");

    public final String getPromptText() {
        return promptText.get();
    }

    /**
     * A convenience property to set the prompt text shown by the text field when no text
     * has been entered yet (e.g. "Search ...").
     *
     * @return the prompt text
     * @see TextField#promptTextProperty()
     */
    public final StringProperty promptTextProperty() {
        return promptText;
    }

    public final void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    private final ReadOnlyStringWrapper autoCompletedText = new ReadOnlyStringWrapper(this, "autoCompletedText");

    public final String getAutoCompletedText() {
        return autoCompletedText.get();
    }

    /**
     * A read-only property containing the automatically completed text. This
     * property is completely managed by the control.
     *
     * @return the auto-completed text (e.g. "ates" after the user entered "United St" in a country search field).
     */
    public final ReadOnlyStringProperty autoCompletedTextProperty() {
        return autoCompletedText.getReadOnlyProperty();
    }

    private final ObjectProperty<BiFunction<T, String, Boolean>> matcher = new SimpleObjectProperty<>(this, "matcher");

    public final BiFunction<T, String, Boolean> getMatcher() {
        return matcher.get();
    }

    /**
     * The function that is used to determine the first item in the suggestion list that is a good match
     * for auto selection. This is normally the case if the text provided by the converter for an item starts
     * with exactly the text typed by the user. Auto selection will cause the field to automatically complete
     * the text typed by the user with the name of the match.
     *
     * @return the function used for determining the best match in the suggestion list
     * @see #converterProperty()
     */
    public final ObjectProperty<BiFunction<T, String, Boolean>> matcherProperty() {
        return matcher;
    }

    public final void setMatcher(BiFunction<T, String, Boolean> matcher) {
        this.matcher.set(matcher);
    }

    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");

    /**
     * Contains the currently selected item.
     *
     * @return the selected item
     */
    public final ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    public final T getSelectedItem() {
        return selectedItem.get();
    }

    public final void setSelectedItem(T selectedItem) {
        this.selectedItem.set(selectedItem);
    }

    private final ObjectProperty<Callback<SearchFieldSuggestionRequest, Collection<T>>> suggestionProvider = new SimpleObjectProperty<>(this, "suggestionProvider");

    public final Callback<SearchFieldSuggestionRequest, Collection<T>> getSuggestionProvider() {
        return suggestionProvider.get();
    }

    /**
     * A callback used for looking up a list of suggestions for the current search text.
     *
     * @return #getSuggestions
     */
    public final ObjectProperty<Callback<SearchFieldSuggestionRequest, Collection<T>>> suggestionProviderProperty() {
        return suggestionProvider;
    }

    public final void setSuggestionProvider(Callback<SearchFieldSuggestionRequest, Collection<T>> suggestionProvider) {
        this.suggestionProvider.set(suggestionProvider);
    }

    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter");

    public final StringConverter<T> getConverter() {
        return converter.get();
    }

    /**
     * A converter for turning the objects returned by the suggestion provider into text.
     *
     * @return the converter for turning the objects returned by the suggestion provider into text
     */
    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<T> converter) {
        this.converter.set(converter);
    }

    // --- Placeholder Node
    private ObjectProperty<Node> placeholder;

    /**
     * The placeholder UI when no suggestions have been returned by the suggestion
     * provider.
     *
     * @return the placeholder property for the list view of the auto suggest popup
     */
    public final ObjectProperty<Node> placeholderProperty() {
        if (placeholder == null) {
            placeholder = new SimpleObjectProperty<>(this, "placeholder");
        }
        return placeholder;
    }

    public final void setPlaceholder(Node value) {
        placeholderProperty().set(value);
    }

    public final Node getPlaceholder() {
        return placeholder == null ? null : placeholder.get();
    }

    private BooleanProperty autoCommitOnFocusLost;

    /**
     * Returns the BooleanProperty that indicates if text should auto-commit when the field loses focus.
     * The property is lazy-initialized and defaults to true, enabling auto-commit by default.
     *
     * @return the BooleanProperty for autoCommitOnFocusLost.
     */
    public final BooleanProperty autoCommitOnFocusLostProperty() {
        if (autoCommitOnFocusLost == null) {
            autoCommitOnFocusLost = new SimpleBooleanProperty(this, "autoCommitOnFocusLost", true);
        }
        return autoCommitOnFocusLost;
    }

    /**
     * Checks if the auto-commit on focus lost feature is enabled.
     *
     * @return true if auto-commit on focus lost is enabled, false otherwise.
     */
    public final boolean isAutoCommitOnFocusLost() {
        return autoCommitOnFocusLost == null || autoCommitOnFocusLost.get();
    }

    /**
     * Sets the value of the autoCommitOnFocusLost property.
     *
     * @param value if true, enables auto-commit on focus lost; if false, disables it.
     */
    public final void setAutoCommitOnFocusLost(boolean value) {
        autoCommitOnFocusLostProperty().set(value);
    }

    /**
     * Represents a suggestion fetch request.
     */
    public interface SearchFieldSuggestionRequest {

        /**
         * Is this request canceled?
         *
         * @return {@code true} if the request is canceled, otherwise {@code false}
         */
        boolean isCancelled();

        /**
         * Get the user text to which suggestions shall be found
         *
         * @return {@link String} containing the user text
         */
        String getUserText();
    }

    /**
     * An event type used by the {@link SearchField} to indicate the start and
     * end of searching operations.
     *
     * @see SearchField#setOnSearchStarted(EventHandler)
     * @see SearchField#setOnSearchFinished(EventHandler)
     */
    public static class SearchEvent extends Event {

        /**
         * An event that gets fired when the field starts a search.
         */
        public static final EventType<SearchEvent> SEARCH_STARTED = new EventType<>(Event.ANY, "SEARCH_STARTED");

        /**
         * An event that gets fired when the field finishes a search.
         */
        public static final EventType<SearchEvent> SEARCH_FINISHED = new EventType<>(Event.ANY, "SEARCH_FINISHED");

        private final String text;

        public SearchEvent(EventType<? extends SearchEvent> eventType, String text) {
            super(eventType);
            this.text = text;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("eventType", eventType)
                    .append("target", target)
                    .append("consumed", consumed)
                    .append("text", text)
                    .toString();
        }
    }

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    public final Node getLeft() {
        return left.get();
    }

    /**
     * A custom node that can be shown on the left-hand side of the field.
     *
     * @return a custom node for the left-hand side (e.g. "clear" button)
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final void setLeft(Node left) {
        this.left.set(left);
    }

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "left");

    public final Node getRight() {
        return right.get();
    }

    /**
     * A custom node that can be shown on the right-hand side of the field.
     *
     * @return a custom node for the right-hand side (e.g. "clear" button)
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    public final void setRight(Node right) {
        this.right.set(right);
    }

    private final BooleanProperty showSearchIcon = new SimpleBooleanProperty(this, "showSearchIcon", true);

    public final boolean isShowSearchIcon() {
        return showSearchIcon.get();
    }

    /**
     * Determines if the field will show an icon on the right-hand side which indicates
     * that the field is a search field.
     *
     * @return true if a search icon will be shown
     */
    public final BooleanProperty showSearchIconProperty() {
        return showSearchIcon;
    }

    public final void setShowSearchIcon(boolean showSearchIcon) {
        this.showSearchIcon.set(showSearchIcon);
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

    // add on commit

    private BooleanProperty addingItemToHistoryOnCommit;

    /**
     * Determines whether the text of the text field should be added to the history when the user commits to a value.
     *
     * @return true if the text should be added to the history on commit, false otherwise
     */
    public final BooleanProperty addingItemToHistoryOnCommitProperty() {
        if (addingItemToHistoryOnCommit == null) {
            addingItemToHistoryOnCommit = new SimpleBooleanProperty(this, "addingItemToHistoryOnCommit", DEFAULT_ADDING_ITEM_TO_HISTORY_ON_COMMIT);
        }
        return addingItemToHistoryOnCommit;
    }

    public final boolean isAddingItemToHistoryOnCommit() {
        return addingItemToHistoryOnCommit == null ? DEFAULT_ADDING_ITEM_TO_HISTORY_ON_COMMIT : addingItemToHistoryOnCommit.get();
    }

    public final void setAddingItemToHistoryOnCommit(boolean addingItemToHistoryOnCommit) {
        addingItemToHistoryOnCommitProperty().set(addingItemToHistoryOnCommit);
    }

    private ObjectProperty<HistoryManager<String>> historyManager;

    /**
     * The history manager that is used to manage the history of the SearchField.
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

    /**
     * A custom list cell implementation that is capable of underlining the part
     * of the text that matches the user-typed search text. The cell uses a text flow
     * node that is composed of three text nodes. One of the text nodes will be underlined
     * and represents the user search text.
     *
     * @param <T> the type of the cell
     */
    public static class SearchFieldListCell<T> extends ListCell<T> {

        private final SearchField<T> searchField;

        private final Text text1 = new Text();
        private final Text text2 = new Text();
        private final Text text3 = new Text();

        public SearchFieldListCell(SearchField<T> searchField) {
            this.searchField = searchField;

            getStyleClass().add("search-field-list-cell");

            TextFlow textFlow = new TextFlow();
            textFlow.getChildren().setAll(text1, text2, text3);

            text1.getStyleClass().addAll("text", "start");
            text2.getStyleClass().addAll("text", "middle");
            text3.getStyleClass().addAll("text", "end");

            setGraphic(textFlow);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                String cellText = searchField.getConverter().toString(item);
                String text = searchField.getEditor().getText();
                int index = cellText.toLowerCase().indexOf(text.toLowerCase());
                if (index >= 0) {
                    text1.setText(cellText.substring(0, index));
                    text2.setText(cellText.substring(index, index + text.length()));
                    text3.setText(cellText.substring(index + text.length()));
                } else {
                    text1.setText(cellText);
                    text2.setText("");
                    text3.setText("");
                }
            } else {
                text1.setText("");
                text2.setText("");
                text3.setText("");
            }
        }

    }

    public final SearchFieldPopup<T> getPopup() {
        return popup;
    }

}
