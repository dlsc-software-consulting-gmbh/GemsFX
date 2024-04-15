package com.dlsc.gemsfx;

import com.dlsc.gemsfx.skins.EnhancedPasswordFieldSkin;
import com.dlsc.gemsfx.util.EchoCharConverter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableObjectProperty;
import javafx.css.StyleableProperty;
import javafx.scene.Node;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Skin;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

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
 */
public class EnhancedPasswordField extends PasswordField {

    public static final char DEFAULT_ECHO_CHAR = '●';
    private static final boolean DEFAULT_SHOW_PASSWORD = false;
    private static final String DEFAULT_STYLE_CLASS = "enhanced-password-field";
    private final Logger LOG = Logger.getLogger(EnhancedPasswordField.class.getName());

    public EnhancedPasswordField() {
        super();
        getStyleClass().add(DEFAULT_STYLE_CLASS);
    }

    public EnhancedPasswordField(String text) {
        this();
        setText(text);
    }

    /**
     * Creates a simple password field with an eye icon on the right side.
     * <p>
     * This method creates a simple password field with an eye icon on the right side. The eye icon
     * can be clicked to toggle the visibility of the password.
     *
     * @return a simple password field with an eye icon on the right side
     */
    public static EnhancedPasswordField createSimplePasswordField() {
        EnhancedPasswordField passwordField = new EnhancedPasswordField();

        //set right node
        FontIcon fontIcon = new FontIcon();
        fontIcon.iconCodeProperty().bind(passwordField.showPasswordProperty().map(it -> it ? MaterialDesign.MDI_EYE : MaterialDesign.MDI_EYE_OFF));

        StackPane right = new StackPane(fontIcon);
        right.getStyleClass().add("right-icon-wrapper");
        right.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                passwordField.setShowPassword(!passwordField.isShowPassword());
            }
        });

        passwordField.setRight(right);
        return passwordField;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        return new EnhancedPasswordFieldSkin(this) {
            @Override
            public ObjectProperty<Node> leftProperty() {
                return EnhancedPasswordField.this.leftProperty();
            }

            @Override
            public ObjectProperty<Node> rightProperty() {
                return EnhancedPasswordField.this.rightProperty();
            }
        };
    }

    /**
     * The node to be shown on the left side of the password field.
     * <p>
     * returns the node to be shown on the left side of the password field.
     */
    private final ObjectProperty<Node> left = new SimpleObjectProperty<>(this, "left");

    public final ObjectProperty<Node> leftProperty() {
        return left;
    }

    public final Node getLeft() {
        return left.get();
    }

    public final void setLeft(Node left) {
        leftProperty().set(left);
    }

    /**
     * The node to be shown on the right side of the password field.
     * <p>
     * returns the node to be shown on the right side of the password field.
     */
    private final ObjectProperty<Node> right = new SimpleObjectProperty<>(this, "right");

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

    public final ObjectProperty<Character> echoCharProperty() {
        if (echoCharProperty == null) {
            echoCharProperty = new StyleableObjectProperty<>(DEFAULT_ECHO_CHAR) {
                @Override
                public Object getBean() {
                    return EnhancedPasswordField.this;
                }

                @Override
                public String getName() {
                    return "echoChar";
                }

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
            @Override
            public boolean isSettable(EnhancedPasswordField control) {
                return control.echoCharProperty == null || !control.echoCharProperty.isBound();
            }

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

    @Override
    public List<CssMetaData<? extends Styleable, ?>> getControlCssMetaData() {
        return getClassCssMetaData();
    }

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return EnhancedPasswordField.StyleableProperties.STYLEABLES;
    }

    @Override
    public String getUserAgentStylesheet() {
        return Objects.requireNonNull(EnhancedPasswordField.class.getResource("enhanced-password-field.css")).toExternalForm();
    }

}
