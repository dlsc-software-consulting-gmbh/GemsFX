package com.dlsc.gemsfx;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;

public class ResizableTextArea extends Control {

    private final TextArea editor = new TextArea();

    public ResizableTextArea() {
        getStyleClass().add("resizable-text-area");
        editor.textProperty().bindBidirectional(textProperty());
    }

    public ResizableTextArea(String text) {
        this();
        setText(text);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new ResizableTextAreaSkin(this);
    }

    public final TextArea getEditor() {
        return editor;
    }

    private final BooleanProperty resizeVertical = new SimpleBooleanProperty(this, "resizeVertical", true);

    public final boolean isResizeVertical() {
        return resizeVertical.get();
    }

    public final BooleanProperty resizeVerticalProperty() {
        return resizeVertical;
    }

    public final void setResizeVertical(boolean resizeVertical) {
        this.resizeVertical.set(resizeVertical);
    }

    private final BooleanProperty resizeHorizontal = new SimpleBooleanProperty(this, "resizeHorizontal", false);

    public final boolean isResizeHorizontal() {
        return resizeHorizontal.get();
    }

    public final BooleanProperty resizeHorizontalProperty() {
        return resizeHorizontal;
    }

    public final void setResizeHorizontal(boolean resizeHorizontal) {
        this.resizeHorizontal.set(resizeHorizontal);
    }

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public final String getText() {
        return text.get();
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    private static class ResizableTextAreaSkin extends SkinBase<ResizableTextArea> {
        private double startX;
        private double startY;
        private double startW;
        private double startH;

        public ResizableTextAreaSkin(ResizableTextArea area) {
            super(area);

            TextArea editor = area.getEditor();

            FontIcon resizeIcon = new FontIcon(MaterialDesign.MDI_RESIZE_BOTTOM_RIGHT);

            StackPane resizeCorner = new StackPane(resizeIcon);
            resizeCorner.getStyleClass().add("resize-corner");
            resizeCorner.setPrefSize(10, 10);
            resizeCorner.setMaxSize(10, 10);
            resizeCorner.setOnMousePressed(evt -> {
                startX = evt.getScreenX();
                startY = evt.getScreenY();
                startW = editor.getWidth();
                startH = editor.getHeight();
            });

            resizeCorner.setOnMouseDragged(evt -> {
                double screenX = evt.getScreenX();
                double screenY = evt.getScreenY();

                double deltaX = screenX - startX;
                double deltaY = screenY - startY;

                double w = startW + deltaX;
                double h = startH + deltaY;

                if (editor.getMaxWidth() > 0) {
                    w = Math.min(editor.getMaxWidth(), w);
                }

                if (editor.getMaxHeight() > 0) {
                    h = Math.min(editor.getMaxHeight(), h);
                }

                if (area.isResizeHorizontal()) {
                    editor.setPrefWidth(w);
                }

                if (area.isResizeVertical()) {
                    editor.setPrefHeight(h);
                }
            });
            StackPane.setAlignment(resizeCorner, Pos.BOTTOM_RIGHT);

            StackPane pane = new StackPane(editor, resizeCorner);
            getChildren().setAll(pane);
        }
    }
}
