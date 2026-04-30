package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Styles;
import atlantafx.base.theme.Theme;
import com.dlsc.gemsfx.GlassPane;
import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.demo.binding.AggregatedListBindingApp;
import com.dlsc.gemsfx.demo.binding.NestedListBindingApp;
import com.dlsc.gemsfx.demo.binding.NestedListChangeTrackerApp;
import com.dlsc.gemsfx.demo.binding.ObservableListBindingApp;
import com.jpro.webapi.WebAPI;
import devtoolsfx.gui.GUI;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import one.jpro.platform.mdfx.MarkdownView;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.prefs.Preferences;

/**
 * A single-window launcher that lists every demo application in the GemsFX
 * demo module.  Double-click (or select + Enter / "Launch" button) to open
 * a demo in its own {@link Stage}.
 */
public class GemsFXDemoLauncher extends GemApplication {

    private CheckBox developerToolCheckBox;
    private GlassPane browserGlassPane;

    // -----------------------------------------------------------------------
    // Demo registry
    // -----------------------------------------------------------------------

    public record DemoEntry(String category, String name, Supplier<Application> factory, boolean desktopOnly) {
    }

    private final List<Stage> openDemoStages = new ArrayList<>();
    private Stage launcherStage;

    /**
     * All demos, in category / alphabetical order.
     */
    static final List<DemoEntry> ALL_DEMOS = List.of(

            // --- Bindings -------------------------------------------------------
            demo("Bindings", "Aggregated List Binding", AggregatedListBindingApp::new),
            demo("Bindings", "Nested List Binding", NestedListBindingApp::new),
            demo("Bindings", "Nested List Change Tracker", NestedListChangeTrackerApp::new),
            demo("Bindings", "Observable List Binding", ObservableListBindingApp::new),

            // --- Calendar & Date / Time -----------------------------------------
            demo("Calendar & Date / Time", "Calendar Picker", CalendarPickerApp::new),
            demo("Calendar & Date / Time", "Calendar View", CalendarViewApp::new),
            demo("Calendar & Date / Time", "Date Range Picker", DateRangePickerApp::new),
            demo("Calendar & Date / Time", "Date Range View", DateRangeViewApp::new),
            demo("Calendar & Date / Time", "Day Of Week Picker", DayOfWeekPickerApp::new),
            demo("Calendar & Date / Time", "Duration Picker", DurationPickerApp::new),
            demo("Calendar & Date / Time", "Time Picker", TimePickerApp::new),
            demo("Calendar & Date / Time", "Time Range Picker", TimeRangePickerApp::new),
            demo("Calendar & Date / Time", "Year Month Picker", YearMonthPickerApp::new),
            demo("Calendar & Date / Time", "Year Month View", YearMonthViewApp::new),
            demo("Calendar & Date / Time", "Year Picker", YearPickerApp::new),
            demo("Calendar & Date / Time", "Year View", YearViewApp::new),

            // --- Layout ---------------------------------------------------------
//            demo("Layout", "Alignment Test", AlignmentTestApp::new),
            demo("Layout", "Drawer Stack Pane", DrawerStackPaneApp::new),
            demo("Layout", "Loading Pane", LoadingPaneApp::new),
            demo("Layout", "Power Pane", PowerPaneApp::new),
            demo("Layout", "Resizing Behaviour", ResizingBehaviourApp::new),
            demo("Layout", "Responsive Pane", ResponsivePaneApp::new),
            demo("Layout", "Scroll Pane", ScrollPaneApp::new),
            demo("Layout", "Spacer", SpacerApp::new),
            demo("Layout", "Stretching Tile Pane", StretchingTilePaneApp::new),
            demo("Layout", "Three Items Pane", ThreeItemsPaneApp::new),

            // --- Lists & Tables -------------------------------------------------
            demo("Lists & Tables", "Filter View", FilterViewApp::new),
            demo("Lists & Tables", "Filter View (Simple)", SimpleFilterViewApp::new),
            demo("Lists & Tables", "Grid Table View", GridTableViewApp::new),
            desktopDemo("Lists & Tables", "Multi Column List View", MultiColumnListViewApp::new),
            demo("Lists & Tables", "Paging Controls", PagingControlsApp::new),
            demo("Lists & Tables", "Paging Grid Table View", PagingGridTableViewApp::new),
            demo("Lists & Tables", "Paging Grid Table View (Simple)", SimplePagingGridTableViewApp::new),
            demo("Lists & Tables", "Paging List View", PagingListViewApp::new),
            demo("Lists & Tables", "Paging List View (Simple)", SimplePagingListViewApp::new),
            demo("Lists & Tables", "Strip View", StripViewApp::new),
            desktopDemo("Lists & Tables", "Table View", TableViewExample::new),

            // --- Media & Graphics -----------------------------------------------
            demo("Media & Graphics", "Avatar View", AvatarViewApp::new),
            demo("Media & Graphics", "Before / After View", BeforeAfterViewApp::new),
            demo("Media & Graphics", "Circle Progress Indicator", CircleProgressIndicatorApp::new),
            demo("Media & Graphics", "Payment Option", PaymentOptionApp::new),
            demo("Media & Graphics", "Payment Option Tiles", PaymentOptionTilesApp::new),
            desktopDemo("Media & Graphics", "Photo View", PhotoViewApp::new),
            demo("Media & Graphics", "Segmented Bar", SegmentedBarApp::new),
            demo("Media & Graphics", "Semi-Circle Progress Indicator", SemiCircleProgressIndicatorApp::new),
            demo("Media & Graphics", "SVG Image View", SVGImageViewApp::new),

            // --- Overlays & Dialogs ---------------------------------------------
            demo("Overlays & Dialogs", "Dialog Pane", DialogPaneApp::new),
            desktopDemo("Overlays & Dialogs", "Dialog Pane with Markdown", DialogPaneWithMarkdownApp::new),
            demo("Overlays & Dialogs", "Info Center", InfoCenterApp::new),
            demo("Overlays & Dialogs", "Notification View", NotificationViewApp::new),
            demo("Overlays & Dialogs", "Pop Over", PopOverApp::new),

            // --- Text & Input ---------------------------------------------------
            demo("Text & Input", "Email Field", EmailFieldApp::new),
            desktopDemo("Text & Input", "Enhanced Label", EnhancedLabelApp::new),
            demo("Text & Input", "Enhanced Password Field", EnhancedPasswordFieldApp::new),
            demo("Text & Input", "Expanding Text Area", ExpandingTextAreaApp::new),
            demo("Text & Input", "Limited Text Area", LimitedTextAreaApp::new),
            demo("Text & Input", "Resizable Text Area", ResizableTextAreaApp::new),
            demo("Text & Input", "Search Field", SearchFieldApp::new),
            demo("Text & Input", "Search Text Field", SearchTextFieldApp::new),
            demo("Text & Input", "Selection Box", SelectionBoxApp::new),
            demo("Text & Input", "Tags Field", TagsFieldApp::new),
            desktopDemo("Text & Input", "Tags Field (Email)", TagsFieldEmailApp::new),
            demo("Text & Input", "Text View", TextViewApp::new),
            desktopDemo("Text & Input", "Text View in VBox", TextViewInVBoxApp::new),
            desktopDemo("Text & Input", "Text View with List View", TextViewWithListViewApp::new),
            desktopDemo("Text & Input", "Text View with Paging List View", TextViewWithPagingListViewApp::new),

            // --- Utilities ------------------------------------------------------
            demo("Utilities", "History Manager", HistoryManagerApp::new),
            desktopDemo("Utilities", "Recent Files", RecentFilesApp::new),
            desktopDemo("Utilities", "Screens View", ScreensViewApp::new),
            demo("Utilities", "Session Manager", SessionManagerApp::new),
            desktopDemo("Utilities", "Stage Manager", StageManagerApp::new),
            demo("Utilities", "Tree Node View", TreeNodeViewApp::new)
    );

