package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordDark;
import atlantafx.base.theme.NordLight;
import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockableFactory;
import com.dlsc.gemsfx.docking.DockingContext;
import com.dlsc.gemsfx.docking.DockingLayout;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingPosition;
import com.dlsc.gemsfx.docking.DockingRestoreResult;
import com.dlsc.gemsfx.docking.DockingStageFactory;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.kordamp.ikonli.Ikon;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

public class DockingPaneApp extends GemApplication {

    private static final DataFormat TITLE_SWAP_FORMAT = new DataFormat("com.dlsc.gemsfx.demo.dockable-title-swap");
    private static final DataFormat SIDEBAR_VIEW_FORMAT = new DataFormat("com.dlsc.gemsfx.demo.sidebar-view");
    private static final List<String> SIDEBAR_VIEW_TYPES = List.of("navigator", "editor", "preview", "console", "inspector");

    private final Map<String, Function<String, Dockable>> templates = createTemplates();
    private final BooleanProperty showFocusBorder = new SimpleBooleanProperty(this, "showFocusBorder", false);
    private DockingLayout savedLayout;
    private Label statusLabel;
    private Dockable dragDockable;
    private Dockable dropDockable;

    @Override
    public void start(Stage stage) {
        super.start(stage);

        DockableFactory dockableFactory = this::createDockable;
        DockingStageFactory stageFactory = pane -> createFloatingStage(stage, pane);
        DockingContext context = new DockingContext(dockableFactory, stageFactory);
        context.setDockableDropPredicate(event -> event.getDragboard() != null
                && event.getDragboard().hasContent(SIDEBAR_VIEW_FORMAT)
                && SIDEBAR_VIEW_TYPES.contains(event.getDragboard().getContent(SIDEBAR_VIEW_FORMAT)));
        context.setDockableDropFactory(event ->
                createSidebarDockable((String) event.getDragboard().getContent(SIDEBAR_VIEW_FORMAT)));

        DockingPane rootPane = context.createPane();
        rootPane.showFocusBorderProperty().bind(showFocusBorder);
        rootPane.setPadding(new Insets(4));
        openInitialLayout(context, rootPane);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("docking-demo-app");
        root.setTop(new VBox(createApplicationHeader(), createToolbar(stage, context, rootPane)));
        root.setLeft(createNavigationRail());
        root.setCenter(rootPane);
        BorderPane.setMargin(rootPane, new Insets(0, 0, 0, 4));
        root.setBottom(createStatusBar(rootPane));

        Scene scene = new Scene(root, 1280, 800);
        applyAppStyles(scene);
        stage.setTitle("DockingPane Demo");
        stage.setScene(scene);
        stage.show();

        status("Drag sidebar icons into the workspace to create views, or move the rocket between its panels. Drag tabs to rearrange views.");
    }

    HBox createStatusBar(DockingPane pane) {
        statusLabel = new Label("Drag tabs onto an outer-edge indicator to span the whole pane, or beside a group to split only that group.");
        statusLabel.setWrapText(true);
        statusLabel.setMinWidth(0);
        statusLabel.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(statusLabel, Priority.ALWAYS);

        Label active = new Label();
        active.setMinWidth(Region.USE_PREF_SIZE);
        active.textProperty().bind(pane.activeDockableProperty()
                .flatMap(Dockable::titleProperty)
                .map(title -> "Active: " + title)
                .orElse("Active: None"));

        HBox statusBar = new HBox(16, statusLabel, active);
        statusBar.getStyleClass().add("docking-demo-status");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        return statusBar;
    }

