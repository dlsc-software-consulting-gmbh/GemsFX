package com.dlsc.gemsfx.richtextarea;

import com.dlsc.gemsfx.skins.RichTextAreaSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

public class RichTextArea extends Control {

    public RichTextArea() {
        getStyleClass().add("rich-text-area");
        setHyperlinkHandler(url -> System.out.println("link pressed: " + url));
    }

    @Override
    public String getUserAgentStylesheet() {
        return RichTextArea.class.getResource("richtextarea.css").toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new RichTextAreaSkin(this);
    }

    private final StringProperty placeholderText = new SimpleStringProperty(this, "placeholderText", "No content");

    public final StringProperty placeholderTextProperty() {
        return placeholderText;
    }

    public void setPlaceholderText(String text) {
        placeholderText.set(text);
    }

    public final String getPlaceholderText() {
        return placeholderText.get();
    }

    private final ObjectProperty<Consumer<String>> hyperlinkHandler = new SimpleObjectProperty<>(this, "hyperlinkHandler");

    public Consumer<String> getHyperlinkHandler() {
        return hyperlinkHandler.get();
    }

    public ObjectProperty<Consumer<String>> hyperlinkHandlerProperty() {
        return hyperlinkHandler;
    }

    public void setHyperlinkHandler(Consumer<String> hyperlinkHandler) {
        this.hyperlinkHandler.set(hyperlinkHandler);
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
