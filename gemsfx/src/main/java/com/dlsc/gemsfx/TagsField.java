package com.dlsc.gemsfx;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Skin;

public class TagsField<T> extends SearchField<T> {

    private static final String DEFAULT_STYLE_CLASS = "tags-field";

    public TagsField() {
        setShowSearchIcon(false);
        getStyleClass().addAll("text-input", DEFAULT_STYLE_CLASS);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TagsFieldSkin<>(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return TagsField.class.getResource("tags-field.css").toExternalForm();
    }

    private final ListProperty<T> tags = new SimpleListProperty<>(this, "tags", FXCollections.observableArrayList());

    public ObservableList<T> getTags() {
        return tags.get();
    }

    public ListProperty<T> tagsProperty() {
        return tags;
    }

    public void setTags(ObservableList<T> tags) {
        this.tags.set(tags);
    }
}
