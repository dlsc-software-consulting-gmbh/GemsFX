package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.EmailFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.controlsfx.control.textfield.CustomTextField;

/**
 * A specialized control for entering an email address. The control validates
 * the entered text whenever the text property changes.
 */
public class EmailField extends Control {

    private static final EmailValidator emailValidator = EmailValidator.getInstance();

    private final CustomTextField editor = new CustomTextField();

    public EmailField() {
        getStyleClass().add("email-field");

        valid.bind(Bindings.createBooleanBinding(() -> {
            if (isRequired()) {
                return emailValidator.isValid(getEmailAddress());
            }
            return StringUtils.isBlank(getEmailAddress()) || emailValidator.isValid(getEmailAddress());
        }, emailAddressProperty(), requiredProperty()));
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmailFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return EmailField.class.getResource("email-field.css").toExternalForm();
    }

    public final CustomTextField getEditor() {
        return editor;
    }

    // required

    private final BooleanProperty required = new SimpleBooleanProperty(this, "required", false);

    public final boolean isRequired() {
        return required.get();
    }

    public final BooleanProperty requiredProperty() {
        return required;
    }

    public final void setRequired(boolean required) {
        this.required.set(required);
    }

    // prompt text support

    private final StringProperty promptText = new SimpleStringProperty(this, "promptText");

    public final String getPromptText() {
        return promptText.get();
    }

    public final StringProperty promptTextProperty() {
        return promptText;
    }

    public final void setPromptText(String promptText) {
        this.promptText.set(promptText);
    }

    // email address support

    private final StringProperty emailAddress = new SimpleStringProperty(this, "emailAddress");

    public final String getEmailAddress() {
        return emailAddress.get();
    }

    public final StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public final void setEmailAddress(String emailAddress) {
        this.emailAddress.set(emailAddress);
    }

    // valid support

    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");

    public final boolean isValid() {
        return valid.get();
    }

    public final ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }
}
