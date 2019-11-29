package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.keyboard.Keyboard;
import com.dlsc.gemsfx.keyboard.Keyboard.Key;
import com.dlsc.gemsfx.keyboard.Keyboard.KeyBase;
import com.dlsc.gemsfx.keyboard.Keyboard.Row;
import com.dlsc.gemsfx.keyboard.Keyboard.SpecialKey;
import com.dlsc.gemsfx.keyboard.KeyboardView;
import javafx.collections.MapChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import org.controlsfx.control.PopOver;

import java.util.List;
import java.util.stream.Collectors;

public class KeyboardViewSkin extends SkinBase<KeyboardView> {

    private final GridPane gridPane = new GridPane();

    public KeyboardViewSkin(KeyboardView view) {
        super(view);

        gridPane.getStyleClass().add("grid");
        gridPane.setGridLinesVisible(true);

        getChildren().addAll(gridPane);

        view.selectedKeyboardProperty().addListener(it -> buildView());
        buildView();

        MapChangeListener l = change -> {
            if (change.wasAdded()) {
                final Object key = change.getKey();
                if (key.equals("keyboard.extra.keys")) {
                    KeyPopoverInfo info = (KeyPopoverInfo) change.getValueAdded();
                    showExtraKeys(info);
                    getSkinnable().getProperties().remove("keyboard.extra.keys");
                    getSkinnable().requestLayout();
                }
            }
        };

        view.getProperties().addListener(l);
    }

    private PopOver extraKeysPopover;

    private void showExtraKeys(KeyPopoverInfo info) {
        if (extraKeysPopover != null) {
            extraKeysPopover.hide();
        }

        List<String> popoverText = info.text;

        final List<KeyView> keyViews = popoverText.stream()
                .map(text -> new KeyView(getSkinnable(), info.key, text))
                .peek(keyView -> {
                    HBox.setHgrow(keyView, Priority.ALWAYS);
                    keyView.setPrefWidth(info.width);
                    keyView.setPrefHeight(info.height);
                    keyView.setMinWidth(info.width);
                    keyView.setMinHeight(info.height);
                    keyView.setMaxWidth(info.width);
                    keyView.setMaxHeight(info.height);
                    keyView.addEventHandler(MouseEvent.MOUSE_RELEASED, evt -> extraKeysPopover.hide());
                })
                .collect(Collectors.toList());

        HBox box = new HBox();
        box.getStyleClass().add("extra-keys-box");
        box.setFillHeight(true);
        box.getChildren().setAll(keyViews);

        extraKeysPopover = new PopOver();
        extraKeysPopover.setContentNode(box);
        extraKeysPopover.setArrowSize(0);
        extraKeysPopover.setAutoFix(true);
        extraKeysPopover.show(info.targetKeyView);
    }

    public static class KeyPopoverInfo {
        public Key key;
        public KeyView targetKeyView;
        public List<String> text;
        public double width;
        public double height;
    }

    private void buildView() {
        gridPane.getChildren().clear();

        final KeyboardView view = getSkinnable();
        final Keyboard keyboard = view.getSelectedKeyboard();

        if (keyboard == null) {
            return;
        }

        gridPane.setMinWidth(keyboard.getMinWidth());
        gridPane.setMaxWidth(keyboard.getMaxWidth());
        gridPane.setPrefWidth(keyboard.getPrefWidth());

        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        int maxColumns = 0;

        for (Row row : keyboard.getRows()) {
            int columns = 0;

            for (KeyBase key : row.getKeys()) {
                columns += key.getColumnSpan();
            }

            maxColumns = Math.max(maxColumns, columns);

            final RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / (double) keyboard.getRows().size());
            gridPane.getRowConstraints().add(rowConstraints);
        }

        for (int i = 0; i < maxColumns; i++) {
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / (double) maxColumns);
            gridPane.getColumnConstraints().add(columnConstraints);
        }

        int r = 0;
        int c;

        for (Row row : keyboard.getRows()) {
            c = 0;

            final Node node = row.getNode();

            if (node != null) {

                gridPane.add(node, c, r);
                GridPane.setColumnSpan(node, maxColumns);
                GridPane.setFillWidth(node, true);
                GridPane.setHgrow(node, Priority.ALWAYS);

            } else {
                for (KeyBase key : row.getKeys()) {
                    KeyViewBase keyView = null;
                    if (key instanceof Key) {
                        keyView = new KeyView(getSkinnable(), (Key) key);
                    } else if (key instanceof SpecialKey) {
                        keyView = new SpecialKeyView(getSkinnable(), (SpecialKey) key);
                    }

                    gridPane.add(keyView, c, r);

                    c += key.getColumnSpan();

                    GridPane.setColumnSpan(keyView, key.getColumnSpan());
                    GridPane.setRowSpan(keyView, key.getRowSpan());
                    GridPane.setFillWidth(keyView, true);
                    GridPane.setHgrow(keyView, Priority.ALWAYS);
                }
            }

            r++;
        }
    }
}
