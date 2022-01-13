//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins.autocomplete;

import com.dlsc.gemsfx.SpotlightTextField;
import com.dlsc.gemsfx.skins.autocomplete.AutoCompletePopup.SuggestionEvent;
import javafx.beans.binding.Bindings;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.input.MouseButton;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AutoCompletePopupSkin<T> implements Skin<AutoCompletePopup<T>> {

    private final AutoCompletePopup<T> control;
    private final ListView<T> suggestionList;
    final int LIST_CELL_HEIGHT;

    public AutoCompletePopupSkin(AutoCompletePopup<T> control) {
        this(control, control.getConverter());
    }

    public AutoCompletePopupSkin(AutoCompletePopup<T> control, StringConverter<T> displayConverter) {
        this(control, TextFieldListCell.forListView(displayConverter));
    }

    public AutoCompletePopupSkin(AutoCompletePopup<T> control, Callback<ListView<T>, ListCell<T>> cellFactory) {
        LIST_CELL_HEIGHT = 24;
        this.control = control;
        suggestionList = new ListView(control.getSuggestions());
        suggestionList.getStyleClass().add("auto-completion-list-view");
        suggestionList.getStylesheets().add(SpotlightTextField.class.getResource("auto-completion-list-view.css").toExternalForm());
        suggestionList.prefHeightProperty().bind(Bindings.min(control.visibleRowCountProperty(), Bindings.size(suggestionList.getItems())).multiply(24).add(18));
        suggestionList.setCellFactory(cellFactory);
        suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
        suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
        suggestionList.minWidthProperty().bind(control.minWidthProperty());
        registerEventListener();
    }

    private void registerEventListener() {
        suggestionList.setOnMouseClicked((me) -> {
            if (me.getButton() == MouseButton.PRIMARY) {
                onSuggestionChosen(suggestionList.getSelectionModel().getSelectedItem());
            }

        });
        suggestionList.setOnKeyPressed((ke) -> {
            switch (ke.getCode()) {
                case TAB:
                case ENTER:
                    onSuggestionChosen(suggestionList.getSelectionModel().getSelectedItem());
                    break;
                case ESCAPE:
                    if (control.isHideOnEscape()) {
                        control.hide();
                    }
            }

        });
    }

    private void onSuggestionChosen(T suggestion) {
        if (suggestion != null) {
            Event.fireEvent(control, new SuggestionEvent(suggestion));
        }
    }

    public Node getNode() {
        return suggestionList;
    }

    public AutoCompletePopup<T> getSkinnable() {
        return control;
    }

    public void dispose() {
    }
}
