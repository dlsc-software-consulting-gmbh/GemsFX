package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SearchTextField;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
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
import java.util.prefs.Preferences;

public class SearchTextFieldApp extends Application {

    private SearchTextField field1;

    @Override
    public void start(Stage primaryStage) throws Exception {

        field1 = new SearchTextField();
        field1.setPreferences(Preferences.userNodeForPackage(SearchTextFieldApp.class).node("field1"));

        SearchTextField field2 = new SearchTextField(true);
        field2.setPreferences(Preferences.userNodeForPackage(SearchTextFieldApp.class).node("field2"));

        Label label = new Label("Max History Size:");
        Spinner<Integer> maxHistorySizeSpinner = new Spinner<>(5, 50, 10, 5);
        field1.maxHistorySizeProperty().bind(maxHistorySizeSpinner.valueProperty());
        maxHistorySizeSpinner.setMaxWidth(Double.MAX_VALUE);
        HBox maxHistorySizeBox = new HBox(5, label, maxHistorySizeSpinner);
        maxHistorySizeBox.setAlignment(Pos.CENTER_LEFT);

        CheckBox enableHistoryPopupBox = new CheckBox("Enable History Popup");
        enableHistoryPopupBox.setSelected(true);
        field1.enableHistoryPopupProperty().bindBidirectional(enableHistoryPopupBox.selectedProperty());
        field2.enableHistoryPopupProperty().bindBidirectional(enableHistoryPopupBox.selectedProperty());

        CheckBox addHistoryOnActionBox = new CheckBox("Add History on Enter");
        addHistoryOnActionBox.setSelected(true);
        field1.addingItemToHistoryOnEnterProperty().bind(addHistoryOnActionBox.selectedProperty());
        field2.addingItemToHistoryOnEnterProperty().bind(addHistoryOnActionBox.selectedProperty());

        CheckBox addHistoryOnFocusLossBox = new CheckBox("Add History on Focus Loss");
        addHistoryOnFocusLossBox.setSelected(true);
        field1.addingItemToHistoryOnFocusLostProperty().bind(addHistoryOnFocusLossBox.selectedProperty());
        field2.addingItemToHistoryOnFocusLostProperty().bind(addHistoryOnFocusLossBox.selectedProperty());

        Button setHistoryButton = new Button("Set History");
        setHistoryButton.setMaxWidth(Double.MAX_VALUE);
        setHistoryButton.setOnAction(e -> {
            List<String> list = List.of("One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten");
            field1.setHistory(list);
            field2.setHistory(list);
        });

        Button addHistoryButton = new Button("Add History");
        addHistoryButton.setMaxWidth(Double.MAX_VALUE);
        addHistoryButton.setOnAction(e -> {
            field1.addHistory("New " + LocalTime.now());
            field2.addHistory("New" + LocalTime.now());
        });

        Button removeStandardHistoryButton = createRemoveHistoryButton("Standard Field Remove First History Item", field1);
        Button removeRoundHistoryButton = createRemoveHistoryButton("Round Field Remove First History Item", field2);

        Button clearButton = new Button("Clear History");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(e -> {
            field1.clearHistory();
            field2.clearHistory();
        });

        VBox vbox = new VBox(20, new Label("Standard"), field1, new Label("Round"), field2,
                new Separator(), maxHistorySizeBox, enableHistoryPopupBox, addHistoryOnActionBox, addHistoryOnFocusLossBox,
                setHistoryButton, addHistoryButton, removeStandardHistoryButton, removeRoundHistoryButton, clearButton);
        vbox.setPadding(new Insets(20));

        Scene scene = new Scene(vbox);
        primaryStage.setTitle("Search Text Field");
        primaryStage.setScene(scene);
        primaryStage.sizeToScene();
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    private Button createRemoveHistoryButton(String text, SearchTextField field) {
        Button removeHistoryButton2 = new Button(text);
        removeHistoryButton2.disableProperty().bind(Bindings.createObjectBinding(() -> field.getUnmodifiableHistory().isEmpty(), field.getUnmodifiableHistory()));
        removeHistoryButton2.setMaxWidth(Double.MAX_VALUE);
        removeHistoryButton2.setOnAction(e -> field.removeHistory(field.getUnmodifiableHistory().get(0)));
        return removeHistoryButton2;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
