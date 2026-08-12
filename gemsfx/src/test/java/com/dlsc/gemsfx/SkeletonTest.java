package com.dlsc.gemsfx;

import com.dlsc.gemsfx.Skeleton.Variant;
import javafx.beans.property.Property;
import javafx.css.StyleOrigin;
import javafx.css.StyleableProperty;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
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

    @Test
    public void testGetUserAgentStylesheet() {
        Skeleton skeleton = invoke(Skeleton::new);
        assertNotNull(skeleton.getUserAgentStylesheet());
    }

    @Test
    public void userAgentStylesheetKeepsDefaults() {
        Skeleton skeleton = layout(invoke(Skeleton::new));
        assertEquals(Variant.ROUNDED_RECTANGLE, skeleton.getVariant());
        assertEquals(4.0, skeleton.getCornerRadius(), 1e-9);
        assertEquals(Duration.millis(1500), skeleton.getCycleDuration());
        assertEquals(56.0, skeleton.getShimmerWidth(), 1e-9);
        assertEquals(1, skeleton.getLineCount());
        assertEquals(14.0, skeleton.getLineHeight(), 1e-9);
        assertEquals(8.0, skeleton.getLineSpacing(), 1e-9);
        assertEquals(70.0, skeleton.getLastLineFillPercent(), 1e-9);
    }

    @Test
    public void baseColorFollowsModenaBackground() {
        Skeleton skeleton = layout(invoke(Skeleton::new));

        assertTrue(skeleton.getBaseColor() instanceof Color);
        Color baseColor = (Color) skeleton.getBaseColor();
        // derive(-fx-background, -8%) resolves to #e1e1e1 with the default Modena theme
        Color expected = Color.web("#e1e1e1");

        assertEquals(expected.getRed(), baseColor.getRed(), 0.02);
        assertEquals(expected.getGreen(), baseColor.getGreen(), 0.02);
        assertEquals(expected.getBlue(), baseColor.getBlue(), 0.02);
        assertEquals(1.0, baseColor.getOpacity(), 1e-9);
    }

    @Test
    public void userAgentStylesheetIsApplied() {
        Skeleton skeleton = layout(invoke(Skeleton::new));

        assertStyledByUserAgent(skeleton.variantProperty());
        assertStyledByUserAgent(skeleton.cornerRadiusProperty());
        assertStyledByUserAgent(skeleton.baseColorProperty());
        assertStyledByUserAgent(skeleton.shimmerFillProperty());
        assertStyledByUserAgent(skeleton.cycleDurationProperty());
        assertStyledByUserAgent(skeleton.shimmerWidthProperty());
        assertStyledByUserAgent(skeleton.lineCountProperty());
        assertStyledByUserAgent(skeleton.lineHeightProperty());
        assertStyledByUserAgent(skeleton.lineSpacingProperty());
        assertStyledByUserAgent(skeleton.lastLineFillPercentProperty());
    }

    private static void assertStyledByUserAgent(Object property) {
        StyleableProperty<?> styleable = (StyleableProperty<?>) property;
        assertEquals("expected " + ((Property<?>) property).getName() + " to be set by the user agent stylesheet",
                StyleOrigin.USER_AGENT, styleable.getStyleOrigin());
    }

    @Test
    public void userAgentStylesheetProvidesShimmerGradient() {
        Skeleton skeleton = layout(invoke(Skeleton::new));

        assertTrue(skeleton.getShimmerFill() instanceof LinearGradient);
        LinearGradient gradient = (LinearGradient) skeleton.getShimmerFill();
        LinearGradient expected = Skeleton.createShimmerGradient(Color.web("#ffffff", 0.6));

        assertTrue(gradient.isProportional());
        assertEquals(expected.getCycleMethod(), gradient.getCycleMethod());
        assertEquals(expected.getStops().size(), gradient.getStops().size());

        for (int i = 0; i < expected.getStops().size(); i++) {
            Stop expectedStop = expected.getStops().get(i);
            Stop actualStop = gradient.getStops().get(i);
            assertEquals(expectedStop.getOffset(), actualStop.getOffset(), 1e-9);
            assertEquals(expectedStop.getColor().getRed(), actualStop.getColor().getRed(), 1e-6);
            assertEquals(expectedStop.getColor().getGreen(), actualStop.getColor().getGreen(), 1e-6);
            assertEquals(expectedStop.getColor().getBlue(), actualStop.getColor().getBlue(), 1e-6);
            assertEquals(expectedStop.getColor().getOpacity(), actualStop.getColor().getOpacity(), 1e-6);
        }
    }

    @Test
    public void constructorVariantSurvivesUserAgentStylesheet() {
        Skeleton skeleton = layout(invoke(() -> new Skeleton(Variant.TEXT)));
        assertEquals(Variant.TEXT, skeleton.getVariant());
    }
}
