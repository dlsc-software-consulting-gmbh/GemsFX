package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.DurationPickerSkin;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.css.converter.EnumConverter;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Skin;
import javafx.scene.layout.Region;
import javafx.util.Callback;
import javafx.util.Pair;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A picker control for selecting a {@link java.time.Duration} value. The picker consists of
 * individual time-unit fields (e.g. hours, minutes, seconds) that can be edited independently
 * or linked together.
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-fill-digits}</td><td>{@code boolean}</td><td>Whether to fill fields with leading zeros</td></tr>
 *     <tr><td>{@code -fx-label-type}</td><td>{@code LabelType}</td><td>Type of labels shown for each time unit</td></tr>
 *     <tr><td>{@code -fx-linking-fields}</td><td>{@code boolean}</td><td>Whether linking fields is enabled</td></tr>
 *     <tr><td>{@code -fx-rollover}</td><td>{@code boolean}</td><td>Whether fields roll over at their limits</td></tr>
 *     <tr><td>{@code -fx-show-popup-trigger-button}</td><td>{@code boolean}</td><td>Whether to show the popup trigger button</td></tr>
 *   </tbody>
 * </table>
 */
public class DurationPicker extends CustomComboBox<Duration> {

    public DurationPicker() {
        getStyleClass().setAll("duration-picker", "text-input");

        setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        minimumDurationProperty().addListener(it -> {
            Duration minimumDuration = getMinimumDuration();
            if (minimumDuration == null) {
                throw new IllegalArgumentException("the minimum duration can not be null, it always has to be at least Duration.ZERO");
            }
            if (minimumDuration.isNegative()) {
                throw new IllegalArgumentException("the minimum duration can not be negative, but was " + minimumDuration);
            }
        });

        InvalidationListener constrainListener = it -> constrain();
        durationProperty().addListener(constrainListener);
        minimumDurationProperty().addListener(constrainListener);
        maximumDurationProperty().addListener(constrainListener);

        setSeparatorFactory(pair -> {
            Label label = new Label(":");
            label.getStyleClass().add("separator");
            return label;
        });

        setOnShowPopup(picker -> show());

        setMaximumDuration(Duration.ofDays(7));

        getFields().setAll(ChronoUnit.DAYS, ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS, ChronoUnit.MILLIS);

        MapChangeListener<? super Object, ? super Object> propertiesListener = change -> {
            if (change.wasAdded()) {
                if (change.getKey().equals("NEW_DURATION")) {
                    setDuration((Duration) change.getValueAdded());
                    getProperties().remove("NEW_DURATION");
                }
            }
        };

        getProperties().addListener(propertiesListener);
    }

