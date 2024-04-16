package com.dlsc.gemsfx.util;

import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a centralized mechanism to retrieve localized strings
 * from property files based on the current locale settings. It caches the resource bundles
 * to avoid repetitive and unnecessary loading of the properties files.
 *
 * <p>Usage involves retrieving strings via base names of the resource bundles
 * or by using predefined types which represent specific views or components
 * in the application.</p>
 */
public class ResourceBundleManager {

    private static final Logger LOG = Logger.getLogger(ResourceBundleManager.class.getName());
    private static final Map<String, ResourceBundle> BUNDLES = new ConcurrentHashMap<>();
    private static Locale currentLocale = Locale.getDefault();

    public enum Type {
        INFO_CENTER_VIEW("info-center-view"),
        NOTIFICATION_VIEW("notification-view");

        private final String baseName;

        Type(String baseName) {
            this.baseName = baseName;
        }

        public String getBaseName() {
            return baseName;
        }
    }

    private ResourceBundleManager() {
    }

    /**
     * Retrieves the resource bundle for the specified base name and the current application locale.
     * This method will return a cached bundle if it exists, or load a new bundle if it does not.
     *
     * @param baseName the base name of the resource bundle.
     * @return the requested resource bundle.
     */
    public static ResourceBundle getBundle(String baseName) {
        return BUNDLES.computeIfAbsent(key(baseName, currentLocale),
                k -> ResourceBundle.getBundle(baseName, currentLocale, ResourceBundleManager.class.getClassLoader()));
    }

    /**
     * Sets the current locale of the application. If the locale is changed,
     * the method clears the cache of loaded resource bundles.
     *
     * @param locale the new locale to set as the current.
     */
    public static void setLocale(Locale locale) {
        if (!locale.equals(currentLocale)) {
            currentLocale = locale;
            // Clear cache as locale has changed
            BUNDLES.clear();
        }
    }


    /**
     *  Generates a unique key based on the base name and locale for caching purposes.
     */
    private static String key(String baseName, Locale locale) {
        return baseName + "_" + locale.toString();
    }

    /**
     * Retrieves a localized string from the resource bundle specified by the base name.
     * If the key is not found, it logs a warning and returns the key itself.
     *
     * @param baseName the base name of the resource bundle.
     * @param key the key for the desired string in the bundle.
     * @return the localized string.
     */
    public static String getString(String baseName, String key) {
        try {
            ResourceBundle bundle = getBundle(baseName);
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            LOG.log(Level.WARNING, "Missing resource key: " + key, ex);
            return key;
        }
    }

    /**
     * Retrieves a localized string from the resource bundle associated with a given type.
     *
     * @param type the type of the resource bundle.
     * @param key the key for the desired string in the bundle.
     * @return the localized string.
     */
    public static String getString(Type type, String key) {
        return getString(type.getBaseName(), key);
    }

}
