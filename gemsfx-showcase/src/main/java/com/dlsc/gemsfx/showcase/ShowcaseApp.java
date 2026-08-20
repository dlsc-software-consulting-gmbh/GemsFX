package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.Styles;
import atlantafx.decorations.HeaderButtonGroup;
import com.dlsc.gemsfx.util.StageManager;
import com.dlsc.pdfviewfx.PDFView;
import devtoolsfx.gui.GUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
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
import javafx.scene.layout.HeaderDragType;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * The GemsFX showcase application. The application lists all controls of the GemsFX library,
 * grouped by category. Selecting a control displays the PDF
 * manual of that control. Double-clicking a control additionally launches the demo application
 * of that control, holding down the shift key while double-clicking also opens the developer
 * tools for the launched demo.
 */
public class ShowcaseApp extends Application {

    private static final String PREF_SELECTED_ENTRY = "selected.entry";
    private static final String PREF_SHOW_ALL = "pdf.show.all";
    private static final String PREF_SHOW_THUMBNAILS = "pdf.show.thumbnails";

    private final Preferences preferences = Preferences.userNodeForPackage(ShowcaseApp.class);
    private final List<Stage> openDemoStages = new ArrayList<>();

    private TreeView<Object> treeView;
    private PDFView pdfView;
    private StackPane manualPane;
    private Label placeholderLabel;
    private Button launchButton;
    private Button downloadButton;
    private ShowcaseThemeManager themeManager;

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.EXTENDED);

        // ── manual (right-hand side) ─────────────────────────────────────────
        pdfView = new PDFView();
        pdfView.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("pdf-view-atlanta.css")).toExternalForm());

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

        launchButton = createFloatingActionButton(MaterialDesign.MDI_PLAY, "Launch the demo application. Hold down SHIFT to also open the developer tools.");
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
            showManual(entry);
        });

        VBox leftSide = new VBox(treeView);
        leftSide.getStyleClass().add("left-side");
        leftSide.setFillWidth(true);

        HBox.setHgrow(manualPane, Priority.ALWAYS);

        HBox contentBox = new HBox(leftSide, manualPane);
        contentBox.getStyleClass().add("content-box");

        BorderPane root = new BorderPane();
        root.getStyleClass().add("showcase");
        root.setCenter(contentBox);

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("showcase.css")).toExternalForm());

        themeManager = new ShowcaseThemeManager(scene, preferences);
        themeManager.darkThemeProperty().addListener(it -> Platform.runLater(() -> updateLeftSideWidth(leftSide)));

        root.setTop(createHeaderBar(stage));

        updateTree();
        restoreSelection();

        stage.setTitle("GemsFX Showcase");
        stage.setScene(scene);
        stage.setOnHidden(evt -> new ArrayList<>(openDemoStages).forEach(Stage::close));

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
        new ArrayList<>(openDemoStages).forEach(Stage::close);
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
        // states
        HeaderButtonGroup.standardGroup().install(headerBar, stage);

        return headerBar;
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
            button.getTooltip().setText("Color scheme: " + mode.getDisplayName() + " (click to switch)");
        };

        button.setOnAction(evt -> {
            ThemeMode[] modes = ThemeMode.values();
            themeManager.setThemeMode(modes[(themeManager.getThemeMode().ordinal() + 1) % modes.length]);
        });

        themeManager.themeModeProperty().addListener(it -> updateButton.run());
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

        if (entry == null) {
            pdfView.unload();
            setManualVisible(false, "Select a control to display its manual.");
            return;
        }

        try (InputStream stream = entry.openManual()) {
            if (stream == null) {
                pdfView.unload();
                setManualVisible(false, "No manual available for \"" + entry.name() + "\".");
            } else {
                pdfView.load(stream);
                setManualVisible(true, null);
            }
        } catch (Exception ex) {
            pdfView.unload();
            setManualVisible(false, "The manual for \"" + entry.name() + "\" can not be displayed.");
        }
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

    private void launchDemo(ShowcaseEntry entry, boolean withDevTools) {
        if (!entry.hasDemo()) {
            return;
        }

        try {
            Application app = entry.demoFactory().get();

            Stage demoStage = new Stage();
            demoStage.setTitle(entry.name());
            app.start(demoStage);

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

    public static void main(String[] args) {
        launch(args);
    }
}
