package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.scene.control.SkinBase;

public class SearchFieldSkin<T> extends SkinBase<SearchField<T>> {

    public SearchFieldSkin(SearchField<T> spotlightTextField) {
        super(spotlightTextField);

        spotlightTextField.getEditor().setSkin(new SearchFieldEditorSkin<>(spotlightTextField));

        getChildren().add(spotlightTextField.getEditor());
    }
}
