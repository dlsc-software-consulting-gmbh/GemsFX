package com.dlsc.gemsfx;

import javafx.css.PseudoClass;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

public class DockingFocusBorderTest extends FxTestBase {

    private static final PseudoClass HIGHLIGHTED = PseudoClass.getPseudoClass("focus-highlighted");

    @Test
    public void optionalBorderFollowsDescendantFocusWithoutChangingSelectionOrLayout() {
        runFx(() -> {
            TextField firstInput = new TextField();
            TextField secondInput = new TextField();
            Dockable first = new Dockable("first", "First", new VBox(firstInput));
            Dockable second = new Dockable("second", "Second", new VBox(secondInput));
            DockingContext context = new DockingContext(
                    id -> Optional.of(id.equals("first") ? first : second), pane -> null);
            DockingPane pane = context.createPane();
            pane.setShowFocusBorder(false);
            context.openDockable(pane, "first", DockingPosition.CENTER);
            context.openDockable(pane, "second", DockingPosition.RIGHT);
            Button toolbar = new Button("Outside docking area");
            StackPane root = new StackPane(new VBox(toolbar, pane));
            root.setStyle("-fx-focus-color: #123456;");
            FocusTestStage stage = new FocusTestStage();
            stage.setScene(new Scene(root, 800, 600));
            root.applyCss();
            root.layout();
            stage.setWindowFocused(true);
            firstInput.requestFocus();
            assertSame(firstInput, stage.getScene().getFocusOwner());
            Region firstGroup = groupOf(firstInput);
            Region secondGroup = groupOf(secondInput);
            DockingLayoutNode layout = pane.getLayoutRoot();

            assertFalse(pane.isShowFocusBorder());
            assertFalse(firstGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            pane.setShowFocusBorder(true);
            root.applyCss();
            root.layout();
            assertTrue(firstGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            assertFalse(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            assertEquals(Color.web("#123456"), firstGroup.getBorder().getStrokes().get(0).getTopStroke());
            assertEquals(1, firstGroup.getBorder().getStrokes().get(0).getWidths().getTop(), 0);
            assertEquals(1, secondGroup.getBorder().getStrokes().get(0).getWidths().getTop(), 0);
            Insets firstInsets = firstGroup.getInsets();

            secondInput.requestFocus();
            root.applyCss();
            assertFalse(firstGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            assertTrue(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            assertEquals(firstInsets, firstGroup.getInsets());
            assertSame(layout, pane.getLayoutRoot());
            assertSame(second, pane.getActiveDockable());
            assertSame(secondInput, stage.getScene().getFocusOwner());

            toolbar.requestFocus();
            assertFalse(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            secondInput.requestFocus();
            assertTrue(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            stage.setWindowFocused(false);
            assertFalse(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            stage.setWindowFocused(true);
            assertTrue(secondGroup.getPseudoClassStates().contains(HIGHLIGHTED));
            pane.updateDividerPosition(List.of(), 0.6);
            assertTrue(groupOf(secondInput).getPseudoClassStates().contains(HIGHLIGHTED));
            pane.setShowFocusBorder(false);
            assertFalse(groupOf(secondInput).getPseudoClassStates().contains(HIGHLIGHTED));
        });
    }

    @Test
    public void focusFeedbackTracksPaneReattachmentAndSkinDisposal() {
        runFx(() -> {
            TextField input = new TextField();
            DockingContext context = new DockingContext(
                    id -> Optional.of(new Dockable(id, id, new VBox(input))), pane -> null);
            DockingPane pane = context.createPane();
            context.openDockable(pane, "editor", DockingPosition.CENTER);
            pane.setShowFocusBorder(true);
            StackPane oldRoot = new StackPane(pane);
            FocusTestStage oldStage = new FocusTestStage();
            oldStage.setScene(new Scene(oldRoot, 800, 600));
            oldRoot.applyCss();
            oldRoot.layout();
            oldStage.setWindowFocused(true);
            input.requestFocus();
            Region group = groupOf(input);
            assertTrue(group.getPseudoClassStates().contains(HIGHLIGHTED));

            oldRoot.getChildren().clear();
            StackPane newRoot = new StackPane(pane);
            FocusTestStage newStage = new FocusTestStage();
            newStage.setScene(new Scene(newRoot, 800, 600));
            assertFalse(group.getPseudoClassStates().contains(HIGHLIGHTED));
            newStage.setWindowFocused(true);
            input.requestFocus();
            assertTrue(group.getPseudoClassStates().contains(HIGHLIGHTED));
            oldStage.setWindowFocused(false);
            assertTrue(group.getPseudoClassStates().contains(HIGHLIGHTED));

            pane.setSkin(null);
            newStage.setWindowFocused(false);
            newStage.setWindowFocused(true);
            assertFalse(group.getPseudoClassStates().contains(HIGHLIGHTED));
        });
    }

    private static Region groupOf(Node node) {
        for (Node ancestor = node; ancestor != null; ancestor = ancestor.getParent()) {
            if (ancestor.getStyleClass().contains("docking-tab-pane")) {
                return (Region) ancestor;
            }
        }
        throw new AssertionError("Node is not inside a tab group");
    }

    private static final class FocusTestStage extends Stage {
        private void setWindowFocused(boolean focused) {
            // Simulate native focus changes without taking focus from the desktop.
            try {
                Class.forName("com.sun.javafx.stage.WindowHelper")
                        .getMethod("setFocused", Window.class, boolean.class).invoke(null, this, focused);
            } catch (ReflectiveOperationException ex) {
                throw new AssertionError(ex);
            }
        }
    }
}
