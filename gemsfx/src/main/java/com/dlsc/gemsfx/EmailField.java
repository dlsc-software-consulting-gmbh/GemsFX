package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.EmailFieldSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.SimpleStyleableBooleanProperty;
import javafx.css.Styleable;
import javafx.css.StyleableBooleanProperty;
import javafx.css.StyleableProperty;
import javafx.css.converter.BooleanConverter;
import javafx.scene.control.Control;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Skin;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.controlsfx.control.textfield.CustomTextField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 * EmailField is a custom control for inputting and validating email addresses.
 * It provides the following functionalities:
 * <p>
 *     <ul>
 *          <li>Automatic email domain suggestions to enhance user experience.</li>
 *          <li>Email address format validation to ensure input validity.</li>
 *          <li>Customizable properties to control the visibility of user interface elements, such as mail and validation icons, according to specific user interface requirements.</li>
 *     </ul>
 * </p>
 */
public class EmailField extends Control {

    private static final boolean DEFAULT_SHOW_MAIL_ICON = true;
    private static final boolean DEFAULT_SHOW_VALIDATION_ICON = true;
    private static final boolean DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED = true;

    private static final PseudoClass VALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("valid");
    private static final PseudoClass INVALID_PSEUDO_CLASS = PseudoClass.getPseudoClass("invalid");
    private static final EmailValidator emailValidator = EmailValidator.getInstance();

    private final CustomTextField editor = new CustomTextField() {
        @Override
        public String getUserAgentStylesheet() {
            return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
        }
    };

    /**
     * Constructs a new email field.
     */
    public EmailField() {
        getStyleClass().add("email-field");

        setFocusTraversable(false);

        focusedProperty().addListener(it -> {
            if (isFocused()) {
                getEditor().requestFocus();
            }
        });

        emailAddress.addListener(it -> {
            if (StringUtils.isNotBlank(getEmailAddress())) {
                editor.setText(getEmailAddress());
            }
        });

        supportingMultipleAddresses.addListener((obs, oldV, newV) -> {
            if (newV) {
                if (StringUtils.isNotBlank(getEmailAddress())) {
                    getMultipleEmailAddresses().setAll(getEmailAddress());
                }
            } else {
                if (!getMultipleEmailAddresses().isEmpty()) {
                    setEmailAddress(getMultipleEmailAddresses().get(0));
                }
            }
        });

        valid.bind(Bindings.createBooleanBinding(() -> {
            List<String> addresses = new ArrayList<>();

            String text = editor.getText();

            if (isSupportingMultipleAddresses()) {
                if (isRequired() && StringUtils.isBlank(text)) {
                    return false;
                } else {
                    StringTokenizer st = new StringTokenizer(text, ",");

                    while (st.hasMoreTokens()) {
                        String token = st.nextToken().trim();
                        if (!emailValidator.isValid(token)) {
                            getMultipleEmailAddresses().setAll(addresses);
                            return false;
                        }

                        addresses.add(token);
                    }

                    getMultipleEmailAddresses().setAll(addresses);

                    return true;
                }
            } else {
                boolean valid;
                if (isRequired()) {
                    valid = emailValidator.isValid(text);
                } else {
                    valid = StringUtils.isBlank(text) || emailValidator.isValid(text);
                }

                if (valid) {
                    setEmailAddress(text);
                } else {
                    setEmailAddress(null);
                }

                return valid;
            }
        }, editor.textProperty(), requiredProperty()));

        updateValidPseudoClass(false);

        valid.getReadOnlyProperty().addListener((ob, ov, newValue) -> updateValidPseudoClass(newValue));
    }

