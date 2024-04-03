package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.skins.DateRangePickerSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Skin;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Objects;

/**
 * A control to let the user select a date range (start date, end date) via two {@link com.dlsc.gemsfx.CalendarView}
 * instances or via a preset link.
 *
 * @see DateRangeView
 */
public class DateRangePicker extends ComboBoxBase<DateRange> {

    private DateRangeView dateRangeView;

    /**
     * Constructs a new picker.
     */
    public DateRangePicker() {
        super();

        dateRangeView = getDateRangeView();

        setValue(dateRangeView.getValue());

        getStyleClass().add("date-range-picker");
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangePickerSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DateRangePicker.class.getResource("date-range-picker.css")).toExternalForm();
    }

    /**
     * Returns the view that will be shown by this picker when expanded.
     *
     * @return the actual view when expanded
     */
    public DateRangeView getDateRangeView() {
        if (dateRangeView == null) {
            dateRangeView = new DateRangeView();
        }
        return dateRangeView;
    }

    // show icon

    private final BooleanProperty showIcon = new SimpleBooleanProperty(this, "showIcon", true);

    public final boolean isShowIcon() {
        return showIcon.get();
    }

    /**
     * Determines if the picker will show the calendar icon in front
     * of the selected date range.
     *
     * @return true if the icon will be shown
     */
    public final BooleanProperty showIconProperty() {
        return showIcon;
    }

    public final void setShowIcon(boolean showIcon) {
        this.showIcon.set(showIcon);
    }

    // preset title

    private final BooleanProperty showPresetTitle = new SimpleBooleanProperty(this, "showPresetTitle", true);

    public final boolean isShowPresetTitle() {
        return showPresetTitle.get();
    }

    /**
     * Determines if the picker will show the name of the selected preset in front
     * of the selected date range, e.g. "Last Week".
     *
     * @return true if the preset title will be shown
     */
    public final BooleanProperty showPresetTitleProperty() {
        return showPresetTitle;
    }

    public final void setShowPresetTitle(boolean showPresetTitle) {
        this.showPresetTitle.set(showPresetTitle);
    }

    // small

    private final BooleanProperty small = new SimpleBooleanProperty(this, "small", true);

    public final boolean isSmall() {
        return small.get();
    }

    /**
     * The picker can either display all of its information on two lines or in a single
     * line (small).
     *
     * @return true if the picker displays its value on a single line of text
     */
    public final BooleanProperty smallProperty() {
        return small;
    }

    public final void setSmall(boolean small) {
        this.small.set(small);
    }

    // formatter

    private final ObjectProperty<DateTimeFormatter> formatter = new SimpleObjectProperty<>(this, "formatter", DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));

    public final DateTimeFormatter getFormatter() {
        return formatter.get();
    }

    /**
     * The formatter used to convert the selected date / date range to text.
     *
     * @return the date formatter
     */
    public final ObjectProperty<DateTimeFormatter> formatterProperty() {
        return formatter;
    }

    public final void setFormatter(DateTimeFormatter formatter) {
        this.formatter.set(formatter);
    }

    // custom range text

    private final StringProperty customRangeText = new SimpleStringProperty(this, "customRangeText", "Date Range");

    public final String getCustomRangeText() {
        return customRangeText.get();
    }

    /**
     * Stores the text shown when a custom date range has been selected by the user instead of
     * a preset range.
     *
     * @return the text to show when a custom range has been selected
     */
    public final StringProperty customRangeTextProperty() {
        return customRangeText;
    }

    public final void setCustomRangeText(String customRangeText) {
        this.customRangeText.set(customRangeText);
    }
}
