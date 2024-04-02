package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.ResizableTextArea;
import javafx.application.Application;
import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ResizableTextAreaApp extends Application {

    @Override
    public void start(Stage stage) {
        TextArea textArea = new TextArea("Standard text area ...");
        textArea.setWrapText(true);
        textArea.setMinHeight(100);
        textArea.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);

        ResizableTextArea resizableTextArea = new ResizableTextArea("Resizable text area ...");
        resizableTextArea.setWrapText(true);
        resizableTextArea.setMinHeight(100);
        resizableTextArea.pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), true);

        VBox box = new VBox(40, textArea, resizableTextArea);
        box.setFillWidth(false);
        box.setPadding(new Insets(50));
        box.setAlignment(Pos.CENTER);

        CheckBox vCheck = new CheckBox("Vertical");
        vCheck.selectedProperty().bindBidirectional(resizableTextArea.resizeVerticalProperty());

        CheckBox hCheck = new CheckBox("Horizontal");
        hCheck.selectedProperty().bindBidirectional(resizableTextArea.resizeHorizontalProperty());

        HBox optionsBox = new HBox(10, new Label("Resizable:"), vCheck, hCheck);
        optionsBox.setAlignment(Pos.CENTER);
        optionsBox.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        StackPane.setAlignment(optionsBox, Pos.BOTTOM_CENTER);
        StackPane.setMargin(optionsBox, new Insets(20));

        StackPane stackPane = new StackPane(box, optionsBox);

        Scene scene = new Scene(stackPane);
        stage.setTitle("Resizable Text Area Demo");
        stage.setScene(scene);
        stage.setWidth(1000);
        stage.setHeight(850);
        stage.centerOnScreen();
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
