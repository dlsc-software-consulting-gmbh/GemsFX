package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.EnhancedPasswordFieldSkin;
import com.dlsc.gemsfx.util.AccessibilityUtil;
import com.dlsc.gemsfx.util.EchoCharConverter;
import com.dlsc.gemsfx.util.UIUtil;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.PseudoClass;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.AccessibleRole;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A custom password field that enhances the standard {@link PasswordField} with additional features.
 * <p>
 * This component allows for the display of icons or nodes on both the left and right sides of the password field.
 * It also provides the capability to toggle the visibility of the password, allowing the password to be shown
 * as plain text or masked with a customizable echo character. The echo character and visibility can be styled
 * and controlled through CSS or programmatically.
 * <p>
 * Key features:
 * <ul>
 *   <li>Customizable echo character: The character used to mask the password can be customized.</li>
 *   <li>Toggle password visibility: Users can toggle between hiding and showing the password as plain text.</li>
 *   <li>Left and right nodes: Allows adding custom nodes (like buttons or icons) to either side of the field.</li>
 * </ul>
 *
 * Usage example:
 * <pre>
 * {@code
 * EnhancedPasswordField passwordField = new EnhancedPasswordField();
 * passwordField.setLeft(new ImageView(new Image("path/to/icon.png")));
 * passwordField.setRight(new Button("Show", e -> passwordField.setShowPassword(!passwordField.isShowPassword())));
 * }
 * </pre>
 *
 * @see PasswordField
 *
 * <p><b>CSS Styleable Properties:</b>
 * <table class="striped">
 *   <caption>CSS Properties</caption>
 *   <thead><tr><th>Property</th><th>Type</th><th>Description</th></tr></thead>
 *   <tbody>
 *     <tr><td>{@code -fx-echo-char}</td><td>{@code char}</td><td>Character used to mask the password</td></tr>
 *   </tbody>
 * </table>
 */
public class EnhancedPasswordField extends PasswordField {

    public static final char DEFAULT_ECHO_CHAR = '●';

    private static final String DEFAULT_STYLE_CLASS = "enhanced-password-field";
    private static final boolean DEFAULT_SHOW_PASSWORD = false;
    private static final PseudoClass SHOWING_PASSWORD_PSEUDO_CLASS = PseudoClass.getPseudoClass("showing-password");

    private final Logger LOG = Logger.getLogger(EnhancedPasswordField.class.getName());

