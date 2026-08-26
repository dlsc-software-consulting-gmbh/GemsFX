package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ChipView;
import com.dlsc.gemsfx.ChipsViewContainer;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.ArrayList;
import java.util.List;

/**
 * Demonstrates the {@link ChipsViewContainer} control. The container shows one
 * {@link ChipView} for each currently selected filter value, plus a hyperlink for clearing the
 * entire selection. The container hides itself automatically when no chips are left.
 */
public class ChipsViewContainerApp extends GemApplication {

    private static final List<String> FRUITS = List.of(
            "Apple", "Apricot", "Banana", "Blueberry", "Cherry", "Coconut",
            "Fig", "Grape", "Kiwi", "Lemon", "Mango", "Melon",
            "Orange", "Papaya", "Peach", "Pear", "Pineapple", "Strawberry");

    private final ChipsViewContainer container = new ChipsViewContainer();

    @Override
    public void start(Stage stage) {
        super.start(stage);

        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(FRUITS);
        listView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listView.getSelectionModel().selectIndices(0, 4, 9);

        // the chips always mirror the current selection of the list view
        listView.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<String>) change -> updateChips(listView));

        // clicking on the "clear" hyperlink resets the entire selection
        container.setOnClear(() -> listView.getSelectionModel().clearSelection());

        updateChips(listView);

        Label hintLabel = new Label("Select one or more fruits. Each selected fruit is shown as a chip above the list.");
        hintLabel.setWrapText(true);

        VBox contentBox = new VBox(10, container, hintLabel, listView);
        contentBox.setPadding(new Insets(20));
        VBox.setVgrow(listView, Priority.ALWAYS);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        HBox box = new HBox(20, contentBox, createOptions(listView));
        box.setFillHeight(true);

        Scene scene = new Scene(box);

        stage.setTitle("Chips View Container Demo");
        stage.setScene(scene);
        stage.setWidth(900);
        stage.setHeight(600);
        stage.show();
    }

    /**
     * Creates one chip for each selected item of the given list view. Closing a chip removes
     * the corresponding item from the selection.
     */
    private void updateChips(ListView<String> listView) {
        List<ChipView<?>> chips = new ArrayList<>();

        for (String fruit : listView.getSelectionModel().getSelectedItems()) {
            if (fruit == null) {
                continue;
            }

            ChipView<String> chipView = new ChipView<>();
            chipView.setValue(fruit);
            chipView.setText(fruit);
            chipView.setGraphic(new FontIcon(MaterialDesign.MDI_FOOD_APPLE));
            chipView.setOnClose(value -> {
                int index = listView.getItems().indexOf(value);
                listView.getSelectionModel().clearSelection(index);
            });

            chips.add(chipView);
        }

        container.getChips().setAll(chips);
    }

    /**
     * Creates the panel with the settings that can be changed by the user.
     */
    private VBox createOptions(ListView<String> listView) {
        TextField clearTextField = new TextField();
        clearTextField.textProperty().bindBidirectional(container.clearTextProperty());
        clearTextField.setMaxWidth(Double.MAX_VALUE);

        CheckBox clearSupportBox = new CheckBox("Support clearing");
        clearSupportBox.setSelected(true);
        clearSupportBox.selectedProperty().addListener(it -> container.setOnClear(clearSupportBox.isSelected()
                ? () -> listView.getSelectionModel().clearSelection()
                : null));

        Button selectAllButton = new Button("Select all");
        selectAllButton.setMaxWidth(Double.MAX_VALUE);
        selectAllButton.setOnAction(evt -> listView.getSelectionModel().selectAll());

        Button clearButton = new Button("Clear selection");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(evt -> listView.getSelectionModel().clearSelection());

        Label visibilityLabel = new Label();
        visibilityLabel.setWrapText(true);
        visibilityLabel.textProperty().bind(Bindings.createStringBinding(
                () -> "The container manages its own visibility. It is currently "
                        + (container.isVisible() ? "visible" : "hidden") + ".",
                container.visibleProperty()));

        Button devTools = configureDevToolsButton(new Button());
        devTools.setMaxWidth(Double.MAX_VALUE);

        VBox optionsBox = new VBox(10,
                new Label("Text of the clear hyperlink"), clearTextField,
                clearSupportBox,
                new Separator(),
                selectAllButton,
                clearButton,
                new Separator(),
                visibilityLabel,
                devTools);

        createThemeSwitcher().ifPresent(switcher -> optionsBox.getChildren().add(0, switcher));

        optionsBox.setPrefWidth(240);
        optionsBox.setMinWidth(240);
        optionsBox.setPadding(new Insets(20));
        return optionsBox;
    }

    public static void main(String[] args) {
        launch();
    }
}
