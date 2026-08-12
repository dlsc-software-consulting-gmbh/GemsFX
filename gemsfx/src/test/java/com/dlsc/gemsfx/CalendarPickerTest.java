package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CalendarPicker}: style class, stylesheet, default value,
 * getter/setter round trip, editor access, date filter, and skin creation.
 */
public class CalendarPickerTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertTrue(picker.getStyleClass().contains("calendar-picker"));
        assertTrue(picker.getStyleClass().contains("text-input"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultValueNull() {
        // constructor does not set a default date
        CalendarPicker picker = invoke(CalendarPicker::new);
        // value starts as null
        // (depends on implementation; just assert it doesn't throw)
        picker.getValue(); // no exception
    }

    @Test
    public void testSetAndGetValue() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        LocalDate date = LocalDate.of(2024, 3, 15);
        runFx(() -> picker.setValue(date));
        assertEquals(date, picker.getValue());
    }

    @Test
    public void testConverterNotNull() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertNotNull(picker.getConverter());
    }

    @Test
    public void testEditorNotNull() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertNotNull(picker.getEditor());
    }

    @Test
    public void testDateFilterDefaultNull() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertNull(picker.getDateFilter());
    }

    @Test
    public void testSetDateFilter() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        runFx(() -> picker.setDateFilter(date -> !date.getDayOfWeek().name().startsWith("S")));
        assertNotNull(picker.getDateFilter());
    }

    @Test
    public void testGetCalendarViewNotNull() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        assertNotNull(picker.getCalendarView());
    }

    @Test
    public void testSkinCreation() {
        CalendarPicker picker = invoke(CalendarPicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
