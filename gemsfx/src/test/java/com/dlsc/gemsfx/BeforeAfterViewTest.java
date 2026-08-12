package com.dlsc.gemsfx;

import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link BeforeAfterView}.
 */
public class BeforeAfterViewTest extends FxTestBase {

    @Test
    public void defaultDividerPosition() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        assertEquals(0.5, view.getDividerPosition(), 1e-9);
    }

    @Test
    public void defaultOrientation() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        assertEquals(Orientation.HORIZONTAL, view.getOrientation());
    }

    @Test
    public void styleClass() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        assertTrue(view.getStyleClass().contains("before-after-view"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void setAndGetDividerPosition() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        runFx(() -> view.setDividerPosition(0.25));
        assertEquals(0.25, view.getDividerPosition(), 1e-9);
    }

    @Test
    public void setAndGetOrientation() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        runFx(() -> view.setOrientation(Orientation.VERTICAL));
        assertEquals(Orientation.VERTICAL, view.getOrientation());
    }

    @Test
    public void beforePropertyRoundTrip() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        Label node = new Label("before");
        runFx(() -> view.setBefore(node));
        assertSame(node, view.getBefore());
    }

    @Test
    public void afterPropertyRoundTrip() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        Label node = new Label("after");
        runFx(() -> view.setAfter(node));
        assertSame(node, view.getAfter());
    }

    @Test
    public void beforeNodeIsMouseTransparent() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        Label node = new Label("before");
        runFx(() -> view.setBefore(node));
        assertTrue(node.isMouseTransparent());
    }

    @Test
    public void skinCreatedAfterLayout() {
        BeforeAfterView view = invoke(() -> new BeforeAfterView(new Label("B"), new Label("A")));
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void dividerPositionPropertyListener() {
        BeforeAfterView view = invoke(BeforeAfterView::new);
        boolean[] fired = {false};
        runFx(() -> view.dividerPositionProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> view.setDividerPosition(0.75));
        assertTrue(fired[0]);
    }
}
