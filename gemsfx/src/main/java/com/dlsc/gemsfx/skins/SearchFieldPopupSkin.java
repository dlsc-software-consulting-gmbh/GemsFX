//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.util.StringConverter;

import java.util.Comparator;

public class SearchFieldPopupSkin<T> implements Skin<SearchFieldPopup<T>> {

    private final SearchFieldPopup<T> control;
    private final ListView<T> suggestionList;
    private final SearchField<T> searchField;

    public SearchFieldPopupSkin(SearchFieldPopup<T> control) {
        this.control = control;

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

            if (converter.toString(o1).toLowerCase().startsWith(searchText)) {
                result = +1;
            }

            if (converter.toString(o2).toLowerCase().startsWith(searchText)) {
                result = -1;
            }

            return result;
        };
    }

    private void registerEventListener() {
        suggestionList.setOnMouseClicked((me) -> {
            if (me.getButton() == MouseButton.PRIMARY) {
                selectItem();
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
                //control.hide();
            }
        });

    }

    private void selectItem() {
        T selectedItem = suggestionList.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            searchField.select(selectedItem);
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
