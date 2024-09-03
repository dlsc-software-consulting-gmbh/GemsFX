package com.dlsc.gemsfx.daterange;

import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.skins.DateRangeViewSkin;
import javafx.beans.property.*;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.*;
import javafx.css.converter.EnumConverter;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.*;

public class DateRangeView extends Control {

    private static final PseudoClass VERTICAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("vertical");

    private static final PseudoClass HORIZONTAL_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("horizontal");

    private CalendarView startCalendarView;

    private CalendarView endCalendarView;

    private final SelectionModel selectionModel;

    public DateRangeView() {
        getStyleClass().add("date-range-view");

        setFocusTraversable(false);

        selectionModel = new SelectionModel();
        selectionModel.setSelectionMode(SelectionModel.SelectionMode.DATE_RANGE);

        startCalendarView = getStartCalendarView();
        startCalendarView.setSelectionModel(selectionModel);

        endCalendarView = getEndCalendarView();
        endCalendarView.setSelectionModel(selectionModel);

        DateRangePreset todayRange = createTodayRangePreset();
        getPresets().addAll(todayRange, createYesterdayPreset(), createThisWeekPreset(), createThisMonthPreset(), createLastMonthPreset());
        setValue(todayRange.getDateRangeSupplier().get());
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DateRangeViewSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DateRangeView.class.getResource("date-range-view.css")).toExternalForm();
    }

    private ObjectProperty<Orientation> orientation;

    /**
     * Determines how the start and end calendars will be laid out, either next to each
     * other (horizontal), or one on top of the other (vertical).
     *
     * @return the layout orientation of the two calendar views
     */
    public final void setOrientation(Orientation value) {
        orientationProperty().set(value);
    }

    public final Orientation getOrientation() {
        return orientation == null ? Orientation.HORIZONTAL : orientation.get();
    }

    public final ObjectProperty<Orientation> orientationProperty() {
        if (orientation == null) {
            orientation = new StyleableObjectProperty<>(Orientation.HORIZONTAL) {
                @Override
                public void invalidated() {
                    final boolean isVertical = (get() == Orientation.VERTICAL);
                    pseudoClassStateChanged(VERTICAL_PSEUDOCLASS_STATE, isVertical);
                    pseudoClassStateChanged(HORIZONTAL_PSEUDOCLASS_STATE, !isVertical);
                }

                @Override
                public CssMetaData<DateRangeView, Orientation> getCssMetaData() {
                    return DateRangeView.StyleableProperties.ORIENTATION;
                }

                @Override
                public Object getBean() {
                    return DateRangeView.this;
                }

                @Override
                public String getName() {
                    return "orientation";
                }
            };
        }
        return orientation;
    }

    /**
     * Returns the selection model used and shared by the two calendar picker instances.
     *
     * @return the shared calendar selection model
     * @see #getStartCalendarView()
     * @see #getEndCalendarView()
     */
    public final SelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Returns the {@link CalendarView} that is being used for the start date
     * selection.
     *
     * @return the calendar view for the start date selection
     */
    public CalendarView getStartCalendarView() {
        if (startCalendarView == null) {
            startCalendarView = createCalendar();
            startCalendarView.setYearMonth(YearMonth.now());
            startCalendarView.getStyleClass().add("start-calendar");
        }
        return startCalendarView;
    }

    /**
     * Returns the {@link CalendarView} that is being used for the end date
     * selection.
     *
     * @return the calendar view for the end date selection
     */
    public CalendarView getEndCalendarView() {
        if (endCalendarView == null) {
            endCalendarView = createCalendar();
            endCalendarView.setYearMonth(YearMonth.now().plusMonths(1));
            endCalendarView.getStyleClass().add("end-calendar");
        }
        return endCalendarView;
    }

    private CalendarView createCalendar() {
        CalendarView calendar = new CalendarView();
        calendar.setShowDaysOfPreviousOrNextMonth(true);
        calendar.setShowToday(false);
        calendar.setMarkSelectedDaysOfPreviousOrNextMonth(false);
        return calendar;
    }

    private final StringProperty toText = new SimpleStringProperty(this, "toText", "TO");

    public final String getToText() {
        return toText.get();
    }

    /**
     * The text shown on the label between the two calendar views.
     *
     * @return the text for the "to" label
     */
    public final StringProperty toTextProperty() {
        return toText;
    }

    public final void setToText(String toText) {
        this.toText.set(toText);
    }

    private final StringProperty cancelText = new SimpleStringProperty(this, "cancelText", "CANCEL");

    public final String getCancelText() {
        return cancelText.get();
    }

    /**
     * The text used for the cancel button.
     *
     * @return the text for the cancel button
     */
    public final StringProperty cancelTextProperty() {
        return cancelText;
    }

    public final void setCancelText(String cancelText) {
        this.cancelText.set(cancelText);
    }

    private final StringProperty applyText = new SimpleStringProperty(this, "applyText", "APPLY");

    public final String getApplyText() {
        return applyText.get();
    }

    /**
     * The text used for the apply button.
     *
     * @return the text for the apply button
     */
    public final StringProperty applyTextProperty() {
        return applyText;
    }

    public final void setApplyText(String applyText) {
        this.applyText.set(applyText);
    }

    private final StringProperty presetTitle = new SimpleStringProperty(this, "presetsTitle", "QUICK SELECT");

    public String getPresetTitle() {
        return presetTitle.get();
    }

    public StringProperty presetTitleProperty() {
        return presetTitle;
    }

    public void setPresetTitle(String presetTitle) {
        this.presetTitle.set(presetTitle);
    }

    private final ObjectProperty<Side> presetsLocation = new SimpleObjectProperty<>(this, "presetsLocation", Side.LEFT) {
        @Override
        public void set(Side side) {
            if (!Objects.equals(side, Side.LEFT) && !Objects.equals(side, Side.RIGHT)) {
                throw new IllegalArgumentException("only sides LEFT and RIGHT are supported");
            }
            super.set(side);
        }
    };

    public final Side getPresetsLocation() {
        return presetsLocation.get();
    }

    public final ObjectProperty<Side> presetsLocationProperty() {
        return presetsLocation;
    }

    public final void setPresetsLocation(Side presetsLocation) {
        this.presetsLocation.set(presetsLocation);
    }

    private final BooleanProperty showPresets = new SimpleBooleanProperty(this, "showQuickSelect", true);

    public final boolean isShowPresets() {
        return showPresets.get();
    }

    public final BooleanProperty showPresetsProperty() {
        return showPresets;
    }

    public final void setShowPresets(boolean showPresets) {
        this.showPresets.set(showPresets);
    }

    private final ObjectProperty<Runnable> onClose = new SimpleObjectProperty<>(this, "onClose", () -> System.out.println("closing"));

    public final Runnable getOnClose() {
        return onClose.get();
    }

    public final ObjectProperty<Runnable> onCloseProperty() {
        return onClose;
    }

    public final void setOnClose(Runnable onClose) {
        this.onClose.set(onClose);
    }

    // date range

    private final ObjectProperty<DateRange> value = new SimpleObjectProperty<>(this, "value");

    public final DateRange getValue() {
        return value.get();
    }

    public final ObjectProperty<DateRange> valueProperty() {
        return value;
    }

    public final void setValue(DateRange value) {
        this.value.set(value);
    }

    private final BooleanProperty showCancelAndApplyButton = new SimpleBooleanProperty(this, "showCancelAndApplyButton", true);

    public final boolean isShowCancelAndApplyButton() {
        return showCancelAndApplyButton.get();
    }

    /**
     * Shows or hides the cancel and the apply buttons.
     *
     * @return true if the buttons will be shown
     */
    public final BooleanProperty showCancelAndApplyButtonProperty() {
        return showCancelAndApplyButton;
    }

    public final void setShowCancelAndApplyButton(boolean showCancelAndApplyButton) {
        this.showCancelAndApplyButton.set(showCancelAndApplyButton);
    }

    // presets

    private final ObservableList<DateRangePreset> presets = FXCollections.observableArrayList();

    public final ObservableList<DateRangePreset> getPresets() {
        return presets;
    }

    private DateRangePreset createTodayRangePreset() {
        return new DateRangePreset("Today", () -> new DateRange("Today", LocalDate.now()));
    }

    private DateRangePreset createYesterdayPreset() {
        return new DateRangePreset("Yesterday", () -> new DateRange("Yesterday", LocalDate.now().minusDays(1)));
    }

    private DateRangePreset createThisWeekPreset() {
        return new DateRangePreset("This Week", () -> {
            TemporalField fieldISO = WeekFields.of(Locale.getDefault()).dayOfWeek();
            return new DateRange("This Week", LocalDate.now().with(fieldISO, 1), LocalDate.now().with(fieldISO, 1).plusDays(6));
        });
    }

    private DateRangePreset createThisMonthPreset() {
        return new DateRangePreset("This Month", () -> {
            LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
            LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange("This Month", start, end);
        });
    }

    private DateRangePreset createLastMonthPreset() {
        return new DateRangePreset("Last Month", () -> {
            LocalDate start = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).minusMonths(1);
            LocalDate end = start.with(TemporalAdjusters.lastDayOfMonth());
            return new DateRange("Last Month", start, end);
        });
    }

    private static class StyleableProperties {
        private static final CssMetaData<DateRangeView, Orientation> ORIENTATION =
                new CssMetaData<>("-fx-orientation",
                        new EnumConverter<>(Orientation.class),
                        Orientation.HORIZONTAL) {

                    @Override
                    public Orientation getInitialValue(DateRangeView node) {
                        return node.getOrientation();
                    }

                    @Override
                    public boolean isSettable(DateRangeView n) {
                        return n.orientation == null || !n.orientation.isBound();
                    }

                    @Override
                    public StyleableProperty<Orientation> getStyleableProperty(DateRangeView n) {
                        return (StyleableProperty<Orientation>) (WritableValue<Orientation>) n.orientationProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables =
                    new ArrayList<CssMetaData<? extends Styleable, ?>>(Control.getClassCssMetaData());
            styleables.add(ORIENTATION);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Gets the {@code CssMetaData} associated with this class, which may include the
     * {@code CssMetaData} of its superclasses.
     *
     * @return the {@code CssMetaData}
     * @since JavaFX 8.0
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return DateRangeView.StyleableProperties.STYLEABLES;
    }

    /**
     * {@inheritDoc}
     *
     * @since JavaFX 8.0
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

}
