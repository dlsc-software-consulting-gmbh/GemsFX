package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker;
import com.dlsc.gemsfx.TimePicker.Format;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

import java.time.LocalTime;
import java.util.Objects;

public class TimePickerPopup extends HBox {

    private final ListView<Integer> hourListView = new ListView<>();
    private final ListView<Integer> minuteListView = new ListView<>();
    private final ListView<Integer> secondListView = new ListView<>();
    private final ListView<Integer> millisecondListView = new ListView<>();
    private final TimePicker timePicker;

    public TimePickerPopup(TimePicker timePicker) {
        this.timePicker = timePicker;
        
        getStyleClass().add("time-picker-popup");

        hourListView.getStyleClass().addAll("time-list-view", "hour-list");
        hourListView.setCellFactory(listView -> new HourCell());
        hourListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newHour = hourListView.getSelectionModel().getSelectedItem();
            if (newHour != null) {
                LocalTime time = timePicker.getTime();
                if (time != null) {
                    timePicker.setTime(LocalTime.of(newHour, time.getMinute(), time.getSecond(), time.getNano()));
                } else {
                    timePicker.setTime(LocalTime.of(newHour, 0, 0, 0));
                }
            }
        });

        minuteListView.getStyleClass().addAll("time-list-view", "minute-list");
        minuteListView.setCellFactory(listView -> new MinuteCell());
        minuteListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newMinute = minuteListView.getSelectionModel().getSelectedItem();
            if (newMinute != null) {
                LocalTime time = timePicker.getTime();
                if (time != null) {
                    timePicker.setTime(LocalTime.of(time.getHour(), newMinute, time.getSecond(), time.getNano()));
                } else {
                    timePicker.setTime(LocalTime.of(0, newMinute, 0, 0));
                }
            }
        });

        secondListView.setVisible(false);
        secondListView.setManaged(false);
        secondListView.getStyleClass().addAll("time-list-view", "second-list");
        secondListView.setCellFactory(listView -> new SecondCell());
        secondListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newSecond = secondListView.getSelectionModel().getSelectedItem();
            if (newSecond != null) {
                LocalTime time = timePicker.getTime();
                if (time != null) {
                    timePicker.setTime(LocalTime.of(time.getHour(), time.getMinute(), newSecond, time.getNano()));
                } else {
                    timePicker.setTime(LocalTime.of(0, 0, newSecond, 0));
                }
            }
        });

        millisecondListView.setVisible(false);
        millisecondListView.setManaged(false);
        millisecondListView.getStyleClass().addAll("time-list-view", "millisecond-list");
        millisecondListView.setCellFactory(listView -> new MillisecondCell());
        millisecondListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newMillisecond = millisecondListView.getSelectionModel().getSelectedItem();
            if (newMillisecond != null) {
                LocalTime time = timePicker.getTime();
                if (time != null) {
                    timePicker.setTime(LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), millisecondToNano(newMillisecond)));
                } else {
                    timePicker.setTime(LocalTime.of(0, 0, 0, millisecondToNano(newMillisecond)));
                }
            }
        });

        timePicker.timeProperty().addListener(it -> updateListViewSelection());

        getChildren().addAll(hourListView, minuteListView, secondListView, millisecondListView);
        setMaxWidth(Region.USE_PREF_SIZE);

        InvalidationListener updateListener = it -> updateLists();
        timePicker.clockTypeProperty().addListener(updateListener);
        timePicker.stepRateInMinutesProperty().addListener(updateListener);
        timePicker.earliestTimeProperty().addListener(updateListener);
        timePicker.latestTimeProperty().addListener(updateListener);

        updateLists();

        timePicker.showingProperty().addListener(it -> initializePopupView());

        updateTimeUnit();
        
        timePicker.formatProperty().addListener(it -> {
            updateTimeUnit();
        });

        initializePopupView();
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(TimePicker.class.getResource("time-picker.css")).toExternalForm();
    }

    private void initializePopupView() {
        Platform.runLater(() -> {
            updateListViewSelection();
            hourListView.scrollTo(hourListView.getSelectionModel().getSelectedIndex());
            minuteListView.scrollTo(minuteListView.getSelectionModel().getSelectedIndex());
            secondListView.scrollTo(secondListView.getSelectionModel().getSelectedIndex());
            millisecondListView.scrollTo(millisecondListView.getSelectionModel().getSelectedIndex());
        });
    }

    private void updateTimeUnit() {
        Format format = timePicker.formatProperty().get();
        if (format == Format.HOURS_MINUTES) {
            updateSecondMillisecondView(false, false);
        } else if (format == Format.HOURS_MINUTES_SECONDS) {
            updateSecondMillisecondView(true, false);
        } else {
            updateSecondMillisecondView(true, true);
        }
    }

    private void updateSecondMillisecondView(boolean secondVisible, boolean millisecondVisible) {
        secondListView.setManaged(secondVisible);
        secondListView.setVisible(secondVisible);
        millisecondListView.setManaged(millisecondVisible);
        millisecondListView.setVisible(millisecondVisible);
    }

    private int nanoToMillisecond(int nano) {
        return Long.valueOf(java.util.concurrent.TimeUnit.NANOSECONDS.toMillis(nano)).intValue();
    }

    private int millisecondToNano(int millisecond) {
        return Long.valueOf(java.util.concurrent.TimeUnit.MILLISECONDS.toNanos(millisecond)).intValue();
    }

    private void updateListViewSelection() {
        LocalTime time = timePicker.getTime();
        if (time != null) {
            hourListView.getSelectionModel().select(Integer.valueOf(time.getHour()));
            minuteListView.getSelectionModel().select(Integer.valueOf(time.getMinute()));
            secondListView.getSelectionModel().select(Integer.valueOf(time.getSecond()));
            millisecondListView.getSelectionModel().select(nanoToMillisecond(time.getNano()));
        } else {
            hourListView.getSelectionModel().clearSelection();
            minuteListView.getSelectionModel().clearSelection();
            secondListView.getSelectionModel().clearSelection();
            millisecondListView.getSelectionModel().clearSelection();
        }
    }

    private void updateLists() {
        hourListView.getItems().clear();
        minuteListView.getItems().clear();
        secondListView.getItems().clear();
        millisecondListView.getItems().clear();


        // TODO: add am / pm support
        for (int hour = timePicker.getEarliestTime().getHour(); hour <= timePicker.getLatestTime().getHour(); hour++) {
            hourListView.getItems().add(hour);
        }

        for (int minute = 0; minute < 60; minute = minute + timePicker.getStepRateInMinutes()) {
            minuteListView.getItems().add(minute);
        }

        for (int second = 0; second < 60; second++) {
            secondListView.getItems().add(second);
        }

        for (int milli = 0; milli < 1000; milli++) {
            millisecondListView.getItems().add(milli);
        }
    }

    private boolean shouldDisable(Integer hour, Integer minute, Integer second, Integer millisecond) {
        if (hour != null && minute != null && second != null && millisecond != null) {
            LocalTime time = LocalTime.of(hour, minute, second, millisecondToNano(millisecond));
            time = time.plusSeconds(second);
            time = time.plusNanos(millisecondToNano(millisecond));
            return time.isAfter(timePicker.getLatestTime()) || time.isBefore(timePicker.getEarliestTime());
        }

        return false;
    }

    public abstract static class TimeCell extends ListCell<Integer> {

        public TimeCell() {
            getStyleClass().add("time-cell");

            Label label = new Label();
            label.getStyleClass().add("time-label");
            label.visibleProperty().bind(emptyProperty().not());
            label.textProperty().bind(textProperty());

            getPseudoClassStates().addListener((Observable it) -> {
                System.out.println("---------");
                getPseudoClassStates().forEach(System.out::println);
            });
            setGraphic(label);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }
    }

    private static class HourCell extends TimeCell {

        public HourCell() {
            getStyleClass().add("hour-cell");
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                setText(Integer.toString(item));
            } else {
                setText("");
            }
        }
    }

    private class MinuteCell extends TimeCell {

        public MinuteCell() {
            getStyleClass().add("minute-cell");

            disableProperty().bind(Bindings.createBooleanBinding(() -> {
                Integer hour = hourListView.getSelectionModel().getSelectedItem();
                Integer minute = getItem();
                Integer second = secondListView.getSelectionModel().getSelectedItem();
                Integer millisecond = millisecondListView.getSelectionModel().getSelectedItem();

                return shouldDisable(hour, minute, second, millisecond);
            }, hourListView.getSelectionModel().selectedItemProperty(), timePicker.earliestTimeProperty(), timePicker.latestTimeProperty(), itemProperty()));

        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                if (item < 10) {
                    setText("0" + item);
                } else {
                    setText(Integer.toString(item));
                }
            } else {
                setText("");
            }
        }
    }

    private class SecondCell extends TimeCell {

        public SecondCell() {
            getStyleClass().add("second-cell");

            disableProperty().bind(Bindings.createBooleanBinding(() -> {
                Integer hour = hourListView.getSelectionModel().getSelectedItem();
                Integer minute = minuteListView.getSelectionModel().getSelectedItem();
                Integer second = getItem();
                Integer millisecond = millisecondListView.getSelectionModel().getSelectedItem();

                return shouldDisable(hour, minute, second, millisecond);
            }, hourListView.getSelectionModel().selectedItemProperty(), timePicker.earliestTimeProperty(), timePicker.latestTimeProperty(), itemProperty()));

        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                if (item < 10) {
                    setText("0" + item);
                } else {
                    setText(Integer.toString(item));
                }
            } else {
                setText("");
            }
        }
    }

    private class MillisecondCell extends TimeCell {

        public MillisecondCell() {
            getStyleClass().add("millisecond-cell");
            disableProperty().bind(Bindings.createBooleanBinding(() -> {
                Integer hour = hourListView.getSelectionModel().getSelectedItem();
                Integer minute = minuteListView.getSelectionModel().getSelectedItem();
                Integer second = secondListView.getSelectionModel().getSelectedItem();
                Integer millisecond = getItem();

                return shouldDisable(hour, minute, second, millisecond);
            }, hourListView.getSelectionModel().selectedItemProperty(), timePicker.earliestTimeProperty(), timePicker.latestTimeProperty(), itemProperty()));
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                if (item < 10) {
                    setText("00" + item);
                }
                if (item < 100) {
                    setText("0" + item);
                } else {
                    setText(Integer.toString(item));
                }
            } else {
                setText("");
            }
        }
    }
}
