package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SearchTextField;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.HistoryManager;
import com.dlsc.gemsfx.util.StringHistoryManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.prefs.Preferences;

public class SearchTextFieldApp extends Application {

    private StringHistoryManager stringHistoryManager;

    @Override
    public void start(Stage primaryStage) throws Exception {

        SearchTextField field = new SearchTextField();

        CheckBox roundBox = new CheckBox("Round");
        field.roundProperty().bind(roundBox.selectedProperty());

        CheckBox enableHistoryBox = new CheckBox("Enable History");
        enableHistoryBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                if (stringHistoryManager == null) {
                    Preferences preferences = Preferences.userNodeForPackage(SearchTextFieldApp.class);
                    stringHistoryManager = new StringHistoryManager(preferences, "search-text-field-id");
                }
                field.setHistoryManager(stringHistoryManager);
            } else {
                field.setHistoryManager(null);
            }
            primaryStage.sizeToScene();
        });
        enableHistoryBox.setSelected(true);

        Label label = new Label("Max History Size:");
        Spinner<Integer> maxHistorySizeSpinner = new Spinner<>(5, 50, 30, 5);
        maxHistorySizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            HistoryManager<String> historyManager = field.getHistoryManager();
            if (newVal != null && historyManager != null) {
                historyManager.setMaxHistorySize(newVal);
            }
        });

        maxHistorySizeSpinner.setMaxWidth(140);
        HBox maxHistorySizeBox = new HBox(5, label, new Spacer(), maxHistorySizeSpinner);
        maxHistorySizeBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox addHistoryOnActionBox = new CheckBox("Add History on Enter");
        addHistoryOnActionBox.setSelected(true);
        field.addingItemToHistoryOnEnterProperty().bind(addHistoryOnActionBox.selectedProperty());

        CheckBox addHistoryOnFocusLossBox = new CheckBox("Add History on Focus Loss");
        addHistoryOnFocusLossBox.setSelected(true);
        field.addingItemToHistoryOnFocusLostProperty().bind(addHistoryOnFocusLossBox.selectedProperty());

        Button setHistoryButton = new Button("Set History");
        setHistoryButton.setMaxWidth(Double.MAX_VALUE);
        setHistoryButton.setOnAction(e -> {
            List<String> list = List.of("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten");
            Optional.ofNullable(field.getHistoryManager()).ifPresent(historyManager -> {
                historyManager.set(list);
                System.out.println("History set to: " + list);
            });
        });

        Button addHistoryButton = new Button("Add History");
        addHistoryButton.setMaxWidth(Double.MAX_VALUE);
        addHistoryButton.setOnAction(e -> Optional.ofNullable(field.getHistoryManager()).ifPresent(historyManager -> historyManager.add("New " + LocalTime.now())));

        Button removeHistoryButton = new Button("Remove First History Item");
        removeHistoryButton.setMaxWidth(Double.MAX_VALUE);
        removeHistoryButton.setOnAction(e -> Optional.ofNullable(field.getHistoryManager()).ifPresent(historyManager -> historyManager.remove(historyManager.getAllUnmodifiable().get(0))));

        Button clearButton = new Button("Clear History");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(e -> Optional.ofNullable(field.getHistoryManager()).ifPresent(HistoryManager::clear));

        VBox historyControls = new VBox(5, new Separator(), maxHistorySizeBox, addHistoryOnActionBox, addHistoryOnFocusLossBox,
                setHistoryButton, addHistoryButton, clearButton);
        historyControls.managedProperty().bind(enableHistoryBox.selectedProperty());
        historyControls.visibleProperty().bind(enableHistoryBox.selectedProperty());

        VBox vbox = new VBox(20, new Label("Standard"), field, roundBox, enableHistoryBox, historyControls);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);
        primaryStage.setTitle("Search Text Field");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
