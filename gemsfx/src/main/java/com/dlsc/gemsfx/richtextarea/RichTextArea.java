package com.dlsc.gemsfx.richtextarea;

import com.dlsc.gemsfx.skins.RichTextAreaSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.util.function.Consumer;

/**
 * A text area capable to displaying rich text based on a custom rich text
 * model (see {@link RTDocument}).
 *
 * The styling of the text elements can be done via CSS.
 */
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

    /**
     * A text that will be displayed if not document / content has been set on the area.
     *
     * @return the placeholder text
     */
    public final StringProperty placeholderTextProperty() {
        return placeholderText;
    }

    public final void setPlaceholderText(String text) {
        placeholderText.set(text);
    }

    public final String getPlaceholderText() {
        return placeholderText.get();
    }

    private final ObjectProperty<Consumer<String>> hyperlinkHandler = new SimpleObjectProperty<>(this, "hyperlinkHandler");

    public final Consumer<String> getHyperlinkHandler() {
        return hyperlinkHandler.get();
    }

    /**
     * A handler used for hyperlinks when the user clicks on them. This passed in consumer then
     * has to decide what to do with the given link information.
     *
     * @return the hyperlink handler
     */
    public final ObjectProperty<Consumer<String>> hyperlinkHandlerProperty() {
        return hyperlinkHandler;
    }

    public void setHyperlinkHandler(Consumer<String> hyperlinkHandler) {
        this.hyperlinkHandler.set(hyperlinkHandler);
    }

    private final ObjectProperty<RTDocument> document = new SimpleObjectProperty<>(this, "document");

    public final RTDocument getDocument() {
        return document.get();
    }

    /**
     * The model of the rich text area. A document consists of many nested
     * elements.
     *
     * @return the document model
     */
    public final ObjectProperty<RTDocument> documentProperty() {
        return document;
    }

    public final void setDocument(RTDocument document) {
        this.document.set(document);
    }
}
