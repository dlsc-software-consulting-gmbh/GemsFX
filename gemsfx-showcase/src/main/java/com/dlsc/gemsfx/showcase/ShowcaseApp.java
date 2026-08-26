package com.dlsc.gemsfx.showcase;

import atlantafx.base.theme.Styles;
import atlantafx.decorations.HeaderButton;
import atlantafx.decorations.HeaderButtonGroup;
import com.dlsc.gemsfx.DialogPane;
import com.dlsc.gemsfx.DialogPane.Dialog;
import com.dlsc.gemsfx.DialogPane.DialogHeader;
import com.dlsc.gemsfx.DrawerStackPane;
import com.dlsc.gemsfx.GlassPane;
import com.dlsc.gemsfx.showcase.DemoEmbedder.EmbeddedDemo;
import com.dlsc.gemsfx.util.StageManager;
import com.dlsc.pdfviewfx.PDFBoxDocument;
import com.dlsc.pdfviewfx.PDFView;
import devtoolsfx.gui.GUI;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.NodeOrientation;
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
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Labeled;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
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
import one.jpro.platform.mdfx.MarkdownView;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.io.File;
import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    private static final Logger LOG = System.getLogger(ShowcaseApp.class.getName());

    /**
     * The font used by IntelliJ IDEA for source code. The font is bundled with the showcase
     * because it can not be expected to be installed on the machine of the user.
     */
    private static final List<String> SOURCE_FONTS = List.of(
            "fonts/JetBrainsMono-Regular.ttf",
            "fonts/JetBrainsMono-Bold.ttf",
            "fonts/JetBrainsMono-Italic.ttf",
            "fonts/JetBrainsMono-BoldItalic.ttf");

    private static final String PREF_SELECTED_ENTRY = "selected.entry";
    private static final String PREF_SHOW_ALL = "pdf.show.all";
    private static final String PREF_SHOW_THUMBNAILS = "pdf.show.thumbnails";
    private static final String PREF_OPEN_IN_WINDOW = "demo.open.in.window";
    private static final String PREF_SHOW_HINT = "hint.show";
    private static final String PREF_SHOW_WELCOME = "welcome.show";

    /**
     * The GitHub repository of the additional AtlantaFX themes used by the showcase.
     */
    private static final String THEMES_URL = "https://github.com/dlsc-software-consulting-gmbh/atlantafx-themes";

    /**
     * The GitHub repository of GemsFX itself.
     */
    private static final String REPOSITORY_URL = "https://github.com/dlsc-software-consulting-gmbh/GemsFX";

    private final Preferences preferences = Preferences.userNodeForPackage(ShowcaseApp.class);
    private final List<Stage> openDemoStages = new ArrayList<>();

    private final PauseTransition manualLoadDelay = new PauseTransition(Duration.millis(250));

    /** The source code that is currently shown inside of the drawer. */
    private final StringProperty currentSource = new SimpleStringProperty(this, "currentSource");

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
    private Button sourceButton;
    private DrawerStackPane sourceDrawer;
    private MarkdownView sourceView;
    private ScrollPane sourcePane;
    private ShowcaseThemeManager themeManager;
    private Stage showcaseStage;
    private StackPane sceneRoot;
    private WelcomePane welcomePane;

    /** Whether the welcome page currently covers the main user interface. */
    private final BooleanProperty welcomeShowing = new SimpleBooleanProperty(this, "welcomeShowing");

    @Override
    public void start(Stage stage) {
        showcaseStage = stage;
        stage.initStyle(StageStyle.EXTENDED);

        loadSourceFonts();

        // ── manual (right-hand side) ─────────────────────────────────────────
        pdfView = new PDFView();
        pdfView.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("pdf-view-atlanta.css")).toExternalForm());

        manualLoadDelay.setOnFinished(evt -> loadManual(pendingEntry));

        pdfView.setShowAll(preferences.getBoolean(PREF_SHOW_ALL, pdfView.isShowAll()));
        pdfView.setShowThumbnails(preferences.getBoolean(PREF_SHOW_THUMBNAILS, pdfView.isShowThumbnails()));

        pdfView.showAllProperty().addListener(it -> preferences.putBoolean(PREF_SHOW_ALL, pdfView.isShowAll()));
        pdfView.showThumbnailsProperty().addListener(it -> preferences.putBoolean(PREF_SHOW_THUMBNAILS, pdfView.isShowThumbnails()));

        placeholderLabel = new Label(ShowcaseBundle.get("manual.placeholder"));
        placeholderLabel.getStyleClass().add("placeholder-label");

        downloadButton = createFloatingActionButton(MaterialDesign.MDI_DOWNLOAD, ShowcaseBundle.get("fab.download.tooltip"));
        downloadButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY) {
                downloadManual(getSelectedEntry());
            }
        });

        launchButton = createFloatingActionButton(MaterialDesign.MDI_PLAY, ShowcaseBundle.get("fab.launch.tooltip"));
        launchButton.getStyleClass().add(Styles.ACCENT);
        launchButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            ShowcaseEntry entry = getSelectedEntry();
            if (evt.getButton() == MouseButton.PRIMARY && entry != null && entry.hasDemo()) {
                launchDemo(entry, evt.isShiftDown());
            }
        });
        // the button is also the default button of the window, hence pressing ENTER launches the
        // demo of the selected control; the default button fires an action event and not a mouse
        // event, so there is no SHIFT modifier to evaluate here - only the "open in window"
        // setting of the status bar decides where the demo is shown
        launchButton.setOnAction(evt -> {
            ShowcaseEntry entry = getSelectedEntry();
            if (entry != null && entry.hasDemo()) {
                launchDemo(entry, false);
            }
        });

        sourceButton = createFloatingActionButton(MaterialDesign.MDI_CODE_BRACES, ShowcaseBundle.get("fab.source.tooltip"));
        sourceButton.addEventHandler(MouseEvent.MOUSE_CLICKED, evt -> {
            if (evt.getButton() == MouseButton.PRIMARY) {
                showSourceCode(getSelectedEntry());
            }
        });

        VBox actionBox = new VBox(launchButton, downloadButton, sourceButton);
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
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // the banner spans the entire width of the window, above the list of controls and the manual
        VBox mainBox = new VBox(createHintBanner(), contentBox);
        mainBox.getStyleClass().add("main-box");
        mainBox.setFillWidth(true);

        dialogPane = new DialogPane();
        dialogPane.setHeaderFactory(dialog -> {
            DialogHeader header = new DialogHeader(dialog);
            // the info icon of the default header makes no sense for an embedded demo
            header.setShowIcon(!dialog.getStyleClass().contains("demo-dialog"));
            return header;
        });

        // the drawer slides in over the entire content area and shows the source code of the
        // currently selected demo application
        sourceDrawer = createSourceDrawer(mainBox);

        /*
         * ENTER launches the demo of the selected control, but only while the manual is really
         * the front-most part of the user interface. A dialog (which may be a running demo) and
         * the source code drawer bring default buttons and key handlers of their own, and so
         * does the welcome page, hence the launch button must step back while one of them is
         * showing.
         */
        launchButton.defaultButtonProperty().bind(launchButton.visibleProperty()
                .and(welcomeShowing.not())
                .and(dialogPane.showingDialogProperty().not())
                .and(sourceDrawer.showDrawerProperty().not()));

        // the dialogs are shown on top of the content, the header bar and the status bar are
        // covered by glass panes of their own so that the entire window gets dimmed
        StackPane centerPane = new StackPane(sourceDrawer, dialogPane);

        BorderPane root = new BorderPane();
        root.setCenter(centerPane);
        root.setBottom(coverWithGlassPane(createStatusBar()));

        // the welcome page is stacked on top of the main user interface and covers the entire
        // window; the "showcase" style class has to sit on the scene root because the theme
        // manager adds the theme-related style classes ("modena-active", "dark-theme", ...)
        // to the scene root as well
        sceneRoot = new StackPane(root);
        sceneRoot.getStyleClass().add("showcase");

        Scene scene = new Scene(sceneRoot, 1400, 900);
        scene.getStylesheets().add(Objects.requireNonNull(ShowcaseApp.class.getResource("showcase.css")).toExternalForm());
        scene.setNodeOrientation(nodeOrientation());

        themeManager = new ShowcaseThemeManager(scene, preferences);
        themeManager.darkThemeProperty().addListener(it -> Platform.runLater(() -> updateLeftSideWidth(leftSide)));

        root.setTop(coverWithGlassPane(createHeaderBar(stage)));

        updateTree();
        restoreSelection();

        stage.setTitle(ShowcaseBundle.get("app.title"));
        stage.setScene(scene);
        stage.setOnHidden(evt -> closeAllDemos());

        StageManager.install(stage, "com/dlsc/gemsfx/showcase", 1200, 800);

        if (preferences.getBoolean(PREF_SHOW_WELCOME, true)) {
            showWelcomePane(false);
        }

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

        Label titleLabel = new Label(ShowcaseBundle.get("app.title"));
        titleLabel.getStyleClass().add("title-label");

        Hyperlink repositoryLink = new Hyperlink(ShowcaseBundle.get("header.repository"));
        repositoryLink.setGraphic(new FontIcon(MaterialDesign.MDI_GITHUB_CIRCLE));
        repositoryLink.getStyleClass().add("repository-link");
        repositoryLink.setMinWidth(Region.USE_PREF_SIZE);
        repositoryLink.setFocusTraversable(false);
        repositoryLink.setTooltip(new Tooltip(ShowcaseBundle.get("header.repository.tooltip")));
        repositoryLink.setOnAction(evt -> getHostServices().showDocument(REPOSITORY_URL));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        MenuButton themeMenuButton = createThemeMenuButton();
        Button modeButton = createModeButton();
        Button welcomeButton = createWelcomeButton();

        HBox center = new HBox(10, logoView, titleLabel, repositoryLink, spacer, welcomeButton, themeMenuButton, modeButton);
        center.getStyleClass().add("header-content");
        center.setAlignment(Pos.CENTER_LEFT);
        center.setPadding(new Insets(0, 6, 0, 10));

        // the header content covers the entire header bar, hence it has to be draggable, but
        // the buttons inside of it must still be clickable
        HeaderBar.setDragType(center, HeaderDragType.DRAGGABLE_SUBTREE);
        HeaderBar.setDragType(themeMenuButton, HeaderDragType.NONE);
        HeaderBar.setDragType(modeButton, HeaderDragType.NONE);
        HeaderBar.setDragType(welcomeButton, HeaderDragType.NONE);
        HeaderBar.setDragType(repositoryLink, HeaderDragType.NONE);

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
    static HeaderButtonGroup createHeaderButtonGroup() {
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            return new HeaderButtonGroup(
                    new HeaderButton(HeaderButtonType.CLOSE),
                    new HeaderButton(HeaderButtonType.ICONIFY),
                    new HeaderButton(HeaderButtonType.MAXIMIZE));
        }

        return HeaderButtonGroup.standardGroup();
    }

    /**
     * Creates the header bar button that brings the welcome page back.
     */
    private Button createWelcomeButton() {
        Button button = new Button(null, new FontIcon(MaterialDesign.MDI_DIAMOND));
        button.getStyleClass().addAll("welcome-button", Styles.BUTTON_ICON, Styles.FLAT);
        button.setFocusTraversable(false);
        button.setTooltip(new Tooltip(ShowcaseBundle.get("header.welcome.tooltip")));
        button.setOnAction(evt -> showWelcomePane(true));
        return button;
    }

    // -----------------------------------------------------------------------
    // welcome page
    // -----------------------------------------------------------------------

    /**
     * Shows the welcome page on top of the main user interface and starts the rain of user
     * interface graphics in its background.
     *
     * @param animated whether the page fades in (when brought back via the header bar button)
     *                 or appears immediately (at startup)
     */
    private void showWelcomePane(boolean animated) {
        if (welcomePane == null) {
            welcomePane = new WelcomePane(showcaseStage);
            welcomePane.setOnExplore(this::hideWelcomePane);
            welcomePane.doNotShowAgainProperty().addListener(it -> preferences.putBoolean(PREF_SHOW_WELCOME, !welcomePane.isDoNotShowAgain()));
        }

        welcomePane.setDoNotShowAgain(!preferences.getBoolean(PREF_SHOW_WELCOME, true));

        if (!sceneRoot.getChildren().contains(welcomePane)) {
            sceneRoot.getChildren().add(welcomePane);
        }

        welcomePane.setScaleX(1);
        welcomePane.setScaleY(1);
        welcomePane.setTranslateY(0);
        welcomePane.play();

        welcomeShowing.set(true);

        if (animated) {
            welcomePane.setOpacity(0);
            FadeTransition fade = new FadeTransition(Duration.millis(300), welcomePane);
            fade.setToValue(1);
            fade.play();
        } else {
            welcomePane.setOpacity(1);
        }
    }

    /**
     * Reveals the main user interface: the welcome page fades out while slightly scaling up
     * and sliding towards the top, then it gets removed from the scene.
     */
    private void hideWelcomePane() {
        Duration duration = Duration.millis(450);

        FadeTransition fade = new FadeTransition(duration, welcomePane);
        fade.setToValue(0);

        ScaleTransition scale = new ScaleTransition(duration, welcomePane);
        scale.setToX(1.05);
        scale.setToY(1.05);

        TranslateTransition slide = new TranslateTransition(duration, welcomePane);
        slide.setToY(-40);
        slide.setInterpolator(Interpolator.EASE_IN);

        ParallelTransition exit = new ParallelTransition(fade, scale, slide);
        exit.setOnFinished(evt -> {
            welcomePane.stopAnimation();
            sceneRoot.getChildren().remove(welcomePane);
            welcomeShowing.set(false);
        });
        exit.play();
    }

    private MenuButton createThemeMenuButton() {
        MenuButton menuButton = new MenuButton(themeText(themeManager.getThemeFamily()));
        menuButton.getStyleClass().addAll("theme-menu-button", Styles.FLAT, Styles.SMALL);
        menuButton.setFocusTraversable(false);
        menuButton.setMaxHeight(Region.USE_PREF_SIZE);
        menuButton.setTooltip(new Tooltip(ShowcaseBundle.get("header.theme.tooltip")));

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
        return ShowcaseBundle.format("header.theme.text", family.name());
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
     * of the currently selected control, a link to the themes used by the showcase, and the
     * settings that control how the demos are being launched.
     */
    private HBox createStatusBar() {
        statusLabel = new Label();
        statusLabel.getStyleClass().add("status-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Hyperlink themesLink = new Hyperlink(ShowcaseBundle.get("status.themes"));
        themesLink.getStyleClass().add("themes-link");
        themesLink.setMinWidth(Region.USE_PREF_SIZE);
        themesLink.setFocusTraversable(false);
        themesLink.setTooltip(new Tooltip(ShowcaseBundle.get("status.themes.tooltip")));
        themesLink.setOnAction(evt -> getHostServices().showDocument(THEMES_URL));

        openInWindowBox = new CheckBox(ShowcaseBundle.get("status.openInWindow"));
        openInWindowBox.setMinWidth(Region.USE_PREF_SIZE);
        openInWindowBox.getStyleClass().add("open-in-window-box");
        openInWindowBox.setFocusTraversable(false);
        openInWindowBox.setSelected(preferences.getBoolean(PREF_OPEN_IN_WINDOW, false));
        openInWindowBox.setTooltip(new Tooltip(ShowcaseBundle.get("status.openInWindow.tooltip")));
        openInWindowBox.selectedProperty().addListener(it -> preferences.putBoolean(PREF_OPEN_IN_WINDOW, openInWindowBox.isSelected()));

        HBox statusBar = new HBox(10, statusLabel, spacer, themesLink, openInWindowBox);
        statusBar.getStyleClass().add("status-bar");
        statusBar.setAlignment(Pos.CENTER_LEFT);

        return statusBar;
    }

    private void updateStatusLabel(ShowcaseEntry entry) {
        if (statusLabel == null) {
            return;
        }

        if (entry == null) {
            statusLabel.setText(ShowcaseBundle.format("status.controls", ShowcaseRegistry.ALL_ENTRIES.size()));
        } else {
            statusLabel.setText(entry.category() + "  \u203a  " + entry.name());
        }
    }

    /**
     * Creates a single icon button that cycles through the available theme modes (light, dark,
     * and system). The button is only visible when the currently selected theme family comes
     * with a light and a dark variant.
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
            button.getTooltip().setText(ShowcaseBundle.format("header.mode.tooltip", mode.getDisplayName()));
        };

        // theme families with only a single variant do not offer a choice
        button.visibleProperty().bind(Bindings.createBooleanBinding(() -> themeManager.getThemeFamily().hasBothVariants(), themeManager.themeFamilyProperty()));
        button.managedProperty().bind(button.visibleProperty());

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

    /**
     * Creates the banner shown above the list of controls and the manual. The banner explains
     * how the demo applications can be launched. The user can either close it for the current
     * session or turn it off permanently.
     */
    private Node createHintBanner() {
        FontIcon icon = new FontIcon(MaterialDesign.MDI_INFORMATION_OUTLINE);
        icon.getStyleClass().add("hint-icon");

        Label textLabel = new Label(ShowcaseBundle.get("hint.text"));
        textLabel.getStyleClass().add("hint-label");
        textLabel.setWrapText(true);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox banner = new HBox();
        banner.getStyleClass().add("hint-banner");
        banner.setAlignment(Pos.CENTER_LEFT);

        Hyperlink doNotShowLink = new Hyperlink(ShowcaseBundle.get("hint.doNotShow"));
        doNotShowLink.getStyleClass().add("hint-link");
        doNotShowLink.setFocusTraversable(false);
        doNotShowLink.setOnAction(evt -> hideHintBanner(banner, true));

        Button closeButton = new Button(null, new FontIcon(MaterialDesign.MDI_CLOSE));
        closeButton.getStyleClass().addAll("hint-close-button", Styles.BUTTON_ICON, Styles.FLAT);
        closeButton.setFocusTraversable(false);
        closeButton.setTooltip(new Tooltip(ShowcaseBundle.get("hint.close.tooltip")));
        closeButton.setOnAction(evt -> hideHintBanner(banner, false));

        banner.getChildren().addAll(icon, textLabel, spacer, doNotShowLink, closeButton);

        boolean showHint = preferences.getBoolean(PREF_SHOW_HINT, true);
        banner.setVisible(showHint);
        banner.setManaged(showHint);

        return banner;
    }

    private void hideHintBanner(Node banner, boolean permanently) {
        banner.setVisible(false);
        banner.setManaged(false);

        if (permanently) {
            preferences.putBoolean(PREF_SHOW_HINT, false);
        }
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
     * Returns the orientation that matches the language of the user interface. Languages such
     * as Arabic are written from right to left, in which case the entire user interface has to
     * be mirrored.
     */
    private NodeOrientation nodeOrientation() {
        String language = ShowcaseBundle.getLocale().getLanguage();
        boolean rightToLeft = List.of("ar", "fa", "he", "iw", "ur").contains(language);
        return rightToLeft ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT;
    }

    /**
     * Loads the font that is used for displaying source code. The font is shipped with the
     * showcase, hence it is available no matter which fonts are installed on the machine of
     * the user.
     */
    private void loadSourceFonts() {
        for (String font : SOURCE_FONTS) {
            try (InputStream stream = ShowcaseApp.class.getResourceAsStream(font)) {
                if (stream == null || Font.loadFont(stream, 12) == null) {
                    LOG.log(Level.WARNING, () -> "the font \"" + font + "\" can not be loaded");
                }
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "the font \"" + font + "\" can not be loaded", ex);
            }
        }
    }

    /**
     * Creates the drawer that slides in from the bottom to show the source code of the demo
     * application of the currently selected control. The given node becomes the content over
     * which the drawer is displayed.
     */
    private DrawerStackPane createSourceDrawer(Node content) {
        sourceView = new MarkdownView() {
            @Override
            public Optional<String> getDefaultLanguage() {
                return Optional.of("java");
            }
        };
        sourceView.getStyleClass().add("source-view");

        sourcePane = new ScrollPane(sourceView);
        sourcePane.getStyleClass().add("source-scroll-pane");
        sourcePane.setFitToWidth(true);

        DrawerStackPane drawer = new DrawerStackPane(content);
        drawer.setAnimateDrawer(true);
        drawer.setShowDrawerTitle(true);
        drawer.setPreferredDrawerWidth(Double.MAX_VALUE);
        drawer.setDrawerContent(sourcePane);

        Button copyButton = new Button(ShowcaseBundle.get("source.copy"));
        copyButton.setFocusTraversable(false);
        copyButton.setTooltip(new Tooltip(ShowcaseBundle.get("source.copy.tooltip")));
        copyButton.disableProperty().bind(currentSource.isNull());

        // the label of the button confirms the copy operation for a moment
        PauseTransition copyFeedbackDelay = new PauseTransition(Duration.seconds(2));
        copyFeedbackDelay.setOnFinished(evt -> copyButton.setText(ShowcaseBundle.get("source.copy")));

        copyButton.setOnAction(evt -> copySourceCode(copyButton, copyFeedbackDelay));

        Button closeButton = new Button(ShowcaseBundle.get("source.close"));
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(evt -> drawer.setShowDrawer(false));

        drawer.getToolbarItems().addAll(copyButton, closeButton);

        return drawer;
    }

    /**
     * Copies the source code that is currently shown inside the drawer to the system clipboard.
     */
    private void copySourceCode(Button copyButton, PauseTransition feedbackDelay) {
        String source = currentSource.get();
        if (source == null) {
            return;
        }

        ClipboardContent content = new ClipboardContent();
        content.putString(source);
        Clipboard.getSystemClipboard().setContent(content);

        copyButton.setText(ShowcaseBundle.get("source.copied"));
        feedbackDelay.playFromStart();
    }

    /**
     * Shows the source code of the demo application of the given entry inside the drawer. The
     * source code is loaded from the classpath, the demo module adds its Java files to its own
     * build output for exactly this purpose.
     */
    private void showSourceCode(ShowcaseEntry entry) {
        if (entry == null || !entry.hasDemo()) {
            return;
        }

        String source = entry.loadDemoSource();
        currentSource.set(source);

        sourceDrawer.setDrawerTitle(ShowcaseBundle.format("source.title", entry.name()));

        // MDFX applies syntax highlighting to fenced code blocks
        sourceView.setMdString(source != null
                ? "```java\n" + source + "\n```"
                : ShowcaseBundle.get("source.notFound"));

        sourcePane.setVvalue(0);
        sourceDrawer.setShowDrawer(true);
    }

    /**
     * Saves the manual of the given entry to a location chosen by the user.
     */
    private void downloadManual(ShowcaseEntry entry) {
        if (entry == null) {
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(ShowcaseBundle.get("manual.save.title"));
        fileChooser.setInitialFileName(entry.manual() + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(ShowcaseBundle.get("manual.save.filter"), "*.pdf"));

        File file = fileChooser.showSaveDialog(manualPane.getScene().getWindow());
        if (file == null) {
            return;
        }

        try (InputStream stream = entry.openManual()) {
            if (stream == null) {
                showError(ShowcaseBundle.format("manual.save.notFound", entry.name()));
            } else {
                Files.copy(stream, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception ex) {
            showError(ShowcaseBundle.format("manual.save.error", entry.name(), ex.getMessage()));
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
        sourceButton.setVisible(demoAvailable);
        sourceButton.setManaged(demoAvailable);

        // the drawer shows the source code of the previously selected demo
        sourceDrawer.setShowDrawer(false);

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
            setManualVisible(false, ShowcaseBundle.get("manual.placeholder"));
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
                setManualVisible(false, ShowcaseBundle.format("manual.none", entry.name()));
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
            setManualVisible(false, ShowcaseBundle.format("manual.error", entry.name()));
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
            demo = DemoEmbedder.embed(entry.createDemo());
        } catch (Exception ex) {
            dialogPane.showError(ShowcaseBundle.get("demo.error.title"),
                    ShowcaseBundle.format("demo.error.embedded", entry.name()), ex);
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
            Application app = entry.createDemo();

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
            alert.setTitle(ShowcaseBundle.get("demo.error.title"));
            alert.setHeaderText(ShowcaseBundle.format("demo.error.header", entry.name()));
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
