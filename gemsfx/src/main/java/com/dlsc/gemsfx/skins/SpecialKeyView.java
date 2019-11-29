package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.keyboard.Keyboard;
import com.dlsc.gemsfx.keyboard.Keyboard.SpecialKey;
import com.dlsc.gemsfx.keyboard.KeyboardView;
import com.dlsc.gemsfx.keyboard.KeyboardView.Mode;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.scene.Node;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.PopOver;

import java.util.function.Consumer;

public class SpecialKeyView extends KeyViewBase<SpecialKey> {

    public static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    public SpecialKeyView(KeyboardView keyboardView, SpecialKey key) {
        super(keyboardView, key);

        getStyleClass().add("special");
        getStyleClass().add(key.getType().name().toLowerCase().replace("_", "-"));

        final Node specialNode = keyboardView.getSpecialKeyFactory().call(key.getType());
        if (specialNode != null) {
            getChildren().add(specialNode);
            StackPane.setAlignment(specialNode, key.getAlignment());
        }

        disableProperty().bind(Bindings.createBooleanBinding(() -> {
            switch (getKey().getType()) {
                case SHIFT:
                case CAPS:
                    /*
                     * Disable the SHIFT and CAPS keys if the keyboard is currently displaying
                     * the special characters / the symbols.
                     */
                    return keyboardView.getMode().equals(Mode.SYMBOLS);
                default:
                    return false;
            }
        }, keyboardView.modeProperty()));

        keyboardView.modeProperty().addListener(it -> updateSelection());
        updateSelection();

        setOnMouseClicked(this::handleClick);
    }

    private void updateSelection() {
        switch (getKey().getType()) {
            case SHIFT:
                pseudoClassStateChanged(SELECTED, getKeyboardView().getMode().equals(Mode.SHIFT));
                break;
            case CAPS:
                pseudoClassStateChanged(SELECTED, getKeyboardView().getMode().equals(Mode.CAPS));
                break;
            case SPECIAL_CHARACTERS:
                pseudoClassStateChanged(SELECTED, getKeyboardView().getMode().equals(Mode.SYMBOLS));
                break;
            default:
                break;
        }
    }

    private void handleClick(MouseEvent evt) {
        switch (getKey().getType()) {
            case SPACE:
                KeyEvent ke = new KeyEvent(
                        KeyEvent.KEY_TYPED,
                        " ",
                        " ",
                        null,
                        evt.isShiftDown(),
                        false,
                        false,
                        false);

                final Node focusOwner = getKeyboardView().getScene().getFocusOwner();
                if (focusOwner != null) {
                    focusOwner.fireEvent(ke);
                }

                break;
            case SHIFT:
                if (getKeyboardView().getMode().equals(Mode.STANDARD)) {
                    getKeyboardView().setMode(Mode.SHIFT);
                } else {
                    getKeyboardView().setMode(Mode.STANDARD);
                }
                break;
            case CAPS:
                if (getKeyboardView().getMode().equals(Mode.CAPS)) {
                    getKeyboardView().setMode(Mode.STANDARD);
                } else {
                    getKeyboardView().setMode(Mode.CAPS);
                }
                break;
            case ENTER:
                firePressedEvent(KeyCode.ENTER);
                break;
            case TAB:
                firePressedEvent(KeyCode.TAB);
                break;
            case BACKSPACE:
                firePressedEvent(KeyCode.BACK_SPACE);
                break;
            case KEYBOARDS:
                showKeyboardSelector();
                break;
            case SPECIAL_CHARACTERS:
                if (getKeyboardView().getMode().equals(Mode.SYMBOLS)) {
                    getKeyboardView().setMode(Mode.STANDARD);
                } else {
                    getKeyboardView().setMode(Mode.SYMBOLS);
                }
                break;
            case MICROPHONE:
                System.out.println("not supported, yet");
                break;
            case HIDE:
                getKeyboardView().getOnClose().run();
                break;
            case PLUS:
                firePressedEvent(KeyCode.PLUS);
                break;
            case MINUS:
                firePressedEvent(KeyCode.MINUS);
                break;
            case MULTIPLY:
                firePressedEvent(KeyCode.MULTIPLY);
                break;
            case DIVIDE:
                firePressedEvent(KeyCode.DIVIDE);
                break;
            case EQUALS:
                firePressedEvent(KeyCode.EQUALS);
                break;
            case UP:
                firePressedEvent(KeyCode.UP);
                break;
            case DOWN:
                firePressedEvent(KeyCode.DOWN);
                break;
            case LEFT:
                firePressedEvent(KeyCode.LEFT);
                break;
            case RIGHT:
                firePressedEvent(KeyCode.RIGHT);
                break;
        }

        final Consumer<SpecialKey> specialKeyCallback = getKeyboardView().getSpecialKeyCallback();
        if (specialKeyCallback != null) {
            specialKeyCallback.accept(getKey());
        }
    }

    private PopOver popOver;

    public void showKeyboardSelector() {
        if (popOver != null) {
            popOver.hide();
        }

        popOver = new PopOver();
        popOver.setArrowSize(0);
        popOver.setAutoFix(true);

        VBox vBox = new VBox();
        vBox.setFillWidth(true);
        vBox.getStyleClass().add("keyboard-selector");

        for (int i = 0; i < getKeyboardView().getKeyboards().size(); i++) {
            Keyboard keyboard = getKeyboardView().getKeyboards().get(i);
            ToggleButton button = new ToggleButton(keyboard.getName());
            button.setSelected(getKeyboardView().getSelectedKeyboard() == keyboard);
            button.setMaxWidth(Double.MAX_VALUE);
            button.setMaxHeight(Double.MAX_VALUE);
            button.setOnAction(evt -> {
                popOver.hide();
                getKeyboardView().setSelectedKeyboard(keyboard);
            });
            VBox.setVgrow(button, Priority.ALWAYS);
            vBox.getChildren().add(button);
            if (i == getKeyboardView().getKeyboards().size() - 1) {
                button.getStyleClass().add("last");
            }
        };

        popOver.setContentNode(vBox);
        popOver.show(this);
    }

    private void firePressedEvent(KeyCode code) {
        KeyEvent ke = new KeyEvent(
                KeyEvent.KEY_PRESSED,
                "",
                code.getName(),
                code,
                false,
                false,
                false,
                false);

        final Node focusOwner = getScene().getFocusOwner();
        if (focusOwner != null) {
            focusOwner.fireEvent(ke);
        }
    }
}
