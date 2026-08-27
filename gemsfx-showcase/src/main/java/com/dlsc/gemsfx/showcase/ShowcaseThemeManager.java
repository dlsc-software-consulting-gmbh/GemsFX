package com.dlsc.gemsfx.showcase;

import com.dlsc.gemsfx.showcase.decorations.Decoration;
import com.dlsc.gemsfx.util.ControlsFXAtlantaFX;
import com.dlsc.gemsfx.util.GemsFXAtlantaFX;
import javafx.application.Application;
import javafx.application.ColorScheme;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.Scene;

import java.util.Objects;
import java.util.prefs.Preferences;

/**
 * Applies an AtlantaFX theme to the showcase application. The theme is defined by a
 * {@link ThemeFamily} (e.g. "Nord") and a {@link ThemeMode} (light, dark, or system). When the
 * mode is {@link ThemeMode#SYSTEM} then the manager listens to the color scheme reported by the
 * operating system and switches between the light and the dark variant of the family
 * accordingly. The current selection is stored in the user preferences.
 * <p>
 * Theme families that only exist in a single variant ignore the theme mode altogether, and so
 * does the special family {@link ThemeFamily#MODENA}, which switches the application back to
 * the standard JavaFX theme without applying any AtlantaFX styling at all.
 */
public class ShowcaseThemeManager {

    private static final String KEY_FAMILY = "theme.family";
    private static final String KEY_MODE = "theme.mode";

    private final Preferences preferences;
    private final Scene scene;

    public ShowcaseThemeManager(Scene scene, Preferences preferences) {
        this.scene = Objects.requireNonNull(scene, "scene can not be null");
        this.preferences = Objects.requireNonNull(preferences, "preferences can not be null");

        setThemeFamily(ThemeFamily.findByName(preferences.get(KEY_FAMILY, ThemeFamily.DEFAULT_NAME)));
        setThemeMode(readMode());

        themeFamily.addListener(it -> updateTheme());
        themeMode.addListener(it -> updateTheme());
        Platform.getPreferences().colorSchemeProperty().addListener(it -> updateTheme());

        updateTheme();
    }

    private ThemeMode readMode() {
        try {
            return ThemeMode.valueOf(preferences.get(KEY_MODE, ThemeMode.SYSTEM.name()));
        } catch (IllegalArgumentException ex) {
            return ThemeMode.SYSTEM;
        }
    }

    private void updateTheme() {
        ThemeFamily family = getThemeFamily();

        // families with a single variant (including the standard JavaFX theme) ignore the mode
        boolean dark;
        if (family.isModena()) {
            dark = false;
        } else if (!family.hasBothVariants()) {
            dark = family.isDarkOnly();
        } else {
            dark = switch (getThemeMode()) {
                case LIGHT -> false;
                case DARK -> true;
                case SYSTEM -> Platform.getPreferences().getColorScheme() == ColorScheme.DARK;
            };
        }

        // the system property ensures that the demo applications use the same styling
        if (family.isModena()) {
            System.setProperty("atlantafx", "false");
            Application.setUserAgentStylesheet(Application.STYLESHEET_MODENA);
            scene.getStylesheets().remove(GemsFXAtlantaFX.STYLESHEET);
            scene.getStylesheets().remove(ControlsFXAtlantaFX.STYLESHEET);
        } else {
            System.setProperty("atlantafx", "true");
            Application.setUserAgentStylesheet(family.getTheme(dark).getUserAgentStylesheet());
            GemsFXAtlantaFX.applyTo(scene);
            ControlsFXAtlantaFX.applyTo(scene);
        }

        applyDecoration(dark);

        scene.getRoot().getStyleClass().removeAll("atlantafx-active", "modena-active");
        scene.getRoot().getStyleClass().add(family.isModena() ? "modena-active" : "atlantafx-active");
        scene.getRoot().getStyleClass().remove("dark-theme");
        if (dark) {
            scene.getRoot().getStyleClass().add("dark-theme");
        }

        preferences.put(KEY_FAMILY, getThemeFamily().name());
        preferences.put(KEY_MODE, getThemeMode().name());

        darkTheme.set(dark);
    }

    /**
     * Applies the AtlantaFX window decoration that matches the operating system and the
     * brightness of the currently applied theme. The decoration determines the look of the
     * window buttons shown inside of the header bar.
     */
    private void applyDecoration(boolean dark) {
        Decoration decoration;

        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("mac")) {
            decoration = dark ? Decoration.MAC_SEQUOIA_DARK : Decoration.MAC_SEQUOIA_LIGHT;
        } else if (os.contains("win")) {
            decoration = dark ? Decoration.WIN10_DARK : Decoration.WIN10_LIGHT;
        } else {
            decoration = dark ? Decoration.GENOME_DARK : Decoration.GENOME_LIGHT;
        }

        String stylesheet = decoration.getStylesheet();
        if (decorationStylesheet != null) {
            scene.getStylesheets().remove(decorationStylesheet);
        }

        decorationStylesheet = stylesheet;
        scene.getStylesheets().add(stylesheet);
    }

    private String decorationStylesheet;

    private final ObjectProperty<ThemeFamily> themeFamily = new SimpleObjectProperty<>(this, "themeFamily", ThemeFamily.getDefault());

    /**
     * The currently selected theme family, e.g. "Nord". Together with the {@link #themeModeProperty()
     * theme mode} the family determines the AtlantaFX theme that will be applied to the application.
     *
     * @return the selected theme family
     */
    public final ObjectProperty<ThemeFamily> themeFamilyProperty() {
        return themeFamily;
    }

    public final ThemeFamily getThemeFamily() {
        return themeFamily.get();
    }

    public final void setThemeFamily(ThemeFamily themeFamily) {
        this.themeFamily.set(themeFamily);
    }

    private final ObjectProperty<ThemeMode> themeMode = new SimpleObjectProperty<>(this, "themeMode", ThemeMode.SYSTEM);

    /**
     * The currently selected theme mode. The mode determines whether the light or the dark
     * variant of the selected {@link #themeFamilyProperty() theme family} will be used. The mode
     * {@link ThemeMode#SYSTEM} delegates this decision to the operating system.
     *
     * @return the selected theme mode
     */
    public final ObjectProperty<ThemeMode> themeModeProperty() {
        return themeMode;
    }

    public final ThemeMode getThemeMode() {
        return themeMode.get();
    }

    public final void setThemeMode(ThemeMode themeMode) {
        this.themeMode.set(themeMode);
    }

    private final ReadOnlyBooleanWrapper darkTheme = new ReadOnlyBooleanWrapper(this, "darkTheme");

    /**
     * Determines whether the currently applied theme is a dark theme. This is useful for
     * controls that need to adjust their own styling, e.g. the PDF view.
     *
     * @return true if the currently applied theme is a dark theme
     */
    public final ReadOnlyBooleanProperty darkThemeProperty() {
        return darkTheme.getReadOnlyProperty();
    }

    public final boolean isDarkTheme() {
        return darkTheme.get();
    }
}
