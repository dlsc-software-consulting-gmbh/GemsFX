package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.Year;

public class YearView extends Control {

    public YearView() {
        getStyleClass().add("year-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return YearView.class.getResource("year-view.css").toExternalForm();
    }

    private final ObjectProperty<Year> value = new SimpleObjectProperty<>(this, "value");

    public Year getValue() {
        return value.get();
    }

    public ObjectProperty<Year> valueProperty() {
        return value;
    }

    public void setValue(Year value) {
        this.value.set(value);
    }

}
