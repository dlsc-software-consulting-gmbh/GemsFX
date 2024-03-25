package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CalendarPicker;
import com.dlsc.gemsfx.CalendarView;
import com.dlsc.gemsfx.CalendarView.SelectionModel;
import com.dlsc.gemsfx.CustomComboBox;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public class CalendarPickerSkin extends ToggleVisibilityComboBoxSkin<CalendarPicker> {

    private CalendarView view;
    private final HBox box;
    private final TextField editor;
    private final StackPane arrowButton;

    public CalendarPickerSkin(CalendarPicker picker) {
        super(picker);

        picker.valueProperty().addListener(it -> {
            if (view != null) {
                view.setYearMonth(YearMonth.from(picker.getValue()));
            }
        });

        Region arrow = new Region();
        arrow.getStyleClass().add("arrow");

        arrowButton = new StackPane(arrow);
        arrowButton.setFocusTraversable(false);
        arrowButton.getStyleClass().add("arrow-button"); // using styles similar to combobox, for consistency
        arrowButton.addEventHandler(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        arrowButton.addEventHandler(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        arrowButton.addEventHandler(MouseEvent.MOUSE_EXITED, this::mouseExited);

        editor = picker.getEditor();
        HBox.setHgrow(editor, Priority.ALWAYS);

        box = new HBox();
        box.setFillHeight(true);
        box.getStyleClass().add("box");
        updateBox();

        getChildren().add(box);
        registerChangeListener(picker.buttonDisplayProperty(), o -> updateBox());
    }

    private void updateBox() {
        CustomComboBox.ButtonDisplay buttonDisplay = getSkinnable().getButtonDisplay();
        switch (buttonDisplay) {
            case LEFT:
                box.getChildren().setAll(arrowButton, editor);
                HBox.setHgrow(arrowButton, Priority.NEVER);
                break;
            case RIGHT:
                box.getChildren().setAll(editor, arrowButton);
                HBox.setHgrow(arrowButton, Priority.NEVER);
                break;
            case BUTTON_ONLY:
                box.getChildren().setAll(arrowButton);
                HBox.setHgrow(arrowButton, Priority.ALWAYS);
                break;
            case FIELD_ONLY:
                box.getChildren().setAll(editor);
                HBox.setHgrow(arrowButton, Priority.NEVER);
                break;
        }
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
