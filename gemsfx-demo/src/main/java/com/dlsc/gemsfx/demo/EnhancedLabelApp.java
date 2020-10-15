package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.EnhancedLabel;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class EnhancedLabelApp extends Application {

    @Override
    public void start(Stage stage) {
        EnhancedLabel label1 = new EnhancedLabel("This is an enhanced label, try to copy me! (Double click to select first, then shortcut. Or use the context menu.)");
        EnhancedLabel label2 = new EnhancedLabel("Enhanced label with a custom copy content supplier. Copies only this ID: 12345678");
        label2.setCopyContentSupplier(() -> "12345678");
        label2.setCopyMenuItemText("Copy ID");

        ToggleButton showButton = new ToggleButton("Toggle Label Selection");
        showButton.selectedProperty().bindBidirectional(label1.selectedProperty());

        VBox box = new VBox(20, label1, showButton, label2);
        box.setPadding(new Insets(20));

        Scene scene = new Scene(box);
        stage.setTitle("Enhanced Label Demo");
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
