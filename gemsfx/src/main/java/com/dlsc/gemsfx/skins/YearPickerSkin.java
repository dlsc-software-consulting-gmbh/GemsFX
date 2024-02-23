package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.YearPicker;
import com.dlsc.gemsfx.YearView;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class YearPickerSkin extends CustomComboBoxSkinBase<YearPicker> {

    private YearView yearView;

    public YearPickerSkin(YearPicker picker) {
        super(picker);

        FontIcon calendarIcon = new FontIcon();
        calendarIcon.getStyleClass().add("edit-icon"); // using styles similar to combobox, for consistency

        StackPane editButton = new StackPane(calendarIcon);
        editButton.setFocusTraversable(false);
        editButton.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        editButton.getStyleClass().add("edit-button"); // using styles similar to combobox, for consistency
        editButton.setOnMouseClicked(evt -> {
            picker.requestFocus();
            picker.show();
        });

        HBox.setHgrow(picker.getEditor(), Priority.ALWAYS);

        HBox box = new HBox(picker.getEditor(), editButton);
        box.getStyleClass().add("box");

        getChildren().add(box);
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