    /**
     * Constructs a new enhanced password field.
     */
    public EnhancedPasswordField() {
        super();
        getStyleClass().add(DEFAULT_STYLE_CLASS);
        AccessibilityUtil.setRole(this, AccessibleRole.PASSWORD_FIELD);

        showPasswordProperty().addListener((obs, wasShowing, showing) -> pseudoClassStateChanged(SHOWING_PASSWORD_PSEUDO_CLASS, showing));
        pseudoClassStateChanged(SHOWING_PASSWORD_PSEUDO_CLASS, isShowPassword());

        //set right node
        Region rightIcon = new Region();
        rightIcon.getStyleClass().add("right-icon");

        StackPane rightWrapper = new StackPane(rightIcon);
        rightWrapper.getStyleClass().add("right-icon-wrapper");
        rightWrapper.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if (UIUtil.isClickOnNode(event)) {
                setShowPassword(!isShowPassword());
                event.consume();
            }
        });
        setRight(rightWrapper);
    }

    /**
     * Constructs a new enhanced password field with the given text.
     *
     * @param text the initial text
     */
    public EnhancedPasswordField(String text) {
        this();
        setText(text);
    }

    /**
     * Creates the default skin for this control.
     *
     * @return the default skin
     */
    @Override
    protected Skin<?> createDefaultSkin() {
        return new EnhancedPasswordFieldSkin(this) {
            /**
             * {@inheritDoc}
             *
             * @return the result
             */
            @Override
            public ObjectProperty<Node> leftProperty() {
                return EnhancedPasswordField.this.leftProperty();
            }

            /**
             * {@inheritDoc}
             *
             * @return the result
             */
            @Override
            public ObjectProperty<Node> rightProperty() {
                return EnhancedPasswordField.this.rightProperty();
            }
        };
    }

    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    /**
     * The node shown on the left side of the password field.
     *
     * @return the left property
     */
    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final Node getLeft() {
        return left.get();
    }

    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

    /**
     * The node shown on the right side of the password field.
     *
     * @return the right property
     */
    public final ObjectProperty<Node> rightProperty() {
        return right;
    }

    public final Node getRight() {
        return right.get();
    }

    public final void setRight(Node right) {
        rightProperty().set(right);
    }

    private BooleanProperty showPassword;

    /**
     * Indicates whether the password is currently shown in plain text.
     * <p>
     * This property can be used to toggle the visibility of the password. If set to {@code true},
     * the password will be shown in plain text. If set to {@code false}, the password will be
     * masked as usual.
     *
     * @return the show password property
     */
    public final BooleanProperty showPasswordProperty() {
        if (showPassword == null) {
            showPassword = new SimpleBooleanProperty(this, "showPassword", DEFAULT_SHOW_PASSWORD);
        }
        return showPassword;
    }

    public final boolean isShowPassword() {
        return showPassword == null ? DEFAULT_SHOW_PASSWORD : showPassword.get();
    }

    public final void setShowPassword(boolean showPassword) {
        showPasswordProperty().set(showPassword);
    }

    private ObjectProperty<Character> echoCharProperty;

    /**
     * The character used to mask the password when it is not shown in plain text.
     * <p>
     * Can be set via CSS using the {@code -fx-echo-char} property.
     * Valid values are: a single character (e.g. {@code '●'}).
     * The default value is {@code '●'}.
     * </p>
     *
     * @return the echo character property
     */
    public final ObjectProperty<Character> echoCharProperty() {
        if (echoCharProperty == null) {
            echoCharProperty = new StyleableObjectProperty<>(DEFAULT_ECHO_CHAR) {
                /**
                 * {@inheritDoc}
                 *
                 * @return the owning bean
                 */
                @Override
                public Object getBean() {
                    return EnhancedPasswordField.this;
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the property name
                 */
                @Override
                public String getName() {
                    return "echoChar";
                }

                /**
                 * {@inheritDoc}
                 *
                 * @return the CSS metadata for this property
                 */
                @Override
                public CssMetaData<? extends Styleable, Character> getCssMetaData() {
                    return StyleableProperties.ECHO_CHAR;
                }
            };
        }
        return echoCharProperty;
    }

    public final Character getEchoChar() {
        return echoCharProperty == null ? DEFAULT_ECHO_CHAR : echoCharProperty.get();
    }

    /**
     * Retrieves the echo character property value safely.
     * <p>
     * This method provides a safe way to access the echo character. It ensures that if the property
     * is not set or an exception occurs during retrieval, a default echo character will be returned.
     * This approach prevents the application from crashing due to unhandled exceptions and ensures
     * that there is always a valid character returned.
     * <p>
     * If the property value is {@code null} or if an error occurs (such as a {@link ClassCastException}
     * when the property value cannot be cast to a {@link Character}), this method logs a warning
     * and returns the default echo character.
     *
     * @return the character to be used as an echo character, or {@link #DEFAULT_ECHO_CHAR} if the
     * property is {@code null} or an exception occurs
     */
    public final Character getEchoCharSafe() {
        if (echoCharProperty == null) {
            return DEFAULT_ECHO_CHAR;
        }

        try {
            Character c = echoCharProperty.get();
            return c == null ? DEFAULT_ECHO_CHAR : c;
        } catch (Exception e) {
            String logMessage = String.format("Caught '%s' while converting value for '-fx-echo-char' style on %s[styleClass=%s]",
                    e, this.getClass().getSimpleName(), this.getClass().getName());
            LOG.log(Level.WARNING, logMessage);

            return DEFAULT_ECHO_CHAR;
        }
    }

    public final void setEchoChar(Character echoChar) {
        echoCharProperty().set(echoChar);
    }

    private static class StyleableProperties {
        private static final CssMetaData<EnhancedPasswordField, Character> ECHO_CHAR = new CssMetaData<>("-fx-echo-char",
                EchoCharConverter.getInstance(), DEFAULT_ECHO_CHAR) {
            /**
             * {@inheritDoc}
             *
             * @param control the control to inspect
             * @return true if the property can be styled
             */
            @Override
            public boolean isSettable(EnhancedPasswordField control) {
                return control.echoCharProperty == null || !control.echoCharProperty.isBound();
            }

            /**
             * {@inheritDoc}
             *
             * @param control the control to inspect
             * @return the styleable property
             */
            @Override
            public StyleableProperty<Character> getStyleableProperty(EnhancedPasswordField control) {
                return (StyleableProperty<Character>) control.echoCharProperty();
            }
        };

        private static final List<CssMetaData<? extends Styleable, ?>> STYLEABLES;

        static {
            List<CssMetaData<? extends Styleable, ?>> styleables = new ArrayList<>(PasswordField.getClassCssMetaData());
            styleables.add(ECHO_CHAR);
            STYLEABLES = Collections.unmodifiableList(styleables);
        }
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the control CSS metadata
     */
    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    /**
     * Returns the CSS metadata supported by this control.
     *
     * @return the class CSS metadata
     */
    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return EnhancedPasswordField.StyleableProperties.STYLEABLES;
    }

    /**
     * Returns the stylesheet used by this control.
     *
     * @return the user agent stylesheet
     */
    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EnhancedPasswordField.class.getResource("enhanced-password-field.css")).toExternalForm();
    }
}
