//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomPopupControl;
import com.dlsc.gemsfx.SearchField;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchFieldPopup<T> extends CustomPopupControl {

    public static final String DEFAULT_STYLE_CLASS = "search-field-popup";

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
                    try {
                        blockingInvoke(()->{
                            hide();
                            searchField.getProperties().remove("committed");
                            searchField.getProperties().remove("cancelled");
                        });
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };

        searchField.getProperties().addListener(l);

        searchField.addEventHandler(SearchField.SearchEvent.SEARCH_FINISHED, evt -> {
            if ((!searchField.getSuggestions().isEmpty() || searchField.getPlaceholder() != null) && (StringUtils.isNotBlank(searchField.getEditor().getText()) || searchField.suggestWhenBlank.get())) {

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
                    showPopup();
//                    selectFirstSuggestion();
//                    if(!wasShown) {
//                        if(!searchField.getEditor().isFocused()) {
//                            searchField.getEditor().requestFocus();
//                            searchField.getEditor().selectEnd();
//                        }
//                    }
                } else {
                    hide();
                }
            } else {
                hide();
            }
        });
    }

    /**
     * block on the current thread until the ui invocation has completed.
     * this is mostly useful for starting transitions or modifying ui elements off the ui thread
     *
     * @param r
     */
    public static void blockingInvoke(Runnable r) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);


        if (!Thread.currentThread().getName().equals("JavaFX Application Thread")) {
            Platform.runLater(() -> {
                r.run();
                latch.countDown();
            });
        }
        else {
            r.run();
            latch.countDown();
        }

        latch.await();
    }

    public void showPopup(){
        show(searchField);
    }

    public SearchField<T> getSearchField() {
        return searchField;
    }

    public ObservableList<T> getSuggestions() {
        return suggestions;
    }

    /**
     * Selects the first suggestion (if any), so the user can choose it
     * by pressing enter immediately.
     */
    private void selectFirstSuggestion() {
        SearchFieldPopupSkin<?> skin = (SearchFieldPopupSkin<?>) getSkin();
        ListView<?> listView = (ListView<?>) skin.getNode();
        if (listView.getItems() != null && !listView.getItems().isEmpty()) {
            listView.getSelectionModel().select(0);
        }
    }

    protected Skin<?> createDefaultSkin() {
        return new SearchFieldPopupSkin<>(this);
    }
}
