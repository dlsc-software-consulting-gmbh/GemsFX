package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.Year;
import java.util.Objects;

/**
 * A view for selecting a year.
 */
public class YearView extends Control {

    /**
     * Constructs a new instance.
     */
    public YearView() {
        getStyleClass().add("year-view");

        setFocusTraversable(false);

        year.bind(Bindings.createIntegerBinding(() -> {
            Year value = getValue();
            if (value != null) {
                return value.getValue();
            }
            return -1;
        }, valueProperty()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(YearView.class.getResource("year-view.css")).toExternalForm();
    }

    private final ReadOnlyIntegerWrapper year = new ReadOnlyIntegerWrapper(this, "year");

    public final int getYear() {
        return year.get();
    }

    /**
     * A read-only integer representation of the current value  / year.
     *
     * @return the year as an integer value
     */
    public final ReadOnlyIntegerProperty yearProperty() {
        return year.getReadOnlyProperty();
    }

    private final ObjectProperty<Year> value = new SimpleObjectProperty<>(this, "value", Year.now());

    public final Year getValue() {
        return value.get();
    }

    /**
     * The currently selected value / year.
     *
     * @return the currently selected value / year
     */
    public final ObjectProperty<Year> valueProperty() {
        return value;
    }

    public final void setValue(Year value) {
        this.value.set(value);
    }

    private final IntegerProperty cols = new SimpleIntegerProperty(this, "cols", 4) {
        @Override
        public void setValue(Number number) {
            if (number.intValue() < 1) {
                throw new IllegalArgumentException("number of columns must be larger than 0");
            }
            super.setValue(number);
        }
    };

    public final int getCols() {
        return cols.get();
    }

    /**
     * Determines how many columns of years will be displayed. The number of
     * columns multiplied with the number of rows determines the total number
     * of years shown per "page".
     *
     * @see #rowsProperty()
     * @return the number of columns
     */
    public final IntegerProperty colsProperty() {
        return cols;
    }

    public final void setCols(int cols) {
        this.cols.set(cols);
    }

    private final IntegerProperty rows = new SimpleIntegerProperty(this, "rows", 5) {
        @Override
        public void setValue(Number number) {
            if (number.intValue() < 1) {
                throw new IllegalArgumentException("number of rows must be larger than 0");
            }
            super.setValue(number);
        }
    };

    public final int getRows() {
        return rows.get();
    }

    /**
     * Determines how many rows of years will be displayed. The number of
     * columns multiplied with the number of rows determines the total number
     * of years shown per "page".
     *
     * @see #colsProperty()
     * @return the number of rows
     */
    public final IntegerProperty rowsProperty() {
        return rows;
    }

    public final void setRows(int rows) {
        this.rows.set(rows);
    }

    private final ObjectProperty<Year> earliestYear = new SimpleObjectProperty<>(this, "earliestYear");

    public final Year getEarliestYear() {
        return earliestYear.get();
    }

    /**
     * The earliest year that the user will be able to select in the view.
     *
     * @return the earliest year
     */
    public final ObjectProperty<Year> earliestYearProperty() {
        return earliestYear;
    }

    public final void setEarliestYear(Year earliestYear) {
        this.earliestYear.set(earliestYear);
    }

    private final ObjectProperty<Year> latestYear = new SimpleObjectProperty<>(this, "latestYear");

    public final Year getLatestYear() {
        return latestYear.get();
    }

    /**
     * The latest year that the user will be able to select in the view.
     *
     * @return the earliest year
     */
    public final ObjectProperty<Year> latestYearProperty() {
        return latestYear;
    }

    public final void setLatestYear(Year latestYear) {
        this.latestYear.set(latestYear);
    }
}
