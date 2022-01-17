package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.TextFieldSkin;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Set;

public class SearchFieldEditorSkin<T> extends TextFieldSkin {

    private final StackPane graphicWrapper = new StackPane();
    private final StackPane searchGraphicWrapper = new StackPane();
    private final Label autoCompletedTextLabel = new Label();
    private final SearchField<T> searchField;

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

        SearchFieldPopup<T> autoCompletionPopup = new SearchFieldPopup<>(searchField);
        autoCompletionPopup.autoHideProperty().bind(searchField.placeholderProperty().isNull());

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
            Optional<Node> lookupOptional = nodes.stream().filter(n -> {
                if (n instanceof Text) {
                    Text textNode = (Text) n;
                    String text = textNode.getText();
                    if (text != null) {
                        return !(text.equals(textField.getPromptText()));
                    }
                }
                return false;
            }).findFirst();

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
