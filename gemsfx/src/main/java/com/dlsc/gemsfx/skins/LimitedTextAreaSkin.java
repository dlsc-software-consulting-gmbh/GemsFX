package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CircleProgressIndicator;
import com.dlsc.gemsfx.LimitedTextArea;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.IntegerRange;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;


public class LimitedTextAreaSkin extends ResizableTextAreaSkin {

    /**
     * The pseudo class for the error state.
     */
    private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");
    private static final PseudoClass WARNING_PSEUDO_CLASS = PseudoClass.getPseudoClass("warning");

    private CircleProgressIndicator progressIndicator;
    private final VBox contentBox;
    private final ReadOnlyBooleanWrapper outOfRange;

    public LimitedTextAreaSkin(LimitedTextArea control, ReadOnlyBooleanWrapper outOfRange) {
        super(control);
        this.outOfRange = outOfRange;

        // init bottom box
        HBox bottomBox = initBottom(control);

        // init content box
        contentBox = new VBox(contentPane, bottomBox);
        contentBox.getStyleClass().add("content-box");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        getChildren().setAll(contentBox);

        updateProgress();
        updateTextAndPseudoClass();

        registerListener(control);
    }

    private HBox initBottom(LimitedTextArea control) {
        // init length indicator
        progressIndicator = createProgressIndicator(control);
        Label lengthLabel = createLengthLabel(control);
        StackPane lengthIndicator = new StackPane(lengthLabel, progressIndicator);
        lengthIndicator.getStyleClass().add("length-indicator");

        // init tips
        Label tips = new Label();
        tips.getStyleClass().add("tips");
        tips.setGraphic(new FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE));
        tips.textProperty().bind(control.tipsProperty());
        tips.managedProperty().bind(tips.visibleProperty());
        tips.visibleProperty().bind(tips.textProperty().isNotEmpty());

        // init bottom box
        HBox bottomBox = new HBox(tips, new Spacer(), lengthIndicator);
        bottomBox.getStyleClass().add("bottom-box");
        bottomBox.setMaxHeight(Region.USE_PREF_SIZE);
        VBox.setVgrow(bottomBox, Priority.NEVER);
        bottomBox.addEventFilter(MouseEvent.ANY, Event::consume);
        bottomBox.managedProperty().bind(bottomBox.visibleProperty());
        bottomBox.visibleProperty().bind(control.showBottomProperty());
        return bottomBox;
    }

    private Label createLengthLabel(LimitedTextArea control) {
        Label lengthLabel = new Label();
        lengthLabel.getStyleClass().add("length-label");
        lengthLabel.textProperty().bind(Bindings.createStringBinding(() -> {
            String text = control.getText();
            int textLen = text == null ? 0 : text.length();

            IntegerRange rangeLimit = control.getCharacterRangeLimit();
            if (rangeLimit == null || rangeLimit.getMax() <= 0) {
                return String.valueOf(textLen);
            }

            return String.valueOf(rangeLimit.getMax() - textLen);
        }, control.textProperty(), control.characterRangeLimitProperty()));

        lengthLabel.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            LimitedTextArea.LengthDisplayMode displayMode = control.getLengthDisplayMode();
            switch (displayMode) {
                case AUTO:
                    IntegerRange limit = control.getCharacterRangeLimit();
                    double warningThreshold = control.getValidWarningThreshold();
                    if (limit != null && limit.getMax() > 0) {
                        int textLen = control.getText() == null ? 0 : control.getText().length();
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
        }, control.textProperty(), control.characterRangeLimitProperty(), control.lengthDisplayModeProperty()));
        return lengthLabel;
    }

    private CircleProgressIndicator createProgressIndicator(LimitedTextArea control) {
        progressIndicator = new CircleProgressIndicator();
        progressIndicator.setConverter(null);
        progressIndicator.visibleProperty().bind(Bindings.createBooleanBinding(() -> {
            LimitedTextArea.LengthDisplayMode displayMode = control.getLengthDisplayMode();
            IntegerRange rangeLimit = control.getCharacterRangeLimit();
            if (displayMode == LimitedTextArea.LengthDisplayMode.ALWAYS_HIDE || rangeLimit == null || rangeLimit.getMax() <= 0) {
                return false;
            }

            int maxLength = rangeLimit.getMax();
            String text = control.getText();
            int textLen = text == null ? 0 : text.length();
            return textLen <= maxLength;
        }, control.textProperty(), control.characterRangeLimitProperty(), control.lengthDisplayModeProperty()));
        return progressIndicator;
    }

    private void registerListener(LimitedTextArea control) {
        control.getExcludedItems().addListener((InvalidationListener) it -> updateTextAndPseudoClass());

        registerChangeListener(control.warningThresholdProperty(), it -> updatePseudoClass());
        registerChangeListener(control.characterRangeLimitProperty(), it -> updatePseudoClass());
        registerChangeListener(control.textProperty(), it -> {
            updateTextAndPseudoClass();
            updateProgress();
        });
    }

    private void updateProgress() {
        LimitedTextArea skinnable = (LimitedTextArea) getSkinnable();
        String text = skinnable.getText();
        IntegerRange rangeLimit = skinnable.getCharacterRangeLimit();
        if (rangeLimit == null || rangeLimit.getMax() <= 0) {
            progressIndicator.setProgress(0);
            return;
        }

        double progress = (text == null ? 0 : text.length()) / (double) rangeLimit.getMax();
        progressIndicator.setProgress(Math.min(progress, 1.0));
    }

    private void updateTextAndPseudoClass() {
        LimitedTextArea control = (LimitedTextArea) getSkinnable();
        String content = control.getText() == null ? "" : control.getText();
        if (control.getExcludedItems().stream().anyMatch(content::contains)) {
            Platform.runLater(() -> {
                String result = content.replaceAll(String.join("|", control.getExcludedItems()), "");
                control.setText(result);
                control.positionCaret(result.length());
            });
        }
        updatePseudoClass();
    }

    private void updatePseudoClass() {
        LimitedTextArea control = (LimitedTextArea) getSkinnable();

        IntegerRange limit = control.getCharacterRangeLimit();
        double warningThreshold = control.getValidWarningThreshold();
        if (limit != null && limit.getMax() > 0) {
            int textLen = control.getText() == null ? 0 : control.getText().length();
            int maximum = limit.getMax();
            int minimum = limit.getMin();
            boolean error = textLen > maximum || textLen < minimum;

            control.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error);
            if (error) {
                control.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, false);
            } else {
                boolean warning = textLen >= maximum * warningThreshold;
                control.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, warning);
            }
            outOfRange.set(error);
        } else {
            control.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, false);
            control.pseudoClassStateChanged(WARNING_PSEUDO_CLASS, false);
            outOfRange.set(false);
        }
    }

    @Override
    protected void layoutChildren(double contentX, double contentY, double contentWidth, double contentHeight) {
        super.layoutChildren(contentX, contentY, contentWidth, contentHeight);

        layoutInArea(contentBox, contentX, contentY, contentWidth, contentHeight, 0, HPos.LEFT, VPos.TOP);
    }

}
