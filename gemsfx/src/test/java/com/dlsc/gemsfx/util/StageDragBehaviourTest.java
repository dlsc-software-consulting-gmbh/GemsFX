package com.dlsc.gemsfx.util;

import com.dlsc.gemsfx.FxTestBase;
import javafx.event.Event;
import javafx.event.EventType;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.junit.Test;

import static org.junit.Assert.*;

public class StageDragBehaviourTest extends FxTestBase {

    @Test
    public void draggingAnyNodeMovesStageWithoutReplacingOrConsumingHandlers() {
        runFx(() -> {
            Rectangle node = new Rectangle(100, 100);
            Stage stage = stageWith(node);
            boolean[] handled = {false};
            node.setOnMouseDragged(event -> handled[0] = true);
            StageDragBehaviour.install(node);

            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 140, 225);
            assertTrue(handled[0]);
        });
    }

    @Test
    public void onlyPrimaryPressStartsDragging() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour.install(node);

            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.SECONDARY, false);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.SECONDARY, false);
            assertPosition(stage, 100, 200);
        });
    }

    @Test
    public void disabledBehaviourDoesNotStartOrContinueDragging() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour behaviour = StageDragBehaviour.install(node);
            behaviour.setEnabled(false);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);

            behaviour.setEnabled(true);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            behaviour.setEnabled(false);
            behaviour.setEnabled(true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 180, 280, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);
        });
    }

    @Test
    public void detachingNodeCancelsDragEvenAfterReattachment() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour.install(node);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);

            StackPane root = (StackPane) node.getScene().getRoot();
            root.getChildren().remove(node);
            root.getChildren().add(node);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);
        });
    }

    @Test
    public void detachingSceneCancelsDragEvenAfterReattachment() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour.install(node);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);

            Scene scene = stage.getScene();
            stage.setScene(null);
            stage.setScene(scene);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);
        });
    }

    @Test
    public void releaseOrLossOfPrimaryButtonEndsDragging() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour.install(node);

            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_RELEASED, 120, 230, MouseButton.PRIMARY, false);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);

            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.NONE, false);
            fire(node, MouseEvent.MOUSE_DRAGGED, 180, 280, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);
        });
    }

    @Test
    public void uninstallRemovesFiltersAndAllowsReinstallation() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            Stage stage = stageWith(node);
            StageDragBehaviour behaviour = StageDragBehaviour.install(node);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            behaviour.uninstall();
            assertFalse(behaviour.isInstalled());
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 100, 200);

            StageDragBehaviour replacement = StageDragBehaviour.install(node);
            behaviour.uninstall();
            assertTrue(replacement.isInstalled());
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 140, 225);
        });
    }

    @Test
    public void canInstallBeforeNodeIsAttachedToStage() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            StageDragBehaviour.install(node);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            new Scene(new StackPane(node));
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);

            Stage stage = new Stage();
            stage.setScene(node.getScene());
            stage.setX(100);
            stage.setY(200);
            fire(node, MouseEvent.MOUSE_PRESSED, 120, 230, MouseButton.PRIMARY, true);
            fire(node, MouseEvent.MOUSE_DRAGGED, 160, 255, MouseButton.PRIMARY, true);
            assertPosition(stage, 140, 225);
        });
    }

    @Test
    public void duplicateInstallationIsRejected() {
        runFx(() -> {
            Rectangle node = new Rectangle();
            StageDragBehaviour.install(node);
            assertThrows(IllegalStateException.class, () -> StageDragBehaviour.install(node));
        });
    }

    private static Stage stageWith(Rectangle node) {
        Stage stage = new Stage();
        stage.setScene(new Scene(new StackPane(node), 400, 300));
        stage.setX(100);
        stage.setY(200);
        return stage;
    }

    private static void assertPosition(Stage stage, double x, double y) {
        assertEquals(x, stage.getX(), 0.1);
        assertEquals(y, stage.getY(), 0.1);
    }

    private static void fire(Rectangle node, EventType<MouseEvent> type,
                             double screenX, double screenY, MouseButton button,
                             boolean primaryButtonDown) {
        Event.fireEvent(node, new MouseEvent(type, 20, 30, screenX, screenY, button, 1,
                false, false, false, false, primaryButtonDown, false, false,
                false, false, true, null));
    }
}
