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

    public SELF withText(String text) {
        setText(text);
        return (SELF) this;
    }

    public SELF withSubscript(boolean on) {
        this.subscript = on;
        return (SELF) this;
    }

    public SELF withSuperscript(boolean on) {
        this.superscript = on;
        return (SELF) this;
    }

    public SELF withItalic(boolean on) {
        this.italic = on;
        return (SELF) this;
    }

    public SELF withBold(boolean on) {
        this.bold = on;
        return (SELF) this;
    }

    public SELF withTextFill(Color color) {
        this.textFill = color;
        return (SELF) this;
    }

    public boolean isSubscript() {
        return subscript;
    }

    public boolean isSuperscript() {
        return superscript;
    }

    public boolean isItalic() {
        return italic;
    }

    public boolean isBold() {
        return bold;
    }

    public Color getTextFill() {
        return textFill;
    }
}
