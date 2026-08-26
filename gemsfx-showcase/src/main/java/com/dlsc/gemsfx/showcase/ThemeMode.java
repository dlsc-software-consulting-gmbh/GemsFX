package com.dlsc.gemsfx.showcase;

/**
 * The three modes supported by the showcase application. {@link #SYSTEM} follows the color
 * scheme reported by the operating system.
 */
public enum ThemeMode {

    LIGHT("mode.light"),
    DARK("mode.dark"),
    SYSTEM("mode.system");

    private final String key;

    ThemeMode(String key) {
        this.key = key;
    }

    /**
     * Returns the localized name of this mode, e.g. for the tooltip of the mode button.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return ShowcaseBundle.get(key);
    }
}
