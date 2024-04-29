package com.dlsc.gemsfx;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * An enhanced label that allows for selecting the (whole) label and copying to the clipboard
 * either via keyboard shortcut or via context menu.
 */
public class EnhancedLabel extends Label {

    private static final PseudoClass SELECTED_PSEUDO_CLASS = PseudoClass.getPseudoClass("selected");

    public EnhancedLabel() {
        init();
    }

    public EnhancedLabel(String text) {
        super(text);
        init();
    }

    public EnhancedLabel(String text, Node node) {
        super(text, node);
        init();
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EnhancedLabel.class.getResource("enhanced-label.css")).toExternalForm();
    }

    private void init() {
        getStyleClass().add("enhanced-label");

        setFocusTraversable(true);

        KeyCombination copy = new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN);

        setOnKeyPressed(evt -> {
            if (copy.match(evt)) {
                copyText();
            }
        });

        setOnMouseClicked(evt -> {
            if (evt.getButton().equals(MouseButton.PRIMARY) && evt.getClickCount() == 2) {
                setSelected(true);
            }
        });

        selectedProperty().addListener(it -> {
            pseudoClassStateChanged(SELECTED_PSEUDO_CLASS, isSelected());
            if (isSelected() && !isFocused()) {
                requestFocus();
            }
        });

        focusedProperty().addListener(it -> {
            if (!isFocused()) {
                setSelected(isFocused());
            }
        });

        MenuItem copyItem = new MenuItem();
        copyItem.textProperty().bind(copyMenuItemTextProperty());
        copyItem.setOnAction(event -> {
            EventHandler<ActionEvent> handler = getOnCopyAction();
            if (handler != null) {
                handler.handle(event);
            }
        });

        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(copyItem);

        setContextMenu(contextMenu);

        setOnContextMenuRequested(evt -> setSelected(true));
    }

    private void copyText() {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(getCopyContentSupplier().get());
        clipboard.setContent(content);
    }

    private void copyText(ActionEvent actionEvent) {
        copyText();
        actionEvent.consume();
    }

    private ObjectProperty<EventHandler<ActionEvent>> onCopyAction;

    public final void setOnCopyAction(EventHandler<ActionEvent> value) {
        onCopyActionProperty().set(value);
    }

    public final EventHandler<ActionEvent> getOnCopyAction() {
        return onCopyAction == null ? this::copyText : onCopyAction.get();
    }

    /**
     * The action that is executed when the user copies the label text. The default action
     * is to copy the text to the clipboard. This action can be overridden by setting a new
     * event handler.
     *
     * @return the action that is executed when the user copies the label text
     */
    public final ObjectProperty<EventHandler<ActionEvent>> onCopyActionProperty() {
        if (onCopyAction == null) {
            onCopyAction = new SimpleObjectProperty<>(this, "onCopyAction", this::copyText);
        }
        return onCopyAction;
    }

    private final BooleanProperty selected = new SimpleBooleanProperty(this, "selected");

    public final boolean isSelected() {
        return selected.get();
    }

    /**
     * Determines whether the label is currently selected or not.
     *
     * @return true if the label is selected
     */
    public final BooleanProperty selectedProperty() {
        return selected;
    }

    public final void setSelected(boolean selected) {
        this.selected.set(selected);
    }

    private final StringProperty copyMenuItemText = new SimpleStringProperty(this, "copyMenuItemText", "Copy text");

    public final String getCopyMenuItemText() {
        return copyMenuItemText.get();
    }

    /**
     * The text used for the "copy" menu item.
     *
     * @return the copy menu item name
     */
    public final StringProperty copyMenuItemTextProperty() {
        return copyMenuItemText;
    }

    public final void setCopyMenuItemText(String copyMenuItemText) {
        this.copyMenuItemText.set(copyMenuItemText);
    }

    private final ObjectProperty<Supplier<String>> copyContentSupplier = new SimpleObjectProperty<>(this, "copyContentProvider", this::getText);

    public final Supplier<String> getCopyContentSupplier() {
        return copyContentSupplier.get();
    }

    /**
     * Stores the supplier used for filling the clipboard when the user copies the label.
     * Sometimes applications want to copy only a part of the label, e.g. an ID shown by
     * the label like this: "Customer account #12345678". The copy content supplier can then
     * decide to just return the ID "12345678" instead of the whole string. The default
     * supplier however returns the whole string.
     *
     * @return the supplier for the clipboard content when the user copies the label
     */
    public final ObjectProperty<Supplier<String>> copyContentSupplierProperty() {
        return copyContentSupplier;
    }

    public final void setCopyContentSupplier(Supplier<String> copyContentSupplier) {
        this.copyContentSupplier.set(copyContentSupplier);
    }
}
