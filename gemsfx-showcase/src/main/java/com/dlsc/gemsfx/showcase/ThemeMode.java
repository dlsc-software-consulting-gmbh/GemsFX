package com.dlsc.gemsfx.showcase;

/**
 * The three modes supported by the showcase application. {@link #SYSTEM} follows the color
 * scheme reported by the operating system.
 */
public enum ThemeMode {

    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System");

    private final String displayName;

    ThemeMode(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
