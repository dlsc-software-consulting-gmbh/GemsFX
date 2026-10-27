package com.dlsc.gemsfx;

import javafx.util.Duration;
import javafx.event.Event;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link GlassPane}.
 */
public class GlassPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        GlassPane pane = invoke(GlassPane::new);
        assertTrue(pane.getStyleClass().contains("glass-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        GlassPane pane = invoke(GlassPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultBlockingOpacity() {
        GlassPane pane = invoke(GlassPane::new);
        assertEquals(0.5, pane.getBlockingOpacity(), 1e-9);
    }

    @Test
    public void defaultFadeInOut() {
        GlassPane pane = invoke(GlassPane::new);
        assertFalse(pane.isFadeInOut());
    }

    @Test
    public void defaultHide() {
        GlassPane pane = invoke(GlassPane::new);
        assertTrue(pane.isHide());
    }

    @Test
    public void defaultFadeInOutDuration() {
        GlassPane pane = invoke(GlassPane::new);
        assertEquals(Duration.millis(100), pane.getFadeInOutDuration());
    }

    @Test
    public void setBlockingOpacity() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setBlockingOpacity(0.8));
        assertEquals(0.8, pane.getBlockingOpacity(), 1e-9);
    }

    @Test
    public void setFadeInOut() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setFadeInOut(true));
        assertTrue(pane.isFadeInOut());
    }

    @Test
    public void setHide() {
        GlassPane pane = invoke(GlassPane::new);
        runFx(() -> pane.setHide(false));
        assertFalse(pane.isHide());
    }

    @Test
    public void blockingOpacityPropertyListener() {
        GlassPane pane = invoke(GlassPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.blockingOpacityProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setBlockingOpacity(0.3));
        assertTrue(fired[0]);
    }

    @Test
    public void stageDraggingIsDisabledByDefault() {
        GlassPane pane = invoke(GlassPane::new);
        assertFalse(pane.isEnableStageDragging());
    }

    @Test
    public void enabledStageDraggingMovesContainingStage() {
        GlassPane pane = invoke(GlassPane::new);
        pane.setEnableStageDragging(true);

        Stage stage = invoke(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane(pane), 400, 300));
            owner.setX(100);
            owner.setY(100);
            owner.show();
            pane.setHide(false);

            Event.fireEvent(pane, mouseEvent(MouseEvent.MOUSE_PRESSED, 20, 30, 120, 130,
                    MouseButton.PRIMARY, true));
            Event.fireEvent(pane, mouseEvent(MouseEvent.MOUSE_DRAGGED, 60, 55, 160, 155,
                    MouseButton.PRIMARY, true));
            return owner;
        });

        try {
            assertEquals(140, invoke(stage::getX), 0.1);
            assertEquals(125, invoke(stage::getY), 0.1);
        } finally {
            runFx(stage::hide);
        }
    }

    @Test
    public void disabledStageDraggingDoesNotMoveContainingStage() {
        GlassPane pane = invoke(GlassPane::new);

        Stage stage = invoke(() -> {
            Stage owner = new Stage();
            owner.setScene(new Scene(new StackPane(pane), 400, 300));
            owner.setX(100);
            owner.setY(100);
            owner.show();
            pane.setHide(false);

            Event.fireEvent(pane, mouseEvent(MouseEvent.MOUSE_PRESSED, 20, 30, 120, 130,
                    MouseButton.PRIMARY, true));
            Event.fireEvent(pane, mouseEvent(MouseEvent.MOUSE_DRAGGED, 60, 55, 160, 155,
                    MouseButton.PRIMARY, true));
            return owner;
        });

        try {
            assertEquals(100, invoke(stage::getX), 0.1);
            assertEquals(100, invoke(stage::getY), 0.1);
        } finally {
            runFx(stage::hide);
        }
    }

    private static MouseEvent mouseEvent(javafx.event.EventType<MouseEvent> type,
                                         double x, double y, double screenX, double screenY,
                                         MouseButton button, boolean primaryButtonDown) {
        return new MouseEvent(type, x, y, screenX, screenY, button, 1,
                false, false, false, false, primaryButtonDown, false, false,
                false, false, true, null);
    }
}
