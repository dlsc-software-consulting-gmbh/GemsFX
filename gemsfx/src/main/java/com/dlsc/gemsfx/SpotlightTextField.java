package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SpotlightTextFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.function.BiFunction;

/**
 * The spotlight text field is a standard text field with auto suggest capabilities
 * and a selection model for a specific type of object. This type is defined by the
 * generic type argument. The main difference to other auto suggest text fields is that
 * the main outcome of this field is an object and not just the text entered by the
 * user. Another difference is how the text field automatically finds and selects the
 * first object that matches the text typed by the user so far. A third feature of
 * this control is its ability to create new instances of the specified object type if
 * no matching object can be found in the list of objects returned by the suggestion
 * provider. This last feature allows an application to let the user either pick an
 * existing object or to create a new one on-the-fly (but only if a new item producer
 * has been set).
 *
 * <h3>Matcher</h3>
 *
 * @param <T> the type of objects to work on
 * @see #setConverter(StringConverter)
 * @see #setCellFactory(Callback)
 * @see #setMatcher(BiFunction)
 * @see #setNewItemProducer(Callback)
 * @see #setComparator(Comparator)
 */
public class SpotlightTextField<T> extends TextField {

    private static final String DEFAULT_STYLE_CLASS = "spotlight-text-field";

    private final SearchService searchService = new SearchService();


