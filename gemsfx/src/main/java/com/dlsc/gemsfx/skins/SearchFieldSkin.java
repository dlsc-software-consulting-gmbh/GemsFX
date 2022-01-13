package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.scene.control.ListCell;
import javafx.scene.control.SkinBase;

public class SearchFieldSkin<T> extends SkinBase<SearchField<T>> {

    public SearchFieldSkin(SearchField<T> searchField) {
        super(searchField);

        searchField.getEditor().setSkin(new SearchFieldEditorSkin<>(searchField));

        getChildren().add(searchField.getEditor());

        searchField.setCellFactory(view -> new SearchFieldListCell());
    }

    public class SearchFieldListCell extends ListCell<T> {

        public SearchFieldListCell() {
            getStyleClass().add("search-field-list-cell");
            setPrefWidth(0);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                setText(getSkinnable().getConverter().toString(item));
            } else {
                setText("");
            }
        }
    }
}
