package com.dlsc.gemsfx.rtf;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class RTList extends RTElement<RTList> {

    private final ListProperty<RTListItem> items = new SimpleListProperty<>(this, "items", FXCollections.observableArrayList());

    private RTList() {
    }

    public static RTList create(RTListItem... items) {
        return new RTList().withItems(items);
    }

    public RTList withItems(RTListItem... items) {
        this.items.setAll(items);
        return this;
    }
}
