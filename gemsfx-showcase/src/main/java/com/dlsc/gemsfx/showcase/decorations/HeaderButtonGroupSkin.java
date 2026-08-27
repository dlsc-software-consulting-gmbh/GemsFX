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

import javafx.geometry.Pos;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;

/**
 * The default skin for the {@link HeaderButtonGroup}.
 */
public class HeaderButtonGroupSkin extends SkinBase<HeaderButtonGroup> {

    protected HeaderButtonGroupSkin(HeaderButtonGroup control) {
        super(control);

        var root = new HBox();
        root.setAlignment(Pos.TOP_LEFT);
        root.setFillHeight(false);
        root.getStyleClass().setAll("container");
        root.getChildren().setAll(control.getButtons());

        getChildren().setAll(root);
    }
}
