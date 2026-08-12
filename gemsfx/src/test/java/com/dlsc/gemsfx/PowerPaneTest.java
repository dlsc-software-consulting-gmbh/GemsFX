package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link PowerPane}.
 */
public class PowerPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        PowerPane pane = invoke(PowerPane::new);
        assertTrue(pane.getStyleClass().contains("power-pane"));
    }

    @Test
    public void defaultContentNull() {
        PowerPane pane = invoke(PowerPane::new);
        assertNull(pane.getContent());
    }

    @Test
    public void contentConstructorSetsContent() {
        Label label = new Label("Content");
        PowerPane pane = invoke(() -> new PowerPane(label));
        assertSame(label, pane.getContent());
    }

    @Test
    public void setAndGetContent() {
        PowerPane pane = invoke(PowerPane::new);
        Label label = new Label("Test");
        runFx(() -> pane.setContent(label));
        assertSame(label, pane.getContent());
    }

    @Test
    public void getDialogPaneNotNull() {
        PowerPane pane = invoke(PowerPane::new);
        assertNotNull(pane.getDialogPane());
    }

    @Test
    public void getDrawerStackPaneNotNull() {
        PowerPane pane = invoke(PowerPane::new);
        assertNotNull(pane.getDrawerStackPane());
    }

    @Test
    public void getHiddenSidesPaneNotNull() {
        PowerPane pane = invoke(PowerPane::new);
        assertNotNull(pane.getHiddenSidesPane());
    }

    @Test
    public void getInfoCenterPaneNotNull() {
        PowerPane pane = invoke(PowerPane::new);
        assertNotNull(pane.getInfoCenterPane());
    }

    @Test
    public void contentPropertyListener() {
        PowerPane pane = invoke(PowerPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.contentProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setContent(new Label("New")));
        assertTrue(fired[0]);
    }
}
