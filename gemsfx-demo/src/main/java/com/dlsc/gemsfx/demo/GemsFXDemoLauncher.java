package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.CupertinoDark;
import atlantafx.base.theme.CupertinoLight;
import atlantafx.base.theme.Dracula;
import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import atlantafx.base.theme.Theme;
import com.dlsc.gemsfx.demo.binding.AggregatedListBindingApp;
import com.dlsc.gemsfx.demo.binding.NestedListBindingApp;
import com.dlsc.gemsfx.demo.binding.NestedListChangeTrackerApp;
import com.dlsc.gemsfx.demo.binding.ObservableListBindingApp;
import com.dlsc.gemsfx.util.StageManager;
import javafx.application.Application;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
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
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import one.jpro.platform.mdfx.MarkdownView;
import org.scenicview.ScenicView;

import java.awt.Desktop;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.Preferences;
import java.util.function.Supplier;

/**
 * A single-window launcher that lists every demo application in the GemsFX
 * demo module.  Double-click (or select + Enter / "Launch" button) to open
 * a demo in its own {@link Stage}.
 */
public class GemsFXDemoLauncher extends GemApplication {

    // -----------------------------------------------------------------------
    // Demo registry
    // -----------------------------------------------------------------------

    public record DemoEntry(String category, String name, Supplier<Application> factory) {
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
            demo("Layout", "Alignment Test", AlignmentTestApp::new),
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
            demo("Lists & Tables", "Multi Column List View", MultiColumnListViewApp::new),
            demo("Lists & Tables", "Paging Controls", PagingControlsApp::new),
            demo("Lists & Tables", "Paging Grid Table View", PagingGridTableViewApp::new),
            demo("Lists & Tables", "Paging Grid Table View (Simple)", SimplePagingGridTableViewApp::new),
            demo("Lists & Tables", "Paging List View", PagingListViewApp::new),
            demo("Lists & Tables", "Paging List View (Simple)", SimplePagingListViewApp::new),
            demo("Lists & Tables", "Strip View", StripViewApp::new),
            demo("Lists & Tables", "Table View", TableViewExample::new),

            // --- Media & Graphics -----------------------------------------------
            demo("Media & Graphics", "Avatar View", AvatarViewApp::new),
            demo("Media & Graphics", "Before / After View", BeforeAfterViewApp::new),
            demo("Media & Graphics", "Circle Progress Indicator", CircleProgressIndicatorApp::new),
            demo("Media & Graphics", "Payment Option", PaymentOptionApp::new),
            demo("Media & Graphics", "Payment Option Tiles", PaymentOptionTilesApp::new),
            demo("Media & Graphics", "Photo View", PhotoViewApp::new),
            demo("Media & Graphics", "Segmented Bar", SegmentedBarApp::new),
            demo("Media & Graphics", "Semi-Circle Progress Indicator", SemiCircleProgressIndicatorApp::new),
            demo("Media & Graphics", "SVG Image View", SVGImageViewApp::new),

            // --- Overlays & Dialogs ---------------------------------------------
            demo("Overlays & Dialogs", "Dialog Pane", DialogPaneApp::new),
            demo("Overlays & Dialogs", "Dialog Pane with Markdown", DialogPaneWithMarkdownApp::new),
            demo("Overlays & Dialogs", "Info Center", InfoCenterApp::new),
            demo("Overlays & Dialogs", "Notification View", NotificationViewApp::new),
            demo("Overlays & Dialogs", "Pop Over", PopOverApp::new),

            // --- Text & Input ---------------------------------------------------
            demo("Text & Input", "Email Field", EmailFieldApp::new),
            demo("Text & Input", "Enhanced Label", EnhancedLabelApp::new),
            demo("Text & Input", "Enhanced Password Field", EnhancedPasswordFieldApp::new),
            demo("Text & Input", "Expanding Text Area", ExpandingTextAreaApp::new),
            demo("Text & Input", "Limited Text Area", LimitedTextAreaApp::new),
            demo("Text & Input", "Resizable Text Area", ResizableTextAreaApp::new),
            demo("Text & Input", "Search Field", SearchFieldApp::new),
            demo("Text & Input", "Search Text Field", SearchTextFieldApp::new),
            demo("Text & Input", "Selection Box", SelectionBoxApp::new),
            demo("Text & Input", "Tags Field", TagsFieldApp::new),
            demo("Text & Input", "Tags Field (Email)", TagsFieldEmailApp::new),
            demo("Text & Input", "Text View", TextViewApp::new),
            demo("Text & Input", "Text View in VBox", TextViewInVBoxApp::new),
            demo("Text & Input", "Text View with List View", TextViewWithListViewApp::new),
            demo("Text & Input", "Text View with Paging List View", TextViewWithPagingListViewApp::new),

