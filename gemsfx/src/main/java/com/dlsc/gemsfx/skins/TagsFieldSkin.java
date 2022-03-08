package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.SearchField.SearchFieldListCell;
import com.dlsc.gemsfx.TagsField;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class TagsFieldSkin<T> extends SkinBase<TagsField<T>> {

    private static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");

    private static final PseudoClass CONTAINS_FOCUS = PseudoClass.getPseudoClass("contains-focus");

    private final FlowPane flowPane;

    public TagsFieldSkin(TagsField<T> field) {
        super(field);

        flowPane = new FlowPane() {
            @Override
            protected void layoutChildren() {
                super.layoutChildren();

                TextField editor = getSkinnable().getEditor();
                double w = getWidth() - getInsets().getLeft() - getInsets().getRight();

                editor.resize(w - editor.getBoundsInParent().getMinX(), editor.getHeight());
            }
        };

        flowPane.getStyleClass().add("flow-pane");
        flowPane.prefWrapLengthProperty().bind(field.widthProperty());

        field.getEditor().focusedProperty().addListener(it -> field.pseudoClassStateChanged(CONTAINS_FOCUS, field.getEditor().isFocused()));

        field.getTags().addListener((Observable it) -> pseudoClassStateChanged(FILLED, !field.getTags().isEmpty()));
        field.getEditor().setSkin(new SearchFieldEditorSkin<>(field));

        getChildren().addAll(flowPane);

        field.setCellFactory(view -> new SearchFieldListCell(field));

        field.getTags().addListener((Observable it) -> updateView());
        updateView();
    }

    private void updateView() {
        flowPane.getChildren().clear();

        TagsField<T> field = getSkinnable();
        ObservableList<T> tags = field.getTags();
        TextField editor = field.getEditor();

        for (int i = 0; i < tags.size(); i++) {
            T tag = tags.get(i);
            ChipView<T> chipView = new ChipView<>();
            chipView.setValue(tag);
            chipView.setText(field.getConverter().toString(tag));
            chipView.setOnClose(evt -> field.getTags().remove(tag));
            chipView.setFocusTraversable(false);
            chipView.getStyleClass().add("tag-view");

            if (i == 0) {
                chipView.getStyleClass().add("first");
            }

            if (i == tags.size() - 1) {
                chipView.getStyleClass().add("last");
            }

            flowPane.getChildren().add(chipView);
        }

        flowPane.getChildren().add(editor);
    }
}