    private void constrain() {
        Duration duration = getDuration();
        if (duration != null) {
            Duration minimumDuration = getMinimumDuration();
            if (minimumDuration != null) {
                if (duration.minus(minimumDuration).isNegative()) {
                    setDuration(minimumDuration);
                }
            }

            Duration maximumDuration = getMaximumDuration();
            if (maximumDuration != null) {
                if (maximumDuration.minus(duration).isNegative()) {
                    setDuration(maximumDuration);
                }
            }
        }
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DurationPicker.class.getResource("duration-picker.css")).toExternalForm();
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new DurationPickerSkin(this);
    }

    private final ObjectProperty<Callback<Pair<ChronoUnit, ChronoUnit>, Node>> separatorFactory = new SimpleObjectProperty<>(this, "separatorFactory");

    public final Callback<Pair<ChronoUnit, ChronoUnit>, Node> getSeparatorFactory() {
        return separatorFactory.get();
    }

    /**
     * The separator factory is used to create nodes that will be placed between two fields
     * of the picker. E.g. to separate hours one would return a label with a colon in it (8 hours
     * 35 minutes and 40 seconds would then look like this -> "8:35:40").
     *
     * @return the separator factory
     */
    public ObjectProperty<Callback<Pair<ChronoUnit, ChronoUnit>, Node>> separatorFactoryProperty() {
        return separatorFactory;
    }

    public void setSeparatorFactory(Callback<Pair<ChronoUnit, ChronoUnit>, Node> separatorFactory) {
        this.separatorFactory.set(separatorFactory);
    }

    // duration

    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration", Duration.ZERO);

    public final Duration getDuration() {
        return duration.get();
    }

    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    public final void setDuration(Duration duration) {
        this.duration.set(duration);
    }

    // fields support

    private final ListProperty<ChronoUnit> fields = new SimpleListProperty<>(this, "fields", FXCollections.observableArrayList(ChronoUnit.HOURS, ChronoUnit.MINUTES, ChronoUnit.SECONDS));

    public final ObservableList<ChronoUnit> getFields() {
        return fields.get();
    }

    /**
     * The list of fields that will be displayed inside the control. Supported units are:
     * weeks, days, hours, minutes, seconds, millis.
     */
    public final ListProperty<ChronoUnit> fieldsProperty() {
        return fields;
    }

    public final void setFields(ObservableList<ChronoUnit> fields) {
        this.fields.set(fields);
    }

    // linking fields

    private final BooleanProperty linkingFields = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return DurationPicker.this;
        }

        @Override
        public String getName() {
            return "linkingFields";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.LINKING_FIELDS;
        }
    };

    public final boolean isLinkingFields() {
        return linkingFields.get();
    }

    /**
     * A property used to control whether the fields should automatically increase or decrease
     * the previous field when they reach their upper or lower limit.
     * <p>
     * Can be set via CSS using the {@code -fx-linking-fields} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if linking fields is enabled
     */
    public final BooleanProperty linkingFieldsProperty() {
        return linkingFields;
    }

    public final void setLinkingFields(boolean linkingFields) {
        this.linkingFields.set(linkingFields);
    }

    // rollover

    private final BooleanProperty rollover = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return DurationPicker.this;
        }

        @Override
        public String getName() {
            return "rollover";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.ROLLOVER;
        }
    };

    public final boolean isRollover() {
        return rollover.get();
    }

    /**
     * A flag used to signal whether the duration fields should start at the beginning of their
     * value range when they reach the end of it.
     * <p>
     * Can be set via CSS using the {@code -fx-rollover} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if the fields should rollover
     */
    public final BooleanProperty rolloverProperty() {
        return rollover;
    }

    public final void setRollover(boolean rollover) {
        this.rollover.set(rollover);
    }

    // popup

    private final ObjectProperty<Consumer<DurationPicker>> onShowPopup = new SimpleObjectProperty<>(this, "onShowPopup");

    public final Consumer<DurationPicker> getOnShowPopup() {
        return onShowPopup.get();
    }

    /**
     * This consumer will be invoked to bring up a control for entering the
     * time without using the keyboard. The default implementation shows a popup.
     *
     * @return the "on show popup" consumer
     */
    public final ObjectProperty<Consumer<DurationPicker>> onShowPopupProperty() {
        return onShowPopup;
    }

    public final void setOnShowPopup(Consumer<DurationPicker> onShowPopup) {
        this.onShowPopup.set(onShowPopup);
    }

    // popup trigger button

    private final BooleanProperty showPopupTriggerButton = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return DurationPicker.this;
        }

        @Override
        public String getName() {
            return "showPopupTriggerButton";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.SHOW_POPUP_TRIGGER_BUTTON;
        }
    };

    public final boolean isShowPopupTriggerButton() {
        return showPopupTriggerButton.get();
    }

    /**
     * Determines if the control will show a button for showing or hiding the popup.
     * <p>
     * Can be set via CSS using the {@code -fx-show-popup-trigger-button} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if the control will show a button for showing the popup
     */
    public final BooleanProperty showPopupTriggerButtonProperty() {
        return showPopupTriggerButton;
    }

    public final void setShowPopupTriggerButton(boolean showPopupTriggerButton) {
        this.showPopupTriggerButton.set(showPopupTriggerButton);
    }

    // minimum duration

    private final ObjectProperty<Duration> minimumDuration = new SimpleObjectProperty<>(this, "earliestTime", Duration.ZERO);

    public final Duration getMinimumDuration() {
        return minimumDuration.get();
    }

    /**
     * Stores the minimum duration that the picker can display. The minimum duration can not
     * be negative.
     *
     * @return the minimum duration
     */
    public final ObjectProperty<Duration> minimumDurationProperty() {
        return minimumDuration;
    }

    public final void setMinimumDuration(Duration minimumDuration) {
        this.minimumDuration.set(minimumDuration);
    }

    // maximum duration

    private final ObjectProperty<Duration> maximumDuration = new SimpleObjectProperty<>(this, "maximumDuration");

    public final Duration getMaximumDuration() {
        return maximumDuration.get();
    }

    /**
     * Stores the maximum duration that the picker can display.
     *
     * @return the maximum duration
     */
    public final ObjectProperty<Duration> maximumDurationProperty() {
        return maximumDuration;
    }

    public final void setMaximumDuration(Duration maximumDuration) {
        this.maximumDuration.set(maximumDuration);
    }

    /**
     * The possible types of labels used by the duration picker, see
     * {@link #setLabelType(LabelType)}.
     */
    public enum LabelType {

        /**
         * Do not display any label for the values.
         */
        NONE,

        /**
         * Display short labels for each field, e.g. "d" for "days",
         * "h" for "hours", etc...
         */
        SHORT,

        /**
         * Display the full name of a field, e.g. "days", "hours", ...
         */
        LONG
    }

    private final ObjectProperty<LabelType> labelType = new StyleableObjectProperty<>(LabelType.SHORT) {
        @Override
        public Object getBean() {
            return DurationPicker.this;
        }

        @Override
        public String getName() {
            return "labelType";
        }

        @Override
        public CssMetaData<? extends Styleable, LabelType> getCssMetaData() {
            return StyleableProperties.LABEL_TYPE;
        }
    };

    public final LabelType getLabelType() {
        return labelType.get();
    }

    /**
     * The label type determines if the control will show no labels, short labels (e.g. "d") or
     * long labels (e.g. "days").
     * <p>
     * Can be set via CSS using the {@code -fx-label-type} property.
     * Valid values are: {@code NONE}, {@code SHORT}, {@code LONG}.
     * The default value is {@code SHORT}.
     * </p>
     *
     * @return the type of labels shown for each unit
     */
    public final ObjectProperty<LabelType> labelTypeProperty() {
        return labelType;
    }

    public final void setLabelType(LabelType labelType) {
        this.labelType.set(labelType);
    }

    // fill digits

    private final BooleanProperty fillDigits = new StyleableBooleanProperty(true) {
        @Override
        public Object getBean() {
            return DurationPicker.this;
        }

        @Override
        public String getName() {
            return "fillDigits";
        }

        @Override
        public CssMetaData<? extends Styleable, Boolean> getCssMetaData() {
            return StyleableProperties.FILL_DIGITS;
        }
    };

    public final boolean isFillDigits() {
        return fillDigits.get();
    }

    /**
     * Determines if the fields will be "filled" with leading zeros or not, example: "04" for
     * 4 hours, or "0005" for 5 milliseconds. This only applies to fields with a granularity
     * of HOURS or lower. It does not make sense to fill DAYS with it as there is no limit on
     * the number of days (no upper bound).
     * <p>
     * Can be set via CSS using the {@code -fx-fill-digits} property.
     * Valid values are: {@code true} or {@code false}.
     * The default value is {@code true}.
     * </p>
     *
     * @return true if the fields will be filled with leading zeros
     */
    public final BooleanProperty fillDigitsProperty() {
        return fillDigits;
    }

    public final void setFillDigits(boolean fillDigits) {
        this.fillDigits.set(fillDigits);
    }

    private static class StyleableProperties {

        private static final CssMetaData<DurationPicker, Boolean> SHOW_POPUP_TRIGGER_BUTTON =
                new CssMetaData<>("-fx-show-popup-trigger-button", BooleanConverter.getInstance(), true) {
                    @Override
                    public boolean isSettable(DurationPicker control) {
                        return !control.showPopupTriggerButton.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(DurationPicker control) {
                        return (StyleableProperty<Boolean>) control.showPopupTriggerButtonProperty();
                    }
                };

        private static final CssMetaData<DurationPicker, Boolean> LINKING_FIELDS =
                new CssMetaData<>("-fx-linking-fields", BooleanConverter.getInstance(), true) {
                    @Override
                    public boolean isSettable(DurationPicker control) {
                        return !control.linkingFields.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(DurationPicker control) {
                        return (StyleableProperty<Boolean>) control.linkingFieldsProperty();
                    }
                };

        private static final CssMetaData<DurationPicker, Boolean> ROLLOVER =
                new CssMetaData<>("-fx-rollover", BooleanConverter.getInstance(), true) {
                    @Override
                    public boolean isSettable(DurationPicker control) {
                        return !control.rollover.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(DurationPicker control) {
                        return (StyleableProperty<Boolean>) control.rolloverProperty();
                    }
                };

        private static final CssMetaData<DurationPicker, LabelType> LABEL_TYPE =
                new CssMetaData<>("-fx-label-type", new EnumConverter<>(LabelType.class), LabelType.SHORT) {
                    @Override
                    public boolean isSettable(DurationPicker control) {
                        return !control.labelType.isBound();
                    }

                    @Override
                    public StyleableProperty<LabelType> getStyleableProperty(DurationPicker control) {
                        return (StyleableProperty<LabelType>) control.labelTypeProperty();
                    }
                };

        private static final CssMetaData<DurationPicker, Boolean> FILL_DIGITS =
                new CssMetaData<>("-fx-fill-digits", BooleanConverter.getInstance(), true) {
                    @Override
                    public boolean isSettable(DurationPicker control) {
                        return !control.fillDigits.isBound();
                    }

                    @Override
                    public StyleableProperty<Boolean> getStyleableProperty(DurationPicker control) {
                        return (StyleableProperty<Boolean>) control.fillDigitsProperty();
                    }
                };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(CustomComboBox.getClassCssMetaData());
            Collections.addAll(styleables, SHOW_POPUP_TRIGGER_BUTTON, LINKING_FIELDS, ROLLOVER, LABEL_TYPE, FILL_DIGITS);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}
