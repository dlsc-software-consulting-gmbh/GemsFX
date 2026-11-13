package com.dlsc.gemsfx.demo;

import atlantafx.base.theme.NordLight;
import com.dlsc.gemsfx.BeforeAfterView;
import com.dlsc.gemsfx.docking.Dockable;
import com.dlsc.gemsfx.docking.DockingContext;
import com.dlsc.gemsfx.docking.DockingLayout;
import com.dlsc.gemsfx.docking.DockingLayoutNode;
import com.dlsc.gemsfx.docking.DockingPane;
import com.dlsc.gemsfx.docking.DockingPosition;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.kordamp.ikonli.javafx.FontIcon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
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
    public void dockingEventsArePrintedToStandardOutput() throws Exception {
        runFx(() -> {
            PrintStream previousOut = System.out;
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Stage stage = new Stage();
            try (PrintStream captured = new PrintStream(output, true, StandardCharsets.UTF_8)) {
                System.setOut(captured);
                new DockingPaneApp().start(stage);
                DockingPane pane = (DockingPane) stage.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                Dockable dockable = context.openDockable(pane, "editor:logging", DockingPosition.CENTER).orElseThrow();
                assertTrue(pane.moveDockable(dockable.getId(), "navigator", DockingPosition.LEFT));
                assertTrue(context.requestCloseDockable(dockable));

                String text = output.toString(StandardCharsets.UTF_8);
                assertTrue(text.lines().anyMatch(line -> line.contains("DOCKABLE_OPENED") && line.contains("editor:logging")), text);
                assertTrue(text.lines().anyMatch(line -> line.contains("DOCKABLE_MOVED") && line.contains("editor:logging")), text);
                assertTrue(text.lines().anyMatch(line -> line.contains("DOCKABLE_CLOSED") && line.contains("editor:logging")), text);
                assertTrue(text.lines().anyMatch(line -> line.contains("Docking selection:") && line.contains("editor:logging")));
            } finally {
                System.setOut(previousOut);
                stage.hide();
            }
        });
    }

    @Test
    public void floatingToggleControlsDragFloating() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                var root = stage.getScene().getRoot();
                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                ToggleButton floating = root.lookupAll(".toggle-button").stream()
                        .filter(ToggleButton.class::isInstance).map(ToggleButton.class::cast)
                        .filter(box -> "Allow Drag to Float".equals(box.getText())).findFirst().orElse(null);
                assertNotNull(floating, "The demo must provide a drag-to-float toggle button");
                assertTrue(floating.isSelected());
                assertTrue(context.isFloatingEnabled());

                floating.fire();
                assertFalse(context.isFloatingEnabled());
                context.setFloatingEnabled(true);
                assertTrue(floating.isSelected());
                context.setFloatingEnabled(false);
                assertFalse(floating.isSelected());
                floating.fire();
                assertTrue(context.isFloatingEnabled());

                root.applyCss();
                root.layout();
                assertTrue(floating.localToScene(floating.getBoundsInLocal()).getMaxX() <= stage.getScene().getWidth(),
                        "The toggle button must be visible at the demo's default window size");
                Button inspector = root.lookupAll(".button").stream()
                        .filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> "Events & Selections".equals(button.getText())).findFirst().orElseThrow();
                assertTrue(inspector.localToScene(inspector.getBoundsInLocal()).getMaxX() <= stage.getScene().getWidth(),
                        "Existing toolbar buttons must remain visible");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void applicationGroupMenuCanFloatInternallyAndRedock() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                var root = stage.getScene().getRoot();
                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                pane.selectDockable("editor");
                MenuButton menu = root.lookupAll(".docking-tab-pane-menu-button").stream()
                        .map(MenuButton.class::cast)
                        .filter(button -> button.getItems().stream()
                                .anyMatch(item -> item.getStyleClass().contains("docking-group-float-internally")))
                        .findFirst().orElseThrow();
                assertFalse(menu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-dock")));
                menu.getItems().stream().filter(item -> item.getStyleClass().contains("docking-group-float-internally"))
                        .findFirst().orElseThrow().fire();
                assertEquals(1, pane.getFloatingGroups().size());
                String floatingId = pane.getFloatingGroups().get(0).getTabs().getDockableIds().get(0);
                root.applyCss();
                root.layout();
                MenuButton floatingMenu = (MenuButton) pane.lookup(".docking-floating-frame")
                        .lookup(".docking-tab-pane-menu-button");
                assertFalse(floatingMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-tab-float-internally")
                                || item.getStyleClass().contains("docking-group-float-internally")
                                || item.getStyleClass().contains("docking-group-maximize")));
                assertTrue(floatingMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-float")));
                assertTrue(floatingMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-close-all")));
                floatingMenu.getItems().stream().filter(item -> item.getStyleClass().contains("docking-group-dock"))
                        .findFirst().orElseThrow().fire();
                assertTrue(pane.getFloatingGroups().isEmpty());
                root.applyCss();
                root.layout();
                var redockedTab = pane.lookupAll(".docking-tab").stream()
                        .filter(tab -> floatingId.equals(tab.getProperties().get("docking.dockable-id")))
                        .findFirst().orElseThrow();
                MenuButton redockedMenu = (MenuButton) redockedTab.getParent().lookup(".docking-tab-pane-menu-button");
                assertTrue(redockedMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-float-internally")));
                assertTrue(redockedMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-maximize") && item.isVisible()));
                assertFalse(redockedMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-dock")));

                DockingPane windowPane = pane.getDockingContext().floatTabGroup(
                        floatingId, stage.getX() + 40, stage.getY() + 40);
                windowPane.getScene().getRoot().applyCss();
                windowPane.getScene().getRoot().layout();
                MenuButton windowMenu = (MenuButton) windowPane.lookup(".docking-tab-pane-menu-button");
                windowMenu.getItems().stream().filter(item -> item.getStyleClass().contains("docking-group-float-internally"))
                        .findFirst().orElseThrow().fire();
                assertTrue(windowPane.isDockableFloating(floatingId));
                List<Dockable> windowDockables = List.copyOf(windowPane.getDockables());
                windowPane.getScene().getRoot().applyCss();
                windowPane.getScene().getRoot().layout();
                MenuButton windowFloatingMenu = (MenuButton) windowPane.lookup(".docking-floating-frame")
                        .lookup(".docking-tab-pane-menu-button");
                windowFloatingMenu.getItems().stream().filter(item -> item.getStyleClass().contains("docking-group-dock"))
                        .findFirst().orElseThrow().fire();
                assertTrue(windowPane.getFloatingGroups().isEmpty());
                for (Dockable dockable : windowDockables) {
                    assertSame(windowPane, dockable.getDockingPane());
                }
                assertTrue(windowPane.getScene().getWindow().isShowing());
                windowPane.getScene().getRoot().applyCss();
                windowPane.getScene().getRoot().layout();
                windowMenu = (MenuButton) windowPane.lookup(".docking-tab-pane-menu-button");
                assertFalse(windowMenu.getItems().stream()
                        .anyMatch(item -> item.getStyleClass().contains("docking-group-dock")
                                || "Dock Group to Main Pane".equals(item.getText())));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void floatTabInternallyUsesGroupSelectionEvenWhenAnotherGroupIsActive() throws Exception {
        runFx(() -> {
            for (boolean activateOtherGroup : new boolean[]{false, true}) {
                Stage stage = new Stage();
                try {
                    new DockingPaneApp().start(stage);
                    Parent root = stage.getScene().getRoot();
                    DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                    pane.selectDockable("map");
                    if (activateOtherGroup) {
                        pane.selectDockable("preview");
                    }
                    root.applyCss();
                    root.layout();
                    var editorTab = pane.lookupAll(".docking-tab").stream()
                            .filter(tab -> "editor".equals(tab.getProperties().get("docking.dockable-id")))
                            .findFirst().orElseThrow();
                    MenuButton menu = (MenuButton) editorTab.getParent().lookup(".docking-tab-pane-menu-button");
                    menu.getItems().stream().filter(item -> item.getStyleClass().contains("docking-tab-float-internally"))
                            .findFirst().orElseThrow().fire();

                    assertEquals(1, pane.getFloatingGroups().size());
                    assertEquals(List.of("map"), pane.getFloatingGroups().get(0).getTabs().getDockableIds());
                    assertFalse(pane.isDockableFloating("editor"));
                    assertFalse(pane.isDockableFloating("preview"));
                } finally {
                    stage.hide();
                }
            }
        });
    }

    @Test
    public void groupMenuToggleDisablesAndRestoresTheDefaultFactory() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                Parent root = stage.getScene().getRoot();
                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                var factory = context.getTabPaneHeaderFactory();
                ToggleButton toggle = root.lookupAll(".toggle-button").stream()
                        .filter(ToggleButton.class::isInstance).map(ToggleButton.class::cast)
                        .filter(button -> "Show Group Menu".equals(button.getText())).findFirst().orElse(null);
                assertNotNull(toggle);
                assertTrue(toggle.isSelected());
                assertNotNull(factory);
                assertNotNull(pane.lookup(".docking-tab-pane-menu-button"));
                assertTrue(toggle.localToScene(toggle.getBoundsInLocal()).getMaxX() <= stage.getScene().getWidth(),
                        "The group menu toggle must be visible at the demo's default window size");

                toggle.fire();
                assertNull(context.getTabPaneHeaderFactory());
                root.applyCss();
                root.layout();
                assertNull(pane.lookup(".docking-tab-pane-menu-button"));
                toggle.fire();
                assertSame(factory, context.getTabPaneHeaderFactory());
                root.applyCss();
                root.layout();
                assertNotNull(pane.lookup(".docking-tab-pane-menu-button"));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void videoDockableCanBeOpenedAndClosed() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            DockingContext context = new DockingContext(app::createDockable, pane -> null);
            DockingPane pane = context.createPane();
            Dockable video = context.openDockable(pane, "video", DockingPosition.CENTER).orElseThrow();
            assertEquals("Video", video.getTitle());
            DockingLayout layout = context.getLayout();
            assertTrue(context.requestCloseDockable(video));
            DockingContext restored = new DockingContext(app::createDockable, floating -> null);
            assertTrue(restored.restoreLayout(layout).getUnresolvedDockableIds().isEmpty());
            Dockable restoredVideo = restored.getDockables().stream()
                    .filter(item -> "video".equals(item.getId())).findFirst().orElseThrow();
            assertTrue(restored.requestCloseDockable(restoredVideo));
        });
    }

    @Test
    public void videoIsInitiallyAvailableAndCanBeReopenedFromMenu() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                Parent root = stage.getScene().getRoot();
                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                Dockable video = context.getDockables().stream()
                        .filter(item -> "video".equals(item.getId())).findFirst().orElseThrow();
                assertTrue(context.requestCloseDockable(video));
                MenuButton openView = root.lookupAll(".menu-button").stream()
                        .filter(MenuButton.class::isInstance).map(MenuButton.class::cast)
                        .filter(menu -> "Open View".equals(menu.getText())).findFirst().orElseThrow();
                openView.getItems().stream().filter(item -> "Video".equals(item.getText()))
                        .findFirst().orElseThrow().fire();
                Dockable reopened = context.getDockables().stream()
                        .filter(item -> "video".equals(item.getId())).findFirst().orElseThrow();
                assertNotSame(video, reopened);
                assertTrue(context.requestCloseDockable(reopened));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void bundledVideoPlaysPausesAndReleasesItsPlayer() throws Exception {
        CompletableFuture<Void> ready = new CompletableFuture<>();
        CompletableFuture<Void> playing = new CompletableFuture<>();
        CompletableFuture<Void> paused = new CompletableFuture<>();
        AtomicReference<MediaPlayer> playerRef = new AtomicReference<>();
        AtomicReference<Button> buttonRef = new AtomicReference<>();
        AtomicReference<DockingContext> contextRef = new AtomicReference<>();
        AtomicReference<Dockable> dockableRef = new AtomicReference<>();
        try {
            runFx(() -> {
                DockingPaneApp app = new DockingPaneApp();
                DockingContext context = new DockingContext(app::createDockable, pane -> null);
                DockingPane pane = context.createPane();
                Dockable dockable = context.openDockable(pane, "video", DockingPosition.CENTER).orElseThrow();
                contextRef.set(context);
                dockableRef.set(dockable);
                BorderPane content = assertInstanceOf(BorderPane.class, dockable.getContent());
                StackPane videoPane = assertInstanceOf(StackPane.class, content.getCenter());
                MediaView view = assertInstanceOf(MediaView.class, videoPane.getChildren().get(0));
                MediaPlayer player = view.getMediaPlayer();
                playerRef.set(player);
                Button button = assertInstanceOf(Button.class,
                        assertInstanceOf(HBox.class, content.getBottom()).getChildren().get(0));
                buttonRef.set(button);
                player.setOnReady(() -> ready.complete(null));
                player.setOnPlaying(() -> playing.complete(null));
                player.setOnPaused(() -> paused.complete(null));
                player.setOnError(() -> ready.completeExceptionally(player.getError()));
                new Scene(content, 640, 360);
                content.applyCss();
                content.layout();
                assertTrue(videoPane.getWidth() > 0);
                assertTrue(videoPane.getHeight() > 0);
                assertEquals(videoPane.getWidth(), view.getFitWidth(), 0.1);
                assertEquals(videoPane.getHeight(), view.getFitHeight(), 0.1);
            });
            ready.get(10, TimeUnit.SECONDS);
            runFx(() -> {
                assertTrue(playerRef.get().getMedia().getWidth() > 0);
                buttonRef.get().fire();
            });
            playing.get(10, TimeUnit.SECONDS);
            runFx(() -> {
                assertEquals("Pause", buttonRef.get().getText());
                buttonRef.get().fire();
            });
            paused.get(10, TimeUnit.SECONDS);
            runFx(() -> assertEquals("Play", buttonRef.get().getText()));
        } finally {
            if (contextRef.get() != null && dockableRef.get() != null) {
                runFx(() -> {
                    assertTrue(contextRef.get().requestCloseDockable(dockableRef.get()));
                    assertEquals(MediaPlayer.Status.DISPOSED, playerRef.get().getStatus());
                });
            }
        }
    }

    @Test
    public void cityDockableShowsResizableBerlinBeforeAfterImages() throws Exception {
        runFx(() -> {
            Dockable city = new DockingPaneApp().createDockable("city").orElseThrow();
            BeforeAfterView comparison = assertInstanceOf(BeforeAfterView.class, city.getContent());
            ImageView before = assertInstanceOf(ImageView.class, comparison.getBefore());
            ImageView after = assertInstanceOf(ImageView.class, comparison.getAfter());
            assertTrue(before.getImage().getUrl().endsWith("/berlin/before1.png"));
            assertTrue(after.getImage().getUrl().endsWith("/berlin/after1.png"));
            assertFalse(before.getImage().isError());
            assertFalse(after.getImage().isError());

            StackPane root = new StackPane(comparison);
            new Scene(root, 360, 200);
            root.applyCss();
            root.layout();
            assertEquals(360, comparison.getWidth(), 0.1);
            assertEquals(comparison.getWidth(), before.getFitWidth(), 0.1);
            assertEquals(comparison.getHeight(), before.getFitHeight(), 0.1);
            assertEquals(before.getFitWidth(), after.getFitWidth(), 0.1);
            assertEquals(before.getFitHeight(), after.getFitHeight(), 0.1);
            assertTrue(before.isPreserveRatio());
            assertTrue(after.isPreserveRatio());

            root.resize(200, 350);
            root.layout();
            assertEquals(200, comparison.getWidth(), 0.1);
            assertEquals(comparison.getWidth(), before.getFitWidth(), 0.1);
            assertEquals(comparison.getHeight(), after.getFitHeight(), 0.1);
        });
    }

    @Test
    public void inspectorShowsEventHistoryAndLiveSelectionAcrossWindows() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                var root = stage.getScene().getRoot();
                Button inspectorButton = root.lookupAll(".button").stream()
                        .filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> "Events & Selections".equals(button.getText())).findFirst().orElse(null);
                assertNotNull(inspectorButton, "The demo must provide the combined inspector button");
                assertTrue(root.lookupAll(".button").stream()
                        .filter(Button.class::isInstance).map(Button.class::cast)
                        .noneMatch(button -> "Events".equals(button.getText()) || "Selection".equals(button.getText())));

                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                inspectorButton.fire();
                Stage inspector = (Stage) Window.getWindows().stream()
                        .filter(window -> window instanceof Stage candidate && candidate.getOwner() == stage)
                        .findFirst().orElseThrow();
                TabPane tabs = assertInstanceOf(TabPane.class, inspector.getScene().getRoot());
                assertEquals("Events", tabs.getSelectionModel().getSelectedItem().getText());
                Parent eventsTab = assertInstanceOf(Parent.class, tabs.getTabs().get(0).getContent());
                TextArea events = assertInstanceOf(TextArea.class, eventsTab.lookup(".text-area"));
                assertTrue(events.isWrapText(), "Event text must wrap");
                assertFalse(events.isEditable());
                assertTrue(events.getText().lines().anyMatch(line -> line.contains("DOCKABLE_OPENED")
                        && line.contains("editor")), "Initial events must be retained");
                Button clear = eventsTab.lookupAll(".button").stream()
                        .filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> "Clear".equals(button.getText())).findFirst().orElse(null);
                assertNotNull(clear, "The Events tab must provide a Clear button");
                clear.fire();
                assertEquals("", events.getText());

                Dockable opened = context.openDockable(pane, "editor:inspector-test", DockingPosition.CENTER).orElseThrow();
                assertTrue(events.getText().lines().anyMatch(line -> line.contains("DOCKABLE_OPENED")
                        && line.contains(opened.getId())));
                DockingPane floating = context.floatDockable(opened.getId(), 100, 100);
                assertNotNull(floating);
                assertTrue(events.getText().lines().anyMatch(line -> line.contains("DOCKABLE_MOVED")
                        && line.contains(opened.getId())));
                tabs.getSelectionModel().select(1);
                assertEquals("Selection", tabs.getSelectionModel().getSelectedItem().getText());
                ListView<?> selection = assertInstanceOf(ListView.class, tabs.getTabs().get(1).getContent());
                assertTrue(selection.getItems().contains(opened), "Floating selections must be visible");
                assertTrue(selection.getItems().size() > 1, "Selections in other groups must remain visible");
                pane.selectDockable("map");
                assertTrue(selection.getItems().stream().anyMatch(item -> ((Dockable) item).getId().equals("map")));
                assertFalse(selection.getItems().stream().anyMatch(item -> ((Dockable) item).getId().equals("editor")));
                assertTrue(selection.getItems().contains(opened), "Selecting in one pane must preserve floating selections");

                inspector.hide();
                assertTrue(context.requestCloseDockable(opened));
                inspectorButton.fire();
                assertTrue(inspector.isShowing(), "The same inspector must reopen");
                assertEquals("Selection", tabs.getSelectionModel().getSelectedItem().getText());
                assertFalse(selection.getItems().contains(opened));
                assertEquals(1, events.getText().lines().filter(line -> line.contains("DOCKABLE_CLOSED")
                        && line.contains(opened.getId())).count());
                assertFalse(events.getText().lines().anyMatch(line -> line.contains("DOCKABLE_OPENED dockable=editor ")),
                        "Cleared events must not return after reopening");
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void modenaStylesApplicationChromeAndFloatingViewContent() throws Exception {
        runFx(() -> {
            String previousTheme = System.getProperty("atlantafx");
            String previousStylesheet = Application.getUserAgentStylesheet();
            List<Window> previousWindows = List.copyOf(Window.getWindows());
            System.clearProperty("atlantafx");
            try {
                Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                Stage stage = new Stage();
                new DockingPaneApp().start(stage);
                Scene scene = stage.getScene();
                var root = scene.getRoot();
                root.applyCss();
                root.layout();

                Region menuIcon = (Region) root.lookup(".docking-demo-menu-icon");
                assertTrue(menuIcon.getWidth() >= 16, "The application menu must have a visible icon");
                assertNotNull(menuIcon.getBackground());
                Label brand = (Label) root.lookup(".docking-demo-brand");
                assertTrue(brand.getFont().getSize() >= 18, "The header needs its brand typography");
                FontIcon rocket = (FontIcon) root.lookup(".docking-demo-drag-graphic");
                assertTrue(rocket.getIconSize() >= 96, "The draggable rocket should be a large content graphic");
                FontIcon target = (FontIcon) root.lookup(".docking-demo-target-graphic");
                assertNotEquals(rocket.getIconColor(), target.getIconColor());

                DockingPane pane = (DockingPane) root.lookup(".docking-pane");
                Region selected = (Region) pane.lookup(".docking-tab:selected");
                Region unselected = (Region) pane.lookupAll(".docking-tab").stream()
                        .filter(tab -> !tab.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")))
                        .findFirst().orElseThrow();
                assertNotEquals(selected.getBackground().getFills().getFirst().getFill(),
                        unselected.getBackground().getFills().getFirst().getFill());
                Region group = (Region) pane.lookup(".docking-tab-pane");
                assertNotNull(group.getBorder(), "Tab groups should have a visible boundary");

                pane.selectDockable("drag");
                root.lookupAll(".button").stream().filter(Button.class::isInstance).map(Button.class::cast)
                        .filter(button -> button.getText().equals("Float Active")).findFirst().orElseThrow().fire();
                Window floating = Window.getWindows().stream()
                        .filter(window -> window != stage && !previousWindows.contains(window))
                        .findFirst().orElseThrow();
                var floatingRoot = floating.getScene().getRoot();
                floatingRoot.applyCss();
                floatingRoot.layout();
                FontIcon floatingRocket = (FontIcon) floatingRoot.lookup(".docking-demo-drag-graphic");
                assertTrue(floatingRocket.getIconSize() >= 96, "Floating windows must retain demo styling");
                FontIcon tabIcon = (FontIcon) floatingRoot.lookup(".docking-tab-title .ikonli-font-icon");
                assertTrue(tabIcon.getIconSize() < 32, "Content icon styling must not enlarge tab icons");
            } finally {
                List.copyOf(Window.getWindows()).stream()
                        .filter(window -> !previousWindows.contains(window)).forEach(Window::hide);
                Application.setUserAgentStylesheet(previousStylesheet);
                if (previousTheme == null) {
                    System.clearProperty("atlantafx");
                } else {
                    System.setProperty("atlantafx", previousTheme);
                }
            }
        });
    }

    @Test
    public void editorMenuAndToolbarToggleWrappingIndependentlyForEachView() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            Dockable editor = app.createDockable("editor").orElseThrow();
            Dockable copy = app.createSidebarDockable("editor");
            TextArea text = (TextArea) editor.getContent();
            ToggleButton wrap = assertInstanceOf(ToggleButton.class, editor.getToolbar());
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
    public void editorVetoesCloseUntilUnsavedChangesAreConfirmed() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                DockingPane pane = (DockingPane) stage.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                com.dlsc.gemsfx.DialogPane dialogs = dialogPane(stage);
                assertNotNull(dialogs, "The main window needs a dialog overlay");

                Dockable unchanged = context.openDockable(pane, "editor:clean", DockingPosition.CENTER).orElseThrow();
                assertNotNull(unchanged.getOnCloseRequest());
                assertTrue(context.requestCloseDockable(unchanged));

                Dockable editor = context.getDockables().stream()
                        .filter(dockable -> dockable.getId().equals("editor")).findFirst().orElseThrow();
                TextArea text = (TextArea) editor.getContent();
                text.appendText("// Unsaved edit");
                pane.selectDockable("map");
                pane.applyCss();
                assertNull(text.getScene(), "The editor is an unselected tab");
                Platform.runLater(() -> dialogs.getDialogs().getFirst().press(ButtonType.CANCEL));
                assertFalse(context.requestCloseDockable(editor));
                assertTrue(context.getDockables().contains(editor));
                assertTrue(text.getText().contains("// Unsaved edit"));

                Platform.runLater(() -> dialogs.getDialogs().getFirst().press(ButtonType.OK));
                assertTrue(context.requestCloseDockable(editor));
                assertFalse(context.getDockables().contains(editor));
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void floatingWindowCloseUsesItsOwnEditorConfirmation() throws Exception {
        runFx(() -> {
            Stage main = new Stage();
            try {
                new DockingPaneApp().start(main);
                DockingPane root = (DockingPane) main.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = root.getDockingContext();
                DockingPane floating = context.floatDockable("editor", 100, 100);
                Stage window = (Stage) floating.getScene().getWindow();
                Dockable editor = floating.getDockables().getFirst();
                ((TextArea) editor.getContent()).appendText(" Unsaved edit");
                Dockable console = context.openDockable(floating, "console:float", DockingPosition.CENTER).orElseThrow();
                floating.applyCss();
                assertNull(editor.getContent().getScene(), "The editor is an unselected tab");
                com.dlsc.gemsfx.DialogPane dialogs = dialogPane(window);
                assertNotNull(dialogs, "The floating window needs a dialog overlay");
                assertNotSame(dialogPane(main), dialogs);

                Platform.runLater(() -> dialogs.getDialogs().getFirst().press(ButtonType.CANCEL));
                WindowEvent cancelled = new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST);
                window.fireEvent(cancelled);
                assertTrue(cancelled.isConsumed());
                assertTrue(window.isShowing());
                assertTrue(floating.getDockables().containsAll(List.of(editor, console)));

                Platform.runLater(() -> dialogs.getDialogs().getFirst().press(ButtonType.OK));
                window.fireEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSE_REQUEST));
                assertFalse(window.isShowing());
                assertFalse(context.getDockables().contains(editor));
                assertFalse(context.getDockables().contains(console));
            } finally {
                main.hide();
            }
        });
    }

    @Test
    public void editorConfirmationWorksWithCustomTabWithoutToolbar() throws Exception {
        runFx(() -> {
            Stage stage = new Stage();
            try {
                new DockingPaneApp().start(stage);
                DockingPane pane = (DockingPane) stage.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                Dockable editor = context.getDockables().stream()
                        .filter(dockable -> dockable.getId().equals("editor")).findFirst().orElseThrow();
                context.setTabFactory(dockable -> new Label(dockable.getTitle()));
                pane.applyCss();
                assertNull(editor.getToolbar().getScene());
                ((TextArea) editor.getContent()).appendText(" Unsaved edit");
                com.dlsc.gemsfx.DialogPane dialogs = dialogPane(stage);
                AtomicBoolean confirmationShown = new AtomicBoolean();
                Platform.runLater(() -> {
                    if (!dialogs.getDialogs().isEmpty()) {
                        confirmationShown.set(true);
                        dialogs.getDialogs().getFirst().press(ButtonType.CANCEL);
                    }
                });

                assertFalse(context.requestCloseDockable(editor));
                assertTrue(confirmationShown.get(), "A custom tab must not prevent the confirmation dialog");
            } finally {
                stage.hide();
            }
        });
    }

    private static com.dlsc.gemsfx.DialogPane dialogPane(Stage stage) {
        return (com.dlsc.gemsfx.DialogPane) stage.getScene().getRoot().lookup(".docking-demo-dialog-pane");
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
    public void dockableToolbarActionsMatchHeaderIconsInBothThemes() throws Exception {
        runFx(() -> {
            for (boolean atlanta : List.of(false, true)) {
                DockingPaneApp app = new DockingPaneApp();
                DockingContext context = new DockingContext(app::createDockable, pane -> new Stage());
                DockingPane pane = context.createPane();
                for (String id : List.of("navigator", "editor", "console")) {
                    context.openDockable(pane, id, DockingPosition.CENTER).orElseThrow().setOnHelp(() -> {});
                }
                StackPane root = new StackPane(pane);
                root.getStyleClass().add("docking-demo-app");
                root.setStyle("-fx-box-border: #123456; -color-neutral-emphasis-plus: #123456;");
                String stylesheet = atlanta ? "docking-pane-app-atlantafx.css" : "docking-pane-app.css";
                root.getStylesheets().add(DockingPaneApp.class.getResource(stylesheet).toExternalForm());
                Scene scene = new Scene(root, 800, 600);
                if (atlanta) {
                    scene.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
                    GemsFXAtlantaFX.applyTo(scene);
                } else {
                    scene.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
                }
                root.applyCss();
                root.layout();
                StackPane groupActions = (StackPane) pane.lookup(".docking-tab-pane-actions");
                var border = groupActions.getBorder().getStrokes().get(0);
                assertNotEquals(Color.TRANSPARENT, border.getLeftStroke());
                assertEquals(Color.TRANSPARENT, border.getRightStroke());
                assertTrue(border.getWidths().getLeft() > 0);
                assertEquals(0, border.getWidths().getRight());
                assertEquals(groupActions.getPadding().getLeft(), groupActions.getPadding().getRight());

                for (String id : List.of("navigator", "editor", "console")) {
                    pane.selectDockable(id);
                    root.applyCss();
                    root.layout();
                    Dockable dockable = pane.getActiveDockable();
                    List<ButtonBase> actions = dockable.getToolbar() instanceof HBox
                            ? ((HBox) dockable.getToolbar()).getChildren().stream().map(ButtonBase.class::cast).toList()
                            : List.of((ButtonBase) dockable.getToolbar());
                    FontIcon helpIcon = (FontIcon) ((Button) pane.lookup(".docking-tab:selected .docking-tab-help-button")).getGraphic();
                    for (ButtonBase action : actions) {
                        assertTrue(action.getText() == null || action.getText().isEmpty());
                        FontIcon icon = assertInstanceOf(FontIcon.class, action.getGraphic());
                        assertEquals(12, icon.getIconSize());
                        assertEquals(helpIcon.getIconColor(), icon.getIconColor());
                        for (String state : List.of("hover", "pressed", "selected")) {
                            action.pseudoClassStateChanged(PseudoClass.getPseudoClass(state), true);
                            root.applyCss();
                            assertTrue(action.getBackground() == null || action.getBackground().getFills().stream()
                                    .allMatch(fill -> Color.TRANSPARENT.equals(fill.getFill())));
                            action.pseudoClassStateChanged(PseudoClass.getPseudoClass(state), false);
                        }
                    }
                }
            }
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
            Stage stage = new Stage();
            try {
                app.start(stage);
                DockingPane pane = (DockingPane) stage.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                Dockable withHelp = null;
                Dockable withoutHelp = null;
                for (int i = 0; i < 128 && (withHelp == null || withoutHelp == null); i++) {
                    Dockable dockable = context.openDockable(pane, "search:" + i, DockingPosition.CENTER).orElseThrow();
                    if (dockable.getOnHelp() == null) {
                        withoutHelp = dockable;
                    } else {
                        withHelp = dockable;
                    }
                }
                assertNotNull(withHelp, "Some demo dockables should offer help");
                assertNotNull(withoutHelp, "Some demo dockables should omit help");
                withHelp.setTitle("Renamed Search");
                com.dlsc.gemsfx.DialogPane dialogs = dialogPane(stage);
                List<Window> previousWindows = List.copyOf(Window.getWindows());
                withHelp.getOnHelp().run();
                assertEquals(1, dialogs.getDialogs().size());
                com.dlsc.gemsfx.DialogPane.Dialog<?> dialog = dialogs.getDialogs().getFirst();
                assertEquals("Help for Renamed Search", dialog.getTitle());
                assertTrue(((Label) dialog.getContent()).getText().contains("Drag"));
                assertEquals(previousWindows, List.copyOf(Window.getWindows()));
                dialog.press(ButtonType.OK);
                assertTrue(dialogs.getDialogs().isEmpty());
            } finally {
                stage.hide();
            }
        });
    }

    @Test
    public void helpForUnselectedFloatingDockableUsesItsWindowOverlay() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            Stage stage = new Stage();
            try {
                app.start(stage);
                DockingPane pane = (DockingPane) stage.getScene().getRoot().lookup(".docking-pane");
                DockingContext context = pane.getDockingContext();
                Dockable helpDockable = null;
                for (int i = 0; i < 128 && helpDockable == null; i++) {
                    Dockable candidate = context.openDockable(pane, "search:" + i, DockingPosition.CENTER).orElseThrow();
                    if (candidate.getOnHelp() != null) {
                        helpDockable = candidate;
                    }
                }
                assertNotNull(helpDockable);
                DockingPane floating = context.floatDockable(helpDockable.getId(), 100, 100);
                Stage floatingStage = helpDockable.getStage();
                context.openDockable(floating, "editor:other", DockingPosition.CENTER);
                floating.applyCss();
                assertNull(helpDockable.getContent().getScene());
                com.dlsc.gemsfx.DialogPane floatingDialogs = dialogPane(floatingStage);
                helpDockable.getOnHelp().run();
                assertEquals(1, floatingDialogs.getDialogs().size());
                assertTrue(dialogPane(stage).getDialogs().isEmpty());
                floatingDialogs.getDialogs().getFirst().press(ButtonType.OK);
            } finally {
                stage.hide();
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
                        new DockingLayoutNode.Split(Orientation.HORIZONTAL, List.of(
                                new DockingLayoutNode.Tabs(List.of("editor", "console"), "console"),
                                new DockingLayoutNode.Tabs(List.of("preview"), "preview"),
                                new DockingLayoutNode.Tabs(List.of("logs"), "logs")), List.of(0.35, 0.8)), null),
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
        assertEquals(2, main.path("dividerPositions").size());
        assertEquals(0.35, main.path("dividerPositions").get(0).asDouble());
        assertEquals(0.8, main.path("dividerPositions").get(1).asDouble());
        assertEquals(3, main.path("children").size());
        assertEquals("console", main.path("children").get(0).path("selectedId").asText());
        assertEquals(2, main.path("children").get(0).path("dockableIds").size());
        assertEquals("preview", main.path("children").get(1).path("selectedId").asText());
        assertEquals("logs", main.path("children").get(2).path("selectedId").asText());
        assertFalse(main.has("dividerPosition"));
        assertFalse(main.has("first"));
        assertFalse(main.has("second"));
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
    public void activeGroupUsesAccentColorWhileOtherSelectedTabsStayNeutral() throws Exception {
        runFx(() -> {
            DockingPaneApp app = new DockingPaneApp();
            DockingContext context = new DockingContext(app::createDockable, pane -> null);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor", DockingPosition.CENTER);
            context.openDockable(pane, "inspector", DockingPosition.RIGHT);
            StackPane root = new StackPane(pane);
            root.getStyleClass().add("docking-demo-app");
            root.setStyle("-color-accent-emphasis: #12ab34; -color-neutral-emphasis: #654321;");
            root.getStylesheets().add(DockingPaneApp.class.getResource("docking-pane-app-atlantafx.css").toExternalForm());
            Scene scene = new Scene(root, 800, 600);
            scene.setUserAgentStylesheet(new NordLight().getUserAgentStylesheet());
            GemsFXAtlantaFX.applyTo(scene);
            root.applyCss();
            root.layout();

            for (String id : List.of("editor", "inspector")) {
                pane.selectDockable(id);
                root.applyCss();
                var active = (Region) pane.lookup(".docking-tab-pane:active .docking-tab:selected");
                assertNotNull(active);
                assertEquals(Color.web("#12ab34"), active.getBackground().getFills().getFirst().getFill());
                var other = pane.lookupAll(".docking-tab:selected").stream()
                        .filter(tab -> tab != active).map(Region.class::cast).findFirst().orElseThrow();
                assertEquals(Color.web("#654321"), other.getBackground().getFills().getFirst().getFill());
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
                Region closeIcon = (Region) pane.lookup(".docking-tab:selected .docking-tab-close-button .icon");
                assertEquals(Color.web("#abcdef"), closeIcon.getBackground().getFills().getFirst().getFill());
                MenuButton menu = (MenuButton) pane.lookup(".docking-tab:selected .docking-tab-menu-button");
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
