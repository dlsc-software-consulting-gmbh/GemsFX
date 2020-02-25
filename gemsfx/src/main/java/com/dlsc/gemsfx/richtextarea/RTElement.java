package com.dlsc.gemsfx.richtextarea;

import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RTElement<SELF extends RTElement> {

    private final ListProperty<String> styleClass = new SimpleListProperty<>(this, "styleClass", FXCollections.observableArrayList());

    public ObservableList<String> getStyleClass() {
        return styleClass.get();
    }

    public ListProperty<String> styleClassProperty() {
        return styleClass;
    }

    public void withStyleClasses(String... styleClass) {
        this.styleClass.setAll(styleClass);
    }

    private final StringProperty id = new SimpleStringProperty(this, "id");

    public String getId() {
        return id.get();
    }

    public StringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }
}
