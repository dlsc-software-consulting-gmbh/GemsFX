package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.PDFView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

import java.io.File;

public class PDFViewApp extends Application {

    private FileChooser chooser;

    @Override
    public void start(Stage primaryStage) {
        PDFView view = new PDFView();

        MenuItem loadItem = new MenuItem("Load PDF ...");
        loadItem.setAccelerator(KeyCombination.valueOf("SHORTCUT+o"));
        loadItem.setOnAction(evt -> {
            if (chooser == null) {
                chooser = new FileChooser();
                chooser.setTitle("Load PDF File");
                final ExtensionFilter filter = new ExtensionFilter("PDF Files", "*.pdf");
                chooser.getExtensionFilters().add(filter);
                chooser.setSelectedExtensionFilter(filter);
            }

            final File file = chooser.showOpenDialog(view.getScene().getWindow());
            if (file != null) {
                view.load(file);
            }
        });

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().add(loadItem);

        MenuBar menuBar = new MenuBar(fileMenu);
        menuBar.setUseSystemMenuBar(false);

        VBox.setVgrow(view, Priority.ALWAYS);
        VBox box = new VBox(menuBar, view);
        box.setFillWidth(true);

        Scene scene = new Scene(box);
        primaryStage.setTitle("PDF View");
        primaryStage.setWidth(1000);
        primaryStage.setHeight(900);
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }
}
