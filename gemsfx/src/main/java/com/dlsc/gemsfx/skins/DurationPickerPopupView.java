package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import javafx.scene.layout.HBox;

public class DurationPickerPopupView extends HBox {

    public DurationPickerPopupView(DurationPicker picker) {
        com.dlsc.pickerfx.DurationPicker durationPicker = new com.dlsc.pickerfx.DurationPicker();
        durationPicker.valueProperty().bindBidirectional(picker.durationProperty());
        durationPicker.maximumDurationProperty().bind(picker.maximumDurationProperty());
        durationPicker.minimumDurationProperty().bind(picker.minimumDurationProperty());
        durationPicker.fieldsProperty().bind(picker.fieldsProperty());
        getChildren().add(durationPicker);
    }
}
