package com.dlsc.gemsfx.util;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.FloatProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.junit.Test;

import java.util.prefs.Preferences;

import static org.junit.Assert.assertEquals;

public class SessionManagerTest {

    @Test
    public void shouldRestoreInteger() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        final int year = 1969;
        IntegerProperty yearProperty = new SimpleIntegerProperty();
        sessionManager.register("test.integer", yearProperty);
        yearProperty.set(year);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        IntegerProperty newYearProperty = new SimpleIntegerProperty();

        // when
        newManager.register("test.integer", newYearProperty);

        // then
        assertEquals(year, newYearProperty.get());
    }

    @Test
    public void shouldRestoreDouble() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        double year = 1969d;
        DoubleProperty yearProperty = new SimpleDoubleProperty();
        sessionManager.register("test.double", yearProperty);
        yearProperty.set(year);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        DoubleProperty newYearProperty = new SimpleDoubleProperty();

        // when
        newManager.register("test.double", newYearProperty);

        // then
        assertEquals(year, newYearProperty.get(), 0);
    }

    @Test
    public void shouldRestoreFloat() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        final float year = 1969f;
        FloatProperty yearProperty = new SimpleFloatProperty();
        sessionManager.register("test.float", yearProperty);
        yearProperty.set(year);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        FloatProperty newYearProperty = new SimpleFloatProperty();

        // when
        newManager.register("test.float", newYearProperty);

        // then
        assertEquals(year, newYearProperty.get(), 0);
    }

    @Test
    public void shouldRestoreLong() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        final long year = 1969L;
        LongProperty yearProperty = new SimpleLongProperty();
        sessionManager.register("test.long", yearProperty);
        yearProperty.set(year);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        LongProperty newYearProperty = new SimpleLongProperty();

        // when
        newManager.register("test.long", newYearProperty);

        // then
        assertEquals(year, newYearProperty.get());
    }

    @Test
    public void shouldRestoreString() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        final String year = "1969";
        StringProperty yearProperty = new SimpleStringProperty();
        sessionManager.register("test.string", yearProperty);
        yearProperty.set(year);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        StringProperty newYearProperty = new SimpleStringProperty();

        // when
        newManager.register("test.string", newYearProperty);

        // then
        assertEquals(year, newYearProperty.get());
    }

    @Test
    public void shouldRestoreBoolean() {
        // given
        SessionManager sessionManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));

        final boolean value = Boolean.TRUE;
        BooleanProperty flagProperty = new SimpleBooleanProperty();
        sessionManager.register("test.boolean", flagProperty);
        flagProperty.set(value);

        SessionManager newManager = new SessionManager(Preferences.userRoot().node("gemsfx.tests"));
        BooleanProperty newFlagProperty = new SimpleBooleanProperty();

        // when
        newManager.register("test.boolean", newFlagProperty);

        // then
        assertEquals(value, newFlagProperty.get());
    }
}