    private static DemoEntry demo(String category, String name, Supplier<Application> factory) {
        return new DemoEntry(category, name, factory, false);
    }

    private static DemoEntry desktopDemo(String category, String name, Supplier<Application> factory) {
        return new DemoEntry(category, name, factory, true);
    }

    // -----------------------------------------------------------------------
    // UI
    // -----------------------------------------------------------------------

    @Override
    public void start(Stage stage) {
        if (!WebAPI.isBrowser()) {
            stage.initStyle(StageStyle.EXTENDED);
        }

        super.start(stage);
        launcherStage = stage;

        // ── AtlantaFX theme toggle ───────────────────────────────────────────
        String atlantaFxCss = getClass().getResource("atlantafx.css").toExternalForm();
        String mdfxModenaCss = Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("mdfx-modena-override.css")).toExternalForm();
        String mdfxOverrideCss = Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("mdfx-atlantafx-override.css")).toExternalForm();

        MarkdownView markdownView = new MarkdownView() {
            @Override
            public Optional<String> getDefaultLanguage() {
                return Optional.of("java");
            }
        };

        markdownView.getStyleClass().add("description-markdown-view");
        markdownView.getStylesheets().add(mdfxModenaCss);

        MarkdownView codeMarkdownView = new MarkdownView() {
            @Override
            public Optional<String> getDefaultLanguage() {
                return Optional.of("java");
            }
        };

        codeMarkdownView.getStyleClass().add("code-markdown-view");
        codeMarkdownView.getStylesheets().add(mdfxModenaCss);

        MarkdownView cssMarkdownView = new MarkdownView() {
            @Override
            public Optional<String> getDefaultLanguage() {
                return Optional.of("css");
            }
        };

        cssMarkdownView.getStyleClass().add("css-markdown-view");
        cssMarkdownView.getStylesheets().add(mdfxModenaCss);

        List<MarkdownView> markdownViews = List.of(markdownView, codeMarkdownView, cssMarkdownView);

        List<Theme> themes = List.of(new NordLight(), new NordDark(), new CupertinoLight(),
                new CupertinoDark(), new PrimerLight(), new PrimerDark(), new Dracula());
        Map<String, Theme> themeOptions = new LinkedHashMap<>();
        themeOptions.put("Modena", null);
        for (Theme theme : themes) {
            themeOptions.put(theme.getName(), theme);
        }

        Preferences prefs = Preferences.userNodeForPackage(GemsFXDemoLauncher.class);
        String legacyTheme = prefs.getBoolean("atlantafx.enabled", true)
                ? prefs.get("atlantafx.theme", new NordDark().getName())
                : "Modena";
        String savedTheme = prefs.get("launcher.theme", legacyTheme);
        String initialThemeName = themeOptions.containsKey(savedTheme) ? savedTheme : new NordDark().getName();

        MenuButton themeMenuButton = new MenuButton(initialThemeName);
        ToggleGroup themeToggleGroup = new ToggleGroup();
        for (String themeName : themeOptions.keySet()) {
            RadioMenuItem item = new RadioMenuItem(themeName);
            item.setToggleGroup(themeToggleGroup);
            item.setUserData(themeName);
            themeMenuButton.getItems().add(item);
            if (themeName.equals(initialThemeName)) {
                item.setSelected(true);
            }
        }

        // Logo image
        Image logoImage = new Image(Objects.requireNonNull(GemsFXDemoLauncher.class.getResourceAsStream("gems.png")));

        Button[] launchButtonRef = new Button[1];

        Runnable applyTheme = () -> {
            Toggle selectedToggle = themeToggleGroup.getSelectedToggle();
            if (selectedToggle == null) {
                return;
            }

            String selectedThemeName = (String) selectedToggle.getUserData();
            Theme theme = themeOptions.get(selectedThemeName);
            themeMenuButton.setText(selectedThemeName);
            prefs.put("launcher.theme", selectedThemeName);
            new ArrayList<>(openDemoStages).forEach(Stage::close);
            boolean darkTheme = isDarkTheme(theme);

            markdownViews.forEach(view -> view.getStyleClass().removeAll("light", "dark"));
            if (darkTheme) {
                markdownViews.forEach(view -> view.getStyleClass().add("dark"));
            } else {
                markdownViews.forEach(view -> view.getStyleClass().add("light"));
            }

            if (theme != null) {
                System.setProperty("atlantafx", "true");
                Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
                stage.getScene().getStylesheets().remove(atlantaFxCss);
                stage.getScene().getStylesheets().add(atlantaFxCss);
                stage.getScene().getRoot().getStyleClass().remove("atlantafx-active");
                stage.getScene().getRoot().getStyleClass().add("atlantafx-active");
                stage.getScene().getRoot().getStyleClass().remove("dark-theme");
                if (darkTheme) {
                    stage.getScene().getRoot().getStyleClass().add("dark-theme");
                }
                markdownView.getStylesheets().remove(mdfxOverrideCss);
                markdownView.getStylesheets().add(mdfxOverrideCss);
                codeMarkdownView.getStylesheets().remove(mdfxOverrideCss);
                codeMarkdownView.getStylesheets().add(mdfxOverrideCss);
                cssMarkdownView.getStylesheets().remove(mdfxOverrideCss);
                cssMarkdownView.getStylesheets().add(mdfxOverrideCss);
                if (launchButtonRef[0] != null) {
                    launchButtonRef[0].getStyleClass().add(Styles.ROUNDED);
                }
            } else {
                System.setProperty("atlantafx", "false");
                Application.setUserAgentStylesheet(null);
                stage.getScene().getStylesheets().remove(atlantaFxCss);
                stage.getScene().getRoot().getStyleClass().remove("atlantafx-active");
                stage.getScene().getRoot().getStyleClass().remove("dark-theme");
                markdownView.getStylesheets().remove(mdfxOverrideCss);
                codeMarkdownView.getStylesheets().remove(mdfxOverrideCss);
                cssMarkdownView.getStylesheets().remove(mdfxOverrideCss);
                if (launchButtonRef[0] != null) {
                    launchButtonRef[0].getStyleClass().remove(Styles.ROUNDED);
                }
            }
        };

        themeToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle != null) {
                applyTheme.run();
            }
        });

        // ── Search ──────────────────────────────────────────────────────────
        TextField searchField = new TextField();
        searchField.setPromptText("Search demos…");

        List<DemoEntry> visibleDemos = ALL_DEMOS.stream()
                .filter(entry -> !WebAPI.isBrowser() || !entry.desktopOnly())
                .toList();

        ObservableList<DemoEntry> allItems = FXCollections.observableArrayList(visibleDemos);
        FilteredList<DemoEntry> filtered = new FilteredList<>(allItems);

        searchField.textProperty().addListener((obs, oldVal, q) -> {
            String lower = q == null ? "" : q.strip().toLowerCase();
            filtered.setPredicate(lower.isEmpty() ? null
                    : e -> e.name().toLowerCase().contains(lower)
                           || e.category().toLowerCase().contains(lower));
        });

        // ── Tree view (default – no search active) ───────────────────────────
        TreeItem<Object> root = new TreeItem<>();
        root.setExpanded(true);

        Map<String, TreeItem<Object>> categoryNodes = new LinkedHashMap<>();
        for (DemoEntry entry : visibleDemos) {
            categoryNodes.computeIfAbsent(entry.category(), cat -> {
                TreeItem<Object> node = new TreeItem<>(cat);
                node.setExpanded(true);
                root.getChildren().add(node);
                return node;
            }).getChildren().add(new TreeItem<>(entry));
        }

        TreeView<Object> treeView = new TreeView<>(root);
        treeView.setShowRoot(false);
        treeView.setCellFactory(tv -> new TreeDemoCell());
        VBox.setVgrow(treeView, Priority.ALWAYS);

        // ── List view (search active) ────────────────────────────────────────
        ListView<DemoEntry> listView = new ListView<>(filtered);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(DemoEntry item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.name());
            }
        });
        VBox.setVgrow(listView, Priority.ALWAYS);

        // ── Status bar ───────────────────────────────────────────────────────
        Label countLabel = new Label(visibleDemos.size() + " demos");
        countLabel.setStyle("-fx-font-size: 11px;");
        countLabel.setOpacity(0.6);
        filtered.addListener((Observable obs) ->
                countLabel.setText(filtered.size() + " / " + visibleDemos.size() + " demos"));

        // ── Launch button ─────────────────────────────────────────────────────
        developerToolCheckBox = new CheckBox("Show Developer Tool");
        developerToolCheckBox.setVisible(!WebAPI.isBrowser());
        developerToolCheckBox.setManaged(!WebAPI.isBrowser());

        Button launchButton = new Button("Launch Demo");
        launchButton.setDefaultButton(true);
        launchButtonRef[0] = launchButton;

        // wire disable state
        updateLaunchButton(launchButton, treeView, listView, searchField);

        Region spacer = new Region();
        spacer.setMinHeight(200);
        spacer.setPrefHeight(200);
        VBox docContent = new VBox(16, markdownView, spacer);
        docContent.setPadding(new Insets(8));
        docContent.setAlignment(Pos.TOP_CENTER);

        ScrollPane docScrollPane = new ScrollPane(docContent);
        docScrollPane.setFitToWidth(true);
        docScrollPane.getStyleClass().add("doc-scroll-pane");

        // Float the gemsfx logo above the description scroll pane, bottom-right corner
        ImageView logoView = new ImageView(logoImage);
        logoView.setPreserveRatio(true);
        logoView.setFitHeight(120);
        logoView.setMouseTransparent(true);
        StackPane.setAlignment(logoView, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(logoView, new Insets(20, 20, 20, 20));
        StackPane descPane = new StackPane(docScrollPane, logoView);

        Tab descriptionTab = new Tab("Description", descPane);
        descriptionTab.setClosable(false);

        // ── Source Code tab ───────────────────────────────────────────────────
        VBox codeContent = new VBox(codeMarkdownView);
        codeContent.setPadding(new Insets(8));
        ScrollPane codeScrollPane = new ScrollPane(codeContent);
        codeScrollPane.setFitToWidth(true);
        codeScrollPane.getStyleClass().add("doc-scroll-pane");

        Tab sourceTab = new Tab("Source Code", codeScrollPane);
        sourceTab.setClosable(false);

        // ── CSS tab ───────────────────────────────────────────────────────────
        VBox cssContent = new VBox(cssMarkdownView);
        cssContent.setPadding(new Insets(8));
        ScrollPane cssScrollPane = new ScrollPane(cssContent);
        cssScrollPane.setFitToWidth(true);
        cssScrollPane.getStyleClass().add("doc-scroll-pane");

        Tab cssTab = new Tab("CSS", cssScrollPane);
        cssTab.setClosable(false);

        TabPane docTabPane = new TabPane(descriptionTab, sourceTab, cssTab);
        docTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        docTabPane.getStyleClass().add(Styles.TABS_CLASSIC);
        docTabPane.setPrefWidth(750);
        HBox.setHgrow(docTabPane, Priority.ALWAYS);

        searchField.textProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            if (entry != null) {
                prefs.put("selected.demo", entry.name());
            }
            showContent(entry, markdownView, codeMarkdownView, cssMarkdownView);
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            if (entry != null) {
                prefs.put("selected.demo", entry.name());
            }
            showContent(entry, markdownView, codeMarkdownView, cssMarkdownView);
        });

        // Restore last selected demo, falling back to the first one.
        String savedDemo = prefs.get("selected.demo", null);
        int[] selectRow = {0};
        for (int i = 0; i < treeView.getExpandedItemCount(); i++) {
            TreeItem<Object> item = treeView.getTreeItem(i);
            if (item != null && item.getValue() instanceof DemoEntry de
                    && de.name().equals(savedDemo)) {
                selectRow[0] = i;
                break;
            }
        }
        treeView.getSelectionModel().select(selectRow[0]);

        launchButton.setOnAction(evt -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            launch(entry);
        });

        // double-click on tree
        treeView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                DemoEntry entry = resolveSelected(treeView, listView, searchField);
                if (entry != null) {
                    launch(entry);
                }
            }
        });
        treeView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                DemoEntry entry = resolveSelected(treeView, listView, searchField);
                if (entry != null) {
                    launch(entry);
                }
            }
        });

        // double-click on list
        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                DemoEntry entry = listView.getSelectionModel().getSelectedItem();
                if (entry != null) {
                    launch(entry);
                }
            }
        });
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                DemoEntry entry = listView.getSelectionModel().getSelectedItem();
                if (entry != null) {
                    launch(entry);
                }
            }
        });

        // ── Toggle between tree / list depending on search ────────────────────
        BorderPane centerPane = new BorderPane(treeView);
        VBox.setVgrow(centerPane, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean searching = newVal != null && !newVal.strip().isEmpty();
            centerPane.setCenter(searching ? listView : treeView);
        });

        VBox layout = new VBox(8, searchField, new Separator(), centerPane);
        layout.setPadding(new Insets(12));
        layout.setMinWidth(Region.USE_PREF_SIZE);
        layout.setPrefWidth(330);

        StackPane tabPaneWrapper = new StackPane(docTabPane);
        tabPaneWrapper.getStyleClass().add("tab-pane-wrapper");
        HBox.setHgrow(tabPaneWrapper, Priority.ALWAYS);

        HBox rootHBox = new HBox(layout, tabPaneWrapper);

        Label titleLabel = new Label("GemsFX Demo Launcher");
        titleLabel.getStyleClass().add("title-label");

        String version = loadProjectVersion();
        Label versionLabel = new Label(version);
        versionLabel.getStyleClass().add("version-label");
        versionLabel.setVisible(!version.isBlank());
        versionLabel.setManaged(!version.isBlank());

        HBox titleBox = new HBox(8, titleLabel, versionLabel);
        titleBox.setAlignment(Pos.BASELINE_LEFT);

        HBox.setHgrow(titleBox, Priority.ALWAYS);

        ImageView dlscLogoView = new ImageView(new Image(Objects.requireNonNull(GemsFXDemoLauncher.class.getResourceAsStream("dlsc-logo.png"))));
        dlscLogoView.setPreserveRatio(true);
        dlscLogoView.setFitHeight(30);
        dlscLogoView.setCursor(javafx.scene.Cursor.HAND);
        dlscLogoView.setOnMouseClicked(e -> getHostServices().showDocument("https://dlsc.com"));

        ImageView githubBadgeView = new ImageView(new Image(Objects.requireNonNull(GemsFXDemoLauncher.class.getResourceAsStream("get-it-on-github.png"))));
        githubBadgeView.setPreserveRatio(true);
        githubBadgeView.setFitHeight(40);
        githubBadgeView.setCursor(javafx.scene.Cursor.HAND);
        githubBadgeView.setOnMouseClicked(e -> getHostServices().showDocument("https://github.com/dlemmermann/GemsFX"));

        Spacer spacer1 = new Spacer();
        spacer1.setMinWidth(50);
        spacer1.setMaxWidth(50);

        HBox header = new HBox(12, titleBox, themeMenuButton, spacer1, githubBadgeView, dlscLogoView);
        header.setAlignment(Pos.CENTER_LEFT);
        header.getStyleClass().add("launcher-header");
        if (WebAPI.isBrowser()) {
            header.getStyleClass().add("browser");
        }

        // Make the header draggable to move the stage
        double[] dragDelta = {0, 0};
        header.setOnMousePressed(e -> {
            dragDelta[0] = stage.getX() - e.getScreenX();
            dragDelta[1] = stage.getY() - e.getScreenY();
        });
        header.setOnMouseDragged(e -> {
            stage.setX(e.getScreenX() + dragDelta[0]);
            stage.setY(e.getScreenY() + dragDelta[1]);
        });

        // ── Footer ────────────────────────────────────────────────────────────
        Hyperlink poweredByJProLink = new Hyperlink("Powered by JPro");
        poweredByJProLink.setOnAction(e -> getHostServices().showDocument("https://www.jpro.one"));
        poweredByJProLink.setVisible(WebAPI.isBrowser());
        poweredByJProLink.setManaged(WebAPI.isBrowser());

        Region footerSpacer = new Region();
        HBox.setHgrow(footerSpacer, Priority.ALWAYS);
        HBox footer = new HBox(12, countLabel, poweredByJProLink, footerSpacer, developerToolCheckBox, launchButton);
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(8, 16, 8, 16));
        footer.getStyleClass().add("launcher-footer");

        VBox rootWithHeader = new VBox(header, rootHBox, footer);
        VBox.setVgrow(rootHBox, Priority.ALWAYS);

        browserGlassPane = new GlassPane();
        browserGlassPane.setHide(true);

        Scene scene = new Scene(new StackPane(rootWithHeader, browserGlassPane));
        scene.getStylesheets().add(Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("launcher.css")).toExternalForm());
        stage.setTitle("GemsFX — Demo Launcher");
        stage.setScene(scene);
        stage.centerOnScreen();
        applyTheme.run();

        stage.setWidth(1200);
        stage.setHeight(800);
        stage.show();
        Platform.runLater(() -> treeView.scrollTo(treeView.getSelectionModel().getSelectedIndex()));
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /**
     * Returns the selected DemoEntry from whichever view is active.
     */
    private DemoEntry resolveSelected(TreeView<Object> treeView,
                                      ListView<DemoEntry> listView,
                                      TextField searchField) {
        boolean searching = !searchField.getText().strip().isEmpty();
        if (searching) {
            return listView.getSelectionModel().getSelectedItem();
        }
        TreeItem<Object> item = treeView.getSelectionModel().getSelectedItem();
        if (item != null && item.getValue() instanceof DemoEntry entry) {
            return entry;
        }
        return null;
    }

    private void updateLaunchButton(Button button, TreeView<Object> treeView,
                                    ListView<DemoEntry> listView, TextField searchField) {
        button.setDisable(resolveSelected(treeView, listView, searchField) == null);
    }

    private void launch(DemoEntry entry) {
        if (entry == null) {
            return;
        }
        try {
            Application app = entry.factory().get();
            Stage demoStage = new Stage();
            demoStage.initOwner(launcherStage);
            if (WebAPI.isBrowser()) {
                demoStage.initModality(Modality.APPLICATION_MODAL);
            }
            app.start(demoStage);

            openDemoStages.add(demoStage);
            updateBrowserGlassPane();
            demoStage.setOnHidden(e -> {
                openDemoStages.remove(demoStage);
                updateBrowserGlassPane();
            });
            if (developerToolCheckBox.isSelected() && demoStage.getScene() != null) {
                GUI.openToolStage(demoStage, app.getHostServices());
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to launch \"" + entry.name() + "\"" + (developerToolCheckBox.isSelected() ? " with debug tool" : ""));
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void centerOnSameScreen(Stage demoStage) {
        if (!Double.isNaN(demoStage.getX())) {
            return; // StageManager already positioned it
        }
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getScreensForRectangle(
                        launcherStage.getX(), launcherStage.getY(),
                        launcherStage.getWidth(), launcherStage.getHeight())
                .stream().findFirst()
                .map(javafx.stage.Screen::getVisualBounds)
                .orElse(javafx.stage.Screen.getPrimary().getVisualBounds());
        demoStage.setX(bounds.getMinX() + (bounds.getWidth() - demoStage.getWidth()) / 2);
        demoStage.setY(bounds.getMinY() + (bounds.getHeight() - demoStage.getHeight()) / 2);
    }

    private void updateBrowserGlassPane() {
        if (browserGlassPane != null) {
            browserGlassPane.setHide(!WebAPI.isBrowser() || openDemoStages.isEmpty());
        }
    }

    // -----------------------------------------------------------------------
    // MarkdownView description helpers
    // -----------------------------------------------------------------------

    private void showContent(DemoEntry entry,
                             MarkdownView markdownView,
                             MarkdownView codeMarkdownView, MarkdownView cssMarkdownView) {
        if (entry == null) {
            markdownView.setMdString("*Select a demo to view its documentation.*");
            codeMarkdownView.setMdString("*Select a demo to view its source code.*");
            cssMarkdownView.setMdString("*Select a demo to view its CSS.*");
            return;
        }

        GemApplication app;
        try {
            app = (GemApplication) entry.factory().get();
        } catch (Exception e) {
            markdownView.setMdString("*No documentation available.*");
            codeMarkdownView.setMdString("*No source code available.*");
            cssMarkdownView.setMdString("*No CSS available.*");
            return;
        }

        String desc = app.getDescription();
        markdownView.setMdString(desc.isBlank()
                ? "*No documentation available.*"
                : desc);

        // Load source code
        String sourceCode = loadSourceCode(app);
        codeMarkdownView.setMdString(sourceCode != null
                ? "```java\n" + sourceCode + "\n```"
                : "*No source code available.*");

        // Load CSS
        String cssCode = loadCssForDemo(app);
        cssMarkdownView.setMdString(cssCode != null
                ? "```css\n" + cssCode + "\n```"
                : "*No CSS file found for this control.*");
    }

    private String loadSourceCode(GemApplication app) {
        String resourcePath = "/" + app.getClass().getName().replace('.', '/') + ".java";
        try (InputStream is = GemsFXDemoLauncher.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    private String loadCssForDemo(GemApplication app) {
        String controlName = app.getClass().getSimpleName();
        if (controlName.endsWith("App")) {
            controlName = controlName.substring(0, controlName.length() - 3);
        }

        // Try to instantiate the matching control class and call getUserAgentStylesheet()
        String[] packages = {
                "com.dlsc.gemsfx",
                "com.dlsc.gemsfx.daterange",
                "com.dlsc.gemsfx.infocenter",
                "com.dlsc.gemsfx.paging",
                "com.dlsc.gemsfx.gridtable",
                "com.dlsc.gemsfx.treeview",
                "com.dlsc.gemsfx.incubator"
        };
        for (String pkg : packages) {
            try {
                Class<?> cls = Class.forName(pkg + "." + controlName);
                Object instance = cls.getDeclaredConstructor().newInstance();
                java.lang.reflect.Method m = cls.getMethod("getUserAgentStylesheet");
                String stylesheet = (String) m.invoke(instance);
                if (stylesheet != null && !stylesheet.isBlank()) {
                    try (InputStream is = new URL(stylesheet).openStream()) {
                        return new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    }
                }
            } catch (Exception ignored) {
                // control not found in this package or no stylesheet — try next
            }
        }
        return null;
    }

    // -----------------------------------------------------------------------
    // Custom TreeCell
    // -----------------------------------------------------------------------

    /**
     * Returns true for AtlantaFX dark themes (NordDark, CupertinoDark, PrimerDark, Dracula).
     */
    private static boolean isDarkTheme(Theme theme) {
        if (theme == null) {
            return false;
        }
        String name = theme.getName().toLowerCase();
        return name.contains("dark") || name.equals("dracula");
    }

    private static String loadProjectVersion() {
        try (InputStream in = GemsFXDemoLauncher.class.getResourceAsStream("version.properties")) {
            if (in == null) {
                return "";
            }
            Properties props = new Properties();
            props.load(in);
            return props.getProperty("version", "");
        } catch (IOException e) {
            return "";
        }
    }

    private static class TreeDemoCell extends javafx.scene.control.TreeCell<Object> {
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setStyle("");
            } else if (item instanceof String category) {
                // Category header
                setText(category);
                setStyle("-fx-font-weight: bold;");
            } else if (item instanceof DemoEntry entry) {
                setText(entry.name());
                setStyle("");
            }
        }
    }

    public static void main(String[] args) {
        System.setProperty("javafx.enablePreview", "true");
        launch(args);
    }
}
