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
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.scene.layout.HeaderBar;
import javafx.scene.layout.HeaderButtonType;

/**
 * Represents a header button component of a given {@link HeaderButtonType}.
 */
@SuppressWarnings("deprecation") // preview feature
public class HeaderButton extends Control {

    private final HeaderButtonType type;

    /**
     * Constructs a HeaderButton with the specified HeaderButtonType.
     *
     * @param type the type of the header button; must not be null
     * @throws NullPointerException if the type is null
     */
    public HeaderButton(HeaderButtonType type) {
        super();

        Objects.requireNonNull(type, "HeaderButtonType type must not be null");
        this.type = type;

        getStyleClass().add("header-button");
        HeaderBar.setButtonType(this, type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Skin<?> createDefaultSkin() {
        return new HeaderButtonSkin(this);
    }

    /**
     * Returns the type of the header button.
     */
    public HeaderButtonType getType() {
        return type;
    }
}
