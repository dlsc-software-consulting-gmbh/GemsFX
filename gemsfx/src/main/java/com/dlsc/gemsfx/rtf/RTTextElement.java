package com.dlsc.gemsfx.rtf;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public abstract class RTTextElement<SELF extends RTTextElement> extends RTElement<SELF> {

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
}
