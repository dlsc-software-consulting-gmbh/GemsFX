package com.dlsc.gemsfx.skins;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class DurationPickerPopupSkin implements Skin<DurationPickerPopup> {
    private final ResourceBundle i18n = ResourceBundle.getBundle("duration-picker");

    private final DurationPickerPopup popup;
    private final HBox box;

    public DurationPickerPopupSkin(DurationPickerPopup popup) {
        this.popup = popup;

        popup.durationProperty().addListener(it -> updateListViewSelection());

        box = new HBox();
        box.getStyleClass().add("box");
        box.setMaxWidth(Region.USE_PREF_SIZE);

        popup.showingProperty().addListener(it -> {
            Platform.runLater(() -> {
                updateListViewSelection();
            });
        });

        InvalidationListener updateListener = it -> updateListViews();
        popup.getFields().addListener(updateListener);
        popup.minimumDurationProperty().addListener(updateListener);
        popup.minimumDurationProperty().addListener(updateListener);

        updateListViews();
    }

    private void updateListViewSelection() {
        Duration time = getSkinnable().getDuration();
        if (time != null) {
            listViewMap.entrySet().forEach(entry -> {
                ChronoUnit chronoUnit = entry.getKey();
                ListView<Integer> listView = entry.getValue();

                Integer value = null;
                Duration duration = getSkinnable().getDuration();

                switch (chronoUnit) {
                    case MILLIS:
                        value = duration.toMillisPart();
                        break;
                    case SECONDS:
                        value = duration.toSecondsPart();
                        break;
                    case MINUTES:
                        value = duration.toMinutesPart();
                        break;
                    case HOURS:
                        value = duration.toHoursPart();
                        break;
                    case DAYS:
                        value = (int) duration.toDaysPart();
                        break;
                }

                listView.getSelectionModel().select(value);
                listView.scrollTo(value);
            });
        }
    }

    private final Map<ChronoUnit, ListView<Integer>> listViewMap = new HashMap<>();

    private void updateListViews() {
        box.getChildren().clear();
        listViewMap.clear();

        getSkinnable().getFields().forEach(chronoUnit -> {
            ListView<Integer> unitListView = new ListView<>();
            listViewMap.put(chronoUnit, unitListView);

            unitListView.getStyleClass().add("unit-list-view");
            unitListView.setCellFactory(view -> new UnitCell());
            unitListView.getSelectionModel().selectedItemProperty().addListener(it -> {
                Integer value = unitListView.getSelectionModel().getSelectedItem();
                if (value != null) {

                    Duration duration = Duration.ZERO;

                    for (ChronoUnit unit : getSkinnable().getFields()) {
                        ListView<Integer> listView = listViewMap.get(unit);
                        Integer selectedItem = listView.getSelectionModel().getSelectedItem();
                        if (selectedItem != null) {
                            duration = duration.plus(selectedItem, unit);
                        }
                    }

                    getSkinnable().setDuration(duration);
                }
            });


            // TODO: i18n
            Label label = new Label();
            label.getStyleClass().add("header-label");

            switch (chronoUnit) {
                default:
                    label.setText(chronoUnit.name());
                case MILLIS:
                    label.setText(i18n.getString("popup.unit.title.millis"));
                    break;
                case SECONDS:
                    label.setText(i18n.getString("popup.unit.title.seconds"));
                    break;
                case MINUTES:
                    label.setText(i18n.getString("popup.unit.title.minutes"));
                    break;
                case HOURS:
                    label.setText(i18n.getString("popup.unit.title.hours"));
                    break;
                case DAYS:
                    label.setText(i18n.getString("popup.unit.title.days"));
                    break;
            }
            VBox.setVgrow(unitListView, Priority.ALWAYS);

            VBox vBox = new VBox(label, unitListView);

            box.getChildren().add(vBox);

            Long maxValue;

            switch (chronoUnit) {
                default:
                case DAYS:
                    Duration maximumDuration = getSkinnable().getMaximumDuration();
                    if (maximumDuration != null) {
                        maxValue = maximumDuration.toDays();
                    } else {
                        maxValue = 99L;
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
                for (int value = 0; value <= maxValue; value++) {
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
