package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;

public class SearchFieldSkin<T> extends GemsSkinBase<SearchField<T>> {

    public SearchFieldSkin(SearchField<T> searchField) {
        super(searchField);
        searchField.getEditor().setSkin(new SearchFieldEditorSkin<>(searchField));
        getChildren().add(searchField.getEditor());
    }
}
