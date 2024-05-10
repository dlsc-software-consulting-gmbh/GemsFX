package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchHistorySupport;
import com.dlsc.gemsfx.SearchField;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.MouseButton;

import java.util.Objects;

public class SearchHistoryPopupSkin implements Skin<SearchHistoryPopup> {

    private final SearchHistoryPopup control;
    private final SearchHistorySupport historySupport;
    private ListView<String> listView;

    public SearchHistoryPopupSkin(SearchHistoryPopup control) {
        this.control = control;
        historySupport = control.getHistorySupport();

        initListView();
    }

    private void initListView() {
        listView = new ListView<>() {
            @Override
            public String getUserAgentStylesheet() {
                return Objects.requireNonNull(SearchField.class.getResource("search-history-popup.css")).toExternalForm();
            }
        };
        listView.getStyleClass().add("search-history-list-view");

        Bindings.bindContent(listView.getItems(), historySupport.getUnmodifiableHistory());

        listView.cellFactoryProperty().bind(historySupport.historyCellFactoryProperty());
        listView.placeholderProperty().bind(historySupport.historyPlaceholderProperty());

        listView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
                selectHistoryItem();
            }
        });

        listView.setOnKeyPressed(keyEvent -> {
            switch (keyEvent.getCode()) {
                case ENTER -> selectHistoryItem();
                case ESCAPE -> control.hide();
            }
        });
    }

    private void selectHistoryItem() {
        String selectedHistory = listView.getSelectionModel().getSelectedItem();
        if (selectedHistory != null) {
            // replace text
            TextInputControl textInputControl = historySupport.getTextInputControl();
            int oldTextLen = textInputControl.textProperty().getValueSafe().length();
            textInputControl.replaceText(0, oldTextLen, selectedHistory);

            // hide popup
            control.hide();
        }
    }

    public Node getNode() {
        return listView;
    }

    public SearchHistoryPopup getSkinnable() {
        return control;
    }

    public void dispose() {
        Bindings.unbindContent(listView.getItems(), historySupport.getUnmodifiableHistory());

        listView.prefWidthProperty().unbind();
        listView.maxWidthProperty().unbind();
        listView.minWidthProperty().unbind();

        listView.cellFactoryProperty().unbind();
        listView.placeholderProperty().unbind();
    }

}
