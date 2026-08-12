package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Tests for the {@link NotificationAction} model class.
 */
public class NotificationActionTest extends FxTestBase {

    @Test
    public void testConstructorWithTextOnly() {
        NotificationAction<Void> action = new NotificationAction<>("Dismiss");
        assertEquals("Dismiss", action.getText());
        // the single argument constructor installs a default action that removes the notification
        assertNotNull(action.getOnAction());
    }

    @Test
    public void testConstructorWithCallback() {
        NotificationAction<Void> action = new NotificationAction<>("OK",
                n -> Notification.OnClickBehaviour.REMOVE);
        assertEquals("OK", action.getText());
        assertNotNull(action.getOnAction());
    }

    @Test
    public void testSetText() {
        NotificationAction<Void> action = new NotificationAction<>("Old");
        action.setText("New");
        assertEquals("New", action.getText());
    }

    @Test
    public void testSetOnAction() {
        NotificationAction<Void> action = new NotificationAction<>("X");
        action.setOnAction(n -> Notification.OnClickBehaviour.HIDE_AND_REMOVE);
        assertNotNull(action.getOnAction());
    }

    @Test
    public void testOnActionCallback() {
        Notification<Void> notification = new Notification<>("T", "S");
        NotificationAction<Void> action = new NotificationAction<>("OK",
                n -> Notification.OnClickBehaviour.REMOVE);
        Notification.OnClickBehaviour result = action.getOnAction().call(notification);
        assertEquals(Notification.OnClickBehaviour.REMOVE, result);
    }
}