    private HBox createApplicationHeader() {
        Button menu = new Button();
        menu.getStyleClass().add("docking-demo-header-button");
        Region menuIcon = new Region();
        menuIcon.getStyleClass().add("docking-demo-menu-icon");
        menu.setGraphic(menuIcon);
        menu.setAccessibleText("Toggle navigation");

        Label brand = new Label("GemsFX");
        brand.getStyleClass().add("docking-demo-brand");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button search = new Button("Search", createIcon(MaterialDesign.MDI_MAGNIFY));
        search.getStyleClass().add("docking-demo-header-button");
        String darkStylesheet = new NordDark().getUserAgentStylesheet();
        String lightStylesheet = new NordLight().getUserAgentStylesheet();
        Button theme = new Button(Objects.equals(Application.getUserAgentStylesheet(), darkStylesheet)
                ? "Nord Light" : "Nord Dark");
        theme.getStyleClass().add("docking-demo-header-button");
        theme.setTooltip(new Tooltip("Switch between Nord Light and Nord Dark"));
        boolean showTheme = Boolean.getBoolean("atlantafx") && !isRunningInShowcase();
        theme.setVisible(showTheme);
        theme.setManaged(showTheme);
        theme.setOnAction(evt -> {
            boolean dark = Objects.equals(Application.getUserAgentStylesheet(), darkStylesheet);
            Application.setUserAgentStylesheet(dark ? lightStylesheet : darkStylesheet);
            theme.setText(dark ? "Nord Dark" : "Nord Light");
        });

        Button settings = configureDevToolsButton(new Button("", createIcon(MaterialDesign.MDI_SETTINGS)));
        settings.setText("");
        settings.setAccessibleText("Open DevToolsFX");
        settings.setTooltip(new Tooltip("Open DevToolsFX"));
        settings.getStyleClass().add("docking-demo-header-button");
        Label user = new Label("DI");
        user.getStyleClass().add("docking-demo-user");

        HBox header = new HBox(10, menu, brand, spacer, search, theme, settings, user);
        header.getStyleClass().add("docking-demo-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createNavigationRail() {
        VBox rail = new VBox(
                createNavigationButton(MaterialDesign.MDI_FOLDER, "Navigator", "navigator"),
                createNavigationButton(MaterialDesign.MDI_CODE_TAGS, "Editor", "editor"),
                createNavigationButton(MaterialDesign.MDI_EYE, "Preview", "preview"),
                createNavigationButton(MaterialDesign.MDI_CONSOLE, "Console", "console"),
                createNavigationButton(MaterialDesign.MDI_SETTINGS, "Inspector", "inspector"));
        rail.getStyleClass().add("docking-demo-navigation");
        rail.setAlignment(Pos.TOP_CENTER);
        return rail;
    }

    private Button createNavigationButton(Ikon icon, String description, String templateId) {
        Button button = new Button("", createIcon(icon));
        button.getStyleClass().add("docking-demo-navigation-button");
        button.setAccessibleText(description);
        button.setTooltip(new Tooltip(description + " - drag into the workspace to create a view"));
        button.setOnDragDetected(event -> {
            Label preview = new Label(description, createIcon(icon));
            preview.getStyleClass().add("docking-demo-sidebar-drag-preview");
            StackPane previewRoot = new StackPane(preview);
            Scene previewScene = new Scene(previewRoot);
            previewScene.getStylesheets().setAll(button.getScene().getStylesheets());
            previewRoot.applyCss();
            previewRoot.autosize();
            previewRoot.layout();
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            Image image = previewRoot.snapshot(parameters, null);
            Dragboard dragboard = button.startDragAndDrop(TransferMode.COPY);
            dragboard.setDragView(image, image.getWidth() / 2, image.getHeight() / 2);
            ClipboardContent content = new ClipboardContent();
            content.put(SIDEBAR_VIEW_FORMAT, templateId);
            dragboard.setContent(content);
            event.consume();
        });
        return button;
    }

    private void applyAppStyles(Scene scene) {
        if (Boolean.getBoolean("atlantafx")) {
            GemsFXAtlantaFX.applyTo(scene);
            scene.getStylesheets().add(Objects.requireNonNull(
                    DockingPaneApp.class.getResource("docking-pane-app-atlantafx.css")).toExternalForm());
        }
    }

    private HBox createToolbar(Stage stage, DockingContext context, DockingPane rootPane) {
        Button openSearch = new Button("Open Search");
        openSearch.setOnAction(evt -> {
            context.openDockable(rootPane, "search", DockingPosition.CENTER);
            status("Opened dockable 'search' via DockableFactory.");
        });

        Button openTasks = new Button("Open Tasks");
        openTasks.setOnAction(evt -> {
            context.openDockable(rootPane, "tasks", DockingPosition.CENTER);
            status("Opened dockable 'tasks' via DockableFactory.");
        });

        MenuButton openView = new MenuButton("Open View");
        for (String id : List.of("map", "charts", "city", "drag", "drop")) {
            MenuItem item = new MenuItem(switch (id) {
                case "map" -> "Map";
                case "charts" -> "Financial Charts";
                case "city" -> "City";
                case "drag" -> "Rocket Source";
                default -> "Rocket Target";
            });
            item.setOnAction(evt -> {
                context.openDockable(rootPane, id, DockingPosition.CENTER);
                status("Opened dockable '" + id + "' via DockableFactory.");
            });
            openView.getItems().add(item);
        }

        Button floatActive = new Button("Float Active");
        floatActive.setOnAction(evt -> {
            Dockable active = rootPane.getActiveDockable();
            if (active == null) {
                status("No active dockable in the main pane.");
                return;
            }
            context.floatDockable(active.getId(), stage.getX() + stage.getWidth() + 30, stage.getY() + 100);
            status("Floated dockable '" + active.getId() + "' into an app-created stage.");
        });

        Button snapshot = new Button("Snapshot Layout");
        snapshot.setOnAction(evt -> {
            savedLayout = context.getLayout();
            status("Captured layout. Use Restore Layout to recreate panes, tabs, and floating windows.");
        });

        Button restore = new Button("Restore Layout");
        restore.setOnAction(evt -> {
            if (savedLayout == null) {
                status("No saved layout yet. Capture one first.");
                return;
            }
            DockingRestoreResult result = context.restoreLayout(savedLayout);
            if (result.getUnresolvedDockableIds().isEmpty()) {
                status("Layout restored successfully.");
            } else {
                status("Layout restored with unresolved IDs: " + result.getUnresolvedDockableIds());
            }
        });

        Button clearAll = new Button("Clear All");
        clearAll.disableProperty().bind(Bindings.isEmpty(context.getDockables()));
        clearAll.setOnAction(evt -> {
            List.copyOf(context.getDockables()).forEach(context::requestCloseDockable);
            status(context.getDockables().isEmpty()
                    ? "Closed all dockables."
                    : "Some dockables could not be closed.");
        });

        Button dumpJson = new Button("Dump Layout JSON");
        dumpJson.setOnAction(event -> showLayoutJson(stage, context));

        CheckBox focusBorder = new CheckBox("Focus Border");
        focusBorder.selectedProperty().bindBidirectional(showFocusBorder);
        focusBorder.setTooltip(new Tooltip("Highlight the tab group containing keyboard focus"));

        HBox toolbar = new HBox(8, openSearch, openTasks, openView, floatActive, snapshot, restore, dumpJson, clearAll, focusBorder);
        toolbar.getStyleClass().add("docking-demo-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        return toolbar;
    }

    private void showLayoutJson(Stage owner, DockingContext context) {
        String json;
        try {
            json = layoutToJson(context.getLayout());
        } catch (JsonProcessingException ex) {
            status("Could not serialize layout: " + ex.getOriginalMessage());
            return;
        }

        TextArea content = new TextArea(json);
        content.setEditable(false);
        content.setWrapText(false);
        content.setPrefColumnCount(80);
        content.setPrefRowCount(30);
        content.getStyleClass().add("docking-demo-code");
        content.setAccessibleText("Current docking layout JSON");

        Dialog<Void> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("Docking Layout JSON");
        dialog.setResizable(true);
        dialog.getDialogPane().getStyleClass().add("docking-demo-app");
        dialog.getDialogPane().getStylesheets().setAll(owner.getScene().getStylesheets());
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.show();
    }

    static String layoutToJson(DockingLayout layout) throws JsonProcessingException {
        return new ObjectMapper()
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(layout);
    }

    private void openInitialLayout(DockingContext context, DockingPane rootPane) {
        context.openDockable(rootPane, "editor", DockingPosition.CENTER);
        context.openDockable(rootPane, "map", DockingPosition.CENTER);
        context.openDockable(rootPane, "preview", DockingPosition.BOTTOM);
        context.openDockable(rootPane, "charts", DockingPosition.CENTER);
        context.openDockable(rootPane, "console", DockingPosition.RIGHT);
        context.openDockable(rootPane, "navigator", DockingPosition.CENTER);
        context.openDockable(rootPane, "inspector", DockingPosition.CENTER);
        context.openDockable(rootPane, "city", DockingPosition.CENTER);
        rootPane.selectDockable("editor");
        context.openDockable(rootPane, "drag", DockingPosition.RIGHT);
        context.openDockable(rootPane, "drop", DockingPosition.RIGHT);
        rootPane.updateDividerPosition(List.of(), 0.38);
        rootPane.updateDividerPosition(List.of(1), 0.62);
        rootPane.updateDividerPosition(List.of(0), 0.5);
        rootPane.updateDividerPosition(List.of(0, 1), 0.5);
    }

    private Stage createFloatingStage(Stage owner, DockingPane pane) {
        pane.showFocusBorderProperty().bind(showFocusBorder);
        Label hint = new Label("Drag between windows. Outer-edge indicators dock beside the entire layout; other edge drops split one group.");
        hint.setWrapText(true);

        BorderPane wrapper = new BorderPane(pane);
        wrapper.getStyleClass().add("docking-demo-app");
        wrapper.setBottom(hint);
        BorderPane.setMargin(hint, new Insets(8));

        Stage floating = new Stage();
        floating.initOwner(owner);
        floating.setTitle("DockingPane Floating Stage");
        Scene scene = new Scene(wrapper, 420, 280);
        applyAppStyles(scene);
        floating.setScene(scene);
        floating.show();
        return floating;
    }

    private Map<String, Function<String, Dockable>> createTemplates() {
        Map<String, Function<String, Dockable>> templates = new LinkedHashMap<>();
        templates.put("editor", this::createEditorDockable);
        templates.put("preview", this::createPreviewDockable);
        templates.put("console", this::createConsoleDockable);
        templates.put("navigator", this::createNavigatorDockable);
        templates.put("inspector", this::createInspectorDockable);
        templates.put("search", id -> createTextDockable(id, "Search", MaterialDesign.MDI_MAGNIFY,
                "Search results\n- DockingContext\n- DockingPane\n- DockingLayout"));
        templates.put("tasks", id -> createListDockable(id, "Tasks", MaterialDesign.MDI_CHECKBOX_MARKED_OUTLINE,
                "TODO: wire toolbar", "TODO: refresh preview", "DONE: snapshot layout"));
        templates.put("map", this::createMapDockable);
        templates.put("charts", this::createChartsDockable);
        templates.put("city", this::createCityDockable);
        templates.put("drag", this::createDragDockable);
        templates.put("drop", this::createDropDockable);
        return templates;
    }

    Optional<Dockable> createDockable(String id) {
        String templateId = id.split(":", 2)[0];
        return Optional.ofNullable(templates.get(templateId)).map(factory -> factory.apply(id));
    }

    Dockable createSidebarDockable(String templateId) {
        if (!SIDEBAR_VIEW_TYPES.contains(templateId)) {
            return null;
        }
        return createDockable(templateId + ":" + UUID.randomUUID()).orElse(null);
    }

    private Dockable createNavigatorDockable(String id) {
        TreeItem<String> project = new TreeItem<>("rocket-mission");
        TreeItem<String> sources = new TreeItem<>("src");
        sources.getChildren().add(new TreeItem<>("MissionControl.java"));
        sources.getChildren().add(new TreeItem<>("Rocket.java"));
        sources.setExpanded(true);
        TreeItem<String> resources = new TreeItem<>("resources");
        resources.getChildren().add(new TreeItem<>("mission.properties"));
        project.getChildren().add(sources);
        project.getChildren().add(resources);
        project.getChildren().add(new TreeItem<>("README.md"));
        project.setExpanded(true);
        TreeView<String> tree = new TreeView<>(project);
        tree.setAccessibleText("Mission project files");
        return new Dockable(id, "Navigator", createIcon(MaterialDesign.MDI_FOLDER), tree);
    }

    private Dockable createEditorDockable(String id) {
        Dockable editor = createTextDockable(id, "Editor", MaterialDesign.MDI_CODE_TAGS,
                "public class MissionControl {\n\n"
                        + "    public void launch() {\n"
                        + "        System.out.println(\"Ready for liftoff!\");\n"
                        + "    }\n}\n");
        editor.getContent().getStyleClass().add("docking-demo-code");
        editor.getContent().setAccessibleText("MissionControl.java editor");
        return editor;
    }

    private Dockable createPreviewDockable(String id) {
        Label title = new Label("Mission overview");
        title.getStyleClass().add("docking-demo-view-title");
        Label introduction = new Label("Welcome aboard! This preview shows the launch notes for our next rocket mission.");
        introduction.setWrapText(true);
        Label checklist = new Label("Launch checklist\n\n"
                + "1. Review the flight plan\n2. Check fuel and telemetry\n3. Clear the launch pad\n4. Start the countdown");
        checklist.setWrapText(true);
        Label footer = new Label("All systems ready. Next stop: the stars.");
        footer.setWrapText(true);
        VBox document = new VBox(16, title, introduction, new Separator(), checklist, footer);
        document.setPadding(new Insets(24));
        ScrollPane preview = new ScrollPane(document);
        preview.setFitToWidth(true);
        return new Dockable(id, "Preview", createIcon(MaterialDesign.MDI_EYE), preview);
    }

    private Dockable createConsoleDockable(String id) {
        TextArea output = new TextArea("[INFO] Mission control initialized\n"
                + "[INFO] Connecting to rocket telemetry...\n"
                + "[OK] Guidance, fuel, and communications ready\n"
                + "[INFO] Waiting for launch clearance\n");
        output.setEditable(false);
        output.getStyleClass().add("docking-demo-code");
        output.setAccessibleText("Mission console output");
        Button clear = new Button("Clear output");
        clear.setOnAction(event -> output.clear());
        HBox toolbar = new HBox(8, new Label("Application output"), clear);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8));
        BorderPane content = new BorderPane(output);
        content.setTop(toolbar);
        return new Dockable(id, "Console", createIcon(MaterialDesign.MDI_CONSOLE), content);
    }

