package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.YearMonthPicker;
import com.dlsc.gemsfx.YearMonthView;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class YearMonthPickerSkin extends ToggleVisibilityComboBoxSkin<YearMonthPicker> {

    private final HBox box;
    private final TextField editor;
    private final StackPane editButton;
    private YearMonthView view;

    public YearMonthPickerSkin(YearMonthPicker picker) {
        super(picker);

        editor = picker.getEditor();
        HBox.setHgrow(editor, Priority.ALWAYS);

        FontIcon calendarIcon = new FontIcon();
        calendarIcon.getStyleClass().add("edit-icon"); // using styles similar to combobox, for consistency

        editButton = new StackPane(calendarIcon);
        editButton.setFocusTraversable(false);
        editButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        editButton.getStyleClass().add("edit-button"); // using styles similar to combobox, for consistency
        editButton.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        editButton.addEventFilter(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        editButton.addEventFilter(MouseEvent.MOUSE_EXITED, this::mouseExited);
        editButton.setMaxWidth(Double.MAX_VALUE);

        box = new HBox();
        box.getStyleClass().add("box");
        updateBox();

        getChildren().add(box);
        registerChangeListener(picker.buttonDisplayProperty(), it -> updateBox());
    }

    private void updateBox() {
        YearMonthPicker.ButtonDisplay buttonDisplay = getSkinnable().getButtonDisplay();
        switch (buttonDisplay) {
            case LEFT:
                box.getChildren().setAll(editButton, editor);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
            case RIGHT:
                box.getChildren().setAll(editor, editButton);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
            case BUTTON_ONLY:
                box.getChildren().setAll(editButton);
                HBox.setHgrow(editButton, Priority.ALWAYS);
                break;
            case FIELD_ONLY:
                box.getChildren().setAll(editor);
                HBox.setHgrow(editButton, Priority.NEVER);
                break;
        }
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
