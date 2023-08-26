package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

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

    private final ObjectProperty<Integer> value = new SimpleObjectProperty<>(this, "value");

    public Integer getValue() {
        return value.get();
    }

    public ObjectProperty<Integer> valueProperty() {
        return value;
    }

    public void setValue(Integer value) {
        this.value.set(value);
    }

}
