package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TextView;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.text.HitInfo;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.lang3.StringUtils;

public class TextViewSkin extends SkinBase<TextView> {

    private final SelectableText selectableText;

    public TextViewSkin(TextView control) {
        super(control);

        selectableText = new SelectableText(control);

        control.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (KeyCodeCombination.keyCombination("shortcut+c").match(evt)) {
                control.copySelection();
            } else if (KeyCodeCombination.keyCombination("shortcut+a").match(evt)) {
                selectableText.selectAll();
            } else if (KeyCodeCombination.keyCombination("backspace").match(evt)) {
                selectableText.removeSelection();
            }
        });

        getChildren().setAll(selectableText);

        control.focusedProperty().addListener(it -> {
            ContextMenu contextMenu = control.getContextMenu();
            if (contextMenu != null && contextMenu.isShowing()) {
                return;
            }

            if (!control.isFocused()) {
                selectableText.removeSelection();
            }
        });
    }

    @Override
    protected double computeMinHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return selectableText.minHeight(width - leftInset - rightInset);
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return selectableText.prefHeight(width - leftInset - rightInset);
    }

    @Override
    protected double computeMaxHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return selectableText.maxHeight(width - leftInset - rightInset);
    }

    private static final class SelectableText extends TextFlow {

        private final Path wrappingPath = new Path();
        private final TextView textView;

        private int mouseDragStartPos = -1;
        private int selectionStartPos = -1;
        private int selectionEndPos = -1;
        private final Text text = new Text();

        public SelectableText(TextView textView) {
            super();

            this.textView = textView;

            setCursor(Cursor.TEXT);
            setPrefWidth(Region.USE_PREF_SIZE);

            text.getStyleClass().add("text");
            text.textProperty().bind(textView.textProperty());
            text.selectionFillProperty().bind(textView.highlightTextFillProperty());

            setText(text);

            wrappingPath.setManaged(false);
            wrappingPath.fillProperty().bind(textView.highlightFillProperty());
            wrappingPath.strokeProperty().bind(textView.highlightStrokeProperty());

            getStyleClass().add("selectable-text");
            initListeners();
        }

        public void selectAll() {
            String t = text.getText();
            if (StringUtils.isNotBlank(t)) {
                selectionStartPos = 0;
                selectionEndPos = t.length();
                performSelection();
            }
        }

        private void initListeners() {
            setOnMousePressed(e -> {
                textView.requestFocus();

                if (!e.isPrimaryButtonDown() || e.isPopupTrigger()) {
                    return;
                }

                HitInfo hit = hitTest(new Point2D(e.getX(), e.getY()));

                int charIndex = hit.getCharIndex();
                if (!e.isShiftDown()) {
                    removeSelection();

                    if (e.isPrimaryButtonDown()) {
                        switch (e.getClickCount()) {
                            case 1:
                                mouseDragStartPos = charIndex;
                                break;
                            case 2:
                                selectWord(hit);
                                break;
                            case 3:
                                selectParagraph(hit);
                                break;
                        }
                    }
                } else {
                    if (charIndex >= mouseDragStartPos) {
                        selectionStartPos = mouseDragStartPos;
                        selectionEndPos = charIndex + 1;
                    } else {
                        selectionStartPos = charIndex;
                        selectionEndPos = mouseDragStartPos + 1;
                    }
                    performSelection();
                }
            });

            setOnMouseDragged(e -> {
                if (e.isStillSincePress() || !e.isPrimaryButtonDown()) {
                    return;
                }
                HitInfo hit = hitTest(new Point2D(e.getX(), e.getY()));

                int charIndex = hit.getCharIndex();
                selectionStartPos = Math.min(mouseDragStartPos, charIndex);
                selectionEndPos = Math.max(mouseDragStartPos, charIndex) + 1;

                performSelection();
            });

            setOnMouseReleased(e -> {
                if (!e.getButton().equals(MouseButton.PRIMARY) || e.isPopupTrigger()) {
                    return;
                }
                textView.getProperties().put("selected.text", getSelectedTextAsString());
            });

            widthProperty().addListener((obs, old, val) -> removeSelection());
            heightProperty().addListener((obs, old, val) -> removeSelection());
        }

        private void selectWord(HitInfo hit) {
            int charIndex = hit.getCharIndex();
            StringBuilder string = getTextFlowContentAsString();
            if (isInvalidIndex(string, charIndex)) {
                return;
            }

            int startIndex = -1;
            int endIndex = -1;

            for (int i = charIndex; i < string.length(); i++) {
                endIndex = i;
                char c = string.charAt(i);
                if (!(Character.isAlphabetic(c) || Character.isDigit(c)) || Character.isWhitespace(c)) {
                    break;
                }
            }

            for (int i = charIndex; i >= 0; i--) {
                startIndex = i;
                char c = string.charAt(i);
                if (!(Character.isAlphabetic(c) || Character.isDigit(c)) || Character.isWhitespace(c)) {
                    startIndex++;
                    break;
                }
            }

            if (startIndex > -1 && endIndex > startIndex) {
                selectionStartPos = startIndex;
                selectionEndPos = endIndex;
                performSelection();
            }
        }

        private void selectParagraph(HitInfo hit) {
            int charIndex = hit.getCharIndex();
            StringBuilder string = getTextFlowContentAsString();
            if (isInvalidIndex(string, charIndex)) {
                return;
            }

            int startIndex = -1;
            int endIndex = -1;

            for (int i = charIndex; i < string.length(); i++) {
                endIndex = i;
                char c = string.charAt(i);
                if (c == '\n' || c == '\r') {
                    break;
                }
            }

            for (int i = charIndex; i >= 0; i--) {
                if (string.charAt(i) == '\n') {
                    break;
                }
                startIndex = i;
            }

            if (startIndex > -1 && endIndex > startIndex) {
                selectionStartPos = startIndex;
                selectionEndPos = endIndex + 1;
                performSelection();
            }
        }

        private void performSelection() {
            text.setSelectionStart(selectionStartPos);
            text.setSelectionEnd(Math.min(selectionEndPos, getTextFlowContentAsString().length()));

            PathElement[] selectionRange = rangeShape(selectionStartPos, selectionEndPos);
            wrappingPath.getElements().setAll(selectionRange);

            textView.getProperties().put("selected.text", getSelectedTextAsString());
        }

        public void setText(Text text) {
            getChildren().setAll(wrappingPath);
            getChildren().addAll(text);
        }

        public void clear() {
            getChildren().setAll(wrappingPath);
        }

        public String getSelectedTextAsString() {
            StringBuilder content = getTextFlowContentAsString();
            return selectionStartPos >= 0 && selectionEndPos > selectionStartPos
                    ? content.substring(selectionStartPos, Math.min(selectionEndPos, content.length()))
                    : null;
        }

        private StringBuilder getTextFlowContentAsString() {
            StringBuilder sb = new StringBuilder();
            for (Node node : getChildren()) {
                if (node instanceof Text t) {
                    sb.append(t.getText());
                }
            }
            return sb;
        }

        private void removeSelection() {
            textView.getProperties().put("selected.text", null);
            selectionStartPos = -1;
            selectionEndPos = -1;
            text.setSelectionStart(selectionStartPos);
            text.setSelectionEnd(selectionEndPos);
            wrappingPath.getElements().clear();
        }

        private boolean isInvalidIndex(StringBuilder string, int charIndex) {
            return string.isEmpty() || charIndex < 0 || charIndex >= string.length();
        }
    }
}