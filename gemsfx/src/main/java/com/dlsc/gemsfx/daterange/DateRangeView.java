package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.skins.DateRangeViewSkin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Skin;

public class DateRangeView extends DateRangeControlBase {

    public DateRangeView() {
        getStyleClass().add("date-range-view");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangeViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return DateRangeView.class.getResource("date-range-view.css").toExternalForm();
    }

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose", () -> System.out.println("closing"));

    public Runnable getOnClose() {
        return onClose.get();
    }

    public ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose.set(onClose);
    }
}
