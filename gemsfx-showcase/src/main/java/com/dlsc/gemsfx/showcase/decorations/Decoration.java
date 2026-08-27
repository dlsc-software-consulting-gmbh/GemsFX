/* SPDX-License-Identifier: MIT */

/*
 * This file is a copy of the "atlantafx-decorations" library by mkpaz
 * (https://github.com/mkpaz/atlantafx), licensed under the MIT license.
 * The code was copied into the showcase because the library is currently not
 * available in any public Maven repository. The only modifications are the
 * package name, the location of the theme stylesheets, the removal of the
 * optional JSpecify annotations, and the use of the "leading" / "trailing"
 * properties of the final HeaderBar API. See LICENSE.txt in this package.
 */

package com.dlsc.gemsfx.showcase.decorations;

import java.util.Objects;

/**
 * This enum contains the links to the window decoration themes.
 * Use {@link #getStylesheet()} to obtain the path to a specific theme.
 */
public enum Decoration {

    CHROME_OS_DARK("chrome-os-dark", "ChromeOS Dark", true),
    CHROME_OS_LIGHT("chrome-os-light", "ChromeOS Light", false),
    DEXY_DARK("dexy-dark", "Dexy Dark", true),
    DEXY_LIGHT("dexy-light", "Dexy Light", false),
    FLUENT_DARK("fluent-dark", "Fluent Dark", true),
    FLUENT_LIGHT("fluent-light", "Fluent Light", false),
    DRACULA("dracula", "Dracula", true),
    GENOME_DARK("genome-dark", "Genome Dark", true),
    GENOME_LIGHT("genome-light", "Genome Light", false),
    MAC_SEQUOIA_DARK("mac-sequoia-dark", "MacSequoia Dark", true),
    MAC_SEQUOIA_LIGHT("mac-sequoia-light", "MacSequoia Light", false),
    NORD_DARK("nord-dark", "Nord Dark", true),
    WIN10_DARK("win10-dark", "Windows10 Dark", true),
    WIN10_LIGHT("win10-light", "Windows10 Light", false);

    private final String id;
    private final String name;
    private final boolean dark;

    Decoration(String id, String name, boolean dark) {
        this.id = id;
        this.name = name;
        this.dark = dark;
    }

    /**
     * Returns the theme identifier.
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the theme name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns whether the theme is light or dark.
     */
    public boolean isDark() {
        return dark;
    }

    /**
     * Returns the path to the theme stylesheet.
     */
    public String getStylesheet() {
        var url = Decoration.class.getResource("theme/" + id + ".css");
        return Objects.requireNonNull(url).toString();
    }
}
