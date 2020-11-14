package com.dlsc.gemsfx.skins;

import java.time.Duration;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public class DurationPickerPopupSkin implements Skin<DurationPickerPopup> {

    private final DurationPickerPopup popup;
    private final HBox box;

    public DurationPickerPopupSkin(DurationPickerPopup popup) {
        this.popup = popup;

        popup.durationProperty().addListener(it -> updateListViewSelection());

        box = new HBox();
        box.getStyleClass().add("box");
        box.setMaxWidth(Region.USE_PREF_SIZE);

        InvalidationListener updateListener = it -> updateListViews();
        popup.getFields().addListener(updateListener);
        popup.minimumDurationProperty().addListener(updateListener);
        popup.minimumDurationProperty().addListener(updateListener);

        updateListViews();

        popup.showingProperty().addListener(it -> {
            Platform.runLater(() -> {
                updateListViewSelection();
            });
        });

        updateListViews();
    }

    private void updateListViewSelection() {
        Duration time = getSkinnable().getDuration();
        if (time != null) {
//            hourListView.getSelectionModel().select(Integer.valueOf(time.getHour()));
//            minuteListView.getSelectionModel().select(Integer.valueOf(time.getMinute()));
        }
    }

    private void updateListViews() {
        box.getChildren().clear();

        getSkinnable().getFields().forEach(chronoUnit -> {
            ListView<Integer> unitListView = new ListView<>();
            unitListView.getStyleClass().add("unit-list-view");
            unitListView.setCellFactory(view -> new UnitCell());
            unitListView.getSelectionModel().selectedItemProperty().addListener(it -> {
                Integer value = unitListView.getSelectionModel().getSelectedItem();
                if (value != null) {
                    Duration duration = popup.getDuration();
                    if (duration != null) {
                        //popup.setDuration(LocalTime.of(value, duration.getMinute()));
                    } else {
                        //popup.setDuration(LocalTime.of(value, 0));
                    }
                }
            });

            box.getChildren().add(unitListView);

            Long maxValue = null;

            switch (chronoUnit) {
                default:
                case DAYS:
                    Duration maximumDuration = getSkinnable().getMaximumDuration();
                    if (maximumDuration != null) {
                        maxValue = maximumDuration.toDays();
                    }
                    break;
                case HOURS:
                    maxValue = 23L;
                    break;
                case MINUTES:
                case SECONDS:
                    maxValue = 59L;
                    break;
                case MILLIS:
                    maxValue = 999L;
                    break;
            }

            if (maxValue != null) {
                for (int value = 0; value < maxValue; value++) {
                    unitListView.getItems().add(value);
                }
            }
        });
    }

    @Override
    public DurationPickerPopup getSkinnable() {
        return popup;
    }

    @Override
    public Node getNode() {
        return box;
    }

    @Override
    public void dispose() {
    }

    private class UnitCell extends ListCell<Integer> {

        private Label label = new Label();

        public UnitCell() {
            getStyleClass().add("unit-cell");
            label.getStyleClass().add("unit-label");
            label.visibleProperty().bind(emptyProperty().not());
            label.textProperty().bind(textProperty());

            setGraphic(label);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
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
}
