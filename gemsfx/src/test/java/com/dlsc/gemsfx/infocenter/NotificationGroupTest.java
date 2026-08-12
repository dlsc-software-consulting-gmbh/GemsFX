package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests for the {@link NotificationGroup} model class.
 */
public class NotificationGroupTest extends FxTestBase {

    @Test
    public void testConstructorSetsName() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("Alerts");
        assertEquals("Alerts", group.getName());
    }

    @Test
    public void testAddNotification() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        group.getNotifications().add(new Notification<>("T", "S"));
        assertEquals(1, group.getNotifications().size());
    }

    @Test
    public void testRemoveNotification() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        Notification<Void> n = new Notification<>("T", "S");
        group.getNotifications().add(n);
        group.getNotifications().remove(n);
        assertTrue(group.getNotifications().isEmpty());
    }

    @Test
    public void testDefaultNotExpanded() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        assertFalse(group.isExpanded());
    }

    @Test
    public void testSetExpanded() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        group.setExpanded(true);
        assertTrue(group.isExpanded());
    }

    @Test
    public void testDefaultNotPinned() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        assertFalse(group.isPinned());
    }

    @Test
    public void testSetPinned() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        group.setPinned(true);
        assertTrue(group.isPinned());
    }

    @Test
    public void testDefaultPinnable() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        assertTrue(group.isPinnable());
    }

    @Test
    public void testDefaultMaximumNumberOfNotifications() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        assertEquals(10, group.getMaximumNumberOfNotifications());
    }

    @Test
    public void testSetMaximumNumberOfNotifications() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        group.setMaximumNumberOfNotifications(5);
        assertEquals(5, group.getMaximumNumberOfNotifications());
    }

    @Test
    public void testSetSortOrder() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        group.setSortOrder(3);
        assertEquals(3, (int) group.getSortOrder());
    }

    @Test
    public void testDefaultShowHeader() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        assertTrue(group.isShowHeader());
    }
}
