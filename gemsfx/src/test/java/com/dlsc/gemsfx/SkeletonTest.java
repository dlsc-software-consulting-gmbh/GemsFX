package com.dlsc.gemsfx;

import com.dlsc.gemsfx.Skeleton.Variant;
import javafx.util.Duration;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Skeleton}.
 */
public class SkeletonTest extends FxTestBase {

    @Test
    public void styleClass() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertTrue(skeleton.getStyleClass().contains("skeleton"));
    }

    @Test
    public void defaultVariant() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(Skeleton.DEFAULT_VARIANT, skeleton.getVariant());
        assertEquals(Variant.ROUNDED_RECTANGLE, skeleton.getVariant());
    }

    @Test
    public void defaultCornerRadius() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(4.0, skeleton.getCornerRadius(), 1e-9);
    }

    @Test
    public void defaultCycleDuration() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(Duration.millis(1500), skeleton.getCycleDuration());
    }

    @Test
    public void defaultShimmerWidth() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(56.0, skeleton.getShimmerWidth(), 1e-9);
    }

    @Test
    public void defaultLineCount() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(1, skeleton.getLineCount());
    }

    @Test
    public void defaultLineHeight() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertEquals(14.0, skeleton.getLineHeight(), 1e-9);
    }

    @Test
    public void setAndGetVariant() {
        Skeleton skeleton = invoke(Skeleton::new);
        runFx(() -> skeleton.setVariant(Variant.CIRCULAR));
        assertEquals(Variant.CIRCULAR, skeleton.getVariant());
    }

    @Test
    public void setAndGetCornerRadius() {
        Skeleton skeleton = invoke(Skeleton::new);
        runFx(() -> skeleton.setCornerRadius(8.0));
        assertEquals(8.0, skeleton.getCornerRadius(), 1e-9);
    }

    @Test
    public void variantPropertyListener() {
        Skeleton skeleton = invoke(Skeleton::new);
        boolean[] fired = {false};
        runFx(() -> skeleton.variantProperty().addListener((obs, o, n) -> fired[0] = true));
        runFx(() -> skeleton.setVariant(Variant.TEXT));
        assertTrue(fired[0]);
    }

    @Test
    public void skinCreatedAfterLayout() {
        Skeleton skeleton = layout(invoke(Skeleton::new));
        assertNotNull(skeleton.getSkin());
    }
}