    private Dockable createInspectorDockable(String id) {
        TextField name = new TextField("Explorer");
        ComboBox<String> status = new ComboBox<>();
        status.getItems().setAll("Preparing", "Ready", "Launched");
        status.setValue("Ready");
        CheckBox telemetry = new CheckBox("Enabled");
        telemetry.setSelected(true);
        Slider fuel = new Slider(0, 100, 85);
        fuel.setShowTickLabels(true);
        fuel.setShowTickMarks(true);
        GridPane properties = new GridPane();
        properties.setPadding(new Insets(16));
        properties.setHgap(12);
        properties.setVgap(16);
        Label nameLabel = new Label("Rocket name");
        nameLabel.setLabelFor(name);
        Label statusLabel = new Label("Launch status");
        statusLabel.setLabelFor(status);
        Label telemetryLabel = new Label("Telemetry");
        telemetryLabel.setLabelFor(telemetry);
        Label fuelLabel = new Label("Fuel level");
        fuelLabel.setLabelFor(fuel);
        properties.addRow(0, nameLabel, name);
        properties.addRow(1, statusLabel, status);
        properties.addRow(2, telemetryLabel, telemetry);
        properties.addRow(3, fuelLabel, fuel);
        GridPane.setHgrow(name, Priority.ALWAYS);
        return new Dockable(id, "Inspector", createIcon(MaterialDesign.MDI_SETTINGS), properties);
    }

