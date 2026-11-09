package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockableFactory;
import com.dlsc.gemsfx.DockingContext;
import com.dlsc.gemsfx.DockingLayout;
import com.dlsc.gemsfx.DockingPane;
import com.dlsc.gemsfx.DockingPosition;
import com.dlsc.gemsfx.DockingRestoreResult;
import com.dlsc.gemsfx.DockingStageFactory;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import javafx.beans.binding.Bindings;
import javafx.concurrent.Worker;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
import java.util.function.Function;

public class DockingPaneApp extends GemApplication {

    private DockingLayout savedLayout;
    private Label statusLabel;

    @Override
    public void start(Stage stage) {
        super.start(stage);

        Map<String, Function<String, Dockable>> templates = createTemplates();

        DockableFactory dockableFactory = id -> Optional.ofNullable(templates.get(id)).map(factory -> factory.apply(id));
        DockingStageFactory stageFactory = pane -> createFloatingStage(stage, pane);
        DockingContext context = new DockingContext(dockableFactory, stageFactory);

        DockingPane rootPane = context.createPane();
        rootPane.setPadding(new Insets(4));
        openInitialLayout(context, rootPane);

        statusLabel = new Label("Drag tabs onto an outer-edge indicator to span the whole pane, or beside a group to split only that group.");
        statusLabel.getStyleClass().add("docking-demo-status");
        statusLabel.setWrapText(true);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("docking-demo-app");
        root.setTop(new VBox(createApplicationHeader(), createToolbar(stage, context, rootPane)));
        root.setLeft(createNavigationRail());
        root.setCenter(rootPane);
        BorderPane.setMargin(rootPane, new Insets(0, 0, 0, 4));
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1280, 800);
        applyAppStyles(scene);
        stage.setTitle("DockingPane Demo");
        stage.setScene(scene);
        stage.show();

        status("Drag tabs between groups or windows. Use Float Active to demonstrate a floating pane.");
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
        Button settings = new Button("", createIcon(MaterialDesign.MDI_SETTINGS));
        settings.setAccessibleText("Settings");
        settings.getStyleClass().add("docking-demo-header-button");
        Label user = new Label("DI");
        user.getStyleClass().add("docking-demo-user");

        HBox header = new HBox(10, menu, brand, spacer, search, settings, user);
        header.getStyleClass().add("docking-demo-header");
        header.setAlignment(Pos.CENTER_LEFT);
        return header;
    }

    private VBox createNavigationRail() {
        VBox rail = new VBox(
                createNavigationButton(MaterialDesign.MDI_FOLDER, "Navigator"),
                createNavigationButton(MaterialDesign.MDI_CODE_TAGS, "Editor"),
                createNavigationButton(MaterialDesign.MDI_EYE, "Preview"),
                createNavigationButton(MaterialDesign.MDI_CONSOLE, "Console"),
                createNavigationButton(MaterialDesign.MDI_SETTINGS, "Inspector"));
        rail.getStyleClass().add("docking-demo-navigation");
        rail.setAlignment(Pos.TOP_CENTER);
        return rail;
    }

    private Button createNavigationButton(Ikon icon, String description) {
        Button button = new Button("", createIcon(icon));
        button.getStyleClass().add("docking-demo-navigation-button");
        button.setAccessibleText(description);
        button.setTooltip(new Tooltip(description));
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
        for (String id : List.of("map", "charts", "city")) {
            MenuItem item = new MenuItem(switch (id) {
                case "map" -> "Map";
                case "charts" -> "Financial Charts";
                default -> "City";
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

        HBox toolbar = new HBox(8, openSearch, openTasks, openView, floatActive, snapshot, restore);
        toolbar.getStyleClass().add("docking-demo-toolbar");
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(8, 12, 8, 12));
        return toolbar;
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
        rootPane.updateDividerPosition(List.of(), 0.38);
        rootPane.updateDividerPosition(List.of(1), 0.62);
    }

    private Stage createFloatingStage(Stage owner, DockingPane pane) {
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
        templates.put("editor", id -> createTextDockable(id, "Editor", MaterialDesign.MDI_CODE_TAGS,
                "public class Example {\n    // edit me\n}\n"));
        templates.put("preview", id -> createTextDockable(id, "Preview", MaterialDesign.MDI_EYE,
                "# Preview\n\nThe selected content appears here."));
        templates.put("console", id -> createTextDockable(id, "Console", MaterialDesign.MDI_CONSOLE,
                "Build started...\nTests running...\nDone."));
        templates.put("navigator", id -> createListDockable(id, "Navigator", MaterialDesign.MDI_FOLDER,
                "src", "resources", "docs", "build"));
        templates.put("inspector", id -> createListDockable(id, "Inspector", MaterialDesign.MDI_SETTINGS,
                "Properties", "Layout", "Accessibility"));
        templates.put("search", id -> createTextDockable(id, "Search", MaterialDesign.MDI_MAGNIFY,
                "Search results\n- DockingContext\n- DockingPane\n- DockingLayout"));
        templates.put("tasks", id -> createListDockable(id, "Tasks", MaterialDesign.MDI_CHECKBOX_MARKED_OUTLINE,
                "TODO: wire toolbar", "TODO: refresh preview", "DONE: snapshot layout"));
        templates.put("map", this::createMapDockable);
        templates.put("charts", this::createChartsDockable);
        templates.put("city", this::createCityDockable);
        return templates;
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
