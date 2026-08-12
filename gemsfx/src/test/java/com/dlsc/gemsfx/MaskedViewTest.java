package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link MaskedView}.
 */
public class MaskedViewTest extends FxTestBase {

    @Test
    public void styleClass() {
        MaskedView view = invoke(MaskedView::new);
        assertTrue(view.getStyleClass().contains("masked-view"));
    }

    @Test
    public void defaultFadingSize() {
        MaskedView view = invoke(MaskedView::new);
        assertEquals(120.0, view.getFadingSize(), 1e-9);
    }

    @Test
    public void contentConstructorSetsContent() {
        Label label = new Label("Hello");
        MaskedView view = invoke(() -> new MaskedView(label));
        assertSame(label, view.getContent());
    }

    @Test
    public void setAndGetContent() {
        MaskedView view = invoke(MaskedView::new);
        Label label = new Label("Test");
        runFx(() -> view.setContent(label));
        assertSame(label, view.getContent());
    }

    @Test
    public void setAndGetFadingSize() {
        MaskedView view = invoke(MaskedView::new);
        runFx(() -> view.setFadingSize(60));
        assertEquals(60.0, view.getFadingSize(), 1e-9);
    }

    @Test
    public void fadingSizePropertyListener() {
        MaskedView view = invoke(MaskedView::new);
        boolean[] fired = {false};
        runFx(() -> view.fadingSizeProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> view.setFadingSize(30));
        assertTrue(fired[0]);
    }

    @Test
    public void skinCreatedAfterLayout() {
        MaskedView view = invoke(() -> new MaskedView(new Label("content")));
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void contentPropertyListener() {
        MaskedView view = invoke(MaskedView::new);
        boolean[] fired = {false};
        runFx(() -> view.contentProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> view.setContent(new Label("New")));
        assertTrue(fired[0]);
    }
}
