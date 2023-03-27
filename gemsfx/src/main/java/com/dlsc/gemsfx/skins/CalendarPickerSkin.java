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
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class CalendarPickerSkin extends CustomComboBoxSkinBase<CalendarPicker> {

    private CalendarView view;

    public CalendarPickerSkin(CalendarPicker picker) {
        super(picker);

        picker.setOnMouseClicked(evt -> picker.show());
        picker.valueProperty().addListener(it -> {
            if (view != null) {
                view.setYearMonth(YearMonth.from(picker.getValue()));
            }
        });

        FontIcon calendarIcon = new FontIcon();
        calendarIcon.getStyleClass().add("edit-icon"); // using styles similar to combobox, for consistency

        StackPane editButton = new StackPane(calendarIcon);
        editButton.setFocusTraversable(false);
        editButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        editButton.getStyleClass().add("edit-button"); // using styles similar to combobox, for consistency
        editButton.setOnMouseClicked(evt -> picker.show());

        HBox.setHgrow(picker.getEditor(), Priority.ALWAYS);

        HBox box = new HBox(picker.getEditor(), editButton);
        box.getStyleClass().add("box");

        getChildren().add(box);
    }

    protected Node getPopupContent() {
        if (view == null) {
            view = new CalendarView();
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
