package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.ReadOnlyListWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class RTElementContainer<SELF extends RTElementContainer> extends RTElement<SELF> {

    private ReadOnlyListWrapper<RTElement> elements = new ReadOnlyListWrapper<>(this, "elements", FXCollections.observableArrayList());

    protected RTElementContainer() {
    }

    public SELF withElements(RTElement... elements) {
        this.elements.setAll(elements);
        return (SELF) this;
    }

    public ObservableList<RTElement> getElements() {
        return elements.getReadOnlyProperty();
    }
}
