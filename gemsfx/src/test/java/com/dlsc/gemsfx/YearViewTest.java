package com.dlsc.gemsfx;

import org.junit.Test;

import java.time.Year;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link YearView}: default value, getter/setter, earliest/latest year,
 * rows/cols properties, style class, skin creation, and stylesheet.
 */
public class YearViewTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        YearView view = invoke(YearView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultValueNotNull() {
        YearView view = invoke(YearView::new);
        // The view initializes to the current year
        assertNotNull(view.getValue());
    }

    @Test
    public void testSetAndGetValue() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setValue(Year.of(2024)));
        assertEquals(Year.of(2024), view.getValue());
    }

    @Test
    public void testGetYear() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setValue(Year.of(2024)));
        assertEquals(2024, view.getYear());
    }

    @Test
    public void testEarliestYear() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setEarliestYear(Year.of(2000)));
        assertEquals(Year.of(2000), view.getEarliestYear());
    }

    @Test
    public void testLatestYear() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setLatestYear(Year.of(2030)));
        assertEquals(Year.of(2030), view.getLatestYear());
    }

    @Test
    public void testColsDefault() {
        YearView view = invoke(YearView::new);
        assertTrue(view.getCols() > 0);
    }

    @Test
    public void testSetCols() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setCols(4));
        assertEquals(4, view.getCols());
    }

    @Test
    public void testRowsDefault() {
        YearView view = invoke(YearView::new);
        assertTrue(view.getRows() > 0);
    }

    @Test
    public void testSetRows() {
        YearView view = invoke(YearView::new);
        runFx(() -> view.setRows(3));
        assertEquals(3, view.getRows());
    }

    @Test
    public void testSkinCreation() {
        YearView view = invoke(YearView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }
}
