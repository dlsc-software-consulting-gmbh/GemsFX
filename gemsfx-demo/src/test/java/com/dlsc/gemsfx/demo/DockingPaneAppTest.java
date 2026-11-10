package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordLight;
import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingContext;
import com.dlsc.gemsfx.docking.DockingLayout;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingPosition;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Window;
import org.kordamp.ikonli.javafx.FontIcon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DockingPaneAppTest {

    @BeforeAll
    static void startToolkit() throws InterruptedException {
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(10, TimeUnit.SECONDS));
        Platform.setImplicitExit(false);
    }

    @Test
    public void editorMenuAndToolbarToggleWrappingIndependentlyForEachView() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            Dockable editor = app.createDockable("editor").orElseThrow();
            Dockable copy = app.createSidebarDockable("editor");
            TextArea text = (TextArea) editor.getContent();
            CheckBox wrap = (CheckBox) editor.getToolbar();
            assertNotNull(wrap);
            assertEquals("Wrap text", wrap.getAccessibleText());
            assertTrue(!editor.getMenuItems().isEmpty(), "Editor should offer a wrapping action");
            CheckMenuItem wrapItem = (CheckMenuItem) editor.getMenuItems().get(0);
            boolean initialWrap = text.isWrapText();
            assertEquals(initialWrap, wrapItem.isSelected());
            wrap.fire();
            assertNotEquals(initialWrap, text.isWrapText());
            assertEquals(text.isWrapText(), wrapItem.isSelected());
            assertEquals(initialWrap, ((TextArea) copy.getContent()).isWrapText());
            wrap.fire();
            assertEquals(initialWrap, text.isWrapText());
            wrapItem.setSelected(!initialWrap);
            assertEquals(!initialWrap, text.isWrapText());
            assertEquals(!initialWrap, wrap.isSelected());
            assertEquals(initialWrap, ((TextArea) copy.getContent()).isWrapText());
            text.setWrapText(initialWrap);
            assertEquals(initialWrap, wrapItem.isSelected());
            assertEquals(initialWrap, wrap.isSelected());
        });
    }

    @Test
    public void consoleMenuAndToolbarClearOnlyTheirOwnOutput() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            Dockable console = app.createDockable("console").orElseThrow();
            Dockable copy = app.createSidebarDockable("console");
            TextArea output = (TextArea) console.getContent().lookup(".text-area");
            TextArea otherOutput = (TextArea) copy.getContent().lookup(".text-area");
            String original = otherOutput.getText();
            assertTrue(!output.getText().isEmpty());
            Button clear = (Button) console.getToolbar();
            assertNotNull(clear);
            assertEquals("Clear output", clear.getAccessibleText());
            clear.fire();
            assertEquals("", output.getText());
            assertEquals(original, otherOutput.getText());
            output.setText("[INFO] More output");
            assertTrue(!console.getMenuItems().isEmpty(), "Console should offer a clear action");
            console.getMenuItems().get(0).fire();
            assertEquals("", output.getText());
            assertEquals(original, otherOutput.getText());
        });
    }

    @Test
    public void navigatorMenuAndToolbarExpandAndCollapseTheWholeTree() throws Exception {
        runFx(() -> {
            Dockable navigator = new DockingPaneApp().createDockable("navigator").orElseThrow();
            TreeView<?> tree = (TreeView<?>) navigator.getContent();
            HBox toolbar = (HBox) navigator.getToolbar();
            assertNotNull(toolbar);
            Button expand = (Button) toolbar.getChildren().get(0);
            Button collapse = (Button) toolbar.getChildren().get(1);
            assertEquals("Expand all", expand.getAccessibleText());
            assertEquals("Collapse all", collapse.getAccessibleText());
            expand.fire();
            assertTrue(tree.getRoot().isExpanded());
            tree.getRoot().getChildren().forEach(item -> assertTrue(item.isExpanded()));
            collapse.fire();
            assertTrue(!tree.getRoot().isExpanded());
            tree.getRoot().getChildren().forEach(item -> assertTrue(!item.isExpanded()));
            assertTrue(!navigator.getMenuItems().isEmpty(), "Navigator should offer tree actions");
            navigator.getMenuItems().get(0).fire();
            assertTrue(tree.getRoot().isExpanded());
            tree.getRoot().getChildren().forEach(item -> assertTrue(item.isExpanded()));
            navigator.getMenuItems().get(1).fire();
            assertTrue(!tree.getRoot().isExpanded());
            tree.getRoot().getChildren().forEach(item -> assertTrue(!item.isExpanded()));
        });
    }

    @Test
    public void repeatedSidebarDropsCreateIndependentViewsThatCanBeRestored() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            for (String type : List.of("navigator", "editor", "preview", "console", "inspector")) {
                Dockable first = app.createSidebarDockable(type);
                Dockable second = app.createSidebarDockable(type);
                Dockable restored = app.createDockable(first.getId()).orElseThrow();

                assertNotEquals(first.getId(), second.getId());
                assertNotSame(first.getContent(), second.getContent());
                assertEquals(first.getId(), restored.getId());
                assertEquals(first.getTitle(), restored.getTitle());
                assertNotSame(first.getContent(), restored.getContent());
            }
            assertNull(app.createSidebarDockable("unknown"));
            assertNull(app.createSidebarDockable("drag"));
        });
    }

    @Test
    public void demoMixesOptionalHelpCallbacksAndShowsHelpForTheCurrentTitle() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            Dockable withHelp = null;
            Dockable withoutHelp = null;
            for (int i = 0; i < 128 && (withHelp == null || withoutHelp == null); i++) {
                Dockable dockable = app.createDockable("search:" + i).orElseThrow();
                if (dockable.getOnHelp() == null) {
                    withoutHelp = dockable;
                } else {
                    withHelp = dockable;
                }
            }
            assertNotNull(withHelp, "Some demo dockables should offer help");
            assertNotNull(withoutHelp, "Some demo dockables should omit help");
            withHelp.setTitle("Renamed Search");
            List<Window> previousWindows = List.copyOf(Window.getWindows());
            try {
                withHelp.getOnHelp().run();
                Window helpWindow = Window.getWindows().stream()
                        .filter(window -> !previousWindows.contains(window)).findFirst().orElseThrow();
                DialogPane dialog = (DialogPane) helpWindow.getScene().getRoot().lookup(".dialog-pane");
                assertEquals("Help for Renamed Search", dialog.getHeaderText());
                assertTrue(dialog.getContentText().contains("Drag"));
            } finally {
                List.copyOf(Window.getWindows()).stream()
                        .filter(window -> !previousWindows.contains(window)).forEach(Window::hide);
            }
        });
    }

    @Test
    public void statusBarFollowsActiveDockableSelectionTitleAndClosing() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            DockingContext context = new DockingContext(
                    id -> Optional.of(new Dockable(id, id, new Group())), pane -> null);
            DockingPane pane = context.createPane();
            HBox statusBar = app.createStatusBar(pane);
            Label active = (Label) statusBar.getChildren().getLast();
            assertEquals("Active: None", active.getText());

            Dockable first = context.openDockable(pane, "Editor", DockingPosition.CENTER).orElseThrow();
            assertEquals("Active: Editor", active.getText());
            Dockable second = context.openDockable(pane, "Console", DockingPosition.RIGHT).orElseThrow();
            assertEquals("Active: Console", active.getText());
            pane.selectDockable(first.getId());
            first.setTitle("Mission Control");
            second.setTitle("Launch Log");
            assertEquals("Active: Mission Control", active.getText());

            context.requestCloseDockable(first);
            assertEquals("Active: Launch Log", active.getText());
            context.requestCloseDockable(second);
            assertEquals("Active: None", active.getText());
        });
    }

    @Test
    public void layoutJsonIncludesSplitsSelectionsFloatingBoundsAndEmptyPanes() throws Exception {
        DockingLayout layout = new DockingLayout("main", List.of(
                new DockingLayout.PaneLayout("main",
                        new DockingLayoutNode.Split(Orientation.HORIZONTAL, 0.35,
                                new DockingLayoutNode.Tabs(List.of("editor", "console"), "console"),
                                new DockingLayoutNode.Tabs(List.of("preview"), "preview")), null),
                new DockingLayout.PaneLayout("floating",
                        new DockingLayoutNode.Tabs(List.of("inspector"), "inspector"),
                        new DockingLayout.StageBounds(100, 200, 640, 480)),
                new DockingLayout.PaneLayout("empty", new DockingLayoutNode.Empty(), null)));

        String json = DockingPaneApp.layoutToJson(layout);
        JsonNode parsed = new ObjectMapper().readTree(json);

        assertTrue(json.contains("\n"));
        assertEquals("main", parsed.path("primaryPaneId").asText());
        assertEquals(3, parsed.path("panes").size());
        JsonNode main = parsed.path("panes").get(0).path("root");
        assertEquals("HORIZONTAL", main.path("orientation").asText());
        assertEquals(0.35, main.path("dividerPosition").asDouble());
        assertEquals("console", main.path("first").path("selectedId").asText());
        assertEquals(2, main.path("first").path("dockableIds").size());
        assertEquals("preview", main.path("second").path("selectedId").asText());
        JsonNode floating = parsed.path("panes").get(1);
        assertEquals("inspector", floating.path("root").path("selectedId").asText());
        assertEquals(640, floating.path("stageBounds").path("width").asInt());
        assertTrue(parsed.path("panes").get(2).path("root").isObject());
        assertTrue(parsed.path("panes").get(2).path("root").isEmpty());
    }

    private static void runFx(Runnable action) throws Exception {
        CompletableFuture<Void> finished = new CompletableFuture<>();
        Platform.runLater(() -> {
            try {
                action.run();
                finished.complete(null);
            } catch (Throwable ex) {
                finished.completeExceptionally(ex);
            }
        });
        finished.get(10, TimeUnit.SECONDS);
    }

    @Test
    public void focusBorderUsesAccentColorWithoutRecoloringOtherGroups() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            DockingContext context = new DockingContext(app::createDockable, pane -> null);
            DockingPane pane = context.createPane();
            pane.setShowFocusBorder(true);
            context.openDockable(pane, "editor", DockingPosition.CENTER);
            context.openDockable(pane, "inspector", DockingPosition.RIGHT);
            StackPane root = new StackPane(pane);
            root.getStyleClass().add("docking-demo-app");
            root.setStyle("-color-accent-emphasis: #12ab34; -color-neutral-emphasis-plus: #654321;");
            root.getStylesheets().add(DockingPaneApp.class.getResource("docking-pane-app-atlantafx.css").toExternalForm());
            Scene scene = new Scene(root, 800, 600);
            scene.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            GemsFXAtlantaFX.applyTo(scene);
            root.applyCss();
            root.layout();

            var groups = pane.lookupAll(".docking-tab-pane");
            assertEquals(2, groups.size());
            var highlighted = groups.iterator().next();
            highlighted.pseudoClassStateChanged(PseudoClass.getPseudoClass("focus-highlighted"), true);
            root.applyCss();

            for (var group : groups) {
                assertEquals(Color.web(group == highlighted ? "#12ab34" : "#654321"),
                        ((Region) group).getBorder().getStrokes().getFirst().getTopStroke());
                assertEquals(1, ((Region) group).getBorder().getStrokes().getFirst().getWidths().getTop());
            }
        });
    }

    @Test
    public void rocketAndTargetContentColorsDoNotOverrideTabHeaderColors() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            DockingContext context = new DockingContext(app::createDockable, pane -> null);
            DockingPane pane = context.createPane();
            Dockable drag = context.openDockable(pane, "drag", DockingPosition.CENTER).orElseThrow();
            Dockable drop = context.openDockable(pane, "drop", DockingPosition.CENTER).orElseThrow();
            drag.getMenuItems().add(new MenuItem("Inspect rocket"));
            drop.getMenuItems().add(new MenuItem("Inspect target"));
            StackPane root = new StackPane(pane);
            root.getStyleClass().add("docking-demo-app");
            root.setStyle("-color-success-emphasis: #123456; -color-danger-emphasis: #ab3456;"
                    + " -color-fg-default: #654321; -color-fg-emphasis: #abcdef; -color-neutral-emphasis: #102030;");
            root.getStylesheets().add(DockingPaneApp.class.getResource("docking-pane-app-atlantafx.css").toExternalForm());
            Scene scene = new Scene(root, 800, 600);
            scene.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            GemsFXAtlantaFX.applyTo(scene);

            for (String id : List.of("drag", "drop")) {
                pane.selectDockable(id);
                root.applyCss();
                root.layout();
                assertEquals(Color.web(id.equals("drag") ? "#abcdef" : "#654321"),
                        ((FontIcon) drag.getGraphic()).getIconColor());
                assertEquals(Color.web(id.equals("drop") ? "#abcdef" : "#654321"),
                        ((FontIcon) drop.getGraphic()).getIconColor());
                Region closeIcon = (Region) pane.lookup(".docking-tab.selected .docking-tab-close-button .icon");
                assertEquals(Color.web("#abcdef"), closeIcon.getBackground().getFills().getFirst().getFill());
                MenuButton menu = (MenuButton) pane.lookup(".docking-tab.selected .docking-tab-menu-button");
                for (boolean showing : List.of(false, true)) {
                    menu.pseudoClassStateChanged(PseudoClass.getPseudoClass("showing"), showing);
                    root.applyCss();
                    assertEquals(Color.web("#abcdef"), ((FontIcon) menu.getGraphic()).getIconColor());
                    assertEquals(0, ((Region) menu.lookup(".arrow-button")).prefWidth(-1), 0);
                    menu.getBackground().getFills().forEach(fill -> assertEquals(Color.TRANSPARENT, fill.getFill()));
                    if (menu.getBorder() != null) {
                        menu.getBorder().getStrokes().forEach(stroke ->
                                assertEquals(Color.TRANSPARENT, stroke.getTopStroke()));
                    }
                }
                Dockable selected = id.equals("drag") ? drag : drop;
                FontIcon contentIcon = (FontIcon) selected.getContent().lookup(id.equals("drag")
                        ? ".docking-demo-drag-graphic" : ".docking-demo-target-graphic");
                assertEquals(Color.web(id.equals("drag") ? "#123456" : "#ab3456"), contentIcon.getIconColor());
            }
        });
    }

    @Test
    public void successfulContentDropSwapsRocketInstructionsTitlesAndIconsInBothDirections() {
        Group rocketAndInstructions = new Group();
        Group dropInstructions = new Group();
        Group sourceIcon = new Group();
        Group targetIcon = new Group();
        StackPane source = new StackPane(rocketAndInstructions);
        StackPane target = new StackPane(dropInstructions);
        Dockable drag = new Dockable("drag", "Rocket Source", sourceIcon, source);
        Dockable drop = new Dockable("drop", "Rocket Target", targetIcon, target);

        DockingPaneApp.swapContentsAndHeaders(drag, drop);

        assertEquals("Rocket Target", drag.getTitle());
        assertEquals("Rocket Source", drop.getTitle());
        assertSame(targetIcon, drag.getGraphic());
        assertSame(sourceIcon, drop.getGraphic());
        assertSame(dropInstructions, source.getChildren().getFirst());
        assertSame(rocketAndInstructions, target.getChildren().getFirst());

        DockingPaneApp.swapContentsAndHeaders(drop, drag);

        assertEquals("Rocket Source", drag.getTitle());
        assertEquals("Rocket Target", drop.getTitle());
        assertSame(sourceIcon, drag.getGraphic());
        assertSame(targetIcon, drop.getGraphic());
        assertSame(rocketAndInstructions, source.getChildren().getFirst());
        assertSame(dropInstructions, target.getChildren().getFirst());
    }
}
