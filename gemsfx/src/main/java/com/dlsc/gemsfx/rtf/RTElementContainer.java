package com.dlsc.gemsfx.rtf;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class RTElementContainer<SELF extends RTElementContainer> extends RTElement<SELF> {

    private ListProperty<RTElement> elements = new SimpleListProperty<>(this, "elements", FXCollections.observableArrayList());

    protected RTElementContainer() {
    }

    public SELF withElements(RTElement... elements) {
        this.elements.setAll(elements);
        return (SELF) this;
    }

    public ObservableList<RTElement> getElements() {
        return elements.get();
    }
}