    private Dockable createDragDockable(String id) {
        FontIcon rocket = createIcon(MaterialDesign.MDI_ROCKET);
        rocket.getStyleClass().addAll("docking-demo-drag-graphic", "docking-demo-rocket-icon");
        rocket.setAccessibleText("Drag the rocket");
        rocket.setOnDragDetected(event -> {
            SnapshotParameters parameters = new SnapshotParameters();
            parameters.setFill(Color.TRANSPARENT);
            Image preview = rocket.snapshot(parameters, null);
            Dragboard dragboard = rocket.startDragAndDrop(TransferMode.MOVE);
            dragboard.setDragView(preview, preview.getWidth() / 2, preview.getHeight() / 2);
            ClipboardContent content = new ClipboardContent();
            content.put(TITLE_SWAP_FORMAT, "swap-titles");
            dragboard.setContent(content);
            event.consume();
        });

        Label hint = new Label("Drag the rocket");
        VBox graphicAndHint = new VBox(12, rocket, hint);
        graphicAndHint.setAlignment(Pos.CENTER);
        StackPane content = new StackPane(graphicAndHint);

        FontIcon headerIcon = createIcon(MaterialDesign.MDI_ROCKET);
        headerIcon.getStyleClass().add("docking-demo-rocket-icon");
        Dockable dockable = new Dockable(id, "Rocket Source", headerIcon, content);
        dragDockable = dockable;
        configureContentDropTarget(dockable);
        return dockable;
    }

