package com.dlsc.gemsfx.showcase;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Gives access to the localized texts of the showcase application. The texts are stored in the
 * resource bundle "showcase.properties" and its translations, the language is determined by the
 * default locale of the JVM.
 * <p>
 * The PDF manuals and the source code of the demo applications are not localized, only the user
 * interface of the showcase itself.
 */
public final class ShowcaseBundle {

    private static final Logger LOG = System.getLogger(ShowcaseBundle.class.getName());

    private static final String BASE_NAME = "com.dlsc.gemsfx.showcase.showcase";

    private static final ResourceBundle BUNDLE = ResourceBundle.getBundle(BASE_NAME);

    private ShowcaseBundle() {
    }

    /**
     * Returns the text stored for the given key.
     *
     * @param key the key of the text
     * @return the localized text, or the key itself if no text is stored for it
     */
    public static String get(String key) {
        try {
            return BUNDLE.getString(key);
        } catch (MissingResourceException ex) {
            LOG.log(Level.WARNING, () -> "missing text for key \"" + key + "\"");
            return key;
        }
    }

    /**
     * Returns the text stored for the given key, with the given arguments filled into the
     * placeholders of the text.
     *
     * @param key  the key of the text
     * @param args the arguments for the placeholders
     * @return the localized and formatted text
     */
    public static String format(String key, Object... args) {
        return MessageFormat.format(get(key), args);
    }

    /**
     * Returns the locale of the texts that are actually being used. This might differ from the
     * default locale of the JVM, e.g. when no translation exists for it.
     *
     * @return the locale of the resource bundle
     */
    public static Locale getLocale() {
        return BUNDLE.getLocale();
    }
}
