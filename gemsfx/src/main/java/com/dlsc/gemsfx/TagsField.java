package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TagsFieldSkin;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.util.Callback;

import java.util.Collection;

public class TagsField<T> extends SearchField<T> {

    private static final String DEFAULT_STYLE_CLASS = "tags-field";

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
}
