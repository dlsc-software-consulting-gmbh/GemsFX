package com.dlsc.gemsfx.skins;
import com.dlsc.gemsfx.EnhancedPasswordField;

import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Set;

/**
 * Base skin for {@link EnhancedPasswordField}.
 * <p>
 * The skin replaces the text node binding installed by the text field skin so that the displayed text follows the
 * field's show-password and echo-character properties.
 */
public abstract class EnhancedPasswordFieldSkin extends CustomTextFieldSkin {

    /**
     * Creates a skin for the given enhanced password field.
     *
     * @param control the enhanced password field rendered by this skin
     */
    public EnhancedPasswordFieldSkin(EnhancedPasswordField control) {
        super(control);

        // find the text nodes
        Set<Node> nodes = control.lookupAll(".text");

        // find the textNode (not the prompt text node)
        Optional<Node> first = nodes.stream()
                .filter(node -> node instanceof Text && node.layoutXProperty().isBound())
                .findFirst();

        // bind the text property of the text node to the control's text property
        first.ifPresent(node -> {
            Text text = (Text) node;
            text.textProperty().unbind();
            text.textProperty().bind(new StringBinding() {
                {
                    bind(control.textProperty(), control.showPasswordProperty(), control.echoCharProperty());
                }

                @Override
                protected String computeValue() {
                    return maskText(control.textProperty().getValueSafe());
                }
            });
        });
    }

    @Override
    protected String maskText(String txt) {
        TextField skinnable = getSkinnable();
        int len = txt.length();
        if (skinnable == null) {
            return getDefaultMaskText(len);
        }

        if (skinnable instanceof EnhancedPasswordField) {
            EnhancedPasswordField passwordField = (EnhancedPasswordField) skinnable;
            if (passwordField.isShowPassword()) {
                return txt;
            }
            return String.valueOf(passwordField.getEchoCharSafe()).repeat(len);
        }
        return getDefaultMaskText(len);
    }

    private String getDefaultMaskText(int len) {
        return String.valueOf(EnhancedPasswordField.DEFAULT_ECHO_CHAR).repeat(len);
    }

}
