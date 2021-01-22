package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import com.dlsc.gemsfx.DurationPicker.LabelType;
import com.dlsc.gemsfx.TimePicker;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * A control used for visualizing digits as part of the {@link TimePicker} control.
 */
public class DurationUnitField extends Label {
    private final ResourceBundle i18n = ResourceBundle.getBundle("duration-picker");

    private final DurationPicker picker;
    private final ChronoUnit chronoUnit;

    private String typedText = "";

    private DurationUnitField nextField;

    private DurationUnitField previousField;

    public DurationUnitField(DurationPicker picker, ChronoUnit chronoUnit) {
        super();

        this.picker = Objects.requireNonNull(picker);
        this.chronoUnit = chronoUnit;

        setMinWidth(Region.USE_PREF_SIZE);

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
                result = isFillDigits() ? fill("", chronoUnit, "-") : "-";
                result += " "; // add extra space to separate from unit, e.g. ("-- hours")
            } else if (isFillDigits()) {
                result = fill(Long.toString(value), chronoUnit, "0");
            } else {
                result = Long.toString(value);
            }

            if (!getLabelType().equals(LabelType.NONE)) {

                boolean shortLabels = getLabelType().equals(LabelType.SHORT);

                switch (chronoUnit) {
                    case DAYS:
                        result += shortLabels ? i18n.getString("unit.short.days") : " " + i18n.getString("unit.long.days");
                        break;
                    case HOURS:
                        result += shortLabels ? i18n.getString("unit.short.hours") : " " + i18n.getString("unit.long.hours");
                        break;
                    case MINUTES:
                        result += shortLabels ? i18n.getString("unit.short.minutes") : " " + i18n.getString("unit.long.minutes");
                        break;
                    case SECONDS:
                        result += shortLabels ? i18n.getString("unit.short.seconds") : " " + i18n.getString("unit.long.seconds");
                        break;
                    case MILLIS:
                        result += shortLabels ? i18n.getString("unit.short.millis") : " " + i18n.getString("unit.long.millis");
                        break;
                }
            }

            return result;

        }, valueProperty(), labelTypeProperty(), fillDigitsProperty()));

        focusedProperty().addListener(it -> {
            if (!isFocused()) {
                constrainValue();
            }
        });

        maximumValueProperty().addListener(it -> constrainValue());

        setFocusTraversable(true);
        setAlignment(Pos.CENTER);

        valueProperty().addListener(it -> {
            Long value = getValue();
            if (value != null) {
                typedText = Long.toString(value);
            } else {
                typedText = "";
            }
        });

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                // we regained focus, so nothing is in the "history"
                typedText = "";
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
                } else if (evt.getCode().equals(KeyCode.BACK_SPACE) && typedText.length() > 0) {
                    handleBackspace();
                    handled = true;
                } else if (evt.getCode().equals(KeyCode.SPACE) && nextField != null) {
                    nextField.requestFocus();
                }

                if (handled) {

                    if (typedText.length() == 0) {
                        setValue(0L);
                    } else {
                        setValue(Math.min(getMaximumValue(), Long.parseLong(typedText)));
                    }

                    int jumpLength = 2;

                    if (chronoUnit.equals(ChronoUnit.DAYS)) {
                        jumpLength = -1; // can't jump for days
                    } else if (chronoUnit.equals(ChronoUnit.MILLIS)) {
                        jumpLength = 4;
                    }

                    if (jumpLength != -1 && typedText.length() == jumpLength && nextField != null) {
                        nextField.requestFocus();
                    }
                }
            }
        });

        duration.addListener(it -> {
            updating = true;
            try {
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
            } finally {
                updating = false;
            }
        });
    }

    private boolean updating;

    public boolean isUpdating() {
        return updating;
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

    private String fill(String str, ChronoUnit chronoUnit, String fillCharacter) {
        int length = str.length();

        switch (chronoUnit) {
            default:
                if (length == 0) {
                    return fillCharacter;
                }
                return str;
            case HOURS:
            case MINUTES:
            case SECONDS:
                switch (length) {
                    case 0:
                        return fillCharacter + fillCharacter;
                    case 1:
                        return fillCharacter + str;
                    default:
                        return str;
                }
            case MILLIS:
                switch (length) {
                    case 0:
                        return fillCharacter + fillCharacter + fillCharacter;
                    case 1:
                        return fillCharacter + fillCharacter + str;
                    case 2:
                        return fillCharacter + str;
                    default:
                        return str;
                }
        }
    }

    private void handleBackspace() {
        typedText = typedText.substring(0, typedText.length() - 1);
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

        if (maxLength != -1 && typedText.length() == maxLength) {
            typedText = "";
        }

        switch (evt.getText()) {
            case "0":
            case "1":
            case "2":
            case "3":
            case "4":
            case "5":
            case "6":
            case "7":
            case "8":
            case "9":
                typedText = typedText + evt.getText();
                break;
            default:
                break;

        }
    }

    private void handleArrowKey(KeyEvent evt) {
        if (evt.getCode().equals(KeyCode.DOWN)) {
            decrement();
            picker.getProperties().put("ADJUST_TIME", "ADJUST_TIME");
        } else if (evt.getCode().equals(KeyCode.UP)) {
            increment();
            picker.getProperties().put("ADJUST_TIME", "ADJUST_TIME");
        } else if (evt.getCode().equals(KeyCode.RIGHT)) {
            if (nextField != null) {
                nextField.requestFocus();
            }
        } else if (evt.getCode().equals(KeyCode.LEFT)) {
            if (previousField != null) {
                previousField.requestFocus();
            }
        }
        evt.consume();
    }

    void decrement() {
        Long value = getValue();
        if (value != null) {
            long newValue = value - 1;
            if (newValue < 0L) {
                // check for max value because days, for example, can't rollover without a max value
                if (picker.isRollover() && getMaximumValue() != Long.MAX_VALUE) {
                    setValue(getMaximumValue());
                    if (picker.isLinkingFields() && previousField != null) {
                        previousField.decrement();
                    }
                } else {
                    setValue(0L);
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
                    setValue(0L);
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
        nextField = field;
    }

    /*
     * Sets the previous field to increase when rolling over.
     *
     * @param field the next
     */
    final void setPreviousField(DurationUnitField field) {
        previousField = field;
    }

    private void constrainValue() {
        Long value = getValue();
        if (value != null) {
            if (value < 0L) {
                setValue(0L);
            } else if (value > getMaximumValue()) {
                setValue(getMaximumValue());
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
     * The maximum value that can be entered in this field.
     *
     * @return the maximum value.
     */
    public final LongProperty maximumValueProperty() {
        return maximumValue;
    }

    private final LongProperty maximumValue = new SimpleLongProperty(this, "maximumValue", Long.MAX_VALUE);

    final Long getMaximumValue() {
        return maximumValueProperty().get();
    }

    final void setMaximumValue(Long maximumValue) {
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

    private final BooleanProperty fillDigits = new SimpleBooleanProperty(this, "fillDigits", true);

    public final boolean isFillDigits() {
        return fillDigits.get();
    }

    public final BooleanProperty fillDigitsProperty() {
        return fillDigits;
    }
}
