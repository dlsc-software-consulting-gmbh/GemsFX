package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.YearMonth;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link YearMonthView}: default value, setter/getter, earliest/latest month,
 * showYear, converter, style class, skin creation, and stylesheet.
 */
public class YearMonthViewTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        YearMonthView view = invoke(YearMonthView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultValueNotNull() {
        YearMonthView view = invoke(YearMonthView::new);
        assertNotNull(view.getValue());
    }

    @Test
    public void testSetAndGetValue() {
        YearMonthView view = invoke(YearMonthView::new);
        YearMonth ym = YearMonth.of(2024, 3);
        runFx(() -> view.setValue(ym));
        assertEquals(ym, view.getValue());
    }

    @Test
    public void testShowYearDefault() {
        YearMonthView view = invoke(YearMonthView::new);
        assertTrue(view.isShowYear());
    }

    @Test
    public void testSetShowYear() {
        YearMonthView view = invoke(YearMonthView::new);
        runFx(() -> view.setShowYear(false));
        assertFalse(view.isShowYear());
    }

    @Test
    public void testEarliestMonth() {
        YearMonthView view = invoke(YearMonthView::new);
        YearMonth earliest = YearMonth.of(2020, 1);
        runFx(() -> view.setEarliestMonth(earliest));
        assertEquals(earliest, view.getEarliestMonth());
    }

    @Test
    public void testLatestMonth() {
        YearMonthView view = invoke(YearMonthView::new);
        YearMonth latest = YearMonth.of(2030, 12);
        runFx(() -> view.setLatestMonth(latest));
        assertEquals(latest, view.getLatestMonth());
    }

    @Test
    public void testConverterNotNull() {
        YearMonthView view = invoke(YearMonthView::new);
        assertNotNull(view.getConverter());
    }

    @Test
    public void testSkinCreation() {
        YearMonthView view = invoke(YearMonthView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }
}
