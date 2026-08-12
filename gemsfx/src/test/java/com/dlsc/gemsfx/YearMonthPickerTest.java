package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.YearMonth;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link YearMonthPicker}: default value, setter/getter, converter,
 * style class, skin creation, and stylesheet.
 */
public class YearMonthPickerTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertTrue(picker.getStyleClass().contains("year-month-picker"));
        assertTrue(picker.getStyleClass().contains("text-input"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultValueNotNull() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertNotNull(picker.getValue());
    }

    @Test
    public void testSetAndGetValue() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        YearMonth ym = YearMonth.of(2024, 3);
        runFx(() -> picker.setValue(ym));
        assertEquals(ym, picker.getValue());
    }

    @Test
    public void testConverterNotNull() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertNotNull(picker.getConverter());
    }

    @Test
    public void testConverterRoundTrip() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        YearMonth ym = YearMonth.of(2024, 3);
        // The default converter uses MMMM yyyy pattern
        // We cannot rely on locale, so just check fromString(toString(v)) == v
        String text = invoke(() -> picker.getConverter().toString(ym));
        YearMonth parsed = invoke(() -> picker.getConverter().fromString(text));
        assertEquals(ym, parsed);
    }

    @Test
    public void testEditorNotNull() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertNotNull(picker.getEditor());
    }

    @Test
    public void testGetYearMonthViewNotNull() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        assertNotNull(picker.getYearMonthView());
    }

    @Test
    public void testSkinCreation() {
        YearMonthPicker picker = invoke(YearMonthPicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
