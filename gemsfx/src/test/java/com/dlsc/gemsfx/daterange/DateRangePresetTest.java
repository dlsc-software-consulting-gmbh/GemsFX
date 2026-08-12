package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DateRangePreset}: title, supplier invocation.
 */
public class DateRangePresetTest extends FxTestBase {

    @Test
    public void testGetTitle() {
        DateRangePreset preset = new DateRangePreset("Last Week",
                () -> new DateRange(LocalDate.of(2024, 3, 4), LocalDate.of(2024, 3, 10)));
        assertEquals("Last Week", preset.getTitle());
    }

    @Test
    public void testGetDateRangeSupplierNotNull() {
        DateRangePreset preset = new DateRangePreset("Today",
                () -> new DateRange(LocalDate.of(2024, 3, 15)));
        assertNotNull(preset.getDateRangeSupplier());
    }

    @Test
    public void testSupplierReturnsExpectedRange() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        DateRangePreset preset = new DateRangePreset("March 2024",
                () -> new DateRange(start, end));
        DateRange range = preset.getDateRangeSupplier().get();
        assertNotNull(range);
        assertEquals(start, range.getStartDate());
        assertEquals(end, range.getEndDate());
    }

    @Test(expected = NullPointerException.class)
    public void testNullTitleThrows() {
        new DateRangePreset(null, () -> new DateRange(LocalDate.of(2024, 3, 1)));
    }

    @Test(expected = NullPointerException.class)
    public void testNullSupplierThrows() {
        new DateRangePreset("Title", null);
    }
}
