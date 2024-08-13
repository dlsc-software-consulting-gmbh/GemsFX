package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.CustomLabelSkin;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

import java.util.Objects;

/**
 * A custom label that allows you to select a text to be copied to the clipboard.
 */
public class CustomLabel extends Control {

    /**
     * Instances a new Custom Label, with no initial text.
     */
    public CustomLabel() {
        this(null);
    }

    /**
     * Instances a new Custom Label.
     * @param text The initial text
     */
    public CustomLabel(String text) {
        super();
        setText(text);
        listenPropertySelectedTextChanged();
        getStyleClass().add("custom-label");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new CustomLabelSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(CustomLabel.class.getResource("custom-label.css")).toExternalForm();
    }

    /**
     * Save the text to the clipboard
     */
    public final void copySelectionToClipboard() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(getSelectedText());
        clipboard.setContent(content);
    }

    /**
     * Removes the current selection.
     */
    public final void clearSelection() {
        setSelectedText(null);
    }

    /**
     * String property used to automatically change the text of the skin
     * @return The String property.
     */
    public final StringProperty textProperty() { return text; }
    public final String getText() { return textProperty().get(); }
    private final StringProperty text = new SimpleStringProperty(this, "text");
    public final void setText(String text) { textProperty().set(text); }

    /**
     * String property used to save selected skin text
     * @return The String property.
     */
    public final String getSelectedText() {  return selectedText.get();  }
    public final ReadOnlyStringProperty selectedTextProperty() {  return selectedText.getReadOnlyProperty(); }
    private ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper(this, "selectedText");
    private final void setSelectedText(String selectedText) {  this.selectedText.set(selectedText);  }

    // listeners
    private void listenPropertySelectedTextChanged() {
        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.getKey().equals("selectedText")) {
                setSelectedText((String) change.getValueAdded());
            }
        });
    }

}