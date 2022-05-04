package com.dlsc.gemsfx.util;

import javafx.beans.property.*;

import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

/**
 * A manager for storing observable values in the user preferences.
 */
public class SessionManager {

    private static final Logger LOG = Logger.getLogger(SessionManager.class.getSimpleName());
    private final Preferences preferences;

    /**
     * Constructs a new session manager that will use the passed in preferences.
     *
     * @param preferences the preferences used for persisting the property value
     */
    public SessionManager(Preferences preferences) {
        this.preferences = Objects.requireNonNull(preferences, "preferences can not be null");
    }

    /**
     * Returns the preferences object used for persisting the property values.
     *
     * @return the preferences
     */
    public final Preferences getPreferences() {
        return preferences;
    }

    /**
     * Registers a double property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, DoubleProperty property) {
        LOG.fine("registering double property at path " + path);
        property.set(preferences.getDouble(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putDouble(path, newValue.doubleValue());
            } else {
                preferences.remove(path);
            }
        });
    }

    /**
     * Registers an integer property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, IntegerProperty property) {
        LOG.fine("registering integer property at path " + path);
        property.set(preferences.getInt(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putInt(path, newValue.intValue());
            } else {
                preferences.remove(path);
            }
        });
    }

    /**
     * Registers a float property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, FloatProperty property) {
        LOG.fine("registering float property at path " + path);
        property.set(preferences.getFloat(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putFloat(path, newValue.floatValue());
            } else {
                preferences.remove(path);
            }
        });
    }

    /**
     * Registers a long property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, LongProperty property) {
        LOG.fine("registering long property at path " + path);
        property.set(preferences.getLong(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putLong(path, newValue.longValue());
            } else {
                preferences.remove(path);
            }
        });
    }

    /**
     * Registers a boolean property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, BooleanProperty property) {
        LOG.fine("registering boolean property at path " + path);
        property.set(preferences.getBoolean(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putBoolean(path, newValue);
            } else {
                preferences.remove(path);
            }
        });
    }

    /**
     * Registers a string property so that any changes made to that property will be
     * persisted in the user's preferences and restored for the next client session.
     *
     * @param path     the path to use for the property (e.g. "divider.location"). Paths must
     *                 be unique for all persisted properties.
     * @param property the property to persist and restore across user sessions
     */
    public void register(String path, StringProperty property) {
        LOG.fine("registering string property at path " + path);
        property.set(preferences.get(path, property.get()));
        property.addListener((it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.put(path, newValue);
            } else {
                preferences.remove(path);
            }
        });
    }
}
