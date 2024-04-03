package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.EmailField;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.CustomTextField;

public class EmailFieldSkin extends SkinBase<EmailField> {

    public EmailFieldSkin(EmailField field) {
        super(field);

        CustomTextField customTextField = field.getEditor();

        Region mailIcon = new Region();
        mailIcon.getStyleClass().add("mail-icon");
        StackPane leftIconWrapper = new StackPane(mailIcon);
        leftIconWrapper.getStyleClass().add("mail-icon-wrapper");
        leftIconWrapper.managedProperty().bind(leftIconWrapper.visibleProperty());
        leftIconWrapper.visibleProperty().bind(field.showMailIconProperty());
        leftIconWrapper.visibleProperty().addListener(it -> customTextField.requestLayout());

        Region rightIcon = new Region();
        rightIcon.getStyleClass().add("validation-icon");
        StackPane rightIconWrapper = new StackPane(rightIcon);
        rightIconWrapper.getStyleClass().add("validation-icon-wrapper");
        rightIconWrapper.managedProperty().bind(rightIconWrapper.visibleProperty());
        rightIconWrapper.visibleProperty().bind(field.showValidationIconProperty().and(field.validProperty().not()));
        rightIconWrapper.visibleProperty().addListener(it -> customTextField.requestLayout());

        Tooltip invalidToolTip = new Tooltip();
        invalidToolTip.textProperty().bind(field.invalidTextProperty());
        updateTooltipVisibility(field.getInvalidText(), rightIconWrapper, invalidToolTip);
        field.invalidTextProperty().addListener((ob, ov, newValue) -> updateTooltipVisibility(newValue, rightIconWrapper, invalidToolTip));

        customTextField.textProperty().bindBidirectional(field.emailAddressProperty());
        customTextField.promptTextProperty().bind(field.promptTextProperty());
        customTextField.setLeft(leftIconWrapper);
        customTextField.setRight(rightIconWrapper);

        getChildren().setAll(customTextField);
    }

    private void updateTooltipVisibility(String invalidText, StackPane node, Tooltip invalidToolTip) {
        if (StringUtils.isEmpty(invalidText)) {
            Tooltip.uninstall(node, invalidToolTip);
        } else {
            Tooltip.install(node, invalidToolTip);
        }
    }

}
