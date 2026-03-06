package com.dlsc.gemsfx.util;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.util.StringConverter;

import java.io.File;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

/**
 * Manages a "Recent Files" list, persisting file paths via Java {@link Preferences}
 * and providing a self-updating JavaFX {@link Menu} that reflects the current list.
 *
 * <p>Typical usage:</p>
 * <pre>{@code
 *   RecentFiles recentFiles = new RecentFiles(
 *       Preferences.userNodeForPackage(MyApp.class));
 *   recentFiles.setOnOpenFile(file -> loadDocument(file));
 *   menuBar.getMenus().add(recentFiles.getMenu());
 *
 *   // After the user opens a file:
 *   recentFiles.add(chosenFile);
 * }</pre>
 *
 * <p>The default maximum number of entries is {@value #DEFAULT_MAX_FILES}.
 * Use {@link #setMaxFiles(int)} to change it.</p>
 */
public class RecentFiles {

    /** Default maximum number of recent file entries shown in the menu. */
    public static final int DEFAULT_MAX_FILES = 5;

    /** Default preferences key under which file paths are stored. */
    public static final String DEFAULT_PREFS_KEY = "recent.files";

    private static final StringConverter<File> FILE_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(File file) {
            return file == null ? "" : file.getAbsolutePath();
        }

        @Override
        public File fromString(String path) {
            return (path == null || path.isBlank()) ? null : new File(path);
        }
    };

    private final PreferencesHistoryManager<File> historyManager;
    private final Menu menu;

    /**
     * Creates a {@code RecentFiles} manager using the given {@link Preferences} node.
     * File paths are stored under the key {@value #DEFAULT_PREFS_KEY}.
     *
     * @param preferences the preferences node used for persistence; must not be {@code null}
     */
    public RecentFiles(Preferences preferences) {
        this(preferences, DEFAULT_PREFS_KEY);
    }

    /**
     * Creates a {@code RecentFiles} manager using the given {@link Preferences} node
     * and a custom preferences key.
     *
     * @param preferences the preferences node used for persistence; must not be {@code null}
     * @param prefsKey    the key under which file paths are stored; must not be {@code null}
     */
    public RecentFiles(Preferences preferences, String prefsKey) {
        Objects.requireNonNull(preferences, "preferences must not be null");
        Objects.requireNonNull(prefsKey, "prefsKey must not be null");

        historyManager = new PreferencesHistoryManager<>(preferences, prefsKey, FILE_CONVERTER);
        historyManager.setMaxHistorySize(DEFAULT_MAX_FILES);
        historyManager.setFilter(file -> file != null && !file.getAbsolutePath().isBlank());

        menu = new Menu("Recent Files");
        historyManager.getAllUnmodifiable().addListener((Observable obs) -> rebuildMenu());
        rebuildMenu();
    }

    /**
     * Adds the given file to the top of the recent files list and persists it immediately.
     * Duplicate entries are automatically removed before re-inserting at the top.
     *
     * @param file the file to add; ignored if {@code null}
     */
    public void add(File file) {
        historyManager.add(file);
    }

    /**
     * Clears the recent files list and persists the change immediately.
     */
    public void clear() {
        historyManager.clear();
    }

    /**
     * Returns the {@link Menu} that reflects the current recent files list.
     * Add this to a {@link javafx.scene.control.MenuBar}.
     * The menu updates automatically whenever files are added or removed.
     *
     * @return the "Recent Files" menu; never {@code null}
     */
    public Menu getMenu() {
        return menu;
    }

    /**
     * Returns an unmodifiable observable list of the current recent files.
     *
     * @return the recent files; never {@code null}
     */
    public ObservableList<File> getRecentFiles() {
        return historyManager.getAllUnmodifiable();
    }

    // -- onOpenFile ----------------------------------------------------------

    private final ObjectProperty<Consumer<File>> onOpenFile =
            new SimpleObjectProperty<>(this, "onOpenFile");

    /**
     * The callback invoked when the user clicks a recent file entry in the menu.
     *
     * @return the property holding the open-file callback
     */
    public final ObjectProperty<Consumer<File>> onOpenFileProperty() {
        return onOpenFile;
    }

    public final Consumer<File> getOnOpenFile() {
        return onOpenFile.get();
    }

    public final void setOnOpenFile(Consumer<File> callback) {
        onOpenFile.set(callback);
    }

    // -- maxFiles ------------------------------------------------------------

    /**
     * Returns the maximum number of recent files tracked and shown in the menu.
     * Defaults to {@value #DEFAULT_MAX_FILES}.
     *
     * @return the current maximum
     */
    public final int getMaxFiles() {
        return historyManager.getMaxHistorySize();
    }

    /**
     * Sets the maximum number of recent files tracked and shown in the menu.
     * If the current list exceeds the new maximum, oldest entries are removed.
     *
     * @param max the new maximum; must be &gt;= 0
     */
    public final void setMaxFiles(int max) {
        historyManager.setMaxHistorySize(max);
    }

    // -- private -------------------------------------------------------------

    private void rebuildMenu() {
        menu.getItems().clear();

        ObservableList<File> files = historyManager.getAllUnmodifiable();

        if (files.isEmpty()) {
            MenuItem empty = new MenuItem("No Recent Files");
            empty.setDisable(true);
            menu.getItems().add(empty);
        } else {
            for (File file : files) {
                MenuItem item = new MenuItem(file.getName());
                item.setOnAction(evt -> {
                    Consumer<File> callback = getOnOpenFile();
                    if (callback != null) {
                        callback.accept(file);
                    }
                });
                menu.getItems().add(item);
            }
            menu.getItems().add(new SeparatorMenuItem());
            MenuItem clearItem = new MenuItem("Clear Recent Files");
            clearItem.setOnAction(evt -> clear());
            menu.getItems().add(clearItem);
        }
    }
}
