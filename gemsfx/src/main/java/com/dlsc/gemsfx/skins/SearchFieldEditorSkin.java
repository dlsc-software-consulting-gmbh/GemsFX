package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import com.dlsc.gemsfx.SearchField.SearchFieldSuggestionRequest;
import com.dlsc.gemsfx.skins.autocomplete.AutoCompletePopup;
import com.dlsc.gemsfx.skins.autocomplete.AutoCompletePopupSkin;
import com.dlsc.gemsfx.skins.autocomplete.AutoCompletionBinding;
import com.dlsc.gemsfx.skins.autocomplete.AutoCompletionTextFieldBinding;
import javafx.beans.InvalidationListener;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class SearchFieldEditorSkin<T> extends TextFieldSkin {

    private final StackPane graphicWrapper = new StackPane();
    private final StackPane searchGraphicWrapper = new StackPane();
    private final Label autoCompletedTextLabel = new Label();
    private final SearchField<T> searchField;

    private AutoCompletionBinding<T> binding;

    public SearchFieldEditorSkin(SearchField<T> searchField) {
        super(searchField.getEditor());

        this.searchField = searchField;

        graphicWrapper.getStyleClass().add("graphic-wrapper");
        graphicWrapper.visibleProperty().bind(searchField.searchingProperty().not());
        graphicWrapper.managedProperty().bind(searchField.searchingProperty().not());

        searchGraphicWrapper.getStyleClass().addAll("graphic-wrapper", "search-graphic-wrapper");
        searchGraphicWrapper.visibleProperty().bind(searchField.searchingProperty());
        searchGraphicWrapper.managedProperty().bind(searchField.searchingProperty());

        setupGraphics();

        searchField.graphicProperty().addListener(it -> setupGraphics());

        autoCompletedTextLabel.getStyleClass().add("auto-completion-label");
        autoCompletedTextLabel.textProperty().bind(searchField.autoCompletedTextProperty());
        autoCompletedTextLabel.visibleProperty().bind(searchField.autoCompletedTextProperty().isEmpty().not());
        autoCompletedTextLabel.setManaged(false);
        autoCompletedTextLabel.setPrefHeight(0);

        registerChangeListener(searchField.autoCompletedTextProperty(), it -> getSkinnable().requestLayout());

        getChildren().addAll(autoCompletedTextLabel, graphicWrapper, searchGraphicWrapper);

        InvalidationListener updateListener = it -> createAutoSuggestBinding();

        searchField.converterProperty().addListener(updateListener);
        searchField.cellFactoryProperty().addListener(updateListener);
        searchField.suggestionProviderProperty().addListener(updateListener);
        searchField.comparatorProperty().addListener(updateListener);

        createAutoSuggestBinding();
    }

    private void setupGraphics() {
        graphicWrapper.getChildren().clear();
        Node graphic = searchField.getGraphic();
        if (graphic != null) {
            graphicWrapper.getChildren().setAll(graphic);
        }

        searchGraphicWrapper.getChildren().clear();
        Node searchGraphic = searchField.getBusyGraphic();
        if (searchGraphic != null) {
            searchGraphicWrapper.getChildren().setAll(searchGraphic);
        }
    }

    private void createAutoSuggestBinding() {
        if (binding != null) {
            binding.dispose();
        }

        Callback<SearchFieldSuggestionRequest, Collection<T>> suggestionProvider = searchField.getSuggestionProvider();
        StringConverter<T> converter = searchField.getConverter();
        Callback<ListView<T>, ListCell<T>> cellFactory = searchField.getCellFactory();

        Callback<SearchFieldSuggestionRequest, Collection<T>> innerSuggestionProvider = request -> {
            if (StringUtils.isNotBlank(request.getUserText())) {
                List<T> result = new ArrayList<>(suggestionProvider.call(request));
                Collections.sort(result, createInnerComparator());
                return result;
            }
            return Collections.emptyList();
        };

        binding = new AutoCompletionTextFieldBinding<>(getSkinnable(), innerSuggestionProvider, converter);

        AutoCompletePopup<T> autoCompletionPopup = binding.getAutoCompletionPopup();
        autoCompletionPopup.autoHideProperty().bind(searchField.placeholderProperty().isNull());

        AutoCompletePopupSkin<T> skin = new AutoCompletePopupSkin<>(autoCompletionPopup, cellFactory);
        autoCompletionPopup.setSkin(skin);

        ListView<T> listView = (ListView<T>) skin.getNode();
        listView.getStylesheets().add(SearchField.class.getResource("search-field.css").toExternalForm());
        listView.setMinHeight(200);
        listView.placeholderProperty().bind(searchField.placeholderProperty());
        listView.getSelectionModel().selectedItemProperty().addListener(it -> {
            if (!searchField.isNewItem()) {
                T selectedItem = listView.getSelectionModel().getSelectedItem();
                searchField.setSelectedItem(selectedItem);
            }
        });

        binding.prefWidthProperty().bind(getSkinnable().widthProperty());
        binding.setVisibleRowCount(10);
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

            T selectedItem = searchField.getSelectedItem();
            if (selectedItem != null) {
                if (o1.equals(selectedItem)) {
                    result = -1;
                }
                if (selectedItem.equals(o2)) {
                    result = +1;
                }
            }

            return result;
        };
    }

    @Override
    protected void layoutChildren(double x, double y, double w, double h) {
        super.layoutChildren(x, y, w, h);

        TextField textField = getSkinnable();
        Insets insets = textField.getInsets();
        double gap = Math.max(0, searchField.getAutoCompletionGap());

        Node lookup = null;
        Set<Node> nodes = textField.lookupAll(".text");
        if (nodes.size() > 2) { // normal text, prompt text, autocomplete tex
            // there might be two nodes with ".text" style class if the field uses a prompt text
            Optional<Node> lookupOptional = nodes.stream().filter(n -> (n instanceof Text) && !((Text) n).getText().equals(textField.getPromptText())).findFirst();
            if (lookupOptional.isPresent()) {
                lookup = lookupOptional.get();
            }
        } else {
            lookup = textField.lookup(".text");
        }

        if (lookup == null) {
            return;
        }

        Bounds standardTextBounds = lookup.getLayoutBounds();

        double autoCompleteWidth = autoCompletedTextLabel.prefWidth(-1);

        double autoCompletionX = insets.getLeft() + gap + standardTextBounds.getMinX() + standardTextBounds.getWidth();
        autoCompletedTextLabel.resizeRelocate(
                autoCompletionX,
                y,
                Math.max(0, Math.min(autoCompleteWidth, w - autoCompletionX)),
                h);

        // standard graphic
        double iconWidth = graphicWrapper.prefWidth(-1);
        double iconHeight = graphicWrapper.prefHeight(-1);
        graphicWrapper.resizeRelocate(x + w - iconWidth, y + h / 2 - iconHeight / 2, iconWidth, iconHeight);

        // search / busy graphic
        iconWidth = searchGraphicWrapper.prefWidth(-1);
        iconHeight = searchGraphicWrapper.prefHeight(-1);
        searchGraphicWrapper.resizeRelocate(x + w - iconWidth, y + h / 2 - iconHeight / 2, iconWidth, iconHeight);
    }
}
