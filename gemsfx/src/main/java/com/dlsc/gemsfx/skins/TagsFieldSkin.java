package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.SearchField.SearchFieldListCell;
import com.dlsc.gemsfx.TagsField;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

import java.util.ArrayDeque;
import java.util.Deque;

public class TagsFieldSkin<T> extends SkinBase<TagsField<T>> {

    private static final PseudoClass FILLED = PseudoClass.getPseudoClass("filled");

    private static final PseudoClass CONTAINS_FOCUS = PseudoClass.getPseudoClass("contains-focus");

    private static final int MAX_UNDO_AND_REDO_STEPS = 50;

    private final FlowPane flowPane;

    private final Deque<Command> undoStack = new ArrayDeque<>();

    private final Deque<Command> redoStack = new ArrayDeque<>();

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

        field.addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = field.getSelectedItem();
                if (selectedItem != null) {
                    if (!field.getTags().contains(selectedItem)) {
                        AddTagCommand cmd = new AddTagCommand(selectedItem);
                        execute(cmd);
                        field.setText("");
                    }
                }
            }
        });

        field.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.BACK_SPACE) && field.getText().equals("")) {
                if (!field.getTags().isEmpty()) {
                    T item = field.getTags().get(field.getTags().size() - 1);
                    execute(new RemoveTagCommand(item));
                }
            } else if (KeyCombination.keyCombination("shortcut+z").match(evt)) {
                undo();
            } else if (KeyCombination.keyCombination("shortcut+shift+z").match(evt)) {
                redo();
            }
        });

        getChildren().addAll(flowPane);

        field.setCellFactory(view -> new SearchFieldListCell(field));

        field.getTags().addListener((Observable it) -> updateView());
        updateView();
    }

    private void execute(Command cmd) {
        cmd.execute();
        undoStack.push(cmd);
        if (undoStack.size() > MAX_UNDO_AND_REDO_STEPS) {
            undoStack.removeLast();
        }
    }

    private void undo() {
        if (!undoStack.isEmpty()) {
            getSkinnable().clear();
            Command cmd = undoStack.pop();
            cmd.undo();
            redoStack.push(cmd);
            if (redoStack.size() > MAX_UNDO_AND_REDO_STEPS) {
                redoStack.removeLast();
            }
        }
    }

    private void redo() {
        if (!redoStack.isEmpty()) {
            Command cmd = redoStack.pop();
            cmd.execute();
            undoStack.push(cmd);
        }
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

    interface Command {

        void undo();

        void execute();
    }

    class AddTagCommand implements Command {

        private T item;

        public AddTagCommand(T item) {
            this.item = item;
        }

        @Override
        public void undo() {
            getSkinnable().getTags().remove(item);
        }

        @Override
        public void execute() {
            getSkinnable().getTags().add(item);
        }
    }

    class RemoveTagCommand implements Command {

        private T item;

        public RemoveTagCommand(T item) {
            this.item = item;
        }

        @Override
        public void undo() {
            getSkinnable().getTags().add(item);
        }

        @Override
        public void execute() {
            getSkinnable().getTags().remove(item);
        }
    }
}
