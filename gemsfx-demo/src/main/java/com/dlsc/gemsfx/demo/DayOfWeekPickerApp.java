package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.DayOfWeekPicker;
import com.dlsc.gemsfx.demo.fake.SimpleControlPane;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class DayOfWeekPickerApp extends GemApplication {

    private final DayOfWeekPicker dayOfWeekPicker = new DayOfWeekPicker();

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage);

        dayOfWeekPicker.prefWidthProperty().bind(dayOfWeekPicker.getSelectionModel().selectionModeProperty().map(sm -> sm == SelectionMode.SINGLE ? 135 : 160));
        StackPane wrapper = new StackPane(dayOfWeekPicker);
        if (Boolean.getBoolean("atlantafx")) {
            wrapper.setStyle("-fx-padding: 30px; -fx-background-color: -color-neutral-subtle;");
        } else {
            wrapper.setStyle("-fx-padding: 30px; -fx-background-color: white;");
        }

        SplitPane splitPane = new SplitPane(wrapper,getControlPanel());
        splitPane.setDividerPositions(0.68);

        primaryStage.setScene(new Scene(splitPane, 800, 600));
        primaryStage.setTitle("DayOfWeekPicker");
        StageManager.install(primaryStage, "day.of.week.picker.app");

        primaryStage.show();
    }

    public Node getControlPanel() {
        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().addAll(SelectionMode.values());
        selectionModeComboBox.valueProperty().bindBidirectional(dayOfWeekPicker.getSelectionModel().selectionModeProperty());

        CheckBox autoHideOnSelection = new CheckBox("Hide On Selection");
        autoHideOnSelection.selectedProperty().bindBidirectional(dayOfWeekPicker.autoHideOnSelectionProperty());

        Button selectAll = new Button("Select All");
        selectAll.setMaxWidth(Double.MAX_VALUE);
        selectAll.managedProperty().bind(selectAll.visibleProperty());
        selectAll.visibleProperty().bind(dayOfWeekPicker.getSelectionModel().selectionModeProperty().isEqualTo(SelectionMode.MULTIPLE));
        selectAll.setOnAction(evt -> dayOfWeekPicker.getSelectionModel().selectAll());

        Button clearSelection = new Button("Clear Selection");
        clearSelection.setMaxWidth(Double.MAX_VALUE);
        clearSelection.setOnAction(evt -> dayOfWeekPicker.getSelectionModel().clearSelection());

        VBox selectionButtons = new VBox(10, selectAll, clearSelection);
        selectionButtons.setAlignment(Pos.CENTER);
        selectionButtons.setFillWidth(true);

        return new SimpleControlPane(
                "DayOfWeekPicker",
                new SimpleControlPane.ControlItem("SelectionMode", selectionModeComboBox),
                new SimpleControlPane.ControlItem("Auto Hide", autoHideOnSelection),
                new SimpleControlPane.ControlItem("Test Select", selectionButtons)
        );
    }
        @Override
    public String getDescription() {
        return """
                ### DayOfWeekPicker
                
                A custom control that allows users to select days of the week.
                It provides support for two `SelectionMode`: single and multiple .
                
                  - `SINGLE` mode allows selection of only one day at a time.
                  - `MULTIPLE` mode allows selection of multiple days.
                """;
    }

}
