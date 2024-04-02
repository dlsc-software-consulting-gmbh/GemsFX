package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.util.Utils;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ObservableValue;
import javafx.css.Styleable;
import javafx.event.Event;
import javafx.geometry.Bounds;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.AccessibleAttribute;
import javafx.scene.Node;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.PopupControl;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Skinnable;
import javafx.scene.layout.Region;
import javafx.stage.WindowEvent;

public abstract class CustomComboBoxSkinBase<T extends ComboBoxBase> extends SkinBase<T> {

    private boolean popupNeedsReconfiguring = true;

    public CustomComboBoxSkinBase(T control) {
        super(control);

        control.showingProperty().addListener(it -> {
            if (control.isShowing()) {
                show();
            } else if (popup != null) {
                hide();
            }
        });

        control.focusedProperty().addListener(it -> {
            if (!control.isFocused()) {
                hide();
            }
        });

    }

    private PopupControl popup;

    private PopupControl getPopup() {
        if (popup == null) {
            createPopup();
        }
        return popup;
    }

    protected abstract Node getPopupContent();

    private void positionAndShowPopup() {
        ComboBoxBase skinnable = getSkinnable();
        if (skinnable.getScene() == null) {
            return;
        }

        getPopup();

        popup.getScene().setNodeOrientation(getSkinnable().getEffectiveNodeOrientation());

        Node popupContent = getPopupContent();
        sizePopup();

        Point2D p = getPrefPopupPosition();

        popupNeedsReconfiguring = true;
        reconfigurePopup();

        popup.show(skinnable.getScene().getWindow(),
                snapPositionX(p.getX()),
                snapPositionY(p.getY()));

        popupContent.requestFocus();

        // second call to sizePopup here to enable proper sizing _after_ the popup
        // has been displayed. See RT-37622 for more detail.
        sizePopup();
    }

    private Point2D getPrefPopupPosition() {
        return Utils.pointRelativeTo(getSkinnable(), getPopupContent(), HPos.CENTER, VPos.BOTTOM, 0, 0, true);
    }

    private void sizePopup() {
        Node popupContent = getPopupContent();

        if (popupContent instanceof Region) {
            Region r = (Region) popupContent;
            // snap to pixel

            // 0 is used here for the width due to RT-46097
            double prefHeight = snapSizeY(r.prefHeight(0));
            double minHeight = snapSizeY(r.minHeight(0));
            double maxHeight = snapSizeY(r.maxHeight(0));
            double h = snapSizeY(Math.min(Math.max(prefHeight, minHeight), Math.max(minHeight, maxHeight)));

            double prefWidth = snapSizeX(r.prefWidth(h));
            double minWidth = snapSizeX(r.minWidth(h));
            double maxWidth = snapSizeX(r.maxWidth(h));
            double w = snapSizeX(Math.min(Math.max(prefWidth, minWidth), Math.max(minWidth, maxWidth)));

            popupContent.resize(w, h);
        } else {
            popupContent.autosize();
        }
    }

    private void createPopup() {
        popup = new PopupControl() {
            @Override
            public Styleable getStyleableParent() {
                return getSkinnable();
            }

            {
                setSkin(new Skin<>() {
                    @Override
                    public Skinnable getSkinnable() {
                        return popup;
                    }

                    @Override
                    public Node getNode() {
                        return getPopupContent();
                    }

                    @Override
                    public void dispose() {
                    }
                });
            }
        };

        popup.setConsumeAutoHidingEvents(false);
        popup.setAutoHide(true);
        popup.setAutoFix(true);
        popup.setHideOnEscape(true);
        popup.addEventHandler(WindowEvent.WINDOW_HIDDEN, t -> {
            // Make sure the accessibility focus returns to the combo box
            // after the window closes.
            getSkinnable().notifyAccessibleAttributeChanged(AccessibleAttribute.FOCUS_NODE);
            getSkinnable().hide();
        });
        popup.setOnAutoHide(this::popupOnAutoHide);

        // Fix for RT-21207
        InvalidationListener layoutPosListener = o -> {
            popupNeedsReconfiguring = true;
            reconfigurePopup();
        };
        getSkinnable().layoutXProperty().addListener(layoutPosListener);
        getSkinnable().layoutYProperty().addListener(layoutPosListener);
        getSkinnable().widthProperty().addListener(layoutPosListener);
        getSkinnable().heightProperty().addListener(layoutPosListener);

        // RT-36966 - if skinnable's scene becomes null, ensure popup is closed
        getSkinnable().sceneProperty().addListener(o -> {
            if (((ObservableValue) o).getValue() == null) {
                hide();
            } else if (getSkinnable().isShowing()) {
                show();
            }
        });

    }

    void reconfigurePopup() {
        // RT-26861. Don't call getPopup() here because it may cause the popup
        // to be created too early, which leads to memory leaks like those noted
        // in RT-32827.
        if (popup == null) {
            return;
        }

        boolean isShowing = popup.isShowing();
        if (!isShowing) {
            return;
        }

        if (!popupNeedsReconfiguring) {
            return;
        }

        popupNeedsReconfiguring = false;

        Point2D p = getPrefPopupPosition();

        Node popupContent = getPopupContent();
        double minWidth = popupContent.prefWidth(Region.USE_COMPUTED_SIZE);
        double minHeight = popupContent.prefHeight(Region.USE_COMPUTED_SIZE);

        if (p.getX() > -1) popup.setAnchorX(p.getX());
        if (p.getY() > -1) popup.setAnchorY(p.getY());
        if (minWidth > -1) popup.setMinWidth(minWidth);
        if (minHeight > -1) popup.setMinHeight(minHeight);

        Bounds b = popupContent.getLayoutBounds();
        double currentWidth = b.getWidth();
        double currentHeight = b.getHeight();
        double newWidth = Math.max(currentWidth, minWidth);
        double newHeight = Math.max(currentHeight, minHeight);

        if (newWidth != currentWidth || newHeight != currentHeight) {
            // Resizing content to resolve issues such as RT-32582 and RT-33700
            // (where RT-33700 was introduced due to a previous fix for RT-32582)
            popupContent.resize(newWidth, newHeight);
            if (popupContent instanceof Region) {
                ((Region) popupContent).setMinSize(newWidth, newHeight);
                ((Region) popupContent).setPrefSize(newWidth, newHeight);
            }
        }
    }

    protected void popupOnAutoHide(Event event) {
    }

    public void show() {
        T control = getSkinnable();
        if (control == null) {
            throw new IllegalStateException("ComboBox is null");
        }
        control.requestFocus();

        Node content = getPopupContent();
        if (content == null) {
            throw new IllegalStateException("Popup node is null");
        }

        positionAndShowPopup();
    }

    public void hide() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
        }
    }
}
