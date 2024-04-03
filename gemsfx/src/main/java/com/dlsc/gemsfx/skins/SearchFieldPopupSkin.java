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
import java.util.Objects;
import java.util.function.Consumer;

public class SearchFieldPopupSkin<T> implements Skin<SearchFieldPopup<T>> {

    private final SearchFieldPopup<T> control;
    private final ListView<T> listView;
    private final SearchField<T> searchField;

    public SearchFieldPopupSkin(SearchFieldPopup<T> control) {
        this.control = control;

        searchField = control.getSearchField();

        SortedList<T> sortedList = new SortedList<>(searchField.getSuggestions(), createInnerComparator());

        listView = new ListView<>(sortedList) {
            @Override
            public String getUserAgentStylesheet() {
                return Objects.requireNonNull(SearchField.class.getResource("search-field.css")).toExternalForm();
            }
        };

        listView.getStyleClass().add("search-field-list-view");
        listView.cellFactoryProperty().bind(searchField.cellFactoryProperty());

        listView.prefWidthProperty().bind(control.prefWidthProperty());
        listView.maxWidthProperty().bind(control.maxWidthProperty());
        listView.minWidthProperty().bind(control.minWidthProperty());

        listView.placeholderProperty().bind(searchField.placeholderProperty());

        listView.getSelectionModel().selectedItemProperty().addListener(it -> control.getSearchField().setSelectedItem(listView.getSelectionModel().getSelectedItem()));
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
                    result = 1;
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
                result = 1;
            }

            return result;
        };
    }

    private void registerEventListener() {
        listView.setOnMouseClicked((me) -> {
            if (me.getButton() == MouseButton.PRIMARY) {
                if (me.getClickCount() == 1) {
                    selectItem();
                }
            }
        });
    }

    private void selectItem() {
        T selectedItem = listView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            searchField.select(selectedItem);
            searchField.commit();
            Consumer<T> onCommit = searchField.getOnCommit();
            if (onCommit != null) {
                onCommit.accept(selectedItem);
            }
        }
    }

    public Node getNode() {
        return listView;
    }

    public SearchFieldPopup<T> getSkinnable() {
        return control;
    }

    public void dispose() {
    }
}
