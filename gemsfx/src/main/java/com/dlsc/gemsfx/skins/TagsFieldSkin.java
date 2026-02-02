package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.SearchField.SearchFieldListCell;
import com.dlsc.gemsfx.TagsField;
import com.dlsc.gemsfx.util.FocusUtil;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;

import java.util.HashMap;
import java.util.Map;

public class TagsFieldSkin<T> extends SkinBase<TagsField<T>> {

    private static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");
    private static final PseudoClass CONTAINS_FOCUS = PseudoClass.getPseudoClass("contains-focus");
    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    private final FlowPane flowPane;

    private final Map<T, Node> tagViewMap = new HashMap<>();

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
        flowPane.prefWrapLengthProperty().bind(Bindings.createDoubleBinding(() -> flowPane.getWidth() - flowPane.getInsets().getLeft() - flowPane.getInsets().getRight(), flowPane.widthProperty(), flowPane.insetsProperty()));

        //FocusUtil.delegateFocus(field, flowPane);

        TextField editor = field.getEditor();

        editor.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
            if (editor.getText().isEmpty()) {
                return field.getEditorMinWidth();
            }
            return field.getEditorPrefWidth();
        }, editor.widthProperty(), editor.textProperty(), field.editorMinWidthProperty(), field.editorPrefWidthProperty()));

        editor.focusedProperty().addListener(it -> field.pseudoClassStateChanged(CONTAINS_FOCUS, editor.isFocused()));
        editor.setSkin(new SearchFieldEditorSkin<>(field));

        field.getTags().addListener((Observable it) -> pseudoClassStateChanged(FILLED, !field.getTags().isEmpty()));

        getChildren().addAll(flowPane);

        field.setCellFactory(view -> new SearchFieldListCell<>(field));

        field.getTagSelectionModel().getSelectedItems().addListener((Observable it) -> tagViewMap.forEach((key, value) -> value.pseudoClassStateChanged(SELECTED, field.getTagSelectionModel().getSelectedItems().contains(key))));
        editor.setOnMouseClicked(evt -> field.getTagSelectionModel().clearSelection());

        InvalidationListener updateViewListener = it -> updateView();
        field.tagViewFactoryProperty().addListener(updateViewListener);
        field.getTags().addListener(updateViewListener);

        updateView();
    }

    private int leadSelection;

    private void updateView() {
        flowPane.getChildren().clear();
        tagViewMap.clear();

        TagsField<T> field = getSkinnable();
        TextField editor = field.getEditor();

        ObservableList<T> tags = field.getTags();

        MultipleSelectionModel<T> selectionModel = field.getTagSelectionModel();

        for (int i = 0; i < tags.size(); i++) {
            T tag = tags.get(i);

            final int index = i;

            Node tagView = getSkinnable().getTagViewFactory().call(tag);
            tagView.setFocusTraversable(false);
            tagView.getStyleClass().add("tag-view");
            tagView.setOnMousePressed(evt -> {
                // the field has to have focus, or we do not receive keyboard events
                field.getEditor().requestFocus();

                if (evt.getButton().equals(MouseButton.PRIMARY)) {

                    if (evt.isShortcutDown()) {
                        if (!selectionModel.isSelected(index)) {
                            selectionModel.select(index);
                        } else {
                            selectionModel.clearSelection(index);
                        }
                    } else if (evt.isShiftDown()) {
                        if (leadSelection >= 0) {
                            int from = Math.min(leadSelection, index);
                            int to = Math.max(leadSelection, index);
                            selectionModel.selectRange(from, to + 1);
                        } else {
                            selectionModel.select(index);
                        }
                    } else {
                        boolean wasSelected = selectionModel.isSelected(index);
                        selectionModel.clearSelection();
                        if (!wasSelected) {
                            selectionModel.select(index);
                        }
                    }

                    leadSelection = index;
                }
            });
            if (tags.size() == 1) {
                tagView.getStyleClass().add("only");
            } else if (i == 0) {
                tagView.getStyleClass().add("first");
            } else if (i == tags.size() - 1) {
                tagView.getStyleClass().add("last");
            } else {
                tagView.getStyleClass().add("middle");
            }

            tagViewMap.put(tag, tagView);

            flowPane.getChildren().add(tagView);
        }

        flowPane.getChildren().add(editor);
    }
}
