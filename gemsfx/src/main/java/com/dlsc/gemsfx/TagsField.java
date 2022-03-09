package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TagsFieldSkin;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.*;
import java.util.stream.Collectors;

public class TagsField<T> extends SearchField<T> {

    private static final int MAX_UNDO_AND_REDO_STEPS = 50;

    private static final String DEFAULT_STYLE_CLASS = "tags-field";

    private final Deque<Command> undoStack = new ArrayDeque<>();

    private final Deque<Command> redoStack = new ArrayDeque<>();

    public TagsField() {
        setShowSearchIcon(false);
        getStyleClass().addAll("text-input", DEFAULT_STYLE_CLASS);

        getEditor().focusedProperty().addListener(it -> {
            if (getEditor().isFocused()) {
                getTagSelectionModel().clearSelection();
            }
        });

        setTagViewFactory(tag -> {
            Label tagLabel = new Label();
            tagLabel.setText(getConverter().toString(tag));
            return tagLabel;
        });

        getStylesheets().add(getUserAgentStylesheet());

        addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    if (!getTags().contains(selectedItem)) {
                        addTags(selectedItem);
                        Platform.runLater(() -> clear());
                    }
                }
            }
        });

        textProperty().addListener(it -> getTagSelectionModel().clearSelection());

        addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            MultipleSelectionModel<T> tagSelectionModel = getTagSelectionModel();
            if (evt.getCode().equals(KeyCode.BACK_SPACE)) {
                if (!tagSelectionModel.isEmpty()) {
                    removeTags((T[]) tagSelectionModel.getSelectedItems().toArray());
                } else if (getText().equals("") && !getTags().isEmpty()) {
                    removeTags(getTags().get(getTags().size() - 1));
                }
            } else if (KeyCombination.keyCombination("shortcut+z").match(evt)) {
                undo();
            } else if (KeyCombination.keyCombination("shortcut+shift+z").match(evt)) {
                redo();
            } else if (KeyCombination.keyCombination("shortcut+a").match(evt)) {
                tagSelectionModel.selectAll();
            } else if (evt.getCode().equals(KeyCode.ESCAPE)) {
                tagSelectionModel.clearSelection();
            }
        });

        setTagSelectionModel(new TagFieldSelectionModel());
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
            addTags(selectedItem);
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
     * Used to add one or more tags programmatically. This ensures that tags are added via an undoable command.
     *
     * @param values the value to add as a tag
     */
    public final void addTags(T... values) {
        execute(new AddTagCommand(values));
    }

    /**
     * Used to remove one or more tags programmatically. This ensures that tags are removed via an undoable command.
     *
     * @param values the tags to add
     */
    public final void removeTags(T... values) {
        execute(new RemoveTagCommand(values));
    }

    /**
     * Used to clear all tags programmatically. This ensures that tags are cleared via an undoable command.
     */
    public final void clearTags() {
        execute(new ClearTagsCommand(getTags()));
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

    private final ObjectProperty<MultipleSelectionModel<T>> tagSelectionModel = new SimpleObjectProperty<>(this, "selectionModel");

    public final MultipleSelectionModel<T> getTagSelectionModel() {
        return tagSelectionModel.get();
    }

    public final ObjectProperty<MultipleSelectionModel<T>> tagSelectionModelProperty() {
        return tagSelectionModel;
    }

    public final void setTagSelectionModel(MultipleSelectionModel<T> tagSelectionModel) {
        this.tagSelectionModel.set(tagSelectionModel);
    }

    private interface Command {
        void execute();

        void undo();
    }

    private class AddTagCommand implements Command {

        private T[] tags;

        public AddTagCommand(T... tags) {
            this.tags = tags;
        }

        @Override
        public void undo() {
            getTags().removeAll(tags);
        }

        @Override
        public void execute() {
            for (T tag : tags) {
                if (!getTags().contains(tag)) {
                    getTags().add(tag);
                }
            }
        }
    }

    private class RemoveTagCommand implements Command {

        private T[] tags;

        public RemoveTagCommand(T... tags) {
            this.tags = tags;
        }

        @Override
        public void undo() {
            for (T tag : tags) {
                if (!getTags().contains(tag)) {
                    getTags().add(tag);
                }
            }
        }

        @Override
        public void execute() {
            getTags().removeAll(tags);
        }
    }

    private class ClearTagsCommand implements Command {

        private List<T> tags = new ArrayList<>();

        public ClearTagsCommand(List<T> tags) {
            this.tags.addAll(tags);
        }

        @Override
        public void undo() {
            getTags().setAll(tags);
        }

        @Override
        public void execute() {
            getTags().clear();
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

    class TagFieldSelectionModel extends MultipleSelectionModel<T> {

        private final ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();

        private final ObservableList<T> selectedItems = FXCollections.observableArrayList();

        public TagFieldSelectionModel() {
            selectedIndices.addListener((Observable it) -> selectedItems.setAll(selectedIndices.stream().map(index -> getTags().get(index)).collect(Collectors.toList())));

            getTags().addListener((javafx.beans.Observable it) -> clearSelection());
        }

        @Override
        public void clearAndSelect(int index) {
            selectedIndices.clear();
            select(index);
        }

        @Override
        public void select(int index) {
            selectedIndices.add(index);
            setSelectedIndex(index);
            setSelectedItem(getTags().get(index));
        }

        @Override
        public void select(T tag) {
            select(getTags().indexOf(tag));
        }

        @Override
        public void clearSelection(int index) {
            selectedIndices.remove((Integer) index);
            if (getSelectedIndex() == index) {
                setSelectedIndex(-1);
                setSelectedItem(null);
            }
        }

        @Override
        public void clearSelection() {
            selectedIndices.clear();
            setSelectedIndex(-1);
            setSelectedItem(null);
        }

        @Override
        public boolean isSelected(int index) {
            return selectedIndices.contains(index);
        }

        @Override
        public boolean isEmpty() {
            return selectedIndices.isEmpty();
        }

        @Override
        public void selectPrevious() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void selectNext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return selectedIndices;
        }

        @Override
        public ObservableList<T> getSelectedItems() {
            return selectedItems;
        }

        @Override
        public void selectRange(int start, int end) {
            super.selectRange(start, end);
        }

        @Override
        public void selectIndices(int index, int... indices) {
            if (!selectedIndices.contains(index)) {
                selectedIndices.add(index);
            }
            for (int i : indices) {
                if (!selectedIndices.contains(i)) {
                    selectedIndices.add(i);
                }
            }
        }

        @Override
        public void selectAll() {
            selectIndices(0, getTags().size() - 1);
        }

        @Override
        public void selectFirst() {
            selectIndices(0);
        }

        @Override
        public void selectLast() {
            selectIndices(getTags().size() - 1);
        }
    }
}
