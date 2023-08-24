//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.beans.property.BooleanProperty;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;

import java.util.Comparator;

public class SearchFieldPopupSkin<T> implements Skin<SearchFieldPopup<T>> {

    private final SearchFieldPopup<T> control;
    private final ListView<T> suggestionList;
    private final SearchField<T> searchField;
    private final BooleanProperty shouldCommit;

    public SearchFieldPopupSkin(SearchFieldPopup<T> control, BooleanProperty shouldCommit) {
        this.control = control;
        this.shouldCommit = shouldCommit;

        searchField = control.getSearchField();

        SortedList<T> sortedList = new SortedList<>(searchField.getSuggestions(), createInnerComparator());

        suggestionList = new ListView<>(sortedList);
        suggestionList.getStyleClass().add("search-field-list-view");
        suggestionList.getStylesheets().add(SearchField.class.getResource("search-field.css").toExternalForm());
        suggestionList.cellFactoryProperty().bind(searchField.cellFactoryProperty());

        suggestionList.prefWidthProperty().bind(control.prefWidthProperty());
        suggestionList.maxWidthProperty().bind(control.maxWidthProperty());
        suggestionList.minWidthProperty().bind(control.minWidthProperty());

        suggestionList.placeholderProperty().bind(searchField.placeholderProperty());

        suggestionList.getSelectionModel().selectedItemProperty().addListener(it -> control.getSearchField().setSelectedItem(suggestionList.getSelectionModel().getSelectedItem()));
        registerEventListener();
    }

    /*
     * We use an inner comparator because for the proper functioning of the auto suggest
     * behaviour we have to make sure that the currently selected item will always show
     * up as the first item in the list.
     */
    private Comparator<T> createInnerComparator() {
        return (o1, o2) -> {
            Comparator<T> comparator = searchField.getComparator();
            int result = comparator.compare(o1, o2);

            // make sure to always show the currently selected item on top of the list
            T selectedItem = searchField.getSelectedItem();
            if (selectedItem != null) {
                if (o1.equals(selectedItem)) {
                    result = -1;
                }
                if (selectedItem.equals(o2)) {
                    result = +1;
                }
            }

            // prefer the suggestions that start with the search term
            StringConverter<T> converter = searchField.getConverter();
            String searchText = searchField.getText().toLowerCase();

            String text1 = converter.toString(o1).toLowerCase();
            String text2 = converter.toString(o2).toLowerCase();

            if (text1.startsWith(searchText) && text2.startsWith(searchText)) {
                return text1.compareTo(text2);
            }

            if (text1.startsWith(searchText)) {
                result = -1;
            }

            if (text2.startsWith(searchText)) {
                result = +1;
            }

            return result;
        };
    }

    private void registerEventListener() {
        suggestionList.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            shouldCommit.set(true);
            if (event.getCode() == KeyCode.ENTER || event.getCode() == KeyCode.TAB) {
                fireSuggestionSelected();
            }
        });

        suggestionList.setOnMouseClicked((me) -> {
            if (me.getButton() == MouseButton.PRIMARY) {
                if (me.getClickCount() == 2) {
                    // hide the popup on double click
                    control.hide();
                } else if (me.getClickCount() == 1) {
                    shouldCommit.set(true);
                    selectItem();
                    fireSuggestionSelected();
                }
            }
        });

        suggestionList.setOnKeyPressed((ke) -> {
            switch (ke.getCode()) {
                case TAB:
                case ENTER:
                    selectItem();
                    control.hide();
                    break;
                case ESCAPE:
                    if (control.isHideOnEscape()) {
                        control.hide();
                    }
            }
        });

        control.getSearchField().getEditor().focusedProperty().addListener((it, oldFocused, newFocused) -> {
            if (!newFocused) {
                control.hide();
            }
        });

    }

    private void fireSuggestionSelected() {
        Object selectedSuggestion = suggestionList.getSelectionModel().getSelectedItem();
        if (selectedSuggestion != null) {
            SearchField.SearchEvent searchEvent = SearchField.SearchEvent.createEventForSuggestion(selectedSuggestion);
            searchField.fireEvent(searchEvent);
        }
    }

    private void selectItem() {
        T selectedItem = suggestionList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            searchField.select(selectedItem);
            searchField.commit();
        }
    }

    public Node getNode() {
        return suggestionList;
    }

    public SearchFieldPopup<T> getSkinnable() {
        return control;
    }

    public void dispose() {
    }
}
