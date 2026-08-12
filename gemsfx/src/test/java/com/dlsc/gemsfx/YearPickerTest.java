package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.Year;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link YearPicker}: default value, setter/getter round trips,
 * read-only year property, editor access, style class, skin, and stylesheet.
 */
public class YearPickerTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        YearPicker picker = invoke(YearPicker::new);
        assertTrue(picker.getStyleClass().contains("year-picker"));
        assertTrue(picker.getStyleClass().contains("text-input"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        YearPicker picker = invoke(YearPicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultValueNotNull() {
        YearPicker picker = invoke(YearPicker::new);
        // constructor calls setValue(Year.now())
        assertNotNull(picker.getValue());
    }

    @Test
    public void testSetAndGetValue() {
        YearPicker picker = invoke(YearPicker::new);
        runFx(() -> picker.setValue(Year.of(2024)));
        assertEquals(Year.of(2024), picker.getValue());
    }

    @Test
    public void testReadOnlyYearProperty() {
        YearPicker picker = invoke(YearPicker::new);
        runFx(() -> picker.setValue(Year.of(2024)));
        assertEquals(Integer.valueOf(2024), picker.getYear());
        assertNotNull(picker.yearProperty());
    }

    @Test
    public void testEditorNotNull() {
        YearPicker picker = invoke(YearPicker::new);
        assertNotNull(picker.getEditor());
    }

    @Test
    public void testGetYearViewNotNull() {
        YearPicker picker = invoke(YearPicker::new);
        assertNotNull(picker.getYearView());
    }

    @Test
    public void testSetValueUpdatesYear() {
        YearPicker picker = invoke(YearPicker::new);
        runFx(() -> picker.setValue(Year.of(2000)));
        assertEquals(Integer.valueOf(2000), picker.getYear());
    }

    @Test
    public void testSkinCreation() {
        YearPicker picker = invoke(YearPicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
