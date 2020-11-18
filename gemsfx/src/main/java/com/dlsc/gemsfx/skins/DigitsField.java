package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;

import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * A control used for visualizing digits as part of the {@link TimePicker} control.
 */
public abstract class DigitsField extends TimeField {

    private String typedText = "";

    private TimeField nextField;

    private TimeField previousField;

    public DigitsField(TimePicker timePicker, boolean fillDigits) {
        super(timePicker);

        getStyleClass().add("digits-field");

        setAlignment(Pos.CENTER);

        setOnMouseClicked(evt -> requestFocus());

        /*
         * Make sure the text is filled with a leading zero when the value is smaller than
         * 10 and the "fillDigits" was true.
         */
        textProperty().bind(Bindings.createStringBinding(() -> {
            Integer value = getValue();
            if (value == null) {
                return "--";
            } else if (value < 10 && fillDigits) {
                return "0" + value;
            }

            return Integer.toString(value);
        }, valueProperty()));

        focusedProperty().addListener(it -> {
            if (!isFocused()) {
                constrainValue();
            }
        });

        minimumValueProperty().addListener(it -> constrainValue());
        maximumValueProperty().addListener(it -> constrainValue());

        setFocusTraversable(true);
        setAlignment(Pos.CENTER);

        valueProperty().addListener(it -> {
            Integer value = getValue();
            if (value == null) {
                typedText = "";
            } else {
                typedText = Integer.toString(value);
            }
        });

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                // we regained focus, so nothing is in the "history"
                typedText = "";
            }
        });

        timePicker.adjustedProperty().addListener(it -> {
            if (timePicker.isAdjusted()) {
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
                        setValue(getMinimumValue());
                    } else {
                        setValue(Integer.parseInt(typedText));
                    }

                    if (typedText.length() == 2 && nextField != null) {
                        nextField.requestFocus();
                    }
                }
            }
        });
    }

    private void handleBackspace() {
        typedText = typedText.substring(0, typedText.length() - 1);
    }

    private void handleDigit(KeyEvent evt) {
        if (typedText.length() == 2) {
            typedText = "";
        }

        typedText = typedText + evt.getCode().getChar();
    }

    private void handleArrowKey(KeyEvent evt) {
        if (evt.getCode().equals(KeyCode.DOWN)) {
            decrement();
            getTimePicker().getProperties().put("ADJUST_TIME", "ADJUST_TIME");
        } else if (evt.getCode().equals(KeyCode.UP)) {
            increment();
            getTimePicker().getProperties().put("ADJUST_TIME", "ADJUST_TIME");
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

    @Override
    void decrement() {
        Integer value = getValue();
        if (value != null) {
            int newValue = value - getStepRate();
            if (newValue < getMinimumValue()) {
                TimePicker timePicker = getTimePicker();
                if (timePicker.isRollover()) {
                    setValue(getMaximumValue() - getMaximumValue() % getStepRate());
                    if (timePicker.isLinkingFields() && previousField != null) {
                        previousField.decrement();
                    }
                } else {
                    setValue(getMinimumValue());
                }
            } else {
                setValue(newValue);
            }
        } else {
            setValue(0);
        }
    }

    @Override
    void increment() {
        Integer value = getValue();
        if (value != null) {
            int newValue = value + getStepRate();
            if (newValue > getMaximumValue()) {
                TimePicker timePicker = getTimePicker();
                if (timePicker.isRollover()) {
                    setValue(getMinimumValue());
                    if (timePicker.isLinkingFields() && previousField != null) {
                        previousField.increment();
                    }
                } else {
                    setValue(getMaximumValue());
                }
            } else {
                setValue(newValue);
            }
        } else {
            setValue(0);
        }
    }

    /*
     * Sets the next field to jump to when the user presses the space or tab keys.
     *
     * @param field the next
     */
    final void setNextField(TimeField field) {
        this.nextField = field;
    }

    /*
     * Sets the previous field to increase when rolling over.
     *
     * @param field the next
     */
    final void setPreviousField(TimeField field) {
        this.previousField = field;
    }

    private final IntegerProperty stepRate = new SimpleIntegerProperty(this, "stepRate", 1);

    public final int getStepRate() {
        return stepRate.get();
    }

    /**
     * The step rate of the field. The field's value increases or decreases by this amount
     * when the user hits the arrow up or down keys.
     *
     * @return the field's step rate
     */
    public final IntegerProperty stepRateProperty() {
        return stepRate;
    }

    public final void setStepRate(int stepRate) {
        this.stepRate.set(stepRate);
    }

    private void constrainValue() {
        Integer value = getValue();
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
    public final ObjectProperty<Integer> valueProperty() {
        return value;
    }

    private final ObjectProperty<Integer> value = new SimpleObjectProperty<>(this, "value");

    public final Integer getValue() {
        return valueProperty().get();
    }

    public final void setValue(Integer value) {
        valueProperty().set(value);
    }

    /**
     * The minimum value that can be entered in this field.
     *
     * @return the minimum value.
     */
    public final IntegerProperty minimumValueProperty() {
        return minimumValue;
    }

    private final IntegerProperty minimumValue = new SimpleIntegerProperty(this, "minimumValue", 0);

    public final Integer getMinimumValue() {
        return minimumValueProperty().get();
    }

    public final void setMinimumValue(Integer minimumValue) {
        minimumValueProperty().set(minimumValue);
    }


    /**
     * The maximum value that can be entered in this field.
     *
     * @return the maximum value.
     */
    public final IntegerProperty maximumValueProperty() {
        return maximumValue;
    }

    private final IntegerProperty maximumValue = new SimpleIntegerProperty(this, "maximumValue");

    public final Integer getMaximumValue() {
        return maximumValueProperty().get();
    }

    public final void setMaximumValue(Integer maximumValue) {
        maximumValueProperty().set(maximumValue);
    }
}
