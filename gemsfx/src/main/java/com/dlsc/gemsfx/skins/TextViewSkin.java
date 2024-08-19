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

    private final SelectableText selectableText = new SelectableText();

    public TextViewSkin(TextView control) {
        super(control);

        control.addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (KeyCodeCombination.keyCombination("shortcut+c").match(evt)) {
                control.copySelection();
            } else if (KeyCodeCombination.keyCombination("shortcut+a").match(evt)) {
                selectableText.selectAll();
            } else if (KeyCodeCombination.keyCombination("backspace").match(evt)) {
                selectableText.removeSelection("shortcut");
            }
        });

        getChildren().setAll(selectableText);

        control.focusedProperty().addListener(it -> {
            ContextMenu contextMenu = control.getContextMenu();
            if (contextMenu != null && contextMenu.isShowing()) {
                return;
            }

            if (!control.isFocused()) {
                selectableText.removeSelection("focus lost");
            }
        });
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        return selectableText.prefHeight(width - leftInset - rightInset);
    }

    private final class SelectableText extends TextFlow {

        private final Path wrappingPath = new Path();

        private int mouseDragStartPos = -1;
        private int selectionStartPos = -1;
        private int selectionEndPos = -1;
        private final Text text = new Text();

        public SelectableText() {
            super();

            setCursor(Cursor.TEXT);
            setPrefWidth(Region.USE_PREF_SIZE);

            text.textProperty().bind(getSkinnable().textProperty());
            text.getStyleClass().add("text");

            setText(text);

            wrappingPath.setManaged(false);
            wrappingPath.fillProperty().bind(getSkinnable().highlightFillProperty());
            wrappingPath.strokeProperty().bind(getSkinnable().highlightStrokeProperty());

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
                getSkinnable().requestFocus();

                if (!e.isPrimaryButtonDown() || e.isPopupTrigger()) {
                    return;
                }

                removeSelection("mouse pressed");

                HitInfo hit = hitTest(new Point2D(e.getX(), e.getY()));

                if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
                    // TODO: Double-click selection
                    return;
                } else {
                    mouseDragStartPos = hit.getCharIndex();
                }
            });

            setOnMouseDragged(e -> {
                if (e.isStillSincePress() || !e.isPrimaryButtonDown()) {
                    return;
                }
                HitInfo hit = hitTest(new Point2D(e.getX(), e.getY()));

                selectionStartPos = Math.min(mouseDragStartPos, hit.getCharIndex());
                selectionEndPos = Math.max(mouseDragStartPos, hit.getCharIndex());

                performSelection();
            });

            setOnMouseReleased(e -> {
                if (!e.getButton().equals(MouseButton.PRIMARY) || e.isPopupTrigger()) {
                    return;
                }
                getSkinnable().getProperties().put("selected.text", getSelectedTextAsString());
                mouseDragStartPos = -1;
            });

            widthProperty().addListener((obs, old, val) -> removeSelection("width changed"));
            heightProperty().addListener((obs, old, val) -> removeSelection("height changed"));
        }

        private void performSelection() {
            text.setSelectionStart(selectionStartPos);
            text.setSelectionEnd(selectionEndPos);

            PathElement[] selectionRange = rangeShape(selectionStartPos, selectionEndPos);
            wrappingPath.getElements().setAll(selectionRange);

            getSkinnable().getProperties().put("selected.text", getSelectedTextAsString());
        }

        public void setText(Text text) {
            if (text != null) {
                text.setSelectionFill(getSkinnable().getHighlightTextFill());
            }
            getChildren().setAll(wrappingPath);
            getChildren().addAll(text);
        }

        public void clear() {
            getChildren().setAll(wrappingPath);
        }

        public String getSelectedTextAsString() {
            StringBuilder content = getTextFlowContentAsString();
            return selectionStartPos >= 0 && selectionEndPos > selectionStartPos
                    ? content.substring(selectionStartPos, selectionEndPos)
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

        private void removeSelection(String reason) {
            getSkinnable().getProperties().put("selected.text", null);
            selectionStartPos = -1;
            selectionEndPos = -1;
            text.setSelectionStart(selectionStartPos);
            text.setSelectionEnd(selectionEndPos);
            wrappingPath.getElements().clear();
        }
    }
}