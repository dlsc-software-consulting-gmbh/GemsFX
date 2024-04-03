package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import javafx.scene.layout.HBox;

import java.util.Objects;

public class DurationPickerPopupView extends HBox {

    public DurationPickerPopupView(DurationPicker picker) {
        getStyleClass().add("duration-picker-popup-view");

        com.dlsc.pickerfx.DurationPicker durationPicker = new com.dlsc.pickerfx.DurationPicker() {
            @Override
            public String getUserAgentStylesheet() {
                return Objects.requireNonNull(DurationPicker.class.getResource("duration-picker.css")).toExternalForm();
            }
        };

        durationPicker.valueProperty().bindBidirectional(picker.durationProperty());
        durationPicker.maximumDurationProperty().bind(picker.maximumDurationProperty());
        durationPicker.minimumDurationProperty().bind(picker.minimumDurationProperty());
        durationPicker.fieldsProperty().bind(picker.fieldsProperty());
        getChildren().add(durationPicker);

        getStylesheets().add(getUserAgentStylesheet());
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(DurationPicker.class.getResource("duration-picker.css")).toExternalForm();
    }
}
