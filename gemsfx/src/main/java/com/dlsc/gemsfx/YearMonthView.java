package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.YearMonthViewSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Objects;

/**
 * A view for displaying and selecting year-month values.
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-show-year}</td><td>{@code Boolean}</td><td>Whether to show the year at the top.</td></tr>
 *   </tbody>
 * </table>
 */
public class YearMonthView extends Control {

    /**
     * Constructs a new year-month view.
     */
    public YearMonthView() {
        getStyleClass().add("year-month-view");
        setFocusTraversable(false);

        // the UI / the skin should prevent this from happening, but just in case, we make sure that the value is
        // always within the allowed range
        valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                YearMonth earliestMonth = getEarliestMonth();
                YearMonth latestMonth = getLatestMonth();
                if (earliestMonth != null && newValue.isBefore(earliestMonth)) {
                    setValue(earliestMonth);
                } else if (latestMonth != null && newValue.isAfter(latestMonth)) {
                    setValue(latestMonth);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new YearMonthViewSkin(this);
    }

    /**
     * {@inheritDoc}
     *
     * @return the user agent stylesheet
     */
    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(YearMonthView.class.getResource("year-month-view.css")).toExternalForm();
    }

    private final StyleableBooleanProperty showYear = new StyleableBooleanProperty(true) {
        /**
         * {@inheritDoc}
         *
         * @return the owning bean
         */
        @Override
        public Object getBean() { return YearMonthView.this; }
        /**
         * {@inheritDoc}
         *
         * @return the property name
         */
        @Override
        public String getName() { return "showYear"; }
        /**
         * {@inheritDoc}
         *
         * @return the CSS metadata for this property
         */
        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.SHOW_YEAR;
        }
    };

    public final boolean isShowYear() {
        return showYear.get();
    }

    /**
     * Determines if the view willl display the current year at the top or not. When this
     * control is used standalone, then showing the year is usually necessary.
     * If the control is part of a more complex control like the {@link CalendarView} then
     * showing the year might not be needed as it becomes obvious from the overall context.
     * <p>
     * Can be set via CSS using the {@code -fx-show-year} property.
     * Valid values are: {@code true}, {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if the year will be shown
     */
    public final BooleanProperty showYearProperty() {
        return showYear;
    }

    public final void setShowYear(boolean showYear) {
        this.showYear.set(showYear);
    }

    private static class StyleableProperties {

        private static final CssMetaData<YearMonthView, Boolean> SHOW_YEAR =
            new CssMetaData<>("-fx-show-year", BooleanConverter.getInstance(), true) {
                /**
                 * {@inheritDoc}
                 *
                 * @return true if the property can be styled
                 *
                 * @param c the control to inspect
                 */
                @Override
                public boolean isSettable(YearMonthView c) {
                    return !c.showYear.isBound();
                }
                /**
                 * {@inheritDoc}
                 *
                 * @return the styleable property
                 *
                 * @param c the control to inspect
                 */
                @Override
                public StyleableProperty<Boolean> getStyleableProperty(YearMonthView c) {
                    return (StyleableProperty<Boolean>) c.showYear;
                }
            };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;
        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            styleables.add(SHOW_YEAR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the CSS metadata supported by this control
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     *
     * @return the supported CSS metadata
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    private final ObjectProperty<YearMonth> value = new SimpleObjectProperty<>(this, "value", YearMonth.now());

    public final YearMonth getValue() {
        return value.get();
    }

    /**
     * Stores the selected year and month.
     *
     * @return the selected year-month property
     */
    public final ObjectProperty<YearMonth> valueProperty() {
        return value;
    }

    public final void setValue(YearMonth value) {
        this.value.set(value);
    }

    private final ObjectProperty<StringConverter<Month>> converter = new SimpleObjectProperty<>(this, "converter", new StringConverter<>() {
        /**
         * {@inheritDoc}
         *
         * @return the string representation of the value
         *
         * @param month the value to convert
         */
        @Override
        public String toString(Month month) {
            if (month != null) {
                return month.getDisplayName(TextStyle.FULL, Locale.getDefault());
            }
            return null;
        }

        /**
         * {@inheritDoc}
         *
         * @return the parsed value
         *
         * @param s the string to parse
         */
        @Override
        public Month fromString(String s) {
            return null;
        }
    });

    public final StringConverter<Month> getConverter() {
        return converter.get();
    }

    /**
     * Stores the converter used to format the month names shown by the view.
     *
     * @return the month converter property
     */
    public final ObjectProperty<StringConverter<Month>> converterProperty() {
        return converter;
    }

    public final void setConverter(StringConverter<Month> converter) {
        this.converter.set(converter);
    }

    private final ObjectProperty<YearMonth> earliestMonth = new SimpleObjectProperty<>(this, "earliestMonth");

    public final YearMonth getEarliestMonth() {
        return earliestMonth.get();
    }

    /**
     * Determines the earliest month that the user can select in the view.
     *
     * @return the earliest selectable month
     */
    public final ObjectProperty<YearMonth> earliestMonthProperty() {
        return earliestMonth;
    }

    public final void setEarliestMonth(YearMonth earliestMonth) {
        this.earliestMonth.set(earliestMonth);
    }

    private final ObjectProperty<YearMonth> latestMonth = new SimpleObjectProperty<>(this, "latestMonth");

    public final YearMonth getLatestMonth() {
        return latestMonth.get();
    }

    /**
     * Determines the latest month that the user can select in the view.
     *
     * @return the latest selectable month
     */
    public final ObjectProperty<YearMonth> latestMonthProperty() {
        return latestMonth;
    }

    public final void setLatestMonth(YearMonth latestMonth) {
        this.latestMonth.set(latestMonth);
    }
}
