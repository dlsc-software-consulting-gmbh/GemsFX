package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link ChipView}.
 */
public class ChipViewTest extends FxTestBase {

    @Test
    public void testDefaultConstruction() {
        ChipView<String> chip = invoke(ChipView::new);
        assertNotNull(chip);
        assertNull(chip.getValue());
        // the text defaults to a localized "untitled" text, hence no assertion on the concrete value
        assertNotNull(chip.getText());
        assertNull(chip.getGraphic());
    }

    @Test
    public void testStyleClass() {
        ChipView<String> chip = invoke(ChipView::new);
        assertTrue(chip.getStyleClass().contains("chip-view"));
    }

    @Test
    public void testGetUserAgentStylesheet() {
        ChipView<String> chip = invoke(ChipView::new);
        assertNotNull(chip.getUserAgentStylesheet());
    }

    @Test
    public void testCreateDefaultSkin() {
        ChipView<String> chip = invoke(ChipView::new);
        layout(chip);
        assertNotNull(chip.getSkin());
    }

    @Test
    public void testValueProperty() {
        ChipView<String> chip = invoke(ChipView::new);
        runFx(() -> chip.setValue("tag1"));
        assertEquals("tag1", chip.getValue());
        assertEquals("tag1", chip.valueProperty().get());
    }

    @Test
    public void testTextProperty() {
        ChipView<String> chip = invoke(ChipView::new);
        runFx(() -> chip.setText("label"));
        assertEquals("label", chip.getText());
    }

    @Test
    public void testGraphicProperty() {
        ChipView<String> chip = invoke(ChipView::new);
        Label lbl = invoke(() -> new Label("G"));
        runFx(() -> chip.setGraphic(lbl));
        assertEquals(lbl, chip.getGraphic());
    }

    @Test
    public void testOnCloseProperty() {
        ChipView<String> chip = invoke(ChipView::new);
        boolean[] fired = {false};
        runFx(() -> chip.setOnClose(v -> fired[0] = true));
        runFx(() -> chip.getOnClose().accept("x"));
        assertTrue(fired[0]);
    }

    @Test
    public void testDefaultContentDisplay() {
        ChipView<String> chip = invoke(ChipView::new);
        assertNotNull(chip.getContentDisplay());
    }
}
