package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SpotlightTextField;
import com.dlsc.gemsfx.SpotlightTextField.ISpotlightSuggestionRequest;
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
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.*;

public class SpotlightTextFieldSkin<T> extends TextFieldSkin {

    private final StackPane icon;
    private final Label autoCompletedTextLabel = new Label();

    private AutoCompletionBinding<T> binding;

    public SpotlightTextFieldSkin(SpotlightTextField<T> textField) {
        super(textField);

        FontIcon fontIcon = new FontIcon(MaterialDesign.MDI_MAGNIFY);

        icon = new StackPane(fontIcon);
        icon.getStyleClass().add("icon-wrapper");

        autoCompletedTextLabel.getStyleClass().add("auto-completion-label");
        autoCompletedTextLabel.textProperty().bind(textField.autoCompletedTextProperty());
        autoCompletedTextLabel.visibleProperty().bind(textField.autoCompletedTextProperty().isEmpty().not());
        autoCompletedTextLabel.setManaged(false);
        autoCompletedTextLabel.setPrefHeight(0);

        registerChangeListener(textField.autoCompletedTextProperty(), it -> textField.requestLayout());

        getChildren().addAll(autoCompletedTextLabel, icon);

        InvalidationListener updateListener = it -> createAutoSuggestBinding();

        textField.converterProperty().addListener(updateListener);
        textField.cellFactoryProperty().addListener(updateListener);
        textField.suggestionProviderProperty().addListener(updateListener);
        textField.comparatorProperty().addListener(updateListener);

        createAutoSuggestBinding();
    }

    private void createAutoSuggestBinding() {
        if (binding != null) {
            binding.dispose();
        }

        SpotlightTextField<T> textField = (SpotlightTextField<T>) getSkinnable();

        Callback<ISpotlightSuggestionRequest, Collection<T>> suggestionProvider = textField.getSuggestionProvider();
        StringConverter<T> converter = textField.getConverter();
        Callback<ListView<T>, ListCell<T>> cellFactory = textField.getCellFactory();

        Callback<ISpotlightSuggestionRequest, Collection<T>> innerSuggestionProvider = request -> {
            if (StringUtils.isNotBlank(request.getUserText())) {
                List<T> result = new ArrayList<>(suggestionProvider.call(request));
                Collections.sort(result, createInnerComparator());
                return result;
            }
            return Collections.emptyList();
        };

        binding = new AutoCompletionTextFieldBinding<>(textField, innerSuggestionProvider, converter);

        AutoCompletePopup<T> autoCompletionPopup = binding.getAutoCompletionPopup();
        autoCompletionPopup.autoHideProperty().bind(textField.placeholderProperty().isNull());

        AutoCompletePopupSkin<T> skin = new AutoCompletePopupSkin<>(autoCompletionPopup, cellFactory);
        autoCompletionPopup.setSkin(skin);

        ListView<T> listView = (ListView<T>) skin.getNode();
        listView.setMinHeight(200);
        listView.placeholderProperty().bind(textField.placeholderProperty());
        listView.getSelectionModel().selectedItemProperty().addListener(it -> {
            if (!textField.isNewItem()) {
                T selectedItem = listView.getSelectionModel().getSelectedItem();
                textField.setSelectedItem(selectedItem);
            }
        });

        binding.prefWidthProperty().bind(textField.widthProperty());
        binding.setVisibleRowCount(10);
    }

    /*
     * We use an inner comparator because for the proper functioning of the auto suggest
     * behaviour we have to make sure that the currently selected item will always show
     * up as the first item in the list.
     */
    private Comparator<T> createInnerComparator() {
        SpotlightTextField<T> textField = (SpotlightTextField<T>) getSkinnable();

        return (o1, o2) -> {
            Comparator<T> comparator = textField.getComparator();
            int result = comparator.compare(o1, o2);

            T selectedItem = textField.getSelectedItem();
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

        SpotlightTextField textField = (SpotlightTextField) getSkinnable();
        Insets insets = textField.getInsets();
        double gap = Math.max(0, textField.getAutoCompletionGap());

        Node lookup = textField.lookup(".text");

        Bounds standardTextBounds = lookup.getLayoutBounds();

        double autoCompleteWidth = autoCompletedTextLabel.prefWidth(-1);

        double autoCompletionX = insets.getLeft() + gap + standardTextBounds.getMinX() + standardTextBounds.getWidth();
        autoCompletedTextLabel.resizeRelocate(
                autoCompletionX,
                y,
                Math.max(0, Math.min(autoCompleteWidth, w - autoCompletionX)),
                h);

        double iconWidth = icon.prefWidth(-1);
        double iconHeight = icon.prefHeight(-1);
        icon.resizeRelocate(x + w - iconWidth, y + h / 2 - iconHeight / 2, iconWidth, iconHeight);
    }
}
