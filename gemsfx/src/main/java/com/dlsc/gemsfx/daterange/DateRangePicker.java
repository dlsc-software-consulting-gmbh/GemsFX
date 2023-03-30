package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.skins.DateRangePickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.control.Skin;

public class DateRangePicker extends DateRangeControlBase {

    public DateRangePicker() {
        super();

        getStyleClass().add("date-range-picker");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangePickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return DateRangePicker.class.getResource("date-range-picker.css").toExternalForm();
    }

    private final BooleanProperty smallVersion = new SimpleBooleanProperty(this, "smallVersion", false);

    public boolean isSmallVersion() {
        return smallVersion.get();
    }

    public BooleanProperty smallVersionProperty() {
        return smallVersion;
    }

    public void setSmallVersion(boolean smallVersion) {
        this.smallVersion.set(smallVersion);
    }
}
