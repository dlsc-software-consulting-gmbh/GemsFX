package com.dlsc.gemsfx.skins;

import javafx.event.Event;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.input.MouseEvent;

public abstract class ToggleVisibilityComboBoxSkin<T extends ComboBoxBase> extends CustomComboBoxSkinBase<T> {

    private boolean showPopupOnMouseRelease = true;
    private boolean mouseInsideTargetNode = false;

    public ToggleVisibilityComboBoxSkin(T control) {
        super(control);
    }

    @Override
    protected void popupOnAutoHide(Event event) {
        showPopupOnMouseRelease = !mouseInsideTargetNode || !showPopupOnMouseRelease;
    }

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

    protected void mouseEntered(MouseEvent mouseEvent) {
        mouseInsideTargetNode = true;
    }

    protected void mouseExited(MouseEvent mouseEvent) {
        mouseInsideTargetNode = false;
    }
}
