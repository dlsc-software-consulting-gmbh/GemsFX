package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;

public abstract class RTTextElement<SELF extends RTTextElement> extends RTElement<SELF> {

    private boolean subscript;

    private boolean superscript;

    private boolean italic;

    private boolean bold;

    private Color textFill = Color.BLACK;

    private final StringProperty text = new SimpleStringProperty(this, "text");

    public final String getText() {
        return text.get();
    }

    public final StringProperty textProperty() {
        return text;
    }

    public final void setText(String text) {
        this.text.set(text);
    }

    public final SELF withText(String text) {
        setText(text);
        return (SELF) this;
    }

    public final SELF withSubscript(boolean on) {
        subscript = on;
        return (SELF) this;
    }

    public final SELF withSuperscript(boolean on) {
        superscript = on;
        return (SELF) this;
    }

    public final SELF withItalic(boolean on) {
        italic = on;
        return (SELF) this;
    }

    public final SELF withBold(boolean on) {
        bold = on;
        return (SELF) this;
    }

    public final SELF withTextFill(Color color) {
        textFill = color;
        return (SELF) this;
    }

    public final boolean isSubscript() {
        return subscript;
    }

    public final boolean isSuperscript() {
        return superscript;
    }

    public final boolean isItalic() {
        return italic;
    }

    public final boolean isBold() {
        return bold;
    }

    public final Color getTextFill() {
        return textFill;
    }
}
