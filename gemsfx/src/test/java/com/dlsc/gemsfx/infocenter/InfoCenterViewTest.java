package com.dlsc.gemsfx.infocenter;

import com.dlsc.gemsfx.FxTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link InfoCenterView} focusing on group management, notification aggregation,
 * and pinned/unpinned filtering — all model-level assertions, no animation.
 */
public class InfoCenterViewTest extends FxTestBase {

    private InfoCenterView create() {
        return invoke(InfoCenterView::new);
    }

    @Test
    public void testGroupsInitiallyEmpty() {
        InfoCenterView view = create();
        assertTrue(view.getGroups().isEmpty());
    }

    @Test
    public void testAddGroup() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> view.getGroups().add(group));
        assertEquals(1, view.getGroups().size());
    }

    @Test
    public void testRemoveGroup() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> {
            view.getGroups().add(group);
            view.getGroups().remove(group);
        });
        assertTrue(view.getGroups().isEmpty());
    }

    @Test
    public void testNotificationsAggregated() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> {
            group.getNotifications().add(new Notification<>("T1", "S1"));
            group.getNotifications().add(new Notification<>("T2", "S2"));
            view.getGroups().add(group);
        });
        waitForFxEvents();
        assertEquals(2, view.getUnmodifiableNotifications().size());
    }

    @Test
    public void testPinnedGroupAppearsInPinnedList() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> {
            group.setPinned(true);
            view.getGroups().add(group);
        });
        waitForFxEvents();
        assertTrue(view.getUnmodifiablePinnedGroups().contains(group));
        assertFalse(view.getUnmodifiableUnpinnedGroups().contains(group));
    }

    @Test
    public void testUnpinnedGroupAppearsInUnpinnedList() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> {
            group.setPinned(false);
            view.getGroups().add(group);
        });
        waitForFxEvents();
        assertTrue(view.getUnmodifiableUnpinnedGroups().contains(group));
        assertFalse(view.getUnmodifiablePinnedGroups().contains(group));
    }

    @Test
    public void testPinnedNotificationsAggregated() {
        InfoCenterView view = create();
        NotificationGroup<Void, Notification<Void>> group = new NotificationGroup<>("G");
        runFx(() -> {
            group.setPinned(true);
            group.getNotifications().add(new Notification<>("T", "S"));
            view.getGroups().add(group);
        });
        waitForFxEvents();
        assertEquals(1, view.getUnmodifiablePinnedNotifications().size());
    }

    @Test
    public void testDefaultAutoOpenGroupIsFalse() {
        InfoCenterView view = create();
        assertFalse(view.isAutoOpenGroup());
    }

    @Test
    public void testSetAutoOpenGroup() {
        InfoCenterView view = create();
        runFx(() -> view.setAutoOpenGroup(true));
        assertTrue(view.isAutoOpenGroup());
    }

    @Test
    public void testShowAllGroupIsNullByDefault() {
        InfoCenterView view = create();
        assertEquals(null, view.getShowAllGroup());
    }

    @Test
    public void testLayout() {
        InfoCenterView view = layout(invoke(InfoCenterView::new));
        assertNotNull(view.getSkin());
    }
}
