package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.SearchTextField;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SearchTextFieldApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        SearchTextField field1 = new SearchTextField();
        SearchTextField field2 = new SearchTextField(true);

        CheckBox enableHistoryPopupBox = new CheckBox("Enable History Popup");
        enableHistoryPopupBox.setSelected(true);
        field1.enableHistoryPopupProperty().bindBidirectional(enableHistoryPopupBox.selectedProperty());
        field2.enableHistoryPopupProperty().bindBidirectional(enableHistoryPopupBox.selectedProperty());

        Spinner<Integer> maxHistorySizeSpinner = new Spinner<>(5, 50, 10, 5);
        field1.maxHistorySizeProperty().bind(maxHistorySizeSpinner.valueProperty());
        maxHistorySizeSpinner.setMaxWidth(Double.MAX_VALUE);

        VBox vbox = new VBox(20, new Label("Standard"), field1, new Label("Round"), field2, enableHistoryPopupBox,maxHistorySizeSpinner);
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
