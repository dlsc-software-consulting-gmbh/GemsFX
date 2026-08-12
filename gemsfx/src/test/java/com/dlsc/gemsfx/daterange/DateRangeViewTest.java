package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.FxTestBase;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DateRangeView}: style class, stylesheet, value, orientation,
 * presets, showPresets, calendar views, and skin creation.
 */
public class DateRangeViewTest extends FxTestBase {

    @Test
    public void testUserAgentStylesheetNotNull() {
        DateRangeView view = invoke(DateRangeView::new);
        assertNotNull(view.getUserAgentStylesheet());
    }

    @Test
    public void testDefaultOrientationHorizontal() {
        DateRangeView view = invoke(DateRangeView::new);
        assertEquals(Orientation.HORIZONTAL, view.getOrientation());
    }

    @Test
    public void testSetOrientation() {
        DateRangeView view = invoke(DateRangeView::new);
        runFx(() -> view.setOrientation(Orientation.VERTICAL));
        assertEquals(Orientation.VERTICAL, view.getOrientation());
    }

    @Test
    public void testDefaultShowPresets() {
        DateRangeView view = invoke(DateRangeView::new);
        assertTrue(view.isShowPresets());
    }

    @Test
    public void testSetShowPresets() {
        DateRangeView view = invoke(DateRangeView::new);
        runFx(() -> view.setShowPresets(false));
        assertFalse(view.isShowPresets());
    }

    @Test
    public void testDefaultPresetsLocation() {
        DateRangeView view = invoke(DateRangeView::new);
        assertEquals(Side.LEFT, view.getPresetsLocation());
    }

    @Test
    public void testSetValue() {
        DateRangeView view = invoke(DateRangeView::new);
        DateRange range = new DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 15));
        runFx(() -> view.setValue(range));
        assertEquals(range, view.getValue());
    }

    @Test
    public void testGetPresetsNotNull() {
        DateRangeView view = invoke(DateRangeView::new);
        assertNotNull(view.getPresets());
    }

    @Test
    public void testAddPreset() {
        DateRangeView view = invoke(DateRangeView::new);
        DateRangePreset preset = new DateRangePreset("Q1 2024",
                () -> new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 3, 31)));
        runFx(() -> view.getPresets().add(preset));
        assertTrue(view.getPresets().contains(preset));
    }

    @Test
    public void testGetStartCalendarViewNotNull() {
        DateRangeView view = invoke(DateRangeView::new);
        assertNotNull(view.getStartCalendarView());
    }

    @Test
    public void testGetEndCalendarViewNotNull() {
        DateRangeView view = invoke(DateRangeView::new);
        assertNotNull(view.getEndCalendarView());
    }

    @Test
    public void testSelectionModelNotNull() {
        DateRangeView view = invoke(DateRangeView::new);
        assertNotNull(view.getSelectionModel());
    }

    @Test
    public void testSkinCreation() {
        DateRangeView view = invoke(DateRangeView::new);
        layout(view);
        assertNotNull(view.getSkin());
    }
}
