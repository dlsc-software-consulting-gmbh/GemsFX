package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import com.dlsc.gemsfx.DurationPicker.LabelType;
import com.dlsc.gemsfx.TimePicker;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import javafx.beans.binding.Bindings;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A control used for visualizing digits as part of the {@link TimePicker} control.
 */
public class DurationUnitField extends Label {

    private final DurationPicker picker;
    private final ChronoUnit chronoUnit;

    private String str = "";

    private DurationUnitField nextField;

    private DurationUnitField previousField;

    public DurationUnitField(DurationPicker picker, ChronoUnit chronoUnit) {
        super();

        this.picker = Objects.requireNonNull(picker);
        this.chronoUnit = chronoUnit;

        updateStyles();
        labelTypeProperty().addListener(it -> updateStyles());

        getStyleClass().addAll("unit-field", chronoUnit.name().toLowerCase());

        setAlignment(Pos.CENTER);

        setOnMouseClicked(evt -> requestFocus());

        /*
         * Make sure the text is filled with a leading zero when the value is smaller than
         * 10 and the "fillDigits" was true.
         */
        textProperty().bind(Bindings.createStringBinding(() -> {
            Long value = getValue();
            String result;

            if (value == null) {
                result = "";
            } else if (picker.isFillDigits()) {
                result = fill(Long.toString(value), chronoUnit);
            } else {
                result = Long.toString(value);
            }

            if (!getLabelType().equals(LabelType.NONE)) {

                boolean shortLabels = getLabelType().equals(LabelType.SHORT);

                // TODO: i18n
                switch (chronoUnit) {
                    case DAYS:
                        result += shortLabels ? "d" : " days";
                        break;
                    case HOURS:
                        result += shortLabels ? "h" : " hours";
                        break;
                    case MINUTES:
                        result += shortLabels ? "m" : " minutes";
                        break;
                    case SECONDS:
                        result += shortLabels ? "s" : " seconds";
                        break;
                    case MILLIS:
                        result += shortLabels ? "ms" : " millis";
                        break;
                }
            }

            return result;

        }, valueProperty(), labelTypeProperty()));

        focusedProperty().addListener(it -> {
            if (!isFocused()) {
                constrainValue();
            }
        });

        minimumValueProperty().addListener(it -> constrainValue());
        maximumValueProperty().addListener(it -> constrainValue());

        setFocusTraversable(true);
        setAlignment(Pos.CENTER);

        valueProperty().addListener(it -> str = Long.toString(getValue()));

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                // we regained focus, so nothing is in the "history"
                str = "";
            }
        });

        addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().isArrowKey()) {
                handleArrowKey(evt);
            } else {
                boolean handled = false;

                if (evt.getCode().isDigitKey()) {
                    handleDigit(evt);
                    handled = true;
                } else if (evt.getCode().equals(KeyCode.BACK_SPACE) && str.length() > 0) {
                    handleBackspace();
                    handled = true;
                } else if (evt.getCode().equals(KeyCode.SPACE) && nextField != null) {
                    nextField.requestFocus();
                }

                if (handled) {

                    if (str.length() == 0) {
                        setValue(getMinimumValue());
                    } else {
                        setValue(Long.parseLong(str));
                    }

                    int jumpLength = 2;

                    if (chronoUnit.equals(ChronoUnit.DAYS)) {
                        jumpLength = -1; // can't jump for days
                    } else if (chronoUnit.equals(ChronoUnit.MILLIS)) {
                        jumpLength = 4;
                    }

                    if (jumpLength != -1 && str.length() == jumpLength && nextField != null) {
                        nextField.requestFocus();
                    }
                }
            }
        });

        duration.addListener(it -> {
            Duration duration = this.duration.get();
            if (duration == null) {
                setValue(null);
                return;
            }

            switch (chronoUnit) {
                case DAYS:
                    setValue(duration.toDaysPart());
                    break;
                case HOURS:
                    setValue((long) duration.toHoursPart());
                    break;
                case MINUTES:
                    setValue((long) duration.toMinutesPart());
                    break;
                case SECONDS:
                    setValue((long) duration.toSecondsPart());
                    break;
                case MILLIS:
                    setValue((long) duration.toMillisPart());
                    break;
            }
        });
    }

    private void updateStyles() {
        getStyleClass().setAll("label", "unit-field");
        switch (getLabelType()) {
            case NONE:
                getStyleClass().add("no-label");
                break;
            case SHORT:
                getStyleClass().add("short-label");
                break;
            case LONG:
                getStyleClass().add("long-label");
                break;
        }

        getStyleClass().add(getChronoUnit().name().toLowerCase());
    }

    public final ChronoUnit getChronoUnit() {
        return chronoUnit;
    }

    private String fill(String str, ChronoUnit chronoUnit) {
        if (value == null) {
            return "";
        }

        switch (chronoUnit) {
            default:
                return str;
            case HOURS:
            case MINUTES:
            case SECONDS:
                if (str.length() < 2) {
                    return "0" + str;
                }
                break;
            case MILLIS:
                if (str.length() < 2) {
                    return "00" + str;
                }
                if (str.length() < 3) {
                    return "0" + str;
                }
                break;
        }

        return str;
    }

    private void handleBackspace() {
        str = str.substring(0, str.length() - 1);
    }

    private void handleDigit(KeyEvent evt) {
        int maxLength;

        switch (chronoUnit) {
            default:
            case DAYS:
                maxLength = -1;
                break;
            case HOURS:
            case MINUTES:
            case SECONDS:
                maxLength = 2;
                break;
            case MILLIS:
                maxLength = 3;
                break;
        }

        System.out.println(maxLength);
        if (maxLength != -1 && str.length() == maxLength) {
            str = "";
        }

        str = str + evt.getCode().getChar();
    }

    private void handleArrowKey(KeyEvent evt) {
        if (evt.getCode().equals(KeyCode.DOWN)) {
            decrement();
            evt.consume();
            picker.getProperties().put("ADJUST_TIME", "ADJUST_TIME");
        } else if (evt.getCode().equals(KeyCode.UP)) {
            increment();
            evt.consume();
            picker.getProperties().put("ADJUST_TIME", "ADJUST_TIME");
        }
    }

    void decrement() {
        Long value = getValue();
        if (value != null) {
            long newValue = value - 1;
            if (newValue < getMinimumValue()) {
                // check for max value because days, for example, can't rollover without a max value
                if (picker.isRollover() && getMaximumValue() != Long.MAX_VALUE) {
                    setValue(getMaximumValue());
                    if (picker.isLinkingFields() && previousField != null) {
                        previousField.decrement();
                    }
                } else {
                    setValue(getMinimumValue());
                }
            } else {
                setValue(newValue);
            }
        } else {
            setValue(0L);
        }
    }

    void increment() {
        Long value = getValue();
        if (value != null) {
            long newValue = value + 1;
            if (newValue > getMaximumValue()) {
                if (picker.isRollover()) {
                    setValue(getMinimumValue());
                    if (picker.isLinkingFields() && previousField != null) {
                        previousField.increment();
                    }
                } else {
                    setValue(getMaximumValue());
                }
            } else {
                setValue(newValue);
            }
        } else {
            setValue(0L);
        }
    }

    /*
     * Sets the next field to jump to when the user presses the space or tab keys.
     *
     * @param field the next
     */
    final void setNextField(DurationUnitField field) {
        this.nextField = field;
    }

    /*
     * Sets the previous field to increase when rolling over.
     *
     * @param field the next
     */
    final void setPreviousField(DurationUnitField field) {
        this.previousField = field;
    }

    private void constrainValue() {
        Long value = getValue();
        if (value != null) {
            if (value < getMinimumValue()) {
                setValue(getMinimumValue());
                return;
            }

            if (value > getMaximumValue()) {
                setValue(getMaximumValue());
                return;
            }
        }
    }

    /**
     * The current value of the field.
     *
     * @return the current value.
     */
    public final ObjectProperty<Long> valueProperty() {
        return value;
    }

    private final ObjectProperty<Long> value = new SimpleObjectProperty<>(this, "value");

    public final Long getValue() {
        return valueProperty().get();
    }

    public final void setValue(Long value) {
        valueProperty().set(value);
    }

    /**
     * The minimum value that can be entered in this field.
     *
     * @return the minimum value.
     */
    public final LongProperty minimumValueProperty() {
        return minimumValue;
    }

    private final LongProperty minimumValue = new SimpleLongProperty(this, "minimumValue", 0);

    public final Long getMinimumValue() {
        return minimumValueProperty().get();
    }

    public final void setMinimumValue(Long minimumValue) {
        minimumValueProperty().set(minimumValue);
    }


    /**
     * The maximum value that can be entered in this field.
     *
     * @return the maximum value.
     */
    public final LongProperty maximumValueProperty() {
        return maximumValue;
    }

    private final LongProperty maximumValue = new SimpleLongProperty(this, "maximumValue", Long.MAX_VALUE);

    public final Long getMaximumValue() {
        return maximumValueProperty().get();
    }

    public final void setMaximumValue(Long maximumValue) {
        maximumValueProperty().set(maximumValue);
    }

    // duration

    private final ObjectProperty<Duration> duration = new SimpleObjectProperty<>(this, "duration");

    public final ObjectProperty<Duration> durationProperty() {
        return duration;
    }

    private final ObjectProperty<LabelType> labelType = new SimpleObjectProperty<>(this, "labelType", LabelType.SHORT);

    public final LabelType getLabelType() {
        return labelType.get();
    }

    public final ObjectProperty<LabelType> labelTypeProperty() {
        return labelType;
    }

    public final void setLabelType(LabelType labelType) {
        this.labelType.set(labelType);
    }

}