    /**
     * Constructs a new spotlight field. The field will set defaults for the
     * matcher, the converter, the cell factory, and the comparator. It will
     * not set a default for the "new item" producer.
     *
     * @see #setNewItemProducer(Callback)
     */
    public SpotlightTextField() {
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        setPlaceholder(new Label("No items found"));

        focusedProperty().addListener(it -> {
            if (!isFocused() && getSelectedItem() == null) {
                setText("");
            }
        });

        addEventFilter(KeyEvent.ANY, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT)) {
                setText(getFullText());
                positionCaret(getFullText().length());
                evt.consume();
            }
        });

        setMatcher((item, searchText) -> item.toString().toLowerCase().startsWith(searchText.toLowerCase()));

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

        setCellFactory(listView -> new ListCell<>() {
            {
                getStyleClass().add("spotlight-list-cell");
                setPrefWidth(0);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (item != null && !empty) {
                    setText(getConverter().toString(item));
                } else {
                    setText("");
                }
            }
        });
        setComparator(Comparator.comparing(Object::toString));

        fullText.bind(Bindings.createStringBinding(() -> getText() + getAutoCompletedText(), textProperty(), autoCompletedText));

        textProperty().addListener(it -> searchService.restart());

        selectedItem.addListener(it -> {
            T selectedItem = getSelectedItem();
            if (selectedItem != null) {
                String displayName = getConverter().toString(selectedItem);
                String text = getText();
                if (StringUtils.startsWithIgnoreCase(displayName, text)) {
                    autoCompletedText.set(displayName.substring(text.length()));
                } else {
                    autoCompletedText.set("");
                }
            } else {
                autoCompletedText.set("");
            }
        });

        textProperty().addListener(it -> autoCompletedText.set(""));

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

        searchService.setOnSucceeded(evt -> updateView(searchService.getValue()));
    }

    private class SearchService extends Service<Collection<T>> {

        @Override
        protected Task<Collection<T>> createTask() {
            return new SearchTask(getText());
        }
    }

    private class SearchTask extends Task<Collection<T>> {

        private final String searchText;

        public SearchTask(String searchText) {
            this.searchText = searchText;
        }

        @Override
        protected Collection<T> call() throws Exception {
            Thread.sleep(Double.valueOf(getSearchDelay().toMillis()).intValue());

            if (!isCancelled() && StringUtils.isNotBlank(searchText)) {

                return getSuggestionProvider().call(new ISpotlightSuggestionRequest() {
                    @Override
                    public boolean isCancelled() {
                        return false;
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

    private final ObjectProperty<Duration> searchDelay = new SimpleObjectProperty<>(this, "searchDelay", Duration.millis(200));

    public Duration getSearchDelay() {
        return searchDelay.get();
    }

    public ObjectProperty<Duration> searchDelayProperty() {
        return searchDelay;
    }

    public void setSearchDelay(Duration searchDelay) {
        this.searchDelay.set(searchDelay);
    }

    private void updateView(Collection<T> items) {
        if (items == null) {
            return;
        }

        String searchText = getText();
        if (StringUtils.isNotBlank(searchText)) {
            try {
                BiFunction<T, String, Boolean> matcher = getMatcher();

                newItem.set(false);

                items.stream().filter(item -> matcher.apply(item, searchText)).findFirst().ifPresentOrElse(item -> {
                    selectedItem.set(null);
                    selectedItem.set(item);
                }, () -> {
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
        return new SpotlightTextFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return SpotlightTextField.class.getResource("spotlight-text-field.css").toExternalForm();
    }

    private final ReadOnlyBooleanWrapper newItem = new ReadOnlyBooleanWrapper(this, "newItem");

    public boolean isNewItem() {
        return newItem.get();
    }

    public ReadOnlyBooleanProperty newItemProperty() {
        return newItem.getReadOnlyProperty();
    }

    private final ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactory = new SimpleObjectProperty<>(this, "cellFactory");

    public Callback<ListView<T>, ListCell<T>> getCellFactory() {
        return cellFactory.get();
    }

    public ObjectProperty<Callback<ListView<T>, ListCell<T>>> cellFactoryProperty() {
        return cellFactory;
    }

    public void setCellFactory(Callback<ListView<T>, ListCell<T>> cellFactory) {
        this.cellFactory.set(cellFactory);
    }

    private final ObjectProperty<Comparator<T>> comparator = new SimpleObjectProperty<>(this, "comparator");

    public final Comparator<T> getComparator() {
        return comparator.get();
    }

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

    public final ReadOnlyStringProperty fullTextProperty() {
        return fullText.getReadOnlyProperty();
    }

    private final ReadOnlyStringWrapper autoCompletedText = new ReadOnlyStringWrapper(this, "autoCompletedText");

    public final String getAutoCompletedText() {
        return autoCompletedText.get();
    }

    public final ReadOnlyStringProperty autoCompletedTextProperty() {
        return autoCompletedText.getReadOnlyProperty();
    }

    private final ObjectProperty<BiFunction<T, String, Boolean>> matcher = new SimpleObjectProperty<>(this, "matcher");

    public final BiFunction<T, String, Boolean> getMatcher() {
        return matcher.get();
    }

    public final ObjectProperty<BiFunction<T, String, Boolean>> matcherProperty() {
        return matcher;
    }

    public final void setMatcher(BiFunction<T, String, Boolean> matcher) {
        this.matcher.set(matcher);
    }

    private final ObjectProperty<T> selectedItem = new SimpleObjectProperty<>(this, "selectedItem");

    public final T getSelectedItem() {
        return selectedItem.get();
    }

    public final void setSelectedItem(T selectedItem) {
        this.selectedItem.set(selectedItem);
    }

    public final ObjectProperty<T> selectedItemProperty() {
        return selectedItem;
    }

    private final ObjectProperty<Callback<ISpotlightSuggestionRequest, Collection<T>>> suggestionProvider = new SimpleObjectProperty<>(this, "suggestionProvider");

    public final Callback<ISpotlightSuggestionRequest, Collection<T>> getSuggestionProvider() {
        return suggestionProvider.get();
    }

    public final ObjectProperty<Callback<ISpotlightSuggestionRequest, Collection<T>>> suggestionProviderProperty() {
        return suggestionProvider;
    }

    public final void setSuggestionProvider(Callback<ISpotlightSuggestionRequest, Collection<T>> suggestionProvider) {
        this.suggestionProvider.set(suggestionProvider);
    }

    private final ObjectProperty<StringConverter<T>> converter = new SimpleObjectProperty<>(this, "converter");

    public final StringConverter<T> getConverter() {
        return converter.get();
    }

    public final ObjectProperty<StringConverter<T>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<T> converter) {
        this.converter.set(converter);
    }

    // --- Placeholder Node
    private ObjectProperty<Node> placeholder;

    /**
     * This Node is shown to the user when the listview has no content to show.
     * This may be the case because the list model has no data in the first
     * place or that a filter has been applied to the list model, resulting
     * in there being nothing to show the user.
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

    /**
     * Represents a suggestion fetch request
     */
    public interface ISpotlightSuggestionRequest {

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
}
