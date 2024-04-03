package com.dlsc.gemsfx;

import com.dlsc.gemsfx.daterange.DateRangePicker;
import com.dlsc.gemsfx.skins.EmailFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.css.*;
import javafx.css.converter.BooleanConverter;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A specialized control for entering an email address. The control validates
 * the entered text whenever the text property changes.
 */
public class EmailField extends Control {

    private static final boolean DEFAULT_SHOW_MAIL_ICON = true;
    private static final boolean DEFAULT_SHOW_VALIDATION_ICON = true;

    private static final PseudoClass VALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("valid");
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");
    private static final EmailValidator emailValidator = EmailValidator.getInstance();

    private final CustomTextField editor = new CustomTextField() {
        @Override
        public String getUserAgentStylesheet() {
            return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
        }
    };

    public EmailField() {
        getStyleClass().add("email-field");

        setFocusTraversable(false);

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                getEditor().requestFocus();
            }
        });

        valid.bind(Bindings.createBooleanBinding(() -> {
            if (isRequired()) {
                return emailValidator.isValid(getEmailAddress());
            }
            return StringUtils.isBlank(getEmailAddress()) || emailValidator.isValid(getEmailAddress());
        }, emailAddressProperty(), requiredProperty()));

        updateValidPseudoClass(false);

        valid.getReadOnlyProperty().addListener((ob, ov, newValue) -> updateValidPseudoClass(newValue));
    }

    public EmailField(String emailAddress) {
        this();
        setEmailAddress(emailAddress);
    }

    private void updateValidPseudoClass(Boolean isValid) {
        pseudoClassStateChanged(VALID_PSEUDO_CLASS, isValid);
        pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !isValid);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmailFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
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

    // Property for the tooltip text displayed when hovering over the icon indicating an invalid email address.
    private final StringProperty invalidText = new SimpleStringProperty(this, "invalidText", "Email address is invalid.");

    /**
     * Retrieves the tooltip text displayed when the email address validation fails and the user hovers over the invalid icon.
     *
     * @return Tooltip text for an invalid email address.
     */
    public final String getInvalidText() {
        return invalidText.get();
    }

    /**
     * Property for changing the tooltip text, which is displayed when hovering over the invalid icon after email address validation fails.
     *
     * @return The StringProperty for the tooltip text of an invalid email address.
     */
    public final StringProperty invalidTextProperty() {
        return invalidText;
    }

    /**
     * Sets the tooltip text that appears when the user hovers over the icon indicating the email address is invalid.
     *
     * @param invalidText The tooltip text to set for an invalid email address.
     */
    public final void setInvalidText(String invalidText) {
        this.invalidText.set(invalidText);
    }

    // Styleable property to control the visibility of the mail icon.
    private final StyleableBooleanProperty showMailIcon = new SimpleStyleableBooleanProperty(
            StyleableProperties.SHOW_MAIL_ICON, EmailField.this, "showMailIcon", DEFAULT_SHOW_MAIL_ICON);

    /**
     * Returns true if the mail icon is visible, otherwise false.
     *
     * @return The mail icon visibility
     */
    public final boolean isShowMailIcon() {
        return showMailIcon.get();
    }

    /**
     * Property for handling the mail icon visibility.
     */
    public final BooleanProperty showMailIconProperty() {
        return showMailIcon;
    }

    /**
     * Sets the visibility of the mail icon.
     *
     * @param showMailIcon true if the mail icon should be visible, otherwise false
     */
    public final void setShowMailIcon(boolean showMailIcon) {
        this.showMailIcon.set(showMailIcon);
    }

    // Styleable property to control the visibility of the validation icon.
    private final StyleableBooleanProperty showValidationIcon = new SimpleStyleableBooleanProperty(
            StyleableProperties.SHOW_VALIDATION_ICON, EmailField.this, "showValidationIcon", DEFAULT_SHOW_VALIDATION_ICON);

    /**
     * Returns true if the validation icon is visible, otherwise false.
     *
     * @return The validation icon visibility
     */
    public final boolean isShowValidationIcon() {
        return showValidationIcon.get();
    }

    /**
     * Property for handling the validation icon visibility.
     */
    public final BooleanProperty showValidationIconProperty() {
        return showValidationIcon;
    }

    /**
     * Sets the visibility of the validation icon.
     *
     * @param showValidationIcon true if the validation icon should be visible, otherwise false
     */
    public final void setShowValidationIcon(boolean showValidationIcon) {
        this.showValidationIcon.set(showValidationIcon);
    }

    private static class StyleableProperties {

        private static final CssMetaData<EmailField, Boolean> SHOW_MAIL_ICON = new CssMetaData<>(
                "-fx-show-mail-icon", BooleanConverter.getInstance(), DEFAULT_SHOW_MAIL_ICON) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(EmailField control) {
                return (StyleableProperty<Boolean>) control.showMailIconProperty();
            }

            @Override
            public boolean isSettable(EmailField control) {
                return !control.showMailIcon.isBound();
            }
        };

        private static final CssMetaData<EmailField, Boolean> SHOW_VALIDATION_ICON = new CssMetaData<>(
                "-fx-show-validation-icon", BooleanConverter.getInstance(), DEFAULT_SHOW_VALIDATION_ICON) {

            @Override
            public StyleableProperty<Boolean> getStyleableProperty(EmailField control) {
                return (StyleableProperty<Boolean>) control.showValidationIconProperty();
            }

            @Override
            public boolean isSettable(EmailField control) {
                return !control.showValidationIcon.isBound();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            final List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(Control.getClassCssMetaData());
            Collections.addAll(styleables, SHOW_MAIL_ICON, SHOW_VALIDATION_ICON);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    @Override
    protected List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return StyleableProperties.STYLEABLES;
    }
}
