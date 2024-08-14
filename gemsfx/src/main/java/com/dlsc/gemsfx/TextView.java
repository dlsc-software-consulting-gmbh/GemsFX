package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.TextViewSkin;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.MapChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Skin;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;

import java.util.Objects;

/**
 * A text view that allows you to display multiline text and supports the selection of
 * text (words only), which can then be copied to the clipboard. The user can copy the
 * selected text view the OS-specific shortcut for copying content (for example, CTRL-C
 * on Windows or Command-C on Mac). Additionally, a context menu is available for copying.
 */
public class TextView extends Control {

    /**
     * Constructs a new text view.
     */
    public TextView() {
        getStyleClass().add("text-view");

        getProperties().addListener((MapChangeListener<Object, Object>) change -> {
            if (change.getKey().equals("selected.text")) {
                selectedText.set((String) change.getValueAdded());
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (KeyCodeCombination.keyCombination("shortcut+c").match(evt)) {
                copySelection();
            }
        });

        focusWithinProperty().addListener(it -> {
            ContextMenu contextMenu = getContextMenu();
            if (contextMenu != null && contextMenu.isShowing()) {
                return;
            }
            if (!isFocusWithin()) {
                clearSelection();
            }
        });

        setOnContextMenuRequested(evt -> {
            if (getContextMenu() == null) {
                MenuItem copySelectionItem = new MenuItem("Copy Selection");
                copySelectionItem.setOnAction(e -> copySelection());

                MenuItem copyAllItem = new MenuItem("Copy All");
                copyAllItem.setOnAction(e -> copyAll());

                ContextMenu contextMenu = new ContextMenu(copyAllItem, copySelectionItem);
                setContextMenu(contextMenu);
                contextMenu.show(this, evt.getScreenX(), evt.getScreenY());
            }
        });
    }

    /**
     * Constructs a new text view.
     *
     * @param text The initial text
     */
    public TextView(String text) {
        this();
        setText(text);
    }

    @Override
    public Orientation getContentBias() {
        return Orientation.HORIZONTAL;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new TextViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(TextView.class.getResource("text-view.css")).toExternalForm();
    }

    /**
     * Copy the text to the clipboard. This method is intentionally non-final so
     * that applications can implement their own logic.
     */
    public void copySelection() {
        doCopy(getSelectedText());
    }

    /**
     * Copy the entire text to the clipboard. This method is intentionally non-final so
     * that applications can implement their own logic.
     */
    public void copyAll() {
        doCopy(getText());
    }

    private void doCopy(String text) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        clipboard.setContent(content);
    }

    /**
     * Removes the current selection.
     */
    public final void clearSelection() {
        selectedText.set(null);
    }

    // text

    private final StringProperty text = new SimpleStringProperty(this, "text");

    /**
     * Stores the text displayed by the view.
     *
     * @return the text shown by the control
     */
    public final StringProperty textProperty() {
        return text;
    }

    public final String getText() {
        return textProperty().get();
    }

    public final void setText(String text) {
        textProperty().set(text);
    }

    // selected text

    private final ReadOnlyStringWrapper selectedText = new ReadOnlyStringWrapper(this, "selectedText");

    /**
     * String property used to save selected skin text
     *
     * @return The String property.
     */
    public final String getSelectedText() {
        return selectedText.get();
    }

    public final ReadOnlyStringProperty selectedTextProperty() {
        return selectedText.getReadOnlyProperty();
    }
}