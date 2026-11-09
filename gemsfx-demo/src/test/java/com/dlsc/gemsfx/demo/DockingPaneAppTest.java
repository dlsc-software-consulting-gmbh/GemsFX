package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordLight;
import com.dlsc.gemsfx.Dockable;
import com.dlsc.gemsfx.DockingContext;
import com.dlsc.gemsfx.DockingLayout;
import com.dlsc.gemsfx.DockingLayoutNode;
import com.dlsc.gemsfx.DockingPane;
import com.dlsc.gemsfx.DockingPosition;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
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
            StackPane root = new StackPane(pane);
            root.getStyleClass().add("docking-demo-app");
            root.setStyle("-color-success-emphasis: #123456; -color-danger-emphasis: #ab3456;"
                    + " -color-fg-default: #654321; -color-fg-emphasis: #abcdef;");
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
