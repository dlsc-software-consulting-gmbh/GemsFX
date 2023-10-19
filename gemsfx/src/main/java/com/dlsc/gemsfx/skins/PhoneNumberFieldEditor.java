package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.CountryCallingCode;
import com.dlsc.gemsfx.PhoneNumberField;
import javafx.beans.InvalidationListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.Objects;
import java.util.Optional;

public class PhoneNumberFieldEditor extends ListCell<CountryCallingCode> {

    private static final Image WORLD_ICON = new Image(Objects.requireNonNull(PhoneNumberField.class.getResource("phonenumberfield/world.png")).toExternalForm());

    private final TextField textField = new TextField();
    private final Label maskLabel = new Label();
    private final HBox buttonBox = new HBox();

    public PhoneNumberFieldEditor(PhoneNumberField field) {
        getStyleClass().add("editor");

        StackPane flagBox = new StackPane();
        flagBox.getStyleClass().add("flag-box");

        InvalidationListener updateFlag = it -> {
            CountryCallingCode callingCode = field.getSelectedCountryCode();
            Node flagIcon = Optional.ofNullable(callingCode).map(CountryCallingCode::getFlagView).orElse(new ImageView(WORLD_ICON));
            flagBox.getChildren().setAll(flagIcon);
        };

       field.selectedCountryCodeProperty().addListener(updateFlag);
        updateFlag.invalidated(null);

        Region arrow = new Region();
        arrow.getStyleClass().add("arrow");

        StackPane arrowButton = new StackPane();
        arrowButton.getStyleClass().add("arrow-button");
        arrowButton.getChildren().add(arrow);

        buttonBox.getStyleClass().add("button-box");
        buttonBox.getChildren().addAll(flagBox, arrowButton);

        maskLabel.getStyleClass().add("text-mask");

        textField.setTextFormatter(PhoneNumberMaskFormatter.configure(field, maskLabel));
    }

    public TextField getTextField() {
        return textField;
    }

    public HBox getButtonBox() {
        return buttonBox;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new SkinBase<>(this) {
            {
                getChildren().addAll(buttonBox, textField, maskLabel);
            }

            @Override
            protected void layoutChildren(double x, double y, double w, double h) {
                final double buttonWidth = snapSizeX(buttonBox.prefWidth(-1));
                buttonBox.resizeRelocate(x, y, buttonWidth, h);

                final double textFieldX = snapPositionX(x + buttonWidth);
                textField.resizeRelocate(textFieldX, y, w - buttonWidth, h);

                final Node textNode = textField.lookup(".text");
                final double maskX = snapPositionX(textFieldX + textNode.getLayoutBounds().getWidth());
                final double maskWidth = snapSizeX(Math.max(0, Math.min(maskLabel.prefWidth(-1), w - maskX)));
                maskLabel.resizeRelocate(maskX, y, maskWidth, h);
            }
        };
    }

}
