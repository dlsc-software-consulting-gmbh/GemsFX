package com.dlsc.gemsfx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.property.*;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.value.ChangeListener;

/**
 * A manager for storing observable values in the user preferences.
 */
public class SessionManager {

    private static final Logger LOG = Logger.getLogger(SessionManager.class.getSimpleName());
    private final Preferences preferences;
    private final HashMap<Property, ArrayList<ChangeListener>> propertyToListeners = new HashMap<>();

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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<Double> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putDouble(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<Integer> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putInt(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<Float> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putFloat(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<Long> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putLong(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<Boolean> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.putBoolean(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
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
        putPropertyValueIntoPreferences(path, property);
        ChangeListener<String> listener = (it, oldValue, newValue) -> {
            if (newValue != null) {
                preferences.put(path, newValue);
            } else {
                preferences.remove(path);
            }
        };
        addListener(property, listener);
    }

    /**
     * Unregisters all listeners created by SessionManager for the specified property.
     *
     * @param property the property to unregister
     */
    public void unregister(Property property) {
        ArrayList<ChangeListener> listeners = propertyToListeners.get(property);
        if (listeners != null) {
            listeners.forEach(listener -> property.removeListener(listener));
            listeners.clear();
            propertyToListeners.remove(property);
        }
    }

    /**
     * Unregisters all listeners created by SessionManager.
     *
     */
    public void unregisterAll() {
        for (Map.Entry<Property, ArrayList<ChangeListener>> entry : propertyToListeners.entrySet()) {
            entry.getValue().forEach(listener -> entry.getKey().removeListener(listener));
            entry.getValue().clear();
        }
        propertyToListeners.clear();
    }

    private void addListener(Property property, ChangeListener listener) {
        property.addListener(listener);
        propertyToListeners.computeIfAbsent(property, k -> new ArrayList<>()).add(listener);
    }

    private void putPropertyValueIntoPreferences(String path, Property property) {
        Object value = property.getValue();
        if (value instanceof Boolean b) {
            preferences.putBoolean(path, b);
        } else if (value instanceof Double d) {
            preferences.putDouble(path, d);
        } else if (value instanceof Float f) {
            preferences.putFloat(path, f);
        } else if (value instanceof Integer i) {
            preferences.putInt(path, i);
        } else if (value instanceof Long l) {
            preferences.putLong(path, l);
        } else if (value instanceof String s) {
            preferences.put(path, s);
        }
    }
}
