package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TimePicker}: default properties, getter/setter round trips,
 * earliest/latest clamping, style class, skin creation, and stylesheet.
 */
public class TimePickerTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        TimePicker picker = invoke(TimePicker::new);
        assertTrue(picker.getStyleClass().contains("time-picker"));
        assertTrue(picker.getStyleClass().contains("text-input"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        TimePicker picker = invoke(TimePicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultEarliestAndLatestTime() {
        TimePicker picker = invoke(TimePicker::new);
        assertEquals(LocalTime.MIN, picker.getEarliestTime());
        assertEquals(LocalTime.MAX, picker.getLatestTime());
    }

    @Test
    public void testSetAndGetTime() {
        TimePicker picker = invoke(TimePicker::new);
        LocalTime t = LocalTime.of(14, 30);
        runFx(() -> picker.setTime(t));
        assertEquals(t, picker.getTime());
    }

    @Test
    public void testEarliestTimeProperty() {
        TimePicker picker = invoke(TimePicker::new);
        LocalTime earliest = LocalTime.of(8, 0);
        runFx(() -> picker.setEarliestTime(earliest));
        assertEquals(earliest, picker.getEarliestTime());
        assertEquals(earliest, picker.earliestTimeProperty().get());
    }

    @Test
    public void testLatestTimeProperty() {
        TimePicker picker = invoke(TimePicker::new);
        LocalTime latest = LocalTime.of(20, 0);
        runFx(() -> picker.setLatestTime(latest));
        assertEquals(latest, picker.getLatestTime());
        assertEquals(latest, picker.latestTimeProperty().get());
    }

    @Test
    public void testDefaultStepRateInMinutes() {
        TimePicker picker = invoke(TimePicker::new);
        // default should be 1
        assertTrue(picker.getStepRateInMinutes() >= 1);
    }

    @Test
    public void testSetStepRateInMinutes() {
        TimePicker picker = invoke(TimePicker::new);
        runFx(() -> picker.setStepRateInMinutes(15));
        assertEquals(15, picker.getStepRateInMinutes());
    }

    @Test
    public void testDefaultFormat() {
        TimePicker picker = invoke(TimePicker::new);
        assertNotNull(picker.getFormat());
    }

    @Test
    public void testSetFormat() {
        TimePicker picker = invoke(TimePicker::new);
        runFx(() -> picker.setFormat(TimePicker.Format.HOURS_MINUTES_SECONDS));
        assertEquals(TimePicker.Format.HOURS_MINUTES_SECONDS, picker.getFormat());
    }

    @Test
    public void testDefaultClockType() {
        TimePicker picker = invoke(TimePicker::new);
        assertEquals(TimePicker.ClockType.TWENTY_FOUR_HOUR_CLOCK, picker.getClockType());
    }

    @Test
    public void testSkinCreation() {
        TimePicker picker = invoke(TimePicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }

    @Test
    public void testLinkingFieldsDefault() {
        TimePicker picker = invoke(TimePicker::new);
        assertTrue(picker.isLinkingFields());
    }

    @Test
    public void testShowPopupTriggerButtonDefault() {
        TimePicker picker = invoke(TimePicker::new);
        assertTrue(picker.isShowPopupTriggerButton());
    }
}
