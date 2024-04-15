package com.dlsc.gemsfx.skins;
import com.dlsc.unitfx.skins.CustomTextFieldSkin;
import com.dlsc.gemsfx.EnhancedPasswordField;
import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Set;

public abstract class EnhancedPasswordFieldSkin extends CustomTextFieldSkin {

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

        if (skinnable instanceof EnhancedPasswordField passwordField) {
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