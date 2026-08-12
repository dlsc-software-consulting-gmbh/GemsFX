package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link Notification} model class.
 */
public class NotificationTest extends FxTestBase {

    private static final ZonedDateTime FIXED = ZonedDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneId.of("UTC"));

    @Test
    public void testConstructorWithDateTime() {
        Notification<Void> n = new Notification<>("Title", "Summary", FIXED);
        assertEquals("Title", n.getTitle());
        assertEquals("Summary", n.getSummary());
        assertEquals(FIXED, n.getDateTime());
    }

    @Test
    public void testConstructorWithoutDateTime() {
        Notification<Void> n = new Notification<>("T", "S");
        assertEquals("T", n.getTitle());
        assertEquals("S", n.getSummary());
        assertNotNull(n.getDateTime());
    }

    @Test
    public void testSetTitle() {
        Notification<Void> n = new Notification<>("Old", "S");
        n.setTitle("New");
        assertEquals("New", n.getTitle());
    }

    @Test
    public void testSetSummary() {
        Notification<Void> n = new Notification<>("T", "Old");
        n.setSummary("New");
        assertEquals("New", n.getSummary());
    }

    @Test
    public void testSetDateTime() {
        Notification<Void> n = new Notification<>("T", "S");
        n.setDateTime(FIXED);
        assertEquals(FIXED, n.getDateTime());
    }

    @Test
    public void testDefaultTypeIsInfo() {
        Notification<Void> n = new Notification<>("T", "S");
        assertEquals(Notification.Type.INFO, n.getType());
    }

    @Test
    public void testSetType() {
        Notification<Void> n = new Notification<>("T", "S");
        n.setType(Notification.Type.WARNING);
        assertEquals(Notification.Type.WARNING, n.getType());
    }

    @Test
    public void testDefaultNotExpanded() {
        Notification<Void> n = new Notification<>("T", "S");
        assertFalse(n.isExpanded());
    }

    @Test
    public void testSetExpanded() {
        Notification<Void> n = new Notification<>("T", "S");
        n.setExpanded(true);
        assertTrue(n.isExpanded());
    }

    @Test
    public void testActionsInitiallyEmpty() {
        Notification<Void> n = new Notification<>("T", "S");
        assertTrue(n.getActions().isEmpty());
    }

    @Test
    public void testAddAction() {
        Notification<Void> n = new Notification<>("T", "S");
        n.getActions().add(new NotificationAction<>("OK"));
        assertEquals(1, n.getActions().size());
    }

    @Test
    public void testUserObject() {
        Notification<String> n = new Notification<>("T", "S");
        n.setUserObject("payload");
        assertEquals("payload", n.getUserObject());
    }

    @Test
    public void testGroupIsNullInitially() {
        Notification<Void> n = new Notification<>("T", "S");
        // not yet added to a group
        assertEquals(null, n.getGroup());
    }
}
