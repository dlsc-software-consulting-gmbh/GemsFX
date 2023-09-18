package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.YearMonth;
import java.util.Objects;

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
        return Objects.requireNonNull(YearMonthView.class.getResource("year-month-view.css")).toExternalForm();
    }

    private final BooleanProperty showYear = new SimpleBooleanProperty(this, "showYear", true);

    public final boolean isShowYear() {
        return showYear.get();
    }

    /**
     * Determines if the view willl display the current year at the top or not. When this
     * control is used standalone, then showing the year is usually necessary.
     * If the control is part of a more complex control like the {@link CalendarView} then
     * showing the year might not be needed as it becomes obvious from the overall context.
     *
     * @return true if the year will be shown
     */
    public final BooleanProperty showYearProperty() {
        return showYear;
    }

    public final void setShowYear(boolean showYear) {
        this.showYear.set(showYear);
    }

    private final ObjectProperty<YearMonth> value = new SimpleObjectProperty<>(this, "value", YearMonth.now());

    public final YearMonth getValue() {
        return value.get();
    }

    public final ObjectProperty<YearMonth> valueProperty() {
        return value;
    }

    public final void setValue(YearMonth value) {
        this.value.set(value);
    }
}
