package com.dlsc.gemsfx.skins;

import com.dlsc.gemsfx.EnhancedPasswordField;
import javafx.beans.binding.StringBinding;
import javafx.scene.Node;
import javafx.scene.text.Text;

import java.util.Optional;
import java.util.Set;

public abstract class EnhancedPasswordFieldSkin extends CustomTextFieldSkin {

    private final EnhancedPasswordField control;

    public EnhancedPasswordFieldSkin(EnhancedPasswordField control) {
        super(control);
        this.control = control;

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
        if (control == null || !control.isShowPassword()) {
            int n = txt.length();
            char echoChar = control != null ? control.getEchoCharSafe() : EnhancedPasswordField.DEFAULT_ECHO_CHAR;
            return String.valueOf(echoChar).repeat(n);
        }
        return txt;
    }

}