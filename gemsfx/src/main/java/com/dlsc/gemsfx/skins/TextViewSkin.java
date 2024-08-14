package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TextView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A custom label Skin that allows you to select a text to be copied to the clipboard
 */
public class TextViewSkin extends SkinBase<TextView> {

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    /**
     * Panel that will be used as for rendering the actual text of the view.
     */
    private final TextFlow textsContainer = new TextFlow();

    /**
     * Lays out the selection nodes.
     */
    private final Pane selectionContainer = new Pane();

    /**
     * Arrangement of the words contained in the text
     */
    private final List<Text> texts = new ArrayList<>();

    /**
     * Set of indices of the words that have been selected
     */
    private final Set<Integer> selectedIndices = new TreeSet<>();

    /**
     * String property used to automatically change the text of the TextView
     */
    private final StringProperty selectedText = new SimpleStringProperty();

    /**
     * Instances a new Custom Label Skin, value TextView
     */
    public TextViewSkin(TextView control) {
        super(control);

        textsContainer.getStyleClass().add("text-container");
        selectionContainer.getStyleClass().add("selection-container");

        selectedText.addListener(obs -> control.getProperties().put("selected.text", selectedText.get()));

        SelectionHandler selectionHandler = new SelectionHandler();
        control.setOnMouseDragged(selectionHandler);
        control.setOnMousePressed(selectionHandler);
        control.setOnMouseReleased(selectionHandler);
        control.textProperty().addListener(obs -> buildView(control.getText()));
        control.selectedTextProperty().addListener(obs -> {
            if (control.getSelectedText() == null) {
                clearSelection();
            }
        });

        buildView(control.getText());

        /*
         * Defines the overlays of the main panels
         */
        selectionContainer.toBack();
        textsContainer.toFront();

        control.widthProperty().addListener((obs, oldV, newV) -> buildSelection());
        control.heightProperty().addListener((obs, oldV, newV) -> buildSelection());

        getChildren().addAll(selectionContainer, textsContainer);
    }


    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        textsContainer.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
        selectionContainer.resizeRelocate(contentX, contentY, contentWidth, contentHeight);
    }

    /**
     * Separates a text into words and indexes
     */
    private void buildView(String text) {
        texts.clear();

        if (text != null && !text.isEmpty()) {

            StringBuilder spaces = new StringBuilder();
            StringBuilder word = new StringBuilder();
            StringBuilder special = new StringBuilder();

            for (int i = 0; i < text.length(); i++) {
                char character = text.charAt(i);
                if (isSpaceCharacter(character)) {
                    spaces.append(character);
                    if (!word.isEmpty()) {
                        addText(word);
                    } else if (!special.isEmpty()) {
                        addText(special);
                    }
                } else if (isSpecialCharacter(character)) {
                    special.append(character);
                    if (!word.isEmpty()) {
                        addText(word);
                    } else if (!spaces.isEmpty()) {
                        addText(spaces);
                    }
                } else if (isLineBreakCharacter(character)) {
                    if (!word.isEmpty()) {
                        addText(word);
                    } else if (!spaces.isEmpty()) {
                        addText(spaces);
                    } else if (!special.isEmpty()) {
                        addText(special);
                    }
                    StringBuilder line = new StringBuilder();
                    line.append(character);
                    addText(line);
                } else {
                    word.append(character);
                    if (!spaces.isEmpty()) {
                        addText(spaces);
                    } else if (!special.isEmpty()) {
                        addText(special);
                    }
                }
            }
            if (!word.isEmpty()) {
                addText(word);
            } else if (!special.isEmpty()) {
                addText(special);
            } else if (!spaces.isEmpty()) {
                addText(spaces);
            }
        }

        textsContainer.getChildren().setAll(texts);
        clearSelection();
    }

    private void addText(StringBuilder text) {
        Text textNode = new Text();
        textNode.setText(text.toString());
        textNode.getStyleClass().add("text");
        texts.add(textNode);
        text.setLength(0);
    }

    /**
     * Iterate over the indices to separate them into regions, which overlap the text
     */
    private void buildSelection() {
        StringBuilder selection = new StringBuilder();
        Map<Double, List<Rectangle2D>> rectangles = new HashMap<>();
        List<Region> regions = new ArrayList<>();

        texts.forEach(t -> t.pseudoClassStateChanged(SELECTED, false));

        for (int index : selectedIndices) {
            if (index < 0 || index > texts.size()) {
                continue;
            }

            Text text = texts.get(index);
            text.pseudoClassStateChanged(SELECTED, true);
            selection.append(text.getText());

            double x = text.getLayoutX();
            double y = text.getLayoutY();
            double w = text.getBoundsInLocal().getWidth();
            double h = text.getBoundsInLocal().getHeight();

            String txt = text.getText();
            char[] chars = txt.toCharArray();

            if (isLineBreakCharacter(chars[0])) {
                w = 5;
            }

            List<Rectangle2D> temp = rectangles.computeIfAbsent(y, ay -> new ArrayList<>());
            temp.add(new Rectangle2D(x, y, w, h));
        }

        for (Double y : rectangles.keySet()) {
            List<Rectangle2D> temp = rectangles.get(y);

            double x = Double.MAX_VALUE;
            double height = 0;
            double width = 0;

            for (Rectangle2D r : temp) {
                if (r.getMinX() < x) {
                    x = r.getMinX();
                }

                if (r.getHeight() > height) {
                    height = r.getHeight();
                }

                width += r.getWidth();
            }

            Region region = new Region();
            region.setLayoutY(y);
            region.setLayoutX(x);
            region.setPrefWidth(width);
            region.setPrefHeight(height);
            region.getStyleClass().add("selection");

            regions.add(region);
        }

        selectedText.set(selection.isEmpty() ? null : selection.toString());
        selectionContainer.getChildren().setAll(regions);
    }

    private void clearSelection() {
        selectedIndices.clear();
        buildSelection();
    }

    private static boolean isSpaceCharacter(char character) {
        return character == ' ';
    }

    private static boolean isSpecialCharacter(char character) {
        return character == '.' || character == ',' || character == ';' || character == ':';
    }

    private static boolean isLineBreakCharacter(char character) {
        return character == '\n';
    }

    /**
     * Anonymous class to control mouse events
     */
    private class SelectionHandler implements EventHandler<MouseEvent> {

        private Integer firstIndex;

        @Override
        public void handle(MouseEvent evt) {
            if (evt.getEventType() == MouseEvent.MOUSE_DRAGGED) {
                handleMouseDragged(evt);
            } else if (evt.getEventType() == MouseEvent.MOUSE_PRESSED) {
                handleMousePressed(evt);
            } else if (evt.getEventType() == MouseEvent.MOUSE_RELEASED) {
                handleMouseReleased(evt);
            }

        }

        private void handleMouseDragged(MouseEvent evt) {
            if (evt.getPickResult().getIntersectedNode() instanceof Text text) {
                addSelectedIndex(texts.indexOf(text));
            }
        }

        private void handleMousePressed(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                return;
            }
            getSkinnable().requestFocus();

            if (evt.getPickResult().getIntersectedNode() instanceof Text text) {
                int index = texts.indexOf(text);
                if (index >= 0) {
                    firstIndex = index;
                }

            }

            selectedIndices.clear();
            buildSelection();
        }

        private void handleMouseReleased(MouseEvent evt) {
            if (evt.isPopupTrigger()) {
                return;
            }
            firstIndex = null;
        }

        /**
         * From an initial and final index, creates a route of the intermediate indexes to avoid skipping indexes
         */
        private void addSelectedIndex(int index) {
            if (index >= 0 && index < texts.size() && firstIndex != null) {
                selectedIndices.clear();
                selectedIndices.add(index);

                if (firstIndex > index) {
                    for (int i = firstIndex; i > index; i--) {
                        selectedIndices.add(i);
                    }
                }
                else {
                    for (int i = firstIndex; i < index; i++) {
                        selectedIndices.add(i);
                    }
                }

                buildSelection();
            }
        }
    }
}