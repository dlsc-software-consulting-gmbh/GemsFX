package com.dlsc.gemsfx.util;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
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

    /**
     * Typed categories for the currently supported i18n bundle domains.
     */
    public enum BundleType {
        ARC_PROGRESS_INDICATOR("arc-progress-indicator"),
        AVATAR_VIEW("avatar-view"),
        BEFORE_AFTER_VIEW("before-after-view"),
        CALENDAR_VIEW("calendar-view"),
        CHIP_VIEW("chip-view"),
        CHIPS_VIEW_CONTAINER("chips-view-container"),
        DATE_RANGE_PICKER("date-range-picker"),
        DATE_RANGE_VIEW("date-range-view"),
        DAY_OF_WEEK_PICKER("day-of-week-picker"),
        DIALOG_PANE("dialog-pane"),
        DRAWER_STACK_PANE("drawer-stack-pane"),
        DURATION_PICKER("duration-picker"),
        EMAIL_FIELD("email-field"),
        ENHANCED_LABEL("enhanced-label"),
        FILTER_VIEW("filter-view"),
        GRID_TABLE_COLUMN("grid-table-column"),
        GRID_TABLE_VIEW("grid-table-view"),
        INFO_CENTER_VIEW("info-center-view"),
        INNER_LIST_VIEW("inner-list-view"),
        ITEM_PAGING_CONTROL("item-paging-control"),
        MULTI_COLUMN_LIST_VIEW("multi-column-list-view"),
        NOTIFICATION_VIEW("notification-view"),
        PAGING_CONTROL("paging-control"),
        PHOTO_VIEW("photo-view"),
        POP_OVER("pop-over"),
        RECENT_FILES("recent-files"),
        SCREENS_VIEW("screens-view"),
        SEARCH_FIELD("search-field"),
        SEARCH_TEXT_FIELD("search-text-field"),
        SEGMENTED_BAR("segmented-bar"),
        SELECTION_BOX("selection-box"),
        SIMPLE_FILTER_VIEW("simple-filter-view"),
        SKELETON("skeleton"),
        TEXT_VIEW("text-view"),
        TIME_PICKER("time-picker"),
        TIME_RANGE_PICKER("time-range-picker"),
        TREE_NODE_VIEW("tree-node-view"),
        YEAR_MONTH_PICKER("year-month-picker"),
        YEAR_MONTH_VIEW("year-month-view"),
        YEAR_VIEW("year-view");

        private final String baseName;

        BundleType(String baseName) {
            this.baseName = baseName;
        }

        /**
         * Returns the base name of the resource bundle.
         *
         * @return the base name
         */
        public String getBaseName() {
            return baseName;
        }

        public String getString(String key) {
            return ResourceBundleManager.getString(this, key);
        }

        public String getString(String key, String fallbackValue) {
            return ResourceBundleManager.getString(this, key, fallbackValue);
        }

        public String format(String key, Object... args) {
            return ResourceBundleManager.format(this, key, args);
        }
    }

    /**
     * @deprecated use {@link BundleType} instead.
     */
    @Deprecated
    public enum Type {
        DURATION_PICKER(BundleType.DURATION_PICKER),
        INFO_CENTER_VIEW(BundleType.INFO_CENTER_VIEW),
        NOTIFICATION_VIEW(BundleType.NOTIFICATION_VIEW);

        private final BundleType bundleType;

        Type(BundleType bundleType) {
            this.bundleType = bundleType;
        }

        public BundleType getBundleType() {
            return bundleType;
        }

        public String getBaseName() {
            return bundleType.getBaseName();
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
        Objects.requireNonNull(baseName, "baseName can not be null");
        return BUNDLES.computeIfAbsent(key(baseName, currentLocale),
                k -> ResourceBundle.getBundle(baseName, currentLocale, ResourceBundleManager.class.getClassLoader()));
    }

    public static ResourceBundle getBundle(BundleType bundleType) {
        Objects.requireNonNull(bundleType, "bundleType can not be null");
        return getBundle(bundleType.getBaseName());
    }

    /**
     * @deprecated use {@link #getBundle(BundleType)} instead.
     */
    @Deprecated
    public static ResourceBundle getBundle(Type type) {
        Objects.requireNonNull(type, "type can not be null");
        return getBundle(type.getBundleType());
    }

    /**
     * Sets the current locale of the application. If the locale is changed,
     * the method clears the cache of loaded resource bundles.
     *
     * @param locale the new locale to set as the current.
     */
    public static void setLocale(Locale locale) {
        Objects.requireNonNull(locale, "locale can not be null");
        if (!locale.equals(currentLocale)) {
            currentLocale = locale;
            // Clear cache as locale has changed
            BUNDLES.clear();
        }
    }

    public static Locale getLocale() {
        return currentLocale;
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
        return getString(baseName, key, key);
    }

    /**
     * Retrieves a localized string from the resource bundle specified by the base name.
     * If the key is not found, it logs a warning and returns the provided fallback value.
     *
     * @param baseName the base name of the resource bundle.
     * @param key the key for the desired string in the bundle.
     * @param fallbackValue the fallback value used when the key or bundle is missing.
     * @return the localized string or fallback value.
     */
    public static String getString(String baseName, String key, String fallbackValue) {
        Objects.requireNonNull(key, "key can not be null");
        try {
            ResourceBundle bundle = getBundle(baseName);
            return bundle.getString(key);
        } catch (MissingResourceException ex) {
            LOG.log(Level.WARNING, () -> "Missing resource for bundle '" + baseName + "', key '" + key + "'. Falling back to '" + fallbackValue + "'.");
            LOG.log(Level.FINER, "Missing resource details", ex);
            return fallbackValue;
        }
    }

    /**
     * Retrieves a localized string from the resource bundle associated with a given bundle type.
     *
     * @param bundleType the type of the resource bundle.
     * @param key the key for the desired string in the bundle.
     * @return the localized string.
     */
    public static String getString(BundleType bundleType, String key) {
        return getString(bundleType, key, key);
    }

    /**
     * Retrieves a localized string from the resource bundle associated with a given bundle type.
     *
     * @param bundleType the type of the resource bundle.
     * @param key the key for the desired string in the bundle.
     * @param fallbackValue the fallback value used when the key or bundle is missing.
     * @return the localized string or fallback value.
     */
    public static String getString(BundleType bundleType, String key, String fallbackValue) {
        Objects.requireNonNull(bundleType, "bundleType can not be null");
        return getString(bundleType.getBaseName(), key, fallbackValue);
    }

    /**
     * @deprecated use {@link #getString(BundleType, String)} instead.
     */
    @Deprecated
    public static String getString(Type type, String key) {
        Objects.requireNonNull(type, "type can not be null");
        return getString(type.getBundleType(), key);
    }

    /**
     * @deprecated use {@link #getString(BundleType, String, String)} instead.
     */
    @Deprecated
    public static String getString(Type type, String key, String fallbackValue) {
        Objects.requireNonNull(type, "type can not be null");
        return getString(type.getBundleType(), key, fallbackValue);
    }

    public static String format(BundleType bundleType, String key, Object... args) {
        return MessageFormat.format(getString(bundleType, key), args);
    }

    public static String format(String baseName, String key, Object... args) {
        return MessageFormat.format(getString(baseName, key), args);
    }

}
