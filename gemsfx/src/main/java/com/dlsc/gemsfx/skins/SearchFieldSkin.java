package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ListCell;
import javafx.scene.control.SkinBase;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class SearchFieldSkin<T> extends SkinBase<SearchField<T>> {

    public SearchFieldSkin(SearchField<T> searchField) {
        super(searchField);

        searchField.getEditor().setSkin(new SearchFieldEditorSkin<>(searchField));

        getChildren().add(searchField.getEditor());

        searchField.setCellFactory(view -> new SearchFieldListCell());
    }

    public class SearchFieldListCell extends ListCell<T> {

        private TextFlow textFlow = new TextFlow();
        private Text text1 = new Text();
        private Text text2 = new Text();
        private Text text3 = new Text();

        public SearchFieldListCell() {
            getStyleClass().add("search-field-list-cell");

            textFlow.getChildren().setAll(text1, text2, text3);
            text1.getStyleClass().addAll("text", "start");
            text2.getStyleClass().addAll("text", "middle");
            text3.getStyleClass().addAll("text", "end");

            setPrefWidth(0);
            setGraphic(textFlow);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                String cellText = getSkinnable().getConverter().toString(item);
                String text = getSkinnable().getEditor().getText();
                int index = cellText.toLowerCase().indexOf(text.toLowerCase());
                if (index >= 0) {
                    text1.setText(cellText.substring(0, index));
                    text2.setText(cellText.substring(index, index + text.length()));
                    text3.setText(cellText.substring(index + text.length()));
                } else {
                    text1.setText(cellText);
                    text2.setText("");
                    text3.setText("");
                }
            } else {
                text1.setText("");
                text2.setText("");
                text3.setText("");
            }
        }
    }
}
