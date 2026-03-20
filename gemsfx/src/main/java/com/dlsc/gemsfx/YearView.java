package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearViewSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableIntegerProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.SizeConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    private final StyleableIntegerProperty cols = new StyleableIntegerProperty(4) {
        @Override
        protected void invalidated() {
            if (get() < 1) {
                throw new IllegalArgumentException("number of columns must be larger than 0");
            }
        }
        @Override
        public Object getBean() { return YearView.this; }
        @Override
        public String getName() { return "cols"; }
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.COLS;
        }
    };

    public final int getCols() {
        return cols.get();
    }

    /**
     * Determines how many columns of years will be displayed. The number of
     * columns multiplied with the number of rows determines the total number
     * of years shown per "page".
     * <p>
     * Can be set via CSS using the {@code -fx-cols} property.
     * Valid values are positive integers (&gt;= 1).
     * The default value is {@code 4}.
     * </p>
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

    private final StyleableIntegerProperty rows = new StyleableIntegerProperty(5) {
        @Override
        protected void invalidated() {
            if (get() < 1) {
                throw new IllegalArgumentException("number of rows must be larger than 0");
            }
        }
        @Override
        public Object getBean() { return YearView.this; }
        @Override
        public String getName() { return "rows"; }
        @Override
        public CssMetaData<? extends Styleable, Number> getCssMetaData() {
            return StyleableProperties.ROWS;
        }
    };

    public final int getRows() {
        return rows.get();
    }

    /**
     * Determines how many rows of years will be displayed. The number of
     * columns multiplied with the number of rows determines the total number
     * of years shown per "page".
     * <p>
     * Can be set via CSS using the {@code -fx-rows} property.
     * Valid values are positive integers (&gt;= 1).
     * The default value is {@code 5}.
     * </p>
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

    private static class StyleableProperties {

        private static final CssMetaData<YearView, Number> COLS =
            new CssMetaData<>("-fx-cols", SizeConverter.getInstance(), 4) {
                @Override
                public boolean isSettable(YearView c) {
                    return !c.cols.isBound();
                }
                @Override
                public StyleableProperty<Number> getStyleableProperty(YearView c) {
                    return (StyleableProperty<Number>) c.cols;
                }
            };

        private static final CssMetaData<YearView, Number> ROWS =
            new CssMetaData<>("-fx-rows", SizeConverter.getInstance(), 5) {
                @Override
                public boolean isSettable(YearView c) {
                    return !c.rows.isBound();
                }
                @Override
                public StyleableProperty<Number> getStyleableProperty(YearView c) {
                    return (StyleableProperty<Number>) c.rows;
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(COLS);
            styleables.add(ROWS);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
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
