package com.dlsc.gemsfx.rtf;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;

public class RichTextArea2 extends Control {

    public RichTextArea2() {
        getStyleClass().add("rich-text-area");
    }

    @Override
    public String getUserAgentStylesheet() {
        return RichTextArea2.class.getResource("rtf.css").toExternalForm();
    }

    private final ObjectProperty<RTDocument> document = new SimpleObjectProperty<>(this, "document");

    public final RTDocument getDocument() {
        return document.get();
    }

    public final ObjectProperty<RTDocument> documentProperty() {
        return document;
    }

    public final void setDocument(RTDocument document) {
        this.document.set(document);
    }
}
