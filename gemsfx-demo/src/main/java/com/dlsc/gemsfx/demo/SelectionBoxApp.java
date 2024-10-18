package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SelectionBox;
import com.dlsc.gemsfx.demo.fake.SimpleControlPane;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.List;

public class SelectionBoxApp extends Application {

    private final SelectionBox<String> selectionBox = new SelectionBox<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
        SplitPane splitPane = new SplitPane();
        splitPane.setDividerPositions(0.7);
        splitPane.getItems().addAll(createControl(), getControlPanel());

        primaryStage.setTitle("SelectionBox Demo");
        primaryStage.setScene(new Scene(splitPane, 860, 600));
        primaryStage.show();

        CSSFX.start();
    }

    private Region createControl() {
        selectionBox.setPrefWidth(220);
        selectionBox.getItems().addAll("Item 1", "Item 2", "Item 3", "Option A", "Option B", "Option C", "Option D");
        // selectionBox.setItemConverter(new SimpleStringConverter<>(s -> ">>" +s));

        StackPane wrapper = new StackPane(selectionBox);
        wrapper.setStyle("-fx-background-color: white; -fx-padding: 50px;");
        return wrapper;
    }

    private Node getControlPanel() {
        // selection mode
        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().addAll(SelectionMode.SINGLE, SelectionMode.MULTIPLE);
        selectionModeComboBox.valueProperty().bindBidirectional(selectionBox.getSelectionModel().selectionModeProperty());

        // visible extra buttons
        CheckBox visibleExtraButtonsCheckBox = new CheckBox("Show Extra Buttons");
        visibleExtraButtonsCheckBox.selectedProperty().bindBidirectional(selectionBox.showExtraButtonsProperty());

        // change extra buttons
        Button changeExtraButtonsButton = new Button("Change Extra Buttons");
        changeExtraButtonsButton.setMaxWidth(Double.MAX_VALUE);
        changeExtraButtonsButton.setOnAction(e ->
                selectionBox.setExtraButtonsProvider(model -> switch (model.getSelectionMode()) {
                    case SINGLE -> List.of(
                            selectionBox.createExtraButton("Select Previous", model::selectPrevious),
                            selectionBox.createExtraButton("Select Next", model::selectNext)
                    );
                    case MULTIPLE -> List.of(
                            selectionBox.createExtraButton("Select First", model::selectFirst),
                            selectionBox.createExtraButton("Select Last", model::selectLast)
                    );
                })
        );

        // extra buttons position
        ComboBox<SelectionBox.VerticalPosition> extraButtonsPositionComboBox = new ComboBox<>();
        extraButtonsPositionComboBox.getItems().addAll(SelectionBox.VerticalPosition.values());
        extraButtonsPositionComboBox.valueProperty().bindBidirectional(selectionBox.extraButtonsPositionProperty());

        // use custom string converter
        CheckBox useCustomStringConverterCheckBox = new CheckBox("Use Custom String Converter");
        useCustomStringConverterCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                selectionBox.setSelectedItemsConverter(new StringConverter<>() {
                    @Override
                    public String toString(List<String> object) {
                        if (object == null || object.isEmpty()) {
                            return "Empty";
                        }
                        return object.stream().map(s -> ">> " + s).reduce((s1, s2) -> s1 + ", " + s2).orElse("");
                    }

                    @Override
                    public List<String> fromString(String string) {
                        return List.of();
                    }
                });
            } else {
                selectionBox.setSelectedItemsConverter(null);
            }
        });

        // auto hide on select
        CheckBox autoHideOnSelectCheckBox = new CheckBox("Auto Hide On Select");
        autoHideOnSelectCheckBox.selectedProperty().bindBidirectional(selectionBox.autoHideOnSelectionProperty());

        // select all
        Button selectAllButton = new Button("Select All");
        selectAllButton.setMaxWidth(Double.MAX_VALUE);
        selectAllButton.setOnAction(e -> selectionBox.getSelectionModel().selectAll());

        // select first
        Button selectFirstButton = new Button("Select First");
        selectFirstButton.setMaxWidth(Double.MAX_VALUE);
        selectFirstButton.setOnAction(e -> selectionBox.getSelectionModel().selectFirst());

        // select last
        Button selectLastButton = new Button("Select Last");
        selectLastButton.setMaxWidth(Double.MAX_VALUE);
        selectLastButton.setOnAction(e -> selectionBox.getSelectionModel().selectLast());

        // select previous
        Button selectPreviousButton = new Button("Select Previous");
        selectPreviousButton.setMaxWidth(Double.MAX_VALUE);
        selectPreviousButton.setOnAction(e -> selectionBox.getSelectionModel().selectPrevious());

        // select next
        Button selectNextButton = new Button("Select Next");
        selectNextButton.setMaxWidth(Double.MAX_VALUE);
        selectNextButton.setOnAction(e -> selectionBox.getSelectionModel().selectNext());

        // select item
        Button selectItemButton = new Button("Select \"Option C\"");
        selectItemButton.setMaxWidth(Double.MAX_VALUE);
        selectItemButton.setOnAction(e -> selectionBox.getSelectionModel().select("Option C"));

        // select item index
        Button selectItemIndexButton = new Button("Select Index (1)");
        selectItemIndexButton.setMaxWidth(Double.MAX_VALUE);
        selectItemIndexButton.setOnAction(e -> selectionBox.getSelectionModel().select(1));

        // select multiple items
        Button selectMultipleItemsButton = new Button("Select Multiple Items");
        selectMultipleItemsButton.setMaxWidth(Double.MAX_VALUE);
        selectMultipleItemsButton.setOnAction(e -> selectionBox.getSelectionModel().selectRange(3, 6));

        VBox selectTestButtonsBox = new VBox(5);
        selectTestButtonsBox.setFillWidth(true);
        selectTestButtonsBox.getChildren().addAll(selectAllButton, selectFirstButton, selectLastButton, selectPreviousButton, selectNextButton, selectItemButton, selectItemIndexButton, selectMultipleItemsButton);

        // deselect item
        Button deselectItemButton = new Button("Deselect Item Index 1");
        deselectItemButton.setMaxWidth(Double.MAX_VALUE);
        deselectItemButton.setOnAction(e -> selectionBox.getSelectionModel().clearSelection(1));

        // clear selection
        Button clearSelectionButton = new Button("Clear Selection");
        clearSelectionButton.setMaxWidth(Double.MAX_VALUE);
        clearSelectionButton.setOnAction(e -> selectionBox.getSelectionModel().clearSelection());

        VBox clearSelectionButtonsBox = new VBox(5);
        clearSelectionButtonsBox.setFillWidth(true);
        clearSelectionButtonsBox.getChildren().addAll(deselectItemButton, clearSelectionButton);

        // change items
        Button changeItemsButton = new Button("Change Items");
        changeItemsButton.setMaxWidth(Double.MAX_VALUE);
        changeItemsButton.setOnAction(e -> {
            selectionBox.setItems(FXCollections.observableArrayList("Changed 1", "Changed 2", "Changed 3"));
        });

        // replace items
        Button replaceItemsButton = new Button("Replace Items");
        replaceItemsButton.setMaxWidth(Double.MAX_VALUE);
        replaceItemsButton.setOnAction(e -> {
            selectionBox.getItems().setAll("Replace 1", "Replace 2", "Replace 3");
        });

        // add items
        Button addItemsButton = new Button("Add Items");
        addItemsButton.setMaxWidth(Double.MAX_VALUE);
        addItemsButton.setOnAction(e -> {
            selectionBox.getItems().addAll("Added 1", "Added 2", "Added 3");
        });

        // remove items
        Button removeItemsButton = new Button("Remove Items");
        removeItemsButton.setMaxWidth(Double.MAX_VALUE);
        removeItemsButton.setOnAction(e -> {
            selectionBox.getItems().remove(0);
        });

        // clear items
        Button clearItemsButton = new Button("Clear Items");
        clearItemsButton.setMaxWidth(Double.MAX_VALUE);
        clearItemsButton.setOnAction(e -> {
            selectionBox.getItems().clear();
        });

        VBox itemsButtonsBox = new VBox(5);
        itemsButtonsBox.setFillWidth(true);
        itemsButtonsBox.getChildren().addAll(changeItemsButton, replaceItemsButton, addItemsButton, removeItemsButton, clearItemsButton);

        return new SimpleControlPane(
                "SelectionBox",
                new SimpleControlPane.ControlItem("Selection Mode", selectionModeComboBox),
                new SimpleControlPane.ControlItem("Show Extra Buttons", visibleExtraButtonsCheckBox),
                new SimpleControlPane.ControlItem("Change Extra Buttons", changeExtraButtonsButton),
                new SimpleControlPane.ControlItem("Extra Buttons Position", extraButtonsPositionComboBox),
                new SimpleControlPane.ControlItem("Use Custom String Converter", useCustomStringConverterCheckBox),
                new SimpleControlPane.ControlItem("Auto Hide On Select", autoHideOnSelectCheckBox),
                new SimpleControlPane.ControlItem("Select Method", selectTestButtonsBox),
                new SimpleControlPane.ControlItem("Clear Selection", clearSelectionButtonsBox),
                new SimpleControlPane.ControlItem("Items", itemsButtonsBox)
        );
    }

}
