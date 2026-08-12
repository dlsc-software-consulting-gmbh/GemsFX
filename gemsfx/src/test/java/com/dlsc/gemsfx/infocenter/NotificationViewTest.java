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
 * Tests for {@link NotificationView} focusing on construction and accessible model state.
 * Each notification is added to a group before constructing a view, because
 * {@link NotificationView} accesses {@code notification.getGroup()} at construction time.
 */
public class NotificationViewTest extends FxTestBase {

    private static final ZonedDateTime FIXED = ZonedDateTime.of(2024, 3, 15, 10, 0, 0, 0, ZoneId.of("UTC"));

    private Notification<Void> notificationInGroup() {
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        Notification<Void> n = new Notification<>("Title", "Summary", FIXED);
        group.getNotifications().add(n);
        return n;
    }

    @Test
    public void testConstructionWithNotification() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = invoke(() -> new NotificationView<>(n));
        assertEquals(n, view.getNotification());
    }

    @Test
    public void testDefaultShowContentIsFalse() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = invoke(() -> new NotificationView<>(n));
        assertFalse(view.isShowContent());
    }

    @Test
    public void testSetContent() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = invoke(() -> {
            NotificationView<Void, Notification<Void>> v = new NotificationView<>(n);
            v.setContent(new javafx.scene.control.Label("content"));
            return v;
        });
        assertNotNull(view.getContent());
    }

    @Test
    public void testSetShowContent() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = invoke(() -> new NotificationView<>(n));
        runFx(() -> view.setShowContent(true));
        assertTrue(view.isShowContent());
    }

    @Test
    public void testSetGraphic() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = invoke(() -> new NotificationView<>(n));
        runFx(() -> view.setGraphic(new javafx.scene.control.Label("icon")));
        assertNotNull(view.getGraphic());
    }

    @Test
    public void testLayout() {
        Notification<Void> n = invoke(this::notificationInGroup);
        NotificationView<Void, Notification<Void>> view = layout(invoke(() -> new NotificationView<>(n)));
        assertNotNull(view);
    }
}
