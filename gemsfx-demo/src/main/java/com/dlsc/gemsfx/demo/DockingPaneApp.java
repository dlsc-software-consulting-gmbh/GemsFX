package com.dlsc.gemsfx.demo;

import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockableFactory;
import com.dlsc.gemsfx.DockingContext;
import com.dlsc.gemsfx.DockingLayout;
import com.dlsc.gemsfx.DockingPane;
import com.dlsc.gemsfx.DockingPosition;
import com.dlsc.gemsfx.DockingRestoreResult;
import com.dlsc.gemsfx.DockingStageFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
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
        openInitialLayout(context, rootPane);

        statusLabel = new Label("Drag tabs to reorder, split, or move between the main and floating panes.");
        statusLabel.setWrapText(true);

        BorderPane root = new BorderPane(rootPane);
        root.setTop(createToolbar(stage, context, rootPane));
        root.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(8, 12, 10, 12));

        Scene scene = new Scene(root, 1100, 760);
        stage.setTitle("DockingPane Demo");
        stage.setScene(scene);
        stage.show();

        context.floatDockable("inspector", stage.getX() + stage.getWidth() + 20, stage.getY() + 80);
        status("Opened app-created floating stage with 'Inspector'. Drag tabs between windows.");
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

        HBox toolbar = new HBox(8, openSearch, openTasks, floatActive, snapshot, restore);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12));
        toolbar.setStyle("-fx-background-color: -fx-box-border, -fx-control-inner-background; -fx-background-insets: 0, 0 0 1 0;");
        return toolbar;
    }

    private void openInitialLayout(DockingContext context, DockingPane rootPane) {
        context.openDockable(rootPane, "editor", DockingPosition.CENTER);
        context.openDockable(rootPane, "preview", DockingPosition.RIGHT);
        context.openDockable(rootPane, "console", DockingPosition.BOTTOM);
        context.openDockable(rootPane, "navigator", DockingPosition.LEFT);
        context.openDockable(rootPane, "inspector", DockingPosition.CENTER);
    }

    private Stage createFloatingStage(Stage owner, DockingPane pane) {
        Label hint = new Label("Cross-pane move: drag tabs between this window and the main DockingPane.");
        hint.setWrapText(true);

        BorderPane wrapper = new BorderPane(pane);
        wrapper.setBottom(hint);
        BorderPane.setMargin(hint, new Insets(8));

        Stage floating = new Stage();
        floating.initOwner(owner);
        floating.setTitle("DockingPane Floating Stage");
        floating.setScene(new Scene(wrapper, 420, 280));
        floating.show();
        return floating;
    }

    private Map<String, Function<String, Dockable>> createTemplates() {
        Map<String, Function<String, Dockable>> templates = new LinkedHashMap<>();
        templates.put("editor", id -> createTextDockable(id, "Editor", "public class Example {\n    // edit me\n}\n"));
        templates.put("preview", id -> createTextDockable(id, "Preview", "# Preview\n\nThe selected content appears here."));
        templates.put("console", id -> createTextDockable(id, "Console", "Build started...\nTests running...\nDone."));
        templates.put("navigator", id -> createListDockable(id, "Navigator", "src", "resources", "docs", "build"));
        templates.put("inspector", id -> createListDockable(id, "Inspector", "Properties", "Layout", "Accessibility"));
        templates.put("search", id -> createTextDockable(id, "Search", "Search results\n- DockingContext\n- DockingPane\n- DockingLayout"));
        templates.put("tasks", id -> createListDockable(id, "Tasks", "TODO: wire toolbar", "TODO: refresh preview", "DONE: snapshot layout"));
        return templates;
    }

    private Dockable createTextDockable(String id, String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setWrapText(true);
        textArea.setPrefRowCount(8);
        return new Dockable(id, title, textArea);
    }

    private Dockable createListDockable(String id, String title, String... values) {
        ListView<String> listView = new ListView<>();
        listView.getItems().setAll(values);

        StackPane container = new StackPane(listView);
        container.setPadding(new Insets(8));
        return new Dockable(id, title, container);
    }

    private void status(String text) {
        statusLabel.setText(text);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
