package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;
import static com.dlsc.gemsfx.TimePicker.Format.HOURS_MINUTES_SECONDS;
import static com.dlsc.gemsfx.TimePicker.Format.HOURS_MINUTES_SECONDS_MILLIS;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalTime;

public class TimePickerSkin extends CustomComboBoxSkinBase<TimePicker> {

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    private final HourField hourField;

    private final MinuteField minuteField;

    private final SecondField secondField;

    private final MillisecondField millisecondField;

    private final Button editButton = new Button();

    private final HBox fieldsBox = new HBox();

    private TimePickerPopup popup;

    public TimePickerSkin(TimePicker picker) {
        super(picker);

        hourField = new HourField(picker);
        minuteField = new MinuteField(picker);
        secondField = new SecondField(picker);
        millisecondField = new MillisecondField(picker);

        hourField.setNextField(minuteField);
        minuteField.setNextField(secondField);
        minuteField.setPreviousField(hourField);
        secondField.setNextField(millisecondField);
        secondField.setPreviousField(minuteField);
        millisecondField.setPreviousField(secondField);

        editButton.getStyleClass().add("edit-button");
        editButton.setOnAction(evt -> picker.getOnShowPopup().accept(picker));
        editButton.setMaxHeight(Double.MAX_VALUE);
        editButton.setGraphic(new FontIcon());
        editButton.setFocusTraversable(false);
        editButton.visibleProperty().bind(picker.showPopupTriggerButtonProperty());
        editButton.managedProperty().bind(picker.showPopupTriggerButtonProperty());

        minuteField.stepRateProperty().bind(picker.stepRateInMinutesProperty());

        InvalidationListener updateFocusListener = it -> updateFocus();

        hourField.focusedProperty().addListener(updateFocusListener);
        minuteField.focusedProperty().addListener(updateFocusListener);
        secondField.focusedProperty().addListener(updateFocusListener);
        millisecondField.focusedProperty().addListener(updateFocusListener);
        editButton.focusedProperty().addListener(updateFocusListener);

        InvalidationListener buildViewListener = it -> buildView();

        picker.hoursSeparatorProperty().addListener(buildViewListener);
        picker.minutesSeparatorProperty().addListener(buildViewListener);
        picker.secondsSeparatorProperty().addListener(buildViewListener);

        buildView();

        picker.timeProperty().addListener(it -> updateFieldValues());
        updateFieldValues();

        picker.formatProperty().addListener(cl ->  {
            updateFormat();
        });
        
        updateEmptyPseudoClass();
        updateFormat();
    }
    
    private void updateFormat() {
        TimePicker picker = getSkinnable();
        if (null == picker.formatProperty().get()) {
                updateSecondsMillisecondsViewable(false, false);
            } else switch (picker.formatProperty().get()) {
                case HOURS_MINUTES_SECONDS:
                    updateSecondsMillisecondsViewable(true, false);
                    break;
                case HOURS_MINUTES_SECONDS_MILLIS:
                    updateSecondsMillisecondsViewable(true, true);
                    break;
                default:
                    updateSecondsMillisecondsViewable(false, false);
                    break;
            }
    }

    @Override
    protected Node getPopupContent() {
        if (popup == null) {
            popup = new TimePickerPopup(getSkinnable());
        }

        return popup;
    }

    private void updateSecondsMillisecondsViewable(boolean secondsVisible, boolean millisecondsVisible) {
        TimePicker timePicker = getSkinnable();

        Node minutesSeparator = timePicker.getMinutesSeparator();
        minutesSeparator.setVisible(secondsVisible);
        minutesSeparator.setManaged(secondsVisible);
        secondField.setVisible(secondsVisible);
        secondField.setManaged(secondsVisible);

        Node secondsSeparator = timePicker.getSecondsSeparator();
        secondsSeparator.setVisible(millisecondsVisible);
        secondsSeparator.setManaged(millisecondsVisible);
        millisecondField.setVisible(millisecondsVisible);
        millisecondField.setManaged(millisecondsVisible);
    }

    private void updateEmptyPseudoClass() {
        hourField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        minuteField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        secondField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        millisecondField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        getSkinnable().getHoursSeparator().pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        getSkinnable().getMinutesSeparator().pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        getSkinnable().getSecondsSeparator().pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
    }

