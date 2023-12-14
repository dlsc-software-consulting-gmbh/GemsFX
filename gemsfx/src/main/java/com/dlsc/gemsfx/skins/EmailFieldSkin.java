package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.EmailField;
import javafx.scene.control.SkinBase;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import org.controlsfx.control.textfield.CustomTextField;
import org.kordamp.ikonli.javafx.FontIcon;

public class EmailFieldSkin extends SkinBase<EmailField> {

    public EmailFieldSkin(EmailField field) {
        super(field);

        Region emailGraphic = new Region();
        emailGraphic.getStyleClass().add("email-graphic");
        StackPane emailGraphicWrapper = new StackPane(emailGraphic);
        emailGraphicWrapper.getStyleClass().add("email-graphic-wrapper");

        FontIcon validationIcon = new FontIcon();

        StackPane validationIconWrapper = new StackPane(validationIcon);
        validationIconWrapper.getStyleClass().add("icon-wrapper");
        Tooltip.install(validationIconWrapper, new Tooltip("Email address is invalid"));
        validationIconWrapper.visibleProperty().bind(field.validProperty().not());
        validationIconWrapper.managedProperty().bind(field.validProperty().not());

        CustomTextField customTextField = field.getEditor();

        /*
         * Needed because custom text field brings its own user agent stylesheet. Not
         * really sure what the logic is behind this.
         */
        customTextField.getStylesheets().add(field.getUserAgentStylesheet());
        customTextField.textProperty().bindBidirectional(field.emailAddressProperty());
        customTextField.promptTextProperty().bind(field.promptTextProperty());
        customTextField.setRight(validationIconWrapper);
        customTextField.setLeft(emailGraphicWrapper);
        getChildren().setAll(customTextField);
    }
}
