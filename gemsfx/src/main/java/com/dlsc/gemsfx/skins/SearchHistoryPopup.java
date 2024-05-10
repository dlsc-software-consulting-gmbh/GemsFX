package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.SearchHistorySupport;
import javafx.scene.control.Skin;

import java.util.Objects;

public class SearchHistoryPopup extends CustomPopupControl {

    public static final String DEFAULT_STYLE_CLASS = "search-history-popup";

    private final SearchHistorySupport historySupport;

    public SearchHistoryPopup(SearchHistorySupport historySupport) {
        this.historySupport = Objects.requireNonNull(historySupport);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        maxWidthProperty().bind(this.historySupport.getNode().widthProperty());

        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);
    }

    protected Skin<?> createDefaultSkin() {
        return new SearchHistoryPopupSkin(this);
    }

    public final SearchHistorySupport getHistorySupport() {
        return historySupport;
    }

}