    private Dockable createDropDockable(String id) {
        FontIcon target = createIcon(MaterialDesign.MDI_TARGET);
        target.getStyleClass().addAll("docking-demo-target-graphic", "docking-demo-target-icon");
        target.setAccessibleText("Rocket drop target");
        Label hint = new Label("Drop the rocket here");
        VBox graphicAndHint = new VBox(12, target, hint);
        graphicAndHint.setAlignment(Pos.CENTER);
        StackPane content = new StackPane(graphicAndHint);

        FontIcon headerIcon = createIcon(MaterialDesign.MDI_INBOX_ARROW_DOWN);
        headerIcon.getStyleClass().add("docking-demo-target-icon");
        Dockable dockable = new Dockable(id, "Rocket Target", headerIcon, content);
        dropDockable = dockable;
        configureContentDropTarget(dockable);
        return dockable;
    }

    private void configureContentDropTarget(Dockable dockable) {
        StackPane content = (StackPane) dockable.getContent();
        content.setOnDragOver(event -> {
            if (dockable == dropDockable && event.getDragboard().hasContent(TITLE_SWAP_FORMAT)) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        });
        content.setOnDragDropped(event -> {
            if (!event.getDragboard().hasContent(TITLE_SWAP_FORMAT)) {
                return;
            }

            boolean dropped = dockable == dropDockable && dragDockable != null;
            if (dropped) {
                swapContentsAndHeaders(dragDockable, dropDockable);
                Dockable previousSource = dragDockable;
                dragDockable = dropDockable;
                dropDockable = previousSource;
                status("Rocket moved; dockable titles, icons, and instructions swapped.");
            }
            event.setDropCompleted(dropped);
            event.consume();
        });
    }

