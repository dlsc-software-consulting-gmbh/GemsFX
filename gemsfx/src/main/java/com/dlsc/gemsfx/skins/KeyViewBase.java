package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.keyboard.Keyboard.KeyBase;
import com.dlsc.gemsfx.keyboard.KeyboardView;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.util.Objects;

public abstract class KeyViewBase<T extends KeyBase> extends StackPane {

    private T key;
    private KeyboardView keyboardView;

    protected KeyViewBase(KeyboardView keyboardView, T key) {
        this.keyboardView = Objects.requireNonNull(keyboardView, "keyboard view can not be null");
        this.key = Objects.requireNonNull(key, "key can not be null");
        getStyleClass().add("key");
        getStyleClass().addAll(key.getStyleClass());
    }

    protected KeyboardView getKeyboardView() {
        return keyboardView;
    }

    protected T getKey() {
        return key;
    }

    protected Label createLabel(String text) {
        final Label label = new Label(text);
        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER);
        label.setTextAlignment(TextAlignment.CENTER);
        VBox.setVgrow(label, Priority.ALWAYS);
        return label;
    }
}