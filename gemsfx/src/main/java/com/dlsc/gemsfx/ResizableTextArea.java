package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.ResizableTextAreaSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;
import javafx.scene.control.TextArea;

public class ResizableTextArea extends TextArea {

    private static final boolean DEFAULT_RESIZE_VERTICAL = true;
    private static final boolean DEFAULT_RESIZE_HORIZONTAL = false;

    public ResizableTextArea() {
        this("");
    }

    public ResizableTextArea(String text) {
        super(text);
        getStyleClass().add("resizable-text-area");
        setFocusTraversable(false);
        getStylesheets().add(getUserAgentStylesheet());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResizableTextAreaSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return ResizableTextArea.class.getResource("resizable-text-area.css").toExternalForm();
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
