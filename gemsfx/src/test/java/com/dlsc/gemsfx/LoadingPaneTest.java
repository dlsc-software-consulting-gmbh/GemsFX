package com.dlsc.gemsfx;

import com.dlsc.gemsfx.LoadingPane.Size;
import com.dlsc.gemsfx.LoadingPane.Status;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link LoadingPane}.
 */
public class LoadingPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertTrue(pane.getStyleClass().contains("loading-pane"));
    }

    @Test
    public void getUserAgentStylesheetNotNull() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertNotNull(pane.getUserAgentStylesheet());
    }

    @Test
    public void defaultStatus() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertEquals(Status.OK, pane.getStatus());
    }

    @Test
    public void defaultSize() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertEquals(Size.MEDIUM, pane.getSize());
    }

    @Test
    public void defaultProgress() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertEquals(0.0, pane.getProgress(), 1e-9);
    }

    @Test
    public void defaultCommitDelay() {
        LoadingPane pane = invoke(LoadingPane::new);
        assertEquals(200L, pane.getCommitDelay());
    }

    @Test
    public void setStatus() {
        LoadingPane pane = invoke(LoadingPane::new);
        runFx(() -> pane.setStatus(Status.LOADING));
        assertEquals(Status.LOADING, pane.getStatus());
    }

    @Test
    public void setSize() {
        LoadingPane pane = invoke(LoadingPane::new);
        runFx(() -> pane.setSize(Size.LARGE));
        assertEquals(Size.LARGE, pane.getSize());
    }

    @Test
    public void setProgress() {
        LoadingPane pane = invoke(LoadingPane::new);
        runFx(() -> pane.setProgress(0.5));
        assertEquals(0.5, pane.getProgress(), 1e-9);
    }

    @Test
    public void statusPropertyListener() {
        LoadingPane pane = invoke(LoadingPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.statusProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setStatus(Status.ERROR));
        assertTrue(fired[0]);
    }

    @Test
    public void setCommitDelay() {
        LoadingPane pane = invoke(LoadingPane::new);
        runFx(() -> pane.setCommitDelay(0L));
        assertEquals(0L, pane.getCommitDelay());
    }
}
