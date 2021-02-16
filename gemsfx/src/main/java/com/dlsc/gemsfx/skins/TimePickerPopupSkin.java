package com.dlsc.gemsfx.skins;

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

    public TimePickerPopupSkin(TimePickerPopup popup) {
        this.popup = popup;

        hourListView.getStyleClass().addAll("time-list-view", "hour-list");
        hourListView.setCellFactory(view -> new HourCell());
        hourListView.getSelectionModel().selectedItemProperty().addListener(it -> {
            Integer newHour = hourListView.getSelectionModel().getSelectedItem();
            if (newHour != null) {
                LocalTime time = popup.getTime();
                if (time != null) {
                    popup.setTime(LocalTime.of(newHour, time.getMinute()));
                } else {
                    popup.setTime(LocalTime.of(newHour, 0));
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
                    popup.setTime(LocalTime.of(time.getHour(), newMinute));
                } else {
                    popup.setTime(LocalTime.of(0, newMinute));
                }
            }
        });

        popup.timeProperty().addListener(it -> updateListViewSelection());

        box = new HBox(hourListView, minuteListView);
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
        }));
    }

    private void updateListViewSelection() {
        LocalTime time = getSkinnable().getTime();
        if (time != null) {
            hourListView.getSelectionModel().select(Integer.valueOf(time.getHour()));
            minuteListView.getSelectionModel().select(Integer.valueOf(time.getMinute()));
        } else {
            hourListView.getSelectionModel().clearSelection();
            minuteListView.getSelectionModel().clearSelection();
        }
    }

    private void updateLists() {
        hourListView.getItems().clear();
        minuteListView.getItems().clear();


        // TODO: add am / pm support
        for (int hour = getSkinnable().getEarliestTime().getHour(); hour <= getSkinnable().getLatestTime().getHour(); hour++) {
            hourListView.getItems().add(hour);
        }

        for (int minute = 0; minute < 60; minute = minute + getSkinnable().getStepRateInMinutes()) {
            minuteListView.getItems().add(minute);
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
                if (hour != null && minute != null) {
                    LocalTime time = LocalTime.of(hour, minute);
                    return time.isAfter(getSkinnable().getLatestTime()) || time.isBefore(getSkinnable().getEarliestTime());
                }

                return false;
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
}
