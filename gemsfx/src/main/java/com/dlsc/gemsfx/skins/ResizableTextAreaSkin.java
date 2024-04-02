package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ResizableTextArea;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.HPos;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.skin.TextAreaSkin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

public class ResizableTextAreaSkin extends TextAreaSkin {

    private static final PseudoClass RESIZE_HORIZONTAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("h-resize");
    private static final PseudoClass RESIZE_VERTICAL_PSEUDO_CLASS = PseudoClass.getPseudoClass("v-resize");
    private static final PseudoClass RESIZE_BOTH_PSEUDO_CLASS = PseudoClass.getPseudoClass("both-resize");
    private static final PseudoClass RESIZE_NONE_PSEUDO_CLASS = PseudoClass.getPseudoClass("no-resize");

    protected final StackPane resizeCorner;
    protected final ScrollPane scrollPane;
    protected final StackPane contentPane;
    private double startX;
    private double startY;
    private double startW;
    private double startH;

    protected ScrollBar verticalScrollBar;
    protected ScrollBar horizontalScrollBar;

    public ResizableTextAreaSkin(ResizableTextArea control) {
        super(control);

        scrollPane = (ScrollPane) getChildren().get(0);
        Region resizeIcon = new Region();
        resizeIcon.getStyleClass().add("resize-icon");
        resizeCorner = new StackPane(resizeIcon);
        resizeCorner.getStyleClass().add("resize-corner");
        resizeCorner.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        contentPane = new StackPane(scrollPane, resizeCorner);
        contentPane.getStyleClass().add("content-pane");
        StackPane.setAlignment(resizeCorner, Pos.BOTTOM_RIGHT);

        getChildren().setAll(contentPane);

        // apply the pseudo classes
        registerChangeListener(control.resizeHorizontalProperty(), it -> updateResizePseudoClasses());
        registerChangeListener(control.resizeVerticalProperty(), it -> updateResizePseudoClasses());
        updateResizePseudoClasses();

        // add the resize event handler
        addResizeEventHandler();
    }

    private void updateResizePseudoClasses() {
        ResizableTextArea control = (ResizableTextArea) getSkinnable();

        boolean horResize = control.isResizeHorizontal();
        boolean verResize = control.isResizeVertical();
        boolean bothResize = horResize && verResize;
        boolean noneResize = !horResize && !verResize;

        // only one direction can be resized apply the corresponding pseudo class
        resizeCorner.pseudoClassStateChanged(RESIZE_HORIZONTAL_PSEUDO_CLASS, horResize && !bothResize);
        resizeCorner.pseudoClassStateChanged(RESIZE_VERTICAL_PSEUDO_CLASS, verResize && !bothResize);

        // both directions can be resized apply the corresponding pseudo class
        resizeCorner.pseudoClassStateChanged(RESIZE_BOTH_PSEUDO_CLASS, bothResize);

        // no direction can be resized apply the corresponding pseudo class
        resizeCorner.pseudoClassStateChanged(RESIZE_NONE_PSEUDO_CLASS, noneResize);
    }

    private void addResizeEventHandler() {
        ResizableTextArea control = (ResizableTextArea) getSkinnable();

        resizeCorner.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            control.requestFocus();

            startX = event.getScreenX();
            startY = event.getScreenY();
            startW = control.getWidth();
            startH = control.getHeight();

            event.consume();
        });

        resizeCorner.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            double w = startW + event.getScreenX() - startX;
            double h = startH + event.getScreenY() - startY;

            if (control.getMaxWidth() > 0) {
                w = Math.min(control.getMaxWidth(), w);
            }

            if (control.getMaxHeight() > 0) {
                h = Math.min(control.getMaxHeight(), h);
            }

            if (control.isResizeHorizontal()) {
                control.setPrefWidth(w);
            }

            if (control.isResizeVertical()) {
                control.setPrefHeight(h);
            }
            event.consume();
        });
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        if (verticalScrollBar == null || horizontalScrollBar == null) {
            findScrollBar();
            bindResizeCornerPosition();
        }

        layoutInArea(contentPane, contentX, contentY, contentWidth, contentHeight, 0, HPos.LEFT, VPos.TOP);
    }

    private void findScrollBar() {
        scrollPane.getChildrenUnmodifiable().forEach(node -> {
            if (node instanceof ScrollBar scrollBar) {
                if (scrollBar.getOrientation() == Orientation.VERTICAL) {
                    verticalScrollBar = scrollBar;
                } else {
                    horizontalScrollBar = scrollBar;
                }
            }
        });
    }

    private void bindResizeCornerPosition() {
        if (verticalScrollBar != null && horizontalScrollBar != null) {
            resizeCorner.translateXProperty().bind(Bindings.when(verticalScrollBar.visibleProperty())
                    .then(verticalScrollBar.widthProperty())
                    .otherwise(0.0)
                    .negate());

            resizeCorner.translateYProperty().bind(Bindings.when(horizontalScrollBar.visibleProperty())
                    .then(horizontalScrollBar.heightProperty())
                    .otherwise(0.0).
                    negate());
        }
    }

}
