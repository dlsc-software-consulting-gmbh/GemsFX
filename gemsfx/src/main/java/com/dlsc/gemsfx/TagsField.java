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
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Skin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This field is a specialization of the {@link SearchField} control and supports
 * the additional feature of using the selected object as a tag. Tags are shown in front
 * of the text input field. The control provides an observable list of the currently
 * added tags. In addition, the field also allows the user to select one or more of
 * the tags. The selection state is provided by the selection model. The field adds
 * and removes tags via undoable commands which means that, for example, a deleted tag
 * can be recovered by pressing the standard undo (or redo) shortcut.
 *
 * @param <T> the type of objects to search for and to tag
 */
public class TagsField<T> extends SearchField<T> {

    private static final int MAX_UNDO_AND_REDO_STEPS = 50;

    private static final String DEFAULT_STYLE_CLASS = "tags-field";

    private final Deque<Command> undoStack = new ArrayDeque<>();

    private final Deque<Command> redoStack = new ArrayDeque<>();

    /**
     * Constructs a new tag field.
     */
    public TagsField() {
        getStyleClass().addAll("text-input", DEFAULT_STYLE_CLASS);

        setFocusTraversable(false);

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

        addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = getSelectedItem();
                if (selectedItem != null) {
                    if (!getTags().contains(selectedItem)) {
                        addTags(selectedItem);
                        Platform.runLater(this::clear);
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
                } else if (getText().isEmpty() && !getTags().isEmpty()) {
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
        return Objects.requireNonNull(TagsField.class.getResource("tags-field.css")).toExternalForm();
    }

    /**
     * Overridden to modify the behaviour for the tag field. The override method
     * commits the currently selected item to become a tag.
     */
    @Override
    public void commit() {
        T selectedItem = getSelectedItem();
        if (selectedItem != null && !getTags().contains(selectedItem)) {
            addTags(selectedItem);
            clear();
        }
        getProperties().put("committed", "");
    }

    /**
     * Overridden to remove the already tagged items from the suggestion list.
     *
     * @param newSuggestions the new suggestions to use for the field
     */
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

    /**
     * Used to add one or more tags programmatically. This ensures that tags are added via an undoable command.
     *
     * @param values the value to add as a tag
     */
    @SafeVarargs
    public final void addTags(T... values) {
        execute(new AddTagCommand(values));
    }

    /**
     * Used to remove one or more tags programmatically. This ensures that tags are removed via an undoable command.
     *
     * @param values the tags to add
     */
    @SafeVarargs
    public final void removeTags(T... values) {
        execute(new RemoveTagCommand(values));
    }

    /**
     * Used to clear all tags programmatically. This ensures that tags are cleared via an undoable command.
     */
    public final void clearTags() {
        execute(new ClearTagsCommand(getTags()));
    }

    private final ObjectProperty<Callback<T, Node>> tagViewFactory = new SimpleObjectProperty<>(this, "tagViewFactory");

    /**
     * A callback used to create the nodes that represent the tags. The default
     * implementation uses labels.
     *
     * @return a node factory for the tags
     */
    public final ObjectProperty<Callback<T, Node>> tagViewFactoryProperty() {
        return tagViewFactory;
    }

    public final Callback<T, Node> getTagViewFactory() {
        return tagViewFactory.get();
    }

    public final void setTagViewFactory(Callback<T, Node> tagViewFactory) {
        this.tagViewFactory.set(tagViewFactory);
    }

    private final ObjectProperty<MultipleSelectionModel<T>> tagSelectionModel = new SimpleObjectProperty<>(this, "selectionModel");

    public final MultipleSelectionModel<T> getTagSelectionModel() {
        return tagSelectionModel.get();
    }

    /**
     * A selection model for the tags shown by the field.
     *
     * @return the selection model
     */
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

        private final T[] tags;

        @SafeVarargs
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

        private final T[] tags;

        @SafeVarargs
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

        private final List<T> tags = new ArrayList<>();

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
            setSelectionMode(SelectionMode.MULTIPLE);
            getTags().addListener((javafx.beans.Observable it) -> clearSelection());
            selectionModeProperty().addListener(it -> clearSelection());
        }

        @Override
        public void clearAndSelect(int index) {
            selectedIndices.clear();
            select(index);
        }

        @Override
        public void select(int index) {
            if (getSelectionMode().equals(SelectionMode.SINGLE)) {
                clearSelection();
            }
            if (!selectedIndices.contains(index)) {
                selectedIndices.add(index);
                setSelectedIndex(index);
                setSelectedItem(getTags().get(index));
            }
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
            if (getSelectionMode().equals(SelectionMode.SINGLE)) {
                clearSelection();
                select(end - 1);
            } else {
                super.selectRange(start, end);
            }
        }

        @Override
        public void selectIndices(int index, int... indices) {
            select(index);
            for (int i : indices) {
                select(i);
            }
        }

        @Override
        public void selectAll() {
            selectRange(0, getTags().size());
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
