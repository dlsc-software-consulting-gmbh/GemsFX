package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.YearMonth;

public class YearMonthView extends Control {

    public YearMonthView() {
        getStyleClass().add("year-month-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return YearMonthView.class.getResource("year-month-view.css").toExternalForm();
    }

    private final ObjectProperty<YearMonth> value = new SimpleObjectProperty<>(this, "value", YearMonth.now());

    public YearMonth getValue() {
        return value.get();
    }

    public ObjectProperty<YearMonth> valueProperty() {
        return value;
    }

    public void setValue(YearMonth value) {
        this.value.set(value);
    }
}
