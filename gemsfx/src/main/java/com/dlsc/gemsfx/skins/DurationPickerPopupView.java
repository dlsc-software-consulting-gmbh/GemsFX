package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.DurationPicker;
import javafx.scene.layout.HBox;

import java.util.Objects;

/**
 * Popup content view for the {@link DurationPicker} control.
 * <p>
 * The view embeds the PickerFX duration picker and binds it to the GemsFX
 * duration picker properties.
 */
public class DurationPickerPopupView extends HBox {

    /**
     * Creates popup content for the given duration picker.
     *
     * @param picker the duration picker that owns this popup content
     */
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
