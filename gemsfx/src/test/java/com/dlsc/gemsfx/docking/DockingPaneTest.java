package com.dlsc.gemsfx.docking;

import com.dlsc.gemsfx.FxTestBase;
import com.dlsc.gemsfx.skins.DockingPaneSkin;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import com.dlsc.gemsfx.util.ResourceBundleManager;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.css.PseudoClass;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.PickResult;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.junit.Test;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class DockingPaneTest extends FxTestBase {

    private static DockingContext context(Dockable... dockables) {
        return new DockingContext(id -> {
            for (Dockable d : dockables) {
                if (d.getId().equals(id)) {
                    return Optional.of(d);
                }
            }
            return Optional.empty();
        }, pane -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(pane));
            return stage;
        });
    }

    private static Dockable dockable(String id) {
        return new Dockable(id, "Title " + id, new Label("Content " + id));
    }

    private static void press(Node node, KeyCode code) {
        Event.fireEvent(node, new KeyEvent(KeyEvent.KEY_PRESSED, "", "", code, false, false, false, false));
    }

    private static void mouse(Node node, EventType<MouseEvent> type, boolean stillSincePress) {
        Event.fireEvent(node, new MouseEvent(type, 5, 5, 105, 105, MouseButton.PRIMARY, 1,
                false, false, false, false, type == MouseEvent.MOUSE_PRESSED, false, false,
                false, false, stillSincePress, new PickResult(node, 5, 5)));
    }

    private static void doubleClick(Node node) {
        Event.fireEvent(node, new MouseEvent(MouseEvent.MOUSE_CLICKED, 5, 5, 105, 105, MouseButton.PRIMARY, 2,
                false, false, false, false, false, false, false,
                false, false, true, new PickResult(node, 5, 5)));
    }

    private static TextField titleEditor(DockingPane pane) {
        pane.applyCss();
        pane.layout();
        TextField editor = (TextField) pane.lookup(".docking-tab-title-editor");
        assertNotNull("Double-clicking a tab should open its title editor", editor);
        return editor;
    }

    private static List<Node> titles(DockingPane pane) {
        return new ArrayList<>(pane.lookupAll(".docking-tab"));
    }

    private static Node title(DockingPane pane, String id) {
        for (Node node : titles(pane)) {
            if (id.equals(node.getProperties().get("docking.dockable-id"))) {
                return node;
            }
        }
        throw new AssertionError("no title for " + id);
    }

    private static Object invokeSkinPrivate(DockingPaneSkin skin, String name, Class<?>[] types, Object... args) {
        try {
            Method method = DockingPaneSkin.class.getDeclaredMethod(name, types);
            method.setAccessible(true);
            return method.invoke(skin, args);
        } catch (ReflectiveOperationException ex) {
            throw new AssertionError(ex);
        }
    }

    private static Rectangle2D coloredBounds(Image image, Color expected) {
        int minX = (int) image.getWidth();
        int minY = (int) image.getHeight();
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                Color actual = image.getPixelReader().getColor(x, y);
                if (actual.getOpacity() > 0.5
                        && Math.abs(actual.getRed() - expected.getRed()) < 0.02
                        && Math.abs(actual.getGreen() - expected.getGreen()) < 0.02
                        && Math.abs(actual.getBlue() - expected.getBlue()) < 0.02) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        assertTrue("The drag image must contain the dockable's rendered content; center pixel: "
                        + image.getPixelReader().getColor((int) image.getWidth() / 2, (int) image.getHeight() / 2),
                maxX >= minX && maxY >= minY);
        return new Rectangle2D(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    @Test
    public void toolbarTracksInitialValueReplacementAndRemovalBeforeTabButtons() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            Button initial = new Button("Initial");
            dockable.setToolbar(initial);
            DockingTab tab = new DockingTab(context(dockable), dockable);
            Node help = tab.lookup(".docking-tab-help-button");
            assertNotNull(initial.getParent());
            assertTrue(help.getParent().getChildrenUnmodifiable().indexOf(initial.getParent())
                    < help.getParent().getChildrenUnmodifiable().indexOf(help));

            Button replacement = new Button("Replacement");
            dockable.toolbarProperty().set(replacement);
            assertNull(initial.getParent());
            assertNotNull(replacement.getParent());
            assertTrue(help.getParent().getChildrenUnmodifiable().indexOf(replacement.getParent())
                    < help.getParent().getChildrenUnmodifiable().indexOf(help));
            dockable.setToolbar(null);
            assertNull(replacement.getParent());
            Node host = tab.lookup(".docking-tab-toolbar");
            assertTrue(host == null || !host.isManaged());

            dockable.setToolbar(initial);
            assertNotNull(initial.getParent());
            assertTrue(initial.getParent().isManaged());
        });
    }

    @Test
    public void tabActionsFadeTogetherAndReleaseTitleSpaceWhenHidden() throws InterruptedException {
        class HoverableTab extends DockingTab {
            HoverableTab(Dockable dockable) {
                super(context(dockable), dockable);
            }

            void hover(boolean hovered) {
                setHover(hovered);
            }
        }
        double[] hiddenTitleWidth = new double[1];
        CountDownLatch fadedIn = new CountDownLatch(1);
        HoverableTab tab = invoke(() -> {
            Dockable dockable = dockable("a");
            Button toolbar = new Button("Action");
            dockable.setToolbar(toolbar);
            dockable.setOnHelp(() -> {});
            dockable.getMenuItems().add(new MenuItem("Inspect"));
            HoverableTab header = new HoverableTab(dockable);
            new Scene(header, 500, 40);
            header.applyCss();
            header.resize(500, 40);
            header.layout();
            Node actions = header.lookup(".docking-tab-close-button").getParent();
            assertEquals(0, actions.getOpacity(), 0);
            assertFalse(actions.isManaged());
            for (String selector : List.of(".docking-tab-toolbar", ".docking-tab-menu-button", ".docking-tab-help-button")) {
                assertSame(actions, header.lookup(selector).getParent());
            }
            hiddenTitleWidth[0] = header.lookup(".docking-tab-title").getLayoutBounds().getWidth();
            actions.opacityProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.doubleValue() == 1) {
                    fadedIn.countDown();
                }
            });
            header.hover(true);
            return header;
        });
        assertTrue("Tab actions must fade in on hover", fadedIn.await(5, TimeUnit.SECONDS));
        CountDownLatch fadedOut = new CountDownLatch(1);
        runFx(() -> {
            Node actions = tab.lookup(".docking-tab-close-button").getParent();
            assertEquals(1, actions.getOpacity(), 0);
            assertTrue(actions.isManaged());
            tab.layout();
            assertTrue(tab.lookup(".docking-tab-title").getLayoutBounds().getWidth() < hiddenTitleWidth[0]);
            actions.opacityProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue.doubleValue() == 0) {
                    fadedOut.countDown();
                }
            });
            tab.hover(false);
        });
        assertTrue("Tab actions must fade out when hover ends", fadedOut.await(5, TimeUnit.SECONDS));
        runFx(() -> {
            Node actions = tab.lookup(".docking-tab-close-button").getParent();
            assertFalse(actions.isManaged());
            tab.layout();
            assertEquals(hiddenTitleWidth[0], tab.lookup(".docking-tab-title").getLayoutBounds().getWidth(), 0);
        });
    }

    @Test
    public void tabHeightIsIndependentOfActionContentAndScalesWithFontSize() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            dockable.setOnHelp(() -> {});
            dockable.getMenuItems().add(new MenuItem("Inspect"));
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            Region tab = (Region) title(pane, "a");
            Node actions = tab.lookup(".docking-tab-actions");

            tab.setStyle("-fx-font-size: 20px;");
            pane.resize(220, 400);
            pane.applyCss();
            pane.layout();
            for (double toolbarHeight : new double[]{0, 48, 72, 0}) {
                if (toolbarHeight == 0) {
                    dockable.setToolbar(null);
                } else {
                    Region toolbar = new Region();
                    toolbar.setMinHeight(toolbarHeight);
                    toolbar.setPrefHeight(toolbarHeight);
                    toolbar.setPrefWidth(60);
                    dockable.setToolbar(toolbar);
                }
                pane.applyCss();
                pane.layout();
                assertFalse(actions.isManaged());
                double hiddenHeight = tab.getHeight();
                double hiddenMinHeight = tab.minHeight(-1);
                double hiddenPrefHeight = tab.prefHeight(-1);
                assertEquals("Content must not determine the tab height", 50, hiddenHeight, 0);
                assertEquals(50, hiddenMinHeight, 0);
                assertEquals(50, tab.maxHeight(-1), 0);

                actions.setOpacity(1);
                pane.layout();
                assertTrue(actions.isManaged());
                assertEquals("Hover must not change the tab height", hiddenHeight, tab.getHeight(), 0);
                assertEquals(hiddenMinHeight, tab.minHeight(-1), 0);
                assertEquals(hiddenPrefHeight, tab.prefHeight(-1), 0);
                Region label = (Region) tab.lookup(".docking-tab-title");
                double availableHeight = tab.getHeight() - tab.getInsets().getTop() - tab.getInsets().getBottom();
                for (String selector : List.of(".docking-tab-title", ".docking-tab-actions",
                        ".docking-tab-toolbar", ".docking-tab-menu-button", ".docking-tab-help-button",
                        ".docking-tab-close-button")) {
                    Region child = (Region) tab.lookup(selector);
                    if (child.isManaged()) {
                        assertEquals(selector, availableHeight, child.getHeight(), 0);
                        Bounds bounds = tab.sceneToLocal(child.localToScene(child.getLayoutBounds()));
                        assertEquals(selector, tab.getInsets().getTop() + availableHeight / 2,
                                (bounds.getMinY() + bounds.getMaxY()) / 2, 0);
                        if (child != label) {
                            assertEquals(selector, Math.max(child.minWidth(availableHeight),
                                    child.prefWidth(availableHeight)), child.getWidth(), 0);
                        }
                    }
                }
                assertEquals(tab.getInsets().getTop(), label.getLayoutY(), 0);
                if (dockable.getToolbar() != null) {
                    Region toolbar = (Region) dockable.getToolbar();
                    assertEquals(availableHeight, toolbar.getHeight(), 0);
                    assertEquals(toolbar.prefWidth(availableHeight), toolbar.getWidth(), 0);
                }

                actions.setOpacity(0);
                pane.layout();
                assertEquals(hiddenHeight, tab.getHeight(), 0);
            }
            tab.setStyle("-fx-font-size: 16px;");
            pane.applyCss();
            pane.layout();
            assertEquals(40, tab.getHeight(), 0);
            tab.setStyle("-fx-font-size: 16px; -fx-pref-height: 3em;");
            pane.applyCss();
            pane.layout();
            assertEquals("Applications can override the height through CSS", 48, tab.getHeight(), 0);
        });
    }

    @Test
    public void titleEditingPermissionCanBeBoundAndDisablingItCancelsAnEdit() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            SimpleBooleanProperty allowed = new SimpleBooleanProperty(false);
            dockable.titleEditableProperty().bind(allowed);
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            dockable.setTitle("Programmatic rename");
            doubleClick(title(pane, "a"));
            assertNull(pane.lookup(".docking-tab-title-editor"));
            assertEquals("Programmatic rename", ((Label) title(pane, "a").lookup(".docking-tab-title")).getText());
            allowed.set(true);
            doubleClick(title(pane, "a"));
            titleEditor(pane).setText("Not allowed to commit");
            allowed.set(false);

            assertEquals("Programmatic rename", dockable.getTitle());
            assertNull(pane.lookup(".docking-tab-title-editor"));
            allowed.set(true);
            doubleClick(title(pane, "a"));
            assertEquals("Programmatic rename", titleEditor(pane).getText());
            press(titleEditor(pane), KeyCode.ESCAPE);
        });
    }

    @Test
    public void doubleClickEditsTitleAndEnterCommitsWithoutRebuildingTheTab() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            Node header = title(pane, "a");
            Label label = (Label) header.lookup(".docking-tab-title");
            doubleClick(label);
            TextField editor = titleEditor(pane);
            assertEquals("Title a", editor.getSelectedText());
            editor.setText("Renamed");
            assertEquals("Title a", dockable.getTitle());
            press(editor, KeyCode.ENTER);

            assertEquals("Renamed", dockable.getTitle());
            assertEquals("Renamed", label.getText());
            assertSame(header, title(pane, "a"));
            assertSame(dockable.getContent(), pane.lookup(".docking-tab-content").lookup(".label"));
            assertNull(pane.lookup(".docking-tab-title-editor"));
        });
    }

    @Test
    public void escapeAndBlankInputLeaveTheTitleUnchanged() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            doubleClick(title(pane, "a"));
            TextField editor = titleEditor(pane);
            editor.setText("Discarded");
            press(editor, KeyCode.ESCAPE);
            assertEquals("Title a", dockable.getTitle());
            assertNull(pane.lookup(".docking-tab-title-editor"));

            for (String blank : List.of("", " \t ")) {
                doubleClick(title(pane, "a"));
                titleEditor(pane).setText(blank);
                press(titleEditor(pane), KeyCode.ENTER);
                assertEquals("Title a", dockable.getTitle());
                assertNull(pane.lookup(".docking-tab-title-editor"));
            }
        });
    }

    @Test
    public void titleEditorDoesNotTriggerTabNavigationClosingOrDragging() {
        runFx(() -> {
            Dockable first = dockable("a");
            Dockable second = dockable("b");
            DockingContext context = context(first, second);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node header = title(pane, "b");
            doubleClick(header);
            TextField editor = titleEditor(pane);
            press(editor, KeyCode.LEFT);
            assertEquals(0, editor.getCaretPosition());
            assertEquals("", editor.getSelectedText());
            press(editor, KeyCode.DELETE);
            assertEquals("itle b", editor.getText());
            press(editor, KeyCode.SPACE);
            mouse(editor, MouseEvent.MOUSE_CLICKED, true);

            assertSame(second, pane.getActiveDockable());
            assertEquals(2, context.getDockables().size());
            assertSame(editor, titleEditor(pane));
            AtomicInteger drags = new AtomicInteger();
            header.addEventHandler(MouseEvent.DRAG_DETECTED, event -> drags.incrementAndGet());
            mouse(editor, MouseEvent.DRAG_DETECTED, false);
            assertEquals(0, drags.get());
        });
    }

    @Test
    public void leavingTheTitleEditorCommitsWithoutStealingFocusBack() {
        DockingPane pane = invoke(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = context(dockable);
            DockingPane result = context.createPane();
            context.openDockable(result, "a", DockingPosition.CENTER);
            layout(result);
            Stage stage = (Stage) result.getScene().getWindow();
            stage.show();
            stage.requestFocus();
            return result;
        });
        try {
            waitForFxEvents();
            runFx(() -> {
                doubleClick(title(pane, "a"));
                TextField editor = titleEditor(pane);
                assertTrue(editor.isFocused());
                editor.setText("Committed on blur");
                pane.getActiveDockable().getContent().requestFocus();

                assertEquals("Committed on blur", pane.getActiveDockable().getTitle());
                assertNull(pane.lookup(".docking-tab-title-editor"));
                assertSame(pane.getActiveDockable().getContent(), pane.getScene().getFocusOwner());
            });
        } finally {
            runFx(() -> ((Stage) pane.getScene().getWindow()).close());
        }
    }

    @Test
    public void menuButtonTracksItemsAndPrecedesHelpWithoutSelectingOrClosingItsDockable() {
        runFx(() -> {
            Dockable first = dockable("a");
            first.setOnHelp(() -> {});
            DockingContext context = context(first, dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            DockingTab header = (DockingTab) title(pane, "a");
            MenuButton menu = (MenuButton) header.lookup(".docking-tab-menu-button");
            assertFalse(menu.isVisible());
            assertFalse(menu.isManaged());

            AtomicInteger calls = new AtomicInteger();
            MenuItem action = new MenuItem("Inspect");
            action.setOnAction(event -> calls.incrementAndGet());
            MenuItem other = new MenuItem("Other");
            first.getMenuItems().addAll(action, other);
            assertTrue(menu.isVisible());
            assertTrue(menu.isManaged());
            assertEquals(List.of(action, other), menu.getItems());
            assertSame(header.lookup(".docking-tab-help-button"),
                    menu.getParent().getChildrenUnmodifiable().get(menu.getParent().getChildrenUnmodifiable().indexOf(menu) + 1));
            menu.getItems().get(0).fire();
            mouse(menu, MouseEvent.MOUSE_CLICKED, true);
            assertEquals(1, calls.get());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, context.getDockables().size());

            first.getMenuItems().setAll(other);
            assertEquals(List.of(other), menu.getItems());
            first.getMenuItems().clear();
            assertTrue(menu.getItems().isEmpty());
            assertFalse(menu.isVisible());
            assertFalse(menu.isManaged());
        });
    }

    @Test
    public void detachedMenuHeadersReleaseItemsAndStopObservingTheirList() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            MenuItem action = new MenuItem("Inspect");
            dockable.getMenuItems().add(action);
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            MenuButton oldMenu = (MenuButton) title(pane, "a").lookup(".docking-tab-menu-button");
            context.setTabFactory(candidate -> new Label(candidate.getTitle()));
            assertTrue(oldMenu.getItems().isEmpty());
            assertNull(action.getParentPopup());
            dockable.getMenuItems().add(new MenuItem("New action"));
            assertTrue(oldMenu.getItems().isEmpty());

            context.setTabFactory(null);
            pane.applyCss();
            MenuButton restored = (MenuButton) title(pane, "a").lookup(".docking-tab-menu-button");
            assertEquals(dockable.getMenuItems(), restored.getItems());
        });
    }

    @Test
    public void helpButtonTracksCallbackChangesWithoutSelectingOrClosingItsDockable() {
        runFx(() -> {
            Dockable first = dockable("a");
            DockingContext context = context(first, dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node header = title(pane, "a");
            Node unavailable = header.lookup(".docking-tab-help-button");
            assertTrue(unavailable == null || (!unavailable.isVisible() && !unavailable.isManaged()));
            AtomicInteger originalCalls = new AtomicInteger();
            first.onHelpProperty().set(originalCalls::incrementAndGet);
            Button help = (Button) header.lookup(".docking-tab-help-button");
            assertNotNull(help);
            assertTrue(help.isVisible());
            assertTrue(help.isManaged());
            List<Node> children = help.getParent().getChildrenUnmodifiable();
            assertSame(header.lookup(".docking-tab-close-button"), children.get(children.indexOf(help) + 1));
            help.fire();
            mouse(help, MouseEvent.MOUSE_CLICKED, true);
            assertEquals(1, originalCalls.get());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, context.getDockables().size());

            AtomicInteger replacementCalls = new AtomicInteger();
            first.setOnHelp(replacementCalls::incrementAndGet);
            help.fire();
            assertEquals(1, originalCalls.get());
            assertEquals(1, replacementCalls.get());
            first.setOnHelp(null);
            Node removed = header.lookup(".docking-tab-help-button");
            assertTrue(removed == null || (!removed.isVisible() && !removed.isManaged()));
            help.fire();
            assertEquals(1, replacementCalls.get());
        });
    }

    @Test
    public void helpButtonHasLocalizedTooltipAndTitleAwareAccessibleText() {
        runFx(() -> {
            Locale originalLocale = ResourceBundleManager.getLocale();
            try {
                ResourceBundleManager.setLocale(Locale.GERMAN);
                Dockable dockable = dockable("a");
                dockable.setOnHelp(() -> {});
                dockable.getMenuItems().add(new MenuItem("Inspect"));
                DockingTab tab = new DockingTab(context(dockable), dockable);
                Button help = (Button) tab.lookup(".docking-tab-help-button");
                MenuButton menu = (MenuButton) tab.lookup(".docking-tab-menu-button");
                assertEquals("Hilfe", help.getTooltip().getText());
                assertEquals("Hilfe zu Title a", help.getAccessibleText());
                assertEquals("Weitere Aktionen", menu.getTooltip().getText());
                assertEquals("Aktionen für Title a", menu.getAccessibleText());
                dockable.setTitle("Editor");
                assertEquals("Hilfe zu Editor", help.getAccessibleText());
                assertEquals("Aktionen für Editor", menu.getAccessibleText());
            } finally {
                ResourceBundleManager.setLocale(originalLocale);
            }
        });
    }

    @Test
    public void actionButtonsAreIconOnlyAcrossThemesAndTabStates() {
        runFx(() -> {
            Dockable dockable = dockable("a");
            dockable.setOnHelp(() -> {});
            dockable.getMenuItems().add(new MenuItem("Inspect"));
            DockingContext context = context(dockable, dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Scene scene = pane.getScene();
            scene.getStylesheets().add(getClass().getResource("docking-drag-theme.css").toExternalForm());
            for (boolean themed : List.of(false, true)) {
                if (themed) {
                    GemsFXAtlantaFX.applyTo(scene);
                }
                for (String selectedId : List.of("b", "a")) {
                    pane.selectDockable(selectedId);
                    Node tab = title(pane, "a");
                    Button close = (Button) tab.lookup(".docking-tab-close-button");
                    Button help = (Button) tab.lookup(".docking-tab-help-button");
                    MenuButton menu = (MenuButton) tab.lookup(".docking-tab-menu-button");
                    FontIcon icon = (FontIcon) help.getGraphic();
                    FontIcon menuIcon = (FontIcon) menu.getGraphic();
                    for (String state : List.of("normal", "hover", "armed", "showing")) {
                        for (ButtonBase button : List.of(close, help, menu)) {
                            button.pseudoClassStateChanged(PseudoClass.getPseudoClass("hover"), state.equals("hover"));
                            button.pseudoClassStateChanged(PseudoClass.getPseudoClass("armed"), state.equals("armed"));
                            button.pseudoClassStateChanged(PseudoClass.getPseudoClass("showing"), state.equals("showing"));
                        }
                        scene.getRoot().applyCss();
                        assertEquals(12, icon.getIconSize());
                        assertEquals(12, menuIcon.getIconSize());
                        assertEquals(0, ((Region) menu.lookup(".arrow-button")).prefWidth(-1), 0);
                        for (ButtonBase button : List.of(close, help, menu)) {
                            if (button.getBackground() != null) {
                                button.getBackground().getFills().forEach(fill ->
                                        assertEquals(Color.TRANSPARENT, fill.getFill()));
                            }
                        }
                        if (themed) {
                            Color expected = selectedId.equals("a") ? Color.WHITE : Color.LIME;
                            assertEquals(expected, icon.getIconColor());
                            assertEquals(expected, menuIcon.getIconColor());
                            Region closeIcon = (Region) close.getGraphic();
                            assertEquals(expected, closeIcon.getBackground().getFills().get(0).getFill());
                        }
                    }
                }
            }
        });
    }

    @Test
    public void tabSelectionUsesPseudoClassForDefaultAndCustomHeaders() {
        runFx(() -> {
            PseudoClass selected = PseudoClass.getPseudoClass("selected");
            for (boolean custom : List.of(false, true)) {
                DockingContext context = context(dockable("a"), dockable("b"));
                if (custom) {
                    context.setTabFactory(dockable -> new Label(dockable.getTitle()));
                }
                DockingPane pane = context.createPane();
                context.openDockable(pane, "a", DockingPosition.CENTER);
                context.openDockable(pane, "b", DockingPosition.CENTER);
                layout(pane);

                Node first = title(pane, "a");
                Node second = title(pane, "b");
                assertFalse(first.getPseudoClassStates().contains(selected));
                assertTrue(second.getPseudoClassStates().contains(selected));
                assertFalse(first.getStyleClass().contains("selected"));
                assertFalse(second.getStyleClass().contains("selected"));

                pane.selectDockable("a");
                assertSame(first, title(pane, "a"));
                assertSame(second, title(pane, "b"));
                assertTrue(first.getPseudoClassStates().contains(selected));
                assertFalse(second.getPseudoClassStates().contains(selected));
                assertFalse(first.getStyleClass().contains("selected"));
                assertFalse(second.getStyleClass().contains("selected"));
            }
        });
    }

    @Test
    public void customTabHeadersKeepSelectionKeyboardActionsAndAccessibility() {
        runFx(() -> {
            Dockable first = dockable("a");
            DockingContext context = context(first, dockable("b"));
            context.setTabFactory(dockable -> new Label("Custom " + dockable.getTitle()));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            Label header = (Label) title(pane, "a");
            assertEquals("Custom Title a", header.getText());
            assertTrue(header.getWidth() > 0);
            assertEquals(javafx.scene.AccessibleRole.TAB_ITEM, header.getAccessibleRole());
            first.setTitle("Renamed");
            assertEquals("Renamed", header.getAccessibleText());
            mouse(header, MouseEvent.MOUSE_CLICKED, true);
            assertSame(first, pane.getActiveDockable());
            assertSame(header, title(pane, "a"));
            assertSame(pane.lookup(".docking-tab-content"), first.getContent().getParent());
            assertTrue(title(pane, "a").getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")));
            press(title(pane, "a"), KeyCode.RIGHT);
            assertEquals("b", pane.getActiveDockable().getId());
            assertSame(header, title(pane, "a"));
            assertFalse(header.getPseudoClassStates().contains(PseudoClass.getPseudoClass("selected")));
            assertNull(first.getContent().getParent());
            press(title(pane, "a"), KeyCode.DELETE);
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
        });
    }

    @Test
    public void changingTabFactoryRefreshesMainAndFloatingPanesAndCanRestoreDefaultTabs() {
        runFx(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane main = context.createPane();
            context.openDockable(main, "a", DockingPosition.CENTER);
            context.openDockable(main, "b", DockingPosition.CENTER);
            layout(main);
            DockingPane floating = context.floatDockable("b", 100, 100);
            Stage stage = (Stage) floating.getScene().getWindow();
            try {
                floating.applyCss();
                assertTrue(title(main, "a") instanceof DockingTab);
                assertTrue(title(floating, "b") instanceof DockingTab);
                Node oldHeader = title(main, "a");
                context.tabFactoryProperty().set(dockable -> {
                    StackPane custom = new StackPane(new Label("Custom " + dockable.getId()));
                    custom.getStyleClass().add("docking-tab-title");
                    return custom;
                });
                assertTrue(title(main, "a") instanceof StackPane);
                assertTrue(title(floating, "b") instanceof StackPane);
                assertNotSame(oldHeader, title(main, "a"));

                context.setTabFactory(null);
                assertTrue(title(main, "a") instanceof DockingTab);
                assertTrue(title(floating, "b") instanceof DockingTab);
                assertNotNull(title(main, "a").lookup(".docking-tab-close-button"));
            } finally {
                stage.close();
            }
        });
    }

    @Test
    public void disposedSkinStopsObservingTabFactoryChanges() {
        runFx(() -> {
            DockingContext context = context(dockable("a"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            pane.setSkin(null);
            AtomicInteger calls = new AtomicInteger();
            context.setTabFactory(dockable -> {
                calls.incrementAndGet();
                return new Label(dockable.getTitle());
            });
            assertEquals(0, calls.get());
        });
    }

    @Test
    public void tabFactoryRejectsNullAndReusedHeaders() {
        runFx(() -> {
            DockingContext missing = context(dockable("a"));
            missing.setTabFactory(dockable -> null);
            DockingPane missingPane = missing.createPane();
            missing.openDockable(missingPane, "a", DockingPosition.CENTER);
            assertThrows(NullPointerException.class, () -> new DockingPaneSkin(missingPane));

            DockingContext reused = context(dockable("a"), dockable("b"));
            Label shared = new Label("Shared header");
            reused.setTabFactory(dockable -> shared);
            DockingPane reusedPane = reused.createPane();
            reused.openDockable(reusedPane, "a", DockingPosition.CENTER);
            reused.openDockable(reusedPane, "b", DockingPosition.CENTER);
            assertThrows(IllegalArgumentException.class, () -> new DockingPaneSkin(reusedPane));
        });
    }

    @Test
    public void dockingPaneCreatesItsCustomSkinRoleAndStylesheet() {
        invoke(() -> {
            DockingPane pane = layout(context().createPane());

            assertTrue(pane.getSkin() instanceof DockingPaneSkin);
            assertEquals(javafx.scene.AccessibleRole.NODE, pane.getAccessibleRole());
            assertNotNull(pane.getAccessibleRoleDescription());
            assertTrue(pane.getUserAgentStylesheet().endsWith("docking-pane.css"));
            assertTrue(pane.getStyleClass().contains("docking-pane"));
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Empty);
            return null;
        });
    }

    @Test
    public void emptyPaneShowsCenteredDockingIllustrationAndRestoresItAfterClosing() {
        invoke(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = context(dockable, dockable("b"));
            context.openDockable(context.createPane(), "b", DockingPosition.CENTER);
            DockingPane pane = layout(context.createPane());
            Node illustration = pane.lookup(".docking-pane-placeholder");
            assertNotNull("An empty pane should show a docking illustration", illustration);
            assertTrue(illustration.getBoundsInParent().getWidth() > 0);
            assertTrue(illustration.getBoundsInParent().getHeight() > 0);
            Region empty = (Region) pane.lookup(".docking-empty");
            assertEquals(empty.getWidth() / 2, illustration.getBoundsInParent().getCenterX(), 0.5);
            assertEquals(empty.getHeight() / 2, illustration.getBoundsInParent().getCenterY(), 0.5);

            context.openDockable(pane, "a", DockingPosition.CENTER);
            assertNull(pane.lookup(".docking-pane-placeholder"));
            assertNull(illustration.getParent());

            assertTrue(context.requestCloseDockable(dockable));
            assertSame(illustration, pane.lookup(".docking-pane-placeholder"));
            return null;
        });
    }

    @Test
    public void placeholderCanBeReplacedClearedAndChangedWhileDockablesAreOpen() {
        invoke(() -> {
            Dockable dockable = dockable("a");
            DockingContext context = context(dockable);
            DockingPane pane = layout(context.createPane());
            Node original = pane.getPlaceholder();
            Label first = new Label("Drop a dockable here");
            pane.setPlaceholder(first);
            assertNull(original.getParent());
            assertNotNull(first.getParent());

            Label replacement = new Label("Empty workspace");
            pane.placeholderProperty().set(replacement);
            assertNull(first.getParent());
            assertNotNull(replacement.getParent());

            pane.setPlaceholder(null);
            assertNull(replacement.getParent());
            assertTrue(((javafx.scene.Parent) pane.lookup(".docking-empty")).getChildrenUnmodifiable().isEmpty());

            context.openDockable(pane, "a", DockingPosition.CENTER);
            pane.setPlaceholder(first);
            assertNull(first.getParent());
            assertNotNull(dockable.getContent().getParent());
            assertTrue(context.requestCloseDockable(dockable));
            assertNotNull(first.getParent());
            return null;
        });
    }

    @Test
    public void disposingSkinReleasesPlaceholderAndItsListener() {
        invoke(() -> {
            DockingPane pane = layout(context().createPane());
            Node original = pane.getPlaceholder();
            pane.setSkin(null);
            assertNull(original.getParent());

            Label replacement = new Label("Empty workspace");
            pane.setPlaceholder(replacement);
            assertNull(replacement.getParent());
            pane.setSkin(new DockingPaneSkin(pane));
            assertNotNull(replacement.getParent());
            return null;
        });
    }

    @Test
    public void placeholderTextAppearsBelowIllustrationAndUpdatesWhenBound() {
        invoke(() -> {
            DockingPanePlaceholder placeholder = layout(new DockingPanePlaceholder("Drop views here"));
            Label label = (Label) placeholder.lookup(".label");
            Node illustration = placeholder.lookup(".illustration");
            assertEquals("Drop views here", label.getText());
            assertTrue(label.isVisible());
            assertTrue(label.isManaged());
            assertTrue(label.getBoundsInParent().getMinY() > illustration.getBoundsInParent().getMaxY());
            assertEquals(illustration.getBoundsInParent().getCenterX(),
                    label.getBoundsInParent().getCenterX(), 0.01);

            javafx.beans.property.StringProperty text = new javafx.beans.property.SimpleStringProperty("An empty workspace. ".repeat(20));
            placeholder.textProperty().bind(text);
            placeholder.getParent().layout();
            assertEquals(text.get(), label.getText());
            assertEquals(text.get(), placeholder.getAccessibleText());
            assertTrue("Long placeholder text should wrap", label.getHeight() > label.getFont().getSize() * 2);

            text.set("");
            placeholder.getParent().layout();
            assertFalse(label.isVisible());
            assertFalse(label.isManaged());
            assertEquals(illustration.getBoundsInParent().getHeight(), placeholder.prefHeight(-1), 0.01);
            text.set(null);
            assertFalse(label.isVisible());
            assertFalse(label.isManaged());
            return null;
        });
    }

    @Test
    public void defaultPlaceholderDisplaysLocalizedTextIncludingRootBundle() {
        invoke(() -> {
            Locale originalLocale = ResourceBundleManager.getLocale();
            try {
                Map<Locale, String> translations = Map.of(
                        Locale.ENGLISH, "Drag views here to dock them.",
                        Locale.GERMAN, "Ziehen Sie Ansichten hierher, um sie anzudocken.",
                        Locale.CHINESE, "将视图拖到此处以停靠。",
                        Locale.ROOT, "Drag views here to dock them.");
                translations.forEach((locale, expected) -> {
                    ResourceBundleManager.setLocale(locale);
                    DockingPanePlaceholder placeholder = layout(new DockingPanePlaceholder());
                    Label label = (Label) placeholder.lookup(".label");
                    assertEquals("Placeholder text for " + locale, expected, label.getText());
                    assertTrue(label.isVisible());
                    assertTrue(label.isManaged());
                    assertEquals(expected, placeholder.getAccessibleText());
                });
            } finally {
                ResourceBundleManager.setLocale(originalLocale);
            }
            return null;
        });
    }

    @Test
    public void openedDockablesAreInsertedAtTheRequestedPosition() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();

            context.openDockable(pane, "a", DockingPosition.RIGHT);
            DockingLayoutNode.Tabs root = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a"), root.getDockableIds());

            context.openDockable(pane, "b", DockingPosition.CENTER);
            root = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a", "b"), root.getDockableIds());
            assertEquals("b", pane.getActiveDockable().getId());

            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            DockingLayoutNode.Split split = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.VERTICAL, split.getOrientation());
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) split.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) split.getChildren().get(1)).getDockableIds());
            return null;
        });
    }

    @Test
    public void everyDockableGetsItsOwnTitleAndOnlySelectedContentIsAttached() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            assertEquals(2, titles(pane).size());
            assertEquals(2, pane.lookupAll(".docking-tab-close-button").size());
            assertTrue(pane.lookupAll(".tab-pane").isEmpty());
            assertNull(a.getContent().getParent());
            assertNotNull(b.getContent().getParent());

            press(title(pane, "a"), KeyCode.ENTER);
            pane.applyCss();
            pane.layout();

            assertSame(a, pane.getActiveDockable());
            assertNotNull(a.getContent().getParent());
            assertNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void singleDockableTitleFillsTheHeaderAndKeepsItsCloseButtonAtTheRight() {
        invoke(() -> {
            DockingContext context = context(dockable("a"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            Region strip = (Region) pane.lookup(".docking-tab-strip");
            Region header = (Region) title(pane, "a");
            Node close = header.lookup(".docking-tab-close-button");
            close.getParent().setOpacity(1);
            pane.layout();

            assertEquals(0, header.getLayoutX(), 0.01);
            assertEquals(0, header.getLayoutY(), 0.01);
            assertEquals(strip.getWidth(), header.getWidth(), 0.01);
            assertEquals(strip.getHeight(), header.getHeight(), 0.01);
            assertEquals(header.getWidth() - header.getInsets().getRight(),
                    header.sceneToLocal(close.localToScene(close.getBoundsInLocal())).getMaxX(), 1);
            return null;
        });
    }

    @Test
    public void dockableHeadersShareTheStripEquallyAndTruncateLongTitlesWhenResized() {
        invoke(() -> {
            Region graphic = new Region();
            graphic.setPrefSize(16, 40);
            DockingContext context = context(
                    new Dockable("a", "Long editor title ".repeat(20), new Label("Editor")),
                    dockable("b"),
                    new Dockable("c", "Inspector", graphic, new Label("Inspector")));
            DockingPane pane = context.createPane();
            for (String id : List.of("a", "b", "c")) {
                context.openDockable(pane, id, DockingPosition.CENTER);
            }
            layout(pane);
            for (Node header : titles(pane)) {
                header.lookup(".docking-tab-close-button").getParent().setOpacity(1);
            }

            for (double width : new double[]{800, 360}) {
                pane.resize(width, 400);
                pane.layout();
                Region strip = (Region) pane.lookup(".docking-tab-strip");
                int index = 0;
                for (String id : List.of("a", "b", "c")) {
                    Region header = (Region) title(pane, id);
                    Node close = header.lookup(".docking-tab-close-button");
                    assertEquals(strip.getWidth() / 3, header.getWidth(), 1);
                    assertEquals(index++ * strip.getWidth() / 3, header.getLayoutX(), 1);
                    assertEquals(0, header.getLayoutY(), 0.01);
                    assertEquals(strip.getHeight(), header.getHeight(), 0.01);
                    assertEquals(header.getWidth() - header.getInsets().getRight(),
                            header.sceneToLocal(close.localToScene(close.getBoundsInLocal())).getMaxX(), 1);
                }
                Label label = (Label) title(pane, "a").lookup(".docking-tab-title");
                Text renderedTitle = (Text) label.lookup(".text");
                assertTrue("Long titles must ellipsize instead of pushing other headers aside",
                        renderedTitle.getText().endsWith("..."));
                Node close = title(pane, "a").lookup(".docking-tab-close-button");
                assertTrue(label.getBoundsInParent().getMaxX()
                        <= close.getParent().getBoundsInParent().getMinX());
            }
            return null;
        });
    }

    @Test
    public void openingDockableAfterPaneIsMountedRendersItsTitleAndContent() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            context.openDockable(pane, "b", DockingPosition.CENTER);
            pane.applyCss();
            pane.layout();

            assertEquals(2, titles(pane).size());
            assertNotNull(title(pane, "b"));
            assertNull(a.getContent().getParent());
            assertNotNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void openingDockableAfterDividerChangeImmediatelyUpdatesSplitLayout() {
        invoke(() -> {
            Dockable search = dockable("search");
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"), search);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            layout(pane);

            javafx.scene.control.SplitPane split =
                    (javafx.scene.control.SplitPane) pane.lookup(".docking-split");
            split.setDividerPositions(0.3);
            context.openDockable(pane, "search", DockingPosition.CENTER);
            pane.applyCss();
            pane.layout();

            assertEquals(4, titles(pane).size());
            assertSame(pane.getScene(), title(pane, "search").getScene());
            assertSame(pane.getScene(), search.getContent().getScene());
            assertSame(search, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void pressingSelectedOrUnselectedTitleKeepsDragSourceInScene() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            for (String id : List.of("b", "a")) {
                Node source = title(pane, id);
                Scene scene = source.getScene();

                mouse(source.lookup(".docking-tab-title"), MouseEvent.MOUSE_PRESSED, true);

                assertSame("The pressed title must stay attached until drag detection", scene, source.getScene());
                assertSame(source, title(pane, id));
            }
            return null;
        });
    }

    @Test
    public void dragImageSnapshotsMountedContentWithoutReparentingOrResizingIt() {
        invoke(() -> {
            Region content = new Region();
            content.setStyle("-fx-background-color: red;");
            Dockable dockable = new Dockable("a", "Editor", content);
            DockingContext context = context(dockable);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            Node parent = content.getParent();
            Scene scene = content.getScene();
            double width = content.getWidth();
            double height = content.getHeight();

            Image image = (Image) invokeSkinPrivate((DockingPaneSkin) pane.getSkin(),
                    "createDockableDragView", new Class<?>[]{String.class}, "a");

            assertTrue("Drag image width: " + image.getWidth(), image.getWidth() <= 320);
            assertTrue("Drag image height: " + image.getHeight(), image.getHeight() <= 240);
            Rectangle2D renderedContent = coloredBounds(image, Color.RED);
            assertEquals(width / height, renderedContent.getWidth() / renderedContent.getHeight(), 0.03);
            assertSame(parent, content.getParent());
            assertSame(scene, content.getScene());
            assertEquals(width, content.getWidth(), 0);
            assertEquals(height, content.getHeight(), 0);
            assertSame(dockable, pane.getActiveDockable());
            return null;
        });
    }

    @Test
    public void dragImageUsesAtlantaFxPreviewColors() {
        invoke(() -> {
            DockingContext context = context(dockable("a"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);
            Scene scene = pane.getScene();
            scene.getStylesheets().add(getClass().getResource("docking-drag-theme.css").toExternalForm());
            GemsFXAtlantaFX.applyTo(scene);
            scene.getRoot().applyCss();
            scene.getRoot().layout();

            Image image = (Image) invokeSkinPrivate((DockingPaneSkin) pane.getSkin(),
                    "createDockableDragView", new Class<?>[]{String.class}, "a");

            assertEquals(Color.web("#123456"), image.getPixelReader().getColor(5, 5));
            assertEquals(Color.web("#654321"), image.getPixelReader().getColor(0, 0));
            coloredBounds(image, Color.LIME);
            return null;
        });
    }

    @Test
    public void dragImageRendersUnselectedContentWithoutChangingSelectionOrLeavingItAttached() {
        invoke(() -> {
            Region content = new Region();
            content.setPrefSize(800, 400);
            content.setStyle("-fx-background-color: blue;");
            Dockable hidden = new Dockable("a", "Background editor", content);
            Dockable selected = dockable("b");
            DockingContext context = context(hidden, selected);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            double width = content.getWidth();
            double height = content.getHeight();
            Node selectedParent = selected.getContent().getParent();

            Image image = (Image) invokeSkinPrivate((DockingPaneSkin) pane.getSkin(),
                    "createDockableDragView", new Class<?>[]{String.class}, "a");

            Rectangle2D renderedContent = coloredBounds(image, Color.BLUE);
            assertEquals(2, renderedContent.getWidth() / renderedContent.getHeight(), 0.03);
            assertNull(content.getParent());
            assertNull(content.getScene());
            assertEquals(width, content.getWidth(), 0);
            assertEquals(height, content.getHeight(), 0);
            assertSame(selected, pane.getActiveDockable());
            assertSame(selectedParent, selected.getContent().getParent());
            return null;
        });
    }

    @Test
    public void clickingTitleSelectsItsContent() {
        invoke(() -> {
            Dockable a = dockable("a");
            Dockable b = dockable("b");
            DockingContext context = context(a, b);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node label = title(pane, "a").lookup(".docking-tab-title");

            mouse(label, MouseEvent.MOUSE_PRESSED, true);
            mouse(label, MouseEvent.MOUSE_RELEASED, true);
            mouse(label, MouseEvent.MOUSE_CLICKED, true);

            assertSame(a, pane.getActiveDockable());
            assertNotNull(a.getContent().getParent());
            assertNull(b.getContent().getParent());
            return null;
        });
    }

    @Test
    public void clickAfterDraggingDoesNotSelectOrRebuildTheSource() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            Node source = title(pane, "a");
            DockingLayoutNode before = pane.getLayoutRoot();

            mouse(source, MouseEvent.MOUSE_PRESSED, true);
            mouse(source, MouseEvent.MOUSE_RELEASED, false);
            mouse(source, MouseEvent.MOUSE_CLICKED, false);

            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertSame(source, title(pane, "a"));
            return null;
        });
    }

    @Test
    public void closeButtonClickDoesNotSelectItsTabWhenCloseIsVetoed() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.setCanCloseDockable(d -> false);
            layout(pane);
            Button close = (Button) title(pane, "a").lookup(".docking-tab-close-button");
            DockingLayoutNode before = pane.getLayoutRoot();

            close.fire();
            mouse(close, MouseEvent.MOUSE_CLICKED, true);

            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void skinDoesNotUseJavaFxTabPane() {
        invoke(() -> {
            DockingContext context = context(dockable("a"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            layout(pane);

            assertFalse(containsTabPane(pane));
            return null;
        });
    }

    private static boolean containsTabPane(Node node) {
        if (node instanceof TabPane) {
            return true;
        }
        if (node instanceof javafx.scene.Parent) {
            for (Node child : ((javafx.scene.Parent) node).getChildrenUnmodifiable()) {
                if (containsTabPane(child)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Test
    public void arrowKeysMoveSelectionBetweenTitles() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            press(title(pane, "b"), KeyCode.LEFT);
            assertEquals("a", pane.getActiveDockable().getId());
            press(title(pane, "a"), KeyCode.RIGHT);
            assertEquals("b", pane.getActiveDockable().getId());
            return null;
        });
    }

    @Test
    public void keyboardCloseRemovesDockableWhenPolicyAllows() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);

            press(title(pane, "b"), KeyCode.DELETE);
            pane.applyCss();
            pane.layout();

            assertEquals(1, context.getDockables().size());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            assertEquals("a", pane.getActiveDockable().getId());
            assertEquals(1, titles(pane).size());
            return null;
        });
    }

    @Test
    public void closeButtonHonoursVeto() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            layout(pane);
            context.setCanCloseDockable(d -> false);
            DockingLayoutNode before = pane.getLayoutRoot();

            ((Button) title(pane, "b").lookup(".docking-tab-close-button")).fire();
            press(title(pane, "b"), KeyCode.DELETE);

            assertEquals(2, context.getDockables().size());
            assertSame(before, pane.getLayoutRoot());
            assertEquals("b", pane.getActiveDockable().getId());
            assertEquals(2, titles(pane).size());

            context.setCanCloseDockable(d -> true);
            ((Button) title(pane, "b").lookup(".docking-tab-close-button")).fire();
            assertEquals(1, context.getDockables().size());
            return null;
        });
    }

    @Test
    public void selectDockableUpdatesSnapshotAndActiveDockable() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);

            pane.selectDockable("a");

            assertEquals("a", ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getSelectedId());
            assertEquals("a", pane.getActiveDockable().getId());
            assertThrows(IllegalArgumentException.class, () -> pane.selectDockable("missing"));
            return null;
        });
    }

    @Test
    public void publicPaneMutationsRejectCallsOffTheFxThread() {
        DockingPane pane = invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane created = context.createPane();
            context.openDockable(created, "a", DockingPosition.CENTER);
            context.openDockable(created, "b", DockingPosition.RIGHT);
            return created;
        });

        assertThrows(IllegalStateException.class, () -> pane.selectDockable("a"));
        assertThrows(IllegalStateException.class, () -> pane.updateDividerPositions(List.of(), List.of(0.7)));
        assertThrows(IllegalStateException.class, () -> pane.canMoveDockableToPaneEdge("a", DockingPosition.BOTTOM));
        assertThrows(IllegalStateException.class, () -> pane.moveDockableToPaneEdge("a", DockingPosition.BOTTOM));
    }

    @Test
    public void dividerVectorChangesPublishOneSnapshotAndRejectInvalidUpdatesAtomically() {
        runFx(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.RIGHT);
            AtomicInteger snapshots = new AtomicInteger();
            pane.layoutRootProperty().addListener(obs -> snapshots.incrementAndGet());
            DockingLayoutNode before = pane.getLayoutRoot();
            assertThrows(IllegalArgumentException.class,
                    () -> pane.updateDividerPositions(List.of(), List.of(0.8, 0.2)));
            assertSame(before, pane.getLayoutRoot());
            assertEquals(0, snapshots.get());
            pane.updateDividerPositions(List.of(), List.of(0.3, 0.7));
            assertEquals(1, snapshots.get());
            assertEquals(List.of(0.3, 0.7), ((DockingLayoutNode.Split) pane.getLayoutRoot()).getDividerPositions());
        });
    }

    @Test
    public void flattenedDividersResizeOnlyAdjacentItemsAndPreserveSavedProportions() {
        runFx(() -> {
            for (Orientation orientation : Orientation.values()) {
                DockingPosition direction = orientation == Orientation.HORIZONTAL
                        ? DockingPosition.RIGHT : DockingPosition.BOTTOM;
                DockingContext context = context(dockable("a"), dockable("b"), dockable("c"), dockable("d"));
                DockingPane pane = context.createPane();
                context.openDockable(pane, "a", DockingPosition.CENTER);
                context.openDockable(pane, "b", direction);
                context.openDockable(pane, "c", direction);
                pane.selectDockable("a");
                context.openDockable(pane, "d", direction);
                layout(pane);
                SplitPane split = (SplitPane) pane.lookup(".docking-split");
                assertEquals(4, split.getItems().size());
                assertEquals(1, pane.lookupAll(".docking-split").size());
                assertArrayEquals(new double[]{0.25, 0.5, 0.75}, split.getDividerPositions(), 0.002);

                for (int divider = 0; divider < 3; divider++) {
                    double[] sizes = split.getItems().stream().mapToDouble(item ->
                            orientation == Orientation.HORIZONTAL
                                    ? item.getLayoutBounds().getWidth() : item.getLayoutBounds().getHeight()).toArray();
                    double[] positions = split.getDividerPositions();
                    split.setDividerPosition(divider, positions[divider] + 0.04);
                    pane.layout();
                    assertSame(split, pane.lookup(".docking-split"));
                    for (int item = 0; item < 4; item++) {
                        double size = orientation == Orientation.HORIZONTAL
                                ? split.getItems().get(item).getLayoutBounds().getWidth()
                                : split.getItems().get(item).getLayoutBounds().getHeight();
                        if (item == divider) {
                            assertTrue("The preceding dockable must grow", size > sizes[item]);
                        } else if (item == divider + 1) {
                            assertTrue("The following dockable must shrink", size < sizes[item]);
                        } else {
                            assertEquals("Other dockables must retain their size", sizes[item], size, 1);
                        }
                    }
                    positions = split.getDividerPositions();
                    DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
                    assertEquals(4, root.getChildren().size());
                    assertArrayEquals(positions, root.getDividerPositions().stream()
                            .mapToDouble(Double::doubleValue).toArray(), 0);
                }
                pane.selectDockable("a");
                assertSame(split, pane.lookup(".docking-split"));
                double[] positions = split.getDividerPositions();
                DockingContext restoredContext = context(dockable("a"), dockable("b"), dockable("c"), dockable("d"));
                DockingPane restored = restoredContext.createPane();
                restoredContext.restoreLayout(context.getLayout());
                layout(restored);
                SplitPane restoredSplit = (SplitPane) restored.lookup(".docking-split");
                assertEquals(4, restoredSplit.getItems().size());
                assertArrayEquals(positions, restoredSplit.getDividerPositions(), 0.002);
            }
        });
    }

    @Test
    public void flatteningStopsAtOppositeOrientationSplits() {
        runFx(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"), dockable("d"), dockable("e"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.RIGHT);
            context.openDockable(pane, "d", DockingPosition.BOTTOM);
            context.openDockable(pane, "e", DockingPosition.BOTTOM);
            layout(pane);
            SplitPane outer = (SplitPane) pane.lookup(".docking-split");
            assertEquals(3, outer.getItems().size());
            assertEquals(2, pane.lookupAll(".docking-split").size());
            SplitPane inner = (SplitPane) outer.getItems().get(2);
            assertEquals(Orientation.VERTICAL, inner.getOrientation());
            assertEquals(3, inner.getItems().size());
            double[] positions = outer.getDividerPositions();
            inner.setDividerPosition(0, 0.7);
            pane.layout();
            assertArrayEquals(positions, outer.getDividerPositions(), 0);
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            DockingLayoutNode.Split vertical = (DockingLayoutNode.Split) root.getChildren().get(2);
            assertArrayEquals(inner.getDividerPositions(), vertical.getDividerPositions().stream()
                    .mapToDouble(Double::doubleValue).toArray(), 0);
        });
    }

    @Test
    public void flattenedCollapsedRangesRetainFiniteModelRatios() {
        runFx(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.RIGHT);
            layout(pane);
            SplitPane split = (SplitPane) pane.lookup(".docking-split");
            assertEquals(2, split.getDividers().size());
            for (double endpoint : new double[]{0, 1}) {
                split.setDividerPositions(endpoint, endpoint);
                DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
                assertEquals(3, root.getChildren().size());
                assertTrue(root.getDividerPositions().stream().allMatch(Double::isFinite));
                assertNotNull(context.getLayout());
            }
        });
    }

    @Test
    public void draggingDividersUpdatesSnapshotWithoutRebuildingTheDraggedSplit() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.BOTTOM);
            layout(pane);

            List<Node> splits = new ArrayList<>(pane.lookupAll(".docking-split"));
            javafx.scene.control.SplitPane outer = (javafx.scene.control.SplitPane) splits.get(0);
            javafx.scene.control.SplitPane inner = (javafx.scene.control.SplitPane) splits.get(1);

            outer.setDividerPositions(0.3);
            inner.setDividerPositions(0.8);

            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(0.3, root.getDividerPositions().get(0), 1e-9);
            assertEquals(0.8, ((DockingLayoutNode.Split) root.getChildren().get(1)).getDividerPositions().get(0), 1e-9);
            assertSame(outer, pane.lookup(".docking-split"));
            assertEquals(0.3, outer.getDividerPositions()[0], 1e-9);

            pane.selectDockable("a");
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(0.3, root.getDividerPositions().get(0), 1e-9);
            assertEquals(0.3, ((javafx.scene.control.SplitPane) pane.lookup(".docking-split"))
                    .getDividerPositions()[0], 1e-9);
            return null;
        });
    }

    @Test
    public void draggingDividersToEndpointsPreservesVisibleProportion() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            layout(pane);

            javafx.scene.control.SplitPane split = (javafx.scene.control.SplitPane) pane.lookup(".docking-split");
            for (double endpoint : new double[]{0.0, 1.0}) {
                split.setDividerPositions(endpoint);
                assertEquals(endpoint, ((DockingLayoutNode.Split) pane.getLayoutRoot()).getDividerPositions().get(0), 0);
                assertSame(split, pane.lookup(".docking-split"));
                assertEquals(endpoint, split.getDividerPositions()[0], 0);
            }

            pane.selectDockable("a");
            assertEquals(1.0, ((DockingLayoutNode.Split) pane.getLayoutRoot()).getDividerPositions().get(0), 0);
            assertEquals(1.0, ((javafx.scene.control.SplitPane) pane.lookup(".docking-split"))
                    .getDividerPositions()[0], 0);
            return null;
        });
    }

    @Test
    public void resolverPathReordersWithinSameGroupUsingHoveredTitle() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.CENTER);

            assertTrue(pane.moveDockable("c", 50, 50, 100, 100, "a"));
            DockingLayoutNode.Tabs tabs = (DockingLayoutNode.Tabs) pane.getLayoutRoot();
            assertEquals(List.of("a", "c", "b"), tabs.getDockableIds());
            assertEquals("c", tabs.getSelectedId());
            return null;
        });
    }

    @Test
    public void resolverPathMovesToEdgeWithExpectedSplitOrientationAndOrder() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 98, 50, 100, 100, "c"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.HORIZONTAL, root.getOrientation());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) root.getChildren().get(0)).getDockableIds());
            assertEquals(3, root.getChildren().size());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) root.getChildren().get(1)).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) root.getChildren().get(2)).getDockableIds());
            return null;
        });
    }

    @Test
    public void resolverPathSplitsSelectedTabFromItsOwnGroupAtAnEdge() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);

            assertTrue(pane.moveDockable("b", 2, 50, 100, 100, "b"));

            DockingLayoutNode.Split split = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.HORIZONTAL, split.getOrientation());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) split.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) split.getChildren().get(1)).getDockableIds());
            return null;
        });
    }

    @Test
    public void paneEdgeApiPlacesDockableBelowAllExistingGroups() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);
            context.openDockable(pane, "c", DockingPosition.CENTER);
            layout(pane);

            assertTrue(pane.canMoveDockableToPaneEdge("c", DockingPosition.BOTTOM));
            assertTrue(pane.moveDockableToPaneEdge("c", DockingPosition.BOTTOM));
            pane.applyCss();
            pane.layout();

            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.VERTICAL, root.getOrientation());
            DockingLayoutNode.Split top = (DockingLayoutNode.Split) root.getChildren().get(0);
            assertEquals(Orientation.HORIZONTAL, top.getOrientation());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) top.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) top.getChildren().get(1)).getDockableIds());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) root.getChildren().get(1)).getDockableIds());
            assertEquals("c", pane.getActiveDockable().getId());
            assertEquals(3, titles(pane).size());
            return null;
        });
    }

    @Test
    public void resolverPathMovesToTopAndBottomWithExpectedOrder() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 70, 2, 100, 100, "c"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            DockingLayoutNode.Split topSplit = (DockingLayoutNode.Split) root.getChildren().get(1);
            assertEquals(Orientation.VERTICAL, topSplit.getOrientation());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) topSplit.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) topSplit.getChildren().get(1)).getDockableIds());

            assertTrue(pane.moveDockable("a", 70, 98, 100, 100, "c"));
            root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            DockingLayoutNode.Split bottomSplit = (DockingLayoutNode.Split) root.getChildren().get(1);
            assertEquals(Orientation.VERTICAL, bottomSplit.getOrientation());
            assertEquals(List.of("c"), ((DockingLayoutNode.Tabs) bottomSplit.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) bottomSplit.getChildren().get(1)).getDockableIds());
            return null;
        });
    }

    @Test
    public void movingToCenterCollapsesEmptyGroupsAndRedundantSplits() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("b", 20, 50, 100, 100, "a"));
            assertTrue(pane.getLayoutRoot() instanceof DockingLayoutNode.Tabs);
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) pane.getLayoutRoot()).getDockableIds());
            return null;
        });
    }

    @Test
    public void edgeMoveAlsoCollapsesSourceGroupAndRedundantSplit() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.RIGHT);

            assertTrue(pane.moveDockable("a", 70, 2, 100, 100, "b"));
            DockingLayoutNode.Split root = (DockingLayoutNode.Split) pane.getLayoutRoot();
            assertEquals(Orientation.VERTICAL, root.getOrientation());
            assertEquals(List.of("a"), ((DockingLayoutNode.Tabs) root.getChildren().get(0)).getDockableIds());
            assertEquals(List.of("b"), ((DockingLayoutNode.Tabs) root.getChildren().get(1)).getDockableIds());
            return null;
        });
    }

    @Test
    public void resolverPathKeepsSourceWhenDropIsRejectedOrCancelled() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.RIGHT);

            DockingLayoutNode beforeRejectedMove = pane.getLayoutRoot();
            assertFalse(pane.moveDockable("c", 75, 50, 100, 100, null));
            assertFalse(pane.moveDockable("a", 50, 50, 0, 100, "b"));
            assertSame(beforeRejectedMove, pane.getLayoutRoot());
            assertEquals(List.of("a", "b"), ((DockingLayoutNode.Tabs) ((DockingLayoutNode.Split) pane.getLayoutRoot())
                    .getChildren().get(0)).getDockableIds());
            return null;
        });
    }

    @Test
    public void skinResolverUsesHoveredPickPathAndDragDoneClearsPreview() {
        invoke(() -> {
            DockingContext context = context(dockable("a"), dockable("b"), dockable("c"));
            DockingPane pane = context.createPane();
            context.openDockable(pane, "a", DockingPosition.CENTER);
            context.openDockable(pane, "b", DockingPosition.CENTER);
            context.openDockable(pane, "c", DockingPosition.CENTER);
            layout(pane);

            DockingPaneSkin skin = (DockingPaneSkin) pane.getSkin();
            Node picked = title(pane, "a").lookup(".docking-tab-title");
            Object resolved = invokeSkinPrivate(skin, "resolveDropTarget",
                    new Class<?>[]{double.class, double.class, Node.class, String.class}, 50d, 50d, picked, "c");
            assertNotNull(resolved);
            try {
                String targetId = (String) resolved.getClass().getMethod("getTargetId").invoke(resolved);
                assertEquals("a", targetId);
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }

            invokeSkinPrivate(skin, "showDropPreview", new Class<?>[]{resolved.getClass()}, resolved);
            Node zonePreview = pane.lookup(".docking-drop-zone-preview");
            assertNotNull(zonePreview);
            assertTrue(zonePreview.isVisible());

            DockingLayoutNode before = pane.getLayoutRoot();
            assertFalse(pane.moveDockable("c", 50, 50, 100, 100, null));
            invokeSkinPrivate(skin, "finishDrag", new Class<?>[]{});
            assertFalse(zonePreview.isVisible());
            assertSame(before, pane.getLayoutRoot());
            return null;
        });
    }
}