            // --- Utilities ------------------------------------------------------
            demo("Utilities", "History Manager", HistoryManagerApp::new),
            demo("Utilities", "Recent Files", RecentFilesApp::new),
            demo("Utilities", "Screens View", ScreensViewApp::new),
            demo("Utilities", "Session Manager", SessionManagerApp::new),
            demo("Utilities", "Stage Manager", StageManagerApp::new),
            demo("Utilities", "Tree Node View", TreeNodeViewApp::new)
    );

    private static DemoEntry demo(String category, String name, Supplier<Application> factory) {
        return new DemoEntry(category, name, factory);
    }

    // -----------------------------------------------------------------------
    // UI
    // -----------------------------------------------------------------------

    @Override
    public void start(Stage stage) {
        stage.initStyle(StageStyle.EXTENDED);
        super.start(stage);
        launcherStage = stage;

        // ── AtlantaFX theme toggle ───────────────────────────────────────────
        String atlantaFxCss = getClass().getResource("atlantafx.css").toExternalForm();
        String mdfxModenaCss = Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("mdfx-modena-override.css")).toExternalForm();
        String mdfxOverrideCss = Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("mdfx-atlantafx-override.css")).toExternalForm();

        MarkdownView markdownView = new MarkdownView();
        markdownView.getStyleClass().add("description-markdown-view");
        markdownView.getStylesheets().add(mdfxModenaCss);

        List<Theme> themes = List.of(new NordLight(), new NordDark(), new CupertinoLight(),
                new CupertinoDark(), new PrimerLight(), new PrimerDark(), new Dracula());

        Preferences prefs = Preferences.userNodeForPackage(GemsFXDemoLauncher.class);
        String savedTheme = prefs.get("atlantafx.theme", themes.get(0).getName());
        Theme initialTheme = themes.stream()
                .filter(t -> t.getName().equals(savedTheme))
                .findFirst().orElse(themes.get(0));

        ComboBox<Theme> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(themes);
        themeComboBox.setValue(initialTheme);
        themeComboBox.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Theme t) {
                return t == null ? "" : t.getName();
            }

            @Override
            public Theme fromString(String s) {
                return null;
            }
        });
        HBox.setHgrow(themeComboBox, Priority.ALWAYS);
        themeComboBox.setMaxWidth(Double.MAX_VALUE);

        CheckBox atlantaFxCheckBox = new CheckBox("AtlantaFX");
        atlantaFxCheckBox.setSelected(prefs.getBoolean("atlantafx.enabled", false));
        if (atlantaFxCheckBox.isSelected()) System.setProperty("atlantafx", "true");
        themeComboBox.disableProperty().bind(atlantaFxCheckBox.selectedProperty().not());

        // Tracks the screenshot subdirectory for the active theme.
        // Updated whenever the theme changes so showDescription loads the right image.
        String[] currentThemeKey = {atlantaFxCheckBox.isSelected()
                ? toThemeKey(themeComboBox.getValue())
                : "modena"};

        // Refreshes the screenshot when the theme changes. Populated once tree/list/search are ready.
        Runnable[] refreshScreenshot = {() -> {}};

        // Logo image
        Image logoImage = new Image(Objects.requireNonNull(
                GemsFXDemoLauncher.class.getResourceAsStream("gems.png")));

        Runnable applyTheme = () -> {
            Theme theme = themeComboBox.getValue();
            if (theme != null) {
                currentThemeKey[0] = toThemeKey(theme);
                Application.setUserAgentStylesheet(theme.getUserAgentStylesheet());
                stage.getScene().getStylesheets().remove(atlantaFxCss);
                stage.getScene().getStylesheets().add(atlantaFxCss);
                stage.getScene().getRoot().getStyleClass().remove("atlantafx-active");
                stage.getScene().getRoot().getStyleClass().add("atlantafx-active");
                markdownView.getStylesheets().remove(mdfxOverrideCss);
                markdownView.getStylesheets().add(mdfxOverrideCss);
                refreshScreenshot[0].run();
            }
        };

        atlantaFxCheckBox.selectedProperty().addListener((obs, wasSelected, selected) -> {
            prefs.putBoolean("atlantafx.enabled", selected);
            System.setProperty("atlantafx", selected ? "true" : "false");
            new ArrayList<>(openDemoStages).forEach(Stage::close);
            if (selected) {
                applyTheme.run();
            } else {
                currentThemeKey[0] = "modena";
                Application.setUserAgentStylesheet(null);
                stage.getScene().getStylesheets().remove(atlantaFxCss);
                stage.getScene().getRoot().getStyleClass().remove("atlantafx-active");
                markdownView.getStylesheets().remove(mdfxOverrideCss);
                refreshScreenshot[0].run();
            }
        });
        themeComboBox.valueProperty().addListener((obs, o, n) -> {
            if (n != null) prefs.put("atlantafx.theme", n.getName());
            if (atlantaFxCheckBox.isSelected()) applyTheme.run();
        });

        HBox themeBar = new HBox(8, atlantaFxCheckBox, themeComboBox);
        themeBar.setAlignment(Pos.CENTER_LEFT);

        // ── Search ──────────────────────────────────────────────────────────
        TextField searchField = new TextField();
        searchField.setPromptText("Search demos…");

        ObservableList<DemoEntry> allItems = FXCollections.observableArrayList(ALL_DEMOS);
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
        for (DemoEntry entry : ALL_DEMOS) {
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
        Label countLabel = new Label(ALL_DEMOS.size() + " demos");
        countLabel.setStyle("-fx-font-size: 11px;");
        countLabel.setOpacity(0.6);
        filtered.addListener((Observable obs) ->
                countLabel.setText(filtered.size() + " / " + ALL_DEMOS.size() + " demos"));

        HBox statusBar = new HBox(countLabel);
        statusBar.setAlignment(Pos.CENTER_RIGHT);

        // ── Launch button ─────────────────────────────────────────────────────
        MenuItem launchItem = new MenuItem("Launch Demo");
        MenuItem launchWithScenicViewItem = new MenuItem("Launch with ScenicView");
        MenuButton launchButton = new MenuButton("Launch Demo", null, launchItem, launchWithScenicViewItem);
        launchButton.setMaxWidth(Double.MAX_VALUE);

        // wire disable + action for both views
        updateLaunchButton(launchButton, treeView, listView, searchField);

        // Screenshot displayed below the description text, with a reflection effect.
        ImageView screenshotView = new ImageView();
        screenshotView.setPreserveRatio(true);
        screenshotView.setSmooth(true);
        screenshotView.setManaged(false);
        screenshotView.setVisible(false);
        screenshotView.setFitWidth(600);
        screenshotView.setFitHeight(600);
        screenshotView.setCursor(javafx.scene.Cursor.HAND);
        screenshotView.setOnMouseClicked(e -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            if (entry != null) launch(entry);
        });

        Region spacer = new Region();
        spacer.setMinHeight(200);
        spacer.setPrefHeight(200);
        VBox docContent = new VBox(16, markdownView, screenshotView, spacer);
        docContent.setPadding(new Insets(8));

        ScrollPane docScrollPane = new ScrollPane(docContent);
        docScrollPane.setFitToWidth(true);
        docScrollPane.getStyleClass().add("doc-scroll-pane");
        docScrollPane.setPrefWidth(750);

        searchField.textProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));
        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) ->
                updateLaunchButton(launchButton, treeView, listView, searchField));

        treeView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            if (entry != null) prefs.put("selected.demo", entry.name());
            showDescription(entry, currentThemeKey[0], markdownView, screenshotView);
        });
        listView.getSelectionModel().selectedItemProperty().addListener((obs, o, n) -> {
            DemoEntry entry = resolveSelected(treeView, listView, searchField);
            if (entry != null) prefs.put("selected.demo", entry.name());
            showDescription(entry, currentThemeKey[0], markdownView, screenshotView);
        });

        // Wire up the screenshot refresh for theme changes (now that views are available).
        refreshScreenshot[0] = () -> showDescription(
                resolveSelected(treeView, listView, searchField),
                currentThemeKey[0], markdownView, screenshotView);

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

        launchItem.setOnAction(evt -> launch(resolveSelected(treeView, listView, searchField)));
        launchWithScenicViewItem.setOnAction(evt -> launchWithScenicView(resolveSelected(treeView, listView, searchField)));

        // double-click on tree
        treeView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                DemoEntry entry = resolveSelected(treeView, listView, searchField);
                if (entry != null) launch(entry);
            }
        });
        treeView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                DemoEntry entry = resolveSelected(treeView, listView, searchField);
                if (entry != null) launch(entry);
            }
        });

        // double-click on list
        listView.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                DemoEntry entry = listView.getSelectionModel().getSelectedItem();
                if (entry != null) launch(entry);
            }
        });
        listView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                DemoEntry entry = listView.getSelectionModel().getSelectedItem();
                if (entry != null) launch(entry);
            }
        });

        // ── Toggle between tree / list depending on search ────────────────────
        BorderPane centerPane = new BorderPane(treeView);
        VBox.setVgrow(centerPane, Priority.ALWAYS);
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean searching = newVal != null && !newVal.strip().isEmpty();
            centerPane.setCenter(searching ? listView : treeView);
        });

        VBox layout = new VBox(8, themeBar, new Separator(), searchField, new Separator(), centerPane, statusBar, launchButton);
        layout.setPadding(new Insets(12));
        layout.setMinWidth(Region.USE_PREF_SIZE);
        layout.setPrefWidth(330);

        HBox.setHgrow(docScrollPane, Priority.ALWAYS);
        HBox rootHBox = new HBox(layout, docScrollPane);

        // ── Header ────────────────────────────────────────────────────────────
        ImageView logoView = new ImageView(logoImage);
        logoView.setPreserveRatio(true);
        logoView.setFitHeight(64);

        Label titleLabel = new Label("GemsFX Demo Launcher");
        titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

        Label subtitleLabel = new Label("Professional custom controls for JavaFX.");
        subtitleLabel.setStyle("-fx-font-size: 13px;");

        VBox textBox = new VBox(2, titleLabel, subtitleLabel);
        textBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        ImageView dlscLogoView = new ImageView(new Image(Objects.requireNonNull(
                GemsFXDemoLauncher.class.getResourceAsStream("dlsc-logo.png"))));
        dlscLogoView.setPreserveRatio(true);
        dlscLogoView.setFitHeight(40);
        dlscLogoView.setCursor(javafx.scene.Cursor.HAND);
        dlscLogoView.setOnMouseClicked(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://dlsc.com"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        ImageView githubBadgeView = new ImageView(new Image(Objects.requireNonNull(
                GemsFXDemoLauncher.class.getResourceAsStream("get-it-on-github.png"))));
        githubBadgeView.setPreserveRatio(true);
        githubBadgeView.setFitHeight(60);
        githubBadgeView.setCursor(javafx.scene.Cursor.HAND);
        githubBadgeView.setOnMouseClicked(e -> {
            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().browse(new URI("https://github.com/dlemmermann/GemsFX"));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        HBox header = new HBox(12, logoView, textBox, githubBadgeView, dlscLogoView);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(27, 16, 12, 16));
        header.getStyleClass().add("launcher-header");

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

        VBox rootWithHeader = new VBox(header, rootHBox);
        VBox.setVgrow(rootHBox, Priority.ALWAYS);

        Scene scene = new Scene(new StackPane(rootWithHeader));
        scene.getStylesheets().add(Objects.requireNonNull(GemsFXDemoLauncher.class.getResource("launcher.css")).toExternalForm());
        stage.setTitle("GemsFX — Demo Launcher");
        stage.setScene(scene);
        stage.centerOnScreen();
        if (atlantaFxCheckBox.isSelected()) {
            applyTheme.run();
        }

        stage.sizeToScene();

        StageManager.install(stage, "gemsfx.demo.launcher");
        stage.show();

        // Lock width but allow height resizing; enforce minimum height
        stage.setMinWidth(stage.getWidth());
        stage.setMaxWidth(stage.getWidth());
        stage.setMinHeight(800);

        ScenicView.show(scene);
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

    private void updateLaunchButton(MenuButton button, TreeView<Object> treeView,
                                    ListView<DemoEntry> listView, TextField searchField) {
        button.setDisable(resolveSelected(treeView, listView, searchField) == null);
    }

    private void launch(DemoEntry entry) {
        if (entry == null) return;
        try {
            Application app = entry.factory().get();
            Stage demoStage = new Stage();
            app.start(demoStage);
            centerOnSameScreen(demoStage);
            openDemoStages.add(demoStage);
            demoStage.setOnHidden(e -> openDemoStages.remove(demoStage));
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to launch \"" + entry.name() + "\"");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void launchWithScenicView(DemoEntry entry) {
        if (entry == null) return;
        try {
            Application app = entry.factory().get();
            Stage demoStage = new Stage();
            app.start(demoStage);
            centerOnSameScreen(demoStage);
            openDemoStages.add(demoStage);
            demoStage.setOnHidden(e -> openDemoStages.remove(demoStage));
            if (demoStage.getScene() != null) {
                ScenicView.show(demoStage.getScene());
            }
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Launch Error");
            alert.setHeaderText("Failed to launch \"" + entry.name() + "\" with ScenicView");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    private void centerOnSameScreen(Stage demoStage) {
        if (!Double.isNaN(demoStage.getX())) return; // StageManager already positioned it
        javafx.geometry.Rectangle2D bounds = javafx.stage.Screen.getScreensForRectangle(
                        launcherStage.getX(), launcherStage.getY(),
                        launcherStage.getWidth(), launcherStage.getHeight())
                .stream().findFirst()
                .map(javafx.stage.Screen::getVisualBounds)
                .orElse(javafx.stage.Screen.getPrimary().getVisualBounds());
        demoStage.setX(bounds.getMinX() + (bounds.getWidth() - demoStage.getWidth()) / 2);
        demoStage.setY(bounds.getMinY() + (bounds.getHeight() - demoStage.getHeight()) / 2);
    }

    // -----------------------------------------------------------------------
    // MarkdownView description helpers
    // -----------------------------------------------------------------------

    private void showDescription(DemoEntry entry, String themeKey, MarkdownView markdownView, ImageView screenshotView) {
        if (entry == null) {
            markdownView.setMdString("*Select a demo to view its documentation.*");
            screenshotView.setImage(null);
            screenshotView.setManaged(false);
            screenshotView.setVisible(false);
            return;
        }
        try {
            GemApplication app = (GemApplication) entry.factory().get();
            String desc = app.getDescription();
            markdownView.setMdString(desc.isBlank()
                    ? "*No documentation available.*"
                    : desc);
        } catch (Exception e) {
            markdownView.setMdString("*No documentation available.*");
        }

        // Load screenshot: prefer theme-specific, fall back to modena.
        try {
            GemApplication app = (GemApplication) entry.factory().get();
            String className = app.getClass().getSimpleName();
            URL screenshotUrl = GemsFXDemoLauncher.class.getResource(
                    "screenshots/" + themeKey + "/" + className + ".png");
            if (screenshotUrl == null) {
                screenshotUrl = GemsFXDemoLauncher.class.getResource(
                        "screenshots/modena/" + className + ".png");
            }
            if (screenshotUrl != null) {
                screenshotView.setImage(new Image(
                        screenshotUrl.toExternalForm(), true));
                screenshotView.setManaged(true);
                screenshotView.setVisible(true);
            } else {
                screenshotView.setImage(null);
                screenshotView.setManaged(false);
                screenshotView.setVisible(false);
            }
        } catch (Exception e) {
            screenshotView.setImage(null);
            screenshotView.setManaged(false);
            screenshotView.setVisible(false);
        }
    }

    // -----------------------------------------------------------------------
    // Custom TreeCell
    // -----------------------------------------------------------------------

    /** Converts an AtlantaFX theme name to a screenshot subdirectory name, e.g. "Nord Light" → "nord-light". */
    private static String toThemeKey(Theme theme) {
        return theme == null ? "modena" : theme.getName().toLowerCase().replace(" ", "-");
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
