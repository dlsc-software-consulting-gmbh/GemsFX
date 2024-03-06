//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.NodeOrientation;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.stage.Screen;
import javafx.stage.Window;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class SearchFieldPopup<T> extends PopupControl {

    public static final String DEFAULT_STYLE_CLASS = "search-field-popup";
    private static final PseudoClass ABOVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("above");
    private static final PseudoClass BELOW_PSEUDO_CLASS = PseudoClass.getPseudoClass("below");

    private final ObservableList<T> suggestions = FXCollections.observableArrayList();
    private final SearchField<T> searchField;

    public SearchFieldPopup(SearchField<T> searchField) {
        this.searchField = Objects.requireNonNull(searchField);

        minWidthProperty().bind(searchField.widthProperty());

        setAutoFix(true);
        setAutoHide(true);
        setHideOnEscape(true);

        getStyleClass().add(DEFAULT_STYLE_CLASS);

        MapChangeListener<? super Object, ? super Object> l = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("committed") || change.getKey().equals("cancelled")) {
                    hide();
                    searchField.getProperties().remove("committed");
                    searchField.getProperties().remove("cancelled");
                }
            }
        };

        searchField.getProperties().addListener(l);

        searchField.addEventHandler(SearchField.SearchEvent.SEARCH_FINISHED, evt -> {
            if ((!searchField.getSuggestions().isEmpty() || searchField.getPlaceholder() != null) && StringUtils.isNotBlank(searchField.getEditor().getText())) {

                // assuming that we don't have to show it
                boolean showIt = false;
                int suggestionsItemsSize = searchField.getSuggestions().size();
                if (suggestionsItemsSize == 0) {
                    if (!searchField.isHidePopupWithNoChoice()) {
                        showIt = true;
                    }
                } else if (suggestionsItemsSize == 1) {
                    if (!searchField.isHidePopupWithSingleChoice() || !searchField.getMatcher().apply(searchField.getSuggestions().get(0), evt.getText())) {
                        showIt = true;
                    }
                } else {
                    // more than one suggested item, definitely show the popup
                    showIt = true;
                }

                if (showIt) {
                    show(searchField);
                    selectFirstSuggestion();
                } else {
                    hide();
                }
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

            double nodeTopY = parent.getY() + node.localToScene(0.0D, 0.0D).getY() + node.getScene().getY();

            double anchorX = parent.getX() + node.localToScene(0.0D, 0.0D).getX() + node.getScene().getX();
            double anchorY = nodeTopY + node.getBoundsInParent().getHeight();

            double bridgeHeight = bridge.getHeight();
            double popupHeight = bridgeHeight == 0 ? getSkin().getNode().prefHeight(-1) : bridgeHeight;
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            boolean isShowAbove = anchorY + popupHeight > screenHeight;
            if (isShowAbove) {
                anchorY = nodeTopY - popupHeight;
            }
            this.pseudoClassStateChanged(ABOVE_PSEUDO_CLASS, isShowAbove);
            this.pseudoClassStateChanged(BELOW_PSEUDO_CLASS, !isShowAbove);

            show(node, anchorX, anchorY);
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
        return new SearchFieldPopupSkin<>(this);
    }
}
