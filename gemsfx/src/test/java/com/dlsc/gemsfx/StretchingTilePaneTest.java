package com.dlsc.gemsfx;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link StretchingTilePane}.
 */
public class StretchingTilePaneTest extends FxTestBase {

    @Test
    public void defaultHgap() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        assertEquals(0.0, pane.getHgap(), 1e-9);
    }

    @Test
    public void defaultVgap() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        assertEquals(0.0, pane.getVgap(), 1e-9);
    }

    @Test
    public void contentBiasIsHorizontal() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        assertEquals(Orientation.HORIZONTAL, pane.getContentBias());
    }

    @Test
    public void hgapVgapConstructor() {
        StretchingTilePane pane = invoke(() -> new StretchingTilePane(8, 4));
        assertEquals(8.0, pane.getHgap(), 1e-9);
        assertEquals(4.0, pane.getVgap(), 1e-9);
    }

    @Test
    public void setAndGetHgap() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        runFx(() -> pane.setHgap(10));
        assertEquals(10.0, pane.getHgap(), 1e-9);
    }

    @Test
    public void setAndGetVgap() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        runFx(() -> pane.setVgap(5));
        assertEquals(5.0, pane.getVgap(), 1e-9);
    }

    @Test
    public void hgapPropertyListener() {
        StretchingTilePane pane = invoke(StretchingTilePane::new);
        boolean[] fired = {false};
        runFx(() -> pane.hgapProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setHgap(3));
        assertTrue(fired[0]);
    }

    @Test
    public void childrenAddedViaVarargConstructor() {
        Label l1 = new Label("A");
        Label l2 = new Label("B");
        StretchingTilePane pane = invoke(() -> new StretchingTilePane(0, 0, l1, l2));
        assertEquals(2, pane.getChildren().size());
    }

    @Test
    public void layoutWithChildren() {
        Label l1 = new Label("A");
        Label l2 = new Label("B");
        StretchingTilePane pane = invoke(() -> new StretchingTilePane(4, 4, l1, l2));
        layout(pane);
        // Just verify layout completes without error and children are still present
        assertEquals(2, pane.getChildren().size());
    }
}
