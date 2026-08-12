package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link CalendarView}: default properties, yearMonth navigation,
 * showToday, showWeekNumbers, style class, skin creation, and stylesheet.
 */
public class CalendarViewTest extends FxTestBase {

    @Test
    public void testStyleClass() {
        CalendarView view = invoke(CalendarView::new);
        assertTrue(view.getStyleClass().contains("calendar-view"));
    }

    @Test
    public void testUserAgentStylesheetNotNull() {
        CalendarView view = invoke(CalendarView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultShowToday() {
        CalendarView view = invoke(CalendarView::new);
        assertTrue(view.isShowToday());
    }

    @Test
    public void testDefaultShowWeekNumbers() {
        CalendarView view = invoke(CalendarView::new);
        assertFalse(view.isShowWeekNumbers());
    }

    @Test
    public void testSetYearMonth() {
        CalendarView view = invoke(CalendarView::new);
        YearMonth ym = YearMonth.of(2024, 3);
        runFx(() -> view.setYearMonth(ym));
        assertEquals(ym, view.getYearMonth());
    }

    @Test
    public void testSetToday() {
        CalendarView view = invoke(CalendarView::new);
        LocalDate date = LocalDate.of(2024, 3, 15);
        runFx(() -> view.setToday(date));
        assertEquals(date, view.getToday());
    }

    @Test
    public void testSetShowToday() {
        CalendarView view = invoke(CalendarView::new);
        runFx(() -> view.setShowToday(false));
        assertFalse(view.isShowToday());
    }

    @Test
    public void testSetShowWeekNumbers() {
        CalendarView view = invoke(CalendarView::new);
        runFx(() -> view.setShowWeekNumbers(true));
        assertTrue(view.isShowWeekNumbers());
    }

    @Test
    public void testGetYearMonthViewNotNull() {
        CalendarView view = invoke(CalendarView::new);
        assertNotNull(view.getYearMonthView());
    }

    @Test
    public void testGetYearViewNotNull() {
        CalendarView view = invoke(CalendarView::new);
        assertNotNull(view.getYearView());
    }

    @Test
    public void testSkinCreation() {
        CalendarView view = invoke(CalendarView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }

    @Test
    public void testDisablePreviousMonthButtonDefault() {
        CalendarView view = invoke(CalendarView::new);
        assertFalse(view.isDisablePreviousMonthButton());
    }

    @Test
    public void testDisableNextMonthButtonDefault() {
        CalendarView view = invoke(CalendarView::new);
        assertFalse(view.isDisableNextMonthButton());
    }
}