    static void swapContentsAndHeaders(Dockable drag, Dockable drop) {
        StackPane source = (StackPane) drag.getContent();
        StackPane target = (StackPane) drop.getContent();
        var sourceChildren = List.copyOf(source.getChildren());
        var targetChildren = List.copyOf(target.getChildren());
        source.getChildren().clear();
        target.getChildren().clear();
        source.getChildren().setAll(targetChildren);
        target.getChildren().setAll(sourceChildren);

        String title = drag.getTitle();
        drag.setTitle(drop.getTitle());
        drop.setTitle(title);

        var sourceGraphic = drag.getGraphic();
        var targetGraphic = drop.getGraphic();
        drag.setGraphic(null);
        drop.setGraphic(null);
        drag.setGraphic(targetGraphic);
        drop.setGraphic(sourceGraphic);
    }

    private Dockable createMapDockable(String id) {
        WebView map = new WebView();
        map.setMinSize(0, 0);
        map.setPrefSize(600, 300);
        map.getEngine().setUserAgent(map.getEngine().getUserAgent()
                + " GemsFX-DockingDemo (https://github.com/dlsc-software-consulting-gmbh/GemsFX)");
        map.getEngine().load(Objects.requireNonNull(
                DockingPaneApp.class.getResource("docking-map.html")).toExternalForm());

        Hyperlink attribution = new Hyperlink("OpenStreetMap contributors");
        attribution.setOnAction(evt -> getHostServices().showDocument("https://www.openstreetmap.org/copyright"));
        Label loading = new Label();
        loading.setWrapText(true);
        loading.textProperty().bind(Bindings.createStringBinding(() -> {
            Worker.State state = map.getEngine().getLoadWorker().getState();
            return switch (state) {
                case FAILED, CANCELLED -> "Map unavailable - internet access is required.";
                case SUCCEEDED -> "Berlin - pan and zoom to explore";
                default -> "Loading map...";
            };
        }, map.getEngine().getLoadWorker().stateProperty()));
        Button reload = new Button("Reload");
        reload.setOnAction(evt -> map.getEngine().reload());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox footer = new HBox(8, attribution, loading, spacer, reload);
        footer.getStyleClass().add("docking-demo-map-footer");
        footer.setAlignment(Pos.CENTER_LEFT);
        footer.setPadding(new Insets(4, 8, 4, 8));
        footer.setMaxWidth(Double.MAX_VALUE);

        footer.setMaxHeight(Region.USE_PREF_SIZE);
        StackPane content = new StackPane(map, footer);
        content.setMinSize(0, 0);
        StackPane.setAlignment(footer, Pos.BOTTOM_CENTER);
        return new Dockable(id, "Map", createIcon(MaterialDesign.MDI_MAP), content);
    }

