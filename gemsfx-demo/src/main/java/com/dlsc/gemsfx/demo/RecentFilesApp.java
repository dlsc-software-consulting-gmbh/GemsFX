package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.util.RecentFiles;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.prefs.Preferences;

public class RecentFilesApp extends GemApplication {

    private static final Preferences PREFS =
            Preferences.userNodeForPackage(RecentFilesApp.class);

    @Override
    public void start(Stage stage) {
        super.start(stage);

        // Create the RecentFiles manager backed by Java Preferences.
        RecentFiles recentFiles = new RecentFiles(PREFS);

        Label statusLabel = new Label("No file opened yet.");
        statusLabel.setWrapText(true);

        // Callback: called when a file is selected from the recent files menu.
        recentFiles.setOnOpenFile(file -> statusLabel.setText("Opened: " + file.getAbsolutePath()));

        // Build a standard File menu that includes Open and the Recent Files sub-menu.
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");

        MenuItem openItem = new MenuItem("Open…");
        openItem.setOnAction(evt -> {
            File chosen = fileChooser.showOpenDialog(stage);
            if (chosen != null) {
                recentFiles.add(chosen);
                statusLabel.setText("Opened: " + chosen.getAbsolutePath());
            }
        });

        Menu fileMenu = new Menu("File");
        fileMenu.getItems().addAll(openItem, new SeparatorMenuItem(), recentFiles.getMenu());

        MenuBar menuBar = new MenuBar(fileMenu);

        Button clearButton = new Button("Clear Recent Files");
        clearButton.setOnAction(evt -> {
            recentFiles.clear();
            statusLabel.setText("Recent files cleared.");
        });

        VBox content = new VBox(16, statusLabel, clearButton);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(24));

        BorderPane root = new BorderPane();
        root.setTop(menuBar);
        root.setCenter(content);

        Scene scene = new Scene(root, 520, 260);
        stage.setTitle("Recent Files Demo");
        stage.setScene(scene);
        StageManager.install(stage, "recent.files.app");

        stage.show();
    }

        @Override
    public String getDescription() {
        return """
                ### RecentFiles
                
                Manages a "Recent Files" list, persisting file paths via Java `Preferences`
                and providing a self-updating JavaFX `Menu` that reflects the current list.
                
                Typical usage:
                ```
                `RecentFiles recentFiles = new RecentFiles(
                      Preferences.userNodeForPackage(MyApp.class));
                  recentFiles.setOnOpenFile(file -> loadDocument(file));
                  menuBar.getMenus().add(recentFiles.getMenu());
                
                  // After the user opens a file:
                  recentFiles.add(chosenFile);
                `
                ```
                
                The default maximum number of entries is {@value #DEFAULT_MAX_FILES}.
                Use `setMaxFiles(int)` to change it.
                """;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
