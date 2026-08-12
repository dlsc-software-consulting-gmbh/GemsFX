package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.LocalTime;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link TimeRangePicker} and its inner {@link TimeRangePicker.TimeRange}:
 * style class, stylesheet, construction with ranges, TimeRange API, and skin creation.
 */
public class TimeRangePickerTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        TimeRangePicker picker = invoke(TimeRangePicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testStyleClass() {
        TimeRangePicker picker = invoke(TimeRangePicker::new);
        assertTrue(picker.getStyleClass().contains("selection-box"));
    }

    @Test
    public void testDefaultItemsNotEmpty() {
        TimeRangePicker picker = invoke(TimeRangePicker::new);
        assertFalse(picker.getItems().isEmpty());
    }

    @Test
    public void testConstructionWithCustomRanges() {
        TimeRangePicker.TimeRange r1 = new TimeRangePicker.TimeRange(LocalTime.of(8, 0), LocalTime.of(12, 0));
        TimeRangePicker.TimeRange r2 = new TimeRangePicker.TimeRange(LocalTime.of(13, 0), LocalTime.of(17, 0));
        TimeRangePicker picker = invoke(() -> new TimeRangePicker(r1, r2));
        assertEquals(2, picker.getItems().size());
    }

    @Test
    public void testTimeRangeStartTime() {
        TimeRangePicker.TimeRange range = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertEquals(LocalTime.of(9, 0), range.startTime());
    }

    @Test
    public void testTimeRangeEndTime() {
        TimeRangePicker.TimeRange range = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertEquals(LocalTime.of(17, 0), range.endTime());
    }

    @Test
    public void testTimeRangeEquals() {
        TimeRangePicker.TimeRange r1 = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        TimeRangePicker.TimeRange r2 = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertEquals(r1, r2);
    }

    @Test
    public void testTimeRangeHashCode() {
        TimeRangePicker.TimeRange r1 = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        TimeRangePicker.TimeRange r2 = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test
    public void testTimeRangeNotEqualDifferentTimes() {
        TimeRangePicker.TimeRange r1 = new TimeRangePicker.TimeRange(LocalTime.of(8, 0), LocalTime.of(17, 0));
        TimeRangePicker.TimeRange r2 = new TimeRangePicker.TimeRange(LocalTime.of(9, 0), LocalTime.of(17, 0));
        assertNotEquals(r1, r2);
    }

    @Test
    public void testSelectionModelNotNull() {
        TimeRangePicker picker = invoke(TimeRangePicker::new);
        assertNotNull(picker.getSelectionModel());
    }

    @Test
    public void testSkinCreation() {
        TimeRangePicker picker = invoke(TimeRangePicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
