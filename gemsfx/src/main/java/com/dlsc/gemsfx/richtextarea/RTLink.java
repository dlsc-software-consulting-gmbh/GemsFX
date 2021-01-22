package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class RTLink extends RTTextElement<RTLink> {

    private RTLink() {
    }

    public static RTLink create(String text) {
        return new RTLink().withText(text);
    }

    public static RTLink create(String text, String url) {
        return create(text).withTarget(url);
    }

    public final RTLink withTarget(String url) {
        target.set(url);
        return this;
    }

    private final StringProperty target = new SimpleStringProperty(this, "target");

    public final String getTarget() {
        return target.get();
    }

    public final StringProperty targetProperty() {
        return target;
    }

    public final void setTarget(String target) {
        this.target.set(target);
    }
}
