package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.DayOfWeek;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DayOfWeekPicker}: style class, stylesheet, day order,
 * weekday/weekend queries, selection model behaviour, and skin creation.
 */
public class DayOfWeekPickerTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        assertNotNull(picker.getUserAgentStylesheet());
    }

    @Test
    public void testStyleClass() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        assertTrue(picker.getStyleClass().contains("selection-box"));
    }

    @Test
    public void testLocalizedDayOrderSize() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        List<DayOfWeek> order = invoke(picker::getLocalizedDayOrder);
        assertEquals(7, order.size());
    }

    @Test
    public void testGetWeekendDaysSize() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        List<DayOfWeek> weekends = invoke(picker::getWeekendDays);
        assertEquals(2, weekends.size());
    }

    @Test
    public void testGetWeekdaysSize() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        List<DayOfWeek> weekdays = invoke(picker::getWeekdays);
        assertEquals(5, weekdays.size());
    }

    @Test
    public void testIsOnlyWeekdaysSelectedFalseInitially() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        boolean result = invoke(picker::isOnlyWeekdaysSelected);
        assertFalse(result);
    }

    @Test
    public void testIsSelectedAllFalseInitially() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        boolean result = invoke(picker::isSelectedAll);
        assertFalse(result);
    }

    @Test
    public void testSelectionModelNotNull() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        assertNotNull(picker.getSelectionModel());
    }

    @Test
    public void testSelectDayOfWeek() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        runFx(() -> picker.getSelectionModel().select(DayOfWeek.MONDAY));
        assertEquals(DayOfWeek.MONDAY, picker.getSelectionModel().getSelectedItem());
    }

    @Test
    public void testItemsContainAllDays() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        assertEquals(7, picker.getItems().size());
    }

    @Test
    public void testSkinCreation() {
        DayOfWeekPicker picker = invoke(DayOfWeekPicker::new);
        layout(picker);
        assertNotNull(picker.getSkin());
    }
}
