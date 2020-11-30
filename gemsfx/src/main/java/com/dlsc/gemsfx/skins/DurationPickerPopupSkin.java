package com.dlsc.gemsfx.skins;

import com.dlsc.pickerfx.DurationPicker;

import javafx.scene.Node;
import javafx.scene.control.Skin;
import javafx.scene.layout.HBox;

public class DurationPickerPopupSkin implements Skin<DurationPickerPopup> {
    private final DurationPickerPopup popup;
    private final DurationPicker durationPicker;
    private final HBox hBox;

    public DurationPickerPopupSkin(DurationPickerPopup popup) {
        this.popup = popup;

        hBox = new HBox();
        hBox.getStyleClass().add("box");

        durationPicker = new DurationPicker();
        durationPicker.valueProperty().bindBidirectional(popup.durationProperty());
        durationPicker.maximumDurationProperty().bind(popup.maximumDurationProperty());
        durationPicker.minimumDurationProperty().bind(popup.minimumDurationProperty());

        hBox.setFillHeight(true);
        hBox.getChildren().add(durationPicker);
    }

    @Override
    public DurationPickerPopup getSkinnable() {
        return popup;
    }

    @Override
    public Node getNode() {
        return hBox;
    }

    @Override
    public void dispose() {
    }
}
