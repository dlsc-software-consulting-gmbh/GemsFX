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
        /**
         * Bundle type for arc progress indicator resources.
         */
        ARC_PROGRESS_INDICATOR("arc-progress-indicator"),
        /**
         * Bundle type for avatar view resources.
         */
        AVATAR_VIEW("avatar-view"),
        /**
         * Bundle type for before-after view resources.
         */
        BEFORE_AFTER_VIEW("before-after-view"),
        /**
         * Bundle type for calendar view resources.
         */
        CALENDAR_VIEW("calendar-view"),
        /**
         * Bundle type for chip view resources.
         */
        CHIP_VIEW("chip-view"),
        /**
         * Bundle type for chips view container resources.
         */
        CHIPS_VIEW_CONTAINER("chips-view-container"),
        /**
         * Bundle type for date range picker resources.
         */
        DATE_RANGE_PICKER("date-range-picker"),
        /**
         * Bundle type for date range view resources.
         */
        DATE_RANGE_VIEW("date-range-view"),
        /**
         * Bundle type for day-of-week picker resources.
         */
        DAY_OF_WEEK_PICKER("day-of-week-picker"),
        /**
         * Bundle type for dialog pane resources.
         */
        DIALOG_PANE("dialog-pane"),
        /**
         * Bundle type for drawer stack pane resources.
         */
        DRAWER_STACK_PANE("drawer-stack-pane"),
        /**
         * Bundle type for duration picker resources.
         */
        DURATION_PICKER("duration-picker"),
        /**
         * Bundle type for email field resources.
         */
        EMAIL_FIELD("email-field"),
        /**
         * Bundle type for enhanced label resources.
         */
        ENHANCED_LABEL("enhanced-label"),
        /**
         * Bundle type for filter view resources.
         */
        FILTER_VIEW("filter-view"),
        /**
         * Bundle type for grid table column resources.
         */
        GRID_TABLE_COLUMN("grid-table-column"),
        /**
         * Bundle type for grid table view resources.
         */
        GRID_TABLE_VIEW("grid-table-view"),
        /**
         * Bundle type for info center view resources.
         */
        INFO_CENTER_VIEW("info-center-view"),
        /**
         * Bundle type for inner list view resources.
         */
        INNER_LIST_VIEW("inner-list-view"),
        /**
         * Bundle type for item paging control resources.
         */
        ITEM_PAGING_CONTROL("item-paging-control"),
        /**
         * Bundle type for multi-column list view resources.
         */
        MULTI_COLUMN_LIST_VIEW("multi-column-list-view"),
        /**
         * Bundle type for notification view resources.
         */
        NOTIFICATION_VIEW("notification-view"),
        /**
         * Bundle type for paging control resources.
         */
        PAGING_CONTROL("paging-control"),
        /**
         * Bundle type for photo view resources.
         */
        PHOTO_VIEW("photo-view"),
        /**
         * Bundle type for pop-over resources.
         */
        POP_OVER("pop-over"),
        /**
         * Bundle type for recent files resources.
         */
        RECENT_FILES("recent-files"),
        /**
         * Bundle type for screens view resources.
         */
        SCREENS_VIEW("screens-view"),
        /**
         * Bundle type for search field resources.
         */
        SEARCH_FIELD("search-field"),
        /**
         * Bundle type for search text field resources.
         */
        SEARCH_TEXT_FIELD("search-text-field"),
        /**
         * Bundle type used for segmented bar localization lookups.
         */
        SEGMENTED_BAR("segmented-bar"),
        /**
         * Bundle type for selection box resources.
         */
        SELECTION_BOX("selection-box"),
        /**
         * Bundle type used for simple filter view localization lookups.
         */
        SIMPLE_FILTER_VIEW("simple-filter-view"),
        /**
         * Bundle type used for skeleton localization lookups.
         */
        SKELETON("skeleton"),
        /**
         * Bundle type for text view resources.
         */
        TEXT_VIEW("text-view"),
        /**
         * Bundle type for time picker resources.
         */
        TIME_PICKER("time-picker"),
        /**
         * Bundle type for time range picker resources.
         */
        TIME_RANGE_PICKER("time-range-picker"),
        /**
         * Bundle type for tree node view resources.
         */
        TREE_NODE_VIEW("tree-node-view"),
        /**
         * Bundle type for year-month picker resources.
         */
        YEAR_MONTH_PICKER("year-month-picker"),
        /**
         * Bundle type used for year-month view localization lookups.
         */
        YEAR_MONTH_VIEW("year-month-view"),
        /**
         * Bundle type used for year view localization lookups.
         */
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

        /**
         * Returns the localized string for the given key from this bundle type.
         *
         * @param key the resource key
         * @return the localized string, or the key if it is missing
         */
        public String getString(String key) {
            return ResourceBundleManager.getString(this, key);
        }

        /**
         * Returns the localized string for the given key from this bundle type.
         *
         * @param key the resource key
         * @param fallbackValue the fallback value used when the key or bundle is missing
         * @return the localized string, or the fallback value if it is missing
         */
        public String getString(String key, String fallbackValue) {
            return ResourceBundleManager.getString(this, key, fallbackValue);
        }

        /**
         * Formats the localized string for the given key with {@link MessageFormat}.
         *
         * @param key the resource key
         * @param args the arguments inserted into the localized pattern
         * @return the formatted localized string
         */
        public String format(String key, Object... args) {
            return ResourceBundleManager.format(this, key, args);
        }
    }

    /**
     * Deprecated bundle categories retained for source compatibility.
     *
     * @deprecated use {@link BundleType} instead.
     */
    @Deprecated
    public enum Type {
        /**
         * Deprecated duration picker bundle type.
         */
        DURATION_PICKER(BundleType.DURATION_PICKER),
        /**
         * Deprecated info center view bundle type.
         */
        INFO_CENTER_VIEW(BundleType.INFO_CENTER_VIEW),
        /**
         * Deprecated notification view bundle type.
         */
        NOTIFICATION_VIEW(BundleType.NOTIFICATION_VIEW);

        private final BundleType bundleType;

        Type(BundleType bundleType) {
            this.bundleType = bundleType;
        }

        /**
         * Returns the replacement {@link BundleType}.
         *
         * @return the replacement bundle type
         */
        public BundleType getBundleType() {
            return bundleType;
        }

        /**
         * Returns the base name of the replacement resource bundle.
         *
         * @return the base name
         */
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

    /**
     * Retrieves the resource bundle for the specified bundle type and the current application locale.
     *
     * @param bundleType the bundle type
     * @return the requested resource bundle
     */
    public static ResourceBundle getBundle(BundleType bundleType) {
        Objects.requireNonNull(bundleType, "bundleType can not be null");
        return getBundle(bundleType.getBaseName());
    }

    /**
     * Retrieves the resource bundle for the specified deprecated type and the current application locale.
     *
     * @param type the deprecated bundle type
     * @return the requested resource bundle
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

    /**
     * Returns the current locale used to load resource bundles.
     *
     * @return the current locale
     */
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
     * Retrieves a localized string from the resource bundle associated with a deprecated type.
     *
     * @param type the deprecated bundle type
     * @param key the key for the desired string in the bundle
     * @return the localized string, or the key if it is missing
     * @deprecated use {@link #getString(BundleType, String)} instead.
     */
    @Deprecated
    public static String getString(Type type, String key) {
        Objects.requireNonNull(type, "type can not be null");
        return getString(type.getBundleType(), key);
    }

    /**
     * Retrieves a localized string from the resource bundle associated with a deprecated type.
     *
     * @param type the deprecated bundle type
     * @param key the key for the desired string in the bundle
     * @param fallbackValue the fallback value used when the key or bundle is missing
     * @return the localized string, or the fallback value if it is missing
     * @deprecated use {@link #getString(BundleType, String, String)} instead.
     */
    @Deprecated
    public static String getString(Type type, String key, String fallbackValue) {
        Objects.requireNonNull(type, "type can not be null");
        return getString(type.getBundleType(), key, fallbackValue);
    }

    /**
     * Formats the localized string for the given bundle type and key with {@link MessageFormat}.
     *
     * @param bundleType the bundle type
     * @param key the resource key
     * @param args the arguments inserted into the localized pattern
     * @return the formatted localized string
     */
    public static String format(BundleType bundleType, String key, Object... args) {
        return MessageFormat.format(getString(bundleType, key), args);
    }

    /**
     * Formats the localized string for the given base name and key with {@link MessageFormat}.
     *
     * @param baseName the base name of the resource bundle
     * @param key the resource key
     * @param args the arguments inserted into the localized pattern
     * @return the formatted localized string
     */
    public static String format(String baseName, String key, Object... args) {
        return MessageFormat.format(getString(baseName, key), args);
    }

}
