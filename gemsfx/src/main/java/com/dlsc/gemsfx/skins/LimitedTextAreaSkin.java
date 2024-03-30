package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CircleProgressIndicator;
import com.dlsc.gemsfx.LimitedTextArea;
import com.dlsc.gemsfx.ResizableTextArea;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.IntegerRange;
import javafx.beans.binding.Bindings;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;


public class LimitedTextAreaSkin extends SkinBase<LimitedTextArea> {

    private final CircleProgressIndicator progressIndicator = new CircleProgressIndicator();

    public LimitedTextAreaSkin(LimitedTextArea limitedTextArea) {
        super(limitedTextArea);

        ResizableTextArea textArea = limitedTextArea.getTextArea();
        VBox.setVgrow(textArea, Priority.ALWAYS);

        textArea.textProperty().addListener(it -> updateProgress());
        updateProgress();

        progressIndicator.setConverter(null);
        progressIndicator.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            LimitedTextArea.LengthDisplayMode displayMode = limitedTextArea.getLengthDisplayMode();
            IntegerRange rangeLimit = limitedTextArea.getCharacterRangeLimit();
            if (displayMode == LimitedTextArea.LengthDisplayMode.ALWAYS_HIDE || rangeLimit == null || rangeLimit.getMax() <= 0) {
                return false;
            }

            int maxLength = rangeLimit.getMax();
            String text = limitedTextArea.getText();
            int textLen = text == null ? 0 : text.length();
            return textLen <= maxLength;
        }, limitedTextArea.textProperty(), limitedTextArea.characterRangeLimitProperty(), limitedTextArea.lengthDisplayModeProperty()));

        Label lengthLabel = new Label();
        lengthLabel.getStyleClass().add("length-label");
        lengthLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String text = limitedTextArea.getText();
            int textLen = text == null ? 0 : text.length();

            IntegerRange rangeLimit = limitedTextArea.getCharacterRangeLimit();
            if (rangeLimit == null || rangeLimit.getMax() <= 0) {
                return String.valueOf(textLen);
            }

            return String.valueOf(rangeLimit.getMax() - textLen);
        }, limitedTextArea.textProperty(), limitedTextArea.characterRangeLimitProperty()));

        lengthLabel.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            LimitedTextArea.LengthDisplayMode displayMode = limitedTextArea.getLengthDisplayMode();
            switch (displayMode) {
                case AUTO:
                    IntegerRange limit = limitedTextArea.getCharacterRangeLimit();
                    double warningThreshold = limitedTextArea.getValidWarningThreshold();
                    if (limit != null && limit.getMax() > 0) {
                        int textLen = limitedTextArea.getText() == null ? 0 : limitedTextArea.getText().length();
                        int maximum = limit.getMax();
                        int minimum = limit.getMin();
                        boolean error = textLen > maximum || textLen < minimum;
                        boolean warning = textLen >= maximum * warningThreshold && textLen <= maximum;
                        return error || warning;
                    } else {
                        return false;
                    }
                case ALWAYS_SHOW:
                    return true;
                default:
                    return false;
            }
        }, limitedTextArea.textProperty(), limitedTextArea.characterRangeLimitProperty(), limitedTextArea.lengthDisplayModeProperty()));

        StackPane lengthIndicator = new StackPane(lengthLabel, progressIndicator);
        lengthIndicator.getStyleClass().add("length-indicator");

        Label tips = new Label();
        tips.getStyleClass().add("tips");
        tips.setGraphic(new FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE));
        tips.textProperty().bind(limitedTextArea.tipsProperty());
        tips.managedProperty().bind(tips.visibleProperty());
        tips.visibleProperty().bind(tips.textProperty().isNotEmpty());

        HBox bottomBox = new HBox(tips, new Spacer(), lengthIndicator);
        bottomBox.getStyleClass().add("bottom-box");

        VBox container = new VBox(textArea, bottomBox);
        container.getStyleClass().add("container");
        getChildren().setAll(container);
    }

    private void updateProgress() {
        LimitedTextArea skinnable = getSkinnable();
        String text = skinnable.getText();
        IntegerRange rangeLimit = skinnable.getCharacterRangeLimit();
        if (rangeLimit == null || rangeLimit.getMax() <= 0) {
            progressIndicator.setProgress(0);
            return;
        }

        double progress = (text == null ? 0 : text.length()) / (double) rangeLimit.getMax();
        progressIndicator.setProgress(Math.min(progress, 1.0));
    }

}
