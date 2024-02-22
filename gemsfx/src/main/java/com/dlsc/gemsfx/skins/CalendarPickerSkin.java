package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class CalendarPickerSkin extends CustomComboBoxSkinBase<CalendarPicker> {

    private CalendarView view;

    public CalendarPickerSkin(CalendarPicker picker) {
        super(picker);

        picker.valueProperty().addListener(it -> {
            if (view != null) {
                view.setYearMonth(YearMonth.from(picker.getValue()));
            }
        });

        Region arrow = new Region();
        arrow.getStyleClass().add("arrow");

        StackPane arrowButton = new StackPane(arrow);
        arrowButton.setFocusTraversable(false);
        arrowButton.getStyleClass().add("arrow-button"); // using styles similar to combobox, for consistency
        arrowButton.setOnMouseClicked(evt -> picker.show());
        HBox.setHgrow(picker.getEditor(), Priority.ALWAYS);

        HBox box = new HBox(picker.getEditor(), arrowButton);
        box.setFillHeight(true);
        box.getStyleClass().add("box");

        getChildren().add(box);
    }

    protected Node getPopupContent() {
        if (view == null) {
            CalendarPicker picker = getSkinnable();
            view = picker.getCalendarView();
            LocalDate pickerValue = picker.getValue();
            if (pickerValue != null) {
                view.setYearMonth(YearMonth.from(pickerValue));
                view.getSelectionModel().select(pickerValue);
            }
            view.setFocusTraversable(false); // keep the picker focused / blue border
            view.selectionModelProperty().addListener((obs, oldModel, newModel) -> bindSelectionModel(oldModel, newModel));
            bindSelectionModel(null, view.getSelectionModel());
        }

        return view;
    }

    private final ChangeListener<LocalDate> localDateChangeListener = (obs, oldValue, newValue) -> {
        if (!Objects.equals(oldValue, newValue)) {
            getSkinnable().hide();
        }
    };

    private final WeakChangeListener<LocalDate> weakLocalDateChangeListener = new WeakChangeListener<>(localDateChangeListener);

    private void bindSelectionModel(SelectionModel oldModel, SelectionModel newModel) {
        if (oldModel != null) {
            oldModel.selectedDateProperty().unbindBidirectional(getSkinnable().valueProperty());
            oldModel.selectedDateProperty().removeListener(weakLocalDateChangeListener);
        }

        if (newModel != null) {
            newModel.setSelectionMode(SelectionModel.SelectionMode.SINGLE_DATE);
            newModel.selectedDateProperty().bindBidirectional(getSkinnable().valueProperty());
            newModel.selectedDateProperty().addListener(weakLocalDateChangeListener);
        }
    }
}
