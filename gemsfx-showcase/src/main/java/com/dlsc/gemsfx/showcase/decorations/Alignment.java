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

import javafx.scene.layout.HeaderBar;

/**
 * Represents the alignment of the {@link HeaderButtonGroup} in the {@link HeaderBar}.
 */
@SuppressWarnings("deprecation") // preview feature
public enum Alignment {

    /**
     * The alignment is based on the operating system.
     * Left (leading) for macOS and right (trailing) otherwise.
     */
    AUTO,

    /**
     * Aligns the button group on the left side of the header bar.
     */
    LEADING,

    /**
     * Aligns the button group on the right side of the header bar.
     */
    TRAILING
}