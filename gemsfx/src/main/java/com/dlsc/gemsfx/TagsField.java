package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TagsFieldSkin;
import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class TagsField<T> extends SearchField<T> {

    private static final int MAX_UNDO_AND_REDO_STEPS = 50;

    private static final String DEFAULT_STYLE_CLASS = "tags-field";

    private final Deque<Command> undoStack = new ArrayDeque<>();

    private final Deque<Command> redoStack = new ArrayDeque<>();

    public TagsField() {
        setShowSearchIcon(false);
        getStyleClass().addAll("text-input", DEFAULT_STYLE_CLASS);

        setTagViewFactory(tag -> {
            ChipView<T> chipView = new ChipView<>();
            chipView.setValue(tag);
            chipView.setText(getConverter().toString(tag));
            chipView.setOnClose(evt -> getTags().remove(tag));
            chipView.setFocusTraversable(false);
            chipView.getStyleClass().add("tag-view");
            return chipView;
        });

        getStylesheets().add(getUserAgentStylesheet());

        addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    if (!getTags().contains(selectedItem)) {
                        AddTagCommand cmd = new AddTagCommand(selectedItem);
                        execute(cmd);
                        Platform.runLater(() -> clear());
                    }
                }
            }
        });

        addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.BACK_SPACE) && getText().equals("")) {
                if (!getTags().isEmpty()) {
                    T item = getTags().get(getTags().size() - 1);
                    execute(new RemoveTagCommand(item));
                }
            } else if (KeyCombination.keyCombination("shortcut+z").match(evt)) {
                undo();
            } else if (KeyCombination.keyCombination("shortcut+shift+z").match(evt)) {
                redo();
            }
        });
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TagsFieldSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return TagsField.class.getResource("tags-field.css").toExternalForm();
    }

    @Override
    public void commit() {
        T selectedItem = getSelectedItem();
        if (selectedItem != null && !getTags().contains(selectedItem)) {
            AddTagCommand cmd = new AddTagCommand(selectedItem);
            execute(cmd);
            clear();
        }
    }

    @Override
    protected void update(Collection<T> newSuggestions) {
        if (newSuggestions != null) {
            newSuggestions.removeAll(getTags());
        }

        super.update(newSuggestions);
    }

    private final ListProperty<T> tags = new SimpleListProperty<>(this, "tags", FXCollections.observableArrayList());

    public final ObservableList<T> getTags() {
        return tags.get();
    }

    /**
     * A list property used to store the tags.
     *
     * @return the list of tags
     */
    public final ListProperty<T> tagsProperty() {
        return tags;
    }

    public final void setTags(ObservableList<T> tags) {
        this.tags.set(tags);
    }

    private final ObjectProperty<Callback<T, Node>> tagViewFactory = new SimpleObjectProperty<>(this, "tagViewFactory");

    public final Callback<T, Node> getTagViewFactory() {
        return tagViewFactory.get();
    }

    /**
     * A callback used to create the nodes that represent the tags. The default
     * implementation uses the {@link ChipView} control.
     *
     * @return a node factory for the tags
     */
    public final ObjectProperty<Callback<T, Node>> tagViewFactoryProperty() {
        return tagViewFactory;
    }

    public final void setTagViewFactory(Callback<T, Node> tagViewFactory) {
        this.tagViewFactory.set(tagViewFactory);
    }

    private interface Command {
        void execute();
        void undo();
    }

    private class AddTagCommand implements Command {

        private T item;

        public AddTagCommand(T item) {
            this.item = item;
        }

        @Override
        public void undo() {
            getTags().remove(item);
        }

        @Override
        public void execute() {
            getTags().add(item);
        }
    }

    private class RemoveTagCommand implements Command {

        private T item;

        public RemoveTagCommand(T item) {
            this.item = item;
        }

        @Override
        public void undo() {
            getTags().add(item);
        }

        @Override
        public void execute() {
            getTags().remove(item);
        }
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
            clear();
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
}
