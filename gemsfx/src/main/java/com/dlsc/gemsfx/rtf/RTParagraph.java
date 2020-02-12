package com.dlsc.gemsfx.rtf;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;

public class RTParagraph extends RTElementContainer<RTParagraph> {

    private ListProperty<RTElement> elements = new SimpleListProperty<>(this, "elements", FXCollections.observableArrayList());

    private RTParagraph() {
    }

    public static RTParagraph create(RTElement... elements) {
        return new RTParagraph().withElements(elements);
    }
}
