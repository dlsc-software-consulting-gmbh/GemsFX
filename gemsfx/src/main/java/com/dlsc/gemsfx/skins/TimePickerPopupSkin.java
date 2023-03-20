package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.TimePicker.TimeUnit;
import java.time.LocalTime;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class TimePickerPopupSkin implements Skin<TimePickerPopup> {

    private final TimePickerPopup popup;
    private final HBox box;
    private final ListView<Integer> hourListView = new ListView<>();
    private final ListView<Integer> minuteListView = new ListView<>();
    private final ListView<Integer> secondListView = new ListView<>();
    private final ListView<Integer> millisecondListView = new ListView<>();
    private TimeUnit timeUnit = TimeUnit.MINUTES;

    public TimePickerPopupSkin(TimePickerPopup popup) {
        this.popup = popup;

        hourListView.getStyleClass().addAll("time-list-view", "hour-list");
        hourListView.setCellFactory(view -> new HourCell());
        hourListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newHour = hourListView.getSelectionModel().getSelectedItem();
            if (newHour != null) {
                LocalTime time = popup.getTime();
                if (time != null) {
                    popup.setTime(LocalTime.of(newHour, time.getMinute(), time.getSecond(), time.getNano()));
                } else {
                    popup.setTime(LocalTime.of(newHour, 0, 0, 0));
                }
            }
        });

        minuteListView.getStyleClass().addAll("time-list-view", "minute-list");
        minuteListView.setCellFactory(view -> new MinuteCell());
        minuteListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newMinute = minuteListView.getSelectionModel().getSelectedItem();
            if (newMinute != null) {
                LocalTime time = popup.getTime();
                if (time != null) {
                    popup.setTime(LocalTime.of(time.getHour(), newMinute, time.getSecond(), time.getNano()));
                } else {
                    popup.setTime(LocalTime.of(0, newMinute, 0, 0));
                }
            }
        });
        
        secondListView.setVisible(false);
        secondListView.setManaged(false);
        secondListView.getStyleClass().addAll("time-list-view", "second-list");
        secondListView.setCellFactory(view -> new SecondCell());
        secondListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newSecond = secondListView.getSelectionModel().getSelectedItem();
            if (newSecond != null) {
                LocalTime time = popup.getTime();
                if (time != null) {
                    popup.setTime(LocalTime.of(time.getHour(), time.getMinute(), newSecond, time.getNano()));
                } else {
                    popup.setTime(LocalTime.of(0, 0, newSecond, 0));
                }
            }
        });   
        
        millisecondListView.setVisible(false);
        millisecondListView.setManaged(false);
        millisecondListView.getStyleClass().addAll("time-list-view", "millisecond-list");
        millisecondListView.setCellFactory(view -> new MillisecondCell());
        millisecondListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newMillisecond = millisecondListView.getSelectionModel().getSelectedItem();
            if (newMillisecond != null) {
                LocalTime time = popup.getTime();
                if (time != null) {
                    popup.setTime(LocalTime.of(time.getHour(), time.getMinute(), time.getSecond(), millisecondToNano(newMillisecond)));
                } else {
                    popup.setTime(LocalTime.of(0, 0, 0, millisecondToNano(newMillisecond)));
                }
            }
        });   

        popup.timeProperty().addListener(it -> updateListViewSelection());

        box = new HBox(hourListView, minuteListView, secondListView, millisecondListView);
        box.getStyleClass().add("box");
        box.setMaxWidth(Region.USE_PREF_SIZE);

        InvalidationListener updateListener = it -> updateLists();
        popup.clockTypeProperty().addListener(updateListener);
        popup.stepRateInMinutesProperty().addListener(updateListener);
        popup.earliestTimeProperty().addListener(updateListener);
        popup.latestTimeProperty().addListener(updateListener);

        updateLists();

        popup.showingProperty().addListener(it -> Platform.runLater(() -> {
            updateListViewSelection();
            hourListView.scrollTo(hourListView.getSelectionModel().getSelectedIndex());
            minuteListView.scrollTo(minuteListView.getSelectionModel().getSelectedIndex());
            secondListView.scrollTo(secondListView.getSelectionModel().getSelectedIndex());
            millisecondListView.scrollTo(millisecondListView.getSelectionModel().getSelectedIndex());
        }));
        updateTimeUnit();
        popup.timeUnitProperty().addListener(it -> {
            updateTimeUnit();
        });
    }
    
    private void updateTimeUnit() {
        timeUnit = popup.timeUnitProperty().get();
        System.out.println("time unit " + timeUnit);

        if (timeUnit == TimeUnit.MINUTES) {
            updateSecondMillisecondView(false, false);
        } else if (timeUnit == TimeUnit.SECONDS) {
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
        LocalTime time = getSkinnable().getTime();
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
        for (int hour = getSkinnable().getEarliestTime().getHour(); hour <= getSkinnable().getLatestTime().getHour(); hour++) {
            hourListView.getItems().add(hour);
        }

        for (int minute = 0; minute < 60; minute = minute + getSkinnable().getStepRateInMinutes()) {
            minuteListView.getItems().add(minute);
        }
        
        for (int second = 0; second < 60; second++) {
            secondListView.getItems().add(second);
        }    
        
        for (int milli = 0; milli < 1000; milli++) {
            millisecondListView.getItems().add(milli);
        }            
    }

    @Override
    public TimePickerPopup getSkinnable() {
        return popup;
    }

    @Override
    public Node getNode() {
        return box;
    }

    @Override
    public void dispose() {
    }
    
    private boolean shouldDisable(Integer hour, Integer minute, Integer second, Integer millisecond) {
            if (hour != null && minute != null && second != null && millisecond != null) {
                    LocalTime time = LocalTime.of(hour, minute, second, millisecondToNano(millisecond));
                    if (second != null) {
                        time = time.plusSeconds(second);
                    }
                    if (millisecond != null) {
                        time = time.plusNanos(millisecondToNano(millisecond));
                    }
                    return time.isAfter(getSkinnable().getLatestTime()) || time.isBefore(getSkinnable().getEarliestTime());
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
            }, hourListView.getSelectionModel().selectedItemProperty(), getSkinnable().earliestTimeProperty(), getSkinnable().latestTimeProperty(), itemProperty()));

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
            }, hourListView.getSelectionModel().selectedItemProperty(), getSkinnable().earliestTimeProperty(), getSkinnable().latestTimeProperty(), itemProperty()));
            
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
            }, hourListView.getSelectionModel().selectedItemProperty(), getSkinnable().earliestTimeProperty(), getSkinnable().latestTimeProperty(), itemProperty()));
        }

        @Override
        protected void updateItem(Integer item, boolean empty) {
            super.updateItem(item, empty);

            if (!empty && item != null) {
                if (item < 10) {
                    setText("00" + item);
                } if (item < 100) {
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