    private Dockable createChartsDockable(String id) {
        NumberAxis priceAxis = new NumberAxis();
        priceAxis.setLabel("Price (USD)");
        priceAxis.setForceZeroInRange(false);
        AreaChart<String, Number> priceChart = new AreaChart<>(new CategoryAxis(), priceAxis);
        priceChart.setTitle("Sample market data (not live)");
        priceChart.setAnimated(false);
        priceChart.setCreateSymbols(false);
        priceChart.setLegendVisible(false);
        priceChart.setMinSize(0, 0);
        priceChart.setPrefHeight(220);

        NumberAxis volumeAxis = new NumberAxis();
        volumeAxis.setLabel("Volume (thousands)");
        BarChart<String, Number> volumeChart = new BarChart<>(new CategoryAxis(), volumeAxis);
        volumeChart.setAnimated(false);
        volumeChart.setLegendVisible(false);
        volumeChart.setMinSize(0, 0);
        volumeChart.setPrefHeight(160);
        volumeChart.setCategoryGap(4);
        volumeChart.setBarGap(0);

        double[] prices = {142.5, 143.2, 142.8, 144.1, 145.6, 145.0, 146.4, 147.1, 146.6, 148.2, 149.4, 148.9};
        int[] volumes = {45, 62, 38, 71, 84, 56, 93, 79, 48, 105, 121, 88};
        XYChart.Series<String, Number> priceSeries = new XYChart.Series<>();
        XYChart.Series<String, Number> volumeSeries = new XYChart.Series<>();
        for (int i = 0; i < prices.length; i++) {
            String time = String.format("%02d:%02d", 9 + (i + 1) / 2, i % 2 == 0 ? 30 : 0);
            priceSeries.getData().add(new XYChart.Data<>(time, prices[i]));
            volumeSeries.getData().add(new XYChart.Data<>(time, volumes[i]));
        }
        priceChart.getData().add(priceSeries);
        volumeChart.getData().add(volumeSeries);

        VBox content = new VBox(4, priceChart, volumeChart);
        content.setPadding(new Insets(8));
        content.setMinSize(0, 0);
        VBox.setVgrow(priceChart, Priority.ALWAYS);
        VBox.setVgrow(volumeChart, Priority.SOMETIMES);
        return new Dockable(id, "Financial Charts", createIcon(MaterialDesign.MDI_CHART_LINE), content);
    }

    private Dockable createCityDockable(String id) {
        Image image = new Image(Objects.requireNonNull(
                DockingPaneApp.class.getResource("berlin/after1.png")).toExternalForm());
        ImageView city = new ImageView(image);
        city.setPreserveRatio(true);
        city.setSmooth(true);
        city.setAccessibleText("Photograph of Berlin");
        StackPane imagePane = new StackPane(city);
        imagePane.setMinSize(0, 0);
        Rectangle clip = new Rectangle();
        clip.widthProperty().bind(imagePane.widthProperty());
        clip.heightProperty().bind(imagePane.heightProperty());
        imagePane.setClip(clip);

        var scale = Bindings.createDoubleBinding(
                () -> Math.max(imagePane.getWidth() / image.getWidth(), imagePane.getHeight() / image.getHeight()),
                imagePane.widthProperty(), imagePane.heightProperty());
        city.fitWidthProperty().bind(scale.multiply(image.getWidth()));
        city.fitHeightProperty().bind(scale.multiply(image.getHeight()));
        return new Dockable(id, "City", createIcon(MaterialDesign.MDI_IMAGE), imagePane);
    }

    private Dockable createTextDockable(String id, String title, Ikon icon, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(8);
        return new Dockable(id, title, createIcon(icon), textArea);
    }

    private Dockable createListDockable(String id, String title, Ikon icon, String... values) {
        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(values);

        StackPane container = new StackPane(listView);
        container.setPadding(new Insets(8));
        return new Dockable(id, title, createIcon(icon), container);
    }

    private FontIcon createIcon(Ikon icon) {
        FontIcon graphic = new FontIcon(icon);
        graphic.setIconSize(16);
        return graphic;
    }

    private void status(String text) {
        statusLabel.setText(text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