    /**
     * Constructs a new email field with the given initial email address.
     */
    public EmailField(String emailAddress) {
        this();
        setEmailAddress(emailAddress);
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EmailFieldSkin(this);
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EmailField.class.getResource("email-field.css")).toExternalForm();
    }

    private void updateValidPseudoClass(Boolean isValid) {
        pseudoClassStateChanged(VALID_PSEUDO_CLASS, isValid);
        pseudoClassStateChanged(INVALID_PSEUDO_CLASS, !isValid);
    }

    /**
     * Returns the text field used for editing purposes.
     *
     * @return the editor text field
     */
    public final CustomTextField getEditor() {
        return editor;
    }

    // domainList

    private final ListProperty<String> domainList = new SimpleListProperty<>(this, "domainList",
            FXCollections.observableArrayList("gmail.com", "yahoo.com", "outlook.com", "hotmail.com",
                    "icloud.com", "aol.com", "mail.com", "protonmail.com", "gmx.com", "zoho.com", "qq.com",
                    "163.com", "126.com", "yeah.net", "msn.com", "live.com", "me.com"));

    public final ObservableList<String> getDomainList() {
        return domainList.get();
    }

    /**
     * Stores a list of known domains that are often used for email addreses, e.g. gmail.com or outlook.com.
     *
     * @return list of known domains
     */
    public final ListProperty<String> domainListProperty() {
        return domainList;
    }

    public final void setDomainList(ObservableList<String> domainList) {
        this.domainList.set(domainList);
    }

    // multiple addresses

    // autoDomainCompletionEnabled

    private BooleanProperty autoDomainCompletionEnabled;

    public final boolean getAutoDomainCompletionEnabled() {
        return autoDomainCompletionEnabled == null ? DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED : autoDomainCompletionEnabled.get();
    }

    /**
     * Property for enabling or disabling the auto-completion of email domains.
     *
     * @return The BooleanProperty representing the state of auto domain completion.
     */
    public final BooleanProperty autoDomainCompletionEnabledProperty() {
        if (autoDomainCompletionEnabled == null) {
            autoDomainCompletionEnabled = new SimpleBooleanProperty(this, "autoDomainCompletionEnabled", DEFAULT_AUTO_DOMAIN_COMPLETION_ENABLED);
        }
        return autoDomainCompletionEnabled;
    }

    public final void setAutoDomainCompletionEnabled(boolean autoDomainCompletionEnabled) {
        autoDomainCompletionEnabledProperty().set(autoDomainCompletionEnabled);
    }

    // domainListCellFactory

    private ObjectProperty<Callback<ListView<String>, ListCell<String>>> domainListCellFactory;

    public final Callback<ListView<String>, ListCell<String>> getDomainListCellFactory() {
        return domainListCellFactory == null ? null : domainListCellFactory.get();
    }

    /**
     * Returns the property for the domain list cell factory.
     * This property can be used to customize the rendering of the domain suggestions in the ListView.
     *
     * @return The ObjectProperty representing the domain list cell factory.
     */
    public final ObjectProperty<Callback<ListView<String>, ListCell<String>>> domainListCellFactoryProperty() {
        if (domainListCellFactory == null) {
            domainListCellFactory = new SimpleObjectProperty<>(this, "domainListCellFactory");
        }
        return domainListCellFactory;
    }

    public final void setDomainListCellFactory(Callback<ListView<String>, ListCell<String>> cellFactory) {
        domainListCellFactoryProperty().set(cellFactory);
    }

    // required

    private final BooleanProperty required = new SimpleBooleanProperty(this, "required", false);

    public final boolean isRequired() {
        return required.get();
    }

    /**
     * A flag signalling that this is a required field. This flag will be taken into account when
     * updating the state of the {@link #validProperty()}.
     *
     * @return the flag used to determine if the field is required
     */
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

    /**
     * The prompt text to display by the editor.
     *
     * @return the prompt text
     */
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

    /**
     * Stores a valid email address. This property will only be non-null if the user has entered
     * a valid email address. This property is only used if the field is configured for entering a
     * single address. If the field is configured for multiple email addresses then this field will
     * be unused and the address list can be found in {@link #multipleEmailAddressesProperty()}.
     *
     * @return the entered email address
     */
    public final StringProperty emailAddressProperty() {
        return emailAddress;
    }

    public final void setEmailAddress(String emailAddress) {
        this.emailAddress.set(emailAddress);
    }

    // multiple address support

    private final BooleanProperty supportingMultipleAddresses = new SimpleBooleanProperty(this, "supportingMultipleAddresses");

    public final boolean isSupportingMultipleAddresses() {
        return supportingMultipleAddresses.get();
    }

    /**
     * A control flag used to determine if the user should be able to enter more than one email address
     * into the field.
     *
     * @return a flag used for controlling input behaviour (single vs. multiple email addresses)
     */
    public final BooleanProperty supportingMultipleAddressesProperty() {
        return supportingMultipleAddresses;
    }

    public final void setSupportingMultipleAddresses(boolean supportingMultipleAddresses) {
        this.supportingMultipleAddresses.set(supportingMultipleAddresses);
    }

    private final ListProperty<String> multipleEmailAddresses = new SimpleListProperty<>(this, "multipleEmailAddresses", FXCollections.observableArrayList());

    public final ObservableList<String> getMultipleEmailAddresses() {
        return multipleEmailAddresses.get();
    }

    /**
     * Stores the list of valid email addresses entered by the user. This list is only used when the
     * field supports entering multiple addresses.
     *
     * @return the list of valid email addresses
     * @see #supportingMultipleAddressesProperty()
     */
    public final ListProperty<String> multipleEmailAddressesProperty() {
        return multipleEmailAddresses;
    }

    public final void setMultipleEmailAddresses(ObservableList<String> multipleEmailAddresses) {
        this.multipleEmailAddresses.set(multipleEmailAddresses);
    }

    // valid support

    private final ReadOnlyBooleanWrapper valid = new ReadOnlyBooleanWrapper(this, "valid");

    public final boolean isValid() {
        return valid.get();
    }

    /**
     * A boolean flag used to indicate whether the field is currently in a valid state. The field is
     * in a valid state when the entered email addresses are all structurally valid (obviously this does
     * not mean that they do exist, only that they have the proper format).
     *
     * @return a boolean property signalling validity
     */
    public final ReadOnlyBooleanProperty validProperty() {
        return valid.getReadOnlyProperty();
    }

    // Property for the tooltip text displayed when hovering over the icon indicating an invalid email address.
    private final StringProperty invalidText = new SimpleStringProperty(this, "invalidText", "Invalid email address.");

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
