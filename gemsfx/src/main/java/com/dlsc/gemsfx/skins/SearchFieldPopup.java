//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class SearchFieldPopup<T> extends PopupControl {

    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private final SearchField searchField;

    public static final String DEFAULT_STYLE_CLASS = "search-field-popup";

    public SearchFieldPopup(SearchField searchField) {
        this.searchField = Objects.requireNonNull(searchField);

        prefWidthProperty().bind(searchField.widthProperty());
        setAutoFix(true);
        setAutoHide(false);
        setHideOnEscape(true);
        getStyleClass().add(DEFAULT_STYLE_CLASS);

        searchField.addEventHandler(SearchField.SearchEvent.SEARCH_FINISHED, evt -> {
            if ((!searchField.getSuggestions().isEmpty() || searchField.getPlaceholder() != null)
                    && StringUtils.isNotBlank(searchField.getEditor().getText())
                    && (!searchField.isHidePopupWithSingleChoice() || !(searchField.getSuggestions().size() == 1 && searchField.getSuggestions().get(0).equals(searchField.getSelectedItem())))) {
                show(searchField);
                selectFirstSuggestion();
            } else {
                hide();
            }
        });
    }

    public SearchField<T> getSearchField() {
        return searchField;
    }

    public ObservableList<T> getSuggestions() {
        return suggestions;
    }

    public void show(Node node) {
        if (node.getScene() != null && node.getScene().getWindow() != null) {
            Window parent = node.getScene().getWindow();
            getScene().setNodeOrientation(node.getEffectiveNodeOrientation());
            if (node.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT) {
                setAnchorLocation(AnchorLocation.CONTENT_TOP_RIGHT);
            } else {
                setAnchorLocation(AnchorLocation.CONTENT_TOP_LEFT);
            }

            show(parent, parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX(), parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY() + node.getBoundsInParent().getHeight());
        } else {
            throw new IllegalStateException("Can not show popup. The node must be attached to a scene/window.");
        }
    }

    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion() {
        SearchFieldPopupSkin<T> skin = (SearchFieldPopupSkin) getSkin();
        ListView<?> listView = (ListView<?>) skin.getNode();
        if (listView.getItems() != null && !listView.getItems().isEmpty()) {
            listView.getSelectionModel().select(0);
        }
    }

    protected Skin<?> createDefaultSkin() {
        return new SearchFieldPopupSkin(this);
    }
}
