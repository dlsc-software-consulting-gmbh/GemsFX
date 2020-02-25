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

    public RTLink withTarget(String url) {
        this.target.set(url);
        return this;
    }

    private final StringProperty target = new SimpleStringProperty(this, "target");

    public String getTarget() {
        return target.get();
    }

    public StringProperty targetProperty() {
        return target;
    }

    public void setTarget(String target) {
        this.target.set(target);
    }
}
