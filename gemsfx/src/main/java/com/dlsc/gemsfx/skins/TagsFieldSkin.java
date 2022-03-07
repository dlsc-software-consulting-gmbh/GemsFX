package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.TagsField;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

public class TagsFieldSkin<T> extends SkinBase<TagsField<T>> {

    private static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");

    private static final PseudoClass CONTAINS_FOCUS = PseudoClass.getPseudoClass("contains-focus");

    private InnerFlowPane flowPane = new InnerFlowPane();

    public TagsFieldSkin(TagsField<T> field) {
        super(field);

        flowPane.getStyleClass().add("flow-pane");

        flowPane.prefWrapLengthProperty().bind(field.widthProperty());

        field.getEditor().focusedProperty().addListener(it -> field.pseudoClassStateChanged(CONTAINS_FOCUS, field.getEditor().isFocused()));

        field.getTags().addListener((Observable it) -> pseudoClassStateChanged(FILLED, !field.getTags().isEmpty()));
        field.getEditor().setSkin(new SearchFieldEditorSkin<>(field));

        field.addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = field.getSelectedItem();
                if (selectedItem != null) {
                    if (!field.getTags().contains(selectedItem)) {
                        field.getTags().add(selectedItem);
                        field.setText("");
                    }
                }
            }
        });

        field.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.BACK_SPACE) && field.getText().equals("")) {
                if (!field.getTags().isEmpty()) {
                    field.getTags().remove(field.getTags().size() - 1);
                }
            }
        });

        getChildren().addAll(flowPane);

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

    class InnerFlowPane extends FlowPane {

        @Override
        protected void layoutChildren() {
            super.layoutChildren();
            TextField editor = getSkinnable().getEditor();
            double w = getWidth() - getInsets().getLeft() - getInsets().getRight();

            editor.resizeRelocate(snapPositionX(editor.getBoundsInParent().getMinX()),
                    snapPositionY(editor.getBoundsInParent().getMinY()),
                    snapSizeX(w - editor.getBoundsInParent().getMinX()),
                    snapSizeY(editor.prefHeight(-1)));
        }
    }
}
