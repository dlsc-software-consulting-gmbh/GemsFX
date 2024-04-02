package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.LimitedTextArea;
import com.dlsc.gemsfx.util.IntegerRange;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LimitedTextAreaApp extends Application {

    private final LimitedTextArea textArea = new LimitedTextArea();

    @Override
    public void start(Stage primaryStage) throws Exception {

        //init the text area
        textArea.setWrapText(true);
        textArea.setText("Hello, World!");
        textArea.setCharacterRangeLimit(new IntegerRange(0, 30));
        textArea.setPrefHeight(380);

        StackPane wrapper = new StackPane(new VBox(textArea));
        wrapper.setPadding(new Insets(20));

        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.75);
        splitPane.getItems().addAll(wrapper, getControlPanel());

        Scene scene = new Scene(splitPane, 560, 420);
        primaryStage.setScene(scene);
        primaryStage.setTitle("LimitedTextArea Demo");
        primaryStage.show();
        CSSFX.start();
    }

    private VBox getControlPanel() {
        Label lengthDisplayMode = new Label("Length Display Mode");
        ComboBox<LimitedTextArea.LengthDisplayMode> lengthDisplayModeComboBox = new ComboBox<>();
        lengthDisplayModeComboBox.setPrefWidth(Double.MAX_VALUE);
        lengthDisplayModeComboBox.getItems().addAll(LimitedTextArea.LengthDisplayMode.values());
        lengthDisplayModeComboBox.setValue(LimitedTextArea.LengthDisplayMode.ALWAYS_SHOW);
        textArea.lengthDisplayModeProperty().bind(lengthDisplayModeComboBox.valueProperty());

        Label warningThreshold = new Label("Warning Threshold");
        Slider warningThresholdSlider = new Slider(0, 1, 0.7);
        textArea.warningThresholdProperty().bind(warningThresholdSlider.valueProperty());

        Label minLength = new Label("Min Length");
        Spinner<Integer> minLengthField = new Spinner<>(0, 30, 0, 10);

        Label maxLength = new Label("Max Length");
        Spinner<Integer> maxLengthField = new Spinner<>(30, 200, 30, 10);
        textArea.characterRangeLimitProperty().bind(Bindings.createObjectBinding(
                () -> new IntegerRange(minLengthField.getValue(), maxLengthField.getValue())
                , maxLengthField.valueProperty(), minLengthField.valueProperty()));

        textArea.tipsProperty().bind(Bindings.createStringBinding(
                () -> {
                    IntegerRange range = textArea.getCharacterRangeLimit();
                    if (range == null || range.getMax() <= 0) {
                        return "";
                    }
                    return "Tips: character range limit is " + range.getMin() + " to " + range.getMax();
                }
                , textArea.characterRangeLimitProperty()));

        CheckBox showBottomCheckBox = new CheckBox("Show bottom");
        showBottomCheckBox.selectedProperty().bindBidirectional(textArea.showBottomProperty());

        Label excludedItems = new Label("Excluded characters");
        ListView<String> excludedItemsView = new ListView<>();
        excludedItems.textProperty().bind(Bindings.createStringBinding(
                () -> "Excluded characters (" + excludedItemsView.getSelectionModel().getSelectedItems().size() + ")"
                , excludedItemsView.getSelectionModel().getSelectedItems()));
        excludedItemsView.getItems().addAll("\r", "\n");
        excludedItemsView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    return;
                }
                if ("\r".equals(item)) {
                    setText("\"\\r\"");
                } else if ("\n".equals(item)) {
                    setText("\"\\n\"");
                } else {
                    setText(item);
                }
            }
        });
        excludedItemsView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        excludedItemsView.setPrefHeight(100);
        excludedItemsView.setPrefWidth(160);
        Bindings.bindContent(textArea.getExcludedItems(), excludedItemsView.getSelectionModel().getSelectedItems());

        return new VBox(10, lengthDisplayMode, lengthDisplayModeComboBox, warningThreshold, warningThresholdSlider,
                minLength, minLengthField, maxLength, maxLengthField, showBottomCheckBox, excludedItems, excludedItemsView);
    }

    public static void main(String[] args) {
        launch(args);
    }
}