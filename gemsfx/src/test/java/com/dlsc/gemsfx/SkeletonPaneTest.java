package com.dlsc.gemsfx;

import javafx.scene.control.Label;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link SkeletonPane}.
 */
public class SkeletonPaneTest extends FxTestBase {

    @Test
    public void styleClass() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        assertTrue(pane.getStyleClass().contains("skeleton-pane"));
    }

    @Test
    public void defaultLoadingTrue() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        assertTrue(pane.isLoading());
    }

    @Test
    public void defaultContentNull() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        assertNull(pane.getContent());
    }

    @Test
    public void defaultSkeletonNull() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        assertNull(pane.getSkeleton());
    }

    @Test
    public void skeletonContentConstructor() {
        Label skeleton = new Label("Loading...");
        Label content = new Label("Done!");
        SkeletonPane pane = invoke(() -> new SkeletonPane(skeleton, content));
        assertSame(skeleton, pane.getSkeleton());
        assertSame(content, pane.getContent());
        assertTrue(pane.isLoading());
    }

    @Test
    public void fullConstructorSetsLoading() {
        Label skeleton = new Label("Loading...");
        Label content = new Label("Done!");
        SkeletonPane pane = invoke(() -> new SkeletonPane(skeleton, content, false));
        assertFalse(pane.isLoading());
    }

    @Test
    public void setAndGetLoading() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        runFx(() -> pane.setLoading(false));
        assertFalse(pane.isLoading());
    }

    @Test
    public void setAndGetContent() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        Label content = new Label("Content");
        runFx(() -> pane.setContent(content));
        assertSame(content, pane.getContent());
    }

    @Test
    public void setAndGetSkeleton() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        Label skeleton = new Label("Skeleton");
        runFx(() -> pane.setSkeleton(skeleton));
        assertSame(skeleton, pane.getSkeleton());
    }

    @Test
    public void loadingPropertyListener() {
        SkeletonPane pane = invoke(SkeletonPane::new);
        boolean[] fired = {false};
        runFx(() -> pane.loadingProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> pane.setLoading(false));
        assertTrue(fired[0]);
    }
}
