package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ResizableTextAreaSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;

import java.util.Objects;

/**
 * A text area with the additional ability to be resizable. The resizing behaviour can
 * be configured to support resizing in vertical and horizontal direction or only in one
 * of them. Resizing can also be turned off completely. When resizable the text area will
 * display a resize icon in the lower right corner.
 */
public class ResizableTextArea extends TextArea {

    private static final boolean DEFAULT_RESIZE_VERTICAL = true;
    private static final boolean DEFAULT_RESIZE_HORIZONTAL = false;

    /**
     * Constructs a new resizable text area.
     */
    public ResizableTextArea() {
        getStyleClass().add("resizable-text-area");
    }

    /**
     * Constructs a new resizable text area with the given text.
     */
    public ResizableTextArea(String text) {
        this();
        setText(text);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResizableTextAreaSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(ResizableTextArea.class.getResource("resizable-text-area.css")).toExternalForm();
    }

    private BooleanProperty resizeVertical;

    public final boolean isResizeVertical() {
        return resizeVertical == null ? DEFAULT_RESIZE_VERTICAL : resizeVertical.get();
    }

    /**
     * Defines whether the ResizableTextArea supports vertical resizing.
     * If true, users can adjust the height of the ResizableTextArea.
     * If false, the height is fixed.
     * <p>
     * The default setting is true.
     *
     * @return a BooleanProperty representing the vertical resize capability.
     */
    public final BooleanProperty resizeVerticalProperty() {
        if (resizeVertical == null) {
            resizeVertical = new SimpleBooleanProperty(this, "resizeVertical", DEFAULT_RESIZE_VERTICAL);
        }
        return resizeVertical;
    }

    public final void setResizeVertical(boolean resizeVertical) {
        resizeVerticalProperty().set(resizeVertical);
    }

    private BooleanProperty resizeHorizontal;

    public final boolean isResizeHorizontal() {
        return resizeHorizontal == null ? DEFAULT_RESIZE_HORIZONTAL : resizeHorizontal.get();
    }

    /**
     * Defines whether the ResizableTextArea supports horizontal resizing.
     * If true, users can adjust the width of the ResizableTextArea.
     * If false, the width is fixed.
     * <p>
     * The default setting is false.
     *
     * @return a BooleanProperty representing the horizontal resize capability.
     */
    public final BooleanProperty resizeHorizontalProperty() {
        if (resizeHorizontal == null) {
            resizeHorizontal = new SimpleBooleanProperty(this, "resizeHorizontal", DEFAULT_RESIZE_HORIZONTAL);
        }
        return resizeHorizontal;
    }

    public final void setResizeHorizontal(boolean resizeHorizontal) {
        resizeHorizontalProperty().set(resizeHorizontal);
    }
}
