package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.time.LocalDate;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link DateRange}: construction, getters, equals, hashCode, and single-day range.
 */
public class DateRangeTest extends FxTestBase {

    @Test
    public void testConstructorStartEnd() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 15);
        DateRange range = new DateRange(start, end);
        assertEquals(start, range.getStartDate());
        assertEquals(end, range.getEndDate());
    }

    @Test
    public void testConstructorWithTitle() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 31);
        DateRange range = new DateRange("March 2024", start, end);
        assertEquals("March 2024", range.getTitle());
        assertEquals(start, range.getStartDate());
        assertEquals(end, range.getEndDate());
    }

    @Test
    public void testSingleDayConstructor() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        DateRange range = new DateRange(date);
        assertEquals(date, range.getStartDate());
        assertEquals(date, range.getEndDate());
    }

    @Test
    public void testSingleDayWithTitle() {
        LocalDate date = LocalDate.of(2024, 3, 15);
        DateRange range = new DateRange("My Day", date);
        assertEquals("My Day", range.getTitle());
        assertEquals(date, range.getStartDate());
        assertEquals(date, range.getEndDate());
    }

    @Test
    public void testNullTitleAllowed() {
        DateRange range = new DateRange(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31));
        assertNull(range.getTitle());
    }

    @Test
    public void testEqualsSymmetric() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 15);
        DateRange r1 = new DateRange(start, end);
        DateRange r2 = new DateRange("Different Title", start, end);
        // equals only compares start/end, not title
        assertEquals(r1, r2);
        assertEquals(r2, r1);
    }

    @Test
    public void testNotEqualDifferentStart() {
        DateRange r1 = new DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 15));
        DateRange r2 = new DateRange(LocalDate.of(2024, 3, 2), LocalDate.of(2024, 3, 15));
        assertNotEquals(r1, r2);
    }

    @Test
    public void testNotEqualDifferentEnd() {
        DateRange r1 = new DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 15));
        DateRange r2 = new DateRange(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 3, 16));
        assertNotEquals(r1, r2);
    }

    @Test
    public void testHashCodeConsistent() {
        LocalDate start = LocalDate.of(2024, 3, 1);
        LocalDate end = LocalDate.of(2024, 3, 15);
        DateRange r1 = new DateRange(start, end);
        DateRange r2 = new DateRange("Title", start, end);
        assertEquals(r1.hashCode(), r2.hashCode());
    }

    @Test(expected = NullPointerException.class)
    public void testNullStartDateThrows() {
        new DateRange((LocalDate) null, LocalDate.of(2024, 3, 15));
    }

    @Test(expected = NullPointerException.class)
    public void testNullEndDateThrows() {
        new DateRange(LocalDate.of(2024, 3, 1), null);
    }
}
