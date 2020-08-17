package com.dlsc.gemsfx.skins;

import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class SearchTextField extends CustomTextField {

    public SearchTextField() {
        this(false);
    }

    public SearchTextField(boolean round) {
        if (round) {
            getStyleClass().add("round");
        }

        getStyleClass().add("search-field");
        setPromptText("Search...");

        FontIcon searchIcon = new FontIcon(MaterialDesign.MDI_MAGNIFY);
        searchIcon.getStyleClass().add("search-icon");
        setLeft(searchIcon);

        FontIcon clearIcon = new FontIcon(MaterialDesign.MDI_FORMAT_CLEAR);
        clearIcon.getStyleClass().add("clear-icon");
        //setRight(clearIcon);

        // TODO: this was commented out because the textfield starts to resize itself and keeps growing while the user is typing.
        // This is a known issue: https://bitbucket.org/controlsfx/controlsfx/issues/687/problem-with-customtextfield-and-fixed
    }
}
