package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;

/**
 * Skin for {@link SearchField}.
 * <p>
 * The skin installs a {@link SearchFieldEditorSkin} on the embedded editor and
 * uses that editor as the visual content of the control.
 *
 * @param <T> the suggestion item type
 */
public class SearchFieldSkin<T> extends GemsSkinBase<SearchField<T>> {

    /**
     * Creates a new skin for the given search field.
     *
     * @param searchField the search field to skin
     */
    public SearchFieldSkin(SearchField<T> searchField) {
        super(searchField);
        searchField.getEditor().setSkin(new SearchFieldEditorSkin<>(searchField));
        getChildren().add(searchField.getEditor());
    }
}
