package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.Styles;
import atlantafx.decorations.HeaderButton;
import atlantafx.decorations.HeaderButtonGroup;
import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.DialogPane.Dialog;
import com.dlsc.gemsfx.DialogPane.DialogHeader;
import com.dlsc.gemsfx.GlassPane;
import com.dlsc.gemsfx.showcase.DemoEmbedder.EmbeddedDemo;
import com.dlsc.gemsfx.util.StageManager;
import com.dlsc.pdfviewfx.PDFBoxDocument;
import com.dlsc.pdfviewfx.PDFView;
import devtoolsfx.gui.GUI;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderButtonType;
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

/**
 * The GemsFX showcase application. The application lists all controls of the GemsFX library,
 * grouped by category. Selecting a control displays the PDF
 * manual of that control. Double-clicking a control additionally launches the demo application
 * of that control, holding down the shift key while double-clicking also opens the developer
 * tools for the launched demo.
 */
public class ShowcaseApp extends Application {

    static {
        // the demos run inside of this application, hence they must not apply a theme of their
        // own, which would replace the theme that the user has selected in the showcase
        System.setProperty("showcase", "true");
    }

    private static final String PREF_SELECTED_ENTRY = "selected.entry";
    private static final String PREF_SHOW_ALL = "pdf.show.all";
    private static final String PREF_SHOW_THUMBNAILS = "pdf.show.thumbnails";
    private static final String PREF_OPEN_IN_WINDOW = "demo.open.in.window";

    private final Preferences preferences = Preferences.userNodeForPackage(ShowcaseApp.class);
    private final List<Stage> openDemoStages = new ArrayList<>();

    private final PauseTransition manualLoadDelay = new PauseTransition(Duration.millis(250));

