package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.SearchFieldEditorSkin;
import javafx.beans.Observable;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.FlowPane;

import java.util.List;
import java.util.stream.Collectors;

public class TagsFieldSkin<T> extends SkinBase<TagsField<T>> {

    private FlowPane flowPane = new FlowPane();

    public TagsFieldSkin(TagsField<T> field) {
        super(field);

        flowPane.getStyleClass().add("flow-pane");
        flowPane.prefWrapLengthProperty().bind(field.widthProperty());

        field.getEditor().setSkin(new SearchFieldEditorSkin<>(field));
        field.getEditor().setManaged(false);

        field.addEventFilter(KeyEvent.KEY_RELEASED, evt -> {
            if (evt.getCode().equals(KeyCode.RIGHT) || evt.getCode().equals(KeyCode.ENTER)) {
                T selectedItem = field.getSelectedItem();
                if (selectedItem != null) {
                    if (!field.getTags().contains(selectedItem)) {
                        field.getTags().add(selectedItem);
                        field.setText("");
                    }
                }
            }
        });

        field.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.BACK_SPACE) && field.getText().equals("")) {
                if (!field.getTags().isEmpty()) {
                    field.getTags().remove(field.getTags().size() - 1);
                }
            }
        });

        getChildren().add(flowPane);

        field.getTags().addListener((Observable it) -> updateView());
        updateView();
    }

    @Override
    protected double computePrefHeight(double width, double topInset, double rightInset, double bottomInset, double leftInset) {
        System.out.println("comp width: " + width);

        if (getSkinnable().getTags().isEmpty()) {
            double ph = getSkinnable().getEditor().prefHeight(-1) + topInset + bottomInset;
            System.out.println("ph: " + ph);
            return ph;
        }

        double ph = super.computePrefHeight(width, topInset, rightInset, bottomInset, leftInset);

        double requiredWidth = 0;
        List<Node> chipViews = flowPane.getChildren().stream().filter(child -> child instanceof ChipView).collect(Collectors.toList());
        for (int i = 0; i < chipViews.size(); i++) {
            requiredWidth += chipViews.get(i).prefWidth(-1);
            if (i < chipViews.size() - 1) {
                requiredWidth += flowPane.getHgap();
            }
        }

        requiredWidth += getSkinnable().getEditor().minWidth(-1);

        if (requiredWidth > width) {
            ph += getSkinnable().getEditor().prefHeight(-1);
        }

        return ph;
    }


    @Override
    protected double computePrefWidth(double height, double topInset, double rightInset, double bottomInset, double leftInset) {
        System.out.println("comp height: " + height);
        return 0;
    }

    private void updateView() {
        flowPane.getChildren().clear();

        TagsField<T> field = getSkinnable();
        field.getTags().forEach(tag -> {
            ChipView<T> chipView = new ChipView<>();
            chipView.setValue(tag);
            chipView.setText(field.getConverter().toString(tag));
            chipView.setOnClose(evt -> field.getTags().remove(tag));
            chipView.setFocusTraversable(false);
            flowPane.getChildren().add(chipView);
        });

        flowPane.getChildren().add(field.getEditor());
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        System.out.println("content height: " + contentHeight + ", content width: " + contentWidth);

        double x = 0;
        double y = 0;
        double h = contentHeight;

        List<Node> chipViews = flowPane.getChildren().stream().filter(child -> child instanceof ChipView).collect(Collectors.toList());
        if (!chipViews.isEmpty()) {

            Node lastChipView = chipViews.get(chipViews.size() - 1);
            Bounds lastBounds = lastChipView.getBoundsInParent();

            x = lastBounds.getMaxX() + flowPane.getHgap();
            y = lastBounds.getMinY();
            h = lastBounds.getHeight();
        }

        TextField editor = getSkinnable().getEditor();
        editor.resizeRelocate(x, y, contentWidth - x, h);
    }
}
