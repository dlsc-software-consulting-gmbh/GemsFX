package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CountryCallingCode;
import javafx.scene.control.ListCell;

public class CountryCallingCodeCell extends ListCell<CountryCallingCode> {

    CountryCallingCodeCell() {
        getStyleClass().add("country-calling-code-cell");
    }

    @Override
    protected void updateItem(CountryCallingCode item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            setText("(+" + item.getCountryCode() + ") " + item.getCountryName("en"));
            setGraphic(item.getFlagView());
        } else {
            setText(null);
            setGraphic(null);
        }
    }

}
