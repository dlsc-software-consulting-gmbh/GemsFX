package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DurationPicker}: default properties, getter/setter round trips,
 * min/max clamping, fields list, label type, style class, skin creation, and stylesheet.
 */
public class DurationPickerTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertTrue(picker.getStyleClass().contains("duration-picker"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultDuration() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertEquals(Duration.ZERO, picker.getDuration());
    }

    @Test
    public void testSetAndGetDuration() {
        DurationPicker picker = invoke(DurationPicker::new);
        Duration d = Duration.ofHours(2).plusMinutes(30);
        runFx(() -> picker.setDuration(d));
        assertEquals(d, picker.getDuration());
    }

    @Test
    public void testMinimumDurationDefault() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertEquals(Duration.ZERO, picker.getMinimumDuration());
    }

    @Test
    public void testMaximumDurationDefault() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertEquals(Duration.ofDays(7), picker.getMaximumDuration());
    }

    @Test
    public void testDurationClampedToMaximum() {
        DurationPicker picker = invoke(DurationPicker::new);
        runFx(() -> {
            picker.setMaximumDuration(Duration.ofHours(5));
            picker.setDuration(Duration.ofHours(10));
        });
        assertEquals(Duration.ofHours(5), picker.getDuration());
    }

    @Test
    public void testDurationClampedToMinimum() {
        DurationPicker picker = invoke(DurationPicker::new);
        runFx(() -> {
            picker.setMinimumDuration(Duration.ofHours(2));
            picker.setDuration(Duration.ofHours(1));
        });
        assertEquals(Duration.ofHours(2), picker.getDuration());
    }

    @Test
    public void testDefaultLabelType() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertEquals(DurationPicker.LabelType.SHORT, picker.getLabelType());
    }

    @Test
    public void testSetLabelType() {
        DurationPicker picker = invoke(DurationPicker::new);
        runFx(() -> picker.setLabelType(DurationPicker.LabelType.LONG));
        assertEquals(DurationPicker.LabelType.LONG, picker.getLabelType());
    }

    @Test
    public void testDefaultFields() {
        DurationPicker picker = invoke(DurationPicker::new);
        // constructor sets DAYS, HOURS, MINUTES, SECONDS, MILLIS
        assertFalse(picker.getFields().isEmpty());
        assertTrue(picker.getFields().contains(ChronoUnit.HOURS));
        assertTrue(picker.getFields().contains(ChronoUnit.MINUTES));
    }

    @Test
    public void testFillDigitsDefault() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertTrue(picker.isFillDigits());
    }

    @Test
    public void testLinkingFieldsDefault() {
        DurationPicker picker = invoke(DurationPicker::new);
        assertTrue(picker.isLinkingFields());
    }

    @Test
    public void testSkinCreation() {
        DurationPicker picker = invoke(DurationPicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
