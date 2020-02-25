package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public abstract class RTElement<SELF extends RTElement> {

    protected RTElement() {
    }

    private final ListProperty<String> styleClass = new SimpleListProperty<>(this, "styleClass", FXCollections.observableArrayList());

    public final ObservableList<String> getStyleClass() {
        return styleClass.get();
    }

    public final ListProperty<String> styleClassProperty() {
        return styleClass;
    }

    public final void withStyleClasses(String... styleClass) {
        this.styleClass.setAll(styleClass);
    }

    private final StringProperty id = new SimpleStringProperty(this, "id");

    public final String getId() {
        return id.get();
    }

    public final StringProperty idProperty() {
        return id;
    }

    public final void setId(String id) {
        this.id.set(id);
    }

    public final SELF withId(String id) {
        setId(id);
        return (SELF) this;
    }

    // style

    private final StringProperty style = new SimpleStringProperty(this, "style");

    public final String getStyle() {
        return style.get();
    }

    public final StringProperty styleProperty() {
        return style;
    }

    public final void setStyle(String style) {
        this.style.set(style);
    }

    public final SELF withStyle(String style) {
        setStyle(style);
        return (SELF) this;
    }
}
