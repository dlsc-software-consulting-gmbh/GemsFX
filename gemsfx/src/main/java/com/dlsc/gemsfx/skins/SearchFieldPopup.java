//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.SearchField;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import com.dlsc.gemsfx.util.StringUtils;

import java.util.Objects;

/**
 * Popup control used by {@link SearchField} to display suggestions.
 * <p>
 * The popup tracks the owning search field, sizes itself to the field, and
 * delegates its visual content to {@link SearchFieldPopupSkin}.
 *
 * @param <T> the suggestion item type
 */
public class SearchFieldPopup<T> extends CustomPopupControl {

    /**
     * The default style class for the popup.
     */
    public static final String DEFAULT_STYLE_CLASS = "search-field-popup";

    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private final SearchField<T> searchField;

    /**
     * Creates a new popup for the given search field.
     *
     * @param searchField the search field that owns this popup
     */
    public SearchFieldPopup(SearchField<T> searchField) {
        this.searchField = Objects.requireNonNull(searchField);

        minWidthProperty().bind(searchField.widthProperty());

        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        MapChangeListener<? super Object, ? super Object> l = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("committed") || change.getKey().equals("cancelled")) {
                    hide();
                    searchField.getProperties().remove("committed");
                    searchField.getProperties().remove("cancelled");
                }
            }
        };

        searchField.getProperties().addListener(l);

        searchField.addEventHandler(SearchField.SearchEvent.SEARCH_FINISHED, evt -> {
            if ((!searchField.getSuggestions().isEmpty() || searchField.getPlaceholder() != null) && StringUtils.isNotBlank(searchField.getEditor().getText())) {

                // assuming that we don't have to show it
                boolean showIt = false;
                int suggestionsItemsSize = searchField.getSuggestions().size();
                if (suggestionsItemsSize == 0) {
                    if (!searchField.isHidePopupWithNoChoice()) {
                        showIt = true;
                    }
                } else if (suggestionsItemsSize == 1) {
                    if (!searchField.isHidePopupWithSingleChoice() || !searchField.getMatcher().apply(searchField.getSuggestions().get(0), evt.getText())) {
                        showIt = true;
                    }
                } else {
                    // more than one suggested item, definitely show the popup
                    showIt = true;
                }

                if (showIt) {
                    show(searchField);
                    selectFirstSuggestion();
                } else {
                    hide();
                }
            } else {
                hide();
            }
        });
    }

    /**
     * Returns the search field that owns this popup.
     *
     * @return the owning search field
     */
    public SearchField<T> getSearchField() {
        return searchField;
    }

    /**
     * Returns this popup's observable suggestions list.
     * <p>
     * The default popup skin displays suggestions from the owning search field.
     *
     * @return this popup's suggestions list
     */
    public ObservableList<T> getSuggestions() {
        return suggestions;
    }

    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion() {
        @SuppressWarnings("rawtypes")
        SearchFieldPopupSkin skin = (SearchFieldPopupSkin) getSkin();
        ListView<?> listView = (ListView<?>) skin.getNode();
        if (listView.getItems() != null && !listView.getItems().isEmpty()) {
            listView.getSelectionModel().select(0);
        }
    }

    /**
     * Creates the default skin for this popup.
     *
     * @return the default popup skin
     */
    protected Skin<?> createDefaultSkin() {
        return new SearchFieldPopupSkin<>(this);
    }
}
