package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;

import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalTime;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.SkinBase;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class TimePickerSkin extends SkinBase<TimePicker> {

    private static final PseudoClass EMPTY_PSEUDO_CLASS = PseudoClass.getPseudoClass("empty");

    private final HourField hourField;

    private final MinuteField minuteField;

    private final Button editButton = new Button();

    private final TimePickerPopup popup;

    public TimePickerSkin(TimePicker picker) {
        super(picker);

        hourField = new HourField(picker);
        minuteField = new MinuteField(picker);

        hourField.setNextField(minuteField);
        minuteField.setPreviousField(hourField);

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
        editButton.focusedProperty().addListener(updateFocusListener);

        popup = new TimePickerPopup();
        popup.timeProperty().bindBidirectional(picker.timeProperty());
        popup.stepRateInMinutesProperty().bind(picker.stepRateInMinutesProperty());
        popup.clockTypeProperty().bind(picker.clockTypeProperty());
        popup.earliestTimeProperty().bind(picker.earliestTimeProperty());
        popup.latestTimeProperty().bind(picker.latestTimeProperty());

        picker.separatorProperty().addListener(it -> buildView());
        buildView();

        picker.timeProperty().addListener(it -> updateFieldValues());
        updateFieldValues();

        picker.showingProperty().addListener(it -> {
            if (picker.isShowing()) {
                showPopup();
            } else {
                popup.hide();
            }
        });

        popup.setOnHidden(evt -> picker.getProperties().put("TIME_PICKER_POPUP", "TIME_PICKER_POPUP"));
        popup.addEventFilter(KeyEvent.KEY_PRESSED, evt -> {
            if (evt.getCode().equals(KeyCode.ESCAPE)) {
                popup.hide();
            }
        });

        updateEmptyPseudoClass();
    }

    private void updateEmptyPseudoClass() {
        hourField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        minuteField.pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        getSkinnable().getSeparator().pseudoClassStateChanged(EMPTY_PSEUDO_CLASS, getSkinnable().getTime() == null);
        System.out.println(hourField.getPseudoClassStates().contains(EMPTY_PSEUDO_CLASS));
    }

    private void showPopup() {
        TimePicker picker = getSkinnable();
        Bounds bounds = picker.getBoundsInLocal();
        Bounds screenBounds = picker.localToScreen(bounds);

        int x = (int) screenBounds.getMinX();
        int y = (int) screenBounds.getMinY();
        int height = (int) screenBounds.getHeight();

        popup.show(picker, x, y + height);
    }

    private void updateFocus() {
        boolean wasFocused = getSkinnable().getPseudoClassStates().stream().anyMatch(s -> s.getPseudoClassName().equals("focused"));
        if (!wasFocused) {
            getSkinnable().getProperties().put("CLEAR_ADJUSTED_TIME", "CLEAR_ADJUSTED_TIME");
        }

        getSkinnable().pseudoClassStateChanged(PseudoClass.getPseudoClass("focused"), minuteField.isFocused() || hourField.isFocused());
    }

    private void buildView() {
        Node separator = getSkinnable().getSeparator();

        Region spacer = new Region();
        spacer.getStyleClass().add("spacer");
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox fieldsBox = new HBox(hourField, separator, minuteField);
        fieldsBox.setFillHeight(true);
        fieldsBox.setAlignment(Pos.CENTER_LEFT);
        fieldsBox.getStyleClass().add("fields-box");

        HBox box = new HBox(fieldsBox, spacer, editButton);
        box.setFillHeight(true);
        box.getStyleClass().add("box");
        box.setAlignment(Pos.CENTER_LEFT);

        separator.setOnMouseClicked(evt -> minuteField.requestFocus());

        getChildren().add(box);
    }

    private void updateFieldValues() {
        TimePicker picker = getSkinnable();
        LocalTime time = picker.getTime();

        if (time != null) {
            hourField.setValue(time.getHour());
            minuteField.setValue(time.getMinute());
        } else {
            hourField.setValue(null);
            minuteField.setValue(null);
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
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(value, time.getMinute()));
                    } else {
                        getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(value, 0));
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
                    if (value != null) {
                        LocalTime time = getSkinnable().getTime();
                        if (time != null) {
                            getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(time.getHour(), value));
                        } else {
                            getSkinnable().getProperties().put("NEW_TIME", LocalTime.of(0, value));
                        }
                    }
                }
            });
        }
    }
}
