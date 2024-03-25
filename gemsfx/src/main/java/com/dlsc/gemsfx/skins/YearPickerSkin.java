package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CustomComboBox;
import com.dlsc.gemsfx.YearPicker;
import com.dlsc.gemsfx.YearView;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class YearPickerSkin extends ToggleVisibilityComboBoxSkin<YearPicker> {

    private final HBox box;
    private final TextField editor;
    private final StackPane editButton;
    private YearView yearView;

    public YearPickerSkin(YearPicker picker) {
        super(picker);

        FontIcon calendarIcon = new FontIcon();
        calendarIcon.getStyleClass().add("edit-icon"); // using styles similar to combobox, for consistency

        editButton = new StackPane(calendarIcon);
        editButton.setFocusTraversable(false);
        editButton.setMaxWidth(Double.MAX_VALUE);
        editButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        editButton.getStyleClass().add("edit-button"); // using styles similar to combobox, for consistency
        editButton.addEventFilter(MouseEvent.MOUSE_RELEASED, this::mouseReleased);
        editButton.addEventFilter(MouseEvent.MOUSE_ENTERED, this::mouseEntered);
        editButton.addEventFilter(MouseEvent.MOUSE_EXITED, this::mouseExited);

        editor = picker.getEditor();
        HBox.setHgrow(editor, Priority.ALWAYS);

        box = new HBox();
        box.getStyleClass().add("box");
        updateBox();

        getChildren().add(box);
        registerChangeListener(picker.buttonDisplayProperty(), it -> updateBox());
    }

    private void updateBox() {
        CustomComboBox.ButtonDisplay buttonDisplay = getSkinnable().getButtonDisplay();
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

    @Override
    protected Node getPopupContent() {
        if (yearView == null) {
            yearView = getSkinnable().getYearView();
            yearView.valueProperty().bindBidirectional(getSkinnable().valueProperty());
            yearView.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (!Objects.equals(oldValue, newValue)) {
                    getSkinnable().hide();
                }
            });
        }
        return yearView;
    }
}