    private final ExecutorService manualLoaderService = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "Manual Loader Thread");
        thread.setDaemon(true);
        return thread;
    });

    private ShowcaseEntry pendingEntry;
    private long manualLoadToken;

    private TreeView<Object> treeView;
    private PDFView pdfView;
    private StackPane manualPane;
    private DialogPane dialogPane;
    private CheckBox openInWindowBox;
    private Label statusLabel;
    private Label placeholderLabel;
    private Button launchButton;
    private Button downloadButton;
    private ShowcaseThemeManager themeManager;
    private Stage showcaseStage;

    @Override
    public void start(Stage stage) {
        showcaseStage = stage;
        stage.initStyle(StageStyle.EXTENDED);

        // ── manual (right-hand side) ─────────────────────────────────────────
        pdfView = new PDFView();
        pdfView.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("pdf-view-atlanta.css")).toExternalForm());

        manualLoadDelay.setOnFinished(evt -> loadManual(pendingEntry));

        pdfView.setShowAll(preferences.getBoolean(PREF_SHOW_ALL, pdfView.isShowAll()));
        pdfView.setShowThumbnails(preferences.getBoolean(PREF_SHOW_THUMBNAILS, pdfView.isShowThumbnails()));

        pdfView.showAllProperty().addListener(it -> preferences.putBoolean(PREF_SHOW_ALL, pdfView.isShowAll()));
        pdfView.showThumbnailsProperty().addListener(it -> preferences.putBoolean(PREF_SHOW_THUMBNAILS, pdfView.isShowThumbnails()));

        placeholderLabel = new Label("Select a control to display its manual.");
        placeholderLabel.getStyleClass().add("placeholder-label");

        downloadButton = createFloatingActionButton(MaterialDesign.MDI_DOWNLOAD, "Save the manual as a PDF file.");
        downloadButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY) {
                downloadManual(getSelectedEntry());
            }
        });

        launchButton = createFloatingActionButton(MaterialDesign.MDI_PLAY, "Launch the demo application. Hold down SHIFT to open it in a separate window together with the developer tools.");
        launchButton.getStyleClass().add(Styles.ACCENT);
        launchButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            ShowcaseEntry entry = getSelectedEntry();
            if (evt.getButton() == MouseButton.PRIMARY && entry != null && entry.hasDemo()) {
                launchDemo(entry, evt.isShiftDown());
            }
        });

        VBox actionBox = new VBox(launchButton, downloadButton);
        actionBox.getStyleClass().add("action-box");
        actionBox.setAlignment(Pos.BOTTOM_RIGHT);
        // only the buttons themselves must react to the mouse, not the entire overlay
        actionBox.setPickOnBounds(false);

        manualPane = new StackPane(pdfView, placeholderLabel, actionBox);
        manualPane.getStyleClass().add("manual-pane");

        StackPane.setAlignment(actionBox, Pos.BOTTOM_RIGHT);

        // ── control list (left-hand side) ────────────────────────────────────
        treeView = new TreeView<>();
        treeView.setShowRoot(false);
        treeView.setCellFactory(view -> new ShowcaseTreeCell());
        VBox.setVgrow(treeView, Priority.ALWAYS);

        treeView.getSelectionModel().selectedItemProperty().addListener(it -> {
            ShowcaseEntry entry = getSelectedEntry();
            if (entry != null) {
                preferences.put(PREF_SELECTED_ENTRY, entry.name());
            }
            updateStatusLabel(entry);
            showManual(entry);
        });

        VBox leftSide = new VBox(treeView);
        leftSide.getStyleClass().add("left-side");
        leftSide.setFillWidth(true);

        HBox.setHgrow(manualPane, Priority.ALWAYS);

        HBox contentBox = new HBox(leftSide, manualPane);
        contentBox.getStyleClass().add("content-box");

        dialogPane = new DialogPane();
        dialogPane.setHeaderFactory(dialog -> {
            DialogHeader header = new DialogHeader(dialog);
            // the info icon of the default header makes no sense for an embedded demo
            header.setShowIcon(!dialog.getStyleClass().contains("demo-dialog"));
            return header;
        });

        // the dialogs are shown on top of the content, the header bar and the status bar are
        // covered by glass panes of their own so that the entire window gets dimmed
        StackPane centerPane = new StackPane(contentBox, dialogPane);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("showcase");
        root.setCenter(centerPane);
        root.setBottom(coverWithGlassPane(createStatusBar()));

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("showcase.css")).toExternalForm());

        themeManager = new ShowcaseThemeManager(scene, preferences);
        themeManager.darkThemeProperty().addListener(it -> Platform.runLater(() -> updateLeftSideWidth(leftSide)));

        root.setTop(coverWithGlassPane(createHeaderBar(stage)));

        updateTree();
        restoreSelection();

        stage.setTitle("GemsFX Showcase");
        stage.setScene(scene);
        stage.setOnHidden(evt -> closeAllDemos());

        StageManager.install(stage, "com/dlsc/gemsfx/showcase", 1200, 800);

        stage.show();

        updateLeftSideWidth(leftSide);
    }

    /**
     * Sets the width of the left-hand side so that the names of all controls are fully visible.
     * The width is calculated based on the font that is actually used by the tree cells, hence
     * it has to be recalculated whenever the theme (and with it possibly the font) changes.
     */
    private void updateLeftSideWidth(VBox leftSide) {
        treeView.applyCss();
        treeView.layout();

        Font font = treeView.lookupAll(".tree-cell").stream()
                .filter(Labeled.class::isInstance)
                .map(node -> ((Labeled) node).getFont())
                .findFirst()
                .orElse(Font.getDefault());

        Text text = new Text();
        text.setFont(font);

        double maxWidth = 0;
        for (ShowcaseEntry entry : ShowcaseRegistry.ALL_ENTRIES) {
            text.setText(entry.name());
            maxWidth = Math.max(maxWidth, text.getLayoutBounds().getWidth());
        }

        // add space for the tree disclosure node, the cell padding, the launch icon, and the
        // vertical scrollbar
        double width = Math.ceil(maxWidth) + 110;

        leftSide.setMinWidth(width);
        leftSide.setPrefWidth(width);
        leftSide.setMaxWidth(width);
    }

    @Override
    public void stop() {
        closeAllDemos();
    }

    private void closeAllDemos() {
        new ArrayList<>(openDemoStages).forEach(Stage::close);
        if (dialogPane != null) {
            dialogPane.hideAllDialogs();
        }
    }

    // -----------------------------------------------------------------------
    // header bar
    // -----------------------------------------------------------------------

    private HeaderBar createHeaderBar(Stage stage) {
        ImageView logoView = new ImageView(new Image(Objects.requireNonNull(ShowcaseApp.class.getResourceAsStream("gems.png"))));
        logoView.setPreserveRatio(true);
        logoView.setFitHeight(20);

        Label titleLabel = new Label("GemsFX Showcase");
        titleLabel.getStyleClass().add("title-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton themeMenuButton = createThemeMenuButton();
        Button modeButton = createModeButton();

        HBox center = new HBox(10, logoView, titleLabel, spacer, themeMenuButton, modeButton);
        center.getStyleClass().add("header-content");
        center.setAlignment(Pos.CENTER_LEFT);
        center.setPadding(new Insets(0, 6, 0, 10));

        // the header content covers the entire header bar, hence it has to be draggable, but
        // the buttons inside of it must still be clickable
        HeaderBar.setDragType(center, HeaderDragType.DRAGGABLE_SUBTREE);
        HeaderBar.setDragType(themeMenuButton, HeaderDragType.NONE);
        HeaderBar.setDragType(modeButton, HeaderDragType.NONE);

        HeaderBar headerBar = new HeaderBar();
        headerBar.getStyleClass().add("showcase-header-bar");
        headerBar.setCenter(center);

        // the window buttons are provided by AtlantaFX, hence no space has to be reserved for
        // the buttons of the operating system
        headerBar.setLeadingSystemPadding(false);
        headerBar.setTrailingSystemPadding(false);

        // AtlantaFX places the buttons on the side that matches the current operating system and
        // takes care of the interaction with the window, including the "inactive" and "maximized"
        // states. The standard group orders the buttons the way Windows does it (minimize,
        // maximize, close), hence the order has to be reversed on macOS where the close button
        // comes first.
        createHeaderButtonGroup().install(headerBar, stage);

        return headerBar;
    }

    /**
     * Creates the group of window buttons shown inside of the header bar. The buttons are
     * provided by AtlantaFX, but their order depends on the operating system.
     */
    private HeaderButtonGroup createHeaderButtonGroup() {
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            return new HeaderButtonGroup(
                    new HeaderButton(HeaderButtonType.CLOSE),
                    new HeaderButton(HeaderButtonType.ICONIFY),
                    new HeaderButton(HeaderButtonType.MAXIMIZE));
        }

        return HeaderButtonGroup.standardGroup();
    }

    private MenuButton createThemeMenuButton() {
        MenuButton menuButton = new MenuButton(themeText(themeManager.getThemeFamily()));
        menuButton.getStyleClass().addAll("theme-menu-button", Styles.FLAT, Styles.SMALL);
        menuButton.setFocusTraversable(false);
        menuButton.setMaxHeight(Region.USE_PREF_SIZE);
        menuButton.setTooltip(new Tooltip("Select the color theme"));

        ToggleGroup group = new ToggleGroup();
        for (ThemeFamily family : ThemeFamily.ALL_FAMILIES) {
            RadioMenuItem item = new RadioMenuItem(family.name());
            item.setUserData(family);
            item.setToggleGroup(group);
            item.setSelected(family.equals(themeManager.getThemeFamily()));
            menuButton.getItems().add(item);

            // separate the standard JavaFX theme from the AtlantaFX themes
            if (family.isModena()) {
                menuButton.getItems().add(new SeparatorMenuItem());
            }
        }

        group.selectedToggleProperty().addListener(it -> {
            Toggle toggle = group.getSelectedToggle();
            if (toggle != null) {
                ThemeFamily family = (ThemeFamily) toggle.getUserData();
                themeManager.setThemeFamily(family);
                menuButton.setText(themeText(family));
            }
        });

        return menuButton;
    }

    private String themeText(ThemeFamily family) {
        return "Theme: " + family.name();
    }

    /**
     * Stacks a glass pane on top of the given node. The glass pane becomes visible whenever a
     * dialog is showing inside the dialog pane. The dialog pane dims the content area all by
     * itself, but the header bar and the status bar are located outside of it and would
     * otherwise stay bright while a demo is showing.
     *
     * @param node the node to cover, e.g. the header bar or the status bar
     * @return the node wrapped inside a stack pane together with the glass pane
     */
    private StackPane coverWithGlassPane(Node node) {
        GlassPane glassPane = new GlassPane();
        glassPane.hideProperty().bind(dialogPane.showingDialogProperty().not());
        glassPane.fadeInOutProperty().bind(dialogPane.fadeInOutProperty());
        glassPane.fadeInOutDurationProperty().bind(dialogPane.getGlassPane().fadeInOutDurationProperty());

        StackPane stackPane = new StackPane(node, glassPane);
        stackPane.getStyleClass().add("glass-pane-wrapper");
        return stackPane;
    }

    /**
     * Creates the status bar shown at the bottom of the window. The status bar displays the name
     * of the currently selected control and the settings that control how the demos are being
     * launched.
     */
    private HBox createStatusBar() {
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        openInWindowBox = new CheckBox("Open demos in separate window");
        openInWindowBox.getStyleClass().add("open-in-window-box");
        openInWindowBox.setFocusTraversable(false);
        openInWindowBox.setSelected(preferences.getBoolean(PREF_OPEN_IN_WINDOW, false));
        openInWindowBox.setTooltip(new Tooltip("If not selected then the demos will be shown as an overlay inside the showcase window."));
        openInWindowBox.selectedProperty().addListener(it -> preferences.putBoolean(PREF_OPEN_IN_WINDOW, openInWindowBox.isSelected()));

        HBox statusBar = new HBox(10, statusLabel, spacer, openInWindowBox);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        return statusBar;
    }

    private void updateStatusLabel(ShowcaseEntry entry) {
        if (statusLabel == null) {
            return;
        }

        if (entry == null) {
            statusLabel.setText(ShowcaseRegistry.ALL_ENTRIES.size() + " controls");
        } else {
            statusLabel.setText(entry.category() + "  \u203a  " + entry.name());
        }
    }

    /**
     * Creates a single icon button that cycles through the available theme modes (light, dark,
     * and system).
     */
    private Button createModeButton() {
        Button button = new Button();
        button.getStyleClass().addAll("mode-button", Styles.BUTTON_ICON, Styles.FLAT);
        button.setFocusTraversable(false);
        button.setGraphic(new FontIcon());
        button.setTooltip(new Tooltip());

        Runnable updateButton = () -> {
            ThemeMode mode = themeManager.getThemeMode();

            MaterialDesign icon = switch (mode) {
                case LIGHT -> MaterialDesign.MDI_WEATHER_SUNNY;
                case DARK -> MaterialDesign.MDI_WEATHER_NIGHT;
                case SYSTEM -> MaterialDesign.MDI_DESKTOP_MAC;
            };

            ((FontIcon) button.getGraphic()).setIconCode(icon);

            if (themeManager.getThemeFamily().isModena()) {
                button.getTooltip().setText("The standard JavaFX theme only supports a light color scheme");
            } else {
                button.getTooltip().setText("Color scheme: " + mode.getDisplayName() + " (click to switch)");
            }
        };

        // the standard JavaFX theme does not come with a dark variant
        button.disableProperty().bind(Bindings.createBooleanBinding(() -> themeManager.getThemeFamily().isModena(), themeManager.themeFamilyProperty()));

        button.setOnAction(evt -> {
            ThemeMode[] modes = ThemeMode.values();
            themeManager.setThemeMode(modes[(themeManager.getThemeMode().ordinal() + 1) % modes.length]);
        });

        themeManager.themeModeProperty().addListener(it -> updateButton.run());
        themeManager.themeFamilyProperty().addListener(it -> updateButton.run());
        updateButton.run();

        return button;
    }

    // -----------------------------------------------------------------------
    // control list
    // -----------------------------------------------------------------------

    private void updateTree() {
        ShowcaseEntry selectedEntry = getSelectedEntry();

        List<ShowcaseEntry> entries = ShowcaseRegistry.ALL_ENTRIES;

        TreeItem<Object> root = new TreeItem<>();
        root.setExpanded(true);

        Map<String, List<ShowcaseEntry>> groups = ShowcaseRegistry.groupByCategory(entries);
        groups.forEach((category, categoryEntries) -> {
            TreeItem<Object> categoryItem = new TreeItem<>(category);
            categoryItem.setExpanded(true);
            categoryEntries.forEach(entry -> categoryItem.getChildren().add(new TreeItem<>(entry)));
            root.getChildren().add(categoryItem);
        });

        treeView.setRoot(root);

        if (selectedEntry != null) {
            selectEntry(selectedEntry.name());
        }
    }

    private void restoreSelection() {
        String name = preferences.get(PREF_SELECTED_ENTRY, null);
        if (name != null) {
            selectEntry(name);
        }

        if (getSelectedEntry() == null) {
            updateStatusLabel(null);
            showManual(null);
        }
    }

    private void selectEntry(String name) {
        for (TreeItem<Object> categoryItem : treeView.getRoot().getChildren()) {
            for (TreeItem<Object> item : categoryItem.getChildren()) {
                if (item.getValue() instanceof ShowcaseEntry entry && entry.name().equals(name)) {
                    treeView.getSelectionModel().select(item);
                    return;
                }
            }
        }
    }

    private ShowcaseEntry getSelectedEntry() {
        TreeItem<Object> item = treeView.getSelectionModel().getSelectedItem();
        return item != null && item.getValue() instanceof ShowcaseEntry entry ? entry : null;
    }

    private class ShowcaseTreeCell extends TreeCell<Object> {

        public ShowcaseTreeCell() {
            getStyleClass().add("showcase-tree-cell");

            addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
                if (evt.getButton() == MouseButton.PRIMARY && evt.getClickCount() == 2
                        && getItem() instanceof ShowcaseEntry entry) {
                    launchDemo(entry, evt.isShiftDown());
                }
            });
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            getStyleClass().removeAll("category-cell", "entry-cell", "no-demo-cell");

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            if (item instanceof ShowcaseEntry entry) {
                setText(entry.name());
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);

                getStyleClass().add("entry-cell");
                if (!entry.hasDemo()) {
                    getStyleClass().add("no-demo-cell");
                }
            } else {
                setGraphic(null);
                setText(String.valueOf(item));
                setContentDisplay(ContentDisplay.TEXT_ONLY);
                getStyleClass().add("category-cell");
            }
        }

    }

    // -----------------------------------------------------------------------
    // manual & demos
    // -----------------------------------------------------------------------

    /**
     * Creates a round floating action button as known from mobile applications. The button is
     * displayed on top of the manual in the lower right corner.
     */
    private Button createFloatingActionButton(MaterialDesign icon, String tooltip) {
        Button button = new Button(null, new FontIcon(icon));
        button.getStyleClass().addAll("fab", Styles.BUTTON_CIRCLE);
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(tooltip));
        button.setVisible(false);
        button.setManaged(false);
        return button;
    }

    /**
     * Saves the manual of the given entry to a location chosen by the user.
     */
    private void downloadManual(ShowcaseEntry entry) {
        if (entry == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Manual");
        fileChooser.setInitialFileName(entry.manual() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));

        File file = fileChooser.showSaveDialog(manualPane.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (InputStream stream = entry.openManual()) {
            if (stream == null) {
                showError("The manual for \"" + entry.name() + "\" can not be found.");
            } else {
                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            showError("The manual for \"" + entry.name() + "\" can not be saved.\n\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.initOwner(manualPane.getScene().getWindow());
        alert.showAndWait();
    }

    private void showManual(ShowcaseEntry entry) {
        boolean demoAvailable = entry != null && entry.hasDemo();
        launchButton.setVisible(demoAvailable);
        launchButton.setManaged(demoAvailable);

        pendingEntry = entry;

        if (entry == null) {
            // nothing to load, so there is no reason to wait
            manualLoadDelay.stop();
            loadManual(null);
            return;
        }

        /*
         * Loading a document causes the PDF view to re-render its main area and all of its
         * thumbnails. Delaying the load a little prevents this expensive work from being
         * triggered over and over again while the user is quickly walking through the tree
         * with the arrow keys.
         */
        manualLoadDelay.playFromStart();
    }

    private void loadManual(ShowcaseEntry entry) {
        final long token = ++manualLoadToken;

        if (entry == null) {
            pdfView.unload();
            setManualVisible(false, "Select a control to display its manual.");
            return;
        }

        Task<PDFView.Document> task = new Task<>() {
            @Override
            protected PDFView.Document call() throws Exception {
                try (InputStream stream = entry.openManual()) {
                    if (stream == null) {
                        return null;
                    }
                    return new PDFBoxDocument(stream);
                }
            }
        };

        /*
         * Parsing a PDF file takes a moment. Doing it in the background keeps the UI responsive
         * and the currently shown manual stays visible until the new one is ready.
         */
        task.setOnSucceeded(evt -> {
            if (token != manualLoadToken) {
                // a newer document has been requested in the meantime
                return;
            }

            PDFView.Document document = task.getValue();
            if (document == null) {
                pdfView.unload();
                setManualVisible(false, "No manual available for \"" + entry.name() + "\".");
            } else {
                pdfView.setDocument(document);
                setManualVisible(true, null);
            }
        });

        task.setOnFailed(evt -> {
            if (token != manualLoadToken) {
                return;
            }
            pdfView.unload();
            setManualVisible(false, "The manual for \"" + entry.name() + "\" can not be displayed.");
        });

        manualLoaderService.execute(task);
    }

    private void setManualVisible(boolean visible, String placeholderText) {
        pdfView.setVisible(visible);
        placeholderLabel.setVisible(!visible);
        downloadButton.setVisible(visible);
        downloadButton.setManaged(visible);
        if (placeholderText != null) {
            placeholderLabel.setText(placeholderText);
        }
    }

    /**
     * Launches the demo of the given entry. The demo is shown as an overlay inside the showcase
     * window unless the user asked for a separate window, either via the toggle button in the
     * header bar or by holding down the SHIFT key. Only a demo running inside its own window can
     * be inspected with the developer tools.
     *
     * @param entry           the entry whose demo shall be launched
     * @param separateWindow  if true the demo will be shown in a window of its own
     */
    private void launchDemo(ShowcaseEntry entry, boolean separateWindow) {
        if (!entry.hasDemo()) {
            return;
        }

        if (separateWindow || openInWindowBox.isSelected()) {
            launchDemoStage(entry, separateWindow);
        } else {
            showDemoOverlay(entry);
        }
    }

    /**
     * Shows the demo of the given entry as an overlay dialog inside the showcase window.
     */
    private void showDemoOverlay(ShowcaseEntry entry) {
        EmbeddedDemo demo;

        try {
            demo = DemoEmbedder.embed(entry.demoFactory().get());
        } catch (Exception ex) {
            dialogPane.showError("Launch Error", "The demo for \"" + entry.name()
                    + "\" can not be shown inside the showcase window. It will be opened in a separate window instead.", ex);
            launchDemoStage(entry, false);
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>(dialogPane, DialogPane.Type.INFORMATION);
        dialog.getStyleClass().add("demo-dialog");
        dialog.setTitle(entry.name());
        dialog.setContent(demo.content());
        dialog.getButtonTypes().setAll(ButtonType.CLOSE);
        dialog.setMaximize(false);
        dialog.setResizable(true);
        dialog.setPrefWidth(constrain(demo.prefWidth(), dialogPane.getWidth()));
        dialog.setPrefHeight(constrain(demo.prefHeight(), dialogPane.getHeight()));

        // remembers the size of the dialog after the user has resized it
        dialog.setId("demo." + entry.manual());
        dialog.setPreferences(preferences.node(dialog.getId()));

        dialogPane.showDialog(dialog);
    }

    /**
     * Ensures that a demo dialog never becomes larger than the space available inside the
     * dialog pane.
     *
     * @param size      the preferred size of the demo
     * @param available the size of the dialog pane, might be zero if the pane has not been laid out yet
     */
    private double constrain(double size, double available) {
        if (available <= 0) {
            return size;
        }

        // leave room for the padding of the dialog pane and for the header and footer of the dialog
        double max = available - 2 * dialogPane.getMaximizedPadding() - 100;
        return max <= 0 ? size : Math.min(size, max);
    }

    /**
     * Launches the demo of the given entry inside a window of its own.
     */
    private void launchDemoStage(ShowcaseEntry entry, boolean withDevTools) {
        try {
            Application app = entry.demoFactory().get();

            Stage demoStage = new Stage();
            demoStage.setTitle(entry.name());
            app.start(demoStage);

            moveToShowcaseScreen(demoStage);

            openDemoStages.add(demoStage);
            demoStage.setOnHidden(evt -> openDemoStages.remove(demoStage));

            if (withDevTools && demoStage.getScene() != null) {
                GUI.openToolStage(demoStage, getHostServices());
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to launch the demo for \"" + entry.name() + "\"");
            alert.setContentText(ex.getMessage());
            alert.show();
        }
    }

    /**
     * Centers the given demo window on the screen that is currently showing the showcase
     * window. The demo applications are not aware of the showcase, hence they end up on the
     * primary screen, which might not be the screen that the user is working on. Demos that
     * place themselves on the screen of the showcase are left alone.
     */
    private void moveToShowcaseScreen(Stage demoStage) {
        double width = demoStage.getWidth();
        double height = demoStage.getHeight();

        if (Double.isNaN(width) || Double.isNaN(height) || width <= 0 || height <= 0) {
            return;
        }

        Screen showcaseScreen = screenOf(showcaseStage);
        if (showcaseScreen.equals(screenOf(demoStage))) {
            return;
        }

        Rectangle2D bounds = showcaseScreen.getVisualBounds();
        demoStage.setX(bounds.getMinX() + (bounds.getWidth() - Math.min(width, bounds.getWidth())) / 2);
        demoStage.setY(bounds.getMinY() + (bounds.getHeight() - Math.min(height, bounds.getHeight())) / 2);
    }

    /**
     * Returns the screen that shows the biggest part of the given window, the primary screen if
     * the window is not located on any of the currently attached screens.
     */
    private Screen screenOf(Stage stage) {
        if (stage == null || Double.isNaN(stage.getX()) || Double.isNaN(stage.getY())
                || Double.isNaN(stage.getWidth()) || Double.isNaN(stage.getHeight())) {
            return Screen.getPrimary();
        }

        return Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight())
                .stream()
                .max(Comparator.comparingDouble(screen -> overlap(screen.getBounds(), stage)))
                .orElseGet(Screen::getPrimary);
    }

    /**
     * Returns the size of the area in which the given window and the given screen overlap.
     */
    private double overlap(Rectangle2D screenBounds, Stage stage) {
        double width = Math.max(0, Math.min(screenBounds.getMaxX(), stage.getX() + stage.getWidth()) - Math.max(screenBounds.getMinX(), stage.getX()));
        double height = Math.max(0, Math.min(screenBounds.getMaxY(), stage.getY() + stage.getHeight()) - Math.max(screenBounds.getMinY(), stage.getY()));
        return width * height;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
