package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SelectionBox;
import com.dlsc.gemsfx.demo.fake.SimpleControlPane;
import com.dlsc.gemsfx.util.SimpleStringConverter;
import fr.brouillard.oss.cssfx.CSSFX;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SelectionBoxApp extends Application {

    private final SelectionBox<String> selectionBox = new SelectionBox<>();
    private final StackPane topNode = new StackPane();
    private final StackPane bottomNode = new StackPane();
    private final StackPane leftNode = new StackPane();
    private final StackPane rightNode = new StackPane();

    @Override
    public void start(Stage primaryStage) throws Exception {
        selectionBox.show();

        topNode.setStyle("-fx-background-color: lightblue;-fx-padding: 10;");
        topNode.getChildren().add(new Label("Top"));

        bottomNode.getChildren().add(new Label("Bottom"));
        bottomNode.setStyle("-fx-background-color: lightcoral;-fx-padding: 10;");

        leftNode.getChildren().add(new Label("Left"));
        leftNode.setStyle("-fx-background-color: lightgreen;-fx-padding: 10;");

        rightNode.getChildren().add(new Label("Right"));
        rightNode.setStyle("-fx-background-color: lightyellow;-fx-padding: 10;");

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
        selectionBox.getSelectionModel().selectFirst();
        // selectionBox.setItemConverter(new SimpleStringConverter<>(s -> ">>" +s));

        StackPane wrapper = new StackPane(selectionBox);
        wrapper.setStyle("-fx-background-color: white; -fx-padding: 50px;");
        return wrapper;
    }

    private Node getControlPanel() {
        // show popup button
        Button showButton = new Button("Show Popup");
        showButton.setOnAction(e -> selectionBox.show());

        // selection mode
        ComboBox<SelectionMode> selectionModeComboBox = new ComboBox<>();
        selectionModeComboBox.getItems().addAll(SelectionMode.SINGLE, SelectionMode.MULTIPLE);
        selectionModeComboBox.valueProperty().bindBidirectional(selectionBox.getSelectionModel().selectionModeProperty());

        // show extra nodes
        CheckBox showExtraNodesCheckBox = new CheckBox("Show Extra Nodes");
        // cache the top node
        Node selectionBoxTop = selectionBox.getTop();
        showExtraNodesCheckBox.selectedProperty().subscribe(showExtraNodes -> {
            if (showExtraNodes) {
                selectionBox.setTop(topNode);
                selectionBox.setBottom(bottomNode);
                selectionBox.setLeft(leftNode);
                selectionBox.setRight(rightNode);
            } else {
                selectionBox.setTop(selectionBoxTop);
                selectionBox.setBottom(null);
                selectionBox.setLeft(null);
                selectionBox.setRight(null);
            }
        });

        // prompt text
        CheckBox promptTextCheckBox = new CheckBox("Change Prompt Text");
        promptTextCheckBox.setOnAction(evt -> {
            selectionBox.getSelectionModel().clearSelection();
            if (promptTextCheckBox.isSelected()) {
                selectionBox.setPromptText("Select");
            } else {
                selectionBox.setPromptText("No Selection");
            }
        });

        // placeholder
        CheckBox placeholderCheckBox = new CheckBox("Add a placeholder when no items");
        placeholderCheckBox.setSelected(true);
        selectionBox.placeholderProperty().bind(Bindings.createObjectBinding(() -> {
            if (placeholderCheckBox.isSelected()) {
                Label label = new Label("No items available");
                label.setStyle("-fx-text-fill: #969696;");
                StackPane stackPane = new StackPane(label);
                stackPane.setStyle("-fx-background-color: #fcfcfc; -fx-padding: 20px;");
                return stackPane;
            } else {
                return null;
            }
        }, placeholderCheckBox.selectedProperty()));

        // use custom string converter
        CheckBox useCustomStringConverterCheckBox = new CheckBox("Use Custom String Converter");
        useCustomStringConverterCheckBox.setSelected(true);
        selectionBox.selectedItemsConverterProperty().bind(Bindings.createObjectBinding(() -> {
                    if (useCustomStringConverterCheckBox.isSelected()) {
                        // No need to consider the case where selectedItems is empty,
                        // because if nothing is selected, promptText will be displayed.
                        // You can always return the promptText as well, for example "Select" or "Please choose".
                        // return "Please choose";  // or return selectionBox.getPromptText();
                        if (selectionBox.getSelectionModel().getSelectionMode() == SelectionMode.SINGLE) {
                            return new SimpleStringConverter<>(selectedItems -> "[ " + selectionBox.getSelectionModel().getSelectedItem() + " ]");
                        } else {
                            return new SimpleStringConverter<>(selectedItems -> selectedItems.size() + " items selected");
                        }
                    } else {
                        return null;
                    }
                },
                useCustomStringConverterCheckBox.selectedProperty(), selectionBox.getSelectionModel().selectionModeProperty()));

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

        // animation enabled
        CheckBox animationEnabledCheckBox = new CheckBox("Enable Animation");
        animationEnabledCheckBox.selectedProperty().bindBidirectional(selectionBox.animationEnabledProperty());

        return new SimpleControlPane(
                "SelectionBox",
                new SimpleControlPane.ControlItem("Show Popup", showButton),
                new SimpleControlPane.ControlItem("Selection Mode", selectionModeComboBox),
                new SimpleControlPane.ControlItem("Show Extra Nodes", showExtraNodesCheckBox),
                new SimpleControlPane.ControlItem("Show Placeholder", placeholderCheckBox),
                new SimpleControlPane.ControlItem("Enable Animation", animationEnabledCheckBox),
                new SimpleControlPane.ControlItem("Change Prompt Text", promptTextCheckBox),
                new SimpleControlPane.ControlItem("Use Custom String Converter", useCustomStringConverterCheckBox),
                new SimpleControlPane.ControlItem("Auto Hide On Select", autoHideOnSelectCheckBox),
                new SimpleControlPane.ControlItem("Select Method", selectTestButtonsBox),
                new SimpleControlPane.ControlItem("Clear Selection", clearSelectionButtonsBox),
                new SimpleControlPane.ControlItem("Items", itemsButtonsBox)
        );
    }

}
