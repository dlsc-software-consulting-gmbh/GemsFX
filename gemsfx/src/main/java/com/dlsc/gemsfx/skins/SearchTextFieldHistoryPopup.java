package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.SearchTextField;
import javafx.scene.control.Skin;

import java.util.Objects;

public class SearchTextFieldHistoryPopup extends CustomPopupControl {

    public static final String DEFAULT_STYLE_CLASS = "search-text-field-history-popup";

    private final SearchTextField searchTextField;

    public SearchTextFieldHistoryPopup(SearchTextField searchTextField) {
        this.searchTextField = Objects.requireNonNull(searchTextField);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        maxWidthProperty().bind(searchTextField.widthProperty());

        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);
    }

    protected Skin<?> createDefaultSkin() {
        return new SearchTextFieldHistoryPopupSkin(this);
    }

    public SearchTextField getSearchTextField() {
        return searchTextField;
    }

}
