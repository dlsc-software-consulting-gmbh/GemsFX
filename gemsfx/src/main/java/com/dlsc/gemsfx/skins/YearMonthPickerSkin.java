package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.YearMonthPicker;
import com.dlsc.gemsfx.YearMonthView;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class YearMonthPickerSkin extends ToggleVisibilityComboBoxSkin<YearMonthPicker> {

    private YearMonthView view;

    public YearMonthPickerSkin(YearMonthPicker picker) {
        super(picker);

        FontIcon calendarIcon = new FontIcon();
        calendarIcon.getStyleClass().add("edit-icon"); // using styles similar to combobox, for consistency

        StackPane editButton = new StackPane(calendarIcon);
        editButton.setFocusTraversable(false);
        editButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        editButton.getStyleClass().add("edit-button"); // using styles similar to combobox, for consistency
        editButton.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        editButton.addEventFilter(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        editButton.addEventFilter(MouseEvent.MOUSE_EXITED, this::mouseExited);
        HBox.setHgrow(picker.getEditor(), Priority.ALWAYS);

        HBox box = new HBox(picker.getEditor(), editButton);
        box.getStyleClass().add("box");

        getChildren().add(box);
    }

    protected Node getPopupContent() {
        if (view == null) {
            view = getSkinnable().getYearMonthView();
            view.valueProperty().bindBidirectional(getSkinnable().valueProperty());
            view.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    getSkinnable().hide();
                }
            });
        }

        return view;
    }
}
