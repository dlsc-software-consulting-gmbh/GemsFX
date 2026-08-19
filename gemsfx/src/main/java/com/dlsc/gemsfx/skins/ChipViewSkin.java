package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.ChipView;

import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

/**
 * Skin for the {@link ChipView} control.
 * <p>
 * The skin renders the chip text and graphic and shows a close icon when the
 * control has a close handler.
 *
 * @param <T> the chip value type
 */
public class ChipViewSkin<T> extends GemsSkinBase<ChipView<T>> {

    /**
     * Creates a skin for the given chip view.
     *
     * @param chip the chip view rendered by this skin
     */
    public ChipViewSkin(ChipView<T> chip) {
        super(chip);

        Label label = new Label();
        label.textProperty().bind(chip.textProperty());
        label.graphicProperty().bind(chip.graphicProperty());
        label.contentDisplayProperty().bind(chip.contentDisplayProperty());

        FontIcon closeIcon = new FontIcon(MaterialDesign.MDI_CLOSE);
        StackPane.setAlignment(closeIcon, Pos.CENTER);

        StackPane iconWrapper = new StackPane(closeIcon);
        iconWrapper.getStyleClass().add("close-icon");
        iconWrapper.visibleProperty().bind(chip.onCloseProperty().isNotNull());
        iconWrapper.managedProperty().bind(chip.onCloseProperty().isNotNull());
        iconWrapper.setOnMouseClicked(evt -> chip.getOnClose().accept(chip.getValue()));

        HBox box = new HBox(label, iconWrapper);
        box.getStyleClass().add("chip-container");
        box.setAlignment(Pos.CENTER_LEFT);

        getChildren().setAll(box);
    }
}
