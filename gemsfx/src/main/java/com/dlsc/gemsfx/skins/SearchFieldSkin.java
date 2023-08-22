package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.scene.control.SkinBase;

public class SearchFieldSkin<T> extends SkinBase<SearchField<T>> {

    public SearchFieldSkin(SearchField<T> searchField) {
        super(searchField);
        searchField.getEditor().setSkin(new SearchFieldEditorSkin<>(searchField));
        getChildren().add(searchField.getEditor());
    }
}
