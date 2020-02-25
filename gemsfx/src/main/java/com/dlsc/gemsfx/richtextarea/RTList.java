package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RTList extends RTElement<RTList> {

    private final ListProperty<RTListItem> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    private int start = 1;

    private Type type = Type.UNORDERED;

    public enum Type {
        ORDERED,
        UNORDERED
    }

    private RTList() {
    }

    public static RTList create(RTListItem... items) {
        return new RTList().withItems(items);
    }

    public RTList withItems(RTListItem... items) {
        this.items.setAll(items);
        return this;
    }

    public RTList withStart(int start) {
        this.start = start;
        return this;
    }

    public RTList withType(Type type) {
        this.type = type;
        return this;
    }

    public int getStart() {
        return start;
    }

    public Type getType() {
        return type;
    }

    public ObservableList<RTListItem> getItems() {
        return items.get();
    }
}
