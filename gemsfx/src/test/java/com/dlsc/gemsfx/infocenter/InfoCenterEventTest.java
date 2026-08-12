package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link InfoCenterEvent} class and its event-type constants.
 */
public class InfoCenterEventTest extends FxTestBase {

    @Test
    public void testEventTypesAreNotNull() {
        assertNotNull(InfoCenterEvent.ANY);
        assertNotNull(InfoCenterEvent.NOTIFICATION_CHOSEN);
        assertNotNull(InfoCenterEvent.NOTIFICATION_ADDED);
        assertNotNull(InfoCenterEvent.NOTIFICATION_REMOVED);
        assertNotNull(InfoCenterEvent.HIDE);
    }

    @Test
    public void testConstructorWithTypeOnly() {
        InfoCenterEvent event = new InfoCenterEvent(InfoCenterEvent.HIDE);
        assertEquals(InfoCenterEvent.HIDE, event.getEventType());
        assertNull(event.getNotification());
    }

    @Test
    public void testConstructorWithNotification() {
        Notification<Void> n = new Notification<>("Title", "Summary");
        InfoCenterEvent event = new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_ADDED, n);
        assertEquals(InfoCenterEvent.NOTIFICATION_ADDED, event.getEventType());
        assertEquals(n, event.getNotification());
    }

    @Test
    public void testNotificationChosenEvent() {
        Notification<Void> n = new Notification<>("T", "S");
        InfoCenterEvent event = new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_CHOSEN, n);
        assertEquals(InfoCenterEvent.NOTIFICATION_CHOSEN, event.getEventType());
        assertEquals(n, event.getNotification());
    }

    @Test
    public void testNotificationRemovedEvent() {
        Notification<Void> n = new Notification<>("T", "S");
        InfoCenterEvent event = new InfoCenterEvent(InfoCenterEvent.NOTIFICATION_REMOVED, n);
        assertEquals(InfoCenterEvent.NOTIFICATION_REMOVED, event.getEventType());
    }

    @Test
    public void testAnyIsParentOfSpecificTypes() {
        // EventType hierarchy: specific types should be subtypes of ANY
        assertEquals(InfoCenterEvent.ANY, InfoCenterEvent.NOTIFICATION_CHOSEN.getSuperType());
        assertEquals(InfoCenterEvent.ANY, InfoCenterEvent.NOTIFICATION_ADDED.getSuperType());
        assertEquals(InfoCenterEvent.ANY, InfoCenterEvent.NOTIFICATION_REMOVED.getSuperType());
        assertEquals(InfoCenterEvent.ANY, InfoCenterEvent.HIDE.getSuperType());
    }
}
