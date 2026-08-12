package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DateRangePicker}: style class, stylesheet, value getter/setter,
 * showIcon, showPresetTitle, small, formatter, and skin creation.
 */
public class DateRangePickerTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultShowIcon() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertTrue(picker.isShowIcon());
    }

    @Test
    public void testSetShowIcon() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        runFx(() -> picker.setShowIcon(false));
        assertFalse(picker.isShowIcon());
    }

    @Test
    public void testDefaultShowPresetTitle() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertTrue(picker.isShowPresetTitle());
    }

    @Test
    public void testSetShowPresetTitle() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        runFx(() -> picker.setShowPresetTitle(false));
        assertFalse(picker.isShowPresetTitle());
    }

    @Test
    public void testDefaultSmall() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertTrue(picker.isSmall());
    }

    @Test
    public void testSetSmall() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        runFx(() -> picker.setSmall(false));
        assertFalse(picker.isSmall());
    }

    @Test
    public void testFormatterNotNull() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertNotNull(picker.getFormatter());
    }

    @Test
    public void testSetAndGetValue() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        DateRange range = new DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 31));
        runFx(() -> picker.setValue(range));
        assertEquals(range, picker.getValue());
    }

    @Test
    public void testGetDateRangeViewNotNull() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        assertNotNull(picker.getDateRangeView());
    }

    @Test
    public void testSkinCreation() {
        DateRangePicker picker = invoke(DateRangePicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