    private void updateFocus() {
        boolean wasFocused = getSkinnable().getPseudoClassStates().stream().anyMatch(s -> s.getPseudoClassName().equals("focused"));
        if (!wasFocused) {
            getSkinnable().getProperties().put("CLEAR_ADJUSTED_TIME", "CLEAR_ADJUSTED_TIME");
        }

        getSkinnable().pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), minuteField.isFocused() || hourField.isFocused() || secondField.isFocused() || millisecondField.isFocused());
    }

    private void buildView() {
        Node hoursSeparator = getSkinnable().getHoursSeparator();
        Node minutesSeparator = getSkinnable().getMinutesSeparator();
        Node secondsSeparator = getSkinnable().getSecondsSeparator();

        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        fieldsBox.getChildren().addAll(hourField, hoursSeparator, minuteField, minutesSeparator, secondField, secondsSeparator, millisecondField);
        fieldsBox.setFillHeight(true);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);
        fieldsBox.getStyleClass().add("fields-box");

        HBox box = new HBox(fieldsBox, spacer, editButton);
        box.setFillHeight(true);
        box.getStyleClass().add("box");
        box.setAlignment(Pos.CENTER_LEFT);

        hoursSeparator.setOnMouseClicked(evt -> minuteField.requestFocus());
        minutesSeparator.setOnMouseClicked(evt -> secondField.requestFocus());
        secondsSeparator.setOnMouseClicked(evt -> millisecondField.requestFocus());

        getChildren().add(box);
    }

    private void updateFieldValues() {
        TimePicker picker = getSkinnable();
        LocalTime time = picker.getTime();

        if (time != null) {
            hourField.setValue(time.getHour());
            minuteField.setValue(time.getMinute());
            secondField.setValue(time.getSecond());
            millisecondField.setValue(time.getNano() / 1000000);
        } else {
            hourField.setValue(null);
            minuteField.setValue(null);
            secondField.setValue(null);
            millisecondField.setValue(null);
        }

        updateEmptyPseudoClass();
    }

    private class HourField extends DigitsField {

        public HourField(TimePicker picker) {
            super(picker, false);

            getStyleClass().add("hour");

            minimumValueProperty().bind(Bindings.createObjectBinding(() -> getSkinnable().getEarliestTime().getHour(), getSkinnable().earliestTimeProperty()));
            maximumValueProperty().bind(Bindings.createObjectBinding(() -> getSkinnable().getLatestTime().getHour(), getSkinnable().latestTimeProperty()));

            addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.getCode().equals(KeyCode.RIGHT)) {
                    minuteField.requestFocus();
                    evt.consume();
                }
            });

            valueProperty().addListener(it -> {
                Integer value = getValue();
                if (value != null) {
                    // constrain value
                    value = Math.min(value, getMaximumValue());
                    LocalTime time = getSkinnable().getTime();
                    if (time != null) {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(value, time.getMinute(), time.getSecond(), time.getNano()));
                    } else {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(value, 0, 0, 0));
                    }
                }
            });
        }
    }

    private class MinuteField extends DigitsField {

        public MinuteField(TimePicker picker) {
            super(picker, true);

            getStyleClass().add("minute");

            setMaximumValue(59);

            addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.getCode().equals(KeyCode.LEFT)) {
                    hourField.requestFocus();
                    evt.consume();
                }
            });

            valueProperty().addListener(it -> {
                Integer value = getValue();
                if (value != null) {
                    // constrain value
                    value = Math.min(value, getMaximumValue());
                    LocalTime time = getSkinnable().getTime();
                    if (time != null) {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(time.getHour(), value, time.getSecond(), time.getNano()));
                    } else {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(0, value, 0, 0));
                    }
                }
            });
        }
    }

    private class SecondField extends DigitsField {

        public SecondField(TimePicker picker) {
            super(picker, true);

            getStyleClass().add("second");

            setMaximumValue(59);

            addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.getCode().equals(KeyCode.LEFT)) {
                    minuteField.requestFocus();
                    evt.consume();
                }
            });

            valueProperty().addListener(it -> {
                Integer value = getValue();
                if (value != null) {
                    // constrain value
                    value = Math.min(value, getMaximumValue());
                    LocalTime time = getSkinnable().getTime();
                    if (time != null) {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(time.getHour(), time.getMinute(), value, time.getNano()));
                    } else {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(0, 0, value, 0));
                    }
                }
            });
        }
    }

    private class MillisecondField extends DigitsField {

        public MillisecondField(TimePicker picker) {
            super(picker, true, 3);

            getStyleClass().add("millisecond");

            setMaximumValue(999);

            addEventHandler(KeyEvent.KEY_PRESSED, evt -> {
                if (evt.getCode().equals(KeyCode.LEFT)) {
                    secondField.requestFocus();
                    evt.consume();
                }
            });

            valueProperty().addListener(it -> {
                Integer value = getValue();
                if (value != null) {
                    // constrain value
                    value = Math.min(value, getMaximumValue());
                    LocalTime time = getSkinnable().getTime();
                    if (time != null) {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), value * 1000000));
                    } else {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(0, 0, 0, value));
                    }
                }
            });
        }
    }
}
