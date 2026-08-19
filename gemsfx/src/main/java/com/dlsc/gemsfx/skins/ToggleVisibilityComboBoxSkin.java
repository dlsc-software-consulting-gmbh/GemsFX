package com.dlsc.gemsfx.skins;

import javafx.event.Event;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * Base skin for combo-box controls that toggle popup visibility from a target node.
 * <p>
 * The skin coordinates mouse release handling with popup auto-hide behavior so
 * clicking the trigger does not immediately reopen a just-hidden popup.
 *
 * @param <T> the combo-box control type
 */
public abstract class ToggleVisibilityComboBoxSkin<T extends ComboBoxBase> extends CustomComboBoxSkinBase<T> {

    private boolean showPopupOnMouseRelease = true;
    private boolean mouseInsideTargetNode = false;

    /**
     * Creates a new toggle-visibility skin for the given control.
     *
     * @param control the combo-box control to skin
     */
    public ToggleVisibilityComboBoxSkin(T control) {
        super(control);

        // Pressed the esc key to hide the popup.
        control.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                showPopupOnMouseRelease = true;
                hide();
            }
        });
    }

    @Override
    protected void popupOnAutoHide(Event event) {
        showPopupOnMouseRelease = !mouseInsideTargetNode || !showPopupOnMouseRelease;
    }

    /**
     * Handles mouse release on the popup trigger node.
     *
     * @param mouseEvent the mouse release event
     */
    protected void mouseReleased(MouseEvent mouseEvent) {
        // The showPopupOnMouseRelease boolean was added to resolve
        // RT-18151: namely, clicking on the comboBox button shouldn't hide,
        // and then immediately show the popup, which was occurring because we
        // can't know whether the popup auto-hide was coming because of a MOUSE_PRESS
        // since PopupWindow calls hide() before it calls onAutoHide().

        if (showPopupOnMouseRelease) {
            show();
        } else {
            showPopupOnMouseRelease = true;
            hide();
        }
    }

    /**
     * Records that the mouse entered the popup trigger node.
     *
     * @param mouseEvent the mouse entered event
     */
    protected void mouseEntered(MouseEvent mouseEvent) {
        mouseInsideTargetNode = true;
    }

    /**
     * Records that the mouse exited the popup trigger node.
     *
     * @param mouseEvent the mouse exited event
     */
    protected void mouseExited(MouseEvent mouseEvent) {
        mouseInsideTargetNode = false;
    }
}
